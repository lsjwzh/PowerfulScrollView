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
import com.lsjwzh.widget.multirvcontainer.ScrollBlock;
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
      deltaY = tryConsume(deltaY, ViewCompat.TYPE_NON_TOUCH);
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
  protected int consumeSelfBlock(View target, ScrollBlock scrollBlock, int unconsumed, int type) {
    int realConsumed = 0;
    if (mScrollBlocks.indexOf(scrollBlock) == 0) {
      // 第一个block就意味着处理pulltorefresh的最佳时机
      float translationY = getRefreshTargetView().getTranslationY();
      if (type == ViewCompat.TYPE_NON_TOUCH && translationY >= getLoadingMaxOffsetY()) {
        // 强制停止fling
        ((RecyclerView) target).stopScroll();
        Log.d(TAG, " onNestedPreScroll stop fling");
        realConsumed = unconsumed;
        unconsumed = 0;
      } else {
        realConsumed = tryConsume(unconsumed, type);
        unconsumed = unconsumed - realConsumed;
      }
    }
    return realConsumed + super.consumeSelfBlock(target, scrollBlock, unconsumed, type);
  }

  @Override
  protected void rebuildScrollBlocks() {
    super.rebuildScrollBlocks();
    mScrollBlocks.add(0, new ScrollBlock());
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
   * @return consumed
   */
  protected int tryConsume(int dyUnconsumed, int type) {
    // 刷新状态下,优先滚动整个ScrollView
    if (isRefreshing()) {
      return dyUnconsumed;
    }
    int dampConsumed = dampConsume(dyUnconsumed);
    dyUnconsumed = dyUnconsumed - dampConsumed;
    // dyUnconsumed和translationY的方向是相反的
    float translationY = getRefreshTargetView().getTranslationY();
    float mayTranslationY = translationY - dyUnconsumed;
    if (translationY - dyUnconsumed >= getLoadingMaxOffsetY()) {
      mayTranslationY = getLoadingMaxOffsetY();
    }
    mayTranslationY = Math.max(0, mayTranslationY);
    Log.d(TAG, "translationY:" + translationY);
    getRefreshLoadingView().cancelAnimation();
    getRefreshLoadingView().setVisibleHeight(getRefreshTargetView(), (int) mayTranslationY,
        IRefreshLoadingView.MoveType.TOUCH);
    getRefreshTargetView().setTranslationY(mayTranslationY);
    return dampConsumed + (int) (translationY - getRefreshTargetView().getTranslationY());
  }

  /**
   * You can custom damp logic here
   *
   * @param dyUnconsumed
   * @return dampConsumed
   */
  protected int dampConsume(int dyUnconsumed) {
    if (dyUnconsumed > 0) {
      return 0;
    }
    float translationY = getRefreshTargetView().getTranslationY();
    int maxTranslationY = getLoadingMaxOffsetY();
    float dampRatio = Math.abs(translationY / maxTranslationY);
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
