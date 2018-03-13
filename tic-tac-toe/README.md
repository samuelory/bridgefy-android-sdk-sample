# Bridgefy Tic-Tac-Toe sample  

Simple Tic-Tac-Toe example that implements the Bridgefy SDK in order to have local, offline games.

### Initialization

Done via an inline declaration

```
// Initialize Bridgefy
Bridgefy.initialize(getApplicationContext(), new RegistrationListener() {
    @Override
    public void onRegistrationSuccessful(BridgefyClient bridgefyClient) {
        // Start Bridgefy
        startBridgefy();
    }

    @Override
    public void onRegistrationFailed(int errorCode, String message) {
        Toast.makeText(getBaseContext(), getString(R.string.registration_error),
                Toast.LENGTH_LONG).show();
    }
});
```

### BridgefyListener

All Bridgefy operations are handled in the BridgefyListener. For example, found & lost Device events are
propagated to the app via the Otto framework or handled internally:

```
private StateListener stateListener = new StateListener() {
    @Override
    public void onDeviceConnected(Device device, Session session) {
        // send a handshake to nearby devices
        device.sendMessage(mPlayer.toHashMap());
    }
    
    @Override
    public void onDeviceLost(Device device) {
        // let our components know that a device is no longer in range
        ottoBus.post(device);
    }
}
```

In this example, private messages serve as App handshakes, with each Player introducing themselves via a private message with other devices.
  
Move and Match events are propagated via Broadcast events so that other Players can view turns in real time. 

```
private MessageListener messageListener = new MessageListener() {
    @Override
    public void onMessageReceived(Message message) {
        // identify the type of incoming event
        Event.EventType eventType = extractType(message);
        switch (eventType) {
            case FIRST_MESSAGE:
                // recreate the Player object from the incoming message
                // post the found object to our activities via the Otto plugin
                ottoBus.post(Player.create(message));
                break;
        }
    }

    @Override
    public void onBroadcastMessageReceived(Message message) {
        // build a TicTacToe Move object from our incoming Bridgefy Message
        Event.EventType eventType = extractType(message);
        switch (eventType) {
            case MOVE_EVENT:
                final Move move = Move.create(message);
                // log
                Log.d(TAG, "Move received for matchId: " + move.getMatchId());
                Log.d(TAG, "... " + move.toString());

                // start the MatchActivity if we are on a Things Device
                if (isThingsDevice(context) && MatchActivity.matchId == null) {
                    // get a reference to our player object
                    int pos = MainActivity.playersAdapter.getPlayerPosition(move.getOtherUuid());
                    if (pos > -1) {
                        final Player player = MainActivity.playersAdapter.matchPlayers.get(pos).getPlayer();
                        if (player != null) {
                            // start the activity with the incoming player
                            Intent intent = new Intent(context, MatchActivity.class)
                                    .putExtra(Constants.INTENT_EXTRA_PLAYER, player.toString())
                                    .putExtra(Constants.INTENT_EXTRA_MOVE, move.toString());
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);

                            // repost to our newly created activity so it responds automatically
                            new Handler().postDelayed(new Runnable() {
                                public void run() {
                                    // post this event via the Otto plugin so our components can update their views
                                    ottoBus.post(move);
                                    // answer automatically if the current device is an Android Things device
                                    ottoBus.post(move.getMatchId());
                                }
                            }, 1500);
                        } else {
                            Log.w(TAG, "Incoming player unknown.");
                        }
                    } else {
                        Log.w(TAG, "Incoming player unknown.");
                    }
                }

                // post this event via the Otto plugin so our components can update their views
                ottoBus.post(move);

                // answer automatically if the current device is an Android Things device
                ottoBus.post(move.getMatchId());
                break;

            case REFUSE_MATCH:
                // recreate the RefuseMatch object from the incoming message
                // post the found object to our activities via the Otto plugin
                ottoBus.post(RefuseMatch.create(message));

                // let iPhone devices know we're available (not required on Android)
                Bridgefy.sendBroadcastMessage(Bridgefy.createMessage(
                        new Event<>(
                                Event.EventType.AVAILABLE,
                                this).toHashMap()));
                break;

            case AVAILABLE:
                Log.d(TAG, "AVAILABLE event not implemented.");
                break;

            default:
                Log.d(TAG, "Unrecognized Event received: " +
                        new Gson().toJson(message.getContent().toString()));
                break;
        }
    }
};
```

### Entity transmission

For this sample we have a lot of custom Entities. It's easy to send and recreate your POJO's using Gson:


```
public class Move {
    // ...
 
   public HashMap<String, Object> toHashMap() {
        return new Event<>(
                Event.EventType.MOVE_EVENT,
                this).toHashMap();
    }
    
    public static Move create(String json) {
        return new Gson().fromJson(json, Move.class);
    }
 
    public static Move create(Message message) {
        return new Gson().fromJson(
                new Gson().toJson(message.getContent().get("content")),
                Move.class);
    }
    
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
```


### Bridgefy SDK shutdown

The Bridgefy SDK is shut down only when the MainActivity is destroyed (and not during a configuration change)
We also unregister the Otto Bus and release BridgefyListener's resources.

```
@Override
protected void onDestroy() {
    // check that the activity is actually finishing before freeing resources
    if (isRegistered && isFinishing()) {
        // unregister the Otto bus and free up resources
        BridgefyListener.getOttoBus().unregister(this);
        BridgefyListener.release();

        // stop bridgefy operations
        Bridgefy.stop();
    }
 
    super.onDestroy();
}
```
