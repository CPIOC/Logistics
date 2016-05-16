package com.cpic.taylor.logistics.fragment;

import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.cpic.taylor.logistics.R;
import com.cpic.taylor.logistics.RongCloudDatabase.UserInfos;
import com.cpic.taylor.logistics.RongCloudModel.MyNewFriends;
import com.cpic.taylor.logistics.RongCloudWidget.WinToast;
import com.cpic.taylor.logistics.activity.HomeActivity;
import com.cpic.taylor.logistics.bean.RouteFriend;
import com.cpic.taylor.logistics.bean.RouteFriendData;
import com.cpic.taylor.logistics.utils.ApkInstaller;
import com.cpic.taylor.logistics.utils.TtsSettings;
import com.cpic.taylor.logistics.utils.UrlUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.sunflower.FlowerCollector;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

import java.util.ArrayList;

/**
 * Created by Taylor on 2016/4/29.
 */
public class HomeRoadFragment extends Fragment {
    private HomeActivity homeActivity;
    private ListView road_info_list;
    private RoadInfoListAdapter roadInfoListAdapter;
    private SpeechSynthesizer mTts;
    // 缓冲进度
    private int mPercentForBuffering = 0;
    // 播放进度
    private int mPercentForPlaying = 0;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    // 默认发音人
    private String voicer = "xiaoyan";

