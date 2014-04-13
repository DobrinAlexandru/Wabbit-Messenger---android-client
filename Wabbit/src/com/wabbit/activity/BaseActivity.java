package com.wabbit.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.wabbit.application.BaseApp;
import com.wabbit.libraries.errorHandling.Reporter;

public class BaseActivity extends FragmentActivity {
    private boolean mIsRunning = false;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);

        Reporter.setErrorReporting(this);
	}
	
	@Override
	public void onResume(){
		super.onResume();

		getApp().actResume();
	}
	
	@Override
	public void onPause(){
		super.onPause();

		getApp().actPause();
	}

    @Override
    public void onStart(){
        super.onStart();
        mIsRunning = true;
    }
    @Override
    public void onStop(){
        super.onStop();
        mIsRunning = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    public boolean isRunning(){
        return mIsRunning;
    }
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
////        Session.getActiveSession()
////                .onActivityResult(this, requestCode, resultCode, data);
//    }

	public void startActivity(Class <?> newActivity, boolean finishCurrent, Bundle extras){
		Intent intent = new Intent(this, newActivity);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if(extras != null)
			intent.putExtras(extras);
		startActivity(intent);
		
		if(finishCurrent)
			finish();
	}
	
	public BaseApp getApp(){
		return (BaseApp)getApplicationContext();
	}

	public int getResId(String resId, String location){
		return getApp().getResources().getIdentifier(resId,
				location, getApp().getPackageName());
	}
	
	public int getDrawableId(String resId){
		return getApp().getResources().getIdentifier(resId,
				"drawable", getApp().getPackageName());
	}
}
