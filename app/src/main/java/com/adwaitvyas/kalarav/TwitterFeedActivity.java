package com.adwaitvyas.kalarav;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class TwitterFeedActivity extends AppCompatActivity {
    ListView feedListView;
    TwitterFeedAdapter twitterFeedAdapter;
    KalaravDatabaseHelper databaseHelper;
    SQLiteDatabase database;
    Cursor cursor;
    TwitterUpdateReceiver updateReceiver;
    private static final String TAG = "TwitterFeedActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton btnTweet = (FloatingActionButton) findViewById(R.id.btnTweet);
        if (btnTweet != null) {
            btnTweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(TwitterFeedActivity.this, ComposeTweetActivity.class));
                }
            });
        }
        feedListView = (ListView) findViewById(R.id.feedList);
        databaseHelper = new KalaravDatabaseHelper(this);
        database = databaseHelper.getReadableDatabase();
        cursor = database.query("home", null, null, null, null, null, "update_time DESC");
        startManagingCursor(cursor); //todo deprecated - find a better way to do this
        twitterFeedAdapter = new TwitterFeedAdapter(this,cursor);
        feedListView.setAdapter(twitterFeedAdapter);
        /*feedListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(TwitterFeedActivity.this,ViewTweetActivity.class);
                TweetData tweetData = (TweetData) view.getTag();
                intent.putExtra("tweetID",tweetData.getId());
                intent.putExtra("username", tweetData.getUsername());
                startActivity(intent);
            }
        });*/
        updateReceiver = new TwitterUpdateReceiver();
        registerReceiver(updateReceiver,new IntentFilter("KALARAV_UPDATE"));
        getApplicationContext().startService(new Intent(getApplicationContext(), TwitterFeedService.class));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(updateReceiver);
        stopService(new Intent(getApplicationContext(), TwitterFeedService.class));
        database.close();
        super.onDestroy();
    }

    class TwitterUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: Incoming Tweets ");
            int rowLimit = 200; //keep only 200 tweets
            if(DatabaseUtils.queryNumEntries(database, "home")>rowLimit) {
                String deleteQuery = "DELETE FROM home WHERE "+ BaseColumns._ID+" NOT IN " +
                        "(SELECT "+BaseColumns._ID+" FROM home ORDER BY "+"update_time DESC " +
                        "limit "+rowLimit+")";
                database.execSQL(deleteQuery);
            }
            cursor = database.query("home", null, null, null, null, null, "update_time DESC");
            startManagingCursor(cursor);
            twitterFeedAdapter = new TwitterFeedAdapter(TwitterFeedActivity.this,cursor);
            feedListView.setAdapter(twitterFeedAdapter);

        }
    }



}
