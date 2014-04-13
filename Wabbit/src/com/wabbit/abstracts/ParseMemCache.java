package com.wabbit.abstracts;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bogdan Tirca on 30.01.2014.
 */
public class ParseMemCache {
    private HashMap<String, ParseUser> parseUserHashMap = new HashMap<String, ParseUser>();
    private HashMap<String, ParseObject> parseObjectHashMap = new HashMap<String, ParseObject>();

    private HashMap<String, ParseObject> generalObjectHashMap = new HashMap<String, ParseObject>();

    private static ParseMemCache obj;
    private ParseMemCache(){
    }
    synchronized public static ParseMemCache gi(){
        if(obj == null){
            obj = new ParseMemCache();
        }
        return obj;
    }

    public void put(String id, ParseObject obj){
        generalObjectHashMap.put(id, obj);
    }
    public ParseObject get(String id){
        return generalObjectHashMap.get(id);
    }

    public void put(ParseUser pUser, boolean toDiskCache){
        parseUserHashMap.put(pUser.getObjectId(), pUser);
        if(toDiskCache){
            Preferences.saveObject(pUser.getObjectId(), pUser);
        }
    }
    public void put(ParseObject pObj, boolean toDiskCache){
        parseObjectHashMap.put(pObj.getObjectId(), pObj);
        if(toDiskCache){
            Preferences.saveObject(pObj.getObjectId(), pObj);
        }
    }

    public void loadUsers(ArrayList<String> ids, final boolean tryDiskCache, final Runnable onFinish){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereContainedIn("objectId", ids);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> parseUsers, ParseException e) {
                if(parseUsers != null){
                    for(ParseUser user : parseUsers)
                        put(user, tryDiskCache);
                    if(onFinish != null)
                        onFinish.run();
                }
            }
        });
    }
    public ParseUser getUser(String id, boolean tryDiskCache){
        ParseUser user = parseUserHashMap.get(id);
        if(user != null)
            return user;    //Memory cache hit
        if(tryDiskCache){
            user = Preferences.getObject(id, ParseUser.class);
            //Save to mem cache
            if(user != null)
                put(user, false);
        }
        return user;
    }
    public ParseObject getObject(String id, boolean tryDiskCache){
        ParseObject obj = parseObjectHashMap.get(id);
        if(obj != null)
            return obj;    //Memory cache hit
        if(tryDiskCache){
            obj = Preferences.getObject(id, ParseUser.class);
            //Save to mem cache
            if(obj != null)
                put(obj, false);
        }
        return obj;
    }

}
