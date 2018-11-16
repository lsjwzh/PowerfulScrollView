package me.zhanghai.android.materialprogressbar;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;


/**
 * A new {@code Drawable} for determinate circular {@code ProgressBar}.
 */
public class RoundCapCircularProgressDrawable extends BaseProgressLayerDrawable<
    SingleCircularProgressDrawable, CircularProgressBackgroundDrawable> {

  /**
   * Create a new {@code CircularProgressDrawable}.
   *
   * @param context the {@code Context} for retrieving style information.
   */
  public RoundCapCircularProgressDrawable(int style, @NonNull Context context) {
    super(new Drawable[]{
        new CircularProgressBackgroundDrawable(),
        new RoundCapSingleCircularProgressDrawable(style),
        new RoundCapSingleCircularProgressDrawable(style),
    }, context);
  }

  public static class RoundCapSingleCircularProgressDrawable extends
      SingleCircularProgressDrawable {
    RoundCapSingleCircularProgressDrawable(int style) {
      super(style);
    }

    @Override
    protected void onPreparePaint(Paint paint) {
      super.onPreparePaint(paint);
      paint.setStrokeCap(Paint.Cap.ROUND);
    }
  }
}