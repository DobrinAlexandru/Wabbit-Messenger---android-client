package com.wabbit.libraries;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UtilSettings {
	// SETTINGS

	public static String loadSettingsString(Context context, String key) {
		SharedPreferences settings = context.getSharedPreferences(key, 0);

		return settings.getString(key, null);
	}

	public static void saveSettingsString(Context context, String key, String val) {
		SharedPreferences settings = context.getSharedPreferences(key, 0);

		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, val);

		editor.commit();
	}

	public static void saveSettingsIntArr(Context ctx, String key, int[] creditsState) {
		if (creditsState == null)
			return;

		saveSettingsString(ctx, key, intArrToString(creditsState, "/"));
	}

	public static int[] loadSettingsIntArr(Context ctx, String key) {
		String str = loadSettingsString(ctx, key);

		return stringToIntArr(str, "/");
	}

	public static int loadSettingsInt(Context context, String key) {
		return loadSettingsInt(context, key, 0);
	}

	public static int loadSettingsInt(Context context, String key, int def) {
		SharedPreferences settings = context.getSharedPreferences(key, 0);

		return settings.getInt(key, def);
	}

	public static void saveSettingsInt(Context context, String key, int newIndex) {
		SharedPreferences settings = context.getSharedPreferences(key, 0);

		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(key, newIndex);

		editor.commit();
	}

	public static SimpleDateFormat DefaultDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

	public static Date loadSettingsDate(Context context, String key) {
		String str = loadSettingsString(context, key);
		if (str == null)
			return null;

		try {
			return DefaultDateFormat.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
			Date dt = new Date();
			saveSettingsDate(context, key, dt);

			return dt;
		}
	}

	public static void saveSettingsDate(Context context, String key, Date val) {
		String str = DefaultDateFormat.format(val);
		saveSettingsString(context, key, str);
	}

	public static final String KEY_FIRST_START = "FIRST_START";

	public static int hoursSinceFirstStart(Context ctx) {
		return hoursSince(ctx, KEY_FIRST_START);
	}

	public static int hoursSince(Context ctx, String key) {
		return hoursSince(ctx, key, true);
	}

	public static int hoursSince(Context ctx, String key, boolean init) {
		Date savedDate = UtilSettings.loadSettingsDate(ctx, key);
		if (savedDate == null) {
			if (init) {
				UtilSettings.saveSettingsDate(ctx, key, new Date());
			}
			return 0;
		}

		return hoursDiff(savedDate, new Date());
	}

	public static boolean loadSettingsBoolean(Context context, String soundState) {
		return loadSettingsInt(context, soundState) == 1;
	}

	public static void saveSettingsBoolean(Context context, String key, boolean val) {
		saveSettingsInt(context, key, val ? 1 : 0);
	}

	public static int hoursDiff(Date from, Date to) {
		int diffHours = (int) ((to.getTime() - from.getTime()) / (1000 * 60 * 60)); // *24 for days diff
		return diffHours;
	}

	public static Date addToDate(Date from, int type, int amountToAdd) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(from);
		cal.add(type, amountToAdd);

		return cal.getTime();
	}

	public static String DateToString(Date dt) {
		if (dt == null)
			return null;

		return DefaultDateFormat.format(dt);
	}

	public static String DateToString(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}

	public static Date StringToDate(String str) {
		try {
			if (str == null)
				return null;

			return DefaultDateFormat.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Date StringToDate(String str, String format) {
		try {
			if (str == null)
				return null;

			return new SimpleDateFormat(format).parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static SimpleDateFormat UsDateTime = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

	public static String DateToUsString(Date expires) {
		return UsDateTime.format(expires);
	}

	public static String intArrToString(int[] arr, String splitter) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			if (i > 0)
				sb.append(splitter);

			sb.append(arr[i]);
		}

		return sb.toString();
	}

	public static int[] stringToIntArr(String str, String splitter) {
		if (str == null)
			return null;

		String[] data = str.split(splitter);
		int[] intData = new int[data.length];
		for (int i = 0; i < data.length; i++) {
			intData[i] = Integer.parseInt(data[i]);
		}

		return intData;
	}

	//

}
