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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.wabbit.activity.MainActivity;
import com.wabbit.messenger.R;

import java.util.ArrayList;
import java.util.WeakHashMap;

public class UsersListAdapter extends BaseAdapter {
	private final ArrayList<NearbyUserHolder> mUsers;
	private final Context mContext;

    public WeakHashMap<Integer, UserViewHolder> holders = new WeakHashMap<Integer, UserViewHolder>();

	public UsersListAdapter(Context context, ArrayList<NearbyUserHolder> urls) {
        mUsers = urls;
		mContext = context;
	}

	@Override
	public int getCount() {
	    if (mUsers == null) {
	        return 0;
	    }
	    return mUsers.size();
	}

	@Override
	public NearbyUserHolder getItem(int position) {
		return mUsers.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    UserViewHolder holder = null;

		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.nearby_item, parent, false);

			holder = new UserViewHolder();
			holder.avatar = (RoundedImageView) convertView.findViewById(R.id.avatar);
//            holder.avatar.setCornerRadius(5);
            holder.avatar.setOval(true);
            holder.avatar.setOnClickListener(onPictureClick);

            holder.distance = (TextView) convertView.findViewById(R.id.distance);
            holder.name = (TextView) convertView.findViewById(R.id.name);

			convertView.setTag(holder);
		} else {
		    holder = (UserViewHolder) convertView.getTag();
		}

        NearbyUserHolder userHolder = getItem(position);

        holder.avatar.setImageDrawable(null);
        holder.avatar.setTag(userHolder.id);
        holder.name.setText(userHolder.name);
        holder.distance.setText(userHolder.distance);

        holders.put(position, holder);
		return convertView;
	}

    private View.OnClickListener onPictureClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(MainActivity.gi() != null)
                MainActivity.gi().showProfile((String)v.getTag());
        }
    };

	public class UserViewHolder {
	    public RoundedImageView avatar;
	    public TextView name;
        public TextView distance;
	}
}
