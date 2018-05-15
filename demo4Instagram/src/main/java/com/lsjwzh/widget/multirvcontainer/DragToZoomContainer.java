package com.lsjwzh.widget.multirvcontainer;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

public class DragToZoomContainer extends MultiRVScrollView {
  private static final String TAG = DragToZoomContainer.class.getSimpleName();
  private int mHeaderHeight;
  private int mTouchSlop;

  public DragToZoomContainer(Context context) {
    super(context);
    init();
  }

  public DragToZoomContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public DragToZoomContainer(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    final ViewConfiguration configuration = ViewConfiguration.get(getContext());
    mTouchSlop = configuration.getScaledTouchSlop();
  }

  @Override
  public void stopNestedScroll() {
    super.stopNestedScroll();
    Log.d(TAG, "stopNestedScroll:");
    View headerView = findHeaderView();
    View otherView = findOtherView();
  }

  @Override
  public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
    super.onNestedPreScroll(target, dx, dy, consumed);
    Log.d(TAG, "dy:" + dy + " consumed:" + consumed[1]);
//    if (getScrollY() >= findHeaderView().getHeight() - 100) {
//      consumed[1] = dy;
//    }
  }

  @Override
  public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
     super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    //    Log.d(TAG, "dyConsumed:" + dyConsumed + " dyUnconsumed:" + dyUnconsumed);
//    for (NestRecyclerViewHelper helper : mNestRecyclerViewHelpers) {
//      helper.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
//    }
//    final int oldScrollY = getScrollY();
//    int maxScrollY = findHeaderView().getHeight() - 100;
//    scrollBy(0, Math.min(dyUnconsumed, maxScrollY - oldScrollY));
//    final int myConsumed = getScrollY() - oldScrollY;
//    final int myUnconsumed = dyUnconsumed - myConsumed;
//    dispatchNestedScroll(0, myConsumed, 0, myUnconsumed, null);
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    Log.d(TAG, "dispatchDraw:");
    super.dispatchDraw(canvas);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    Log.d(TAG, "onDraw:");
    super.onDraw(canvas);
  }

  @Override
  public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
    Log.d(TAG, "onNestedPreFling:" + velocityY);
    return super.onNestedPreFling(target, velocityX, velocityY);
  }

  @Override
  protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
    Log.d(TAG, "onOverScrolled scrollY:" + scrollY + " clampedY:" + clampedY);
    super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
//    if (clampedY) {
//      if (getScrollY() == 0) {
//        View headerView = findHeaderView();
//        View otherView = findOtherView();
//        if (headerView != null) {
//          int height = headerView.getHeight();
//          int targetHeight = Math.max(mHeaderHeight, mHeaderHeight + mTouchSlop);
//          mHeaderHeight = targetHeight;
//          float translationY = otherView.getTranslationY() + mTouchSlop;
//          if (translationY > 0) {
//            otherView.setTranslationY(translationY);
//          }
//        }
//      }
//    }
  }

  @Override
  public int getScrollableHeight() {
    return super.getScrollableHeight() - 100;
  }

  @Override
  public boolean startNestedScroll(int axes) {
    View headerView = findHeaderView();
    mHeaderHeight = headerView.getHeight();
    boolean b = super.startNestedScroll(axes);
    Log.d(TAG, "startNestedScroll:" + b);
    return b;
  }

  @Override
  public void onStopNestedScroll(View target) {
    Log.d(TAG, "onStopNestedScroll");
    super.onStopNestedScroll(target);
  }

  View findHeaderView() {
    return ((ViewGroup) getChildAt(0)).getChildAt(0);
  }




  View findOtherView() {
    return ((ViewGroup) getChildAt(0)).getChildAt(1);
  }
}
