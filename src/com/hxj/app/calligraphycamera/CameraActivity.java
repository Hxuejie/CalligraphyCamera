package com.hxj.app.calligraphycamera;

import java.io.IOException;
import java.util.List;

import com.hxj.app.calligraphycamera.apicloud.APICloudAdapterActivity;
import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 相机
 * 
 * @author Hxuejie hxuejie@126.com
 */
@SuppressWarnings("deprecation")
public class CameraActivity extends APICloudAdapterActivity {
	private static final String	TAG			= "CAMERA_ACTIVITY";
	private static final int	COLOR_RANGE	= 20;

	private SurfaceView			cameraView;
	private ImageView			wordView;
	private View				colorView;
	private TextView			watermarkView;

	private Camera				camera;
	private Bitmap				wordImage;
	private Bitmap				filterWordImage;
	private int					filterColor;
	private String				wordURL;
	private int					photoSize	= 500;
	private Bitmap				photo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView("camera_activity");
		cameraView = (SurfaceView) findViewById("camnera_cameraview");
		wordView = (ImageView) findViewById("camera_word");
		watermarkView = (TextView) findViewById("camera_watermark");
		colorView = findViewById("camear_color");
		initView();

		Intent data = getIntent();
		if (data == null) {
			debug("no intent data");
			return;
		}
		wordURL = data.getStringExtra("url");
		downloadWordImage();
		prepareCamera();
	}

	/**
	 * 初始化界面
	 */
	private void initView() {
		filterColor = Color.WHITE;
		colorView.setBackgroundColor(filterColor);

		Display display = this.getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		LayoutParams lp = cameraView.getLayoutParams();
		lp.height = width;
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

	/**
	 * XML返回按钮事件回调<BR>
	 * 在这里处理界面返回
	 * 
	 * @param v
	 */
	public void onBack(View v) {
		this.finish();
	}

	/**
	 * XML调色板按钮事件回调<BR>
	 * 在这里调用调色板
	 * 
	 * @param v
	 */
	public void onShowColorPalette(View v) {
		showColorPalette();
	}

	/**
	 * XML相机按钮事件回调
	 * 
	 * @param v
	 */
	public void onCameraOperation(View v) {
		// 关闭
		if (camera != null) {
			takePhoto();
			return;
		}

		// 1.创建过滤图片
		if (wordImage != null) {
			filterWordImage = colorFilter(wordImage, filterColor, COLOR_RANGE);
			wordView.setImageBitmap(filterWordImage);
		} else {
			tip(getString("no_image"));
		}

		// 2.打开相机
		openCamera();
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
				photo = BitmapFactory.decodeByteArray(data, 0, data.length);
				debug("photo size: " + photo.getWidth() + " x "
						+ photo.getHeight());
				Bitmap mixPicture = mixPictures();
				savePicture(mixPicture);
			}
		});
	}

	private void savePicture(Bitmap img) {
		MediaStore.Images.Media.insertImage(getContentResolver(), img, "title",
				"description");
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
		int padding = (ph - pw) / 2;
		Rect src = new Rect(0, padding, pw, ph - padding);
		Rect dst = new Rect(0, 0, w, h);
		Paint p = new Paint();
		p.setAntiAlias(true);
		c.drawBitmap(photo, src, dst, p);
		// 画字
		if (filterWordImage != null) {
			int left = (w - filterWordImage.getWidth()) / 2;
			int top = (h - filterWordImage.getHeight()) / 2;
			c.drawBitmap(filterWordImage, left, top, p);
		}
		// 画水印
		String watermark = watermarkView.getText().toString();
		p.setTextAlign(Align.RIGHT);
		c.drawText(watermark, w, h, p);
		return ret;

	}

	/**
	 * 打开相机
	 */
	private void openCamera() {
		debug("opean camera");
		try {
			camera = Camera.open(0);
			Parameters parameters = camera.getParameters();
			parameters.setJpegQuality(100);
			List<Size> list = parameters.getSupportedPictureSizes();
			Size size = selectPhotoSize(list, photoSize);
			debug("set photo size: " + size.width + " x " + size.height);
			parameters.setPictureSize(size.width, size.height);
			camera.setParameters(parameters);
			camera.setPreviewDisplay(cameraView.getHolder());
			camera.setDisplayOrientation(90);
			camera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 选择照片尺寸
	 * 
	 * @param list
	 * @param baseSize
	 * @return
	 */
	private Size selectPhotoSize(List<Size> list, int baseSize) {
		for (Size size : list) {
			if (size.width >= baseSize && size.height >= baseSize) {
				return size;
			}
		}
		return list.get(0);
	}

	/**
	 * 关闭相机
	 */
	private void closeCamera() {
		debug("close camera");
		if (camera != null) {
			camera.stopPreview();
			camera.release();
			camera = null;
		}
	}

	/**
	 * 配置相机
	 */
	private void prepareCamera() {
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
	 * 显示调色板
	 */
	private void showColorPalette() {
		String title = getString("colorpalette_title");
		int curColor = filterColor;
		new ColorPickerDialog(this, curColor, title,
				new ColorPickerDialog.OnColorChangedListener() {
					@Override
					public void colorChanged(int color) {
						filterColor = color;
						colorView.setBackgroundColor(color);
						debug("change filter color: " + filterColor);
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
		tip(getString("downloading"));
		UrlImageViewHelper.setUrlDrawable(wordView, wordURL,
				new UrlImageViewCallback() {
					@Override
					public void onLoaded(ImageView paramImageView,
							Bitmap paramBitmap, String paramString,
							boolean paramBoolean) {
						if (paramBitmap == null) {
							tip(getString("download_fail"));
							return;
						}
						tip(getString("download_success"));
						wordImage = paramBitmap;
					}
				});
	}

	/**
	 * 弹出简短的提示信息
	 * 
	 * @param msg 信息内容
	 */
	private void tip(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
