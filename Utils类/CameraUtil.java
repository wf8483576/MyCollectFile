package com.lhdz.util;

import java.io.File;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.Toast;

//在onActivityResult方法中根据requestCode和resultCode来获取当前拍照的图片地址。
//注意：这里有个问题，在有些机型当中（如SamsungI939、note2等）遇见了当拍照并存储之后，intent当中得到的data为空：
/**
 * data = null 的情况主要是由于拍照的时候横屏了,导致重新create,
 * 普通的解决方法可以在sharedpreference里面保存拍照文件的路径(onSaveInstance保存),
 * 在onRestoreSaveInstance里面在获取出来. 最简单的可以用fileUtil 里面的一个静态变量保存起来..
 * */

public class CameraUtil {

	private static final String IMAGE_TYPE = "image/*";

	private Context mContext;

	public CameraUtil(Context context) {
		mContext = context;
	}

	/**
	 * 打开照相机
	 * 
	 * @param activity
	 *            当前的activity
	 * @param requestCode
	 *            拍照成功时activity forResult 的时候的requestCode
	 * @param photoFile
	 *            拍照完毕时,图片保存的位置
	 */
	public void openCamera(Activity activity, int requestCode, File photoFile) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
		activity.startActivityForResult(intent, requestCode);
	}

	/**
	 * 本地照片调用
	 * 
	 * @param activity
	 * @param requestCode
	 */
	public void openPhotos(Activity activity, int requestCode) {
		if (openPhotosNormal(activity, requestCode)
				&& openPhotosBrowser(activity, requestCode)
				&& openPhotosFinally())
			;
	}

	/**
	 * PopupMenu打开本地相册.
	 */
	private boolean openPhotosNormal(Activity activity, int actResultCode) {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		 intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
		 IMAGE_TYPE);
		try {
			activity.startActivityForResult(intent, actResultCode);

		} catch (android.content.ActivityNotFoundException e) {

			return true;
		}

		return false;
	}

	/**
	 * 打开其他的一文件浏览器,如果没有本地相册的话
	 */
	private boolean openPhotosBrowser(Activity activity, int requestCode) {
		Toast.makeText(mContext, "没有相册软件，运行文件浏览器", Toast.LENGTH_LONG).show();
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT); // "android.intent.action.GET_CONTENT"
		intent.setType(IMAGE_TYPE); // 查看类型 String IMAGE_UNSPECIFIED =
									// "image/*";
		Intent wrapperIntent = Intent.createChooser(intent, null);
		try {
			activity.startActivityForResult(wrapperIntent, requestCode);
		} catch (android.content.ActivityNotFoundException e1) {
			return true;
		}
		return false;
	}

	/**
	 * 这个是找不到相关的图片浏览器,或者相册
	 */
	private boolean openPhotosFinally() {
		Toast.makeText(mContext, "您的系统没有文件浏览器或则相册支持,请安装！", Toast.LENGTH_LONG)
				.show();
		return false;
	}

	/**
	 * 获取从本地图库返回来的时候的URI解析出来的文件路径
	 * 
	 * @return
	 */
	public static String getPhotoPathByLocalUri(Context context, Intent data) {
		Uri selectedImage = data.getData();
		String[] filePathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = context.getContentResolver().query(selectedImage,
				filePathColumn, null, null, null);
		cursor.moveToFirst();
		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		String picturePath = cursor.getString(columnIndex);
		cursor.close();
		return picturePath;
	}

	/**
	 * 获取图片的绝对路径
	 * 
	 * @param context
	 * @param imageUri
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.KITKAT)
	public static String getImageAbsolutePath(Activity context, Uri imageUri) {
		if (context == null || imageUri == null)
			return null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT
				&& DocumentsContract.isDocumentUri(context, imageUri)) {
			if (isExternalStorageDocument(imageUri)) {
				String docId = DocumentsContract.getDocumentId(imageUri);
				String[] split = docId.split(":");
				String type = split[0];
				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/"
							+ split[1];
				}
			} else if (isDownloadsDocument(imageUri)) {
				String id = DocumentsContract.getDocumentId(imageUri);
				Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));
				return getDataColumn(context, contentUri, null, null);
			} else if (isMediaDocument(imageUri)) {
				String docId = DocumentsContract.getDocumentId(imageUri);
				String[] split = docId.split(":");
				String type = split[0];
				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				String selection = MediaStore.Images.Media._ID + "=?";
				String[] selectionArgs = new String[] { split[1] };
				return getDataColumn(context, contentUri, selection,
						selectionArgs);
			}
		} // MediaStore (and general)
		else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
			// Return the remote address
			if (isGooglePhotosUri(imageUri))
				return imageUri.getLastPathSegment();
			return getDataColumn(context, imageUri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
			return imageUri.getPath();
		}
		return null;
	}

	public static String getDataColumn(Context context, Uri uri,
			String selection, String[] selectionArgs) {
		Cursor cursor = null;
		String column = MediaStore.Images.Media.DATA;
		String[] projection = { column };
		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri
				.getAuthority());
	}
}
