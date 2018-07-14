package com.lsjwzh.widget.multirvcontainer;


import android.support.v7.widget.RVScrollViewUtils;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

public class NestRecyclerViewHelper {
  private static final String TAG = NestRecyclerViewHelper.class.getSimpleName();
  final MultiRVScrollView mHostScrollView;
  RecyclerView mNestedRecyclerView;
  private View mChildContainsRecyclerView;
  RecyclerView.OnScrollListener mOnScrollListener;
  View.OnLayoutChangeListener mNestRecyclerViewLayoutChangeListener =
      new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                                   int oldTop, int oldRight, int oldBottom) {
          if (mNestedRecyclerView.getHeight() > mHostScrollView.getScrollableHeight()) {
            mNestedRecyclerView.getLayoutParams().height = mHostScrollView.getScrollableHeight();
            mNestedRecyclerView.getLayoutManager().setAutoMeasureEnabled(false);
            // sometimes requestLayout will not cause a layout action, so call forceLayout before
            mNestedRecyclerView.post(new Runnable() {
              @Override
              public void run() {
                mNestedRecyclerView.forceLayout();
                mNestedRecyclerView.requestLayout();
              }
            });
            if (mChildContainsRecyclerView != null) {
              mChildContainsRecyclerView.requestLayout();
            }
          }
        }
      };

  NestRecyclerViewHelper(RecyclerView recyclerView,
                         MultiRVScrollView scrollView) {
    mNestedRecyclerView = recyclerView;
    mHostScrollView = scrollView;

    // 由于在嵌套RecyclerView时会自动Focus到RecyclerView上，而RecyclerView调用clearFocus无效
    // 因此需要禁用Focus
    mNestedRecyclerView.setFocusable(false);
    mNestedRecyclerView.setNestedScrollingEnabled(true);
    mNestedRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    mChildContainsRecyclerView = findDirectChildContainsRecyclerView();
  }

  void removeLayoutChangeRelationship() {
    mNestedRecyclerView.removeOnLayoutChangeListener(mNestRecyclerViewLayoutChangeListener);
  }

  void fitRecyclerViewHeight() {
    if (mNestedRecyclerView.getLayoutManager().isAutoMeasureEnabled()) {
      if (mNestedRecyclerView.getHeight() > mHostScrollView.getScrollableHeight()) {
        // 如果RecyclerView是automeasurable且自动高度大于ScrollView高度，则需要对齐高度做限制
        // 所以如果RecyclerView的Adapter一开始就有很多数据，最好禁用automeasurable
        mNestedRecyclerView.getLayoutParams().height = mHostScrollView.getScrollableHeight();
        mNestedRecyclerView.getLayoutManager().setAutoMeasureEnabled(false);
        mNestedRecyclerView.requestLayout();
      } else if (mNestedRecyclerView.getLayoutParams().height < 0) {
        // 如果是自适应高度，则需要监听其高度变化
        mNestedRecyclerView.addOnLayoutChangeListener(mNestRecyclerViewLayoutChangeListener);
      }
    } else {
      // 如果RecyclerView不是automeasurable的则需指定其高度
      mNestedRecyclerView.getLayoutParams().height = mHostScrollView.getScrollableHeight();
      mNestedRecyclerView.requestLayout();
    }
  }

  void startNestedScroll(int axes) {
    if (mOnScrollListener != null) {
      mNestedRecyclerView.removeOnScrollListener(mOnScrollListener);
    }
    // cancel fling and smoothScrollBy operations
    mHostScrollView.fling(0);
    mHostScrollView.smoothScrollBy(0, 0);
  }


  void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
    if (isRecyclerViewNestedScrollingEnabled(target)) {
      int unconsumed = dy - consumed[1];
      if (canHandleByHostScrollView(unconsumed)) {
        int oldScrollY = mHostScrollView.getScrollY();
        Log.d(TAG, "want scrollBy " + unconsumed);
        mHostScrollView.scrollBy(0, unconsumed);
        consumed[1] += mHostScrollView.getScrollY() - oldScrollY;
        Log.d(TAG, "real scrollBy " + consumed[1]);
      }
    }
  }

  boolean canHandleByHostScrollView(int dy) {
    return !shouldHandleByRecyclerView()
        || (mNestedRecyclerView.computeVerticalScrollOffset() == 0 && dy < 0);
  }

  public boolean tryConsumeScroll(int dyConsumed) {
    if (shouldHandleByRecyclerView()) {
      RVScrollViewUtils.scrollVerticallyBy(mNestedRecyclerView, dyConsumed);
      Log.d(TAG, "scrollVerticallyBy " + dyConsumed);
      return true;
    }
    return false;
  }

 public boolean onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
                         int dyUnconsumed) {
    if (isRecyclerViewNestedScrollingEnabled(target)) {
      if (!shouldHandleByRecyclerView()) {
        RVScrollViewUtils.scrollVerticallyBy(mNestedRecyclerView, -dyConsumed);
        Log.d(TAG, "scrollBy " + dyConsumed);
        mHostScrollView.scrollBy(0, dyConsumed);
        return true;
      }
    }
    return false;
  }

  boolean onNestedPreFling(View target, float velocityX, final float velocityY) {
    if (isRecyclerViewNestedScrollingEnabled(target)) {
      if (shouldHandleByRecyclerView()) {
        if (mOnScrollListener != null) {
          mNestedRecyclerView.removeOnScrollListener(mOnScrollListener);
        }
        mOnScrollListener = new RecyclerView.OnScrollListener() {
          @Override
          public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
              recyclerView.removeOnScrollListener(this);
              if ((RVScrollViewUtils.isTopOverScrolled(recyclerView)
                  && velocityY < 0 && shouldHandleByRecyclerView()
                  && mHostScrollView.getScrollY() > 0
                  && !mHostScrollView.getScroller().isOverScrolled())
                  || (RVScrollViewUtils.isBottomOverScrolled(recyclerView)
                  && velocityY > 0
                  && shouldHandleByRecyclerView()
                  && mHostScrollView.getScrollY() < mHostScrollView.getChildAt(0).getHeight()
                  - mHostScrollView.getScrollableHeight()
                  && !mHostScrollView.getScroller().isOverScrolled())) {
                float currentVelocityYAbs = RVScrollViewUtils.getCurrentVelocityY(recyclerView);
                float currentVelocityY = velocityY > 0 ? currentVelocityYAbs : -currentVelocityYAbs;
                mHostScrollView.fling(currentVelocityY == 0 ?
                    (int) (velocityY / 2) : (int) currentVelocityY);
                Log.d(TAG, mNestedRecyclerView.getId() +
                    " fling onScrollStateChanged:" + velocityY +
                    " recyclerView velocityY:" + currentVelocityY);
              }
            }
          }
        };
        mNestedRecyclerView.addOnScrollListener(mOnScrollListener);
      } else {
        mHostScrollView.fling((int) velocityY);
        Log.d(TAG, "fling onNestedPreFling:" + velocityY);
        return true;
      }
    }
    return false;
  }

  boolean onFlingStop(int scrollY, boolean clampedY) {
    float currVelocity = mHostScrollView.getScroller().getCurrVelocity();
    if (clampedY && shouldHandleByRecyclerView()
        && !Float.isNaN(currVelocity)) {
      currVelocity = scrollY == 0 ? -currVelocity : currVelocity;
      mNestedRecyclerView.stopScroll();
      mNestedRecyclerView.fling(0, (int) currVelocity);
      Log.d(TAG, mNestedRecyclerView.getId() + " fling onFlingStop" + currVelocity + " scrollY" + scrollY);
      return true;
    }
    return false;
  }

  boolean isRecyclerViewNestedScrollingEnabled(View target) {
    return mNestedRecyclerView == target && mNestedRecyclerView.isNestedScrollingEnabled();
  }

  boolean shouldHandleByRecyclerView() {
    return mHostScrollView.getScrollY() + getRecyclerViewTopOffset() == getRecyclerViewPartTop();
  }

  int getRecyclerViewTopOffset() {
    return mHostScrollView.getHeight() - mHostScrollView.getScrollableHeight();
  }

  int getRecyclerViewPartTop() {
    return (int) (mChildContainsRecyclerView == null
            ? mNestedRecyclerView.getTop() + mNestedRecyclerView.getTranslationY()
            : mChildContainsRecyclerView.getTop() + mChildContainsRecyclerView.getTranslationY()
            + mNestedRecyclerView.getTop() + mNestedRecyclerView.getTranslationY());
  }

  private View findDirectChildContainsRecyclerView() {
    View parent = (View) mNestedRecyclerView.getParent();
    if (parent == mHostScrollView.getScrollableCoreChild()) {
      return null;
    }
    while (parent.getParent() != mHostScrollView.getScrollableCoreChild()) {
      parent = (View) parent.getParent();
    }
    return parent;
  }

}
