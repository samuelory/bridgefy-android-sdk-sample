# Bridgefy Chat Sample

Quick demonstration of how easy it is to exchange information with nearby devices.

### Bridgefy SDK Initialization

The API_KEY is included in the AndroidManifest so it's not provided during initialization.
The Bridgefy SDK is started immediately after a successful registration:

```
Bridgefy.initialize(getApplicationContext(), new RegistrationListener() {
    @Override
    public void onRegistrationSuccessful(BridgefyClient bridgefyClient) {
        // Start Bridgefy
        startBridgefy();
    }
    
    // ...
}
```

### App level Handshake

Devices introduce themselves after a successful connection:

```
StateListener stateListener = new StateListener() {
    @Override
    public void onDeviceConnected(final Device device, Session session) {
        Log.i(TAG, "onDeviceConnected: " + device.getUserId());
        // send our information to the Device
        HashMap<String, Object> map = new HashMap<>();
        map.put("device_name", Build.MANUFACTURER + " " + Build.MODEL);
        map.put("device_type", Peer.DeviceType.ANDROID.ordinal());
        device.sendMessage(map);
    }
    
    // ...
}
```

### Sending messages

Messages are sent within the `ChatActivity`, independently from the `MessageListener`:

```
// send text message to device(s)
if (conversationId.equals(BROADCAST_CHAT)) {
    // we put extra information in broadcast packets since they won't be bound to a session
    content.put("device_name", Build.MANUFACTURER + " " + Build.MODEL);
    content.put("device_type", Peer.DeviceType.ANDROID.ordinal());
    Bridgefy.sendBroadcastMessage(
            Bridgefy.createMessage(content),
            BFEngineProfile.BFConfigProfileLongReach);
} else {
    Bridgefy.sendMessage(
            Bridgefy.createMessage(conversationId, content),
            BFEngineProfile.BFConfigProfileLongReach);
}
```
