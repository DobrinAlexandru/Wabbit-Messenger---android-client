package com.wabbit.libraries;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class LinksUtil {

	public static int marketType = 0; // 0 - Google Play, 1 Amazon

	public static void linkToAllApps(Context ctx, String companyName) {
		linkToAllApps(ctx, companyName, ctx.getPackageName());
	}

	public static void linkToAllApps(Context ctx, String companyName, String packageName) {
		Uri uri = null;
		if (marketType == 0)
			// market://search?q=pub:
			uri = Uri.parse("http://play.google.com/store/search?q=pub:" + companyName);
		else if (marketType == 1)
			uri = Uri.parse("http://www.amazon.com/gp/mas/dl/android?p=" + packageName + "&showAll=1");

		if (uri != null)
			launchLink(ctx, uri);
	}

	public static void linkToApp(Context ctx) {
		linkToApp(ctx, ctx.getPackageName());
	}

	public static void linkToApp(Context ctx, String packageName) {
		Uri uri = Uri.parse(getAppLink(ctx, packageName));

		if (uri != null)
			launchLink(ctx, uri);
	}

	protected static void launchLink(Context ctx, Uri uri) {
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri).
				setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(goToMarket);
	}
	
	public static String getAppLink(Context ctx)
	{
		return getAppLink(ctx, ctx.getPackageName());
	}
	
	public static String getAppLink(Context ctx, String packageName) {
		if (marketType == 1)
			return "http://www.amazon.com/gp/mas/dl/android?p=" + packageName;
		else
			return "http://play.google.com/store/apps/details?id=" + packageName;
	}

	public static void share(Context ctx, String shareSubject, String shareBody) {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
		shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

		ctx.startActivity(Intent.createChooser(shareIntent, "Share Wabbit").
				setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}
}
