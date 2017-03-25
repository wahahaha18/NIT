package com.aqtx.app.contact.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aqtx.app.Cate;
import com.aqtx.app.Contact;
import com.aqtx.app.ContactSection;
import com.aqtx.app.ContactSectionAdapter;
import com.aqtx.app.ContantValue;
import com.aqtx.app.DemoCache;
import com.aqtx.app.FirstLetterUtil;
import com.aqtx.app.HttpManager;
import com.aqtx.app.L;
import com.aqtx.app.R;
import com.aqtx.app.config.preference.Preferences;
import com.aqtx.app.main.activity.MainActivity;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.netease.nim.uikit.BuildConfig;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nim.uikit.permission.MPermission;
import com.netease.nim.uikit.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.permission.annotation.OnMPermissionGranted;
import com.orhanobut.logger.Logger;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;

/**
 * 添加好友页面
 * Created by huangjun on 2015/8/11.
 */
public class AddContactActivity extends UI {
    /**
     * 获取库Phon表字段
     **/
    private static final String[] PHONES_PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Photo.PHOTO_ID, ContactsContract.CommonDataKinds.Phone.CONTACT_ID};

    /**
     * 联系人显示名称
     **/
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;

    /**
     * 电话号码
     **/
    private static final int PHONES_NUMBER_INDEX = 1;

    /**
     * 头像ID
     **/
    private static final int PHONES_PHOTO_ID_INDEX = 2;

    /**
     * 联系人的ID
     **/
    private static final int PHONES_CONTACT_ID_INDEX = 3;


    /**
     * 联系人
     **/
    private ArrayList<String[]> mContactsName = new ArrayList<String[]>();
    private List<Cate> list;
    private Map<String, String> map = new HashMap<>();
    //    手机号明文
    private Map<String, String> mapPhone = new HashMap<>();
    String[] contactArray;
    //---------------------------------------------------//

    //    通讯录列表
    private RecyclerView rvContact;

    private ContactSectionAdapter adapter;

    public static final void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, AddContactActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        NIMClient.getService(FriendServiceObserve.class).observeFriendChangedNotify(friendChangedNotifyObserver, true);

    }


    @Override
    protected void onStop() {
        super.onStop();
//        NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
    }

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact_activity);
        TelephonyManager phoneMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        ToolBarOptions options = new ToolBarOptions();
        options.titleId = R.string.look_contact;
        setToolBar(R.id.toolbar, options);
        findViews();

        rvContact.setLayoutManager(new LinearLayoutManager(this));
        rvContact.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
