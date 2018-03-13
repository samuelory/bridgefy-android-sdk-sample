# Bridgefy Nearby Sample

Basic app that shows nearby devices infomration

### Initialization

Permissions are checked preemptively:

```
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_devices);
    ButterKnife.bind(this);
 
    // initialize the DevicesAdapter and the RecyclerView
    devicesAdapter = new DevicesAdapter();
    devicesRecyclerView.setAdapter(devicesAdapter);
    devicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
 
    // check that we have Location permissions
    if (ContextCompat.checkSelfPermission(getApplicationContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        initializeBridgefy();
    } else {
        ActivityCompat.requestPermissions(this,
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 0);
    }
}
```
The Bridgefy SDK is started immediately after a successful registration: 

``` 
RegistrationListener registrationListener = new RegistrationListener() {
    @Override
    public void onRegistrationSuccessful(BridgefyClient bridgefyClient) {
        Log.i(TAG, "onRegistrationSuccessful: current userId is: " + bridgefyClient.getUserUuid());
        Log.i(TAG, "Device Rating " + bridgefyClient.getDeviceProfile().getRating());
        Log.i(TAG, "Device Evaluation " + bridgefyClient.getDeviceProfile().getDeviceEvaluation());

        // Start the Bridgefy SDK
        Bridgefy.start(messageListener,stateListener);
    }
 
    @Override
    public void onRegistrationFailed(int errorCode, String message) {
        Log.e(TAG, "onRegistrationFailed: failed with ERROR_CODE: " + errorCode + ", MESSAGE: " + message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DevicesActivity.this, "Bridgefy registration did not succeed.", Toast.LENGTH_LONG).show();
            }
        });
    }
};
```

### Information exchange

Information is exchanged as soon as a Device is connected:

```
StateListener stateListener = new StateListener() {
    @Override
    public void onDeviceConnected(Device device, Session session) {
        Log.i(TAG, "Device found: " + device.getUserId());
        sendMessage(device);
    }
    
    // ...
}
 
MessageListener messageListener = new MessageListener() {
    @Override
    public void onMessageReceived(Message message) {
        String s = message.getContent().get("manufacturer ") + " " + message.getContent().get("model");
        Log.d(TAG, "Message Received: " + message.getSenderId() + ", content: " + s);
        devicesAdapter.addDevice(s);
    }
    
    // ...
}
```

### Bridgefy SDK shutdown

The Bridgefy SDK is shut down when the Activity is destroyed:

```
@Override
protected void onDestroy() {
    super.onDestroy();
    Bridgefy.stop();
}
```
