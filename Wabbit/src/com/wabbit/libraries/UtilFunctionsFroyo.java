package com.wabbit.libraries;

import android.content.Context;
import android.util.Log;

public class UtilFunctionsFroyo {
	//
	public final static String UD_EMAIL = "UD_EMAIL";

	// Requires android.permission.GET_ACCOUNTS
	public static String getAccountEmail(Context ctx) {
		String res = UtilSettings.loadSettingsString(ctx, UD_EMAIL);
		// if (res != null)
		// return res;

		try {
			// user info
			android.accounts.AccountManager am = android.accounts.AccountManager.get(ctx);
			android.accounts.Account[] accounts = am.getAccounts();
			for (android.accounts.Account account : accounts) {
				// TODO: Check possibleEmail against an email regex or treat
				// account.name as an email address only for certain
				// account.type values.
				if (account.type.compareTo("com.google") == 0) {
					res = account.name;

					// String firname = am.getUserData(account,
					// AccountManager.KEY_USERDATA);
					break;
				} else if (account.name.contains("@")) {
					res = account.name;
					break;
				}
			}
		} catch (Exception ex) {
			Log.d("prepopulateFields", ex.toString());
		}

		if (res != null)
			UtilSettings.saveSettingsString(ctx, UD_EMAIL, res);

		return res;
	}
}
