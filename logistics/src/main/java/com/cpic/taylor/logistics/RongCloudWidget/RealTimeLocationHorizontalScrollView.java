package com.cpic.taylor.logistics.RongCloudWidget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.cpic.taylor.logistics.R;
import com.sea_monster.resource.Resource;

import io.rong.imkit.RLog;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.model.UserInfo;

/**
 * Created by zhjchen on 8/12/15.
 */
public class RealTimeLocationHorizontalScrollView extends RongHorizontalScrollView {

    private LayoutInflater mInflater;

    public RealTimeLocationHorizontalScrollView(Context context) {
        super(context);
        init();
    }

    public RealTimeLocationHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RealTimeLocationHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public RealTimeLocationHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mInflater = LayoutInflater.from(getContext());
    }


    public void addUserToView(UserInfo userInfo) {
        RLog.e(this, "RealTimeLocationHorizontalScrollView", "addUserToView:---" + userInfo.getUserId());

        View view = mInflater.inflate(R.layout.item_horizontal_scroll_view, null);
        AsyncImageView imageView = (AsyncImageView) view.findViewById(android.R.id.icon);

        if (null != userInfo.getPortraitUri())
            imageView.setResource(new Resource(userInfo.getPortraitUri()));

        view.setTag(userInfo.getUserId());

        addLayoutChildView(view);
    }

    public void removeUserFromView(String userId) {
        RLog.e(this, "RealTimeLocationHorizontalScrollView", "removeUserFromView:---" + userId);
        View view = getLayoutChileView(userId);
        removeLayoutChildView(view);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
//        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        EventBus.getDefault().unregister(this);
    }
}
