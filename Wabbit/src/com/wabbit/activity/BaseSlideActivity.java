//package com.wabbit.activity;
//
//import android.content.Intent;
//import android.os.Bundle;
//
//import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
//import com.parse.ParseFacebookUtils;
//import com.wabbit.application.BaseApp;
//
//public class BaseSlideActivity extends SlidingFragmentActivity {
//    @Override
//    public void onCreate(Bundle savedInstanceState){
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    public void onResume(){
//        super.onResume();
//
//        getApp().actResume();
//    }
//
//    @Override
//    public void onPause(){
//        super.onPause();
//
//        getApp().actPause();
//    }
//
//    @Override
//    public void onStart(){
//        super.onStart();
//    }
//    @Override
//    public void onStop(){
//        super.onStop();
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
//    }
//
//    public void startActivity(Class <?> newActivity, boolean finishCurrent, Bundle extras){
//        Intent intent = new Intent(this, newActivity);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if(extras != null)
//            intent.putExtras(extras);
//        startActivity(intent);
//
//        if(finishCurrent)
//            finish();
//    }
//
//    public BaseApp getApp(){
//        return (BaseApp)getApplicationContext();
//    }
//
//    public int getResId(String resId, String location){
//        return getApp().getResources().getIdentifier(resId,
//                location, getApp().getPackageName());
//    }
//
//    public int getDrawableId(String resId){
//        return getApp().getResources().getIdentifier(resId,
//                "drawable", getApp().getPackageName());
//    }
//}
