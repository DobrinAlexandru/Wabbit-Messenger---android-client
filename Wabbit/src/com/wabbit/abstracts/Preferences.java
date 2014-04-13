package com.wabbit.abstracts;


import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.wabbit.parse.PMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Preferences {
	public static final String PREFS_NAME = "options";
	
	private static SharedPreferences settings = null;
	private static SharedPreferences.Editor editor = null;

    //User info
    public static final String LAST_USER_TO_CHAT = "lutc";


    //Parse
    public static final String PARSE_USER_LIST_KEY = "pul";
    public static final String PARSE_OBJECT_LIST_KEY = "pol";

    public static final String PARSE_USER_KEY = "pu";
    public static final String PARSE_OBJECT_KEY = "po";

    public static final String READ_MESSAGES_LIST_KEY = "rml";
    public static final String UNREAD_MESSAGES_LIST_KEY = "uml";

    public static final String RECENT_MESSAGES_LIST_KEY = "rrml";
    public static final String RECENT_OBJECT_KEY = "rok";

    public static final String LIST_KEY = "L";
    public static final String LIST_SZ_KEY = "LS";

    private static final int INF = 1 << 24;
	
	public static void setUp(SharedPreferences s, SharedPreferences.Editor e) {
		settings = s;
		editor = e;
	}

    public static void addReadMessage(String partner, PMessage msg){
        addMsg(getCK(READ_MESSAGES_LIST_KEY, partner), msg);
    }
    public static void addReadMessages(String partner, List<PMessage> messages){
        addMsgs(getCK(READ_MESSAGES_LIST_KEY, partner), messages);
    }
    public static ArrayList<PMessage> getReadMessages(String partner){
        return getMsgs(getCK(READ_MESSAGES_LIST_KEY, partner));
    }

    public static void saveUnreadMessages(List<PMessage> messages){
        replaceMsgs(READ_MESSAGES_LIST_KEY, messages);
    }
    public static ArrayList<PMessage> getUnreadMessages(){
        return getMsgs(UNREAD_MESSAGES_LIST_KEY);
    }

    public static void saveRecentMessages(ArrayList<String> list){
        replaceList(RECENT_MESSAGES_LIST_KEY, list, true);
    }
    public static ArrayList<String> getRecentMessages(){
        return getList(RECENT_MESSAGES_LIST_KEY);
    }

    private static void addMsg(String key, PMessage msg){
        //Save object
        saveObject(msg.getObjectId(), msg);
        //Add id to list(Create list of ids)
        addToList(key, msg.getObjectId());
    }
    private static void addMsgs(String key, List<PMessage> messages){
        ArrayList<String> ids = new ArrayList<String>();
        for(PMessage msg : messages){
            saveObject(msg.getObjectId(), msg);
            ids.add(msg.getObjectId());
        }
        addToList(key, ids);
    }
    private static void replaceMsgs(String key, List<PMessage> messages){
        ArrayList<String> ids = new ArrayList<String>();
        for(PMessage msg : messages){
            saveObject(msg.getObjectId(), msg);
            ids.add(msg.getObjectId());
        }
        replaceList(key, ids, true);
    }
    private static ArrayList<PMessage> getMsgs(String key){
        ArrayList<String> ids = getList(key);
        return getObjects(ids, PMessage.class);
    }

    public static <T> void saveRecentMsg(String id, T obj){
        Gson json = new Gson();
        String objJson = json.toJson(obj);
        writeString(getCK(RECENT_OBJECT_KEY, id), objJson);
    }
    public static  <T> T getRecentMsg(String id, Class<T> className){
        String objJson = getString(getCK(RECENT_OBJECT_KEY, id));
        T obj;
        Gson gson = new Gson();
        obj = gson.fromJson(objJson, className);
        return obj;
    }

    public static <T> void saveObject(String id, T obj){
        Gson json = new Gson();
        String objJson = json.toJson(obj);
        writeString(getCK(PARSE_OBJECT_KEY, id), objJson);
    }
    public static  <T> T getObject(String parseId, Class<T> className){
        String objJson = getString(getCK(PARSE_OBJECT_KEY, parseId));
        T obj;
        Gson gson = new Gson();
        obj = gson.fromJson(objJson, className);
        return obj;
    }
    public static <T> ArrayList<T> getObjects(ArrayList<String> ids, Class<T> className){
        ArrayList<T> objects = new ArrayList<T>();
        for(String id : ids)
            objects.add(getObject(id, className));
        return objects;
    }
    /*
    O(n)
     */
    public static void addToList(String listName, String toAdd){
        ArrayList<String> listToAdd = new ArrayList<String>();
        listToAdd.add(toAdd);
        addToList(listName, listToAdd);
    }
    public static void addToList(String listName, ArrayList<String> listToAdd){
        ArrayList<String> old = getList(listName);
        old.addAll(listToAdd);
        replaceList(listName, old, false);
    }
    public static void replaceList(String listName, ArrayList<String> listToSave, boolean clearOldList){
        //Clear old list
        if(clearOldList) {
            ArrayList<String> old = getList(listName);
            for (String id : old)
                removeKey(id);
        }
        Gson json = new Gson();
        String appsStr = json.toJson(listToSave);
        writeString(listName, appsStr);
    }
    public static ArrayList<String> getList(String listName){
        String stringItems = getString(listName);
        ArrayList<String> items = new ArrayList<String>();
        Gson gson = new Gson();
        String []array = gson.fromJson(stringItems, String[].class);
        if(array != null)
            items = new ArrayList<String>( Arrays.asList(array) );
        return items;
    }

    public static void removeKey(String key){
        editor.remove(key);
        editor.commit();
    }
	public static void writeString(String key, String dic){
		editor.putString(getKey(key), dic);
		editor.commit();
	}
	public static void writeBoolean(String key, boolean value){
		editor.putBoolean(key, value);
		editor.commit();
	}
	public static void writeInt(String key, int value){
		editor.putInt(key, value);
		editor.commit();
	}
	
	public static String getString(String key){
		return settings.getString(getKey(key), "");
	}
	public static String getStringOrDeffault(String key, String defaultValue){
		return settings.getString(getKey(key), defaultValue);
	}
	public static boolean getBoolean(String key){
		return settings.getBoolean(key, false);
	}
	public static boolean getBooleanOrDeffault(String key, boolean defaultValue){
		return settings.getBoolean(getKey(key), defaultValue);
	}
	public static int getInt(String key){
		return settings.getInt(key, 0);
	}
	public static int getIntOrDeffault(String key, int defaultValue){
		return settings.getInt(key, defaultValue);
	}
	
	private static String getKey(String s){
		return s;
	}
	
	
	private static String getCK(String x1, int x2){
		return x1 + "/" + x2 + "/";
	}
	private static String getCK(String x1, String x2){
		return x1 + "/" + x2 + "/";
	}
	private static String getCK(int x1, int x2){
		return x1 + "/" + x2 + "/";
	}
    private static String getCK(int x1, int x2, int x3){
        return x1 + "/" + x2 + "/" + x3 + "/";
    }
    private static String getCK(String x1, String x2, int x3){
        return x1 + "/" + x2 + "/" + x3 + "/";
    }

}
