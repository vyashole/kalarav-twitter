package com.adwaitvyas.kalarav;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class ComposeTweetActivity extends AppCompatActivity {
    SharedPreferences preferences;
    Twitter twitter;
    String replyTo;
    long tweetID;
    String retweet;
    String like;
    EditText editText;
    Button btnSend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(Constants.PREFS_FILE,MODE_PRIVATE);
        String userToken = preferences.getString(Constants.USER_TOKEN, null);
        String userSecret = preferences.getString(Constants.USER_SECRET, null);
        Configuration configuration = new ConfigurationBuilder()
                .setOAuthConsumerKey(Constants.TWITTER_KEY)
                .setOAuthConsumerSecret(Constants.TWITTER_SECRET)
                .setOAuthAccessToken(userToken)
                .setOAuthAccessTokenSecret(userSecret)
                .build();
        twitter = new TwitterFactory(configuration).getInstance();
         tweetID = getIntent().getLongExtra("tweetID",0);
        replyTo = getIntent().getStringExtra("reply");
        retweet = getIntent().getStringExtra("retweet");
        like = getIntent().getStringExtra("like");
        if(retweet != null){
            new RetweetTask().execute(tweetID);
        }
        else if(like != null){
           new LikeTask().execute(tweetID);
        }
        else {
            setContentView(R.layout.activity_compose_tweet);
            editText = (EditText) findViewById(R.id.editText);
            btnSend  = (Button) findViewById(R.id.btnSend);

            if(replyTo != null){
                editText.setText("@"+replyTo);
            }
            if (btnSend != null && editText != null) {
                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String textToTweet = editText.getText().toString().trim();
                        StatusUpdate statusUpdate;
                        statusUpdate = new StatusUpdate(textToTweet);
                        if(replyTo !=null){
                            statusUpdate.inReplyToStatusId(tweetID);
                        }
                        new TweetTask().execute(statusUpdate);
                    }
                });
            }

        }

    }

    class TweetTask extends AsyncTask<StatusUpdate, Void, Status> {
        twitter4j.Status status = null;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ComposeTweetActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Please wait");
            progressDialog.show();
        }
        @Override
        protected twitter4j.Status doInBackground(StatusUpdate... params) {
            try {
                status = twitter.updateStatus(params[0]);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return status;
        }

        @Override
        protected void onPostExecute(twitter4j.Status status) {
            super.onPostExecute(status);
            progressDialog.dismiss();
            if (status != null){
                Toast.makeText(ComposeTweetActivity.this,"Posted", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(ComposeTweetActivity.this,"Post Failed", Toast.LENGTH_LONG).show();
            }
            finish();
        }
    }

    class RetweetTask extends AsyncTask<Long, Void, Status> {
        twitter4j.Status status = null;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ComposeTweetActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Please wait");
            progressDialog.show();
        }
        @Override
        protected twitter4j.Status doInBackground(Long... params) {
            try {
                status = twitter.retweetStatus(params[0]);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return status;
        }

        @Override
        protected void onPostExecute(twitter4j.Status status) {
            super.onPostExecute(status);
            progressDialog.dismiss();
            if (status != null){
                Toast.makeText(ComposeTweetActivity.this,"Retweeted", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(ComposeTweetActivity.this,"Retweet Failed", Toast.LENGTH_LONG).show();
            }
            finish();
        }
    }

    class LikeTask extends AsyncTask<Long, Void, Status> {
        twitter4j.Status status = null;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ComposeTweetActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Please wait");
            progressDialog.show();
        }
        @Override
        protected twitter4j.Status doInBackground(Long... params) {
            try {
                status = twitter.createFavorite(params[0]);
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return status;
        }

        @Override
        protected void onPostExecute(twitter4j.Status status) {
            super.onPostExecute(status);
            progressDialog.dismiss();
            if (status != null){
                Toast.makeText(ComposeTweetActivity.this,"Liked", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(ComposeTweetActivity.this,"Like Failed", Toast.LENGTH_LONG).show();
            }
            finish();
        }
    }
}
