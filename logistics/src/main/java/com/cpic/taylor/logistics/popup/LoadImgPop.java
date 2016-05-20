package com.cpic.taylor.logistics.popup;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.bumptech.glide.Glide;
import com.cpic.taylor.logistics.R;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by Taylor on 2016/5/20.
 */
public class LoadImgPop {

    private PopupWindow pw;
    private int screenWidth;
    private Activity activity;
    private PhotoView ivIcon;
    private SharedPreferences sp;
    private String imgUrl;

    public LoadImgPop( PopupWindow pw, int screenWidth, Activity activity,String imgUrl) {
        this.pw = pw;
        this.screenWidth = screenWidth;
        this.activity = activity;
        this.imgUrl = imgUrl;
    }

    /**
     * 弹出土地选择
     */
    public void showLookCameraPop() {
        View view = View.inflate(activity, R.layout.popload_img, null);
        pw = new PopupWindow(view, screenWidth,screenWidth);
        pw.setFocusable(true);
        ivIcon = (PhotoView) view.findViewById(R.id.pop_load_iv);
        WindowManager.LayoutParams params =	activity.getWindow().getAttributes();
        activity.getWindow().setAttributes(params);
        pw.setBackgroundDrawable(new ColorDrawable());
        pw.setOutsideTouchable(false);
        pw.showAtLocation(view, Gravity.CENTER, 0, 0);
        pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = activity.getWindow().getAttributes();
                params.alpha = 1f;
                activity.getWindow().setAttributes(params);
            }
        });
        Glide.with(activity).load(imgUrl).placeholder(R.mipmap.empty_photo).into(ivIcon);

    }

}
