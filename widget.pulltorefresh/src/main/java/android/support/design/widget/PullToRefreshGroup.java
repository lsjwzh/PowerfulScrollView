package android.support.design.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.lsjwzh.widget.pulltorefresh.R;

public class PullToRefreshGroup extends FrameLayout {
  public PullToRefreshGroup(@NonNull Context context) {
    this(context, null);
  }

  public PullToRefreshGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PullToRefreshGroup(@NonNull Context context, @Nullable AttributeSet attrs, int
      defStyleAttr) {
    super(context, attrs, defStyleAttr);
    final TypedArray a = context.obtainStyledAttributes(
        attrs, R.styleable.PullToRefreshGroup, defStyleAttr, 0);

    int layoutId = a.getResourceId(R.styleable.PullToRefreshGroup_headerLayout, 0);
    if (layoutId != 0) {
      LayoutInflater.from(context).inflate(layoutId, this, true);
    }

    a.recycle();
  }

  public PullToRefreshHostScrollView.IRefreshHeader getRefreshHeader() {
    return (PullToRefreshHostScrollView.IRefreshHeader) getChildAt(0);
  }

  public View getRefreshTargetView() {
    return getChildAt(1);
  }

}
