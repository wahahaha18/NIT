package com.aqtx.app;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aqtx.app.contact.activity.UserProfileActivity;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;

/**
 * 找到我
 * Created by huangjun on 2015/8/11.
 */
public class SetPrivacyActivity extends UI implements CompoundButton.OnCheckedChangeListener {
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

    //---------------------------------------------------//
//    private ClearableEditTextWithIcon searchEdit;
//    private ClearableEditTextWithIcon searchEdit;
//    private TextView tvPhone;
    //    通讯录列表
    private RecyclerView rvContact;
    private SwitchCompat switchCompatAccount,
            switchCompatPhone;


    public static final void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, SetPrivacyActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_privacy);
        TelephonyManager phoneMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        ToolBarOptions options = new ToolBarOptions();
        options.titleId = R.string.privacy;
        setToolBar(R.id.toolbar, options);
        switchCompatAccount = findView(R.id.switch_nim_account);
        switchCompatPhone = findView(R.id.switch_nim_phone);
        switchCompatPhone.setOnCheckedChangeListener(this);
        switchCompatAccount.setOnCheckedChangeListener(this);
        getStatus();
//        findViews();


//        initActionbar();
        // TODO: 2016/12/6 测试添加好友
//        addFriend();
//        ReminderManager.getInstance().registerUnreadNumChangedCallback(new ReminderManager.UnreadNumChangedCallback() {
//            @Override
//            public void onUnreadNumChanged(ReminderItem item) {
//                if (item.getId() != ReminderId.CONTACT) {
//                    return;
//                }
//                addFriend();
//            }
//        });
    }

    //获取联系人
    private void getContact() {
//        OkHttpUtils.get().addParams("accid",DemoCache.getAccount())

    }

    //   获取设置状态
    private void getStatus() {
        OkHttpUtils.get().url(ContantValue.FIND_STATUS).addParams("accid", DemoCache.getAccount()).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                JSONObject jsonObject = JSON.parseObject(response);
                if (jsonObject.getString("code").equals("200")) {
                    String acclinks = jsonObject.getJSONObject("data").getString("acclinks");
                    String links = jsonObject.getJSONObject("data").getString("links");
                    if (!TextUtils.isEmpty(acclinks)) {
                        switchCompatAccount.setOnCheckedChangeListener(null);
                        if ("1".equals(acclinks)) {
                            switchCompatAccount.setChecked(true);
                        } else {
                            switchCompatAccount.setChecked(false);
                        }
                        switchCompatAccount.setOnCheckedChangeListener(SetPrivacyActivity.this);
                    }
                    if (!TextUtils.isEmpty(links)) {
                        switchCompatPhone.setOnCheckedChangeListener(null);
                        if ("1".equals(links)) {
                            switchCompatPhone.setChecked(true);
                        } else {
                            switchCompatPhone.setChecked(false);
                        }
                        switchCompatPhone.setOnCheckedChangeListener(SetPrivacyActivity.this);
                    }
                } else {
                    Toast.makeText(SetPrivacyActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }








    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == switchCompatAccount.getId()) {
            int links = 1;
            if (b) {
                links = 1;
            } else {
                links = 0;
            }
            OkHttpUtils.get().url(ContantValue.SET_FIND_ME).addParams("accid", DemoCache.getAccount())
                    .addParams("type", "1")
                    .addParams("links", String.valueOf(links))
                    .build().execute(new StringCallback() {
                public void onBefore(Request request, int id) {
                    DialogMaker.showProgressDialog(SetPrivacyActivity.this, null, "正在修改", true, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {

                        }
                    }).setCanceledOnTouchOutside(false);
                    super.onBefore(request, id);
                }

                @Override
                public void onError(Call call, Exception e, int id) {
                    Toast.makeText(SetPrivacyActivity.this, "网络有误，请稍后重试", Toast.LENGTH_SHORT)
                            .show();
                    DialogMaker.dismissProgressDialog();
                }

                @Override
                public void onResponse(String response, int id) {
                    Log.d("SetPrivacyActivity", response);
                    JSONObject jsonObject = JSON.parseObject(response);
                    if (jsonObject.getString("code").equals("200")) {
                        getStatus();
                        Toast.makeText(SetPrivacyActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SetPrivacyActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                    }
                    DialogMaker.dismissProgressDialog();
                }
            });
        } else {
            int links = 1;
            if (b) {
                links = 1;
            } else {
                links = 0;
            }
            OkHttpUtils.get().url(ContantValue.SET_FIND_ME).addParams("accid", DemoCache.getAccount())
                    .addParams("type", "2")
                    .addParams("links", String.valueOf(links))
                    .build().execute(new StringCallback() {
                @Override
                public void onBefore(Request request, int id) {
                    DialogMaker.showProgressDialog(SetPrivacyActivity.this, null, "正在修改", true, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {

                        }
                    }).setCanceledOnTouchOutside(false);
                    super.onBefore(request, id);
                }

                @Override
                public void onError(Call call, Exception e, int id) {
                    Toast.makeText(SetPrivacyActivity.this, "网络有误，请稍后重试", Toast.LENGTH_SHORT)
                            .show();
                    DialogMaker.dismissProgressDialog();
                }

                @Override
                public void onResponse(String response, int id) {
                    JSONObject jsonObject = JSON.parseObject(response);
                    if (jsonObject.getString("code").equals("200")) {
                        getStatus();
                        Toast.makeText(SetPrivacyActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SetPrivacyActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                    }
                    DialogMaker.dismissProgressDialog();
                }
            });

        }
    }
}
