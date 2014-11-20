package com.ppinera.popcorntv.ui_mobile.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class BlockTouchFrameLayout extends FrameLayout {

	private boolean isBlockTouch = false;

	public BlockTouchFrameLayout(Context context) {
		super(context);
	}

	public BlockTouchFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BlockTouchFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (isBlockTouch) {
			return true;
		}

		return super.onInterceptTouchEvent(ev);
	}

	public void setBlockTouchEvent(boolean block) {
		isBlockTouch = block;
	}
}