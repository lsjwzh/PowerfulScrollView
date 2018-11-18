package android.support.design.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;

import com.lsjwzh.widget.multirvcontainer.MultiRVScrollView;

import java.util.ArrayList;
import java.util.List;

public class PullToZoomContainer extends MultiRVScrollView {
  private static final String TAG = PullToZoomContainer.class.getSimpleName();
  private int mTouchSlop;
  private List<RefreshListener> mRefreshListeners = new ArrayList<>();
  private float mPullTranslationY;
  private ValueAnimator mRollbackAnimator;

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
  public void stopNestedScroll(int type) {
    super.stopNestedScroll(type);
    Log.d(TAG, "stopNestedScroll:" + type);
    rollbackIfNeed();
  }

  @Override
  public void onNestedPreScroll(View target, int dx, int dy, int[] consumed, int type) {
    super.onNestedPreScroll(target, dx, dy, consumed, type);
    Log.d(TAG, "dy:" + dy + " consumed:" + consumed[1]);
    if (dy < -mTouchSlop && getScrollY() == 0 && consumed[1] == 0) {
      consumed[1] = mTouchSlop + 1 + dy;
    }
  }

  @Override
  public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int
      dyUnconsumed, int type) {
    Log.d(TAG, "dyConsumed:" + dyConsumed + " dyUnconsumed:" + dyUnconsumed);
    if (dyUnconsumed != 0) {
      if (tryConsume(dyUnconsumed)) {
        return;
      }
    }
    super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
  }

  protected boolean tryConsume(int dyUnconsumed) {
    List<View> headerViews = findScaleViews();
    List<View> otherViews = findTranslationViews();
    if (!headerViews.isEmpty() && !otherViews.isEmpty() && getScrollY() == 0) {
      mPullTranslationY -= dyUnconsumed;
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
        return true;
      }
    }
    return false;
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
    if (!clampedY && scrollY > 0 && mPullTranslationY > 0) {
      scrollTo(0, 0);
    }
  }

  @Override
  public boolean startNestedScroll(int axes, int type) {
    boolean b = super.startNestedScroll(axes, type);
    Log.d(TAG, "startNestedScroll:" + b + " type " + type);
    if (mRollbackAnimator != null) {
      mRollbackAnimator.cancel();
      mRollbackAnimator = null;
    }
    return b;
  }

  @Override
  public void onStopNestedScroll(View target, int type) {
    Log.d(TAG, "onStopNestedScroll type " + type);
    super.onStopNestedScroll(target, type);
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

  void rollbackIfNeed() {
    if (mPullTranslationY > 0) {
      for (View view : findScaleViews()) {
        view.animate().scaleY(1.01f).scaleX(1.01f).start();
      }
      mRollbackAnimator = ObjectAnimator.ofFloat(mPullTranslationY, 0);
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

  public interface RefreshListener {
    void onPullOffset(float offset);

    void onRollbackAnimationEnd();
  }
}
