package android.support.design.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.lsjwzh.widget.multirvcontainer.MultiRVScrollView;
import com.lsjwzh.widget.pulltozoom.R;

import java.util.ArrayList;
import java.util.List;

public class PullToZoomContainer extends MultiRVScrollView {
  private static final String TAG = PullToZoomContainer.class.getSimpleName();
  private int mHeaderHeight;
  private int mTouchSlop;
  private int mScaleViewId;
  private int mTranslationViewId;
  private List<RefreshListener> mRefreshListeners = new ArrayList<>();

  public PullToZoomContainer(Context context) {
    super(context);
    init(context, null, 0);
  }

  public PullToZoomContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs, 0);
  }

  public PullToZoomContainer(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs, defStyleAttr);
  }

  void init(Context context, AttributeSet attrs, int defStyleAttr) {
    final ViewConfiguration configuration = ViewConfiguration.get(getContext());
    mTouchSlop = configuration.getScaledTouchSlop();

    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullToZoomContainer);
    mScaleViewId = a.getResourceId(R.styleable.PullToZoomContainer_scaleViewId, View.NO_ID);
    mTranslationViewId
        = a.getResourceId(R.styleable.PullToZoomContainer_translationViewId, View.NO_ID);
    a.recycle();
  }

  public void addRefreshListener(RefreshListener refreshListener) {
    mRefreshListeners.add(refreshListener);
  }

  public void removeRefreshListener(RefreshListener refreshListener) {
    mRefreshListeners.remove(refreshListener);
  }

  public List<RefreshListener> getRefreshListeners() {
    return mRefreshListeners;
  }

  @Override
  protected void onFlingStop() {
    Log.d(TAG, "onFlingStop:");
    super.onFlingStop();
    rollbackIfNeed();
  }

  @Override
  public void stopNestedScroll() {
    super.stopNestedScroll();
    Log.d(TAG, "stopNestedScroll:");
    rollbackIfNeed();
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
  public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int
      dyUnconsumed) {
    Log.d(TAG, "dyConsumed:" + dyConsumed + " dyUnconsumed:" + dyUnconsumed);
    if (dyUnconsumed != 0) {
      if (tryConsume(dyUnconsumed)) {
        return;
      }
    }
    super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
  }

  protected boolean tryConsume(int dyUnconsumed) {
    View headerView = findScaleView();
    View otherView = findTranslationView();
    if (headerView != null && otherView != null && getScrollY() == 0) {
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
        for (RefreshListener listener : mRefreshListeners) {
          listener.onPullOffset(translationY);
        }
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
    if (clamp || isTouchEvent) {
      tryConsume(deltaY);
    }
    return clamp;
  }

  @Override
  protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
    Log.d(TAG, "onOverScrolled scrollY:" + scrollY + " clampedY:" + clampedY);
    super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    View headerView = findScaleView();
    if (!clampedY && scrollY > 0 && headerView != null && headerView.getScaleX() > 1.01f) {
      scrollTo(0, 0);
    }
  }

  @Override
  public boolean startNestedScroll(int axes) {
    View headerView = findScaleView();
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

  protected View findScaleView() {
    if (mScaleViewId != View.NO_ID) {
      return findViewById(mScaleViewId);
    }
    return ((ViewGroup) getChildAt(0)).getChildAt(0);
  }


  protected View findTranslationView() {
    if (mTranslationViewId != View.NO_ID) {
      return findViewById(mTranslationViewId);
    }
    return ((ViewGroup) getChildAt(0)).getChildAt(1);
  }

  private void rollbackIfNeed() {
    View headerView = findScaleView();
    View otherView = findTranslationView();
    if (otherView.getTranslationY() > 0) {
      headerView.animate().scaleY(1.01f).scaleX(1.01f).start();
      otherView.animate().translationY(0).setListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          for (RefreshListener listener : mRefreshListeners) {
            listener.onRollbaclAnimationEnd();
          }
        }
      }).start();
    }
  }

  public interface RefreshListener {
    void onPullOffset(float offset);

    void onRollbaclAnimationEnd();
  }
}
