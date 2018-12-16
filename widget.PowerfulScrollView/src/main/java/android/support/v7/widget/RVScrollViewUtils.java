package android.support.v7.widget;


import android.support.v4.widget.ScrollerCompat;
import android.view.View;
import android.widget.OverScroller;

import java.lang.reflect.Field;

public class RVScrollViewUtils {

  public static int scrollVerticallyBy(RecyclerView recyclerView, int scroll) {
    return recyclerView.getLayoutManager()
        .scrollVerticallyBy(scroll, recyclerView.mRecycler, recyclerView.mState);
  }

  public static boolean isTopOverScrolled(RecyclerView recyclerView) {
    if (recyclerView.getChildCount() == 0) {
      return true;
    }
    View topChild = recyclerView.getChildAt(0);
    final int topChildAdapterPosition = recyclerView.getChildAdapterPosition(topChild);
    return topChildAdapterPosition == 0 && topChild.getScrollY() == 0;
  }

  public static boolean isBottomOverScrolled(RecyclerView recyclerView) {
    if (recyclerView.getChildCount() == 0) {
      return true;
    }
    View bottomChild = recyclerView.getChildAt(recyclerView.getChildCount() - 1);
    final int topChildAdapterPosition = recyclerView.getChildAdapterPosition(bottomChild);
    return topChildAdapterPosition == recyclerView.getAdapter().getItemCount() - 1
        && bottomChild.getBottom() + recyclerView.getPaddingBottom() == recyclerView.getHeight();
  }

  public static float getCurrentVelocityY(RecyclerView recyclerView) {
    try {
      Field mScrollerField = recyclerView.mViewFlinger.getClass().getDeclaredField("mScroller");
      mScrollerField.setAccessible(true);
      Object scroller = mScrollerField.get(recyclerView.mViewFlinger);
      return (scroller instanceof ScrollerCompat)
          ? ((ScrollerCompat) scroller).getCurrVelocity()
          : ((OverScroller) scroller).getCurrVelocity();
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
      return 0;
    } catch (IllegalAccessException e) {
      e.printStackTrace();
      return 0;
    }
  }
}
