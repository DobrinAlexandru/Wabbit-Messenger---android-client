package com.wabbit.libraries.errorHandling;

import android.app.Activity;

public class Reporter {
    public static PostMortemReportExceptionHandler _damageReport;

	public static void setErrorReporting(Activity cur) {
		_damageReport = new PostMortemReportExceptionHandler(cur);
        _damageReport.run();
        Thread.setDefaultUncaughtExceptionHandler(_damageReport);
	}
	
	public static void reportError(Exception ex) {
		_damageReport.submit(ex);
	}    

	public static void addToLog(String str) {
		_damageReport.addToLog(str);
	}
}
