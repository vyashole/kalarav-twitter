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

    EditText editText;
    Button btnSend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_tweet);
        editText = (EditText) findViewById(R.id.editText);
        btnSend  = (Button) findViewById(R.id.btnSend);
        if (btnSend != null && editText != null) {
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String textToTweet = editText.getText().toString().trim();
                    StatusUpdate statusUpdate;
                    statusUpdate = new StatusUpdate(textToTweet);
                    new TweetTask().execute(statusUpdate);
                }
            });
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
}