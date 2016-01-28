package com.hxj.app.camerademo;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

/**
 * 相关主界面
 * 
 * @author Hxuejie hxuejie@126.com
 */
@SuppressWarnings("deprecation")
public class CameraActivity extends Activity {
	private static final String	TAG			= "CAMERA_DEMO";
	private static final int	COLOR_RANGE	= 20;

	private SurfaceView			cameraView;
	private ImageView			wordView;
	private View				colorView;

	private Camera				camera;
	private Bitmap				wordBitmap;
	private Bitmap				filterWordBitmap;
	private int					filterColor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		debug("camera activity create");
		setContentView("camera_activity");
		cameraView = (SurfaceView) findViewById("camnera_cameraview");
		wordView = (ImageView) findViewById("camera_word");
		colorView = findViewById("camear_color");
		filterColor = 0xFFFFFFFF;

		Intent data = getIntent();
		String url = data.getStringExtra("url");
		if (!TextUtils.isEmpty(url)) {
			debug("word url:" + url);
			Toast.makeText(this, "正在下载图片...", Toast.LENGTH_SHORT).show();
			UrlImageViewHelper.setUrlDrawable(wordView, url,
					new UrlImageViewCallback() {
						@Override
						public void onLoaded(ImageView paramImageView,
								Bitmap paramBitmap, String paramString,
								boolean paramBoolean) {
							if (paramBitmap == null) {
								Toast.makeText(paramImageView.getContext(),
										"下载失败", Toast.LENGTH_SHORT).show();
								return;
							}
							Toast.makeText(paramImageView.getContext(), "下载成功",
									Toast.LENGTH_SHORT).show();
							wordBitmap = paramBitmap;
						}
					});
		}

		prepareCamera();
	}
	
	private void setContentView(String id) {
		this.setContentView(UZResourcesIDFinder.getResLayoutID(id));
	}
	
	private View findViewById(String id) {
		return findViewById(UZResourcesIDFinder.getResIdID(id));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			if (camera != null) {
				camera.autoFocus(null);
			}
			break;
		}
		return super.onTouchEvent(event);
	}

	public void onBack(View v) {
		this.finish();
	}

	public void onShowColorDialog(View v) {
		int color = ((ColorDrawable) colorView.getBackground()).getColor();
		new ColorPickerDialog(this, color, "底色选择",
				new ColorPickerDialog.OnColorChangedListener() {
					@Override
					public void colorChanged(int color) {
						filterColor = color;
						colorView.setBackgroundColor(color);
					}
				}).show();
		;
	}

	/**
	 * 对比按钮点击事件回调
	 * 
	 * @param v
	 */
	public void onCompare(View v) {
		debug("open camera");

		// 关闭
		if (camera != null) {
			closeCamera();
			return;
		}

		if (wordBitmap == null) {
			debug("no image");
			Toast.makeText(this, "没有对比图片", Toast.LENGTH_SHORT).show();
			return;
		}
		filterWordBitmap = Bitmap.createBitmap(wordBitmap.getWidth(),
				wordBitmap.getHeight(), Config.ARGB_8888);
		Canvas c = new Canvas(filterWordBitmap);
		c.drawBitmap(wordBitmap, 0, 0, new Paint());
		colorFilter(filterWordBitmap);
		wordView.setImageBitmap(filterWordBitmap);
		openCamera();
	}

	/**
	 * 打开相机
	 */
	private void openCamera() {
		try {
			camera = Camera.open(0);
			camera.setPreviewDisplay(cameraView.getHolder());
			camera.setDisplayOrientation(90);
			camera.startPreview();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	 * @param src
	 */
	private void colorFilter(Bitmap src) {
		int w = src.getWidth();
		int h = src.getHeight();
		for (int r = 0; r < h; ++r) {
			for (int l = 0; l < w; ++l) {
				int px = src.getPixel(l, r);
				if (colorEqual(px, filterColor, COLOR_RANGE)) {
					src.setPixel(l, r, 0x00000000);
				} else {
					src.setPixel(l, r, 0x90FFFFFF & px);
				}
			}
		}
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
	 * 输出调试信息
	 * 
	 * @param msg
	 */
	private static void debug(String msg) {
		Log.d(TAG, msg);
	}
}
