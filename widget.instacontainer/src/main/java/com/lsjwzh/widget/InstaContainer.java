package com.lsjwzh.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.lsjwzh.widget.instacontainer.R;

import com.lsjwzh.widget.multirvcontainer.MultiRVScrollView;

public class InstaContainer extends MultiRVScrollView {
  private static final String TAG = InstaContainer.class.getSimpleName();
  private boolean mHasTakeOverRV;
  private Rect mHeaderRect = new Rect();
  private long mLastStopNestedScrollCallTime;
  private ObjectAnimator mScrollAnimation;
  private int mTopSpaceHeight;

  public InstaContainer(Context context) {
    this(context, null);
  }

  public InstaContainer(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public InstaContainer(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    final TypedArray a = context.obtainStyledAttributes(attrs,
        R.styleable.InstaContainer, defStyleAttr, 0);
    mTopSpaceHeight = a.getDimensionPixelSize(R.styleable.InstaContainer_topSpaceHeight, 0);
    a.recycle();
  }

  public int getTopSpaceHeight() {
    return mTopSpaceHeight;
  }

  public void setTopSpaceHeight(int topSpaceHeight) {
    mTopSpaceHeight = topSpaceHeight;
  }

  @Override
  public boolean startNestedScroll(int axes) {
    // stop first
    if (mScrollAnimation != null) {
      mScrollAnimation.cancel();
      mScrollAnimation = null;
    }
    return super.startNestedScroll(axes);
  }

  @Override
  public void stopNestedScroll() {
    mHasTakeOverRV = false;
    super.stopNestedScroll();
    Log.d(TAG, "stopNestedScroll:");
    if (SystemClock.elapsedRealtime() - mLastStopNestedScrollCallTime < 10) {
      return;
    }
    mLastStopNestedScrollCallTime = SystemClock.elapsedRealtime();

    int gapHeight = getHeight() - getScrollableHeight();
    int maxScrollY = findHeaderView().getHeight() - gapHeight;

    int scrollY = getScrollY();
    // stop first
    scrollBy(0, 0);
    if (mScrollAnimation != null) {
      mScrollAnimation.cancel();
      mScrollAnimation = null;
    }
    if (scrollY < maxScrollY / 2) {
      mScrollAnimation = ObjectAnimator.ofInt(this, "scrollY", 0)
          .setDuration(200);
      mScrollAnimation.start();

    } else {
      mScrollAnimation = ObjectAnimator.ofInt(this, "scrollY", maxScrollY)
          .setDuration(200);
      mScrollAnimation.start();
    }
    Log.d(TAG, "stopNestedScroll getScrollY: " + scrollY);
    Log.d(TAG, "stopNestedScroll maxScrollY: " + maxScrollY);
  }

  @Override
  public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
    super.onNestedPreScroll(target, dx, dy, consumed);
    Log.d(TAG, "onNestedPreScroll dy:" + dy + " consumed:" + consumed[1]);
    if (mHasTakeOverRV) {
      smoothScrollBy(0, dy);
      consumed[1] = dy;
    }
  }


  @Override
  public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int
      dyUnconsumed) {
    final int oldScrollY = getScrollY();
    if (!mHasTakeOverRV) {
      scrollBy(0, dyUnconsumed);
    }
    final int myConsumed = getScrollY() - oldScrollY;
    final int myUnconsumed = dyUnconsumed - myConsumed;
    dispatchNestedScroll(0, myConsumed, 0, myUnconsumed, null);
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    findHeaderView().getGlobalVisibleRect(mHeaderRect);
    if (mHeaderRect.contains((int) ev.getRawX(), (int) ev.getRawY()) && !mHasTakeOverRV) {
      mHasTakeOverRV = true;
    }
    return super.dispatchTouchEvent(ev);
  }

  @Override
  public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
    Log.d(TAG, "onNestedPreFling:" + velocityY);
    return dispatchNestedPreFling(velocityX, velocityY);
  }

  @Override
  public int getScrollableHeight() {
    return super.getScrollableHeight() - mTopSpaceHeight;
  }

  View findHeaderView() {
    return ((ViewGroup) getChildAt(0)).getChildAt(0);
  }

}
