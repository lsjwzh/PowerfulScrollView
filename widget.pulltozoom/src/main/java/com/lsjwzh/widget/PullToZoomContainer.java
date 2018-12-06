package com.lsjwzh.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;

import com.lsjwzh.widget.multirvcontainer.MultiRVScrollView;
import com.lsjwzh.widget.multirvcontainer.ScrollBlock;

import java.util.ArrayList;
import java.util.List;

public class PullToZoomContainer extends MultiRVScrollView {
  private static final String TAG = PullToZoomContainer.class.getSimpleName();
  private int mTouchSlop;
  private List<RefreshListener> mRefreshListeners = new ArrayList<>();
  private float mPullTranslationY;
  private ValueAnimator mRollbackAnimator;
  private Runnable mRollbackRunnable = new Runnable() {
    @Override
    public void run() {
      rollbackIfNeed();
    }
  };
  private int mLastStartNestedScrollType;

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
  protected void onFlingStop() {
    Log.d(TAG, "onFlingStop:");
    super.onFlingStop();
    rollbackIfNeed();
  }

  @Override
  protected int consumeSelfBlock(View target, ScrollBlock scrollBlock, int unconsumed, int type) {
    int realConsumed = 0;
    if (mScrollBlocks.indexOf(scrollBlock) == 0) {
      // 第一个block就意味着处理pulltozoom的最佳时机
      if (mRollbackAnimator != null && mRollbackAnimator.isRunning()) {
        // 强制停止fling
        ((RecyclerView) target).stopScroll();
        restartRollbackAnim();
        Log.d(TAG, " onNestedPreScroll stop fling");
        realConsumed = unconsumed;
        unconsumed = 0;
      } else {
        realConsumed = tryConsume(unconsumed, type);
        unconsumed = unconsumed - realConsumed;
        if (type == ViewCompat.TYPE_NON_TOUCH && unconsumed == 0) {
          // 强制停止fling
          ((RecyclerView) target).stopScroll();
          restartRollbackAnim();
          Log.d(TAG, " onNestedPreScroll stop fling");
        }
      }
    }
    return realConsumed + super.consumeSelfBlock(target, scrollBlock, unconsumed, type);
  }

  protected int tryConsume(int dyUnconsumed, int type) {
    float translationYBefore = mPullTranslationY;
    int dampConsumed = 0;
    List<View> headerViews = findScaleViews();
    List<View> otherViews = findTranslationViews();
    if (!headerViews.isEmpty() && !otherViews.isEmpty() && getScrollY() == 0) {
      dampConsumed = dampConsume(dyUnconsumed, type);
      Log.d(TAG, "dampConsume:" + dampConsumed + " getScrollY:" + getScrollY());
      dyUnconsumed = dyUnconsumed - dampConsumed;
      mPullTranslationY -= dyUnconsumed;
      mPullTranslationY = Math.min(mPullTranslationY, getMaxTranslationY());
      for (View scaleView : headerViews) {
        int height = scaleView.getHeight();
        int targetHeight = (int) (height + mPullTranslationY);
        float scale = targetHeight * 1f / height;
        scaleView.setScaleY(Math.max(1, scale));
        scaleView.setScaleX(Math.max(1, scale));
        scaleView.setPivotY(0f);
        Log.d(TAG, "scaleY:" + scale);
      }
      if (mPullTranslationY > 0) {
        for (RefreshListener listener : mRefreshListeners) {
          listener.onPullOffset(mPullTranslationY);
        }
        for (View view : otherViews) {
          view.setTranslationY(mPullTranslationY);
        }
      }
    }
    int realConsumed = (int) (mPullTranslationY - translationYBefore);
    Log.d(TAG, String.format("dyUnconsumed %s dampConsume %s realConsumed %s",
        dyUnconsumed, dampConsumed, realConsumed));
    return dampConsumed + realConsumed;
  }

  protected int getMaxTranslationY() {
    return getHeight() / 3;
  }


  /**
   * You can custom damp logic here
   *
   * @param dyUnconsumed
   * @param type         Touch Type
   * @return dampConsumed
   */
  protected int dampConsume(int dyUnconsumed, int type) {
    if (dyUnconsumed > 0) {
      return 0;
    }
    float translationY = mPullTranslationY;
    int maxTranslationY = getMaxTranslationY();
    float dampRatio = Math.abs(translationY * 1f / maxTranslationY);
    if (type == ViewCompat.TYPE_NON_TOUCH) {
      dampRatio = Math.min(dampRatio * 4, 1);
    }
    return (int) (dampRatio * dyUnconsumed);
  }

