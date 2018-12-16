package com.support.android.designlibdemo;

import android.content.Context;
import android.util.AttributeSet;

import com.lsjwzh.widget.PullToRefreshContainer;

public class AppPullToRefreshContainer extends PullToRefreshContainer{
  public AppPullToRefreshContainer(Context context) {
    super(context);
  }

  public AppPullToRefreshContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public AppPullToRefreshContainer(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected int dampConsume(int dyUnconsumed, int type) {
    int dampConsume = super.dampConsume(dyUnconsumed, type);
//    if (type == ViewCompat.TYPE_NON_TOUCH) {
//      return dyUnconsumed;
//    }
    return dampConsume;
  }
}
