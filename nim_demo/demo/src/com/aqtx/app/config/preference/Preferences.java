package com.aqtx.app.config.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.aqtx.app.DemoCache;

/**
 * Created by hzxuwen on 2015/4/13.
 */
public class Preferences {
    private static final String KEY_USER_ACCOUNT = "account";
    private static final String KEY_USER_TOKEN = "token";
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_USER_UPDATE = "isUpdate";
    private static final String KEY_USER_CONTACT = "contact";
    private static final String KEY_USER_CONTACT_PHONE = "contact_phone";

    public static void saveContactPhone(String contact) {
        saveString(KEY_USER_CONTACT_PHONE, contact);
    }

    public static String getContactPhone() {
        return getSharedPreferences().getString(KEY_USER_CONTACT_PHONE, "{}");
    }

    public static void saveContact(String contact) {
        saveString(KEY_USER_CONTACT, contact);
    }

    public static String getContact() {
        return getSharedPreferences().getString(KEY_USER_CONTACT, "{}");
    }

    public static void saveIsUpdate(boolean isUpdate) {
        saveBoolean(KEY_USER_UPDATE, isUpdate);
    }

    public static boolean isUpdate() {
        return getBoolean(KEY_USER_UPDATE);
    }

    public static void saveUserAccount(String account) {
        saveString(KEY_USER_ACCOUNT, account);
    }

    public static String getUserAccount() {
        return getString(KEY_USER_ACCOUNT);
    }

    public static void saveUserName(String name) {
        saveString(KEY_USER_NAME, name);
    }

    public static String getUserName() {
        return getString(KEY_USER_NAME);
    }

    public static void saveUserToken(String token) {
        saveString(KEY_USER_TOKEN, token);
    }

    public static String getUserToken() {
        return getString(KEY_USER_TOKEN);
    }

    private static void saveString(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(key, value);
        editor.commit();
    }

    private static String getString(String key) {
        return getSharedPreferences().getString(key, "");
    }


    private static void saveBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    private static boolean getBoolean(String key) {
        return getSharedPreferences().getBoolean(key, false);
    }

    static SharedPreferences getSharedPreferences() {
        return DemoCache.getContext().getSharedPreferences("Demo", Context.MODE_PRIVATE);
    }

}
