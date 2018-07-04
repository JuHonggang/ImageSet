package com.sxu.smartpicture.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.sxu.smartpicture.R;

/*******************************************************************************
 * Description: 动态申请权限
 *
 * Author: Freeman
 *
 * Date: 2018/6/8
 *
 * Copyright: all rights reserved by Freeman.
 *******************************************************************************/

public class PermissionUtil {

	private final static int PERMISSION_REQUEST_CODE = 1000;
	private static String permissionDesc;
	private static String permissionSettingDesc;
	private static OnPermissionRequestListener requestListener;

	public static boolean checkPermission(Activity context, String permission) {
		if (ContextCompat.checkSelfPermission(context, permission)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(context, new String[] {permission}, PERMISSION_REQUEST_CODE);
			return false;
		}

		return true;
	}

	public static boolean checkPermission(Activity context, String[] permission) {
		if (permission == null || permission.length == 0) {
			return true;
		}

		for (int i = 0; i < permission.length; i++) {
			if (!checkPermission(context, permission[i])) {
				return false;
			}
		}

		return true;
	}

	public static void requestCallback(final Activity context, int requestCode, String permissions[], int[] grantResults) {
		if (requestCode != PERMISSION_REQUEST_CODE) {
			return;
		}

		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			if (requestListener != null) {
				requestListener.onGranted();
				requestListener = null;
			}
		} else {
			if (ActivityCompat.shouldShowRequestPermissionRationale(context,
					permissions != null && permissions.length > 0 ? permissions[0] : "")) {
				Toast.makeText(context, permissionDesc, Toast.LENGTH_LONG).show();
			} else {
				showSettingPermissionDialog(context);
			}
		}
	}

	private static void showSettingPermissionDialog(final Context context) {
		new AlertDialog.Builder(context, android.R.style.Theme_Holo_Light_Dialog)
				.setMessage(permissionSettingDesc)
				.setNegativeButton(context.getString(R.string.go_to_setting),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
								intent.setData(Uri.fromParts("package", context.getPackageName(), null));
								if (intent.resolveActivity(context.getPackageManager()) != null) {
									context.startActivity(intent);
								} else {
									Toast.makeText(context, context.getString(R.string.permission_setting), Toast.LENGTH_LONG).show();
								}
							}
						})
				.show();
	}

	public static void setPermissionRequestListener(String desc, String settingDesc, OnPermissionRequestListener listener) {
		permissionDesc = desc;
		permissionSettingDesc = settingDesc;
		requestListener = listener;
	}

	public interface OnPermissionRequestListener {
		void onGranted();
	}
}