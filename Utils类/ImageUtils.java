package com.lhdz.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

public class ImageUtils {

	public static final String TAG = ImageUtils.class.getSimpleName();

	/**
	 * 压缩图片基准值，最小边超过160px时,对图片进行压缩。
	 */
	public static final int BASE_SIZE_160 = 160;
	public static final int BASE_SIZE_320 = 320;
	public static final int BASE_SIZE_480 = 480;

	/**
	 * 获取压缩后图片。
	 * 
	 * @param path
	 *            原图路径
	 * @return Bitmap
	 */
	public static Bitmap resize(String path, int baseSize) {
		Bitmap bm = null;
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		// 图片参数。
		BitmapFactory.Options options = new BitmapFactory.Options();
		// 只计算几何尺寸，不返回bitmap，不占内存。
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		// 宽、高。
		int w = options.outWidth;
		int h = options.outHeight;
		Log.i(TAG, "--img src,w:" + w + " h:" + h);
		// 最小边。
		int min = w < h ? w : h;
		// 压缩比。
		int rate = min / baseSize;
		if (rate <= 0) {
			rate = 1;
		}
		// 设置压缩参数。
		options.inSampleSize = rate;
		options.inJustDecodeBounds = false;
		// 压缩。
		bm = BitmapFactory.decodeFile(path, options);
		if (bm != null) {
			Log.i(TAG, "--img dst,w:" + bm.getWidth() + " h:" + bm.getHeight());
		}
		return compressImage(bm);
	}

	/**
	 * 获取相册图片路径。
	 * 
	 * @param context
	 * @param uri
	 *            图片Uri
	 * @return path
	 */
	public static String getAlbumImagePath(Context context, Uri uri) {
		String path = "";
		// 查询的字段
		String[] proj = { MediaStore.Images.Media.DATA };
		// 得到游标
		Cursor cursor = context.getContentResolver().query(uri, proj, null,
				null, null);
		// 获取索引
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		// 根据索引拿字段值
		path = cursor.getString(column_index);
		// 关闭游标
		cursor.close();
		Log.i(TAG, "--path:" + path);
		return path;
	}

	public static InputStream getAlbumInputStream(Context context, Uri uri) {
		try {
			return context.getContentResolver().openInputStream(uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 保存Bitmap
	 * 
	 * @param bitmap
	 *            保存对象
	 * @param dstPath
	 *            文件路径
	 * @param fileName
	 *            文件名
	 */
	public static void saveBitmap(Bitmap bitmap, String dstPath, String fileName) {
		if (bitmap == null) {
			return;
		}
		// 创建目录
		File dir = new File(dstPath);
		if (!dir.exists()) {
			boolean isSucc = dir.mkdirs();
			if (!isSucc) {
				return;
			}
		}
		File imgFile = new File(dstPath, fileName);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(imgFile);
			boolean isSucc = bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
					fos);
			if (isSucc) {
				fos.flush();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			imgFile.delete();
		} catch (IOException e) {
			e.printStackTrace();
			imgFile.delete();
		} finally {
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void showPhoto(ImageView photo, String path) {
		String picturePath = path;
		if (picturePath.equals(""))
			return;
		// 缩放图片, width, height 按相同比例缩放图片
		BitmapFactory.Options options = new BitmapFactory.Options();
		// options 设为true时，构造出的bitmap没有图片，只有一些长宽等配置信息，但比较快，设为false时，才有图片
		options.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(picturePath, options);
		int scale = (int) (options.outWidth / (float) 300);
		if (scale <= 0)
			scale = 1;
		options.inSampleSize = scale;
		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(picturePath, options);
		photo.setImageBitmap(bitmap);
		photo.setMaxHeight(350);
	}
	
	private static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while ( baos.toByteArray().length / 1024>100) {    //循环判断如果压缩后图片是否大于100kb,大于继续压缩        
            baos.reset();//重置baos即清空baos
            options -= 10;//每次都减少10
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中

        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }
}
