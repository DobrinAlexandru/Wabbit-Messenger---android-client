package com.wabbit.lists;

import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.widget.Adapter;

import com.wabbit.libraries.cache.BitmapLruCache;
import com.wabbit.libraries.cache.CacheableBitmapDrawable;
import com.wabbit.libraries.remoting.HttpHelper;
import com.wabbit.parse.PMessage;

import asynclist.SimpleItemLoader;

/**
 * Created by Bogdan Tirca on 03.02.2014.
 */
public class MessagesListLoader extends SimpleItemLoader<PMessage, CacheableBitmapDrawable> {
    final BitmapLruCache mCache;

    public MessagesListLoader(BitmapLruCache cache) {
        mCache = cache;
    }

    @Override
    public CacheableBitmapDrawable loadItemFromMemory(PMessage msg) {
        return mCache.getFromMemoryCache(msg.getSenderAvatar());
    }

    @Override
    public PMessage getItemParams(Adapter adapter, int position) {
        return (PMessage) adapter.getItem(position);
    }

    @Override
    public CacheableBitmapDrawable loadItem(PMessage msg) {
        final String pic = msg.getSenderAvatar();
        CacheableBitmapDrawable wrapper = mCache.get(pic);
        if (wrapper == null) {
            wrapper = mCache.put(pic, HttpHelper.loadImage(pic));
        }

        return wrapper;
    }

    @Override
    public void displayItem(View itemView, CacheableBitmapDrawable result, boolean fromMemory) {
        MessagesListAdapter.MessageViewHolder holder = (MessagesListAdapter.MessageViewHolder) itemView.getTag();

        if (result == null) {
            return;
        }

        result.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        if(holder.sent)
            return;
        if (fromMemory) {
            holder.getImg().setImageDrawable(result);
        } else {
            BitmapDrawable emptyDrawable = new BitmapDrawable(itemView.getResources());

            TransitionDrawable fadeInDrawable =
                    new TransitionDrawable(new Drawable[] { emptyDrawable, result });

            holder.getImg().setImageDrawable(fadeInDrawable);
            fadeInDrawable.startTransition(200);
        }

        //holder.title.setText("Loaded");
    }
}
