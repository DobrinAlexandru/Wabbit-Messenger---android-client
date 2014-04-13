package com.wabbit.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.activeandroid.util.Log;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibraryConstants;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.wabbit.abstracts.Enums;
import com.wabbit.activity.MainActivity;

/**
 * Created by Bogdan Tirca on 23.03.2014.
 */
public class LocationReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            final LocationInfo locationInfo = (LocationInfo) intent.getSerializableExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO);
            ParseUser.getCurrentUser().put(Enums.ParseKey.USER_LOCATION,
                    new ParseGeoPoint(locationInfo.lastLat, locationInfo.lastLong));
            Log.d("Location my", locationInfo.lastLat + " " + locationInfo.lastLong);
            ParseUser.getCurrentUser().saveInBackground();
        }
        catch (Exception e){
            Log.e("location error:", e.getMessage());
        }

        if(MainActivity.gi() != null)
            MainActivity.gi().onLocationUpdate();
    }
}
