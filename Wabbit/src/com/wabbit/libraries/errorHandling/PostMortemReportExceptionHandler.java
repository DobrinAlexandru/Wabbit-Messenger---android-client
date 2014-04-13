package com.wabbit.libraries.errorHandling;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

// http://blog.blackmoonit.com/2010/02/android-postmortem-reports-via-email.html
public class PostMortemReportExceptionHandler implements UncaughtExceptionHandler, Runnable {
	public static final String ExceptionReportFilename = "postmortem.trace";

	private static final String MSG_SUBJECT_TAG = "Exception Report"; // "app title + this tag" = email subject
	private static final String MSG_SENDTO = "exception@getwabbit.com"; // email will be sent to this account
	// the following may be something you wish to consider localizing
	private static final String MSG_BODY = "Wabbit CRASHED!\n\n"
			+ "We are sorry to see this happen and would like to fix it! Please help by sending this email!\n\n"
			+ "No personal information is being sent (you can check by reading the rest of the email).\n\n"
			+ "If you have something to add to report please do so - write additional details on how the application crashed (how long you have been using it, are there any repeating steps that result in crash, etc) !!";

	private UncaughtExceptionHandler mDefaultUEH;
	private Activity mApp = null;

	public PostMortemReportExceptionHandler(Activity aApp) {
		mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		mApp = aApp;
	}

	public void uncaughtException(Thread t, Throwable e) {
		submit(e);
		// do not forget to pass this exception through up the chain
		mDefaultUEH.uncaughtException(t, e);
	}

	public String getDebugReport(Throwable aException) {
		//NumberFormat theFormatter = new DecimalFormat("#0.");
		String theErrReport = "";

		// pm
		PackageManager pm = mApp.getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(mApp.getPackageName(), 0);
		} catch (NameNotFoundException eNnf) {
			// doubt this will ever run since we want info about our own package
			pi = new PackageInfo();
			pi.versionName = "unknown";
			pi.versionCode = 69;
		}
		//

		theErrReport += theErrReport += mApp.getPackageName() + ", version " + pi.versionName + " (build "
				+ pi.versionCode + ")\n" + " generated exception:\n";
		theErrReport += aException.toString() + "\n\n";

		// stack trace
		StackTraceElement[] theStackTrace = aException.getStackTrace();
		if (theStackTrace.length > 0) {
			theErrReport += "--------- Stack trace ---------\n";
			for (int i = 0; i < theStackTrace.length; i++) {
				theErrReport += "at " + "\t" + theStackTrace[i].toString() + "\n";
			}// for
			theErrReport += "-------------------------------\n\n";
		}

		// if the exception was thrown in a background thread inside
		// AsyncTask, then the actual exception can be found with getCause
		Throwable theCause = aException.getCause();
		if (theCause != null) {
			theErrReport += "----------- Cause -----------\n";
			theErrReport += theCause.toString() + "\n\n";
			theStackTrace = theCause.getStackTrace();
			for (int i = 0; i < theStackTrace.length; i++) {
				theErrReport += "at " + "\t" + theStackTrace[i].toString() + "\n";
			}// for
			theErrReport += "-----------------------------\n\n";
		}// if

