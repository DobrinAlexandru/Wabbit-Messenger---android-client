package com.wabbit.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.wabbit.abstracts.Enums;
import com.wabbit.abstracts.ParseMemCache;
import com.wabbit.activity.BaseActivity;
import com.wabbit.application.BaseApp;
import com.wabbit.interfaces.IHelper;
import com.wabbit.libraries.LinksUtil;
import com.wabbit.libraries.LocationUtils;
import com.wabbit.libraries.UtilFunctions;
import com.wabbit.libraries.cache.BitmapLruCache;
import com.wabbit.libraries.remoting.FBMgr;
import com.wabbit.lists.NearbyUserHolder;
import com.wabbit.lists.UsersListAdapter;
import com.wabbit.lists.UsersListLoader;
import com.wabbit.messenger.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import asynclist.ItemManager;
import bolts.Continuation;
import bolts.Task;
import eu.erikw.PullToRefreshListView;

/**
 * Created by Bogdan Tirca on 26.02.2014.
 */
public class PeopleNearbyFragment extends BaseFragment{
    //List view
    private PullToRefreshListView mListView;
    private UsersListAdapter mListAdapter;
    private ArrayList<NearbyUserHolder> mListItems = new ArrayList<NearbyUserHolder>();

    //Progrress
    ProgressDialog progress;

    //Location
    private LocationInfo locationInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.nearby_layout, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //Location
        locationInfo = new LocationInfo(getActivity());

        createList();

