package com.hxj.app.calligraphycamera;

import java.util.Date;

import com.hxj.app.calligraphycamera.CalligraphyCamera.OnTakePhotoCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * 相机
 * 
 * @author Hxuejie hxuejie@126.com
 */
public class CameraActivity extends Activity {
	private static final String	TAG					= "CAMERA_ACTIVITY";
	private static final int	COLOR_RANGE			= 20;
	private static final int	WATERMARK_PADDING	= 20;

	private SurfaceView			cameraView;
	private ImageView			wordView;
	private ImageView			gridView;
	private ImageView			watermarkView;

	private CalligraphyCamera	calligraphyCamera;
	private int					bgColor				= Color.WHITE;
	private String				wordURL;
	private String				gridURL;
	private String				watermarkURL;
	private Toast				toast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(UZResourcesIDFinder.getResLayoutID("camera_activity"));
		cameraView = (SurfaceView) findViewById(
				UZResourcesIDFinder.getResIdID("camnera_cameraview"));
		wordView = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("camera_word"));
		gridView = (ImageView) findViewById(UZResourcesIDFinder.getResIdID("camera_wordgrid"));
		watermarkView = (ImageView) findViewById(
				UZResourcesIDFinder.getResIdID("camera_watermark"));

		calligraphyCamera = new CalligraphyCamera(cameraView, 500);
		calligraphyCamera.setShowGrid(true);
		calligraphyCamera.setWatermarkPadding(px2dip(WATERMARK_PADDING));
		calligraphyCamera.setTackPhotoCallback(new OnTakePhotoCallback() {
			@Override
			public void onTakePhoto(Bitmap photo) {
				String photoUri = savePhoto(photo);

				Intent ret = new Intent();
				String path = FileUtils.getRealFilePath(CameraActivity.this, Uri.parse(photoUri));
				ret.putExtra("url", path);
				setResult(RESULT_OK, ret);
				finish();
			}
		});

		Intent data = getIntent();
		if (data == null) {
			Log.d(TAG, "no intent data");
			return;
		}
		wordURL = data.getStringExtra("word_url");
		gridURL = data.getStringExtra("grid_url");
		watermarkURL = data.getStringExtra("watermark_url");
		downloadImages();
		resetWatermarkLocation();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			calligraphyCamera.autoFocus();
			break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		calligraphyCamera.openCamera();
	}

	@Override
	protected void onPause() {
		super.onPause();
		calligraphyCamera.closeCamera();
	}

	/**
	 * XML拍照按钮事件回调
	 * 
	 * @param v
	 */
	public void onTakePhoto(View v) {
		calligraphyCamera.takePhoto();
	}

	/**
	 * XML显示/隐藏米字格
	 * 
	 * @param v
	 */
	public void onToggleGridShow(View v) {
		ToggleButton tb = (ToggleButton) v;
		calligraphyCamera.setShowGrid(tb.isChecked());
		postUpdate();
	}

	/**
	 * 保存照片
	 * 
	 * @param img
	 * @return img uri
	 */
	private String savePhoto(Bitmap img) {
		tip(UZResourcesIDFinder.getString("savePhoto"));
		String imgUri = MediaStore.Images.Media.insertImage(getContentResolver(), img,
				"CalligraphyCamera", "CalligraphyCamera Photo:" + new Date().toString());
		return imgUri;
	}

	/**
	 * 重围水印位置
	 */
	private void resetWatermarkLocation() {
		watermarkView.post(new Runnable() {
			@Override
			public void run() {
				// 设置水印位置
				int width = cameraView.getWidth();
				int height = cameraView.getHeight();
				int margin = (height - width) / 2;
				int padding = px2dip(WATERMARK_PADDING);
				MarginLayoutParams mlp = (MarginLayoutParams) watermarkView.getLayoutParams();
				mlp.bottomMargin = margin + padding;
				mlp.rightMargin = padding;
				watermarkView.setLayoutParams(mlp);
			}
		});
	}

	/**
	 * 下载字体图片
	 */
	private void downloadImages() {
		if (TextUtils.isEmpty(wordURL)) {
			return;
		}
		Log.d(TAG, "download word image: " + wordURL);
		UrlImageViewHelper.setUrlDrawable(wordView, wordURL, new UrlImageViewCallback() {
			@Override
			public void onLoaded(ImageView paramImageView, Bitmap paramBitmap, String paramString,
					boolean paramBoolean) {
				if (paramBitmap == null) {
					tip(UZResourcesIDFinder.getString("download_fail"));
					return;
				}
				tip(UZResourcesIDFinder.getString("download_success"));
				final Bitmap wordImage = paramBitmap;
				new Thread(new Runnable() {
					@Override
					public void run() {
						Bitmap ret = ImageUtils.colorFilter(wordImage, bgColor, COLOR_RANGE);
						calligraphyCamera.setWordPicture(ret);
						postUpdate();
					}
				}, "CreateFilterImage").start();
			}
		});
		Log.d(TAG, "download grid image: " + gridURL);
		UrlImageViewHelper.setUrlDrawable(gridView, gridURL, new UrlImageViewCallback() {
			@Override
			public void onLoaded(ImageView paramImageView, Bitmap paramBitmap, String paramString,
					boolean paramBoolean) {
				if (paramBitmap == null) {
					tip(UZResourcesIDFinder.getString("download_fail"));
					return;
				}
				tip(UZResourcesIDFinder.getString("download_success"));
				final Bitmap gridImage = paramBitmap;

				new Thread(new Runnable() {
					@Override
					public void run() {
						Bitmap ret = ImageUtils.colorFilter(gridImage, bgColor, COLOR_RANGE);
						calligraphyCamera.setGridPicture(ret);
						postUpdate();
					}
				}, "CreateFilterImage").start();
			}
		});
		Log.d(TAG, "download watermark image: " + watermarkURL);
		UrlImageViewHelper.setUrlDrawable(watermarkView, watermarkURL, new UrlImageViewCallback() {
			@Override
			public void onLoaded(ImageView paramImageView, Bitmap paramBitmap, String paramString,
					boolean paramBoolean) {
				if (paramBitmap == null) {
					tip(UZResourcesIDFinder.getString("download_fail"));
					return;
				}
				tip(UZResourcesIDFinder.getString("download_success"));
				calligraphyCamera.setWatermarkPicture(paramBitmap);
				postUpdate();
			}
		});
		tip(UZResourcesIDFinder.getString("downloading"));
	}

	private void postUpdate() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				wordView.setImageBitmap(calligraphyCamera.getWordPicture());
				gridView.setImageBitmap(calligraphyCamera.getGridPicture());
				watermarkView.setImageBitmap(calligraphyCamera.getWatermarkPicture());
				if (calligraphyCamera.isShowGrid()) {
					gridView.setVisibility(View.VISIBLE);
				} else {
					gridView.setVisibility(View.GONE);
				}
			}
		});
	}

	public int px2dip(float pxValue) {
		final float scale = getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
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

}
