package android.support.design.widget;

import android.content.Context;
import android.graphics.Canvas;
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

public class PullToRefreshContainer extends MultiRVScrollView {
  private static final String TAG = PullToRefreshContainer.class.getSimpleName();
  private int mTouchSlop;
  private int mLastEventAction;
  private List<RefreshListener> mRefreshListeners = new ArrayList<>();
  private boolean mMoveBeforeTouchRelease;

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
  }

  public void endRefresh() {
    getRefreshChild().getRefreshHeader().collapse(getRefreshChild().getRefreshTargetView(),
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
    getRefreshChild().getRefreshHeader().expand(getRefreshChild().getRefreshTargetView(),
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
      if (isRefreshHeaderExpanded()) {
        for (RefreshListener listener : mRefreshListeners) {
          listener.onRefreshing();
        }
      } else if (getRefreshChild().getRefreshTargetView().getTranslationY() > 0) {
        getRefreshChild().getRefreshHeader().collapse(getRefreshChild().getRefreshTargetView(), null);
      }
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
      PullToRefreshChild refreshChild = getRefreshChild();
      if (refreshChild != null) {
        float translationY = refreshChild.getRefreshTargetView().getTranslationY();
        if (translationY < refreshChild.getRefreshHeader().getMaxHeight()
            || mMoveBeforeTouchRelease) {
          mMoveBeforeTouchRelease = true;
          translationY = Math.min(translationY - dyUnconsumed, refreshChild.getRefreshHeader()
              .getMaxHeight());
          translationY = Math.max(0, translationY);
          Log.d(TAG, "translationY:" + translationY);
          refreshChild.getRefreshHeader().cancelAnimation();
          refreshChild.getRefreshHeader().setVisibleHeight((int) translationY);
          refreshChild.getRefreshTargetView().setTranslationY(translationY);
          return;
        }
      }
    }
    super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
  }

  private boolean isRefreshHeaderExpanded() {
    PullToRefreshChild refreshChild = getRefreshChild();
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
    if (clampedY) {
      if (getScrollY() == 0) {
        PullToRefreshChild refreshChild = getRefreshChild();
        if (refreshChild != null) {
          float translationY = refreshChild.getRefreshTargetView().getTranslationY();
          if (translationY < refreshChild.getRefreshHeader().getMaxHeight()) {
            translationY = Math.min(translationY + mTouchSlop, refreshChild.getRefreshHeader()
                .getMaxHeight());
            Log.d(TAG, "translationY:" + translationY);
            refreshChild.getRefreshHeader().cancelAnimation();
            refreshChild.getRefreshHeader().setVisibleHeight((int) translationY);
            refreshChild.getRefreshTargetView().setTranslationY(translationY);
          }
        }
      }
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

  PullToRefreshChild getRefreshChild() {
    return (PullToRefreshChild) getChildAt(0);
  }

  public interface RefreshListener {
    void onRefreshing();
    void onRefreshAnimationEnd();
  }

  public interface IRefreshHeader {

    void expand(View refreshTargetView, Runnable animationEndCallback);

    void collapse(View refreshTargetView, Runnable animationEndCallback);

    int getMaxHeight();

    void cancelAnimation();

    void setVisibleHeight(int targetHeight);
  }
}
