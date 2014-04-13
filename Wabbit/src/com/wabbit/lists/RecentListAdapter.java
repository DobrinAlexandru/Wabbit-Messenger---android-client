package com.wabbit.lists;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.parse.ParseUser;
import com.wabbit.abstracts.Enums;
import com.wabbit.abstracts.ParseMemCache;
import com.wabbit.activity.MainActivity;
import com.wabbit.database.Recent;
import com.wabbit.messenger.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.WeakHashMap;

/**
 * Created by Bogdan Tirca on 20.03.2014.
 */
public class RecentListAdapter extends CustBaseAdapter {
    private final ArrayList<Recent> mRecents;
    private final Context mContext;

    private Calendar cal;

    public WeakHashMap<Integer, RecentViewHolder> holders = new WeakHashMap<Integer, RecentViewHolder>();

    public RecentListAdapter(Context context, ArrayList<Recent> recents) {
        mRecents = recents;
        mContext = context;

        cal = Calendar.getInstance();

        final ArrayList<String> ids = new ArrayList<String>();
        loadMissingContent = new Runnable() {
            @Override
            public void run() {
                ParseMemCache.gi().loadUsers(ids, false, new Runnable() {
                    @Override
                    public void run() {
                        updateList(true, false);
                    }
                });
            }
        };
        contentCheck = new IContentCheck() {
            @Override
            public boolean consistentContent() {
                ids.clear();
                for(Recent recent : mRecents){
                    if(ParseMemCache.gi().getUser(recent.userId, false) == null)
                        ids.add(recent.userId);
                }
                if(ids.isEmpty())
                    return true;
                return false;
            }
        };
    }

    @Override
    public int getCount() {
        if (mRecents == null) {
            return 0;
        }
        return mRecents.size();
    }

    @Override
    public Recent getItem(int position) {
        return mRecents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RecentViewHolder holder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.recent_item, parent, false);

            holder = new RecentViewHolder();
            holder.avatar = (RoundedImageView) convertView.findViewById(R.id.avatar);
//            holder.avatar.setCornerRadius(5);
            holder.avatar.setOval(true);
            holder.avatar.setOnClickListener(onPictureClick);
            holder.lastMsg = (TextView) convertView.findViewById(R.id.last_msg);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.time = (TextView) convertView.findViewById(R.id.time);

            convertView.setTag(holder);
        } else {
            holder = (RecentViewHolder) convertView.getTag();
        }

        final Recent recent = getItem(position);
        final ParseUser user = ParseMemCache.gi().getUser(recent.userId, false);
        String userName = ". . .";
        if(user != null)
            userName = user.getString(Enums.ParseKey.USER_NAME);
        //If unread messages
        if(recent.lastMsgRead == false){
            holder.lastMsg.setTypeface(null, Typeface.BOLD);
            holder.lastMsg.setTextColor(Color.BLACK);
            holder.name.setTypeface(null, Typeface.BOLD);
        }else{
            holder.lastMsg.setTypeface(null, Typeface.NORMAL);
            holder.lastMsg.setTextColor(0xFFAAAAAA);
            holder.name.setTypeface(null, Typeface.NORMAL);
        }
        holder.avatar.setImageDrawable(null);
        holder.avatar.setTag(recent.userId);
        holder.lastMsg.setText(recent.lastMsgText);
        holder.name.setText(userName);

        String time = getTime(recent.lastMsgTime);
        holder.time.setText(time);

        holders.put(position, holder);
        return convertView;
    }

    private String getTime(long previous){
        long current = System.currentTimeMillis();
        long oneDay = 1000 * 60 * 60 * 24;
        long delta = (current - previous) / oneDay;
        if(delta == 0){
            cal.setTimeInMillis(previous);
            return String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", cal.get(Calendar.MINUTE));
        }
        else    if(delta == 1){
            return "yesterday";
        }
        else    if(delta < 30){
            return delta + " days ago";
        }
        else    if(delta < 360){
            long months = delta / 30;
            if(months == 1)
                return "1 month ago";
            return (delta / 30) + " months ago";
        }
        else    {
            long years = delta / 360;
            if(years == 1)
                return "1 year ago";
            return (delta / 360) + " years ago";
        }

    }
    private View.OnClickListener onPictureClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(MainActivity.gi() != null)
                MainActivity.gi().showProfile((String)v.getTag());
        }
    };

    public class RecentViewHolder {
        public RoundedImageView avatar;
        public TextView name;
        public TextView lastMsg;
        public TextView time;
    }
}
