package com.adwaitvyas.kalarav;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterFeedService extends Service {
    private Twitter twitter;
    private KalaravDatabaseHelper databaseHelper;
    private SQLiteDatabase database;
    private SharedPreferences preferences;
    private Handler handler;
    private Fetcher fetcher;
    private static final long FETCH_INTERVAL = 2*60*1000;
    private static final String TAG = "TwitterFeedService";

    public TwitterFeedService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences(Constants.PREFS_FILE, MODE_PRIVATE);
        databaseHelper = new KalaravDatabaseHelper(this);
        database = databaseHelper.getWritableDatabase();
        String token = preferences.getString(Constants.USER_TOKEN,null);
        String secret = preferences.getString(Constants.USER_SECRET,null);
        Configuration configuration = new ConfigurationBuilder()
                .setOAuthConsumerKey(Constants.TWITTER_KEY)
                .setOAuthConsumerSecret(Constants.TWITTER_SECRET)
                .setOAuthAccessToken(token)
                .setOAuthAccessTokenSecret(secret)
                .build();
        twitter = new TwitterFactory(configuration).getInstance();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        fetcher = new Fetcher();
        new Thread(fetcher).start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(fetcher);
        database.close();
    }

    class Fetcher implements Runnable {

        @Override
        public void run() {
            Looper.prepare();
            handler = new Handler();
            boolean statusChanges = false;

            try {
                List<Status> timeline = twitter.getHomeTimeline();
                for (Status status : timeline){
                    ContentValues values = KalaravDatabaseHelper.getValues(status);
                    database.insertOrThrow("home", null, values);
                    statusChanges = true;
                }
                if(statusChanges){
                    sendBroadcast(new Intent("KALARAV_UPDATE"));
                }
            }
            catch (SQLiteConstraintException ignored){

            }
            catch (Exception e){
                e.printStackTrace();
            }
            handler.postDelayed(this, FETCH_INTERVAL);
        }
    }
}
