package com.hxj.app.calligraphycamera;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.hxj.app.calligraphycamera.thirdparty.ColorPickerDialog;
import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 相机
 * 
 * @author Hxuejie hxuejie@126.com
 */
@SuppressWarnings("deprecation")
public class CameraActivity extends Activity {
	private static final String	TAG			= "CAMERA_ACTIVITY";
	private static final int	COLOR_RANGE	= 20;

	private SurfaceView			cameraView;
	private ImageView			wordView;
	private TextView			watermarkView;

	private Camera				camera;
	private Bitmap				wordImage;
	private Bitmap				filterWordImage;
	private int					backgroundColor	= Color.WHITE;
	private int 				lineColor = Color.RED;
	private String				wordURL;
	private int					photoSize	= 500;
	private Bitmap				photo;
	private Toast				toast;
	private String				photoUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("camera_activity"));
		cameraView = (SurfaceView) findViewById(UZResourcesIDFinder.getResIdID("camnera_cameraview"));
		wordView = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("camera_word"));
		watermarkView = (TextView) findViewById(UZResourcesIDFinder.getResIdID("camera_watermark"));
		
		cameraView.getHolder().addCallback(new Callback() {
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				closeCamera();
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
			}
		});
		
		Intent data = getIntent();
		if (data == null) {
			debug("no intent data");
			return;
		}
		wordURL = data.getStringExtra("url");
		downloadWordImage();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(UZResourcesIDFinder.getResMenuID("main"), menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int itemID = item.getItemId();
		final int bgcolor = UZResourcesIDFinder.getResIdID("menu_bgcolor");
		final int linecolor = UZResourcesIDFinder.getResIdID("menu_linecolor");
		if(itemID ==  bgcolor){
			selectBackgrundColor();
		}else
		if(itemID == linecolor){
			selectLineColor();
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			if (camera != null) {
				camera.autoFocus(null);// 相机自动对焦
			}
			break;
		}
		return super.onTouchEvent(event);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		openCamera();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		closeCamera();
	}

	/**
	 * XML拍照按钮事件回调
	 * 
	 * @param v
	 */
	public void onTakePhoto(View v) {
		// 关闭
		if (camera != null) {
			takePhoto();
			return;
		}
	}
	
	/**
	 * 拍照
	 */
	private void takePhoto() {
		if (camera == null) {
			return;
		}
		debug("take photo");
		camera.takePicture(null, null, new PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				// stop preview
				camera.stopPreview();

				photo = BitmapFactory.decodeByteArray(data, 0, data.length);
				debug("photo size: " + photo.getWidth() + " x "
						+ photo.getHeight());
				Bitmap mixPicture = mixPictures();
				photoUri = savePhoto(mixPicture);

				Intent ret = new Intent();
				ret.putExtra("url", getRealFilePath(Uri.parse(photoUri)));
				setResult(RESULT_OK, ret);
				finish();
			}
		});
	}

	/**
	 * 保存照片
	 * @param img
	 * @return img uri
	 */
	private String savePhoto(Bitmap img) {
		tip(UZResourcesIDFinder.getString("savePhoto"));
		String imgUri = MediaStore.Images.Media.insertImage(
				getContentResolver(), img, "CalligraphyCamera",
				"CalligraphyCamera Photo:" + new Date().toString());
		return imgUri;
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
		int w = photoSize;
		int h = photoSize;
		Bitmap ret = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas c = new Canvas(ret);
		// 画底图
		int pw = photo.getWidth();
		int ph = photo.getHeight();
		int padding = (pw - ph) / 2;
		Rect src = new Rect(padding, 0, pw - padding, ph);
		Rect dst = new Rect(0, 0, w, h);
		Paint p = new Paint();
		p.setAntiAlias(true);
		c.save();
		c.rotate(90, w / 2.0F, h / 2.0F);
		c.drawBitmap(photo, src, dst, p);
		c.restore();
		// 画字
		if (filterWordImage != null) {
			float s = 1.0F * w / filterWordImage.getWidth();
			Matrix matrix = new Matrix();
			matrix.postScale(s, s);
			c.drawBitmap(filterWordImage, matrix, p);
		}
		// 画水印
		String watermark = watermarkView.getText().toString();
		p.setTextAlign(Align.RIGHT);
		p.setTextSize(watermarkView.getTextSize());
		p.setColor(watermarkView.getTextColors().getDefaultColor());
		int dx = -watermarkView.getPaddingRight();
		int dy = -watermarkView.getPaddingBottom();
		c.translate(dx, dy);
		c.drawText(watermark, w, h, p);
		return ret;

	}

	/**
	 * 打开相机
	 */
	private void openCamera() {
		debug("opean camera");
		// open camera
		new Thread(new Runnable() {
			@Override
			public void run() {
				camera = Camera.open(0);
				if (camera == null) {
					debug("camera open fail!!!");
					return;
				}
				debug("camera opened");
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							prepareCamera();
							debug("start preview");
							camera.startPreview();
							showWatermark();
						} catch (IOException e) {
							e.printStackTrace();
							debug("camera preview fail!!!");
						}
					}
				});
			}
		},"OpenCamera").start();
	}
	
	/**
	 * 显示水印
	 */
	private void showWatermark(){
		//设置水印位置
		LayoutParams lp = cameraView.getLayoutParams();
		int width = lp.width;
		int height = lp.height;
		int margin = (height - width) / 2;
		MarginLayoutParams mlp = (MarginLayoutParams)watermarkView.getLayoutParams();
		mlp.bottomMargin = margin;
		watermarkView.setLayoutParams(mlp);
		
		watermarkView.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 配置相机
	 * @throws IOException 
	 */
	private void prepareCamera() throws IOException {
		debug("prepare camera");
		camera.setPreviewDisplay(cameraView.getHolder());
		camera.setDisplayOrientation(90);
		Parameters parameters = camera.getParameters();
		parameters.setJpegQuality(100);
		// set photo size
		List<Size> sizeList = parameters.getSupportedPictureSizes();
		Size size = selectPhotoSize(sizeList, photoSize);
		debug("select photo size: " + size.width + " x " + size.height);
		parameters.setPictureSize(size.width, size.height);
		// set preview size
		sizeList = parameters.getSupportedPreviewSizes();
		int minSize = Math.min(cameraView.getWidth(), cameraView.getHeight());
		size = selectPreviewSize(sizeList, minSize);
		debug("select preview size: " + size.width + " x " + size.height);
		parameters.setPreviewSize(size.width, size.height);
		LayoutParams lp = cameraView.getLayoutParams();
		lp.width = size.height;
		lp.height = size.width;
		cameraView.setLayoutParams(lp);

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
	 * @param list
	 */
	private void sortSizeList(List<Size> list){
		Collections.sort(list, new Comparator<Size>() {
			@Override
			public int compare(Size lhs, Size rhs) {
				if (lhs.width > rhs.width) return 1;
				if (lhs.width < rhs.width) return -1;
				return 0;
			}
		});
	}

	/**
	 * 关闭相机
	 */
	private void closeCamera() {
		debug("close camera");
		if (camera == null) {
			return;
		}
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	/**
	 * 颜色过滤
	 * 
	 * @param src 源图
	 * @param filterColor 过滤颜色
	 * @param colorRange 容差
	 * @return 返回过滤过的图片
	 */
	private Bitmap colorFilter(Bitmap src, int filterColor, int colorRange) {
		if (src == null) {
			return null;
		}
		int w = src.getWidth();
		int h = src.getHeight();
		// 创建可变图片
		Bitmap retImg = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas c = new Canvas(retImg);
		c.drawBitmap(src, 0, 0, new Paint());
		// 过滤颜色
		for (int r = 0; r < h; ++r) {
			for (int l = 0; l < w; ++l) {
				int px = retImg.getPixel(l, r);
				if (colorEqual(px, filterColor, colorRange)) {
					retImg.setPixel(l, r, Color.TRANSPARENT);
				} else {

					retImg.setPixel(l, r, 0x90FFFFFF & px);
				}
			}
		}
		return retImg;
	}

	/**
	 * 比较颜色是否在指定颜色的容差范围内
	 * 
	 * @param color
	 * @param baseColor
	 * @param range
	 * @return
	 */
	private boolean colorEqual(int color, int baseColor, int range) {
		if (Math.abs(Color.red(color) - Color.red(baseColor)) > range) {
			return false;
		}
		if (Math.abs(Color.green(color) - Color.green(baseColor)) > range) {
			return false;
		}
		if (Math.abs(Color.blue(color) - Color.blue(baseColor)) > range) {
			return false;
		}
		return true;
	}

	/**
	 * 选择背景色
	 */
	private void selectBackgrundColor() {
		String title = UZResourcesIDFinder.getString("colorpalette_title");
		int curColor = backgroundColor;
		new ColorPickerDialog(this, curColor, title,
				new ColorPickerDialog.OnColorChangedListener() {
					@Override
					public void colorChanged(int color) {
						if (backgroundColor == color) {
							return;
						}
						backgroundColor = color;
						debug("change background color: " + backgroundColor);
						showWordImage();
					}
				}).show();
	}
	
	/**
	 * 选择标线颜色
	 */
	private void selectLineColor() {
		String title = UZResourcesIDFinder.getString("colorpalette_title");
		int curColor = backgroundColor;
		new ColorPickerDialog(this, curColor, title,
				new ColorPickerDialog.OnColorChangedListener() {
					@Override
					public void colorChanged(int color) {
						if (lineColor == color) {
							return;
						}
						lineColor = color;
						debug("change line color: " + lineColor);
						showWordImage();
					}
				}).show();
	}

	/**
	 * 下载字体图片
	 */
	private void downloadWordImage() {
		if (TextUtils.isEmpty(wordURL)) {
			return;
		}
		debug("download word image: " + wordURL);
		tip(UZResourcesIDFinder.getString("downloading"));
		UrlImageViewHelper.setUrlDrawable(wordView, wordURL,
				new UrlImageViewCallback() {
					@Override
					public void onLoaded(ImageView paramImageView,
							Bitmap paramBitmap, String paramString,
							boolean paramBoolean) {
						if (paramBitmap == null) {
							tip(UZResourcesIDFinder.getString("download_fail"));
							return;
						}
						tip(UZResourcesIDFinder.getString("download_success"));
						wordImage = paramBitmap;
						showWordImage();
					}
				});
	}
	
	/**
	 * 显示字体图片
	 */
	private void showWordImage(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				filterWordImage = colorFilter(wordImage,
						backgroundColor, COLOR_RANGE);
				filterWordImage = colorFilter(filterWordImage,
						lineColor, COLOR_RANGE);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						wordView.setImageBitmap(filterWordImage);
					}
				});
			}
		}, "CreateFilterImage").start();
	}
	
	/**
	 * 得到真实的照片路径
	 *
	 * @param uri
	 * @return
	 */
	private String getRealFilePath(final Uri uri) {
		if (null == uri)
			return null;
		final String scheme = uri.getScheme();
		String data = null;
		if (scheme == null)
			data = uri.getPath();
		else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
			data = uri.getPath();
		} else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
			Cursor cursor = this.getContentResolver().query(uri,
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


	/**
	 * 弹出简短的提示信息
	 * 
	 * @param msg 信息内容
	 */
	private void tip(String msg) {
		if (toast != null) {
			toast.cancel();
		}
		toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		toast.show();
	}

	/**
	 * 输出调试信息到控制台
	 * 
	 * @param msg 调试信息
	 */
	private static void debug(String msg) {
		Log.d(TAG, msg);
	}
	
}
