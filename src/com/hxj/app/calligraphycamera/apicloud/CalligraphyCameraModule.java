package com.hxj.app.calligraphycamera.apicloud;

import android.content.Intent;

import com.hxj.app.calligraphycamera.CameraActivity;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

/**
 * APICloud模块
 * 
 * @author huangxuejie hxuejie@126.com
 */
public class CalligraphyCameraModule extends UZModule {

	static final int ACTIVITY_REQUEST_CODE = 100;

	public CalligraphyCameraModule(UZWebView webView) {
		super(webView);
	}

	/**
	 * 打开相机
	 * @param moduleContext
	 */
	public void jsmethod_openCamera(UZModuleContext moduleContext) {
		Intent intent = new Intent(getContext(), CameraActivity.class);
		intent.putExtra("url", moduleContext.optString("url"));
		startActivity(intent);
	}

	public void jsmethod_openCameraForResult(UZModuleContext moduleContext) {
		Intent intent = new Intent(getContext(), CameraActivity.class);
		intent.putExtra("url", moduleContext.optString("url"));
		intent.putExtra("needResult", true);
		startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
	}

}
