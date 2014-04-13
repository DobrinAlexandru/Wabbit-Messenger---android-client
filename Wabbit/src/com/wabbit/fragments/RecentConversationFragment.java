package com.wabbit.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.wabbit.abstracts.MessagesMgr;
import com.wabbit.application.BaseApp;
import com.wabbit.database.Recent;
import com.wabbit.interfaces.IHelper;
import com.wabbit.libraries.cache.BitmapLruCache;
import com.wabbit.lists.RecentListAdapter;
import com.wabbit.lists.RecentListLoader;
import com.wabbit.messenger.R;

import asynclist.AsyncListView;
import asynclist.ItemManager;

/**
 * Created by Bogdan Tirca on 27.02.2014.
 */
public class RecentConversationFragment extends BaseFragment{
    //List view
    private AsyncListView mListView;
    private RecentListAdapter mListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recent_layout, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        createList();
    }

    private void createList(){
        mListView = (AsyncListView) getView().findViewById(R.id.recentList);

        BitmapLruCache cache = BaseApp.getApp().getBitmapCache();
        RecentListLoader loader = new RecentListLoader(cache);

        ItemManager.Builder builder = new ItemManager.Builder(loader);
        builder.setPreloadItemsEnabled(true).setPreloadItemsCount(5);
        builder.setThreadPoolSize(4);

        mListView.setItemManager(builder.build());
        mListView.setOnItemClickListener(onItemClick);
        mListAdapter = new RecentListAdapter(getActivity(), MessagesMgr.gi().getRecents());
        mListAdapter.setListView(mListView);
        mListView.setAdapter(mListAdapter);
    }

    @Override
    public void onResume(){
        super.onResume();
        updateList.run();
    }
    @Override
    public void onStart(){
        super.onStart();
        MessagesMgr.gi().refreshRecentList();
        MessagesMgr.gi().loadUnreadMessages(MessagesMgr.FROM_ONLINE, onLoadUnreadMessages);
    }

    public void updateList(){
        updateList.run();
    }
    private AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BaseApp.getApp().mixtrack("Open recent chat");
            if(getActivity() instanceof IHelper){
                String objid = ((Recent)parent.getItemAtPosition(position)).userId;
                if(objid.equals("id"))
                    Toast.makeText(getActivity(), "This user is not available right now", Toast.LENGTH_LONG).show();
                else
                    ((IHelper)getActivity()).openChatWith(objid);
            }
            else{
                Toast.makeText(getActivity(), "Failed to open chat", Toast.LENGTH_LONG).show();
            }
        }
    };

    private Runnable onLoadUnreadMessages = new Runnable() {
        @Override
        public void run() {
            MessagesMgr.gi().addUnreadToRecent();
            updateList.run();
        }
    };
    private Runnable updateListOnUi = new Runnable() {
        @Override
        public void run() {
            Activity act = getActivity();
            if(act != null)
                act.runOnUiThread(updateList);
        }
    };
    private Runnable updateList = new Runnable() {
        @Override
        public void run() {
            mListAdapter.updateList(false, true);
        }
    };
}
