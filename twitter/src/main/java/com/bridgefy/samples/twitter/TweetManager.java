package com.bridgefy.samples.twitter;

import android.os.AsyncTask;
import android.util.Log;

import com.bridgefy.samples.twitter.entities.Tweet;
import com.bridgefy.sdk.client.Bridgefy;
import com.bridgefy.sdk.client.Message;
import com.bridgefy.sdk.client.MessageListener;

import java.util.ArrayList;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

class TweetManager extends MessageListener {

    interface TweetListener {
        void onTweetReceived(Tweet tweet);
        void onTweetPosted(Tweet tweet);
    }

    private final String TAG = "TweetManager";

    private TweetListener tweetListener;
    private Twitter twitter;
    private boolean gateway = true;
    private String username;

    // This list only holds posted tweets. Unsuccessful tweets are simply broadcasted to the network
    // as offline tweets in hopes for peers to post them online and return the tweet status with the
    // an enabled posted flag
    // TODO add observer to update the TimelineActivity UI
    private ArrayList<Tweet> mPostedTweets = new ArrayList<>();


    TweetManager(String username, TweetListener tweetListener) {
        this.username = username;
        this.tweetListener = tweetListener;

        // configure the Twitter client
        String token  = "YOUR TOKEN HERE";
        String secret = "YOUR SECRET HERE";
        String consumerKey = "CONSUMER KEY";
        String consumerSecret = "CONSUMER SECRET";

        twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);
        twitter.setOAuthAccessToken(new AccessToken(token,secret));
    }

    void postTweet(Tweet tweet) {
        new PostTweetsAsyncTask().execute(tweet);
    }

    private class PostTweetsAsyncTask extends AsyncTask<Tweet, Void, Tweet> {
        @Override
        protected Tweet doInBackground(Tweet... tweets) {
            Tweet tweet = tweets[0];

            if (isGateway()) {
                try {
                    // post the status online
                    twitter4j.Status status = twitter.updateStatus("#" + tweet.getSender() + " " + tweet.getContent());
                    tweet.setPosted(true);

                    // add the tweet to our control list
                    mPostedTweets.add(tweet);
                    Log.d(TAG, "Tweet posted successfuly, status: http://twitter.com/statuses/" + status.getId());
                } catch (TwitterException te) {
                    Log.w(TAG, "Tweet wasn't posted: " + te.getMessage());
                }
            }

            return tweet;
        }

        @Override
        protected void onPostExecute(Tweet tweet) {
            super.onPostExecute(tweet);
            // broadcast the tweet only if it was ours or if post was successful
            if (tweet.getSender().equals(username) || tweet.isPosted()) {
                Log.v(TAG, tweet.toString());
                Bridgefy.sendBroadcastMessage(tweet.toHashMap());

                if (tweet.isPosted())
                    tweetListener.onTweetPosted(tweet);
            }
        }
    }

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



    public boolean isGateway() {
        return gateway;
    }

    public void setGateway(boolean gateway) {
        this.gateway = gateway;
    }
}
