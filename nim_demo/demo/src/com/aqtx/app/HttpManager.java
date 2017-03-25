package com.aqtx.app;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.Map;

/**
 * 请求封装
 * Created by y11621546 on 2016/12/23.
 */

public class HttpManager {
    private volatile static HttpManager INSTANCE;

    //获取单例
    public static HttpManager getInstance() {
        if (INSTANCE == null) {
            synchronized (HttpManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HttpManager();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * post请求
     *
     * @param url      地址
     * @param params   参数
     * @param callback 回调
     */
    public void post(String url, Map<String, String> params, StringCallback callback) {
        OkHttpUtils.post().url(url).params(params).build().execute(callback);
    }

    /**
     * get请求
     *
     * @param url      地址
     * @param callback 回调
     */
    private void post(String url, StringCallback callback) {
        OkHttpUtils.post().url(url).build().execute(callback);
    }

    //添加好友
    public void addFriend(String url, String accid, String faccid, String type, String msg, StringCallback callback) {

    }


}
