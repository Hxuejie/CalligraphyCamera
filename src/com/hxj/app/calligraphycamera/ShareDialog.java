package com.hxj.app.calligraphycamera;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.hxj.app.calligraphycamera.common.ShareConfig;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.Tencent;
import com.uzmap.pkg.uzcore.UZResourcesIDFinder;

/**
 * 分享弹出框
 * 
 * @author Hxuejie hxuejie@126.com
 */
public class ShareDialog extends Dialog implements android.view.View.OnClickListener {

	private static final String	TAG	= "SHARE";

	private View				qqView;
	private View				qqZoneView;
	private View				wxView;
	private View				wxpyqView;
	private View				sinaView;

	private String				imgUri;
	private Bitmap				img;
	private Activity			activity;
	private Tencent				qqapi;
	private IWXAPI				wxapi;

	public ShareDialog(Activity activity, String imgUri, Bitmap img) {
		super(activity);
		this.activity = activity;
		this.imgUri = imgUri;
		this.img = img;
		this.setTitle(UZResourcesIDFinder.getString("share_dialog_title"));
		this.setContentView(UZResourcesIDFinder.getResLayoutID("share_view"));
		
		qqView = findViewById(UZResourcesIDFinder.getResIdID("share_qq"));
		qqZoneView = findViewById(UZResourcesIDFinder.getResIdID("share_qq_zone"));
		wxView = findViewById(UZResourcesIDFinder.getResIdID("share_wx"));
		wxpyqView = findViewById(UZResourcesIDFinder.getResIdID("share_wx_pyq"));
		sinaView = findViewById(UZResourcesIDFinder.getResIdID("share_sina"));
		
		qqView.setOnClickListener(this);
		qqZoneView.setOnClickListener(this);
		wxView.setOnClickListener(this);
		wxpyqView.setOnClickListener(this);
		sinaView.setOnClickListener(this);

		// 初始化SDK
		new Thread(new Runnable() {
			@Override
			public void run() {
				// QQ
				qqapi = Tencent.createInstance(ShareConfig.QQ_APP_ID,
						ShareDialog.this.activity.getApplicationContext());
				if (qqapi == null) {
					Log.e(TAG, "初始化QQ API失败!!!");
				}
				// 微信
				wxapi = WXAPIFactory.createWXAPI(ShareDialog.this.activity,
						ShareConfig.WX_APP_ID, true);
				if (wxapi == null) {
					Log.e(TAG, "初始化微信 API失败!!!");
				} else {
					if (!wxapi.registerApp(ShareConfig.WX_APP_ID)) {
						Log.e(TAG, "注册微信app 失败!!!");
					}
				}
				Log.i(TAG, "初始化API成功!!!");
			}
		}, "INIT SDK").start();
	}
	
	private void shareToQQ() {
		Log.d(TAG, "share to qq");
		if (qqapi == null) {
			return;
		}
		String url = getRealFilePath(Uri.parse(imgUri));
		final Bundle params = new Bundle();
	    params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
	    params.putString(QQShare.SHARE_TO_QQ_TITLE, UZResourcesIDFinder.getString("app_name"));
	    params.putString(QQShare.SHARE_TO_QQ_SUMMARY,  UZResourcesIDFinder.getString("share_content"));
	    params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  ShareConfig.SHARE_TARGET_URL);
	    params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, url);
	    params.putString(QQShare.SHARE_TO_QQ_APP_NAME,  UZResourcesIDFinder.getString("app_name"));
	    qqapi.shareToQQ(activity, params, null);
		this.cancel();
	}
	
	private void shareToQQZone() {
		Log.d(TAG, "share to qq zone");
		if (qqapi == null) {
			return;
		}
		final String url = getRealFilePath(Uri.parse(imgUri));
		final Bundle params = new Bundle();
	    params.putString(QzoneShare.SHARE_TO_QQ_TITLE, UZResourcesIDFinder.getString("app_name"));
	    params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, UZResourcesIDFinder.getString("share_content"));
	    params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, ShareConfig.SHARE_TARGET_URL);
	    ArrayList<String> imgList = new ArrayList<String>();
	    imgList.add(url);
	    params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imgList);
	    qqapi.shareToQzone(activity, params, null);
		this.cancel();
	}
	
	private void shareToWX() {
		Log.d(TAG, "share to wx");
		if (wxapi == null) {
			return;
		}
		WXImageObject imgObj = new WXImageObject(img);
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imgObj;
		msg.title = UZResourcesIDFinder.getString("app_name");
		msg.description = UZResourcesIDFinder.getString("share_content");
		Bitmap thumbBmp = Bitmap.createScaledBitmap(img, 100, 100, true);
		msg.setThumbImage(thumbBmp);

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		req.scene = SendMessageToWX.Req.WXSceneSession;
		wxapi.sendReq(req);
		this.cancel();
	}
	
	private void shareToPYQ() {
		Log.d(TAG, "share to pyq");
		if (wxapi == null) {
			return;
		}
		WXImageObject imgObj = new WXImageObject(img);
		WXMediaMessage msg = new WXMediaMessage();
		msg.mediaObject = imgObj;
		msg.title = UZResourcesIDFinder.getString("app_name");
		msg.description = UZResourcesIDFinder.getString("share_content");
		Bitmap thumbBmp = Bitmap.createScaledBitmap(img, 100, 100, true);
		msg.setThumbImage(thumbBmp);

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		req.scene = SendMessageToWX.Req.WXSceneTimeline;
		wxapi.sendReq(req);
		this.cancel();
	}
	
	private void shareToSina() {
		Log.d(TAG, "share to sina");
		Toast.makeText(activity, "功能未开放", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onClick(View v) {
		if (v == qqView) {
			shareToQQ();
		} else if (v == qqZoneView) {
			shareToQQZone();
		} else if (v == wxView) {
			shareToWX();
		} else if (v == wxpyqView) {
			shareToPYQ();
		} else if (v == sinaView) {
			shareToSina();
		}
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
			Cursor cursor = this.getContext().getContentResolver().query(uri,
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
