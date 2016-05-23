package com.cpic.taylor.logistics.utils;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuan on 2016/5/23.
 * 关闭所有的Activity
 */
public class CloseActivityClass {

    public static List<Activity> activityList = new ArrayList<Activity>();

    public static void exitClient(Context ctx)
    {
        // 关闭所有Activity
        for (int i = 0; i < activityList.size(); i++)
        {
            if (null != activityList.get(i))
            {

                activityList.get(i).finish();
            }
        }

    }
}
