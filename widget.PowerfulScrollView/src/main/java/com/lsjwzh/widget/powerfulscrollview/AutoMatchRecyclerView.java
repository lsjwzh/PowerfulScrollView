package com.lsjwzh.widget.powerfulscrollview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * 此View处于ConstaintLayout的子view时,会有问题,主要是ConstaintLayout的WRAP_CONTENT计算有问题
 * Make RecyclerView.scrollToPosition works well in MultiRVScrollView.
 */
public class AutoMatchRecyclerView extends RecyclerView {

  private ObserverForReMeasure observerForReMeasure = new ObserverForReMeasure();

  public AutoMatchRecyclerView(Context context) {
    super(context);
  }

  public AutoMatchRecyclerView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public AutoMatchRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  public void setAdapter(@Nullable Adapter adapter) {
    if (getAdapter() != null) {
      getAdapter().unregisterAdapterDataObserver(observerForReMeasure);
    }
    super.setAdapter(adapter);
    if (adapter != null) {
      adapter.registerAdapterDataObserver(observerForReMeasure);
    }
  }

  @Override
  public void scrollToPosition(int position) {
    scrollToPositionInternal(position, false);
  }

  @Override
  public void smoothScrollToPosition(final int position) {
    scrollToPositionInternal(position, true);
  }

  @Override
  protected void onMeasure(int widthSpec, int heightSpec) {
    PowerfulScrollView multiRVScrollView = findMultiRVScrollView();
    super.onMeasure(widthSpec, heightSpec);
    if (multiRVScrollView != null && multiRVScrollView.getMeasuredHeight() > 0) {
      int targetHeight = getMeasuredHeight();
      if (targetHeight >= multiRVScrollView.getMeasuredHeight()) {
        targetHeight = multiRVScrollView.getMeasuredHeight();
      } else {
        targetHeight = getTargetHeight(multiRVScrollView);
      }
      getLayoutParams().height = targetHeight;
      recurseSetParentHeight(targetHeight);
      setMeasuredDimension(getMeasuredWidth(), targetHeight);
    }
  }

  private void recurseSetParentHeight(int targetHeight) {
    ViewGroup parent = (ViewGroup) getParent();
    while (parent != null) {
      if (parent instanceof PowerfulScrollView) {
        break;
      }
      parent.setMinimumHeight(targetHeight);
      parent = (ViewGroup) parent.getParent();
    }
  }

  private int getTargetHeight(PowerfulScrollView multiRVScrollView) {
    // match gap in multiRVScrollView
    int topOffset = getTop();
    ViewGroup parent = (ViewGroup) getParent();
    while (parent != null) {
      topOffset += parent.getTop();
      if (parent == multiRVScrollView) {
        break;
      }
      parent = (ViewGroup) parent.getParent();
    }
    int gapInScrollView = multiRVScrollView.getMeasuredHeight() - topOffset;
    int scrollRange = computeVerticalScrollRange();
    int targetHeight = gapInScrollView;
    if (scrollRange > gapInScrollView) {
      targetHeight = Math.min(scrollRange, multiRVScrollView.getMeasuredHeight());
    }
    return targetHeight;
  }

  private void refreshHeightIfNeed() {
    int scrollRange = computeVerticalScrollRange();
    if (scrollRange > getHeight()) {
      PowerfulScrollView multiRVScrollView = findMultiRVScrollView();
      if (multiRVScrollView != null && multiRVScrollView.getMeasuredHeight() > 0) {
        // match gap in multiRVScrollView
        int targetHeight = getTargetHeight(multiRVScrollView);
        if (targetHeight != getHeight()) {
          requestLayout();
        }
      }
    }
  }

  private void scrollToPositionInternal(int position, boolean isSmooth) {
    PowerfulScrollView multiRVScrollView = findMultiRVScrollView();
    if (multiRVScrollView != null && multiRVScrollView.isCoordinatedWith(this)
        && getChildCount() > 0) {
      ViewHolder childViewHolder = getChildViewHolder(getChildAt(getChildCount() - 1));
      if (childViewHolder != null) {
        float coordinatedTop = multiRVScrollView.getCoordinatedTop(this);
        if (childViewHolder.getAdapterPosition() < position) {
          multiRVScrollView.scrollTo(0, (int) coordinatedTop);
          rvScrollToPosition(position, isSmooth);
        } else {
          ViewHolder targetViewHolder = findViewHolderForAdapterPosition(position);
          if (targetViewHolder != null &&
              targetViewHolder.itemView.getBottom()
                  + coordinatedTop
                  - multiRVScrollView.getScrollY() > multiRVScrollView.getHeight()) {
            multiRVScrollView.scrollTo(0, (int) coordinatedTop);
          }
          rvScrollToPosition(position, isSmooth);
        }
      }
    } else {
      rvScrollToPosition(position, isSmooth);
    }
  }

  private void rvScrollToPosition(int position, boolean isSmooth) {
    if (isSmooth) {
      AutoMatchRecyclerView.super.smoothScrollToPosition(position);
    } else {
      AutoMatchRecyclerView.super.scrollToPosition(position);
    }
  }

  private PowerfulScrollView findMultiRVScrollView() {
    ViewParent parent = getParent();
    while (parent != null) {
      if (parent instanceof PowerfulScrollView) {
        return (PowerfulScrollView) parent;
      }
      parent = parent.getParent();
    }
    return null;
  }

  class ObserverForReMeasure extends AdapterDataObserver {
    @Override
    public void onChanged() {
      super.onChanged();
      refreshHeightIfNeed();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
      super.onItemRangeChanged(positionStart, itemCount);
      refreshHeightIfNeed();
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
      super.onItemRangeChanged(positionStart, itemCount, payload);
      refreshHeightIfNeed();
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
      super.onItemRangeInserted(positionStart, itemCount);
      refreshHeightIfNeed();
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
      super.onItemRangeRemoved(positionStart, itemCount);
      refreshHeightIfNeed();
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
      super.onItemRangeMoved(fromPosition, toPosition, itemCount);
      refreshHeightIfNeed();
    }
  }
}
