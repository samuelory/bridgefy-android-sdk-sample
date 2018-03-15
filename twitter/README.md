# Bridgefy Offline Twitter Sample  

This sample allows you to broadcast tweets to your local current network and optionally relay them to an online Twitter account.

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

### TweetManager

Posts are handled by the TweetManager and sent via a Broadcast Message and sent to the TimelineActivity

```
Bridgefy.sendBroadcastMessage(
        Bridgefy.createMessage(tweet.toHashMap()));

if (tweet.isPosted())
    tweetListener.onTweetPosted(tweet);
```

When receiving a Tweet, we check if we should post it or if it's already published:

```
@Override
public void onBroadcastMessageReceived(Message message) {
    Log.d(TAG, "onBroadcastMessageReceived: " + message.getContent());
    // build the tweet from the incoming message
    Tweet tweet = Tweet.create(message);

    // if the tweet has already been posted online...
    if (tweet.isPosted() || mPostedTweets.contains(tweet)) {
        Log.v(TAG, "... Received posted Tweet.");

        // add it to our local list and update the views if it hasn't been added
        if (!mPostedTweets.contains(tweet)) {
            mPostedTweets.add(tweet);
            tweetListener.onTweetPosted(tweet);
        }
    }
    // if the tweet hasn't been posted online, post it and propagate the new object
    else {
        Log.v(TAG, "... Received unposted Tweet.");
        postTweet(tweet);
    }

    // pass the tweet on to the TweetListener
    tweetListener.onTweetReceived(tweet);
}
```


### Bridgefy SDK shutdown

The Bridgefy SDK is shut down when the Activity is destroyed. We catch an Exception in case the SDK wasn't started in the first place.

```
@Override
protected void onDestroy() {
    if (isFinishing()) {
        try {
            Bridgefy.stop();
        } catch (IllegalStateException ise) {
            ise.printStackTrace();
        }
    }
    super.onDestroy();
}
```
