package com.hxj.app.calligraphycamera;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

public class ImageUtils {

	public static Bitmap mix(Bitmap photo, Bitmap word, Bitmap watermark, int size) {
		if (photo == null) {
			return null;
		}
		// 创建透明图片
		Bitmap ret = Bitmap.createBitmap(size, size, Config.ARGB_8888);
		Canvas c = new Canvas(ret);
		// 画底图
		int pw = photo.getWidth();
		int ph = photo.getHeight();
		int padding = (pw - ph) / 2;
		Rect src = new Rect(padding, 0, pw - padding, ph);
		Rect dst = new Rect(0, 0, size, size);
		Paint p = new Paint();
		p.setAntiAlias(true);
		c.save();
		c.rotate(90, size / 2.0F, size / 2.0F);
		c.drawBitmap(photo, src, dst, p);
		c.restore();
		// 画字
		if (word != null) {
			float s = 1.0F * size / word.getWidth();
			Matrix matrix = new Matrix();
			matrix.postScale(s, s);
			c.drawBitmap(word, matrix, p);
		}
		// 画水印
		// String watermark = watermarkView.getText().toString();
		// p.setTextAlign(Align.RIGHT);
		// p.setTextSize(watermarkView.getTextSize());
		// p.setColor(watermarkView.getTextColors().getDefaultColor());
		// int dx = -watermarkView.getPaddingRight();
		// int dy = -watermarkView.getPaddingBottom();
		// c.translate(dx, dy);
		// c.drawText(watermark, w, h, p);
		return ret;

	}

	/**
	 * 颜色过滤
	 * 
	 * @param src 源图
	 * @param filterColor 过滤颜色
	 * @param colorRange 容差
	 * @return 返回过滤过的图片
	 */
	public static Bitmap colorFilter(Bitmap src, int filterColor, int colorRange) {
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
	private static boolean colorEqual(int color, int baseColor, int range) {
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
}
