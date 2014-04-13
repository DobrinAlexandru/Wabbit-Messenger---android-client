package com.wabbit.lists;

import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.widget.Adapter;

import com.wabbit.database.Recent;
import com.wabbit.libraries.cache.BitmapLruCache;
import com.wabbit.libraries.cache.CacheableBitmapDrawable;
import com.wabbit.libraries.remoting.FBMgr;
import com.wabbit.libraries.remoting.HttpHelper;

import asynclist.SimpleItemLoader;

/**
 * Created by Bogdan Tirca on 20.03.2014.
 */
public class RecentListLoader  extends SimpleItemLoader<Recent, CacheableBitmapDrawable> {
    final BitmapLruCache mCache;

    public RecentListLoader(BitmapLruCache cache) {
        mCache = cache;
    }

    @Override
    public CacheableBitmapDrawable loadItemFromMemory(Recent recent) {
        final String pic = FBMgr.gi().getLinkToProfilePictureByParseId(recent.userId);
        return mCache.getFromMemoryCache(pic);
    }

    @Override
    public Recent getItemParams(Adapter adapter, int position) {
        return (Recent) adapter.getItem(position);
    }

    @Override
    public CacheableBitmapDrawable loadItem(Recent recent) {
        final String pic = FBMgr.gi().getLinkToProfilePictureByParseId(recent.userId);
        CacheableBitmapDrawable wrapper = mCache.get(pic);
        if (wrapper == null) {
            wrapper = mCache.put(pic, HttpHelper.loadImage(pic));
        }
        return wrapper;
    }

    @Override
    public void displayItem(View itemView, CacheableBitmapDrawable result, boolean fromMemory) {
        RecentListAdapter.RecentViewHolder holder = (RecentListAdapter.RecentViewHolder) itemView.getTag();

        if (result == null) {
            //holder.title.setText("Failed");
            return;
        }

        result.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

        if (fromMemory) {
            holder.avatar.setImageDrawable(result);
        } else {
            BitmapDrawable emptyDrawable = new BitmapDrawable(itemView.getResources());

            TransitionDrawable fadeInDrawable =
                    new TransitionDrawable(new Drawable[] { emptyDrawable, result });

            holder.avatar.setImageDrawable(result);
            fadeInDrawable.startTransition(200);
        }
    }
}