package com.aqtx.app;

/**
 * Created by y11621546 on 2016/12/8.
 */

public class ContantValue {
    //    是否调试
    //    public static final String BaseUrl = "http://60.28.240.121:4505/";
    public static final String BaseUrl = "https://www.anxinchat.com:11443/";
    //    public static final String BaseUrl = "http://192.168.0.200:8080/";
    //    获取验证码
    public static final String GET_VERIFICATION = BaseUrl + "index/sendVerify";
    //   手机号注册
    public static final String REGISTER_PHONE = BaseUrl + "index/MobileToCreate";
    //    删除好友
    public static final String DELETE_TOMOTACHI = BaseUrl + "index/deleteTomotachi";
    //    账号注册
    public static final String REGISTER_ACCOUNT = BaseUrl + "Reg/AccessToCreate";
    //    登录
    public static final String LOGIN = BaseUrl + "Login/getAccid";
    //    更新用户信息
    public static final String UPDATE_USER_INFO = BaseUrl + "User/updateUserInfo";
    //    获取消息通信录
    public static final String GET_TOMOTACHI = BaseUrl + "Friend/getTomotachi";
    //    获取通讯录联系人
//    public static final String GET_CONTACT_LIST = BaseUrl + "Real/getRealListFive";
    public static final String GET_CONTACT_LIST = BaseUrl + "Real/getRealList12";

    //    保存头像通知
    public static final String SAVE_USRE_ICON = BaseUrl + "User/saveUserIcon";
    //    修改手机号 发送验证码接口
    public static final String SEND_SET_PHONE_VERIFY = BaseUrl + "User/SendSetMobileVerify";
    //    获取用户信息_   搜索
    public static final String GET_USER_INFO = BaseUrl + "User/getUserInfo";
    //    获取用户信息_
    public static final String GET_USER_INFO_INDEX = BaseUrl + "index/getUserInfo";
    //    添加好友
    public static final String MAKE_FRIEND = BaseUrl + "index/makeFriend";
    //设置安信号可以找到我
    public static final String SET_ACCESS_FIND_ME = BaseUrl + "User/setAccessFindMe";
    //    设置找到我
    public static final String SET_FIND_ME = BaseUrl + "User/setFindMe";
    //    设置找到我的状态
    public static final String FIND_STATUS = BaseUrl + "User/findStatus";
    //    删除通知
    public static final String DELETE_FRIEND = BaseUrl + "index/deleteFriend";
    //    对方加入我的群  拒绝
    public static final String REFUSE_GTOUP = BaseUrl + "User/RefusedInTeam";
    //获取二维码
    public static final String GET_QRCODE = BaseUrl + "QRcode/getQRcode";
    //    修改密码
    public static final String SET_PASS = BaseUrl + "index/setpass";

}
