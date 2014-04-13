package com.wabbit.lists;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nineoldandroids.view.ViewHelper;
import com.wabbit.application.BaseApp;
import com.wabbit.messenger.R;
import com.wabbit.parse.PMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.WeakHashMap;

/**
 * Created by Bogdan Tirca on 03.02.2014.
 */
public class MessagesListAdapter extends BaseAdapter {
    private final ArrayList<PMessage> mMessages;
    private final Activity mContext;

    public WeakHashMap<Integer, MessageViewHolder> holders = new WeakHashMap<Integer, MessageViewHolder>();

    private Drawable leftBubble, rightBubble;
    private int marginSize, marginThird;
    public MessagesListAdapter(Activity context, ArrayList<PMessage> messages) {
        mMessages = messages;
        mContext = context;

        leftBubble = mContext.getResources().getDrawable(R.drawable.triangle_left);
        rightBubble = mContext.getResources().getDrawable(R.drawable.triangle_right);

        marginSize = (int)(BaseApp.getApp().getDisplaySize().x * 0.1f);
        marginThird = marginSize / 3;
    }

    @Override
    public int getCount() {
        if (mMessages == null) {
            return 0;
        }

        return mMessages.size();
    }

    @Override
    public PMessage getItem(int position) {
        return mMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private PMessage currentItem;
    private RelativeLayout.LayoutParams params;
    private HashMap<Integer, Drawable> mSentBgs = new HashMap<Integer, Drawable>();
    private HashMap<Integer, Drawable> mReceivedBgs = new HashMap<Integer, Drawable>();

    private Drawable mSentBg, mReceivedBg;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageViewHolder holder = null;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, parent, false);

            holder = new MessageViewHolder();
            holder.left = (RelativeLayout) convertView.findViewById(R.id.received);
            holder.right = (RelativeLayout) convertView.findViewById(R.id.sent);

            holder.leftImg = (RoundedImageView) convertView.findViewById(R.id.avatar_left);
//            holder.leftImg.setCornerRadius(5);
            holder.leftImg.setOval(true);
//            holder.rightImg = (RoundedImageView) convertView.findViewById(R.id.avatar_right);
//            holder.rightImg.setCornerRadius(5);

            holder.leftText = (TextView) convertView.findViewById(R.id.text_body_left);
            holder.rightText = (TextView) convertView.findViewById(R.id.text_body_right);

            convertView.setTag(holder);
        } else {
            holder = (MessageViewHolder) convertView.getTag();
        }

        currentItem = getItem(position);

        //Manage sent & received message style
        if(currentItem.isSent()) {
            holder.right.setVisibility(View.VISIBLE);
            holder.left.setVisibility(View.GONE);
            holder.rightText.setText(currentItem.getText());
//            holder.rightImg.setImageDrawable(null);
            holder.sent = true;
            if(currentItem.hasTemporaryId()){
                holder.rightText.setTextColor(0xFFC9C9C9);
                ViewHelper.setScaleX(holder.right, 0.8f);
                ViewHelper.setScaleY(holder.right, 0.8f);
            }
            else{
                ViewHelper.setScaleX(holder.right, 1f);
                ViewHelper.setScaleY(holder.right, 1f);
                holder.rightText.setTextColor(0xFFFFFFFF);
            }
        }
        else{
            holder.left.setVisibility(View.VISIBLE);
            holder.right.setVisibility(View.GONE);
            holder.leftText.setText(currentItem.getText());
            holder.leftImg.setImageDrawable(null);
            holder.sent = false;
        }
        holders.put(position, holder);
        return convertView;
    }

    public class MessageViewHolder {
        public RoundedImageView leftImg, rightImg;
        public TextView leftText, rightText;
        public RelativeLayout left, right;
        public boolean sent;
        public RoundedImageView getImg(){
//            if(sent)
//                return rightImg;
            return leftImg;
        }
    }
}