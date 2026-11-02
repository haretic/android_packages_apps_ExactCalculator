/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.calculator2;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class DisplayMotionLayout extends MotionLayout {
    private int mPointerId;
    private boolean mIsScrolling;
    private PointF mPreviousPoint;
    private MotionEvent mPreviousEvent;
    private final int mTouchSlop;
    private boolean mOutOfBounds;
    private int mDisplayHeight = -1;
    private int mPadsHeight = -1;

    public DisplayMotionLayout(@NonNull Context context) {
        this(context, null);
    }

    public DisplayMotionLayout(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public DisplayMotionLayout(@NonNull Context context, @Nullable AttributeSet attributeSet,
                               int i) {
        super(context, attributeSet, i);
        mPointerId = -1;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int height = MeasureSpec.getSize(heightMeasureSpec)
                - getPaddingTop() - getPaddingBottom();
        if (findViewById(R.id.pads_scroll) != null && height > 0) {
            final View display = findViewById(R.id.display);
            final int width = Math.max(0, MeasureSpec.getSize(widthMeasureSpec)
                    - getPaddingLeft() - getPaddingRight());
            // The display's percentage must leave room for the toolbar and both text rows.
            display.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            final int displayHeight = Math.min(height, Math.max(display.getMeasuredHeight(),
                    Math.round(height * getResources().getFloat(R.dimen.layout_display_ratio))));
            final int padsHeight = height - displayHeight;
            if (displayHeight != mDisplayHeight || padsHeight != mPadsHeight) {
                mDisplayHeight = displayHeight;
                mPadsHeight = padsHeight;
                // History moves the same display and pads below the screen without resizing them.
                for (int state : new int[] {R.id.start_state, R.id.end_state}) {
                    final ConstraintSet constraints = getConstraintSet(state);
                    constraints.constrainHeight(R.id.display, displayHeight);
                    constraints.constrainHeight(R.id.pads_scroll, padsHeight);
                    constraints.constrainHeight(R.id.animation_helper, padsHeight);
                    updateState(state, constraints);
                }
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public final boolean onInterceptTouchEvent(@NonNull MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                Rect hitRect = new Rect();

                findViewById(R.id.display).getHitRect(hitRect);
                if (hitRect.contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
                    mPreviousPoint = new PointF(motionEvent.getX(),motionEvent.getY());
                    mPointerId = motionEvent.getPointerId(0);
                    mIsScrolling = false;
                    mOutOfBounds = false;
                    saveLastMotion(motionEvent);
                } else {
                    mPointerId = -1;
                    mIsScrolling = false;
                    mOutOfBounds = true;
                    clearLastMotion();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_UP:
                mIsScrolling = false;
                mOutOfBounds = false;
                mPointerId = -1;
                clearLastMotion();
                break;
            case MotionEvent.ACTION_MOVE:
                int pointerIndex = motionEvent.findPointerIndex(mPointerId);
                if (mPointerId != -1 && pointerIndex != -1 && !mOutOfBounds) {
                    float y = Math.abs(motionEvent.getY(pointerIndex) - mPreviousPoint.y);
                    if (y > mTouchSlop) {
                        mIsScrolling = true;
                        onTouchEvent(mPreviousEvent);
                    }
                }
                break;
        }
        if (super.onInterceptTouchEvent(motionEvent)) {
            return true;
        }
        return mIsScrolling & !mOutOfBounds;
    }

    private void saveLastMotion(@NonNull MotionEvent motionEvent) {
        if (mPreviousEvent != null) {
            mPreviousEvent.recycle();
        }
        mPreviousEvent = MotionEvent.obtain(motionEvent);
    }

    private void clearLastMotion()
    {
        if (mPreviousEvent != null) {
            mPreviousEvent.recycle();
            mPreviousEvent = null;
        }
    }
}
