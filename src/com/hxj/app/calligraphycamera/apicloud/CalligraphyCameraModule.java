package com.hxj.app.calligraphycamera.apicloud;

import org.json.JSONException;
import org.json.JSONObject;

import com.hxj.app.calligraphycamera.CameraActivity;
import com.uzmap.pkg.uzcore.UZWebView;
import com.uzmap.pkg.uzcore.uzmodule.UZModule;
import com.uzmap.pkg.uzcore.uzmodule.UZModuleContext;

import android.app.Activity;
import android.content.Intent;

/**
 * APICloud模块
 * 
 * @author huangxuejie hxuejie@126.com
 */
public class CalligraphyCameraModule extends UZModule {

	static final int ACTIVITY_REQUEST_CODE = 100;
	private static UZModuleContext uzContext;

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
		uzContext = moduleContext;
		Intent intent = new Intent(getContext(), CameraActivity.class);
		intent.putExtra("url", moduleContext.optString("url"));
		intent.putExtra("needResult", true);
		startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == Activity.RESULT_OK && requestCode == ACTIVITY_REQUEST_CODE){
			String result = data.getStringExtra("url");
			if(null != result && null != uzContext){
				try {
					JSONObject ret = new JSONObject();
					ret.put("url", result);
					uzContext.success(ret, true);
					uzContext = null;
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
