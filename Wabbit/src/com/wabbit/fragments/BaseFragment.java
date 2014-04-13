package com.wabbit.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wabbit.messenger.R;

/**
 * Created by Bogdan Tirca on 18.03.2014.
 */
public class BaseFragment extends Fragment{
    private boolean running = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("frag create view", ((Object) this).getClass().toString());
        return inflater.inflate(R.layout.messages_layout, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("frag activity created", ((Object) this).getClass().toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("frag resume", ((Object) this).getClass().toString());
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d("frag pause", ((Object) this).getClass().toString());
    }

    @Override
    public void onStart(){
        super.onStart();
        running = true;
        Log.d("frag start", ((Object) this).getClass().toString());
    }
    @Override
    public void onStop(){
        super.onStop();
        running = false;
        Log.d("frag stop", ((Object) this).getClass().toString());
    }
    public boolean isRunning(){
        return running;
    }
}