		// app environment
		Date theDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss_zzz");
		theErrReport += "-------- Environment --------\n";
		theErrReport += "Time\t=" + sdf.format(theDate) + "\n";
		theErrReport += "Device\t=" + Build.FINGERPRINT + "\n";
		try {
			Field theMfrField = Build.class.getField("MANUFACTURER");
			theErrReport += "Make\t=" + theMfrField.get(null) + "\n";
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
		theErrReport += "Model\t=" + Build.MODEL + "\n";
		theErrReport += "Product\t=" + Build.PRODUCT + "\n";
		theErrReport += "App\t\t=" + mApp.getPackageName() + ", version " + pi.versionName + " (build "
				+ pi.versionCode + ")\n";
		theErrReport += "Locale=" + mApp.getResources().getConfiguration().locale.getDisplayName() + "\n";

		DisplayMetrics dm = mApp.getResources().getDisplayMetrics();
		theErrReport += "Build.Display=" + Build.DISPLAY + "\n";
		theErrReport += "Resolution=" + dm.widthPixels + "x" + dm.heightPixels + "\n";
		theErrReport += "Density=" + dm.density * 160 + "\n";

		try {
			// Runtime info = Runtime.getRuntime();
			// theErrReport += "TotalMemory=" + info.totalMemory() + "\n";
			// theErrReport += "UsedMemory=" + (info.totalMemory() - info.freeMemory()) + "\n";

			theErrReport += "DeviceHeap=" + Runtime.getRuntime().maxMemory() + "\n";
			theErrReport += "JavaTotalMemory=" + Runtime.getRuntime().totalMemory() + "\n";
			theErrReport += "JavaUsedMemory="
					+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + "\n";

			theErrReport += "NativeTotalMemory=" + android.os.Debug.getNativeHeapSize() + "\n";
			theErrReport += "NativeUsedMemory="
					+ (android.os.Debug.getNativeHeapSize() - android.os.Debug.getNativeHeapFreeSize()) + "\n";
		} catch (Exception e) {
			e.printStackTrace();
		}

		theErrReport += "-----------------------------\n\n";

		theErrReport += "-------- Debug Log --------\n";
		for (String str : _log) {
			theErrReport += str + "\n";
		}

		theErrReport += "-----------------------------\n\n";

		theErrReport += "END REPORT.";
		return theErrReport;
	}

	protected void saveDebugReport(String aReport) {
		// save report to file
		try {
			FileOutputStream theFile = mApp.openFileOutput(ExceptionReportFilename, Context.MODE_PRIVATE);
			theFile.write(aReport.getBytes());
			theFile.close();
		} catch (IOException ioe) {
			// error during error report needs to be ignored, do not wish to start infinite loop
		}
	}

	public void sendDebugReportToAuthor() {
		String theLine = "";
		String theTrace = "";
		try {
			BufferedReader theReader = new BufferedReader(new InputStreamReader(
					mApp.openFileInput(ExceptionReportFilename)));
			while ((theLine = theReader.readLine()) != null) {
				theTrace += theLine + "\n";
			}
			if (sendDebugReportToAuthor(theTrace)) {
				mApp.deleteFile(ExceptionReportFilename);
			}
		} catch (FileNotFoundException eFnf) {
			// nothing to do
		} catch (IOException eIo) {
			// not going to report
		}
	}

	public Boolean sendDebugReportToAuthor(String aReport) {
		if (aReport != null) {
			Intent theIntent = new Intent(Intent.ACTION_SEND);
			String theSubject = mApp.getTitle() + " " + MSG_SUBJECT_TAG;
			String theBody = "\n" + MSG_BODY + "\n\n" + aReport + "\n\n";
			theIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { MSG_SENDTO });
			theIntent.putExtra(Intent.EXTRA_TEXT, theBody);
			theIntent.putExtra(Intent.EXTRA_SUBJECT, theSubject);
			theIntent.setType("message/rfc822");
			Boolean hasSendRecipients = (mApp.getPackageManager().queryIntentActivities(theIntent, 0).size() > 0);
			if (hasSendRecipients) {
				mApp.startActivity(theIntent);
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	public void run() {
		sendDebugReportToAuthor();
	}

	public void submit(Throwable e) {
		String theErrReport = getDebugReport(e);
		saveDebugReport(theErrReport);
		// try to send file contents via email (need to do so via the UI thread)
		mApp.runOnUiThread(this);
		mApp.finish();
	}

	public static SimpleDateFormat DefaultDateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS");
	public static int LOG_MAX_ENTRIES = 25;
	public static boolean LOG_TO_CAT_ALSO = true;
	private ArrayList<String> _log = new ArrayList<String>();

	public void addToLog(String str) {
		String toWrite = DefaultDateFormat.format(new Date()) + " - " + str;
		_log.add(0, toWrite);
		while (_log.size() > LOG_MAX_ENTRIES) {
			_log.remove(_log.size() - 1);
		}

		if (LOG_TO_CAT_ALSO) {
			Log.d("Reporter.Log", toWrite);
		}
	}
}
