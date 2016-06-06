package com.adwaitvyas.kalarav;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class ViewTweetActivity extends AppCompatActivity {

    private Twitter twitter;
    private ListView listViewReplies;
    private View parentTweet;
    private KalaravDatabaseHelper databaseHelper;
    private SQLiteDatabase database;
    private long tweetID;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences(Constants.PREFS_FILE, MODE_PRIVATE);
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
        Intent intent = getIntent();
        tweetID = intent.getLongExtra("tweetID", 0L);
        username = intent.getStringExtra("username");
        setContentView(R.layout.activity_view_tweet);
        listViewReplies = (ListView) findViewById(R.id.listViewReplies);
        parentTweet = findViewById(R.id.parentTweet);
        loadParent();
        new FetchTask().execute(username, tweetID);
    }

    private void loadParent() {

        Cursor cursor = database.query("home",null,BaseColumns._ID+"="+Long.toString(tweetID), null,null,null,null);
        cursor.moveToFirst();
        //get profile image
        ImageView profilePic = (ImageView)parentTweet.findViewById(R.id.imgUserImage);
        //set the image in the view for the current tweet
        Glide.with(ViewTweetActivity.this).load(cursor.getString(cursor.getColumnIndex("user_img"))).into(profilePic);

        long timestamp =  cursor.getLong(cursor.getColumnIndex("update_time"));
        TextView txtUpdateTime  = (TextView)parentTweet.findViewById(R.id.txtUpdateTime);
        txtUpdateTime.setText((DateUtils.getRelativeTimeSpanString(timestamp)));
        ((TextView)parentTweet.findViewById(R.id.updateText)).setText(cursor.getString(cursor.getColumnIndex("update_text")));
        long statusID = tweetID;
        String statusName = username;
        ((TextView)parentTweet.findViewById(R.id.txtUserName)).setText(statusName);
        TweetData tweetData = new TweetData(statusName, statusID);
        parentTweet.setTag(tweetData);
        parentTweet.findViewById(R.id.btnRetweet).setTag(tweetData);
        parentTweet.findViewById(R.id.btnReply).setTag(tweetData);
        parentTweet.findViewById(R.id.btnRetweet).setOnClickListener(onClickListener);
        parentTweet.findViewById(R.id.btnReply).setOnClickListener(onClickListener);
        parentTweet.findViewById(R.id.btnLike).setTag(tweetData);
        parentTweet.findViewById(R.id.btnLike).setOnClickListener(onClickListener);
        parentTweet.findViewById(R.id.btnView).setVisibility(View.GONE);
        cursor.close();
    }

    public ArrayList<Status> getDiscussion(String username, long id , Twitter twitter) {
        ArrayList<Status> replies = new ArrayList<>();

        ArrayList<Status> all = null;

        try {

            Query query = new Query("@" + username + " since_id:" + id);

            try {
                query.setCount(100);
            } catch (Throwable e) {
                //this has something to do with buffersize that i don't understand
                query.setCount(30);
            }

            QueryResult result = twitter.search(query);

            all = new ArrayList<Status>();

            do {
                List<Status> tweets = result.getTweets();

                for (Status tweet : tweets)
                    if (tweet.getInReplyToStatusId() == id)
                        all.add(tweet);

                if (all.size() > 0) {
                    for (int i = all.size() - 1; i >= 0; i--)
                        replies.add(all.get(i));
                    all.clear();
                }
                query = result.nextQuery();

                if (query != null)
                    result = twitter.search(query);

            } while (query != null);

        } catch (Exception | OutOfMemoryError e) {
            e.printStackTrace();
        }
        return replies;
    }

    class FetchTask extends AsyncTask<Object, Void, ArrayList<Status>> {
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ViewTweetActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Please wait");
            progressDialog.show();
        }
        @Override
        protected ArrayList<twitter4j.Status> doInBackground(Object... params) {
          return getDiscussion((String)params[0],(Long) params[1], twitter);
        }

        @Override
        protected void onPostExecute(ArrayList<twitter4j.Status> list) {
            super.onPostExecute(list);
            progressDialog.dismiss();
            listViewReplies.setAdapter(new ReplyAdapter(list));
        }
    }

    class ReplyAdapter extends BaseAdapter {

        ArrayList<Status> items;

        public ReplyAdapter(ArrayList<Status> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Status status = items.get(position);
            if(convertView == null) {
                convertView = ViewTweetActivity.this.getLayoutInflater()
                        .inflate(R.layout.layout_single_tweet, listViewReplies, false);
            }

            //get profile image
            ImageView profilePic = (ImageView)convertView.findViewById(R.id.imgUserImage);
            //set the image in the view for the current tweet
            Glide.with(ViewTweetActivity.this).load(status.getUser().getProfileImageURL()).into(profilePic);

            long timestamp =  status.getCreatedAt().getTime();
            TextView txtUpdateTime  = (TextView)convertView.findViewById(R.id.txtUpdateTime);
            txtUpdateTime.setText((DateUtils.getRelativeTimeSpanString(timestamp)));
            ((TextView)convertView.findViewById(R.id.updateText)).setText(status.getText());
            long statusID = status.getId();
            String statusName = status.getUser().getScreenName();
            ((TextView)convertView.findViewById(R.id.txtUserName)).setText(statusName);
            TweetData tweetData = new TweetData(statusName, statusID);
            convertView.setTag(tweetData);
            convertView.findViewById(R.id.btnRetweet).setTag(tweetData);
            convertView.findViewById(R.id.btnReply).setTag(tweetData);
            convertView.findViewById(R.id.btnRetweet).setOnClickListener(onClickListener);
            convertView.findViewById(R.id.btnReply).setOnClickListener(onClickListener);
            convertView.findViewById(R.id.btnLike).setTag(tweetData);
            convertView.findViewById(R.id.btnLike).setOnClickListener(onClickListener);
            convertView.findViewById(R.id.btnView).setVisibility(View.GONE);
            return convertView;
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TweetData tweetData = (TweetData) v.getTag();
            switch (v.getId()){
                case R.id.btnReply:
                    v.getContext().startActivity(new Intent(v.getContext(),ComposeTweetActivity.class)
                            .putExtra("tweetID", tweetData.getId())
                            .putExtra("reply", tweetData.getUsername()));
                    break;
                case R.id.btnRetweet:
                    v.getContext().startActivity(new Intent(v.getContext(),ComposeTweetActivity.class)
                            .putExtra("tweetID", tweetData.getId())
                            .putExtra("retweet", tweetData.getUsername()));
                    break;
                case R.id.btnLike:
                    v.getContext().startActivity(new Intent(v.getContext(),ComposeTweetActivity.class)
                            .putExtra("tweetID", tweetData.getId())
                            .putExtra("like", tweetData.getUsername()));
                    break;
                case R.id.btnView:
                    v.getContext().startActivity(new Intent(v.getContext(),ViewTweetActivity.class)
                            .putExtra("tweetID", tweetData.getId())
                            .putExtra("username", tweetData.getUsername()));
                    break;


            }
        }
    };
}
