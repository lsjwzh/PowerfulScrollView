package com.lsjwzh.widget.multirvcontainer;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.ScrollerCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class MultiRVScrollView extends NestedScrollView {
  static final String TAG = MultiRVScrollView.class.getSimpleName();
  ScrollerCompat mScrollerCompat;
  List<NestRecyclerViewHelper> mNestRecyclerViewHelpers = new ArrayList<>();
  private final List<OnScrollChangeListener> mListeners = new ArrayList<>();

  private final OnScrollChangeListener mNestScrollListener = new OnScrollChangeListener() {
    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX,
                               int oldScrollY) {
      for (OnScrollChangeListener listener : mListeners) {
        listener.onScrollChange(v, scrollX, scrollY, oldScrollX, oldScrollY);
      }
    }
  };

  public MultiRVScrollView(Context context) {
    this(context, null);
  }

  public MultiRVScrollView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public void addOnScrollChangeListener(OnScrollChangeListener listener) {
    mListeners.add(listener);
  }

  public void removeOnScrollChangeListener(OnScrollChangeListener listener) {
    mListeners.remove(listener);
  }

  public MultiRVScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs, defStyleAttr);
  }

  void init(Context context, AttributeSet attrs, int defStyleAttr) {
    try {
      final Field scrollerField = NestedScrollView.class.getDeclaredField("mScroller");
      scrollerField.setAccessible(true);
      this.mScrollerCompat = (ScrollerCompat) scrollerField.get(this);
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
      this.mScrollerCompat = ScrollerCompat.create(getContext(), null);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      this.mScrollerCompat = ScrollerCompat.create(getContext(), null);
    }
    setOnScrollChangeListener(mNestScrollListener);
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

  @Override
  public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
                             int dyUnconsumed) {
    Log.d(TAG, "onNestedScroll dyConsumed:" + dyConsumed + " dyUnconsumed:" + dyUnconsumed);
    for (NestRecyclerViewHelper helper : mNestRecyclerViewHelpers) {
      helper.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }
    super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
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
  protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
    super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    Log.d(TAG, "onOverScrolled: scrollY " + scrollY + " clampedY " + clampedY);
    for (NestRecyclerViewHelper helper : mNestRecyclerViewHelpers) {
      if (helper.onOverScrolled(scrollX, scrollY, clampedX, clampedY)) {
        break;
      }
    }
  }

  private void fitRecyclerViewHeight() {
    for (NestRecyclerViewHelper helper : mNestRecyclerViewHelpers) {
      helper.fitRecyclerViewHeight();
    }
  }

  public void setHeight(int height) {
    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.getLayoutParams();
    layoutParams.height = height;
    this.setLayoutParams(layoutParams);
    requestLayout();
  }

  public void setPaddingTop(int paddingTop) {
    this.setPadding(0, paddingTop, 0, 0);
    requestLayout();
  }

  public int getPaddingTop() {
    return super.getPaddingTop();
  }

}
