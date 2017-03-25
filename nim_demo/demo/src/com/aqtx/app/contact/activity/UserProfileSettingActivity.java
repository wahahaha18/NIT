package com.aqtx.app.contact.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aqtx.app.ContantValue;
import com.aqtx.app.DemoCache;
import com.aqtx.app.HttpManager;
import com.aqtx.app.R;
import com.aqtx.app.main.model.Extras;
import com.aqtx.app.contact.constant.UserConstant;
import com.aqtx.app.contact.helper.UserUpdateHelper;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.media.picker.PickImageHelper;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nim.uikit.session.actions.PickImageAction;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by hzxuwen on 2015/9/14.
 */
public class UserProfileSettingActivity extends UI implements View.OnClickListener {
    private final String TAG = UserProfileSettingActivity.class.getSimpleName();

    // constant
    private static final int PICK_AVATAR_REQUEST = 0x0E;
    private static final int AVATAR_TIME_OUT = 30000;

    private String account;

    // view
    private HeadImageView userHead;
    private RelativeLayout nickLayout;
    private RelativeLayout nimAccountLayout;

    private RelativeLayout genderLayout;
    private RelativeLayout birthLayout;
    private RelativeLayout phoneLayout;
    private RelativeLayout emailLayout;
    private RelativeLayout signatureLayout;

    private TextView nickText;
    private TextView nimAccountText;
    private TextView genderText;
    private TextView birthText;
    private TextView phoneText;
    private TextView emailText;
    private TextView signatureText;

    // data
    AbortableFuture<String> uploadAvatarFuture;
    private NimUserInfo userInfo;

    public static void start(Context context, String account) {
        Intent intent = new Intent();
        intent.setClass(context, UserProfileSettingActivity.class);
        intent.putExtra(Extras.EXTRA_ACCOUNT, account);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.aqtx.app.R.layout.user_profile_set_activity);

        ToolBarOptions options = new ToolBarOptions();
        options.titleId = com.aqtx.app.R.string.user_information;
        setToolBar(com.aqtx.app.R.id.toolbar, options);

