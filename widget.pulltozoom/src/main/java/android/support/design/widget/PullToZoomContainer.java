package android.support.design.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.lsjwzh.widget.multirvcontainer.MultiRVScrollView;

public class PullToZoomContainer extends MultiRVScrollView {
  private static final String TAG = PullToZoomContainer.class.getSimpleName();
  private int mHeaderHeight;
  private int mTouchSlop;

  public PullToZoomContainer(Context context) {
    super(context);
    init();
  }

  public PullToZoomContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public PullToZoomContainer(Context context, AttributeSet attrs, int defStyleAttr) {
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
    if (otherView.getTranslationY() > 0) {
      headerView.animate().scaleY(1.01f).scaleX(1.01f).start();
      otherView.animate().translationY(0).start();
    }
  }

  @Override
  public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
    super.onNestedPreScroll(target, dx, dy, consumed);
    Log.d(TAG, "dy:" + dy + " consumed:" + consumed[1]);
    if (dy < -mTouchSlop && getScrollY() == 0 && consumed[1] == 0) {
      consumed[1] = mTouchSlop + 1 + dy;
    }
  }

  @Override
  public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
    Log.d(TAG, "dyConsumed:" + dyConsumed + " dyUnconsumed:" + dyUnconsumed);
    if (dyUnconsumed != 0) {
      if (tryConsume(dyUnconsumed)) {
        return;
      }
    }
    super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
  }

  protected boolean tryConsume(int dyUnconsumed) {
    View headerView = findHeaderView();
    View otherView = findOtherView();
    if (headerView != null && getScrollY() == 0) {
      int height = headerView.getHeight();
      float translationY = otherView.getTranslationY() - dyUnconsumed;
      int targetHeight = (int) (otherView.getTop() + translationY);
      float scale = targetHeight * 1f / height;
      headerView.setScaleY(Math.max(1, scale));
      headerView.setScaleX(Math.max(1, scale));
      headerView.setPivotY(0f);
      mHeaderHeight = targetHeight;
      Log.d(TAG, "scaleY:" + scale);
      if (translationY > 0) {
        otherView.setTranslationY(translationY);
        return true;
      }
    }
    return false;
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
  protected boolean overScrollByCompat(int deltaX, int deltaY, int scrollX, int scrollY, int
      scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean
                                           isTouchEvent) {
    Log.d(TAG, String.format("overScrollByCompat getScrollY() %s", getScrollY()));
    boolean clamp = super.overScrollByCompat(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
        scrollRangeY,
        maxOverScrollX, maxOverScrollY, isTouchEvent);
    if(clamp || isTouchEvent) {
      tryConsume(deltaY);
    }
    return clamp;
  }

  @Override
  protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
    Log.d(TAG, "onOverScrolled scrollY:" + scrollY + " clampedY:" + clampedY);
    super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    View headerView = findHeaderView();
    if (!clampedY && scrollY > 0 && headerView != null && headerView.getScaleX()> 1.01f) {
      scrollTo(0, 0);
    }
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
