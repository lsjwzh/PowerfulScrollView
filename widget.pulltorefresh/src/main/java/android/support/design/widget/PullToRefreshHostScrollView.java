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
import com.lsjwzh.widget.multirvcontainer.NestRecyclerViewHelper;

import java.util.ArrayList;
import java.util.List;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_OUTSIDE;
import static android.view.MotionEvent.ACTION_UP;

public class PullToRefreshHostScrollView extends MultiRVScrollView {
  static final String TAG = PullToRefreshHostScrollView.class.getSimpleName();
  int mTouchSlop;
  int mLastEventAction = ACTION_OUTSIDE;
  List<RefreshListener> mRefreshListeners = new ArrayList<>();
  boolean mMoveBeforeTouchRelease;
  boolean mIsRefreshing = false;

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
  protected void onTopEdgePull(float x, float deltaY) {
    // donothing
  }

  @Override
  protected boolean onTopFlingOverScrollAbsorb(int velocity) {
    return false;
  }

  @Override
  protected boolean overScrollByCompat(int deltaX, int deltaY, int scrollX, int scrollY, int
      scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean
                                           isTouchEvent) {
    Log.d(TAG, String.format("overScrollByCompat getScrollY() %s deltaY: %s", getScrollY(), deltaY));
    if (getScrollY() == 0 || (isTouchEvent && mMoveBeforeTouchRelease)) {
      deltaY = tryConsume(deltaY, true);
      if (deltaY != 0 && mMoveBeforeTouchRelease) {
        PullToRefreshGroup refreshChild = getRefreshGroup();
        float translationY = refreshChild.getRefreshTargetView().getTranslationY();
        float mayTranslationY = translationY - deltaY;
        if (mayTranslationY < 0) {
          for (NestRecyclerViewHelper helper : mNestRecyclerViewHelpers) {
            helper.tryConsumeScroll(deltaY);
          }
          return true;
        }
      }
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

  protected void adjustRefreshViewState() {
    if (canTryMoveToStable()) {
      if (getRefreshGroup().getRefreshHeader().moveToStableState(
          getRefreshGroup().getRefreshTargetView(), null)) {
        notifyOnRefreshing();
      }
    } else if (getRefreshGroup().getRefreshTargetView().getTranslationY() > 0
        && !mIsRefreshing) {
      getRefreshGroup().getRefreshHeader().collapse(getRefreshGroup().getRefreshTargetView(),
          null);
    }
  }

  void notifyOnRefreshing() {
    if (mIsRefreshing) {
      return;
    }
    mIsRefreshing = true;
    for (RefreshListener listener : mRefreshListeners) {
      listener.onRefreshing();
    }
  }


  public boolean isRefreshing() {
    return mIsRefreshing;
  }

  public void endRefresh() {
    getRefreshGroup().getRefreshHeader().collapse(getRefreshGroup().getRefreshTargetView(),
        new Runnable() {

          @Override
          public void run() {
            mIsRefreshing = false;
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
            notifyOnRefreshing();
          }
        });
  }


  @Override
  public void stopNestedScroll() {
    super.stopNestedScroll();
    Log.d(TAG, "stopNestedScroll:");
    if (mLastEventAction == ACTION_UP || mLastEventAction == ACTION_CANCEL) {
      adjustRefreshViewState();
      mLastEventAction = ACTION_OUTSIDE;
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
    int dyUnconsumed = dy - consumed[1];
    Log.d(TAG, " onNestedPreScroll dyConsumed:" + consumed[1] + " dyUnconsumed:" + dyUnconsumed);
    if (dyUnconsumed != 0 && (getScrollY() == 0 || mMoveBeforeTouchRelease)
        && canHandleByHostScrollView(dyUnconsumed)) {
      consumed[1] = dyUnconsumed - tryConsume(dyUnconsumed);
    }
    super.onNestedPreScroll(target, dx, dy, consumed);
    Log.d(TAG, "dy:" + dy + " consumed:" + consumed[1]);
  }

  @Override
  public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int
      dyUnconsumed) {
    Log.d(TAG, "onNestedScroll dyConsumed:" + dyConsumed + " dyUnconsumed:" + dyUnconsumed);
    if (dyUnconsumed != 0 && (getScrollY() == 0 || mMoveBeforeTouchRelease) &&
        canHandleByHostScrollView(dyUnconsumed)) {
      dyUnconsumed = tryConsume(dyUnconsumed);
    }
    super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
  }

  private boolean canTryMoveToStable() {
    PullToRefreshGroup refreshChild = getRefreshGroup();
    float translationY = refreshChild.getRefreshTargetView().getTranslationY();
    return translationY >= refreshChild.getRefreshHeader().getRefreshTriggerHeight();
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

  /**
   *
   * @param dyUnconsumed
   * @return dyUnconsumed
   */
  protected int tryConsume(int dyUnconsumed) {
    return tryConsume(dyUnconsumed, false);
  }

  /**
   *
   * @param dyUnconsumed
   * @return dyUnconsumed
   */
  protected int tryConsume(int dyUnconsumed, boolean limitMaxTranslationY) {
    PullToRefreshGroup refreshChild = getRefreshGroup();
    if (refreshChild != null) {
      float translationY = refreshChild.getRefreshTargetView().getTranslationY();
      float mayTranslationY = translationY - dyUnconsumed;
      if (limitMaxTranslationY
          && translationY >= refreshChild.getRefreshHeader().getRefreshTriggerHeight()) {
        mayTranslationY = translationY;
      }
      if (mayTranslationY > 0 && getScrollY() == 0) {
        mMoveBeforeTouchRelease = true;
      }
      if (mMoveBeforeTouchRelease) {
        if (getScrollY() > 0) {
          int oldScroll = getScrollY();
          scrollBy(0, dyUnconsumed);
          int scroll = getScrollY();
          dyUnconsumed -= scroll - oldScroll;
          mayTranslationY = translationY - dyUnconsumed;
          Log.d(TAG, "scrollBy BeforeTouchRelease:" + (scroll - oldScroll));
        }
        translationY = Math.max(0, mayTranslationY);
        Log.d(TAG, "translationY:" + translationY);
        refreshChild.getRefreshHeader().cancelAnimation();
        refreshChild.getRefreshHeader().setVisibleHeight(refreshChild.getRefreshTargetView(),
            (int) translationY);
        refreshChild.getRefreshTargetView().setTranslationY(translationY);
        return (int) (translationY - mayTranslationY);
      }
    }
    return dyUnconsumed;
  }

  public interface RefreshListener {
    void onRefreshing();

    void onRefreshAnimationEnd();
  }

  public interface IRefreshHeader {

    void expand(View refreshTargetView, Runnable animationEndCallback);

    void collapse(View refreshTargetView, Runnable animationEndCallback);

    /**
     *
     * @param refreshTargetView
     * @param animationEndCallback
     * @return if true trigger refreshing, else just do moveToStableState self
     */
    boolean moveToStableState(View refreshTargetView, Runnable animationEndCallback);

    int getRefreshTriggerHeight();

    void cancelAnimation();

    void setVisibleHeight(View refreshTargetView, int targetHeight);

  }

}