        account = getIntent().getStringExtra(Extras.EXTRA_ACCOUNT);
        findViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserInfo();
    }


    private void findViews() {
        userHead = findView(com.aqtx.app.R.id.user_head);
        nickLayout = findView(com.aqtx.app.R.id.nick_layout);
        nimAccountLayout = findView(R.id.nim_account_layout);
        genderLayout = findView(com.aqtx.app.R.id.gender_layout);
        birthLayout = findView(com.aqtx.app.R.id.birth_layout);
        phoneLayout = findView(com.aqtx.app.R.id.phone_layout);
        emailLayout = findView(com.aqtx.app.R.id.email_layout);
        signatureLayout = findView(com.aqtx.app.R.id.signature_layout);
// TODO: 2016/12/5 更改昵称为姓名 1
//        ((TextView) nickLayout.findViewById(com.aqtx.app.R.id.attribute)).setText(com.aqtx.app.R.string.nickname);
        ((TextView) nickLayout.findViewById(com.aqtx.app.R.id.attribute)).setText("姓名");
        ((TextView) nimAccountLayout.findViewById(com.aqtx.app.R.id.attribute)).setText("安信账号");
        ((TextView) genderLayout.findViewById(com.aqtx.app.R.id.attribute)).setText(com.aqtx.app.R.string.gender);
        ((TextView) birthLayout.findViewById(com.aqtx.app.R.id.attribute)).setText(com.aqtx.app.R.string.birthday);
        ((TextView) phoneLayout.findViewById(com.aqtx.app.R.id.attribute)).setText(com.aqtx.app.R.string.phone);
        ((TextView) emailLayout.findViewById(com.aqtx.app.R.id.attribute)).setText(com.aqtx.app.R.string.email);
        ((TextView) signatureLayout.findViewById(com.aqtx.app.R.id.attribute)).setText(com.aqtx.app.R.string.signature);

        nickText = (TextView) nickLayout.findViewById(com.aqtx.app.R.id.value);
        nimAccountText = (TextView) nimAccountLayout.findViewById(com.aqtx.app.R.id.value);
        nimAccountText.setText("未设置");
        genderText = (TextView) genderLayout.findViewById(com.aqtx.app.R.id.value);
        birthText = (TextView) birthLayout.findViewById(com.aqtx.app.R.id.value);
        phoneText = (TextView) phoneLayout.findViewById(com.aqtx.app.R.id.value);
        phoneText.setText("未设置");
        emailText = (TextView) emailLayout.findViewById(com.aqtx.app.R.id.value);
        signatureText = (TextView) signatureLayout.findViewById(com.aqtx.app.R.id.value);

        findViewById(com.aqtx.app.R.id.head_layout).setOnClickListener(this);
        nickLayout.setOnClickListener(this);
        nimAccountLayout.setOnClickListener(this);
        genderLayout.setOnClickListener(this);
        birthLayout.setOnClickListener(this);
        phoneLayout.setOnClickListener(this);
        emailLayout.setOnClickListener(this);
        signatureLayout.setOnClickListener(this);
    }

    private void getUserInfo() {
        userInfo = NimUserInfoCache.getInstance().getUserInfo(account);
        if (userInfo == null) {
            NimUserInfoCache.getInstance().getUserInfoFromRemote(account, new RequestCallback<NimUserInfo>() {
                @Override
                public void onSuccess(NimUserInfo param) {
                    userInfo = param;
                    updateUI();
                }

                @Override
                public void onFailed(int code) {
                    Toast.makeText(UserProfileSettingActivity.this, "getUserInfoFromRemote failed:" + code, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onException(Throwable exception) {
                    Toast.makeText(UserProfileSettingActivity.this, "getUserInfoFromRemote exception:" + exception, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            updateUI();
        }
    }

    private void updateUI() {
        userHead.loadBuddyAvatar(account);
        nickText.setText(TextUtils.isEmpty(userInfo.getName()) ? "" : userInfo.getName());
        nimAccountText.setText(TextUtils.isEmpty(userInfo.getExtension()) ? "未设置" : userInfo.getExtension());
        phoneText.setText(TextUtils.isEmpty(userInfo.getMobile()) ? "未设置" : userInfo.getMobile());
        if (userInfo.getGenderEnum() != null) {
            if (userInfo.getGenderEnum() == GenderEnum.MALE) {
                genderText.setText("男");
            } else if (userInfo.getGenderEnum() == GenderEnum.FEMALE) {
                genderText.setText("女");
            } else {
                genderText.setText("其他");
            }
        }
        if (userInfo.getBirthday() != null) {
            birthText.setText(userInfo.getBirthday());
        }
        if (userInfo.getMobile() != null) {
            phoneText.setText(userInfo.getMobile());
        }
        if (userInfo.getEmail() != null) {
            emailText.setText(userInfo.getEmail());
        }
        if (userInfo.getSignature() != null) {
            signatureText.setText(userInfo.getSignature());
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case com.aqtx.app.R.id.head_layout:
                PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
                option.titleResId = com.aqtx.app.R.string.set_head_image;
                option.crop = true;
                option.multiSelect = false;
                option.cropOutputImageWidth = 720;
                option.cropOutputImageHeight = 720;
                PickImageHelper.pickImage(UserProfileSettingActivity.this, PICK_AVATAR_REQUEST, option);
                break;
            case com.aqtx.app.R.id.nick_layout:
                UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_NICKNAME,
                        nickText.getText().toString());
                break;
            case com.aqtx.app.R.id.gender_layout:
                UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_GENDER,
                        String.valueOf(userInfo.getGenderEnum().getValue()));
                break;
            case com.aqtx.app.R.id.birth_layout:
                UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_BIRTH,
                        birthText.getText().toString());
                break;
            case com.aqtx.app.R.id.phone_layout:
                String text1 = phoneText.getText().toString().trim();
                if ("未设置".equals(text1) || TextUtils.isEmpty(text1.trim())) {
                    UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_PHONE,
                            phoneText.getText().toString());
                } else {
                    Toast.makeText(this, "暂时不支持更换手机号码", Toast.LENGTH_SHORT).show();
                }

                break;
            case com.aqtx.app.R.id.email_layout:
                UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_EMAIL,
                        emailText.getText().toString());
                break;
            case com.aqtx.app.R.id.signature_layout:
                UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_SIGNATURE,
                        signatureText.getText().toString());
                break;
            case R.id.nim_account_layout:
                String text = nimAccountText.getText().toString().trim();
                if ("未设置".equals(text) || TextUtils.isEmpty(text.trim())) {
                    UserProfileEditItemActivity.startActivity(UserProfileSettingActivity.this, UserConstant.KEY_ACCOUNT,
                            nimAccountText.getText().toString());
                } else {
                    Toast.makeText(this, "您已设置过安信号了", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == PICK_AVATAR_REQUEST) {
            String path = data.getStringExtra(com.netease.nim.uikit.session.constant.Extras.EXTRA_FILE_PATH);
            updateAvatar(path);
        }
    }

    /**
     * 更新头像
     */
    private void updateAvatar(final String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        File file = new File(path);
        if (file == null) {
            return;
        }

        DialogMaker.showProgressDialog(this, null, null, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelUpload(com.aqtx.app.R.string.user_info_update_cancel);
            }
        }).setCanceledOnTouchOutside(true);

        LogUtil.i(TAG, "start upload avatar, local file path=" + file.getAbsolutePath());
        new Handler().postDelayed(outimeTask, AVATAR_TIME_OUT);
        uploadAvatarFuture = NIMClient.getService(NosService.class).upload(file, PickImageAction.MIME_JPEG);
        uploadAvatarFuture.setCallback(new RequestCallbackWrapper<String>() {
            @Override
            public void onResult(int code, String url, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS && !TextUtils.isEmpty(url)) {
                    LogUtil.i(TAG, "upload avatar success, url =" + url);

                    UserUpdateHelper.update(UserInfoFieldEnum.AVATAR, url, new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int code, Void result, Throwable exception) {
                            if (code == ResponseCode.RES_SUCCESS) {
                                Map<String, String> map = new HashMap<String, String>();
                                map.put("accid", DemoCache.getAccount());
                                HttpManager.getInstance().post(ContantValue.SAVE_USRE_ICON, map, new StringCallback() {
                                    @Override
                                    public void onError(Call call, Exception e, int id) {

                                    }

                                    @Override
                                    public void onResponse(String response, int id) {

                                    }
                                });
//                                OkHttpUtils.get().addParams("accid", DemoCache.getAccount())
//                                        .url(ContantValue.SAVE_USRE_ICON).build().execute(new StringCallback() {
//                                    @Override
//                                    public void onError(Call call, Exception e, int id) {
//
//                                    }
//
//                                    @Override
//                                    public void onResponse(String response, int id) {
//
//                                    }
//                                });
                                Toast.makeText(UserProfileSettingActivity.this, com.aqtx.app.R.string.head_update_success, Toast.LENGTH_SHORT).show();
                                onUpdateDone();
                            } else {
                                Toast.makeText(UserProfileSettingActivity.this, com.aqtx.app.R.string.head_update_failed, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }); // 更新资料
                } else {
                    Toast.makeText(UserProfileSettingActivity.this, com.aqtx.app.R.string.user_info_update_failed, Toast
                            .LENGTH_SHORT).show();
                    onUpdateDone();
                }
            }
        });
    }

    private void cancelUpload(int resId) {
        if (uploadAvatarFuture != null) {
            uploadAvatarFuture.abort();
            Toast.makeText(UserProfileSettingActivity.this, resId, Toast.LENGTH_SHORT).show();
            onUpdateDone();
        }
    }

    private Runnable outimeTask = new Runnable() {
        @Override
        public void run() {
            cancelUpload(com.aqtx.app.R.string.user_info_update_failed);
        }
    };

    private void onUpdateDone() {
        uploadAvatarFuture = null;
        DialogMaker.dismissProgressDialog();
        getUserInfo();
    }
}
