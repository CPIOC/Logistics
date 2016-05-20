package com.cpic.taylor.logistics.utils;

/**
 * Created by Taylor on 2016/5/11.
 * 物流APP Url
 */
public class UrlUtils {

    public final static String POST_URL = "http://wx.cpioc.com/wl/index.php?m=Api&c=Api&a=";
    //登录
    public final static String path_login = "login";
    //注册接口
    public final static String path_register = "register";
    //验证码
    public final static String path_code = "code";
    //忘记密码
    public final static String path_forgotPwd = "forgotPwd";
    //修改用户资料
    public final static String path_modifyInfo = "modifyUserInfo";
    //报警类型列表
    public final static String path_categorylist = "categorylist";
    //好友列表
    public final static String path_friendslist= "friendslist";
    //群列表
    public final static String path_chat_grouplist = "chat_grouplist";
    //路况列表
    public final static String path_warninglist = "warninglist";
    //添加好友:
    public final static String path_addfriend = "addfriend";
    //创建聊天群:
    public final static String path_createChat= "createChatGroup";
    //报警接口:
    public final static String path_warning = "warning";
    //设置线路接口:
    public final static String path_setRoute = "setRoute";
    //设置位置接口更新经纬度:
    public final static String path_setLocation = "setLocation";
    //删除群成员\退出群组\解散群组:
    public final static String path_delete_meb = "delete_meb";
    //搜索好友:
    public final static String path_search_friends = "search_friends";
    //申请好友状态列表
    public final static String path_applyList = "applyList";
    //申请好友
    public final static String path_apply = "apply";
    //同意拒绝好友操作
    public final static String path_friendAction = "friendAction";
    //通过融云id获取用户信息
    public final static String path_getUserinfo = "getUserinfo";
    //附近的人
    public final static String path_nearList = "nearList";
    //通过账号获取用户信息
    public final static String path_getLogininfo = "getLogininfo";

}
