package com.lsjwzh.widget.powerfulscrollview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewParent;

/**
 * Make RecyclerView.scrollToPosition works well in MultiRVScrollView.
 */
public class AutoExtendRecyclerView extends RecyclerView {
  public AutoExtendRecyclerView(Context context) {
    super(context);
  }

  public AutoExtendRecyclerView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public AutoExtendRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
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
    if (multiRVScrollView != null && multiRVScrollView.getHeight() > 0
        && getMeasuredHeight() > multiRVScrollView.getHeight()) {
      getLayoutManager().setAutoMeasureEnabled(false);
      getLayoutParams().height = multiRVScrollView.getHeight();
      heightSpec = MeasureSpec.makeMeasureSpec(multiRVScrollView.getHeight(), MeasureSpec.EXACTLY);
      super.onMeasure(widthSpec, heightSpec);
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
      AutoExtendRecyclerView.super.smoothScrollToPosition(position);
    } else {
      AutoExtendRecyclerView.super.scrollToPosition(position);
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
}