//        initActionbar();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            addFriend();
        }


    }

    /**
     * 基本权限管理
     */
    private void requestBasicPermission() {
        MPermission.with(AddContactActivity.this)
                .addRequestCode(1)
                .permissions(
                        Manifest.permission.READ_CONTACTS
                )
                .request();
    }


    @OnMPermissionGranted(1)
    public void onBasicPermissionSuccess() {
//        Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
        addFriend();
    }

    @OnMPermissionDenied(1)
    public void onBasicPermissionFailed() {
        Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
    }


    /**
     * 获取通讯录名字
     *
     * @param key 手机号MD5
     */
    private String getContactName(Map<String, String> map, String key) {

        if (map.isEmpty()) {
            return "";
        } else {
            if (map.containsKey(key)) {
                return map.get(key);
            } else {
                return "";
            }
        }

    }

    private void addFriend() {
        mContactsName.clear();
        final List<ContactSection> data = new ArrayList<>();
        ContentResolver resolver = getContentResolver();
        // 获取手机联系人
        Cursor phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PHONES_PROJECTION, null, null, null);


        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {
                //得到手机号码
                String phoneNumber = phoneCursor.getString(PHONES_NUMBER_INDEX);
                //当手机号码为空的或者为空字段 跳过当前循环
                if (TextUtils.isEmpty(phoneNumber)) continue;
                //得到联系人名称
                String contactName = phoneCursor.getString(PHONES_DISPLAY_NAME_INDEX);
//                Log.d("AddFriendActivity", phoneNumber.replace(" ", ""));
                contactArray = new String[]{FirstLetterUtil.getFirstLetter(contactName), MD5.getStringMD5(phoneNumber.replace(" ", ""))};
//                contactArray = new String[]{contactName, phoneNumber.replace(" ", "")};
                mContactsName.add(contactArray);
                map.put(MD5.getStringMD5(phoneNumber.replace(" ", "")), contactName);
                mapPhone.put(MD5.getStringMD5(phoneNumber.replace(" ", "")), phoneNumber);


            }
            if (!phoneCursor.moveToNext()) {
                Preferences.saveContact(JSON.toJSONString(map));
                Preferences.saveContactPhone(JSON.toJSONString(mapPhone));
                Map<String, String> params = new HashMap<>();
                params.put("accid", DemoCache.getAccount());
                params.put("mobile", JSON.toJSONString(mContactsName));
//                HttpManager.getInstance().post("http://60.28.240.35:14505/Real/getRealList12", params, new StringCallback() {
                HttpManager.getInstance().post(ContantValue.GET_CONTACT_LIST, params, new StringCallback() {
                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        DialogMaker.showProgressDialog(AddContactActivity.this, "正在匹配通讯录", false);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Toast.makeText(AddContactActivity.this, "请求超时,请重试", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        DialogMaker.dismissProgressDialog();
                        finish();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.d("AddFriendActivity222", response);
                        JSONObject jsonObject = JSON.parseObject(response);
                        DialogMaker.dismissProgressDialog();
                        if (jsonObject.getString("code").equals("200")) {
                            list = JSON.parseArray(jsonObject.getJSONArray("data").toJSONString(), Cate.class);
                            for (int i = 0; i < list.size(); i++) {
                                data.add(new ContactSection(true, list.get(i).getF()));
                                for (int j = 0; j < list.get(i).getList().size(); j++) {
                                    data.add(new ContactSection(list.get(i).getList().get(j)));
                                }
                            }
                            adapter = new ContactSectionAdapter(data);
                            rvContact.setAdapter(adapter);

                            rvContact.addOnItemTouchListener(new OnItemChildClickListener() {
                                @Override
                                public void onSimpleItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, final int i) {
                                    switch (view.getId()) {
                                        case R.id.btn_accept:
                                            check(data.get(i).t.getFaccid(), "3");
                                            break;
                                        case R.id.btn_add:
//                                            query(data.get(i).t.getFaccid());
                                            // TODO: 2016/12/12 添加好友
                                            String text = ((Button) view).getText().toString().trim();
                                            if (TextUtils.isEmpty(text)) return;
                                            if ("添加".equals(text)) {
                                                Map<String, String> map = new HashMap<String, String>();
                                                map.put("accid", DemoCache.getAccount());
                                                map.put("faccid", data.get(i).t.getFaccid());
                                                map.put("type", "2");
                                                map.put("msg", "我是" + Preferences.getUserName());
                                                HttpManager.getInstance().post(ContantValue.MAKE_FRIEND, map, new StringCallback() {
                                                    @Override
                                                    public void onError(Call call, Exception e, int id) {
                                                        DialogMaker.dismissProgressDialog();
                                                        Toast.makeText(AddContactActivity.this, "网络有误，添加失败", Toast.LENGTH_SHORT).show();
                                                    }

                                                    @Override
                                                    public void onResponse(String response, int id) {
                                                        JSONObject jsonObject = JSON.parseObject(response);
                                                        if (jsonObject.getString("code").equals("200")) {
                                                            data.get(i).t.setStatus("0");
                                                            adapter.notifyItemChanged(i);
                                                        } else {
                                                            Toast.makeText(AddContactActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                                                        }
                                                        DialogMaker.dismissProgressDialog();

                                                    }
                                                });
                                            } else {
//                                                Uri smsToUri = Uri.parse("smsto:" + "13102008538");
                                                Uri smsToUri = Uri.parse("smsto:" + getContactName(mapPhone, data.get(i).t.getPhone()));
//                                                Log.d("AddContactActivity", getContactName(mapPhone, data.get(i).t.getPhone()));
                                                Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
                                                intent.putExtra("sms_body", "我在使用安信，邀请您也加入，一起沟通工作事宜。http://dh.anxinchat.com/ 安信-安全可信的聊天工具。");
                                                startActivity(intent);
                                            }
                                            break;
                                        case R.id.btn_refuse:
                                            check(data.get(i).t.getFaccid(), "4");
                                            break;
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(AddContactActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            phoneCursor.close();

        }


    }

    //审核
    private void check(String faccid, String type) {
        Map<String, String> map = new HashMap<>();
        map.put("accid", DemoCache.getAccount());
        map.put("faccid", faccid);
        map.put("type", type);
        map.put("msg", "");
        HttpManager.getInstance().post(ContantValue.MAKE_FRIEND, map, new StringCallback() {
            @Override
            public void onBefore(Request request, int id) {
                super.onBefore(request, id);
                DialogMaker.showProgressDialog(AddContactActivity.this, "", true);
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                DialogMaker.dismissProgressDialog();
                Toast.makeText(AddContactActivity.this, "网络有误，添加失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response, int id) {
                DialogMaker.dismissProgressDialog();
                Log.d("AddFriendActivity", response);
                JSONObject jsonObject = JSON.parseObject(response);
                if (jsonObject.getString("code").equals("200")) {
//                   刷新列表
                    addFriend();
                } else {
                    Toast.makeText(AddContactActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void findViews() {
        rvContact = findView(R.id.rv_contact);

    }


    private void query(String account) {
        DialogMaker.showProgressDialog(this, null, false);
//        final String account = searchEdit.getText().toString().toLowerCase();
        // TODO: 2016/11/24  通过昵称或账号添加好友
        Map<String, String> map = new HashMap<>();
        map.put("keyword", account);
        HttpManager.getInstance().post(ContantValue.GET_USER_INFO_INDEX, map, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Toast.makeText(AddContactActivity.this, "网络有误，请稍候重试", Toast.LENGTH_SHORT).show();
                DialogMaker.dismissProgressDialog();
            }

            @Override
            public void onResponse(String response, int id) {
                Log.d("AddFriendActivity555", response);
                JSONObject jsonObject = JSON.parseObject(response);
                DialogMaker.dismissProgressDialog();
                if (jsonObject.getString("code").equals("200")) {
                    UserProfileActivity.start(AddContactActivity.this, jsonObject.getJSONObject("data").getString("accid").toLowerCase());
                } else {
                    Toast.makeText(AddContactActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
