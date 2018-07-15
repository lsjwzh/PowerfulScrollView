package android.support.design.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class SimpleRefreshHeader extends FrameLayout
    implements PullToRefreshHostScrollView.IRefreshHeader {

  public static final int STATE_NONE = 0;
  public static final int STATE_REFRESING = 1;
  public static final int STATE_EXPANDED = 2;
  int mState = STATE_NONE;
  private ProgressBar mProgress;
  private ValueAnimator mCollapseAnimator;
  private ValueAnimator mExpandAnimator;
  private ValueAnimator mStableAnimator;

  public SimpleRefreshHeader(@NonNull Context context) {
    super(context);
    init();
  }

  public SimpleRefreshHeader(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public SimpleRefreshHeader(@NonNull Context context, @Nullable AttributeSet attrs, int
      defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public SimpleRefreshHeader(@NonNull Context context, @Nullable AttributeSet attrs, int
      defStyleAttr,
                             int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {
    mProgress = new ProgressBar(getContext());
    addView(mProgress, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT));
  }

  @Override
  public void expand(final View refreshTargetView, final Runnable animationEndCallback) {
    cancelCollapseAnim();
    cancelStableAnim();
    if (mExpandAnimator == null) {
      mExpandAnimator = ObjectAnimator.ofInt(0, getRefreshHeight());
      mExpandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
          setVisibleHeight(refreshTargetView, (Integer) animation.getAnimatedValue());
          refreshTargetView.setTranslationY((Integer) animation.getAnimatedValue());
        }
      });
      mExpandAnimator.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          mExpandAnimator = null;
          refreshTargetView.setTranslationY(0);
          if (animationEndCallback != null) {
            animationEndCallback.run();
          }
        }
      });
      mExpandAnimator.start();
    }
  }

  @Override
  public void collapse(final View refreshTargetView, final Runnable animationEndCallback) {
    cancelExpandAnim();
    cancelStableAnim();
    mState = STATE_NONE;
    if (mCollapseAnimator == null) {
      mCollapseAnimator = ObjectAnimator.ofInt((int) refreshTargetView.getTranslationY(), 0);
      mCollapseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
          setVisibleHeight(refreshTargetView, (Integer) animation.getAnimatedValue());
          refreshTargetView.setTranslationY((Integer) animation.getAnimatedValue());
        }
      });
      mCollapseAnimator.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          mCollapseAnimator = null;
          refreshTargetView.setTranslationY(0);
          if (animationEndCallback != null) {
            animationEndCallback.run();
          }
        }
      });
      mCollapseAnimator.start();
    }
  }

  public int getPullToExpandTriggerHeight() {
    return Integer.MAX_VALUE;
  }


  @Override
  public int getRefreshTriggerHeight() {
    return getHeight();
  }

  public int getRefreshHeight() {
    return getHeight();
  }

  @Override
  public void cancelAnimation() {
    cancelCollapseAnim();
    cancelExpandAnim();
    cancelStableAnim();
  }

  @Override
  public void setVisibleHeight(View refreshTargetView, int targetHeight) {
    if (mState != STATE_REFRESING) {
      setTranslationY(Math.max(targetHeight - getRefreshHeight(), 0));
      targetHeight = Math.min(targetHeight, getRefreshHeight());
      mProgress.setMax(getRefreshHeight());
      mProgress.setProgress(targetHeight);
      float scale = targetHeight * 1f / getRefreshHeight();
      mProgress.setScaleX(scale);
      mProgress.setScaleY(scale);
      mProgress.setTranslationY((scale - 1) * mProgress.getHeight() / 2);
    } else {
      setTranslationY(targetHeight - getRefreshHeight());
    }
  }

  @Override
  public boolean moveToStableState(final View refreshTargetView, final Runnable
      animationEndCallback) {
    cancelExpandAnim();
    cancelCollapseAnim();
    if (mStableAnimator == null) {
      final int targetY;
      if (mState == STATE_NONE
          && refreshTargetView.getTranslationY() >= getPullToExpandTriggerHeight()) {
        targetY = getScrollViewHeight();
        mState = STATE_EXPANDED;
      } else if (mState == STATE_EXPANDED) {
        targetY = 0;
        mState = STATE_NONE;
      } else {
        targetY = getRefreshHeight();
        mState = STATE_REFRESING;
      }
      mStableAnimator = ObjectAnimator.ofInt((int) refreshTargetView.getTranslationY(),
          targetY);
      mStableAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
          setVisibleHeight(refreshTargetView, (Integer) animation.getAnimatedValue());
          refreshTargetView.setTranslationY((Integer) animation.getAnimatedValue());
        }
      });
      mStableAnimator.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          mStableAnimator = null;
          setVisibleHeight(refreshTargetView, targetY);
          refreshTargetView.setTranslationY(targetY);
          if (animationEndCallback != null) {
            animationEndCallback.run();
          }
        }
      });
      mStableAnimator.start();
    }
    return mState == STATE_REFRESING;
  }

  private int getScrollViewHeight() {
    return ((View) getParent().getParent()).getHeight();
  }

  private void cancelStableAnim() {
    if (mStableAnimator != null) {
      mStableAnimator.cancel();
      mStableAnimator = null;
    }
  }

  private void cancelExpandAnim() {
    if (mExpandAnimator != null) {
      mExpandAnimator.cancel();
      mExpandAnimator = null;
    }
  }

  private void cancelCollapseAnim() {
    if (mCollapseAnimator != null) {
      mCollapseAnimator.cancel();
      mCollapseAnimator = null;
    }
  }


}
