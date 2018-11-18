package com.lsjwzh.widget.multirvcontainer;


import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.NestedScrollViewExtend;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class MultiRVScrollView extends NestedScrollViewExtend {
  static final String TAG = MultiRVScrollView.class.getSimpleName();
  private final List<OnScrollChangeListener> mListeners = new ArrayList<>();
  protected List<NestRecyclerViewHelper> mNestRecyclerViewHelpers = new ArrayList<>();

  public MultiRVScrollView(Context context) {
    this(context, null);
  }

  public MultiRVScrollView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MultiRVScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT);
  }

  @Override
  protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
    if (lp instanceof MarginLayoutParams) {
      return new LayoutParams((MarginLayoutParams) lp);
    }
    return new LayoutParams(lp);
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(getContext(), attrs);
  }

  @Override
  protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    super.onScrollChanged(l, t, oldl, oldt);
    int childCount = getChildCount();
    if (childCount > 0) {
      for (int i = 0; i < childCount; i++) {
        View child = getChildAt(i);
        if (child == getScrollableCoreChild()) {
          continue;
        }
        LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
        if (layoutParams.actionType == LayoutParams.ACTION_TYPE_STICKY) {
          if (layoutParams.stickyCopyView != View.NO_ID) {
            View stickyCopyView = findViewById(layoutParams.stickyCopyView);
            if (stickyCopyView == null || stickyCopyView.getVisibility() == GONE) {
              child.setVisibility(GONE);
              continue;
            }
            float copyViewRealY = 0;
            View parent = stickyCopyView;
            while (parent != this) {
              copyViewRealY += parent.getY();
              parent = (View) parent.getParent();
            }
            if (copyViewRealY >= child.getY()) {
              child.setVisibility(GONE);
              stickyCopyView.setVisibility(VISIBLE);
            } else {
              child.setVisibility(VISIBLE);
              stickyCopyView.setVisibility(INVISIBLE);
            }
          }
          child.setTranslationY(getScrollY());
        }
      }
    }
    for (OnScrollChangeListener listener : mListeners) {
      listener.onScrollChange(this, l, t, oldl, oldt);
    }
  }

  public void addOnScrollChangeListener(OnScrollChangeListener listener) {
    mListeners.add(listener);
  }

  public void removeOnScrollChangeListener(OnScrollChangeListener listener) {
    mListeners.remove(listener);
  }

  protected boolean canHandleByHostScrollView(int dy) {
    for (NestRecyclerViewHelper next : mNestRecyclerViewHelpers) {
      if (next.canHandleByHostScrollView(dy)) {
        return true;
      }
    }
    return false;
  }

  public boolean isCoordinatedWith(@NonNull RecyclerView recyclerView) {
    for (NestRecyclerViewHelper next : mNestRecyclerViewHelpers) {
      if (next.mNestedRecyclerView == recyclerView) {
        return true;
      }
    }
    return false;
  }

  @Override
  public View getScrollableCoreChild() {
    int childCount = getChildCount();
    if (childCount > 0) {
      for (int i = 0; i < childCount; i++) {
        View child = getChildAt(i);
        LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
        if (layoutParams.actionType == LayoutParams.ACTION_TYPE_SCROLL_CORE) {
          return child;
        }
      }
    }
    return super.getScrollableCoreChild();
  }

  public int getCoordinatedTop(@NonNull RecyclerView recyclerView) {
    for (NestRecyclerViewHelper next : mNestRecyclerViewHelpers) {
      if (next.mNestedRecyclerView == recyclerView) {
        return next.getRecyclerViewPartTop();
      }
    }
    return 0;
  }

  public void takeOverScrollBehavior(@NonNull RecyclerView recyclerView) {
    final NestRecyclerViewHelper nestRecyclerViewHelper =
        new NestRecyclerViewHelper(recyclerView, this);
    mNestRecyclerViewHelpers.add(nestRecyclerViewHelper);
    if (getHeight() > 0) {
      fitRecyclerViewHeight();
    } else {
      getViewTreeObserver()
          .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
              getViewTreeObserver().removeGlobalOnLayoutListener(this);
              fitRecyclerViewHeight();
            }
          });
    }
  }

  public void handOverScrollBehavior(@NonNull RecyclerView recyclerView) {
    final ListIterator<NestRecyclerViewHelper> iterator = mNestRecyclerViewHelpers.listIterator();
    while (iterator.hasNext()) {
      final NestRecyclerViewHelper next = iterator.next();
      if (next.mNestedRecyclerView == recyclerView) {
        next.removeLayoutChangeRelationship();
        iterator.remove();
      }
    }
  }

  public boolean hasTakeOver(@NonNull RecyclerView recyclerView) {
    for (NestRecyclerViewHelper next : mNestRecyclerViewHelpers) {
      if (next.mNestedRecyclerView == recyclerView) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void onBottomEdgePull(float x, float deltaY) {
    if (!canHandleByHostScrollView((int) deltaY)) {
      return;
    }
    super.onBottomEdgePull(x, deltaY);
  }

  @Override
  protected void onTopEdgePull(float x, float deltaY) {
    if (!canHandleByHostScrollView((int) deltaY)) {
      return;
    }
    super.onTopEdgePull(x, deltaY);
  }

  @Override
  protected boolean onTopFlingOverScrollAbsorb(int velocity) {
    if (!canHandleByHostScrollView(velocity > 0 ? 1 : -1)) {
      return false;
    }
    return super.onTopFlingOverScrollAbsorb(velocity);
  }

  @Override
  protected boolean onBottomFlingOverScrollAbsorb(int velocity) {
    if (!canHandleByHostScrollView(velocity > 0 ? 1 : -1)) {
      return false;
    }
    return super.onBottomFlingOverScrollAbsorb(velocity);
  }

  @Override
  public boolean startNestedScroll(int axes, int type) {
    Log.d(TAG, "startNestedScroll axes:" + axes + " type" + type);
    for (NestRecyclerViewHelper helper : mNestRecyclerViewHelpers) {
      helper.startNestedScroll(axes, type);
    }
    return super.startNestedScroll(axes, type);
  }

  @Override
  public void stopNestedScroll(int type) {
    super.stopNestedScroll(type);
  }

  @Override
  public boolean hasNestedScrollingParent(int type) {
    return super.hasNestedScrollingParent(type);
  }

  @Override
  public void onNestedPreScroll(View target, int dx, int dy, int[] consumed, int type) {
    Log.d(TAG, "onNestedPreScroll dy:" + dy + " consumed:" + consumed[1]);
    for (NestRecyclerViewHelper helper : mNestRecyclerViewHelpers) {
      helper.onNestedPreScroll(target, dx, dy, consumed, type);
    }
    super.onNestedPreScroll(target, dx, dy, consumed, type);
  }

  @Override
  public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
                             int dyUnconsumed, int type) {
    Log.d(TAG, "onNestedScroll dyConsumed:" + dyConsumed + " dyUnconsumed:" + dyUnconsumed);
    for (NestRecyclerViewHelper helper : mNestRecyclerViewHelpers) {
      helper.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
    }
    super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type);
    if (dyConsumed == 0 && dyUnconsumed > 0) {
      onBottomEdgePull(getWidth() / 2, dyUnconsumed);
      postInvalidate();
    }
  }

  @Override
  public boolean onNestedPreFling(View target, float velocityX, final float velocityY) {
    for (NestRecyclerViewHelper helper : mNestRecyclerViewHelpers) {
      if (helper.onNestedPreFling(target, velocityX, velocityY)) {
        return true;
      }
    }
    return super.onNestedPreFling(target, velocityX, velocityY);
  }

  @Override
  protected void onFlingStop() {
    Log.d(TAG, "onFlingStop: scrollY " + getScrollY());
    for (NestRecyclerViewHelper helper : mNestRecyclerViewHelpers) {
      if (helper.onFlingStop(getScrollY(), getScrollY() >=
          computeVerticalScrollRange() - computeVerticalScrollExtent())) {
        break;
      }
    }
    super.onFlingStop();
  }

  public int getScrollableHeight() {
    return getHeight();
  }

  private void fitRecyclerViewHeight() {
    for (NestRecyclerViewHelper helper : mNestRecyclerViewHelpers) {
      helper.fitRecyclerViewHeight();
    }
  }


  public static class LayoutParams extends FrameLayout.LayoutParams {
    public static final int ACTION_TYPE_NONE = 0;
    public static final int ACTION_TYPE_STICKY = 1;
    public static final int ACTION_TYPE_SCROLL_CORE = 2;

    public int actionType = ACTION_TYPE_NONE;
    public int stickyCopyView = View.NO_ID;


    public LayoutParams(int width, int height) {
      super(width, height);
    }

    public LayoutParams(int width, int height, int gravity) {
      super(width, height, gravity);
    }

    public LayoutParams(@NonNull Context c, @Nullable AttributeSet attrs) {
      super(c, attrs);
      final TypedArray a
          = c.obtainStyledAttributes(attrs, R.styleable.MultiRVScrollView_Layout);
      actionType
          = a.getInt(R.styleable.MultiRVScrollView_Layout_actionType, ACTION_TYPE_NONE);
      stickyCopyView
          = a.getResourceId(R.styleable.MultiRVScrollView_Layout_stickyCopyView, View.NO_ID);
      a.recycle();
    }

    public LayoutParams(@NonNull ViewGroup.LayoutParams source) {
      super(source);
    }

    public LayoutParams(@NonNull MarginLayoutParams source) {
      super(source);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public LayoutParams(@NonNull FrameLayout.LayoutParams source) {
      super(source);
    }
  }

}
