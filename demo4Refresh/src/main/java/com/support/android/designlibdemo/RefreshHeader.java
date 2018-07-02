package com.support.android.designlibdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.PullToRefreshContainer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class RefreshHeader extends FrameLayout implements PullToRefreshContainer.IRefreshHeader {

  private ProgressBar mProgress;
  private ValueAnimator mCollapseAnimator;
  private ValueAnimator mExpandAnimator;

  public RefreshHeader(@NonNull Context context) {
    super(context);
    init();
  }

  public RefreshHeader(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public RefreshHeader(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public RefreshHeader(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
                       int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {
    LayoutInflater.from(getContext()).inflate(R.layout.refresh_header, this, true);
    mProgress = (ProgressBar) findViewById(R.id.progress);
  }

  @Override
  public void expand(final View refreshTargetView, final Runnable animationEndCallback) {
    cancelCollapseAnim();
    if (mExpandAnimator == null) {
      mExpandAnimator = ObjectAnimator.ofInt(getMaxHeight(), 0);
      mExpandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
          setVisibleHeight((Integer) animation.getAnimatedValue());
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
    if (mCollapseAnimator == null) {
      mCollapseAnimator = ObjectAnimator.ofInt((int) refreshTargetView.getTranslationY(), 0);
      mCollapseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
          setVisibleHeight((Integer) animation.getAnimatedValue());
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

  @Override
  public int getMaxHeight() {
    return getHeight();
  }

  @Override
  public void cancelAnimation() {
    cancelCollapseAnim();
    cancelExpandAnim();
  }

  @Override
  public void setVisibleHeight(int targetHeight) {
    mProgress.setMax(getMaxHeight());
    mProgress.setProgress(targetHeight);
    float scale = targetHeight * 1f / getMaxHeight();
    mProgress.setScaleX(scale);
    mProgress.setScaleY(scale);
    mProgress.setTranslationY((scale - 1) * mProgress.getHeight() / 2);
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
