package com.bridgefy.samples.twitter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bridgefy.samples.twitter.entities.Tweet;
import com.bridgefy.sdk.client.Bridgefy;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.bridgefy.samples.twitter.IntroActivity.INTENT_USERNAME;


public class TimelineActivity extends AppCompatActivity implements TweetManager.TweetListener {

    private String TAG = "TimelineActivity";

    private String username;

    @BindView(R.id.txtTweet)
    EditText txtMessage;

    @BindView(R.id.gatewaySwitch)
    ToggleButton gatewaySwitch;

    TweetManager tweetManager;

    TweetsRecyclerViewAdapter tweetsAdapter =
            new TweetsRecyclerViewAdapter(new ArrayList<Tweet>());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        ButterKnife.bind(this);

        // recover our username and set the maximum tweet size
        username = getIntent().getStringExtra(INTENT_USERNAME);
        txtMessage.setFilters(new InputFilter[] { new InputFilter.LengthFilter(138 - username.length()) });

        // Configure the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        gatewaySwitch.setChecked(true);

        // configure the recyclerview
        RecyclerView tweetsRecyclerView = findViewById(R.id.tweet_list);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        tweetsRecyclerView.setLayoutManager(mLinearLayoutManager);
        tweetsRecyclerView.setAdapter(tweetsAdapter);

        // Set the Bridgefy MessageListener
        Log.d(TAG, "Setting new State and Message Listeners");
        Bridgefy.setMessageListener(tweetManager = new TweetManager(username, this));
    }


    /**
     *      ACTION AND INTERFACE METHODS
     */
    @OnClick(R.id.gatewaySwitch)
    public void onGatewaySwitched(ToggleButton gatewaySwitch) {
        Log.d(TAG, "Internet relaying toggled: " +  gatewaySwitch.isChecked());
        tweetManager.setGateway(gatewaySwitch.isChecked());
    }

    @OnClick(R.id.gatewayHelp)
    public void onGatewayHelpTap(View v) {
        Toast.makeText(this, "Toggle this switch if you want this device to act as a Twitter gateway", Toast.LENGTH_LONG).show();
    }

    @OnClick({R.id.btnSubmit})
    public void onSubmitTweet(View v) {
        // get the tweet and push it to the views
        String messageString = txtMessage.getText().toString();
        if (messageString.trim().length() > 0) {
            // create the Tweet object to send
            Tweet tweet = new Tweet(messageString, username);

            // update the views
            txtMessage.setText("");
            tweetsAdapter.addTweet(tweet);

            // send the tweet
            tweetManager.postTweet(tweet);
        }
    }

    @Override
    public void onTweetReceived(Tweet tweet) {
        tweetsAdapter.addTweet(tweet);
    }

    @Override
    public void onTweetPosted(Tweet tweet) {
        tweetsAdapter.refreshTweetView(tweet);
    }

    /**
     *      RECYCLER VIEW CLASSES
     */
    class TweetsRecyclerViewAdapter
            extends RecyclerView.Adapter<TweetsRecyclerViewAdapter.TweetViewHolder> {

        private List<Tweet> tweets;

        TweetsRecyclerViewAdapter(List<Tweet> tweets) {
            this.tweets = tweets;
        }

        @Override
        public int getItemCount() {
            return tweets.size();
        }

        void addTweet(Tweet tweet) {
            if (!tweets.contains(tweet)) {
                tweets.add(0, tweet);
                notifyDataSetChanged();
            }
        }

        void refreshTweetView(Tweet tweet) {
            for (int i = 0; i < tweets.size(); i++) {
                Tweet t = tweets.get(i);
                if (t.getId().equals(tweet.getId())) {
                    tweets.set(i, tweet);
                    notifyItemChanged(i);
                    return;
                }
            }
        }

        @Override
        public TweetViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            return new TweetViewHolder(LayoutInflater
                            .from(viewGroup.getContext())
                            .inflate((R.layout.tweet_row), viewGroup, false));
        }

        @Override
        public void onBindViewHolder(final TweetViewHolder tweetViewHolder, int position) {
            tweetViewHolder.setTweet(tweets.get(position));
        }


        class TweetViewHolder extends RecyclerView.ViewHolder {
            Tweet tweet;
            @BindView(R.id.txtTweet)       TextView txtTweet;
            @BindView(R.id.txtTweetDate)   TextView txtTweetDate;
            @BindView(R.id.txtTweetSender) TextView txtTweetSender;
            @BindView(R.id.imgTweetIcon)   ImageView imgTweetIcon;

            TweetViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            void setTweet(Tweet tweet) {
                this.tweet = tweet;
                txtTweet.setText(tweet.getContent());
                txtTweetSender.setText("#" + tweet.getSender());
                txtTweetDate.setText(getRelativeDate(tweet.getDate()));
                imgTweetIcon.setImageDrawable(getDrawable(
                        tweet.isPosted() ? R.drawable.tweet_online : R.drawable.tweet_mesh));
            }

            private String getRelativeDate(long datesent) {
                long relativeDate = System.currentTimeMillis()/1000 - datesent;
                if (relativeDate > 0) {
                    if (relativeDate < 60)
                        return relativeDate + "s";
                    if (relativeDate < 60*60)
                        return relativeDate/60 + "m";
                    if (relativeDate < 60*60*24)
                        return relativeDate/(60*24) + "hr";
                    if (relativeDate < 60*60*24*7)
                        return relativeDate/(60*24*7) + " days";
                    else
                        return relativeDate + "w";
                } else {
                    return "0s";
                }
            }
        }
    }
}