    private SharedPreferences mSharedPreferences;
    // 语记安装助手类
    ApkInstaller mInstaller;
    private String[] mCloudVoicersEntries;
    private String[] mCloudVoicersValue;
    private AnimationDrawable animationDrawable;
    private ImageView iv;
    private HttpUtils post;
    private RequestParams params;
    private SharedPreferences sp;
    MyNewFriends myFriends;
    ArrayList<UserInfos> friendsList = new ArrayList<UserInfos>();
    private RouteFriend routeFriend;
    private RouteFriendData routeFriendData;
    private ArrayList<RouteFriendData> routeFriendDataList = new ArrayList<RouteFriendData>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        homeActivity = (HomeActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_home_road, null);
        road_info_list = (ListView) view.findViewById(R.id.road_info_list);
        mSharedPreferences = homeActivity.getSharedPreferences(TtsSettings.PREFER_NAME, homeActivity.MODE_PRIVATE);
        //roadInfoListAdapter = new RoadInfoListAdapter();
        //road_info_list.setAdapter(roadInfoListAdapter);
        // 云端发音人名称列表
        mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);
        mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);

        mSharedPreferences = homeActivity.getSharedPreferences(TtsSettings.PREFER_NAME, homeActivity.MODE_PRIVATE);
        mInstaller = new ApkInstaller(homeActivity);

        mTts = SpeechSynthesizer.createSynthesizer(homeActivity, null);

        road_info_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (null != animationDrawable) {
                    animationDrawable.stop();
                }
                if (null != iv) {
                    iv.setImageResource(R.mipmap.icon0);
                }
                iv = (ImageView) view.findViewById(R.id.iv_voice);
                iv.setImageResource(R.drawable.ic_launcher);

                iv.setImageResource(R.drawable.animation1);
                animationDrawable = (AnimationDrawable) iv.getDrawable();
                animationDrawable.stop();
                iv.setImageResource(R.mipmap.icon0);
                mTts.stopSpeaking();
                startPlay(iv, "在"+routeFriendDataList.get(i).getAddress()+"发生了"+routeFriendDataList.get(i).getContent());
                iv.setImageResource(R.drawable.ani);
                animationDrawable = (AnimationDrawable) iv.getDrawable();
                animationDrawable.start();
            }
        });
        loadData();
        return view;

    }

    private void loadData() {
        post = new HttpUtils();
        params = new RequestParams();
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        params.addBodyParameter("token", sp.getString("token", null));
        params.addBodyParameter("Start", sp.getString("Start", null));
        params.addBodyParameter("end", sp.getString("end", null));
        String url = UrlUtils.POST_URL + UrlUtils.path_warninglist;
        post.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFailure(HttpException e, String s) {
                showShortToast("连接失败，请检查网络连接");
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result;
                JSONObject jsonObj = null;
                try {

                    Gson gson = new Gson();
                    java.lang.reflect.Type type = new TypeToken<RouteFriend>() {
                    }.getType();
                    routeFriend = gson.fromJson(result, type);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (routeFriend.getCode() == 1) {

                    if (null != routeFriend.getData()) {
                        routeFriendDataList = routeFriend.getData();
                        roadInfoListAdapter = new RoadInfoListAdapter(routeFriendDataList);
                        road_info_list.setAdapter(roadInfoListAdapter);


                    }


                } else {
                    showShortToast(myFriends.getMsg());
                }

            }

        });
    }

    /**
     * Toast短显示
     *
     * @param msg
     */
    protected void showShortToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    private void startPlay(ImageView iv_voice, String str) {
        // 移动数据分析，收集开始合成事件
        FlowerCollector.onEvent(homeActivity, "tts_play");

        String text = "调用此接口请注释";
        text = str;
        // 设置参数
        setParam();

        int code = mTts.startSpeaking(text, mTtsListener);
//			/**
//			 * 只保存音频不进行播放接口,调用此接口请注释startSpeaking接口
//			 * text:要合成的文本，uri:需要保存的音频全路径，listener:回调接口
//			*/
//			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
//			int code = mTts.synthesizeToUri(text, path, mTtsListener);

        if (code != ErrorCode.SUCCESS) {
            if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
//                未安装则跳转到提示安装页面
                mInstaller.install();
            } else {
                WinToast.toast(homeActivity, "语音合成失败,错误码: " + code);
                Log.e("Tag", "code = " + code);
            }
        }
    }

    /**
     * 初始化监听。
     */
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {

            if (code != ErrorCode.SUCCESS) {
                WinToast.toast(homeActivity, "初始化失败,错误码：" + code);
            } else {
                // 初始化成功，之后可以调用startSpeaking方法
                // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };
    /**
     * 合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            // WinToast.toast(homeActivity, "开始播放");
        }

        @Override
        public void onSpeakPaused() {
            WinToast.toast(homeActivity, "暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            WinToast.toast(homeActivity, "继续播放");
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            mPercentForBuffering = percent;
            //WinToast.toast(homeActivity, String.format(getString(R.string.tts_toast_format), mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            mPercentForPlaying = percent;
            //WinToast.toast(homeActivity, String.format(getString(R.string.tts_toast_format),mPercentForBuffering, mPercentForPlaying));

        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                // WinToast.toast(homeActivity, "播放完成");
                animationDrawable = (AnimationDrawable) iv.getDrawable();
                animationDrawable.stop();
                iv.setImageResource(R.drawable.voice);

            } else if (error != null) {
                WinToast.toast(homeActivity, error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };

    /**
     * 参数设置
     *
     * @return
     */
    private void setParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        // 根据合成引擎设置相应参数
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            // 设置在线合成发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
            //设置合成语速
            mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString("speed_preference", "50"));
            //设置合成音调
            mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString("pitch_preference", "50"));
            //设置合成音量
            mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString("volume_preference", "50"));
        } else {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            // 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
            mTts.setParameter(SpeechConstant.VOICE_NAME, "");
            /**
             * TODO 本地合成不设置语速、音调、音量，默认使用语记设置
             * 开发者如需自定义参数，请参考在线合成参数设置
             */
        }
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString("stream_preference", "3"));
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }

    /**
     * 路口信息列表适配器
     */
    public class RoadInfoListAdapter extends BaseAdapter {

        private ArrayList<RouteFriendData> roadInfoList;
        private int id;

        public RoadInfoListAdapter(ArrayList<RouteFriendData> roadInfoList) {

            this.roadInfoList = roadInfoList;
        }

        public RoadInfoListAdapter() {

        }

        @Override
        public int getCount() {
            return roadInfoList.size();
            //return roadInfoList.size();
        }

        @Override
        public Object getItem(int i) {
            return roadInfoList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertview, ViewGroup viewGroup) {

            final ViewHolder vh;
            if (convertview == null) {
                vh = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(homeActivity);
                convertview = inflater.inflate(R.layout.row_received_voice, null);
                vh.userLogo = (ImageView) convertview.findViewById(R.id.iv_userhead);
                vh.timestamp = (TextView) convertview.findViewById(R.id.timestamp);
                vh.iv_voice = (ImageView) convertview.findViewById(R.id.iv_voice);
                vh.tv_length = (TextView) convertview.findViewById(R.id.tv_length);
                vh.iv_unread_voice = (ImageView) convertview.findViewById(R.id.iv_unread_voice);
                vh.pb_sending = (ProgressBar) convertview.findViewById(R.id.pb_sending);
                vh.tv_userid = (TextView) convertview.findViewById(R.id.tv_userid);
                vh.content_layout = (RelativeLayout) convertview.findViewById(R.id.content_layout);
                convertview.setTag(vh);
            } else {
                vh = (ViewHolder) convertview.getTag();
            }

            vh.timestamp.setText(roadInfoList.get(position).getCreated_at());
            Glide.with(getActivity()).load(roadInfoList.get(position).getUser_img()).placeholder(R.mipmap.empty_photo).fitCenter().into(vh.userLogo);


            return convertview;
        }


    }

    class ViewHolder {

        ImageView userLogo;
        TextView timestamp;
        ImageView iv_voice;
        TextView tv_length;
        ImageView iv_unread_voice;
        ProgressBar pb_sending;
        TextView tv_userid;
        RelativeLayout content_layout;

    }
}
