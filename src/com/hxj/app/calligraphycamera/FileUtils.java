package com.hxj.app.calligraphycamera;

import java.util.Date;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;

public class FileUtils {

	/**
	 * 保存照片
	 * 
	 * @param photo
	 * @return img uri
	 */
	public static String savePhoto(Context context,Bitmap photo) {
		String imgUri = MediaStore.Images.Media.insertImage(context.getContentResolver(), photo,
				"CalligraphyCamera", "CalligraphyCamera Photo:" + new Date().toString());
		return imgUri;
	}

	/**
	 * 得到真实的照片路径
	 *
	 * @param context
	 * @param uri
	 * @return
	 */
	public static String getRealFilePath(Context context, Uri uri) {
		if (null == uri)
			return null;
		final String scheme = uri.getScheme();
		String data = null;
		if (scheme == null)
			data = uri.getPath();
		else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			Cursor cursor = context.getContentResolver().query(uri,
					new String[] { ImageColumns.DATA }, null, null, null);
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					int index = cursor.getColumnIndex(ImageColumns.DATA);
					if (index > -1) {
						data = cursor.getString(index);
					}
				}
				cursor.close();
			}
		}
		return data;
	}
}
