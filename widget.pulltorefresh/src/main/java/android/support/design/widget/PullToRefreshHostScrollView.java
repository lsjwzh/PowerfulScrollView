package android.support.design.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.widget.ScrollerCompatExtend;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.lsjwzh.widget.multirvcontainer.MultiRVScrollView;

import java.util.ArrayList;
import java.util.List;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_UP;

public class PullToRefreshHostScrollView extends MultiRVScrollView {
  private static final String TAG = PullToRefreshHostScrollView.class.getSimpleName();
  private int mTouchSlop;
  private int mLastEventAction;
  private List<RefreshListener> mRefreshListeners = new ArrayList<>();
  private boolean mMoveBeforeTouchRelease;

  public PullToRefreshHostScrollView(Context context) {
    super(context);
    init();
  }

  public PullToRefreshHostScrollView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public PullToRefreshHostScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    final ViewConfiguration configuration = ViewConfiguration.get(getContext());
    mTouchSlop = configuration.getScaledTouchSlop();
    setScroller(new ScrollerCompatExtend(getContext(), null) {
      @Override
      public boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) {
        Log.d(TAG, String.format("springBack startY %s maxY %s", startY, maxY));
        return super.springBack(startX, startY, minX, maxX, minY, maxY);
      }
    });
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
  protected boolean overScrollByCompat(int deltaX, int deltaY, int scrollX, int scrollY, int
      scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean
                                           isTouchEvent) {
    Log.d(TAG, String.format("overScrollByCompat getScrollY() %s", getScrollY()));
    if (getScrollY() == 0 || (isTouchEvent && mMoveBeforeTouchRelease)) {
      tryConsume(deltaY);
      Log.d(TAG, String.format("overScrollByCompat tryConsume %s", deltaY));
    }
    return super.overScrollByCompat(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY,
        maxOverScrollX, maxOverScrollY, isTouchEvent);
  }

  @Override
  protected void onFlingStop() {
    Log.d(TAG, String.format("onFlingStop scrollY %s", getScrollY()));
    getScroller().abortAnimation();
    adjustRefreshViewState();
  }

  private void adjustRefreshViewState() {
    if (isRefreshHeaderExpanded()) {
      getRefreshGroup().getRefreshHeader().moveToStableState(
          getRefreshGroup().getRefreshTargetView(), null);
      for (RefreshListener listener : mRefreshListeners) {
        listener.onRefreshing();
      }
    } else if (getRefreshGroup().getRefreshTargetView().getTranslationY() > 0) {
      getRefreshGroup().getRefreshHeader().collapse(getRefreshGroup().getRefreshTargetView(),
          null);
    }
  }


  public void endRefresh() {
    getRefreshGroup().getRefreshHeader().collapse(getRefreshGroup().getRefreshTargetView(),
        new Runnable() {

          @Override
          public void run() {
            for (RefreshListener listener : mRefreshListeners) {
              listener.onRefreshAnimationEnd();
            }
          }
        });
  }

  public void startRefresh() {
    getRefreshGroup().getRefreshHeader().expand(getRefreshGroup().getRefreshTargetView(),
        new Runnable() {

          @Override
          public void run() {
            for (RefreshListener listener : mRefreshListeners) {
              listener.onRefreshing();
            }
          }
        });
  }


  @Override
  public void stopNestedScroll() {
    super.stopNestedScroll();
    Log.d(TAG, "stopNestedScroll:");
    if (mLastEventAction == ACTION_UP) {
      adjustRefreshViewState();
    }
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    mLastEventAction = ev.getAction();
    if (ev.getAction() == ACTION_UP || ev.getAction() == ACTION_CANCEL) {
      mMoveBeforeTouchRelease = false;
    }
    Log.d(TAG, "dispatchTouchEvent:" + mLastEventAction);
    return super.dispatchTouchEvent(ev);
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
    if (dyUnconsumed != 0 && (getScrollY() == 0 || mMoveBeforeTouchRelease)) {
      if (tryConsume(dyUnconsumed)) {
        return;
      }
    }
    super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
  }

  private boolean isRefreshHeaderExpanded() {
    PullToRefreshGroup refreshChild = getRefreshGroup();
    float translationY = refreshChild.getRefreshTargetView().getTranslationY();
    return translationY >= refreshChild.getRefreshHeader().getMaxHeight();
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
  }

  @Override
  protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
    Log.d(TAG, "onOverScrolled scrollY:" + scrollY + " clampedY:" + clampedY);
    super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    if (mMoveBeforeTouchRelease && scrollY != 0) {
      // 由于super.onOverScrolled会调用scrollTo,会导致在pulltofresh过程中错误的调用scrollTo
      scrollTo(0, 0);
    }
  }

  @Override
  public boolean startNestedScroll(int axes) {
    boolean b = super.startNestedScroll(axes);
    Log.d(TAG, "startNestedScroll:" + b);
    return b;
  }

  @Override
  public void onStopNestedScroll(View target) {
    Log.d(TAG, "onStopNestedScroll");
    super.onStopNestedScroll(target);
  }

  PullToRefreshGroup getRefreshGroup() {
    return (PullToRefreshGroup) getChildAt(0);
  }

  protected boolean tryConsume(int dyUnconsumed) {
    PullToRefreshGroup refreshChild = getRefreshGroup();
    if (refreshChild != null) {
      float translationY = refreshChild.getRefreshTargetView().getTranslationY();
      float mayTranslationY = translationY - dyUnconsumed;
      if ((mayTranslationY > 0 && getScrollY() == 0)
          || mMoveBeforeTouchRelease) {
        mMoveBeforeTouchRelease = true;
        translationY = Math.max(0, mayTranslationY);
        Log.d(TAG, "translationY:" + translationY);
        refreshChild.getRefreshHeader().cancelAnimation();
        refreshChild.getRefreshHeader().setVisibleHeight((int) translationY);
        refreshChild.getRefreshTargetView().setTranslationY(translationY);
        return true;
      }
    }
    return false;
  }

  public interface RefreshListener {
    void onRefreshing();

    void onRefreshAnimationEnd();
  }

  public interface IRefreshHeader {

    void expand(View refreshTargetView, Runnable animationEndCallback);

    void collapse(View refreshTargetView, Runnable animationEndCallback);

    void moveToStableState(View refreshTargetView, Runnable animationEndCallback);

    int getMaxHeight();

    void cancelAnimation();

    void setVisibleHeight(int targetHeight);

  }

}
