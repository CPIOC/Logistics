package com.cpic.taylor.logistics.RongCloudWidget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.cpic.taylor.logistics.R;


public class SwitchGroup extends ViewGroup {

    private int mPinnedViewResId;
    int mOrientation;
    View mPinnedView;
    ItemHander mItemHander;
    Rect mSwitchRect;

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    public SwitchGroup(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwitchGroup);
        mPinnedViewResId = a.getResourceId(R.styleable.SwitchGroup_pinned_view1, 0);
        mOrientation = a.getInt(R.styleable.SwitchGroup_orientation1, 0);
        int itemResId = a.getResourceId(R.styleable.SwitchGroup_pinned_item1, 0);
        int itemArrayResId = a.getResourceId(R.styleable.SwitchGroup_pinned_item_array1, 0);

        a.recycle();
        mSwitchRect = new Rect();
        if (itemResId != 0 && itemArrayResId != 0) {
            String[] letters = getResources().getStringArray(itemArrayResId);
            int size = letters.length;
            for (int i = 0; i < size; i++) {
                SwitchItemView switchItemView = (SwitchItemView) LayoutInflater.from(getContext()).inflate(itemResId, null);
                switchItemView.setText(letters[i]);
                addView(switchItemView);
            }
            letters = null;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mPinnedView == null && mPinnedViewResId != 0) {
            View parentView = LayoutInflater.from(getContext()).inflate(mPinnedViewResId, (ViewGroup) this.getParent(), true);
            mPinnedView = parentView.findViewById(R.id.de_ui_friend_message);
        }
    }

    public ItemHander getItemHander() {
        return mItemHander;
    }

    public void setItemHander(ItemHander itemHander) {
        this.mItemHander = itemHander;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int cellWidth =1;
         int cellHeight = 1;
        if (mOrientation == HORIZONTAL) {
            if (count != 0) {
                cellWidth = (width - getPaddingLeft() - getPaddingRight()) / count;
                cellHeight = height - getPaddingTop() - getPaddingBottom();
            }
        } else {
            if (count != 0) {
                cellWidth = width - getPaddingLeft() - getPaddingRight();
                cellHeight = (height - getPaddingTop() - getPaddingBottom()) / count;
            }
        }

        final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(cellWidth, MeasureSpec.EXACTLY);
        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(cellHeight, MeasureSpec.EXACTLY);

        for (int i = 0; i < count; ++i) {
            final View child = getChildAt(i);

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int newChildWidthMeasureSpec = getChildMeasureSpec(childWidthMeasureSpec, lp.rightMargin + lp.leftMargin, cellWidth - lp.rightMargin - lp.leftMargin);
            int newChildHeightMeasureSpec = getChildMeasureSpec(childHeightMeasureSpec, lp.topMargin + lp.bottomMargin, cellHeight - lp.topMargin - lp.rightMargin);

            child.measure(newChildWidthMeasureSpec, newChildHeightMeasureSpec);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();

        int currentLeft = getPaddingLeft();
        int currentTop = getPaddingTop();
        int currentRight = 0;
        int currentBottom = 0;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();

            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            currentRight = currentLeft + lp.leftMargin + childWidth;
            currentBottom = currentTop + lp.topMargin + childHeight;

            child.layout(currentLeft + lp.leftMargin, currentTop + lp.rightMargin, currentRight, currentBottom);

            if (mOrientation == HORIZONTAL) {
                currentLeft = currentRight + lp.rightMargin;
            } else {
                currentTop = currentBottom + lp.bottomMargin;
            }
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    public boolean onTouchEvent(MotionEvent event) {

        final float xf = event.getX();
        final float yf = event.getY();
        final float scrolledXFloat = xf + getScrollX();
        final float scrolledYFloat = yf + getScrollY();
        final Rect frame = mSwitchRect;

        final int scrolledXInt = (int) scrolledXFloat;
        final int scrolledYInt = (int) scrolledYFloat;

        int count = getChildCount();

        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child.getVisibility() == VISIBLE || child.getAnimation() != null) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    if (child.isSelected()) {
                        child.setSelected(false);
                    }
                    if (mPinnedView != null && mPinnedView.getVisibility() == View.VISIBLE) {
                        mPinnedView.setVisibility(View.GONE);
                    }
                } else {
                    child.getHitRect(frame);
                    if (frame.contains(scrolledXInt, scrolledYInt)) {
                        if (!child.isSelected()) {
                            child.setSelected(true);
                            if (child instanceof SwitchItemView) {
                                ((SwitchItemView) child).handlerPinnedView(mPinnedView);
                                if (mItemHander != null)
                                    mItemHander.onClick(child);
                                invalidate();
                            }
                        }
                    } else {
                        if (child.isSelected()) {
                            child.setSelected(false);
                        }
                    }
                    if (mPinnedView != null) {
                        int visibility = mPinnedView.getVisibility();
                        if (visibility == View.GONE || visibility == View.INVISIBLE) {
                            mPinnedView.setVisibility(View.VISIBLE);
                        }
                    }
                }

            }
        }

        return true;

    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }
    }

    @Override
    protected void attachViewToParent(View child, int index, ViewGroup.LayoutParams params) {
        super.attachViewToParent(child, index, params);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

    }

    public interface ItemHander extends OnClickListener {

    }

}
