package com.sxu.smartpicture.choosepicture;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;

import com.sxu.permission.CheckPermission;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Freeman
 * @date 17/11/8
 */

public class ChoosePhotoManager {

	private Uri iconUri;
	private Uri cropImageUri;
	private File imageFile;
	private boolean autoCrop = false;
	private OnChoosePhotoListener listener;

	private static final int REQUEST_CODE_TAKE_PHOTO = 1001;
	private static final int REQUEST_CODE_CHOOSE_IMAGE = 1002;
	private static final int REQUEST_CODE_CROP_IMAGE = 1003;

	private static ChoosePhotoManager instance;

	private ChoosePhotoManager() {

	}

	public static ChoosePhotoManager getInstance() {
		if (instance == null) {
			instance = new ChoosePhotoManager();
		}

		return instance;
	}

	public void choosePhotoFromAlbum(Activity activity) {
		choosePhotoFromAlbum(activity, false);
	}

	public void choosePhotoFromAlbum(Activity activity, boolean autoCrop) {
		this.autoCrop = autoCrop;
		Intent intent = new Intent(Intent.ACTION_PICK, null);
		intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
		if (intent.resolveActivity(activity.getPackageManager()) != null) {
			activity.startActivityForResult(intent, REQUEST_CODE_CHOOSE_IMAGE);
		}
	}

	public void takePicture(Activity activity) {
		takePicture(activity, false);
	}

	@CheckPermission(permissions = {Manifest.permission.CAMERA}, permissionDesc = "此功能需要相机权限才可使用")
	public void takePicture(Activity activity, boolean autoCrop) {
		this.autoCrop = autoCrop;
		Date date = new Date();
		String fileName = "IMG_" + new SimpleDateFormat("yyyyMMddHHmmss").format(date);
		Log.i("out", "rootPath=" + Environment.getExternalStorageDirectory() + " cahce==" + activity.getCacheDir());
		imageFile = new File(activity.getExternalCacheDir() + fileName);
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());
		iconUri = Uri.fromFile(imageFile);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, iconUri);
		if (intent.resolveActivity(activity.getPackageManager()) != null) {
			activity.startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
		}
	}

	public void cropPhoto(Activity activity, Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scale", true);
		intent.putExtra("outputX", 300);
		intent.putExtra("outputY", 300);
		// 以Uri的方式传递照片
		File cropFile = new File(activity.getExternalCacheDir() + "crop_image.jpg");
		cropImageUri = Uri.fromFile(cropFile);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, cropImageUri);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		// return-data=true传递的为缩略图，小米手机默认传递大图，所以会导致onActivityResult调用失败
		intent.putExtra("return-data", false);
		intent.putExtra("noFaceDetection", false);
		if (intent.resolveActivity(activity.getPackageManager()) != null) {
			activity.startActivityForResult(intent, REQUEST_CODE_CROP_IMAGE);
		}
	}

	public void onActivityResult(Activity activity, int requestCode, Intent intent) {
		if(intent != null) {
			switch (requestCode) {
				case REQUEST_CODE_TAKE_PHOTO:
					if (autoCrop && imageFile.length() != 0) {
						cropPhoto(activity, iconUri);
					}
					if (listener != null && imageFile.length() != 0) {
						listener.choosePhotoFromCamera(iconUri);
					}
					break;
				case REQUEST_CODE_CHOOSE_IMAGE:
					iconUri = intent.getData();
					if (autoCrop && iconUri != null) {
						cropPhoto(activity, iconUri);
					}
					if (listener != null) {
						listener.choosePhotoFromAlbum(iconUri);
					}
					break;
				case REQUEST_CODE_CROP_IMAGE:
					if (listener != null) {
						listener.cropPhoto(cropImageUri);
					}
					break;
				default:
					break;
			}
		} else {
			// 关闭拍照界面或拍照完成后都会调用
			if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
				if (autoCrop && imageFile.length() != 0) {
					cropPhoto(activity, iconUri);
				}
				if (listener != null && imageFile.length() != 0) {
					listener.choosePhotoFromCamera(iconUri);
				}
			}
		}
	}

	/**
	 * 选择照片或者拍照后是否需要裁剪
	 * @param autoCrop
	 */
	public void setAutoCrop(boolean autoCrop) {
		this.autoCrop = autoCrop;
	}

	public void setChoosePhotoListener(OnChoosePhotoListener listener) {
		this.listener = listener;
	}
}
