package com.hxj.app.calligraphycamera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.View;

/**
 * 遮罩
 * 
 * @author Hxuejie hxuejie@126.com
 */
public class MaskView extends View {

	private int		maskColor	= Color.BLACK;
	private Paint	paint		= new Paint();

	public MaskView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int width = getWidth();
		int height = getHeight();
		float padding = (height - width) / 2.0F;
		paint.setColor(Color.WHITE);
		canvas.drawRect(0, padding, width, height - padding, paint);
		canvas.drawColor(maskColor, Mode.XOR);
	}

}
