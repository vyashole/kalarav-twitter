package com.adwaitvyas.kalarav;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;
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

    }

}
