package com.netease.nim.uikit;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by hzxuwen on 2015/10/21.
 */
public class UserPreferences {

    private final static String KEY_EARPHONE_MODE = "KEY_EARPHONE_MODE";
    private final static String KEY_MSG_MODE = "KEY_MSG_MODE";

    public static void saveMsg(String msg) {
        getSharedPreferences().edit().putString(KEY_MSG_MODE, msg).apply();
    }

    public static String getMsg() {
        return getSharedPreferences().getString(KEY_MSG_MODE, "");
    }

    public static void setEarPhoneModeEnable(boolean on) {
        saveBoolean(KEY_EARPHONE_MODE, on);
    }

    public static boolean isEarPhoneModeEnable() {
        return getBoolean(KEY_EARPHONE_MODE, true);
    }

    private static boolean getBoolean(String key, boolean value) {
        return getSharedPreferences().getBoolean(key, value);
    }

    private static void saveBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

   public static SharedPreferences getSharedPreferences() {
        return NimUIKit.getContext().getSharedPreferences("UIKit." + NimUIKit.getAccount(), Context.MODE_PRIVATE);
    }
}
