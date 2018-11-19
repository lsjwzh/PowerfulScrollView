package com.lsjwzh.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.lsjwzh.widget.pulltozoom.R;

public class PullToZoomCoreChild extends RelativeLayout {
  public PullToZoomCoreChild(Context context) {
    super(context);
  }

  public PullToZoomCoreChild(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public PullToZoomCoreChild(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
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


  public static class LayoutParams extends RelativeLayout.LayoutParams {
    public static final int ACTION_TRANSLATION = 0;
    public static final int ACTION_SCALE = 1;

    public int actionWhenOverScroll = ACTION_TRANSLATION;

    public LayoutParams(Context c, AttributeSet attrs) {
      super(c, attrs);
      init(c, attrs);
    }

    public LayoutParams(int w, int h) {
      super(w, h);
    }

    public LayoutParams(ViewGroup.LayoutParams source) {
      super(source);
    }

    public LayoutParams(MarginLayoutParams source) {
      super(source);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public LayoutParams(RelativeLayout.LayoutParams source) {
      super(source);
    }


    public void init(@NonNull Context c, @Nullable AttributeSet attrs) {
      final TypedArray a
          = c.obtainStyledAttributes(attrs, R.styleable.PullToZoomCoreChild_Layout);
      actionWhenOverScroll = a.getInt(R.styleable.PullToZoomCoreChild_Layout_actionWhenOverScroll,
          ACTION_TRANSLATION);
      a.recycle();
    }
  }
}
