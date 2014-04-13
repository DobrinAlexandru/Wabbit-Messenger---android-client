package com.wabbit.libraries;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class UtilFunctionsDonut {
	public static boolean androidMinimum(int verCode) {
		return android.os.Build.VERSION.SDK_INT >= verCode;
	}	
	
	public static float convertDpToPixel(float dp,Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float px = dp * (metrics.densityDpi/160f);
	    return px;
	}

	public static float convertPixelsToDp(float px,Context context){
	    Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float dp = px / (metrics.densityDpi / 160f);
	    return dp;

	}
}
