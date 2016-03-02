package com.hxj.app.calligraphycamera;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;

/**
 * 封装书法相机
 * 
 * @author Hxuejie hxuejie@126.com
 */
public class CalligraphyCamera {

	public interface OnTakePhotoCallback {
		void onTakePhoto(Bitmap photo);
	}

	private static final String	TAG	= "CalligraphyCamera";

	private Camera				camera;
	private SurfaceView			displayView;
	private int					photoSize;
	private int					watermarkPadding;
	private OnTakePhotoCallback	tackPhotoCallback;
	private boolean				isShowGrid;
	private Bitmap				photo;
	private Bitmap				wordPicture;
	private Bitmap				gridPicture;
	private Bitmap				watermarkPicture;

	public CalligraphyCamera(SurfaceView displayView, int photoSize) {
		super();
		this.displayView = displayView;
		this.photoSize = photoSize;
		displayView.getHolder().addCallback(new Callback() {
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				closeCamera();
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			}
		});
	}

	/**
	 * 打开相机
	 */
	public void openCamera() {
		Log.d(TAG, "opean camera");
		if (!checkOpen()) {
			Log.d(TAG, "unable opean");
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				camera = Camera.open(0);
				if (camera == null) {
					Log.d(TAG, "camera open fail!!!");
					return;
				}
				Log.d(TAG, "camera opened");
				displayView.post(new Runnable() {
					@Override
					public void run() {
						try {
							prepareCamera();
							Log.d(TAG, "start preview");
							camera.startPreview();
						} catch (IOException e) {
							e.printStackTrace();
							Log.d(TAG, "camera preview fail!!!");
						}
					}

				});
			}
		}, "OpenCamera").start();
	}

	/**
	 * 关闭相机
	 */
	public void closeCamera() {
		Log.d(TAG, "close camera");
		if (camera == null) {
			return;
		}
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	/**
	 * 自动对焦
	 */
	public void autoFocus() {
		if (camera != null) {
			camera.autoFocus(null);// 相机自动对焦
		}
	}

	/**
	 * 拍照
	 */
	public void takePhoto() {
		if (camera == null) {
			return;
		}
		Log.d(TAG, "take photo");
		camera.takePicture(null, null, new PictureCallback() {

			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				if (data == null || data.length == 0) {
					return;
				}
				photo = BitmapFactory.decodeByteArray(data, 0, data.length);
				Log.d(TAG, "photo size: " + photo.getWidth() + " x " + photo.getHeight());
				Bitmap mixPicture = mixPictures();

				if (tackPhotoCallback != null) {
					tackPhotoCallback.onTakePhoto(mixPicture);
				}
			}
		});
	}

	/**
	 * 设置拍照回调
	 * 
	 * @param tackPhotoCallback
	 */
	public void setTackPhotoCallback(OnTakePhotoCallback tackPhotoCallback) {
		this.tackPhotoCallback = tackPhotoCallback;
	}

	/**
	 * 设置字帖图片
	 * 
	 * @param wordPicture
	 */
	public void setWordPicture(Bitmap wordPicture) {
		this.wordPicture = wordPicture;
	}

	/**
	 * 设置米字格图片
	 * 
	 * @param gridPicture
	 */
	public void setGridPicture(Bitmap gridPicture) {
		this.gridPicture = gridPicture;
	}

	/**
	 * 设置水印图片
	 * 
	 * @param watermarkPicture
	 */
	public void setWatermarkPicture(Bitmap watermarkPicture) {
		this.watermarkPicture = watermarkPicture;
	}

	/**
	 * 设置是否显示米字格
	 * 
	 * @param isShowGrid true显示，否则不显示
	 */
	public void setShowGrid(boolean isShowGrid) {
		this.isShowGrid = isShowGrid;
	}

	public Bitmap getWordPicture() {
		return wordPicture;
	}

	public Bitmap getGridPicture() {
		return gridPicture;
	}

	public Bitmap getWatermarkPicture() {
		return watermarkPicture;
	}

	public boolean isShowGrid() {
		return isShowGrid;
	}

	public int getWatermarkPadding() {
		return watermarkPadding;
	}

	public void setWatermarkPadding(int watermarkPadding) {
		this.watermarkPadding = watermarkPadding;
	}

	/**
	 * 混合图片
	 * 
	 * @return
	 */
	private Bitmap mixPictures() {
		if (photo == null) {
			return null;
		}
		// 创建透明图片
		Bitmap ret = Bitmap.createBitmap(photoSize, photoSize, Config.ARGB_8888);
		Canvas c = new Canvas(ret);
		// 画底图
		int pw = photo.getWidth();
		int ph = photo.getHeight();
		int padding = (pw - ph) / 2;
		Rect src = new Rect(padding, 0, pw - padding, ph);
		Rect dst = new Rect(0, 0, photoSize, photoSize);
		Paint p = new Paint();
		p.setAntiAlias(true);
		c.save();
		c.rotate(90, photoSize / 2.0F, photoSize / 2.0F);
		c.drawBitmap(photo, src, dst, p);
		c.restore();
		// 画字
		if (wordPicture != null) {
			float s = 1.0F * photoSize / wordPicture.getWidth();
			float h = wordPicture.getHeight() * s;
			float dy = (photoSize - h) / 2;
			Matrix matrix = new Matrix();
			matrix.postScale(s, s);
			c.save();
			c.translate(0, dy);
			c.drawBitmap(wordPicture, matrix, p);
			c.restore();
		}
		// 画框
		if (isShowGrid && gridPicture != null) {
			float s = 1.0F * photoSize / gridPicture.getWidth();
			Matrix matrix = new Matrix();
			matrix.postScale(s, s);
			c.drawBitmap(gridPicture, matrix, p);
		}
		// 画水印
		if (watermarkPicture != null) {
			int x = photoSize - watermarkPicture.getWidth() - watermarkPadding;
			int y = photoSize - watermarkPicture.getHeight() - watermarkPadding;
			c.drawBitmap(watermarkPicture, x, y, p);
		}
		return ret;

	}

	/**
	 * 配置相机
	 * 
	 * @throws IOException
	 */
	private void prepareCamera() throws IOException {
		Log.d(TAG, "prepare camera");
		camera.setPreviewDisplay(displayView.getHolder());
		camera.setDisplayOrientation(90);
		Parameters parameters = camera.getParameters();
		parameters.setJpegQuality(100);
		// set photo size
		List<Size> sizeList = parameters.getSupportedPictureSizes();
		Size size = selectPhotoSize(sizeList, photoSize);
		Log.d(TAG, "select photo size: " + size.width + " x " + size.height);
		parameters.setPictureSize(size.width, size.height);
		// set preview size
		sizeList = parameters.getSupportedPreviewSizes();
		int minSize = Math.min(displayView.getWidth(), displayView.getHeight());
		size = selectPreviewSize(sizeList, minSize);
		Log.d(TAG, "select preview size: " + size.width + " x " + size.height);
		parameters.setPreviewSize(size.width, size.height);
		LayoutParams lp = displayView.getLayoutParams();
		lp.width = size.height;
		lp.height = size.width;
		displayView.setLayoutParams(lp);

		camera.setParameters(parameters);
	}

	/**
	 * 选择照片尺寸
	 * 
	 * @param list
	 * @param minSize
	 * @return 返回大于baseSize的最小的照片尺寸,没有时返回最大的支持的尺寸.当list=null或list为空时返回NULL
	 */
	private Size selectPhotoSize(List<Size> list, int minSize) {
		if (list == null || list.isEmpty()) {
			return null;
		}
		sortSizeList(list);
		for (Size size : list) {
			if (size.width >= minSize && size.height >= minSize) {
				return size;
			}
		}
		return list.get(list.size() - 1);
	}

	/**
	 * 选择预览窗口大小
	 * 
	 * @param list
	 * @param minSize
	 * @return
	 */
	private Size selectPreviewSize(List<Size> list, int minSize) {
		if (list == null || list.isEmpty()) {
			return null;
		}
		sortSizeList(list);
		for (Size size : list) {
			if (size.width >= minSize && size.height >= minSize) {
				return size;
			}
		}
		return list.get(list.size() - 1);
	}

	/**
	 * 从小到大排序
	 * 
	 * @param list
	 */
	private void sortSizeList(List<Size> list) {
		Collections.sort(list, new Comparator<Size>() {
			@Override
			public int compare(Size lhs, Size rhs) {
				if (lhs.width > rhs.width)
					return 1;
				if (lhs.width < rhs.width)
					return -1;
				return 0;
			}
		});
	}

	/**
	 * 检测是否可以打开相机
	 * 
	 * @return
	 */
	private boolean checkOpen() {
		return camera == null && displayView != null;
	}
}
