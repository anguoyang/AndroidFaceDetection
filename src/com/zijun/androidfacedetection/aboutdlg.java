package com.zijun.androidfacedetection;

import android.content.Context;
import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.widget.TextView;

public class aboutdlg {
	public static AlertDialog create(Context context)
			throws NameNotFoundException {
		// Try to load the a package matching the name of our own package
		PackageInfo pInfo = context.getPackageManager().getPackageInfo(
				context.getPackageName(), PackageManager.GET_META_DATA);
		String versionInfo = pInfo.versionName;

		String aboutTitle = String.format("About %s",
				context.getString(R.string.app_name));
		String versionString = String.format("Version: %s", versionInfo);
		String aboutText = context.getString(R.string.about_text);

		// Set up the TextView
		final TextView message = new TextView(context);


		// Set some padding
		message.setPadding(5, 5, 5, 5);
		// Set up the final string
		message.setText(versionString + "\n\n" + aboutText);

		return new AlertDialog.Builder(context)
				.setTitle(aboutTitle)
				.setCancelable(true)
				.setView(message).create();
}
}
