package com.lsjwzh.widget;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompatExtend;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.lsjwzh.widget.multirvcontainer.MultiRVScrollView;
import com.lsjwzh.widget.pulltorefresh.R;

import java.util.ArrayList;
import java.util.List;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_OUTSIDE;
import static android.view.MotionEvent.ACTION_UP;

public class PullToRefreshContainer extends MultiRVScrollView {
  static final String TAG = PullToRefreshContainer.class.getSimpleName();
  protected boolean mMoveBeforeTouchRelease;
  protected boolean mIsRefreshing = false;
  protected int mTouchSlop;
  protected int mLastEventAction = ACTION_OUTSIDE;
  private List<RefreshListener> mRefreshListeners = new ArrayList<>();

  public PullToRefreshContainer(Context context) {
    super(context);
    init();
  }

  public PullToRefreshContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public PullToRefreshContainer(Context context, AttributeSet attrs, int defStyleAttr) {
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
    Log.d(TAG, String.format("overScrollByCompat getScrollY() %s deltaY: %s", getScrollY(),
        deltaY));
    if (getScrollY() == 0 || (isTouchEvent && mMoveBeforeTouchRelease)) {
      deltaY = tryConsume(deltaY, true);
      if (deltaY != 0 && mMoveBeforeTouchRelease) {
        float translationY = getRefreshTargetView().getTranslationY();
        float mayTranslationY = translationY - deltaY;
        if (mayTranslationY < 0) {
//          for (NestRecyclerViewHelper helper : mNestRecyclerViewHelpers) {
//            helper.tryConsumeScroll(deltaY);
//          }
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
      if (getRefreshLoadingView().moveToStableState(
          getRefreshTargetView(), null)) {
        notifyOnRefreshing();
      }
    } else if (getRefreshTargetView().getTranslationY() > 0
        && !mIsRefreshing) {
      getRefreshLoadingView().collapse(getRefreshTargetView(), null);
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
    getRefreshLoadingView().collapse(getRefreshTargetView(),
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
    getRefreshLoadingView().expand(getRefreshTargetView(),
        new Runnable() {

          @Override
          public void run() {
            notifyOnRefreshing();
          }
        });
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
  public void onNestedPreScroll(View target, int dx, int dy, int[] consumed, int type) {
    int dyUnconsumed = dy - consumed[1];
    Log.d(TAG, " onNestedPreScroll dyConsumed:" + consumed[1] + " dyUnconsumed:" + dyUnconsumed);
    if (isRefreshing()) {
      consumed[1] = dy;
      Log.d(TAG, " onNestedPreScroll eat scroll when refreshing");
      return;
    }
    if (dyUnconsumed != 0 && (getScrollY() == 0 || mMoveBeforeTouchRelease)
        && canHandleByHostScrollView(dyUnconsumed) && type == ViewCompat.TYPE_NON_TOUCH) {
      float translationY = getRefreshTargetView().getTranslationY();
      if (translationY > getLoadingMaxOffsetY()) {
        consumed[1] = dy;
        // 强制停止fling
        ((RecyclerView)target).stopScroll();
        Log.d(TAG, " onNestedPreScroll stop fling");
      }
    }
    super.onNestedPreScroll(target, dx, dy, consumed, type);
    Log.d(TAG, "dy:" + dy + " consumed:" + consumed[1]);
  }

  @Override
  public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int
      dyUnconsumed, int type) {
    Log.d(TAG, "onNestedScroll dyConsumed:" + dyConsumed + " dyUnconsumed:" + dyUnconsumed);
    if (dyUnconsumed != 0 && (getScrollY() == 0 || mMoveBeforeTouchRelease) &&
        canHandleByHostScrollView(dyUnconsumed)) {
      dyUnconsumed = tryConsume(dyUnconsumed);
    }
    super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
  }

  private boolean canTryMoveToStable() {
    float translationY = getRefreshTargetView().getTranslationY();
    return translationY >= getRefreshLoadingView().getRefreshTriggerHeight();
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
  public boolean startNestedScroll(int axes, int type) {
    boolean b = super.startNestedScroll(axes, type);
    Log.d(TAG, "startNestedScroll:" + b);
    return b;
  }

  @Override
  public void stopNestedScroll(int type) {
    Log.d(TAG, "stopNestedScroll:" + type);
    super.stopNestedScroll(type);
    if (type == ViewCompat.TYPE_NON_TOUCH || mLastEventAction == ACTION_UP
        || mLastEventAction == ACTION_CANCEL) {
      adjustRefreshViewState();
      mLastEventAction = ACTION_OUTSIDE;
    }
  }

  public View getRefreshTargetView() {
    return findViewById(R.id.ptr_refreshTargetView);
  }

  public IRefreshLoadingView getRefreshLoadingView() {
    return findViewById(R.id.ptr_refreshLoadingView);
  }

  /**
   * @param dyUnconsumed
   * @return dyUnconsumed
   */
  protected int tryConsume(int dyUnconsumed) {
    return tryConsume(dyUnconsumed, false);
  }

  /**
   * @param dyUnconsumed
   * @return dyUnconsumed
   */
  protected int tryConsume(int dyUnconsumed, boolean limitMaxTranslationY) {
    dyUnconsumed = dampUnconsumed(dyUnconsumed);
    float translationY = getRefreshTargetView().getTranslationY();
    float mayTranslationY = translationY - dyUnconsumed;
    if (limitMaxTranslationY
        && translationY >= getRefreshLoadingView().getRefreshTriggerHeight()) {
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
      getRefreshLoadingView().cancelAnimation();
      getRefreshLoadingView().setVisibleHeight(getRefreshTargetView(), (int) translationY,
          IRefreshLoadingView.MoveType.TOUCH);
      getRefreshTargetView().setTranslationY(translationY);
      return (int) (translationY - mayTranslationY);
    }
    return dyUnconsumed;
  }

  /**
   * You can custom damp logic here
   *
   * @param dyUnconsumed
   * @return dampUnconsumed
   */
  protected int dampUnconsumed(int dyUnconsumed) {
    if (dyUnconsumed > 0) {
      return dyUnconsumed;
    }
    float translationY = getRefreshTargetView().getTranslationY();
    int maxTranslationY = getLoadingMaxOffsetY();
    float dampRatio = 1 - Math.abs(translationY / maxTranslationY);
    dampRatio = Math.max(0.05f, dampRatio);
    return (int) (dampRatio * dyUnconsumed);
  }

  protected int getLoadingMaxOffsetY() {
    return getRefreshLoadingView().getRefreshTriggerHeight() * 2;
  }

  public interface RefreshListener {
    void onRefreshing();

    void onRefreshAnimationEnd();
  }

  public interface IRefreshLoadingView {
    void expand(View refreshTargetView, Runnable animationEndCallback);

    void collapse(View refreshTargetView, Runnable animationEndCallback);

    /**
     * @param refreshTargetView
     * @param animationEndCallback
     * @return if true trigger refreshing, else just do moveToStableState self
     */
    boolean moveToStableState(View refreshTargetView, Runnable animationEndCallback);

    int getRefreshTriggerHeight();

    void cancelAnimation();

    void setVisibleHeight(View refreshTargetView, int targetHeight, MoveType moveType);

    enum MoveType {
      TOUCH, ANIMATION
    }

  }

}
