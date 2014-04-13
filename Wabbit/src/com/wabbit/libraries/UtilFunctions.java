package com.wabbit.libraries;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;

public class UtilFunctions {
	public static void sendEmail(Context ctx, String to, String subject, String body, String filePath) {

		try {
			Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { to });
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
			emailIntent.putExtra(Intent.EXTRA_TEXT, body);
			if (filePath != null) {
				emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(filePath));
			}

			emailIntent.setType("message/rfc822");

			ctx.startActivity(Intent.createChooser(emailIntent, "Send mail...").
					setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		} catch (Exception ex) {
			Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { to });
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
			emailIntent.putExtra(Intent.EXTRA_TEXT, body);
			if (filePath != null) {
				// intent.putExtra(Intent.EXTRA_STREAM,
				// Uri.parse("file:///sdcard/file.ext"));
				emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(filePath));
			}

			emailIntent.setType("text/plain");

			ctx.startActivity(Intent.createChooser(emailIntent, "Send mail...").
					setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}
	}

	public static void FireLongToast(final Toast toast, final int numberOfSeconds) {
		Thread t = new Thread() {
			public void run() {
				ShowLongToast = true;
				int count = 0;
				try {
					while (ShowLongToast && count < numberOfSeconds) {
						toast.show();
						sleep(1000);
						count++;
					}
				} catch (Exception e) {
					Log.e("LongToast", "", e);
				}
			}
		};
		t.start();
	}

	public static Boolean ShowLongToast = true;

	public static void ShowMessageBox(Context ctx, String title, String text, String okButtonText,
			OnClickListener okListener, String cancelButtonText, OnClickListener cancelListener) {

		final AlertDialog ad = new AlertDialog.Builder(ctx).create();
		ad.setTitle(title);
		ad.setMessage(text);
		ad.setButton(DialogInterface.BUTTON_POSITIVE, okButtonText, okListener);
		ad.setButton(DialogInterface.BUTTON_NEGATIVE, cancelButtonText, cancelListener);

		ad.show();
	}

	//
	public static String getVersionName(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			return null;
		}
	}

	public static int getVersionCode(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			return 0;
		}
	}

	public static String getPackageAndVersion(Context ctx) {
		String str = ctx.getPackageName() + "-" + UtilFunctions.getVersionName(ctx);
		return str;
	}

	//

	public final static String UD_UUID = "UD_UUID";

	public static String getDeviceId(Context ctx) {
		String res = UtilSettings.loadSettingsString(ctx, UD_UUID);
		if (res == null) {
			TelephonyManager tm = (TelephonyManager) ctx.getSystemService("phone");

			res = tm.getDeviceId();
			if (res == null) {
				res = Installation.id(ctx);
			}

			if (res != null)
				UtilSettings.saveSettingsString(ctx, UD_UUID, res);
		}

		return res;
	}

	public static boolean froyoOrNewer() {
		// SDK_INT is introduced in 1.6 (API Level 4) so code referencing that would fail
		// Also we can't use SDK_INT since some modified ROMs play around with this value, RELEASE is most versatile
		// variable
		if (android.os.Build.VERSION.RELEASE.startsWith("1.") || android.os.Build.VERSION.RELEASE.startsWith("2.0")
				|| android.os.Build.VERSION.RELEASE.startsWith("2.1"))
			return false;

		return true;
	}

	// returns true if current Android OS on device is >= verCode
	public static boolean androidMinimum(int verCode) {
		if (android.os.Build.VERSION.RELEASE.startsWith("1.0"))
			return verCode == 1;
		else if (android.os.Build.VERSION.RELEASE.startsWith("1.1")) {
			return verCode <= 2;
		} else if (android.os.Build.VERSION.RELEASE.startsWith("1.5")) {
			return verCode <= 3;
		} else {
			// SDK_INT is introduced in 1.6 (API Level 4) so code referencing that would fail
			// Also we can't use SDK_INT since some modified ROMs play around with this value, RELEASE is most versatile
			// variable
			return UtilFunctionsDonut.androidMinimum(verCode);
		}
	}

	public static String getAccountEmail(Context ctx) {
		if (froyoOrNewer())
			return UtilFunctionsFroyo.getAccountEmail(ctx);
		else
			return "";
	}

	public static boolean appInstalledOrNot(Context ctx, String packageName) {
		PackageManager pm = ctx.getPackageManager();
		boolean app_installed = false;
		try {
			pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			app_installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			app_installed = false;
		}

		return app_installed;
	}
	
	public static Bitmap openBitmap(String path) {
		Bitmap bmp = null;
		try {
			File f = new File(path);
			FileInputStream fis = new FileInputStream(f);
			bmp = BitmapFactory.decodeStream(fis);
		} catch (Exception e) {
			e.printStackTrace();
			bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_4444);
		}

		return bmp;
	}

}
