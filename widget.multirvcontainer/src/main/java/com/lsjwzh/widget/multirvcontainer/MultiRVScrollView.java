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
  private final OnScrollChangeListener mNestScrollListener = new OnScrollChangeListener() {
    @Override
    public void onScrollChange(NestedScrollViewExtend v, int scrollX, int scrollY, int
        oldScrollX, int oldScrollY) {
      for (OnScrollChangeListener listener : mListeners) {
        listener.onScrollChange(v, scrollX, scrollY, oldScrollX, oldScrollY);
      }
    }
  };
  protected List<NestRecyclerViewHelper> mNestRecyclerViewHelpers = new ArrayList<>();

  public MultiRVScrollView(Context context) {
    this(context, null);
  }

  public MultiRVScrollView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MultiRVScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs, defStyleAttr);
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
  }

  public void addOnScrollChangeListener(OnScrollChangeListener listener) {
    mListeners.add(listener);
  }

  public void removeOnScrollChangeListener(OnScrollChangeListener listener) {
    mListeners.remove(listener);
  }

  void init(Context context, AttributeSet attrs, int defStyleAttr) {
    setOnScrollChangeListener(mNestScrollListener);
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
  public boolean startNestedScroll(int axes) {
    Log.d(TAG, "startNestedScroll axes:" + axes);
    for (NestRecyclerViewHelper helper : mNestRecyclerViewHelpers) {
      helper.startNestedScroll(axes);
    }
    return super.startNestedScroll(axes);
  }

  @Override
  public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
    Log.d(TAG, "onNestedPreScroll dy:" + dy + " consumed:" + consumed[1]);
    for (NestRecyclerViewHelper helper : mNestRecyclerViewHelpers) {
      helper.onNestedPreScroll(target, dx, dy, consumed);
    }
    super.onNestedPreScroll(target, dx, dy, consumed);
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
  protected void onFlingStop() {
    Log.d(TAG, "onFlingStop: scrollY " + getScrollY());
    for (NestRecyclerViewHelper helper : mNestRecyclerViewHelpers) {
      if (helper.onFlingStop(getScrollY(), getScrollY() >= computeVerticalScrollRange())) {
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
