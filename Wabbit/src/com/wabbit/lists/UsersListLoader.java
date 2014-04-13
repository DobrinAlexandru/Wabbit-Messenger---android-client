/*
 * Copyright (C) 2012 Lucas Rocha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wabbit.lists;

import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.View;
import android.widget.Adapter;

import com.wabbit.libraries.cache.BitmapLruCache;
import com.wabbit.libraries.cache.CacheableBitmapDrawable;
import com.wabbit.libraries.remoting.HttpHelper;

import asynclist.SimpleItemLoader;

public class UsersListLoader extends SimpleItemLoader<NearbyUserHolder, CacheableBitmapDrawable> {
    final BitmapLruCache mCache;

    public UsersListLoader(BitmapLruCache cache) {
        mCache = cache;
    }

    @Override
    public CacheableBitmapDrawable loadItemFromMemory(NearbyUserHolder user) {
        return mCache.getFromMemoryCache(user.avatar);
    }

    @Override
    public NearbyUserHolder getItemParams(Adapter adapter, int position) {
        return (NearbyUserHolder) adapter.getItem(position);
    }

    @Override
    public CacheableBitmapDrawable loadItem(NearbyUserHolder user) {
        CacheableBitmapDrawable wrapper = mCache.get(user.avatar);
        if (wrapper == null) {
            wrapper = mCache.put(user.avatar, HttpHelper.loadImage(user.avatar));
        }

        return wrapper;
    }

    @Override
    public void displayItem(View itemView, CacheableBitmapDrawable result, boolean fromMemory) {
        UsersListAdapter.UserViewHolder holder = (UsersListAdapter.UserViewHolder) itemView.getTag();

        if (result == null) {
            //holder.title.setText("Failed");
            return;
        }

        result.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);

        if (fromMemory) {
            holder.avatar.setImageDrawable(result);
        } else {
            BitmapDrawable emptyDrawable = new BitmapDrawable(itemView.getResources());

            TransitionDrawable fadeInDrawable =
                    new TransitionDrawable(new Drawable[] { emptyDrawable, result });

            holder.avatar.setImageDrawable(result);
            fadeInDrawable.startTransition(200);
        }

        //holder.title.setText("Loaded");
    }
}
