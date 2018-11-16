/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialprogressbar;

import android.content.Context;
import android.graphics.Paint;

/**
 * A backported {@code Drawable} for indeterminate circular {@code ProgressBar}.
 */
public class RoundCapIndeterminateCircularProgressDrawable extends IndeterminateCircularProgressDrawable {


    /**
     * Create a new {@code IndeterminateCircularProgressDrawable}.
     *
     * @param context the {@code Context} for retrieving style information.
     */
    public RoundCapIndeterminateCircularProgressDrawable(Context context) {
        super(context);
    }

    @Override
    protected void onPreparePaint(Paint paint) {
        super.onPreparePaint(paint);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }
}
