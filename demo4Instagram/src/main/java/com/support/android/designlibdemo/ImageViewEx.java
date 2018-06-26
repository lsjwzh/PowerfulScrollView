package com.support.android.designlibdemo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by wenye on 2018/5/17.
 */

public class ImageViewEx extends android.support.v7.widget.AppCompatImageView {
  public ImageViewEx(Context context) {
    super(context);
  }

  public ImageViewEx(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public ImageViewEx(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent event) {
    Log.e("ImageViewEx", "dispatchTouchEvent: " + event.getAction());
    return super.dispatchTouchEvent(event);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return true;
  }

//  @Override
//  public boolean startNestedScroll(int axes) {
//    return false;
//  }
}
