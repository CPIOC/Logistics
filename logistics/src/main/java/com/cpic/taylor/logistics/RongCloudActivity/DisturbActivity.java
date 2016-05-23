package com.cpic.taylor.logistics.RongCloudActivity;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.base.RongYunContext;
import com.cpic.taylor.logistics.RongCloudUtils.DateUtils;
import com.cpic.taylor.logistics.RongCloudWidget.WinToast;
import com.cpic.taylor.logistics.utils.CloseActivityClass;

import java.util.Calendar;
import java.util.Date;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;

/**
 * Created by Bob on 15/8/24.
 */
public class DisturbActivity extends BaseActionBarActivity implements View.OnClickListener, Handler.Callback {

    private String TAG = DisturbActivity.class.getSimpleName();

    private String mTimeFormat = "HH:mm:ss";
    boolean mIsSetting = false;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disturb);
        CloseActivityClass.activityList.add(this);

        Log.e(TAG, "--------onCreate-DisturbActivity-----");

        getSupportActionBar().setTitle(R.string.new_message_notice);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);

        mCloseNotifacation = (LinearLayout) findViewById(R.id.close_notification);
        mStartNotifacation = (RelativeLayout) findViewById(R.id.start_notification);
        mStartTimeNofication = (TextView) findViewById(R.id.start_time_notification);
        mEndNotifacation = (RelativeLayout) findViewById(R.id.end_notification);
        mEndTimeNofication = (TextView) findViewById(R.id.end_time_notification);
        mNotificationCheckBox = (CheckBox) findViewById(R.id.notification_checkbox);

        mStartNotifacation.setOnClickListener(this);
        mEndNotifacation.setOnClickListener(this);
        mNotificationCheckBox.setOnClickListener(this);

        mHandler = new Handler(this);
        Calendar calendar = Calendar.getInstance();
        hourOfDays = calendar.get(Calendar.HOUR_OF_DAY);
        minutes = calendar.get(Calendar.MINUTE);

        getNotificationStatus();
    }

    /**
     * 得到当前消息免打扰状态
     */
    private void getNotificationStatus() {

        if (RongYunContext.getInstance() == null)
            return;

        if (RongIM.getInstance() == null || RongIM.getInstance().getRongIMClient() == null) {
            Log.e(TAG, "--connect 成功后调用--");
            return;
        }

        mIsSetting = RongYunContext.getInstance().getSharedPreferences().getBoolean("IS_SETTING", false);

        if (!mIsSetting) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    RongIM.getInstance().getRongIMClient().getNotificationQuietHours(new RongIMClient.GetNotificationQuietHoursCallback() {
                        @Override
                        public void onSuccess(String startTime, int spanMins) {
                            if (spanMins > 0) {
                                Message msg = Message.obtain();
                                msg.what = 1;
                                msg.obj = startTime;
                                msg.arg1 = spanMins;
                                mHandler.sendMessage(msg);
                            } else {
                                Message mssg = Message.obtain();
                                mssg.what = 2;
                                mHandler.sendMessage(mssg);
                            }
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {
                            mNotificationCheckBox.setChecked(false);
                            mCloseNotifacation.setVisibility(View.GONE);
                        }
                    });
                }
            });

        } else {
            Message msg = Message.obtain();
            msg.what = 3;
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void onClick(View v) {

//        if (v.equals(mStartNotifacation)) {//开始时间
//
//        }


        switch (v.getId()) {
            case R.id.start_notification://开始时间

                String starttime = RongYunContext.getInstance().getSharedPreferences().getString("START_TIME", null);
                if (starttime != null && !"".equals(starttime)) {
                    hourOfDays = Integer.parseInt(starttime.substring(0, 2));
                    minutes = Integer.parseInt(starttime.substring(3, 5));
                }

                TimePickerDialog timePickerDialog = new TimePickerDialog(DisturbActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        mStartTime = getDaysTime(hourOfDay, minute);

                        mStartTimeNofication.setText(mStartTime);
                        SharedPreferences.Editor editor = RongYunContext.getInstance().getSharedPreferences().edit();
                        editor.putString("START_TIME", mStartTime);
                        editor.commit();

                        String endtime = RongYunContext.getInstance().getSharedPreferences().getString("END_TIME", null);
                        if (endtime != null && !"".equals(endtime)) {
                            Date datastart = DateUtils.stringToDate(mStartTime, mTimeFormat);
                            Date dataend = DateUtils.stringToDate(endtime, mTimeFormat);
                            long spansTime = DateUtils.compareMin(datastart, dataend);
                            setConversationTime(mStartTime, (int) Math.abs(spansTime));
                        }
                    }
                }, hourOfDays, minutes, true);
                timePickerDialog.show();

                break;
            case R.id.end_notification://结束时间
                String endtime = RongYunContext.getInstance().getSharedPreferences().getString("END_TIME", null);
                if (endtime != null && !"".equals(endtime)) {
                    hourOfDays = Integer.parseInt(endtime.substring(0, 2));
                    minutes = Integer.parseInt(endtime.substring(3, 5));
                }
                timePickerDialog = new TimePickerDialog(DisturbActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        mEndTime = getDaysTime(hourOfDay, minute);
                        mEndTimeNofication.setText(mEndTime);
                        SharedPreferences.Editor editor = RongYunContext.getInstance().getSharedPreferences().edit();
                        editor.putString("END_TIME", mEndTime);
                        editor.apply();

                        String starttime = RongYunContext.getInstance().getSharedPreferences().getString("START_TIME", null);
                        if (starttime != null && !"".equals(starttime)) {
                            Date datastart = DateUtils.stringToDate(starttime, mTimeFormat);
                            Date dataend = DateUtils.stringToDate(mEndTime, mTimeFormat);
                            long spansTime = DateUtils.compareMin(datastart, dataend);

                            setConversationTime(starttime, (int) Math.abs(spansTime));
                        }
                    }
                }, hourOfDays, minutes, true);
                timePickerDialog.show();

                break;
            case R.id.notification_checkbox://开关
                if (mNotificationCheckBox.isChecked()) {
                    Message msg = Message.obtain();
                    msg.what = 3;
                    mHandler.sendMessage(msg);
                } else {
                    if (RongIM.getInstance() != null) {

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                RongIM.getInstance().getRongIMClient().removeNotificationQuietHours(new RongIMClient.OperationCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Message msg = Message.obtain();
                                        msg.what = 2;
                                        mHandler.sendMessage(msg);
                                    }

                                    @Override
                                    public void onError(RongIMClient.ErrorCode errorCode) {
                                        Log.e(TAG, "----yb-----移除会话通知周期-oonError:" + errorCode.getValue());
                                    }
                                });
                            }
                        });
                    }
                }
                break;
        }
    }

    /**
     * 得到"HH:mm:ss"类型时间
     *
     * @param hourOfDay 小时
     * @param minite    分钟
     * @return "HH:mm:ss"类型时间
     */
    private String getDaysTime(final int hourOfDay, final int minite) {
        String daysTime;
        String hourOfDayString = "0" + hourOfDay;
        String minuteString = "0" + minite;
        if (hourOfDay < 10 && minite >= 10) {
            daysTime = hourOfDayString + ":" + minite + ":00";
        } else if (minite < 10 && hourOfDay >= 10) {
            daysTime = hourOfDay + ":" + minuteString + ":00";
        } else if (hourOfDay < 10 && minite < 10) {
            daysTime = hourOfDayString + ":" + minuteString + ":00";
        } else {
            daysTime = hourOfDay + ":" + minite + ":00";
        }
        return daysTime;
    }

    /**
     * 设置勿扰时间
     *
     * @param startTime 设置勿扰开始时间 格式为：HH:mm:ss
     * @param spanMins  0 < 间隔时间 < 1440
     */
    private void setConversationTime(final String startTime, final int spanMins) {

        if (RongIM.getInstance() != null && RongIM.getInstance().getRongIMClient() != null && !TextUtils.isEmpty(startTime)) {

            if (spanMins > 0 && spanMins < 1440) {
                Log.e("", "----设置勿扰时间startTime；" + startTime + "---spanMins:" + spanMins);

                RongIM.getInstance().getRongIMClient().setNotificationQuietHours(startTime, spanMins, new RongIMClient.OperationCallback() {

                    @Override
                    public void onSuccess() {
                        SharedPreferences.Editor editor = RongYunContext.getInstance().getSharedPreferences().edit();
                        editor.putBoolean("IS_SETTING", true);
                        editor.apply();

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                WinToast.toast(DisturbActivity.this, "设置消息免打扰成功");
                            }
                        });
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                        Log.e(TAG, "----设置会话通知周期-oonError:" + errorCode.getValue());
                    }
                });
            } else {
                WinToast.toast(DisturbActivity.this, "间隔时间必须>0");
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        SharedPreferences.Editor editor;
        switch (msg.what) {
            case 1:
                mNotificationCheckBox.setChecked(true);
                mCloseNotifacation.setVisibility(View.VISIBLE);
                if (msg != null) {
                    mStartTime = msg.obj.toString();
                    hourOfDays = Integer.parseInt(mStartTime.substring(0, 2));
                    minutes = Integer.parseInt(mStartTime.substring(3, 5));
                    int spanMins = msg.arg1;

                    String time = DateUtils.dateToString(DateUtils.addMinutes(DateUtils.stringToDate(mStartTime, mTimeFormat), spanMins), mTimeFormat);
                    mStartTimeNofication.setText(mStartTime);
                    mEndTimeNofication.setText(time);

                    editor = RongYunContext.getInstance().getSharedPreferences().edit();
                    editor.putString("START_TIME", mStartTime);
                    editor.putString("END_TIME", DateUtils.dateToString(DateUtils.addMinutes(DateUtils.stringToDate(mStartTime, mTimeFormat), spanMins), mTimeFormat));
                    editor.apply();
                }
                break;
            case 2:
                mCloseNotifacation.setVisibility(View.GONE);
                editor = RongYunContext.getInstance().getSharedPreferences().edit();
                editor.remove("IS_SETTING");
                editor.apply();
                break;

            case 3:
                mNotificationCheckBox.setChecked(true);
                mCloseNotifacation.setVisibility(View.VISIBLE);

                if (RongYunContext.getInstance().getSharedPreferences() != null) {
                    String endtime = RongYunContext.getInstance().getSharedPreferences().getString("END_TIME", null);
                    String starttimes = RongYunContext.getInstance().getSharedPreferences().getString("START_TIME", null);

                    if (endtime != null && starttimes != null && !"".equals(endtime) && !"".equals(starttimes)) {
                        Date datastart = DateUtils.stringToDate(starttimes, mTimeFormat);
                        Date dataend = DateUtils.stringToDate(endtime, mTimeFormat);
                        long spansTime = DateUtils.compareMin(datastart, dataend);
                        mStartTimeNofication.setText(starttimes);
                        mEndTimeNofication.setText(endtime);
                        setConversationTime(starttimes, (int) spansTime);
                    } else {
                        mStartTimeNofication.setText("23:59:59");
                        mEndTimeNofication.setText("00:00:00");
                        editor = RongYunContext.getInstance().getSharedPreferences().edit();
                        editor.putString("START_TIME", "23:59:59");
                        editor.putString("END_TIME", "00:00:00");
                        editor.apply();
                    }
                }
                break;
        }
        return false;
    }


    /**
     * 关闭勿扰模式
     */
    private LinearLayout mCloseNotifacation;
    /**
     * 开始时间 RelativeLayout
     */
    private RelativeLayout mStartNotifacation;
    /**
     * 关闭时间 RelativeLayout
     */
    private RelativeLayout mEndNotifacation;
    /**
     * 开始时间
     */
    private TextView mStartTimeNofication;
    /**
     * 关闭时间
     */
    private TextView mEndTimeNofication;
    /**
     * 开关
     */
    private CheckBox mNotificationCheckBox;
    /**
     * 开始时间
     */
    private String mStartTime;
    /**
     * 结束时间
     */
    private String mEndTime;
    /**
     * 小时
     */
    int hourOfDays;
    /**
     * 分钟
     */
    int minutes;
}