        BaseApp.getApp().mixtrack("List displayed");

//        if(!gpsTracker.canGetLocation()){
//            gpsTracker.showSettingsAlert();
//        }
//        else{
////            gpsTracker.getLocation();
//        LocationLibrary.forceLocationUpdate(getActivity());

    }

    @Override
    public void onStart(){
        super.onStart();
    }
    @Override
    public void onResume(){
        super.onResume();
//            gpsTracker.getLocation();
        //if list not displayed
        if(!loadedOnce) {
            //if location available
            if(isLocationAvailable()){
                //Check released location
                progress = ProgressDialog.show(getActivity(), "Please wait", "Getting your location...", true, true);
                checkLocationAndGeneral(new Runnable() {
                    @Override
                    public void run() {//if is released location
                        loadNearbyPeople(null);
                    }
                });
            }
            //Else
            //  promp dialog to enable location
            //         Activate  -> activate or not -> goes back to onResume
            //         Cancel    -> load people in last known location
        }
        //Force location update
        LocationLibrary.forceLocationUpdate(getActivity());
    }
    @Override
    public void onPause(){
        super.onPause();

        dismissDialog();
    }

    private void loadPeopleWithDialog(){
        if(!loadedOnce)
            progress = ProgressDialog.show(getActivity(), "Please wait", "Getting your location...", true, true);
        loadNearbyPeople(null);
    }

    private void createList(){
        mListView = (PullToRefreshListView) getView().findViewById(R.id.nearbyList);
        mListView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadNearbyPeople(new Runnable() {
                    @Override
                    public void run() {
                        mListView.onRefreshComplete();
                    }
                });
            }
        });
        BitmapLruCache cache = BaseApp.getApp().getBitmapCache();
        UsersListLoader loader = new UsersListLoader(cache);

        ItemManager.Builder builder = new ItemManager.Builder(loader);
        builder.setPreloadItemsEnabled(true).setPreloadItemsCount(5);
        builder.setThreadPoolSize(4);

        mListAdapter = new UsersListAdapter(getActivity(), mListItems);
        mListView.setAdapter(mListAdapter);
        mListView.setItemManager(builder.build());
        mListView.setOnItemClickListener(onItemClick);
    }


    public void onLocationChanged(){
        //If activity is not running, do nothing
        if(((BaseActivity)getActivity()).isRunning() == false)
            return ;
        if(locationInfo == null)
            locationInfo = new LocationInfo(getActivity());
        locationInfo.refresh(getActivity());
        checkLocationAndGeneral(new Runnable() {
            @Override
            public void run() {
                loadNearbyPeople(null);
                saveUserLocation();
            }
        });
    }


    private List<ParseObject> acceptedPoints;
    private int minimumVersion;
    private boolean locationRestriction;
    private void checkLocation(final Runnable onOk){
        final ParseQuery<ParseObject> qAccepted = ParseQuery.getQuery("acceptedLocations");
        qAccepted.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    acceptedPoints = parseObjects;
                    boolean isInLocation = isInLocations(acceptedPoints);
                    if(isInLocation && onOk != null)
                        onOk.run();
                } else {
                    Log.e("locations", "Error: " + e.getMessage());
                    dismissDialog();
                }
            }
        });
    }
    private void checkLocationAndGeneral(final Runnable onOk){
        //Do in parralel:
        //      get accepted locations,
        //      get general settings from server
        //Then
        //      if the version is right
        ArrayList<Task<Void>> paralel = new ArrayList<Task<Void>>();
        final Task<Void>.TaskCompletionSource locationsTask = Task.<Void> create();
        final Task<Void>.TaskCompletionSource generalTask = Task.<Void> create();

        final ParseQuery<ParseObject> qAccepted = ParseQuery.getQuery("acceptedLocations");
        qAccepted.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    acceptedPoints = parseObjects;
                    locationsTask.setResult(null);
                } else {
                    Log.e("locations", "Error: " + e.getMessage());
                    dismissDialog();
                    locationsTask.setError(e);
                }
            }
        });
        final ParseQuery<ParseObject> qGeneral = ParseQuery.getQuery("General");
        qGeneral.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject generalSettings, ParseException e) {
                if (e == null) {
                    if(generalSettings != null) {
                        minimumVersion = generalSettings.getInt("version");
                        locationRestriction = generalSettings.getBoolean("location_restriction");
                    }
                    generalTask.setResult(null);
                } else {
                    generalTask.setError(e);
                }
            }
        });

        paralel.add(locationsTask.getTask());
        paralel.add(generalTask.getTask());
        Task.whenAll(paralel).onSuccess(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
//                Log.d("xxx", UtilFunctions.getVersionCode(getActivity()) + "");
                if(UtilFunctions.getVersionCode(getActivity()) >= minimumVersion) {
                    if(locationRestriction == false){
                        if (onOk != null)
                            onOk.run();
                        return null;
                    }
                    boolean isInLocation = isInLocations(acceptedPoints);
                    if (isInLocation){
                        if(onOk != null)
                            onOk.run();
                    }
                    else
                        dismissDialog();
                }
                else{
                    dismissDialog();
                    showNeedUpdate();
                }
                return null;
            }
        });
    }
    private boolean isInLocations(List<ParseObject> parseObjects) {
        if(parseObjects == null)
            return false;
        Log.d("locations", "Retrieved " + parseObjects.size() + " locations");
        boolean isReleasedAtLocation = false;
        for(ParseObject po : parseObjects){
            if(po.getBoolean(Enums.ParseKey.PLACE_RELEASED)) {
                ParseGeoPoint point = po.getParseGeoPoint(Enums.ParseKey.USER_LOCATION);
                double radius = po.getDouble(Enums.ParseKey.PLACE_RADIUS); //KM
                float[] results = new float[3];
                Location.distanceBetween(point.getLatitude(), point.getLongitude(),
                        locationInfo.lastLat, locationInfo.lastLong, results);
                Log.d("distance", Float.toString(results[0]));

                if(results[0] <= radius * 1000) {
                    isReleasedAtLocation = true;
                    break;
                }
            }
        }
        if(!isReleasedAtLocation) {
            showNotReleasedAtLocationDialog();
            dismissDialog();
            return false;
        }
        else
            return true;
    }
    private void showNotReleasedAtLocationDialog(){
        final EditText inviteCode = new EditText(getActivity());
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.error_not_available)
                .setMessage(R.string.dialog_not_released)
                .setView(inviteCode)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().finish();
                        Log.d("invite code", inviteCode.getText().toString());
                    }
                })
                .setCancelable(false)
                .show();
    }
    private void showNeedUpdate(){
        new AlertDialog.Builder(getActivity())
                .setTitle("Update required")
                .setMessage("In order to continue using Wabbit, please update")
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LinksUtil.linkToApp(getActivity());
                        getActivity().finish();
                    }
                })
                .setCancelable(false)
                .show();
    }
