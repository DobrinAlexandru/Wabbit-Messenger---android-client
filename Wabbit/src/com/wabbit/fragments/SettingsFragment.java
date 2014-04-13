package com.wabbit.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseUser;
import com.wabbit.abstracts.Enums;
import com.wabbit.application.BaseApp;
import com.wabbit.imagesutils.ImageUtils;
import com.wabbit.libraries.Email;
import com.wabbit.libraries.ScrollViewPhoto;
import com.wabbit.libraries.remoting.FBMgr;
import com.wabbit.lists.PhotoAdapter;
import com.wabbit.messenger.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Bogdan Tirca on 19.03.2014.
 */
public class SettingsFragment extends BaseFragment {
    private ViewPager pager;
    private PhotoAdapter adapter;
    private ArrayList<Bitmap> photos = new ArrayList<Bitmap>();

    private ScrollViewPhoto scrollPhoto;
    private ProgressBar loadingCircle;
    private TextView userName;
    private Button logOut;
    private Button sendFeedback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_layout, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        pager = (ViewPager) getView().findViewById(R.id.pager);
        pager.setAdapter(adapter = new PhotoAdapter(getActivity(), photos));

        scrollPhoto = (ScrollViewPhoto) getView().findViewById(R.id.scroll_photo);
        loadingCircle = (ProgressBar) getView().findViewById(R.id.loading_circle);
        userName = (TextView) getView().findViewById(R.id.userName);
        logOut = (Button) getView().findViewById(R.id.log_out_button);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
            }
        });
        sendFeedback = (Button) getView().findViewById(R.id.feedback);
        sendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Email.sendFeedbackEmail(getActivity());
            }
        });

        scrollPhoto.setHeader(pager,
                BaseApp.getApp().getDisplaySize().y * 0.5f,
                BaseApp.getApp().getDisplaySize().y * 0.8f);
        //Update profile
        updateProfile(ParseUser.getCurrentUser());
    }

    private void updateProfile(ParseUser user){
        loadProfilePicture(user);
        userName.setText(user.getString(Enums.ParseKey.USER_NAME));
    }

    /**
     * loads profile picture directly from facebook
     * @param user
     */
    private void loadProfilePicture(final ParseUser user){
        Task.<Bitmap>callInBackground(new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                String link = FBMgr.gi().getLinkToProfilePictureByParse(user,
                        BaseApp.getApp().getDisplaySize().x,
                        BaseApp.getApp().getDisplaySize().x);
                final Bitmap bmp = ImageUtils.downloadBitmap(link);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadingCircle.setVisibility(View.GONE);
                        photos.add(bmp);
                        adapter.notifyDataSetChanged();
                    }
                });
                return bmp;
            }
        });
    }
    /**
     * loads all pictures from Parse. facebook /me/photos
     * @param user
     */
    private void loadProfilePictures(ParseUser user){
        String list = (String)user.get(Enums.ParseKey.USER_FB_PHOTOS);
        JSONArray array = null;
        try {
            array = new JSONArray(list);
            ArrayList<Task<Bitmap>> tasks = new ArrayList<Task<Bitmap>>();
            final ArrayList<Bitmap> bmps = new ArrayList<Bitmap>();

            for(int i = 0; i < Math.min(array.length(), 6); i ++){
                JSONArray images = (JSONArray)((JSONObject)array.get(i)).get("images");
                //take the first one for now
                final String link = (String)((JSONObject)images.get(0)).get("source");
                tasks.add(Task.<Bitmap>callInBackground(new Callable<Bitmap>() {
                    @Override
                    public Bitmap call() throws Exception {
                        Bitmap bmp = ImageUtils.downloadBitmap(link);
                        bmps.add(bmp);
                        return bmp;
                    }
                }));
                Task.whenAll(tasks).continueWith(new Continuation<Void, Object>() {
                    @Override
                    public Object then(Task<Void> task) throws Exception {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                photos.clear();
                                photos.addAll(bmps);
                                adapter.notifyDataSetChanged();
                            }
                        });
                        return null;
                    }
                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void logOut() {
        BaseApp.getApp().mixtrack("Logout");
        ParseUser.getCurrentUser().put("visible",false);
        ParseUser.getCurrentUser().saveInBackground();
        ParseUser.logOut();
        getActivity().finish();
    }

}
