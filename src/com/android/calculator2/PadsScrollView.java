/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.calculator2;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * The pads on a short screen: the input pad fills the view but for the advanced pad's label
 * peeking under it, the advanced pad below, scrolled or stepped to. Its child is a vertical
 * LinearLayout of the input pad, the label and the advanced pad.
 */
public class PadsScrollView extends ScrollView {

    /** The advanced pad's height against the input pad's. */
    private static final float ADVANCED_RATIO = 0.45f;

    public PadsScrollView(Context context) {
        this(context, null);
    }

    public PadsScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFillViewport(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        if (getChildCount() > 0 && getChildAt(0) instanceof ViewGroup pads
                && pads.getChildCount() == 3) {
            final View input = pads.getChildAt(0);
            final View label = pads.getChildAt(1);
            final View advanced = pads.getChildAt(2);
            label.measure(MeasureSpec.makeMeasureSpec(
                    MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            final int inputHeight = Math.max(0, height - label.getMeasuredHeight());
            setHeight(input, inputHeight);
            setHeight(advanced, Math.round(inputHeight * ADVANCED_RATIO));
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private static void setHeight(View view, int height) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp.height != height) {
            lp.height = height;
            view.setLayoutParams(lp);
        }
    }
}
