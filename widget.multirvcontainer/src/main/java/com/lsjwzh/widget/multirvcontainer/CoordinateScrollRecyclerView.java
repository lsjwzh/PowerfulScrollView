package com.lsjwzh.widget.multirvcontainer;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewParent;

/**
 * Make RecyclerView.scrollToPosition works well in MultiRVScrollView.
 */
public class CoordinateScrollRecyclerView extends RecyclerView {
  public CoordinateScrollRecyclerView(Context context) {
    super(context);
  }

  public CoordinateScrollRecyclerView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public CoordinateScrollRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
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

  private void scrollToPositionInternal(int position, boolean isSmooth) {
    MultiRVScrollView multiRVScrollView = findMultiRVScrollView();
    if (multiRVScrollView != null && multiRVScrollView.isCoordinatedWith(this)
        && getChildCount() > 0) {
      ViewHolder childViewHolder = getChildViewHolder(getChildAt(getChildCount() - 1));
      if (childViewHolder != null) {
        int coordinatedTop = multiRVScrollView.getCoordinatedTop(this);
        if (childViewHolder.getAdapterPosition() < position) {
          multiRVScrollView.scrollTo(0, coordinatedTop);
          rvScrollToPosition(position, isSmooth);
        } else {
          ViewHolder targetViewHolder = findViewHolderForAdapterPosition(position);
          if (targetViewHolder != null &&
              targetViewHolder.itemView.getBottom()
                  + coordinatedTop
                  - multiRVScrollView.getScrollY() > multiRVScrollView.getHeight()) {
            multiRVScrollView.scrollTo(0, coordinatedTop);
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
      CoordinateScrollRecyclerView.super.smoothScrollToPosition(position);
    } else {
      CoordinateScrollRecyclerView.super.scrollToPosition(position);
    }
  }

  private MultiRVScrollView findMultiRVScrollView() {
    ViewParent parent = getParent();
    while (parent != null) {
      if (parent instanceof MultiRVScrollView) {
        return (MultiRVScrollView) parent;
      }
      parent = parent.getParent();
    }
    return null;
  }
}
