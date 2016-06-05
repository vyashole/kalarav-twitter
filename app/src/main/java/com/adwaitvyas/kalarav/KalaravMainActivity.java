package com.adwaitvyas.kalarav;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class KalaravMainActivity extends AppCompatActivity {
    private static final String TAG = "KalaravMainActivity";
    private Twitter twitter;
    private RequestToken requestToken;
    private AccessToken accessToken;
    private SharedPreferences kalaravPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //create shared prefs
        kalaravPrefs = getSharedPreferences(Constants.PREFS_FILE, MODE_PRIVATE);
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(Constants.TWITTER_KEY);
        builder.setOAuthConsumerSecret(Constants.TWITTER_SECRET);
        Configuration configuration = builder.build();
        TwitterFactory factory = new TwitterFactory(configuration);
        twitter = factory.getInstance();

        //check Auth token
        String authToken = kalaravPrefs.getString(Constants.USER_TOKEN, null);
        if(authToken == null){
            //not logged in
            setContentView(R.layout.activity_kalarav_main);
            Button btnSignIn = (Button) findViewById(R.id.btnSignIn);
            assert btnSignIn != null; //something is terribly wrong if this assert throws

            btnSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()){
                        case R.id.btnSignIn:
                            //sign in new user
                            String url = requestToken.getAuthenticationURL();
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    }
                }
            });

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // TODO this should ideally be an AsyncTask with a splash screen
                        requestToken = twitter.getOAuthRequestToken(Constants.APP_URL);
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } else {
            //already logged in
            //TODO load twitter feed
            Toast.makeText(this, "Logged in!", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: intent received");
        Uri data = intent.getData();
        if(data !=null ){

            String oauthVerifier = data.getQueryParameter("oauth_verifier");
            AsyncTask<String, Void, AccessToken> accessTokenTask = new AsyncTask<String, Void, AccessToken> () {

                @Override
                protected AccessToken doInBackground(String... params) {
                    AccessToken token = null;
                    try {
                       token = twitter.getOAuthAccessToken(requestToken, params[0]);
                    } catch (TwitterException e) {
                        e.printStackTrace();
                    }
                    return token;
                }

                @Override
                protected void onPostExecute(AccessToken accessToken) {
                    super.onPostExecute(accessToken);
                    KalaravMainActivity.this.accessToken = accessToken;
                    kalaravPrefs.edit().putString(Constants.USER_TOKEN,accessToken.getToken())
                            .putString(Constants.USER_SECRET, accessToken.getTokenSecret()).apply();
                    Toast.makeText(KalaravMainActivity.this, "Logged in!", Toast.LENGTH_LONG).show();
                    finish();
                }
            };

            accessTokenTask.execute(oauthVerifier);


        }
    }
}
