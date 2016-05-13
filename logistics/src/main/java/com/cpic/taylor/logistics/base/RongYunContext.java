package com.cpic.taylor.logistics.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.cpic.taylor.logistics.RongCloudDatabase.DBManager;
import com.cpic.taylor.logistics.RongCloudDatabase.UserInfos;
import com.cpic.taylor.logistics.RongCloudDatabase.UserInfosDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.UserInfo;

/**
 * Created by Bob on 2015/1/30.
 */
public class RongYunContext {

    private static RongYunContext mDemoContext;
    public Context mContext;
    private RongYunApi mDemoApi;
    private HashMap<String, Group> groupMap;
    private SharedPreferences mPreferences;
    private RongIM.LocationProvider.LocationCallback mLastLocationCallback;
    private UserInfosDao mUserInfoDao;


    public static void init(Context context) {
        mDemoContext = new RongYunContext(context);
    }

    public static RongYunContext getInstance() {

        if (mDemoContext == null) {
            mDemoContext = new RongYunContext();
        }
        return mDemoContext;
    }

    private RongYunContext() {

    }

    private RongYunContext(Context context) {
        mContext = context;
        mDemoContext = this;

        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        //http初始化 用于登录、注册使用
        mDemoApi = new RongYunApi(context);

        mUserInfoDao = DBManager.getInstance(mContext).getDaoSession().getUserInfosDao();
    }


    /**
     * 查询所有数据
     */
    public List<UserInfos> loadAllUserInfos() {

        return mUserInfoDao.loadAll();
    }


    /**
     * 删除 userinfos 表
     */
    public void deleteUserInfos() {

        mUserInfoDao.deleteAll();
    }

    /**
     * 更新 好友信息
     *
     * @param targetid
     * @param status
     */
    public void updateUserInfos(String targetid, String status) {

        UserInfos userInfos = mUserInfoDao.queryBuilder().where(UserInfosDao.Properties.Userid.eq(targetid)).unique();
        userInfos.setStatus(status);
        userInfos.setUsername(userInfos.getUsername());
        userInfos.setPortrait(userInfos.getPortrait());
        userInfos.setUserid(userInfos.getUserid());

        mUserInfoDao.update(userInfos);

    }

    /**
     * 向数据库插入数据
     *
     * @param info 用户信息
     */
    public void insertOrReplaceUserInfos(UserInfos info) {


        mUserInfoDao.insertOrReplace(info);
    }

    /**
     * 向数据库插入数据
     *
     * @param info   用户信息
     * @param status 状态
     */
    public void insertOrReplaceUserInfo(UserInfo info, String status) {

        UserInfos userInfos = new UserInfos();
        userInfos.setStatus(status);
        userInfos.setUsername(info.getName());
        userInfos.setPortrait(String.valueOf(info.getPortraitUri()));
        userInfos.setUserid(info.getUserId());
        mUserInfoDao.insertOrReplace(userInfos);
    }

