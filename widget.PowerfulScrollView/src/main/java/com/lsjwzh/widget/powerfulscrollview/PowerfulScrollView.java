package com.lsjwzh.widget.powerfulscrollview;


import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollViewExtend;
import android.support.v7.widget.RVScrollViewUtils;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class PowerfulScrollView extends NestedScrollViewExtend {
  static final String TAG = PowerfulScrollView.class.getSimpleName();
  private final List<OnScrollChangeListener> mListeners = new ArrayList<>();
  protected List<NestRecyclerViewHelper> mNestRecyclerViewHelpers = new ArrayList<>();
  protected boolean mNonTouchScrollStarted;
  protected LinkedList<ScrollBlock> mScrollBlocks = new LinkedList<>();
  protected RecyclerView mScrollingRecyclerViewWhenActionDown;

  public PowerfulScrollView(Context context) {
    this(context, null);
  }

  public PowerfulScrollView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PowerfulScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public void onViewAdded(View child) {
    super.onViewAdded(child);
  }

  @Override
  public void onViewRemoved(View child) {
    super.onViewRemoved(child);
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
  public boolean dispatchTouchEvent(MotionEvent ev) {
    // 避免在手动停止fling时，触发RecyclerViewItem的click或者touch事件
    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
      for (NestRecyclerViewHelper next : mNestRecyclerViewHelpers) {
        if (next.mNestedRecyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) {
          mScrollingRecyclerViewWhenActionDown = next.mNestedRecyclerView;
          Log.d(TAG,
                  "set mScrollingRecyclerViewWhenActionDown " + mScrollingRecyclerViewWhenActionDown.getScrollState());
        }
      }
    } else if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
      if (mScrollingRecyclerViewWhenActionDown != null) {
        mScrollingRecyclerViewWhenActionDown = null;
        Log.d(TAG, "release mScrollingRecyclerViewWhenActionDown");
        return true;
      }
    }
    return super.dispatchTouchEvent(ev);
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    if (mScrollingRecyclerViewWhenActionDown != null) {
      mScrollingRecyclerViewWhenActionDown.onInterceptTouchEvent(ev);
      return true;
    }
    return super.onInterceptTouchEvent(ev);
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

  public void takeOverScrollBehavior(@NonNull final RecyclerView recyclerView) {
    final NestRecyclerViewHelper nestRecyclerViewHelper =
        new NestRecyclerViewHelper(recyclerView, this);
    mNestRecyclerViewHelpers.add(nestRecyclerViewHelper);
    if (getHeight() == 0) {
      // 由于第一次measure时,ScrollView高度为0,
      // 所以CoordinateScrollRecyclerView的Measure不能准确校准,
      // 所以需要手动触发layout
      getViewTreeObserver()
          .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
              getViewTreeObserver().removeGlobalOnLayoutListener(this);
              recyclerView.requestLayout();
              rebuildScrollBlocks();
            }
          });
    } else {
      rebuildScrollBlocks();
    }
  }

  public void handOverScrollBehavior(@NonNull RecyclerView recyclerView) {
    final ListIterator<NestRecyclerViewHelper> iterator = mNestRecyclerViewHelpers.listIterator();
    while (iterator.hasNext()) {
      final NestRecyclerViewHelper next = iterator.next();
      if (next.mNestedRecyclerView == recyclerView) {
        iterator.remove();
      }
    }
    rebuildScrollBlocks();
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
    final boolean ret = super.startNestedScroll(axes, type);
    Log.d(TAG, "startNestedScroll axes:" + axes + " type" + type + " ret" + ret);
    return ret;
  }

  @Override
  public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes, int type) {
    if (!getScroller().isFinished()) {
      Log.d(TAG, "onStartNestedScroll scroll not finished");
    }
    for (NestRecyclerViewHelper helper : mNestRecyclerViewHelpers) {
      if (helper.mNestedRecyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) {
        helper.mNestedRecyclerView.stopScroll();
        Log.d(TAG, helper.mNestedRecyclerView + " onStartNestedScroll mNestedRecyclerView " +
            "stopScroll");
      }
    }
    if (type == ViewCompat.TYPE_NON_TOUCH) {
      mNonTouchScrollStarted = true;
      Log.d(TAG, "mNonTouchScrollStarted");
    }
    return true;
  }

  @Override
  public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes, int type) {
    Log.d(TAG, "onNestedScrollAccepted type" + type);
    super.onNestedScrollAccepted(child, target, nestedScrollAxes, type);

  }

  @Override
  public void stopNestedScroll(int type) {
    Log.d(TAG, "stopNestedScroll type" + type);
    super.stopNestedScroll(type);
  }

  @Override
  public void onStopNestedScroll(@NonNull View target, int type) {
    super.onStopNestedScroll(target, type);
    if (type == ViewCompat.TYPE_NON_TOUCH) {
      mNonTouchScrollStarted = false;
      Log.d(TAG, "mNonTouchScrollStarted end");
    }
  }

  @Override
  public boolean hasNestedScrollingParent(int type) {
    return super.hasNestedScrollingParent(type);
  }

  @Override
  public void onNestedPreScroll(View target, int dx, int dy, int[] consumed, int type) {
    Log.d(TAG, "onNestedPreScroll dy:" + dy + " consumed:" + consumed[1]);
    super.onNestedPreScroll(target, dx, dy, consumed, type);
  }

  @Override
  public void fling(int velocityY) {
    Log.d(TAG, "srcollView self fling");
    if (mNestRecyclerViewHelpers.size() > 0) {
      mNestRecyclerViewHelpers.get(0).mNestedRecyclerView.fling(0, velocityY);
    } else {
      super.fling(velocityY);
    }
  }

  @Override
  public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
                             int dyUnconsumed, int type) {
    int[] consumed = new int[]{dxConsumed, dyConsumed};
    int[] unconsumed = new int[]{dxUnconsumed, dyUnconsumed};
    Log.d(TAG, target + "onNestedScroll dyConsumed:" + dyConsumed + " dyUnconsumed:" +
        dyUnconsumed);
    // dyConsumed 是rv已经scroll过的,dyUnconsumed是剩余需要处理的,
    // 这个时候如果dyConsumed==0,说明当前RV已经触底
    // dyConsumed != 0 说明RV可以滚动,需要反向把RV的滚动距离回滚回来
    // 如果dyUnconsumed!=0, dyConsumed == 0 说明ScrollView已经触底,需要触发其他RV的滚动

    // 考虑如下情况
    // -----
    // A
    // ==
    // B
    // -----
    // 分为四种情况:
    // 1.在A处往上滑动 滚到头  consumed[1] == 0 && unconsumed[1] > 0 未到头 consumed[1] > 0 && unconsumed[1]
    // == 0
    // 2.在A处往下滑动 滚到头  consumed[1] < 0 && unconsumed[1] == 0
    // 3.在B处往上滑动 滚到头  consumed[1] > 0 && unconsumed[1] == 0 未到头 consumed[1] == 0 && unconsumed[1]
    // > 0
    // 4.在B处往下滑动 滚到头  consumed[1] == 0 && unconsumed[1] < 0

    // 先把RecyclerView滚动的距离处理掉
    int reverseScroll = -RVScrollViewUtils.scrollVerticallyBy((RecyclerView) target,
        -consumed[1]);
    consumed[1] -= reverseScroll;
    unconsumed[1] += reverseScroll;
    // 按照顺序消费 Scroll
    drainScrollY(target, consumed, unconsumed, type);
    super.onNestedScroll(target, consumed[0], consumed[1], unconsumed[0], unconsumed[1], type);
    if (consumed[1] == 0 && unconsumed[1] > 0) {
      onBottomEdgePull(getWidth() / 2, dyUnconsumed);
      postInvalidate();
    }
  }

  private void drainScrollY(View target, int[] consumed, int[] unconsumed, int type) {
    if (unconsumed[1] > 0) {
      for (int i = 0; i < mScrollBlocks.size(); i++) {
        if (doDrain(target, consumed, unconsumed, i, type)) break;
      }
    } else if (unconsumed[1] < 0) {
      for (int i = mScrollBlocks.size() - 1; i >= 0; i--) {
        if (doDrain(target, consumed, unconsumed, i, type)) break;
      }
    }
  }

  private boolean doDrain(View target, int[] consumed, int[] unconsumed, int i, int type) {
    ScrollBlock scrollBlock = mScrollBlocks.get(i);
    if (scrollBlock.type == ScrollBlock.BlockType.Self) {
      int realScroll = consumeSelfBlock(target, scrollBlock, unconsumed[1], type);
      consumed[1] += realScroll;
      unconsumed[1] -= realScroll;
      return unconsumed[1] == 0;
    } else if (scrollBlock.type == ScrollBlock.BlockType.RecyclerView) {
      if (scrollBlock.recyclerView.isNestedScrollingEnabled()) {
        int scroll = consumeRecyclerViewBlock(target, scrollBlock, unconsumed[1], type);
        consumed[1] += scroll;
        unconsumed[1] -= scroll;
        return unconsumed[1] == 0;
      }
    }

    return false;
  }

  /**
   * @param unconsumed
   * @return consumed by current block
   */
  protected int consumeSelfBlock(View target, ScrollBlock scrollBlock, int unconsumed, int type) {
    Log.d(TAG, "try consume " + unconsumed + "by self");
    int oldScrollY = getScrollY();
    scrollBy(0, unconsumed);
    int realScroll = getScrollY() - oldScrollY;
    Log.d(TAG, "self consume" + realScroll);
    return realScroll;
  }

  /**
   * @param unconsumed
   * @return consumed by current block
   */
  protected int consumeRecyclerViewBlock(View target, ScrollBlock scrollBlock, int unconsumed,
                                         int type) {
    Log.d(TAG, "try consume" + unconsumed + " by recyclerView" + scrollBlock.recyclerView);
    int scroll = RVScrollViewUtils.scrollVerticallyBy(scrollBlock.recyclerView,
        unconsumed);
    Log.d(TAG, "recyclerView consume " + scroll);
    return scroll;
  }


  @Override
  protected void onFlingStop() {
    Log.d(TAG, "onFlingStop: scrollY " + getScrollY());
    super.onFlingStop();
  }

  protected void rebuildScrollBlocks() {
    mScrollBlocks.clear();
    int blockOffsetCursor = 0;
    for (NestRecyclerViewHelper helper : mNestRecyclerViewHelpers) {
      if (helper.getRecyclerViewPartTop() > blockOffsetCursor) {
        mScrollBlocks.add(new ScrollBlock());
        blockOffsetCursor = helper.getRecyclerViewPartTop();
      }
      mScrollBlocks.add(new ScrollBlock(helper.mNestedRecyclerView));
    }
    if (mNestRecyclerViewHelpers.size() > 0) {
      // 如果列表不是最后一个block,则需要补齐一个Self的block
      NestRecyclerViewHelper lastNRVH = mNestRecyclerViewHelpers.get
          (mNestRecyclerViewHelpers.size() - 1);
      if (lastNRVH.getRecyclerViewPartTop() + lastNRVH.mNestedRecyclerView.getHeight() <
          getScrollRange()) {
        mScrollBlocks.add(new ScrollBlock());
      }
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
          = c.obtainStyledAttributes(attrs, R.styleable.PowerfulScrollView_Layout);
      actionType
          = a.getInt(R.styleable.PowerfulScrollView_Layout_actionType, ACTION_TYPE_NONE);
      stickyCopyView
          = a.getResourceId(R.styleable.PowerfulScrollView_Layout_stickyCopyView, View.NO_ID);
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
