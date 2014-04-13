package com.wabbit.libraries;

import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.wabbit.abstracts.Enums;

/**
 * Created by Bogdan Tirca on 02.04.2014.
 */
public class LocationUtils {
    public static int getDistanceInMeters(ParseUser otherUser){
        //Location
        ParseGeoPoint myLocation = ParseUser.getCurrentUser().getParseGeoPoint(Enums.ParseKey.USER_LOCATION);
        ParseGeoPoint otherLocation = null;
        if(otherUser != null)
            otherLocation = otherUser.getParseGeoPoint(Enums.ParseKey.USER_LOCATION);

        //Calculate distance
        double km = 0;
        if(otherUser != null && otherLocation != null)
            km = myLocation.distanceInKilometersTo(otherLocation);
        return (int)(km * 1000);
    }
    public static String getDistanceFrom(ParseUser otherUser){
        //Location
        ParseGeoPoint myLocation = ParseUser.getCurrentUser().getParseGeoPoint(Enums.ParseKey.USER_LOCATION);
        ParseGeoPoint otherLocation = null;
        if(otherUser != null)
            otherLocation = otherUser.getParseGeoPoint(Enums.ParseKey.USER_LOCATION);

        //Calculate distance
        double km = 0;
        if(otherUser != null && otherLocation != null)
            km = myLocation.distanceInKilometersTo(otherLocation);
        else
            km = Math.random() * 4;
        //Update ui
        if(km < 0.5){
            int m = (int)(km * 100);
            m = Math.max(m, 10);    //10 meters at least
            return String.format("within %d m from you", m);
        }
        else{
            return String.format("within %.1f km from you", km);
        }
    }
}
