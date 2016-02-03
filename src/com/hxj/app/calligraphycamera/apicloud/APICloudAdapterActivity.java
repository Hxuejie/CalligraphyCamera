package com.hxj.app.calligraphycamera.apicloud;

import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

import android.app.Activity;
import android.view.View;

/**
 * APICloud适配Activity
 * 
 * @author Hxuejie hxuejie@126.com
 */
public class APICloudAdapterActivity extends Activity {

	/**
	 * adapter: {@link android.app.Activity#setContentView(int)}
	 * 
	 * @param idName
	 */
	protected void setContentView(String idName) {
		this.setContentView(UZResourcesIDFinder.getResLayoutID(idName));
	}

	/**
	 * adapter: {@link android.app.Activity#findViewById(int)}
	 * 
	 * @param idName
	 * @return
	 */
	protected View findViewById(String idName) {
		return findViewById(UZResourcesIDFinder.getResIdID(idName));
	}

	/**
	 * adapter: {@link android.app.Activity#getString(int)}
	 * 
	 * @param resName
	 * @return
	 */
	protected String getString(String resName) {
		return getString(UZResourcesIDFinder.getResStringID(resName));
	}
}
