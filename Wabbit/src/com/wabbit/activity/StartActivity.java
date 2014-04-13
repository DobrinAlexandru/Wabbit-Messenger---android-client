package com.wabbit.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wabbit.abstracts.Enums;
import com.wabbit.abstracts.ParseMemCache;
import com.wabbit.application.BaseApp;
import com.wabbit.libraries.remoting.FBMgr;
import com.wabbit.messenger.R;

import java.util.ArrayList;
import java.util.List;

public class StartActivity extends BaseActivity{
	private Button button;

    @Override
	public void onCreate(Bundle savedExtras){
		super.onCreate(savedExtras);

		setContentView(R.layout.login_layout);

		button = (Button)findViewById(R.id.sign_in_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseApp.getApp().mixtrack("Click login");
                if(BaseApp.getApp().isNetworkConnected() == false){
                    Toast.makeText(StartActivity.this, "No internet connection!", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Facebook permissions
                List<String> permissions = new ArrayList<String>();
//                permissions.add("user_photos");
                ParseFacebookUtils.logIn(permissions, StartActivity.this, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException err) {
                        if (user == null) {
                            Log.d("MyApp", "Uh oh. The user canceled the Facebook login.");
                            Toast.makeText(StartActivity.this, "Login canceled", Toast.LENGTH_LONG).show();
                            BaseApp.getApp().mixtrack("Login canceled");
                        } else if (user.isNew()) {
                            Log.d("MyApp", "User signed up and logged in through Facebook!");
                            BaseApp.getApp().mixtrack("Sign up");
                            collectUserData();
                        } else {
                            Log.d("MyApp", "User logged in through Facebook!");
                            BaseApp.getApp().mixtrack("Login complete");
                            collectUserData();
                        }
                    }
                });
            }
        });
	}
    @Override
    public void onResume(){
        super.onResume();
        BaseApp.getApp().mixtrack("Start app");
        if(ParseUser.getCurrentUser() != null){
            collectUserData();
            return ;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    private ProgressDialog progress;
    private void collectUserData(){
        ParseMemCache.gi().put(ParseUser.getCurrentUser(), false);
        /**BUG FIX: trying to show progress on paused activity*/
        if(isRunning())
            progress = ProgressDialog.show(this, "Please wait", "Logging you in...", true, false);
        FBMgr.gi().meRequest(new Runnable() {
            @Override
            public void run() {
                if(FBMgr.gi().getUser() == null) {
                    Toast.makeText(StartActivity.this,"Couldn't connect to facebook", Toast.LENGTH_LONG).show();
                    progress.dismiss();
                    return;
                }
//                If user has no photo
//                if(ParseUser.getCurrentUser().get(Enums.ParseKey.USER_FB_PHOTOS) == null){
//                    FBMgr.gi().getPhotos();
//                }
                //Save name and fbid if them weren't saved before
                if(ParseUser.getCurrentUser().get(Enums.ParseKey.USER_FBID) == null ||
                        ParseUser.getCurrentUser().getBoolean(Enums.ParseKey.USER_VISIBLE) == false){
                    ParseUser.getCurrentUser().put(Enums.ParseKey.USER_FBID, FBMgr.gi().getUser().getId());
                    ParseUser.getCurrentUser().put(Enums.ParseKey.USER_NAME, FBMgr.gi().getUser().getName());

                    //visible
                    if(ParseUser.getCurrentUser().getBoolean(Enums.ParseKey.USER_VISIBLE) == false)
                        ParseUser.getCurrentUser().put(Enums.ParseKey.USER_VISIBLE, true);

                    ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null)
                                startMainAct();
                            else {
                                e.printStackTrace();
                                Toast.makeText(StartActivity.this,"Couldn't connect to server", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else
                    startMainAct();

                // Associate the device with a user
                ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                if(installation.get("user") == null){
                    installation.put(Enums.ParseKey.INSTAL_USER, ParseUser.getCurrentUser());
                    installation.put(Enums.ParseKey.INSTAL_ANDROID_ID, Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                    installation.saveInBackground();
                }

                dismissProgress();
            }
        });
    }

    private void startMainAct(){
        startActivity(MainActivity.class, true, null);
        finish();
    }

    private void dismissProgress(){
        if(progress != null){
            progress.dismiss();
            progress = null;
        }
    }
}
