package com.lsjwzh.widget.powerfulscrollview;


import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class NestRecyclerViewHelper {
  private static final String TAG = NestRecyclerViewHelper.class.getSimpleName();
  final PowerfulScrollView mHostScrollView;
  public RecyclerView mNestedRecyclerView;
  private View mChildContainsRecyclerView;

  NestRecyclerViewHelper(RecyclerView recyclerView,
                         PowerfulScrollView scrollView) {
    mNestedRecyclerView = recyclerView;
    mHostScrollView = scrollView;

    // 由于在嵌套RecyclerView时会自动Focus到RecyclerView上，而RecyclerView调用clearFocus无效
    // 因此需要禁用Focus
    mNestedRecyclerView.setFocusable(false);
    mNestedRecyclerView.setNestedScrollingEnabled(true);
    mNestedRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
  }

  View getDirectChildContainsRecyclerView() {
    if (mChildContainsRecyclerView == null) {
      mChildContainsRecyclerView = findDirectChildContainsRecyclerView();
    }
    return mChildContainsRecyclerView;
  }

  boolean canHandleByHostScrollView(int dy) {
    return !shouldHandleByRecyclerView(dy);
  }

  boolean isRecyclerViewNestedScrollingEnabled(View target) {
    return mNestedRecyclerView == target && mNestedRecyclerView.isNestedScrollingEnabled();
  }

  /**
   * @param direction 1 == 向上滑动; -1 == 向下滑动
   * @return
   */
  boolean shouldHandleByRecyclerView(int direction) {
    // 1.向上滑动的情况下,RecyclerView占满区域,且RecyclerView没有到最底部
    // 2.向下滑动的情况下,RecyclerView占满区域,且RecyclerView没有到最顶部
    return mNestedRecyclerView.canScrollVertically(direction) && isRecyclerViewActive();
  }

  boolean isRecyclerViewActive() {
    return mHostScrollView.getScrollY() == getRecyclerViewPartTop();
  }

  int getRecyclerViewPartTop() {
    final View directChildContainsRecyclerView = getDirectChildContainsRecyclerView();
    return (int) (directChildContainsRecyclerView == null
        ? mNestedRecyclerView.getTop() + mNestedRecyclerView.getTranslationY()
        : directChildContainsRecyclerView.getTop() + directChildContainsRecyclerView.getTranslationY()
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