  @Override
  protected boolean overScrollByCompat(int deltaX, int deltaY, int scrollX, int scrollY, int
      scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean
                                           isTouchEvent) {
    Log.d(TAG, String.format("overScrollByCompat getScrollY() %s", getScrollY()));
    boolean clamp = super.overScrollByCompat(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
        scrollRangeY,
        maxOverScrollX, maxOverScrollY, isTouchEvent);
    if (isTouchEvent) {
      tryConsume(deltaY, ViewCompat.TYPE_TOUCH);
    }
    return clamp;
  }

  @Override
  protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
    Log.d(TAG, "onOverScrolled scrollY:" + scrollY + " clampedY:" + clampedY);
    super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    if (!clampedY && scrollY > 0 && mPullTranslationY > 0) {
      scrollTo(0, 0);
    }
  }

  @Override
  public boolean startNestedScroll(int axes, int type) {
    mLastStartNestedScrollType = type;
    boolean b = super.startNestedScroll(axes, type);
    Log.d(TAG, "onStartNestedScroll:" + b + " type " + type);
    cancelRollback();
    return true;
  }

  @Override
  public void stopNestedScroll(int type) {
    super.stopNestedScroll(type);
    Log.d(TAG, "stopNestedScroll:" + type);
    if (mLastStartNestedScrollType == type) {
      Log.d(TAG, "post rollback:" + type);
      restartRollbackAnim();
    }
  }

  private void cancelRollback() {
    removeCallbacks(mRollbackRunnable);
    if (mRollbackAnimator != null) {
      mRollbackAnimator.cancel();
      mRollbackAnimator = null;
    }
  }

  private void restartRollbackAnim() {
    cancelRollback();
    post(mRollbackRunnable);
  }

  protected List<View> findScaleViews() {
    List<View> scaleViews = new ArrayList<>();
    View coreChild = getScrollableCoreChild();
    if (coreChild instanceof PullToZoomCoreChild) {
      PullToZoomCoreChild pullToZoomCoreChild = (PullToZoomCoreChild) coreChild;
      for (int i = 0; i < pullToZoomCoreChild.getChildCount(); i++) {
        View view = pullToZoomCoreChild.getChildAt(i);
        int actionWhenOverScroll
            = ((PullToZoomCoreChild.LayoutParams) view.getLayoutParams()).actionWhenOverScroll;
        if (actionWhenOverScroll == PullToZoomCoreChild.LayoutParams.ACTION_SCALE) {
          scaleViews.add(view);
        }
      }
    }
    return scaleViews;
  }

  protected List<View> findTranslationViews() {
    List<View> translationViews = new ArrayList<>();
    View coreChild = getScrollableCoreChild();
    if (coreChild instanceof PullToZoomCoreChild) {
      PullToZoomCoreChild pullToZoomCoreChild = (PullToZoomCoreChild) coreChild;
      for (int i = 0; i < pullToZoomCoreChild.getChildCount(); i++) {
        View view = pullToZoomCoreChild.getChildAt(i);
        int actionWhenOverScroll
            = ((PullToZoomCoreChild.LayoutParams) view.getLayoutParams()).actionWhenOverScroll;
        if (actionWhenOverScroll == PullToZoomCoreChild.LayoutParams.ACTION_TRANSLATION) {
          translationViews.add(view);
        }
      }
    }
    return translationViews;
  }

  protected void rollbackIfNeed() {
    if (mPullTranslationY > 0 && (mRollbackAnimator == null || !mRollbackAnimator.isRunning())) {
      for (View view : findScaleViews()) {
        view.animate().cancel();
        view.animate()
            .scaleY(1.01f).scaleX(1.01f)
            .setDuration(getRollbackAnimDuration(mPullTranslationY))
            .start();
      }
      mRollbackAnimator = ObjectAnimator.ofFloat(mPullTranslationY, 0);
      mRollbackAnimator.setDuration(getRollbackAnimDuration(mPullTranslationY));
      mRollbackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
          mPullTranslationY = (float) animation.getAnimatedValue();
          for (View view : findTranslationViews()) {
            view.setTranslationY(mPullTranslationY);
          }
        }
      });
      mRollbackAnimator.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          mPullTranslationY = 0;
          for (View view : findTranslationViews()) {
            view.setTranslationY(mPullTranslationY);
          }
          for (RefreshListener listener : mRefreshListeners) {
            listener.onRollbackAnimationEnd();
          }
        }
      });
      mRollbackAnimator.start();
    }
  }

  protected int getRollbackAnimDuration(float distance) {
    return 150;
  }

  public interface RefreshListener {
    void onPullOffset(float offset);

    void onRollbackAnimationEnd();
  }
}
