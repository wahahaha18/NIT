package com.aqtx.app.contact.activity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aqtx.app.Contact;
import com.aqtx.app.ContactAdapter;
import com.aqtx.app.ContantValue;
import com.aqtx.app.DemoCache;
import com.aqtx.app.HttpManager;
import com.aqtx.app.R;
import com.aqtx.app.main.reminder.ReminderId;
import com.aqtx.app.main.reminder.ReminderItem;
import com.aqtx.app.main.reminder.ReminderManager;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
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
public class AddFriendMenuActivity extends UI {
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
    private List<Contact> list;

    //---------------------------------------------------//
//    private ClearableEditTextWithIcon searchEdit;
    private ClearableEditTextWithIcon searchEdit;
    private TextView tvPhone;
    private View layoutContact;
    private View layoutQrCode;
    private SearchView mSearchView;
    private SearchView.SearchAutoComplete mEdit;
    //    通讯录列表
//    private RecyclerView rvFriend;

    public static final void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, AddFriendMenuActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSearchView.setQueryHint("手机号/安信号/姓名");
        mSearchView.setIconified(false);
        mSearchView.clearFocus();
//        NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
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
        setContentView(R.layout.add_friend_menu_activity);
        ToolBarOptions options = new ToolBarOptions();
        options.titleId = R.string.add_menu_friend;
        setToolBar(R.id.toolbar, options);
        findViews();
        layoutContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddContactActivity.start(AddFriendMenuActivity.this);
            }
        });
        layoutQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator integrator = new IntentIntegrator(AddFriendMenuActivity.this);
                integrator.setPrompt("扫描二维码,添加安信好友");
                integrator.setOrientationLocked(false);
                integrator.setBarcodeImageEnabled(true);
                integrator.initiateScan();
            }
        });
        tvPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QrCodeActivity.start(AddFriendMenuActivity.this);
            }
        });
        initActionbar();

    }

    private void setSearchView() {
        final float density = getResources().getDisplayMetrics().density;
        mSearchView.setIconified(false);
//        mSearchView.setIconifiedByDefault(false);
        final int closeImgId = getResources().getIdentifier("search_close_btn", "id", getPackageName());
        ImageView closeImg = (ImageView) mSearchView.findViewById(closeImgId);
        if (closeImg != null) {
            LinearLayout.LayoutParams paramsImg = (LinearLayout.LayoutParams) closeImg.getLayoutParams();
            paramsImg.topMargin = (int) (-2 * density);
            closeImg.setVisibility(View.GONE);
            closeImg.setLayoutParams(paramsImg);
        }
        final int editViewId = getResources().getIdentifier("search_src_text", "id", getPackageName());
        mEdit = (SearchView.SearchAutoComplete) mSearchView.findViewById(editViewId);
        if (mEdit != null) {
            mEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        }
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (TextUtils.isEmpty(query)) {
                    Toast.makeText(AddFriendMenuActivity.this, R.string.not_allow_empty, Toast.LENGTH_SHORT).show();
                } else if (query.equals(DemoCache.getAccount())) {
                    Toast.makeText(AddFriendMenuActivity.this, R.string.add_friend_self_tip, Toast.LENGTH_SHORT).show();
                } else {
                    mSearchView.clearFocus();
                    query(query.toLowerCase());
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }


    private void findViews() {
        searchEdit = findView(R.id.search_friend_edit);
        mSearchView = findView(R.id.search);
        setSearchView();
        tvPhone = findView(R.id.tv_phone);
        layoutContact = findView(R.id.layout_contact);
        layoutQrCode = findView(R.id.layout_qrcode);
//        rvFriend = findView(R.id.tv_friend);
        searchEdit.setDeleteImage(R.drawable.nim_grey_delete_icon);
    }

    private void initActionbar() {
        TextView toolbarView = findView(R.id.action_bar_right_clickable_textview);
        toolbarView.setText(R.string.search);
        toolbarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    private void query(String account) {
        DialogMaker.showProgressDialog(this, null, false);
//        final String account = searchEdit.getText().toString().toLowerCase();
        // TODO: 2016/11/24  通过昵称或账号添加好友
        Map<String, String> map = new HashMap<>();
        map.put("keyword", account);
        HttpManager.getInstance().post(ContantValue.GET_USER_INFO, map, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Toast.makeText(AddFriendMenuActivity.this, "网络有误，请稍候重试", Toast.LENGTH_SHORT).show();
                DialogMaker.dismissProgressDialog();
            }

            @Override
            public void onResponse(String response, int id) {
                Log.d("AddFriendActivity", response);
                JSONObject jsonObject = JSON.parseObject(response);
                DialogMaker.dismissProgressDialog();
                if (jsonObject.getString("code").equals("200")) {
                    UserProfileActivity.start(AddFriendMenuActivity.this, jsonObject.getJSONObject("data").getString("accid").toLowerCase());
                } else {
                    Toast.makeText(AddFriendMenuActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Bitmap encodeAsBitmap(String str) {
        Bitmap bitmap = null;
        BitMatrix result = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            result = multiFormatWriter.encode(str, BarcodeFormat.QR_CODE, 200, 200);
            // 使用 ZXing Android Embedded 要写的代码
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.createBitmap(result);
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException iae) { // ?
            return null;
        }
        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
//                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
//                query(result.getContents().split("accid=")[1]);
//                Log.d("AddFriendMenuActivity", result.getContents().split("accid=")[1].trim());
//                Toast.makeText(this, "Scanned: " + result.getContents().split("=")[1], Toast.LENGTH_LONG).show();
                NimUserInfoCache.getInstance().getUserInfoFromRemote(result.getContents().split("accid=")[1], new RequestCallback<NimUserInfo>() {
                    @Override
                    public void onSuccess(NimUserInfo param) {
                        UserProfileActivity.start(AddFriendMenuActivity.this, result.getContents().split("accid=")[1]);
                    }

                    @Override
                    public void onFailed(int code) {
                        Toast.makeText(AddFriendMenuActivity.this, "用户未找到", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onException(Throwable exception) {
//                        Toast.makeText(AddFriendMenuActivity.this, "用户未找到", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
