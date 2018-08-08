package android.support.v7.widget;

import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class FlingStopSupportLinearSnapHelper extends LinearSnapHelper {

  List<OnFlingStopListener> mListeners = new ArrayList<>();

  @Nullable
  @Override
  protected RecyclerView.SmoothScroller createScroller(RecyclerView.LayoutManager layoutManager) {
    if (!(layoutManager instanceof RecyclerView.SmoothScroller.ScrollVectorProvider)) {
      return null;
    }
    return new LinearSmoothScroller(mRecyclerView.getContext()) {
      @Override
      protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
        int[] snapDistances = calculateDistanceToFinalSnap(mRecyclerView.getLayoutManager(),
            targetView);
        final int dx = snapDistances[0];
        final int dy = snapDistances[1];
        final int time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)));
        if (time > 0) {
          action.update(dx, dy, time, mDecelerateInterpolator);
        }
      }

      @Override
      protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
        return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
      }

      @Override
      protected void onStop() {
        super.onStop();
        for (OnFlingStopListener listener : mListeners) {
          listener.onFlingStop();
        }
      }
    };
  }

  public void addOnFlingStopListener(OnFlingStopListener listener) {
    mListeners.add(listener);
  }

  public void removeOnFlingStopListener(OnFlingStopListener listener) {
    mListeners.remove(listener);
  }

  public interface OnFlingStopListener {
    void onFlingStop();
  }
}
