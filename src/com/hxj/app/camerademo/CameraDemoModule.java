package com.hxj.app.camerademo;

import android.content.Intent;

import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

/**
 * APICloud模块
 * 
 * @author huangxuejie
 */
public class CameraDemoModule extends UZModule {

	static final int		ACTIVITY_REQUEST_CODE_A	= 100;

	public CameraDemoModule(UZWebView webView) {
		super(webView);
	}

	public void jsmethod_openCamera(UZModuleContext moduleContext) {
		Intent intent = new Intent(getContext(), CameraActivity.class);
		intent.putExtra("url", moduleContext.optString("url"));
		startActivity(intent);
	}

	public void jsmethod_openCameraForResult(UZModuleContext moduleContext) {
		Intent intent = new Intent(getContext(), CameraActivity.class);
		intent.putExtra("url", moduleContext.optString("url"));
		intent.putExtra("needResult", true);
		startActivityForResult(intent, ACTIVITY_REQUEST_CODE_A);
	}

}
