package com.cpic.taylor.logistics.base;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.cpic.taylor.logistics.RongCloudMessage.AgreedFriendRequestMessage;
import com.cpic.taylor.logistics.RongCloudMessage.provider.ContactNotificationMessageProvider;
import com.cpic.taylor.logistics.RongCloudMessage.provider.NewDiscussionConversationProvider;
import com.cpic.taylor.logistics.RongCloudMessage.provider.RealTimeLocationMessageProvider;
import com.iflytek.cloud.SpeechUtility;

import cn.jpush.android.api.JPushInterface;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imlib.ipc.RongExceptionHandler;

public class MyApplication extends Application {
    /**
     * 日志的开关，false：不打印Log；true：打印Log
     */
    public static final boolean isDebug = false;

    /**
     * 全局上下文
     */
    private Context mContext;

    /**
     * 屏幕的宽度
     */
    public static int mDisplayWitdh;

    /**
     * 屏幕的高度
     */
    public static int mDisplayHeight;

    private static final String TAG = "JPush";


    private SharedPreferences sp;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        initDisplay();

        /**
         * 科大讯飞初始化
         */
        SpeechUtility.createUtility(MyApplication.this, "appid=" + "565bb0b6");

        /**
         * Jpush
         */
//        JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);            // 初始化 JPush

/**
 * 注意：
 *
 * IMKit SDK调用第一步 初始化
 *
 * context上下文
 *
 * 只有两个进程需要初始化，主进程和 push 进程
 */
        if (getApplicationInfo().packageName.equals(getCurProcessName(getApplicationContext())) ||
                "io.rong.push".equals(getCurProcessName(getApplicationContext()))) {

            RongIM.init(this);

            /**
             * 融云SDK事件监听处理
             *
             * 注册相关代码，只需要在主进程里做。
             */
            if (getApplicationInfo().packageName.equals(getCurProcessName(getApplicationContext()))) {

                RongCloudEvent.init(this);
                RongYunContext.init(this);

                Thread.setDefaultUncaughtExceptionHandler(new RongExceptionHandler(this));

                try {
                    RongIM.registerMessageType(AgreedFriendRequestMessage.class);

                    RongIM.registerMessageTemplate(new ContactNotificationMessageProvider());
                    RongIM.registerMessageTemplate(new RealTimeLocationMessageProvider());
                    //@ 消息模板展示
                    RongContext.getInstance().registerConversationTemplate(new NewDiscussionConversationProvider());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        initLogin();


    }

    private void initLogin() {
//        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        boolean isLogin = sp.getBoolean("isLogin",false);
//        if (isLogin){
//            new Handler().postDelayed(new Runnable() {
//                public void run() {
//                    Intent mainIntent = new Intent(getApplicationContext(),HomeActivity.class);
//                   startActivity(mainIntent);
//
//                }
//            }, 1000);

//            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
//            startActivity(intent);

//        }
    }

    private void initDisplay() {

    }

    /**************************************融云代码************************************************/


    /**
     * 获取进程名称
     *
     * @param context
     * @return
     */
    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                Log.e("Test", "" + appProcess.processName);
                return appProcess.processName;
            }
        }
        return null;
    }

}
