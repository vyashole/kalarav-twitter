package com.adwaitvyas.kalarav;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * Adapter for twitter list
 */
public class TwitterFeedAdapter extends SimpleCursorAdapter {

    static final String[] from = { "update_text", "user_screen",
            "update_time", "user_img" };

    static final int[] to = { R.id.updateText, R.id.txtUserName,
            R.id.txtUpdateTime, R.id.imgUserImage };

    private static final String TAG = "TwitterFeedAdapter";


    public TwitterFeedAdapter(Context context, Cursor c) {
        super(context, R.layout.layout_single_tweet, c, from, to, FLAG_AUTO_REQUERY);
    }

    @Override
    public void bindView(View row, Context context, Cursor cursor) {
        super.bindView(row, context, cursor);
        //get profile image
         ImageView profilePic = (ImageView)row.findViewById(R.id.imgUserImage);
        //set the image in the view for the current tweet
        Glide.with(context).load(cursor.getString(cursor.getColumnIndex("user_img"))).into(profilePic);

        long timestamp =  cursor.getLong(cursor.getColumnIndex("update_time"));
        TextView txtUpdateTime  = (TextView)row.findViewById(R.id.txtUpdateTime);
        txtUpdateTime.setText((DateUtils.getRelativeTimeSpanString(timestamp)));

        long statusID = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        String statusName = cursor.getString(cursor.getColumnIndex("user_screen"));
        TweetData tweetData = new TweetData(statusName, statusID);
        row.findViewById(R.id.btnRetweet).setTag(tweetData);
        row.findViewById(R.id.btnReply).setTag(tweetData);
        row.findViewById(R.id.btnRetweet).setOnClickListener(onClickListener);
        row.findViewById(R.id.btnReply).setOnClickListener(onClickListener);
        row.findViewById(R.id.btnLike).setTag(tweetData);
        row.findViewById(R.id.btnLike).setOnClickListener(onClickListener);


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

            }
        }
    };

}