    /**
     * 通过userid 查找 UserInfos,判断是否为好友，查找的是本地的数据库
     *
     * @param userId
     * @return
     */
    public boolean searcheUserInfosById(String userId) {
        if (userId != null) {

            UserInfos userInfos = mUserInfoDao.queryBuilder().where(UserInfosDao.Properties.Userid.eq(userId)).unique();

            if (userInfos == null)
                return false;

            if (userInfos.getStatus().equals("1") || userInfos.getStatus().equals("3") || userInfos.getStatus().equals("5")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通过userid 查找 UserInfos，查找的是本地的数据库
     *
     * @param userId
     * @return
     */
    public UserInfos getUserInfosById(String userId) {

        if (userId == null)
            return null;

        UserInfos userInfos = mUserInfoDao.queryBuilder().where(UserInfosDao.Properties.Userid.eq(userId)).unique();

        if (userInfos == null)
            return null;

        return userInfos;
    }


    /**
     * 通过userid 查找 UserInfo，查找的是本地的数据库
     *
     * @param userId
     * @return
     */
    public UserInfo getUserInfoById(String userId) {

        if (userId == null)
            return null;
        UserInfos userInfos = mUserInfoDao.queryBuilder().where(UserInfosDao.Properties.Userid.eq(userId)).unique();
        if (userInfos == null && RongYunContext.getInstance() != null) {
            return null;
        }

        if(null==userInfos.getPortrait()){
            userInfos.setPortrait("www.cpioc.com");
        }


        return new UserInfo(userInfos.getUserid(), userInfos.getUsername(), Uri.parse(userInfos.getPortrait()));
    }

    public boolean hasUserId(String userId) {

        if (userId != null) {

            UserInfos userInfos = mUserInfoDao.queryBuilder().where(UserInfosDao.Properties.Userid.eq(userId)).unique();

            if (userInfos == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获得好友列表 ID
     *
     * @return
     */
    public List getFriendListId() {
        List userInfoList = new ArrayList();

        List<UserInfos> userInfos = mUserInfoDao.queryBuilder().where(UserInfosDao.Properties.Status.eq("1")).list();

        if (userInfos == null)
            return null;

        for (int i = 0; i < userInfos.size(); i++) {

            userInfoList.add(userInfos.get(i).getUserid());
        }

        return userInfoList;
    }


    /**
     * 获得好友列表
     *
     * @return
     */
    public ArrayList<UserInfo> getFriendList() {
        List<UserInfo> userInfoList = new ArrayList<UserInfo>();

        List<UserInfos> userInfos = mUserInfoDao.queryBuilder().where(UserInfosDao.Properties.Status.eq("1")).list();

        if (userInfos == null)
            return null;

        for (int i = 0; i < userInfos.size(); i++) {

            if(null==userInfos.get(i).getPortrait()){
                userInfos.get(i).setPortrait("userInfos.get(i).getPortrait()");
            }

            UserInfo userInfo = new UserInfo(userInfos.get(i).getUserid(), userInfos.get(i).getUsername(), Uri.parse(userInfos.get(i).getPortrait()));

            userInfoList.add(userInfo);
        }
        return (ArrayList) userInfoList;
    }

    /**
     * 根据userids获得好友列表
     *
     * @return
     */
    public ArrayList<UserInfo> getUserInfoList(String[] userIds) {

        List<UserInfo> userInfoList = new ArrayList<UserInfo>();
        List<UserInfos> userInfosList = new ArrayList<UserInfos>();
        UserInfo userInfo;
        UserInfos userInfos;

        for (int i = 0; i < userIds.length; i++) {
            userInfos = mUserInfoDao.queryBuilder().where(UserInfosDao.Properties.Userid.eq(userIds[i])).unique();
            userInfosList.add(userInfos);
            if (mUserInfoDao.getKey(userInfosList.get(i)) != null) {
                userInfo = new UserInfo(userInfosList.get(i).getUserid(), userInfosList.get(i).getUsername(), Uri.parse(userInfosList.get(i).getPortrait()));
                userInfoList.add(userInfo);
            }
        }
        if (userInfosList == null)
            return null;


        return (ArrayList) userInfoList;
    }

    /**
     * 根据userids获得好友列表
     *
     * @return
     */
    public ArrayList<UserInfo> getUserInfoList(List list) {

        List<UserInfo> userInfoList = new ArrayList<UserInfo>();
        List<UserInfos> userInfosList = new ArrayList<UserInfos>();
        UserInfo userInfo;
        UserInfos userInfos;

        for (int i = 0; i < list.size(); i++) {
            userInfos = mUserInfoDao.queryBuilder().where(UserInfosDao.Properties.Userid.eq(list.get(i))).unique();
            userInfosList.add(userInfos);
            if (mUserInfoDao.getKey(userInfosList.get(i)) != null) {
                userInfo = new UserInfo(userInfosList.get(i).getUserid(), userInfosList.get(i).getUsername(), Uri.parse(userInfosList.get(i).getPortrait()));
                userInfoList.add(userInfo);
            }
        }

        if (userInfosList == null)
            return null;


        return (ArrayList) userInfoList;
    }

    /**
     * 通过groupid 获得groupname
     *
     * @param groupid
     * @return
     */
    public String getGroupNameById(String groupid) {
        Group groupReturn = null;
        if (!TextUtils.isEmpty(groupid) && groupMap != null) {

            if (groupMap.containsKey(groupid)) {
                groupReturn = groupMap.get(groupid);
            } else
                return null;

        }
        if (groupReturn != null)
            return groupReturn.getName();
        else
            return null;
    }


    public SharedPreferences getSharedPreferences() {
        return mPreferences;
    }


    public void setGroupMap(HashMap<String, Group> groupMap) {
        this.groupMap = groupMap;
    }

    public HashMap<String, Group> getGroupMap() {
        return groupMap;
    }


    public RongYunApi getDemoApi() {
        return mDemoApi;
    }


    public RongIM.LocationProvider.LocationCallback getLastLocationCallback() {
        return mLastLocationCallback;
    }

    public void setLastLocationCallback(RongIM.LocationProvider.LocationCallback lastLocationCallback) {
        this.mLastLocationCallback = lastLocationCallback;
    }


}