//    private boolean isReleasedAtLocation() {
//        for (ParseGeoPoint point : acceptedPoints){
//            float[] results = new float[3];
//            Location.distanceBetween(point.getLatitude(), point.getLongitude(),
//                    locationInfo.lastLat, locationInfo.lastLong, results);
//            Log.d("distance", Float.toString(results[0]));
//
//            if(results[0] <= 1000)
//                return true;
//        }
//
//        return false;
//    }

    private void saveUserLocation(){
        ParseUser.getCurrentUser().put(Enums.ParseKey.USER_LOCATION,
                new ParseGeoPoint(locationInfo.lastLat, locationInfo.lastLong));
        ParseUser.getCurrentUser().saveInBackground();
    }

    private boolean loadedOnce = false;
    public void loadNearbyPeople(final Runnable onLoaded){
        loadedOnce = true;

        ParseQuery<ParseUser> usersQuery = ParseUser.getQuery();
        ParseGeoPoint userPosition = ParseUser.getCurrentUser().getParseGeoPoint(Enums.ParseKey.USER_LOCATION);
        //Exclude current user
        usersQuery.whereNotEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        usersQuery.whereWithinKilometers(Enums.ParseKey.USER_LOCATION, userPosition, 1);
        usersQuery.whereEqualTo("visible",true);
        usersQuery.setLimit(50);
//        usersQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        usersQuery.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> users, ParseException e) {
                if (e == null) {
                    mListItems.clear();
                    for(ParseUser user : users){
                        String imageUrl = FBMgr.gi().getLinkToProfilePictureByParse(user);
                        String userName = user.getString(Enums.ParseKey.USER_NAME);
                        String distance = LocationUtils.getDistanceFrom(user);
                        int distanceMeters = LocationUtils.getDistanceInMeters(user);
                        NearbyUserHolder item = new NearbyUserHolder(user.getObjectId(), imageUrl, userName, distance, distanceMeters, true);
                        mListItems.add(item);
                        //Cache users
                        ParseMemCache.gi().put(user, false);
                    }
                } else {
                    e.printStackTrace();
                    if(getActivity() != null)
                        Toast.makeText(getActivity(), "Connection error. Please try again!", Toast.LENGTH_LONG).show();
                }
                Collections.sort(mListItems,new Comparator<NearbyUserHolder>() {
                    @Override
                    public int compare(NearbyUserHolder nearbyUserHolder, NearbyUserHolder nearbyUserHolder2) {
                        return nearbyUserHolder.distanceMeters - nearbyUserHolder2.distanceMeters;
                    }
                });
                if(onLoaded != null)
                    onLoaded.run();
                updateList.run();
                dismissDialog();
            }
        });
    }


    private void dismissDialog(){
        if(progress != null) {
            progress.dismiss();
            progress = null;
        }
    }

    private boolean isLocationAvailable() {
        String provider = Settings.Secure.getString(getActivity().getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (!provider.equals("")) {
            return true;
        } else {
            //!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setMessage("For best performances, please enable location");
            dialog.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    getActivity().startActivity(intent);
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Toast.makeText(getActivity(), "Will load last known location. For best performances, please enable location", Toast.LENGTH_LONG).show();
                    loadPeopleWithDialog();
                }
            });
            dialog.setCancelable(false);
            dialog.show();
            return false;
        }
    }

    private AdapterView.OnItemClickListener onItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BaseApp.getApp().mixtrack("Open around me chat");
            if(getActivity() instanceof IHelper){
                String objid = ((NearbyUserHolder)parent.getItemAtPosition(position + 1)).id;
//                                    ((HeaderViewListAdapter)parent.getAdapter()).getWrappedAdapter()
//                                        .getItem(position)).id;

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
//            Toast.makeText(getActivity(), "wtf", Toast.LENGTH_SHORT).show();
//            ((BaseAdapter)mListView.getAdapter()).notifyDataSetChanged();
            mListAdapter.notifyDataSetChanged();
        }
    };
}
