package com.aqtx.app;

import android.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aqtx.app.config.preference.Preferences;
import com.aqtx.app.config.preference.UserPreferences;
import com.aqtx.app.main.activity.MainActivity;
import com.aqtx.app.main.helper.SoftUpdate;
import com.netease.nim.uikit.cache.DataCacheManager;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nim.uikit.permission.MPermission;
import com.netease.nim.uikit.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.permission.annotation.OnMPermissionGranted;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.ClientType;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;

public class RegisterActivity extends UI implements View.OnKeyListener {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private static final String KICK_OUT = "KICK_OUT";
    private final int BASIC_PERMISSION_REQUEST_CODE = 110;


    private View loginLayout;
    private View registerLayoutPhone;
    private View registerLayoutAccount;
    private View verificationLayout;
    private TextView btnLoginJump;
    private Button btnCommitPhone,//手机号注册
            btnCommitAccount,//账号注册
            btn_next,//下一步
            btnLogin,//登录
            btnRegisterJump, //跳转到注册页面
            btnPhoneJump//      跳转到手机号号注册页面
                    ;
    private EditText etPhone, etName, etPwd, etCode;
    private EditText etAccount, etName1, etPwd1, etPwdConfirm;
    private EditText etUserName, etUserPwd;
    private TextView tvVerificationPhone;
    private TextView tvTimer;
    private TextView btnJumpAccount1;
    //    手机号注册参数
    private String name, phone, pass;
    //    账号注册参数
    private String account, name1, pass1, passConfirm;
    //    登录参数
    private String userName, userPwd;
    private CountDownTimer countDownTimer;
    private TextView btnLoginJumpCopy;


    private AbortableFuture<LoginInfo> loginRequest;
    private boolean registerMode = false; // 注册模式
    private boolean registerPanelInited = false; // 注册面板是否初始化

    public static void start(Context context) {
        start(context, false);
    }

    public static void start(Context context, boolean kickOut) {
        Intent intent = new Intent(context, RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(KICK_OUT, kickOut);
        context.startActivity(intent);
    }

    @Override
    protected boolean displayHomeAsUpEnabled() {
        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Toast.makeText(this, "afdafa", Toast.LENGTH_SHORT).show();

        return true;
    }

    @Override
    public void onBackPressed() {
        if ("使用账号注册".equals(getTitle())) {
            ToolBarOptions options = new ToolBarOptions();
            options.isNeedNavigate = false;
            setTitle("登录");
            setToolBar(R.id.toolbar, options);
            getToolBar().setNavigationIcon(null);
            registerLayoutAccount.setVisibility(View.GONE);
            registerLayoutPhone.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
            verificationLayout.setVisibility(View.GONE);
        } else if ("登录".equals(getTitle())) {
            finish();
        } else if ("使用手机号注册".equals(getTitle())) {
            ToolBarOptions options = new ToolBarOptions();
            options.isNeedNavigate = true;
            setTitle("使用账号注册");
            setToolBar(R.id.toolbar, options);
            registerLayoutAccount.setVisibility(View.VISIBLE);
            registerLayoutPhone.setVisibility(View.GONE);
            loginLayout.setVisibility(View.GONE);
            verificationLayout.setVisibility(View.GONE);
        } else if ("填写验证码".equals(getTitle())) {
            ToolBarOptions options = new ToolBarOptions();
            options.isNeedNavigate = true;
            setTitle("使用手机号注册");
            setToolBar(R.id.toolbar, options);
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            registerLayoutAccount.setVisibility(View.GONE);
            registerLayoutPhone.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);
            verificationLayout.setVisibility(View.GONE);
        }
//        Toast.makeText(this, "getTitle():" + getTitle(), Toast.LENGTH_SHORT).show();

        return;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ToolBarOptions options = new ToolBarOptions();
        options.isNeedNavigate = false;
        setTitle("登录");
        setToolBar(R.id.toolbar, options);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            SoftUpdate update = new SoftUpdate(this);
            update.update();
        } else {
            //没有权限,判断是否会弹权限申请对话框
            boolean shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (shouldShow) {
                //申请权限
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                //被禁止显示弹窗
                //TODO 显示对话框告知用户必须打开权限
                Toast.makeText(this, "您的权限还没被打开，请去往设置打开", Toast.LENGTH_SHORT).show();
            }
        }
        requestBasicPermission();
        //手机号注册
        registerLayoutPhone = findView(R.id.layout_register_phone);
//        账号注册
        registerLayoutAccount = findView(R.id.layout_register_account);
//        验证码
        verificationLayout = findView(R.id.layout_verification);
//        登录
        loginLayout = findView(R.id.layout_login);
        btnLoginJumpCopy = findView(R.id.btn_login_jump_copy);

//        跳转到登录页
        btnLoginJump = findView(R.id.btn_login_jump);
        btnRegisterJump = findView(R.id.btn_account_jump);
//         登录
        btnLogin = findView(R.id.btn_commit_login);
//        登录用户名
        etUserName = findView(R.id.et_login_name);
//        登录密码
        etUserPwd = findView(R.id.et_login_password);
        tvTimer = findView(R.id.tv_timen);

        btnCommitPhone = findView(R.id.btn_commit);
//        跳转到账号注册
        btnJumpAccount1 = findView(R.id.btn_account1_jump);

        btnCommitAccount = findView(R.id.btn_commit_account);
        btnPhoneJump = findView(R.id.btn_phone_jump);
        etName = findView(R.id.et_register_name);
        etPhone = findView(R.id.et_register_phone);
        etPwd = findView(R.id.et_register_password);
        etCode = findView(R.id.et_code);
        btn_next = findView(R.id.btn_next);

        etAccount = findView(R.id.et_register_account_account);
        etName1 = findView(R.id.et_register_name_account);
        etPwd1 = findView(R.id.et_register_password_account);
        etPwdConfirm = findView(R.id.et_register_password_confirm_account);
        tvVerificationPhone = findView(R.id.tv_verification_phone);
        String userAccount = Preferences.getUserName();
        etUserName.setText(userAccount);
        btnLoginJumpCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToolBarOptions options = new ToolBarOptions();
                options.isNeedNavigate = false;
                setTitle("登录");
                setToolBar(R.id.toolbar, options);
                getToolBar().setNavigationIcon(null);
                registerLayoutAccount.setVisibility(View.GONE);
                registerLayoutPhone.setVisibility(View.GONE);
                loginLayout.setVisibility(View.VISIBLE);
                verificationLayout.setVisibility(View.GONE);
            }
        });
        btnJumpAccount1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToolBarOptions options = new ToolBarOptions();
                options.isNeedNavigate = true;
                setTitle("使用账号注册");
                setToolBar(R.id.toolbar, options);
                registerLayoutAccount.setVisibility(View.VISIBLE);
                registerLayoutPhone.setVisibility(View.GONE);
                loginLayout.setVisibility(View.GONE);
                verificationLayout.setVisibility(View.GONE);
            }
        });
        btnLoginJump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToolBarOptions options = new ToolBarOptions();
                options.isNeedNavigate = false;
                setTitle("登录");
                setToolBar(R.id.toolbar, options);
                getToolBar().setNavigationIcon(null);
                registerLayoutAccount.setVisibility(View.GONE);
                registerLayoutPhone.setVisibility(View.GONE);
                loginLayout.setVisibility(View.VISIBLE);
                verificationLayout.setVisibility(View.GONE);
            }
        });
//        跳转到手机号注册
        btnPhoneJump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToolBarOptions options = new ToolBarOptions();
                options.isNeedNavigate = true;
                setTitle("使用手机号注册");
                setToolBar(R.id.toolbar, options);
                registerLayoutAccount.setVisibility(View.GONE);
                registerLayoutPhone.setVisibility(View.VISIBLE);
                loginLayout.setVisibility(View.GONE);
                verificationLayout.setVisibility(View.GONE);
            }
        });
//        跳转到账号注册


        btnRegisterJump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToolBarOptions options = new ToolBarOptions();
                options.isNeedNavigate = true;
                setTitle("使用手机号注册");
                setToolBar(R.id.toolbar, options);
                registerLayoutAccount.setVisibility(View.GONE);
                registerLayoutPhone.setVisibility(View.VISIBLE);
                loginLayout.setVisibility(View.GONE);
                verificationLayout.setVisibility(View.GONE);
            }
        });
//        d登录
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userName = etUserName.getText().toString().trim();
                userPwd = etUserPwd.getText().toString().trim();
                if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(userPwd)) {
                    Toast.makeText(RegisterActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    login2(userName, userPwd);
                }

            }
        });
        //下一步
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String code = etCode.getText().toString().trim();
                if (TextUtils.isEmpty(code)) {
                    Toast.makeText(RegisterActivity.this, "请填写正确的验证码", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("mobile", phone);
                    map.put("name", name);
                    map.put("token", pass);
                    map.put("code", code);
                    HttpManager.getInstance().post(ContantValue.REGISTER_PHONE, map, new StringCallback() {
                        @Override
                        public void onBefore(Request request, int id) {
                            super.onBefore(request, id);
                            DialogMaker.showProgressDialog(RegisterActivity.this, null, "正在提交", true, new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    DialogMaker.dismissProgressDialog();
                                }
                            }).setCanceledOnTouchOutside(false);
                        }

                        @Override
                        public void onError(Call call, Exception e, int id) {
                            Toast.makeText(RegisterActivity.this, "网络有误，请稍后重试", Toast.LENGTH_SHORT)
                                    .show();
                            DialogMaker.dismissProgressDialog();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, response);
                            JSONObject jsonObject = JSON.parseObject(response);
                            DialogMaker.dismissProgressDialog();
                            if (jsonObject.getString("code").equals("200")) {
                                if (countDownTimer != null) {
                                    countDownTimer.cancel();
                                }
                                Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                                etUserName.setText(phone);
//                                etUserPwd.setText(userPwd);
//                                ToolBarOptions options = new ToolBarOptions();
//                                options.isNeedNavigate = false;
//                                setTitle("登录");
//                                setToolBar(R.id.toolbar, options);
//                                getToolBar().setNavigationIcon(null);
//                                registerLayoutAccount.setVisibility(View.GONE);
//                                registerLayoutPhone.setVisibility(View.GONE);
//                                loginLayout.setVisibility(View.VISIBLE);
//                                verificationLayout.setVisibility(View.GONE);
                                login1(phone, pass);
                            } else {
                                Toast.makeText(RegisterActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
//                    OkHttpUtils.get().url(ContantValue.REGISTER_PHONE)
//                            .addParams("mobile", phone)
//                            .addParams("name", name)
//                            .addParams("token", pass)
//                            .addParams("code", code)
//                            .build().execute(new StringCallback() {
//
//                        @Override
//                        public void onBefore(Request request, int id) {
//                            super.onBefore(request, id);
//                            DialogMaker.showProgressDialog(RegisterActivity.this, null, "正在提交", true, new DialogInterface.OnCancelListener() {
//                                @Override
//                                public void onCancel(DialogInterface dialog) {
//                                    DialogMaker.dismissProgressDialog();
//                                }
//                            }).setCanceledOnTouchOutside(false);
//                        }
//
//                        @Override
//                        public void onError(Call call, Exception e, int id) {
//                            Toast.makeText(RegisterActivity.this, "网络有误，请稍后重试", Toast.LENGTH_SHORT)
//                                    .show();
//                            DialogMaker.dismissProgressDialog();
//                        }
//
//                        @Override
//                        public void onResponse(String response, int id) {
//                            Log.d(TAG, response);
//                            JSONObject jsonObject = JSON.parseObject(response);
//                            DialogMaker.dismissProgressDialog();
//                            if (jsonObject.getString("code").equals("200")) {
//                                if (countDownTimer != null) {
//                                    countDownTimer.cancel();
//                                }
//                                Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
//                                etUserName.setText(phone);
////                                etUserPwd.setText(userPwd);
////                                ToolBarOptions options = new ToolBarOptions();
////                                options.isNeedNavigate = false;
////                                setTitle("登录");
////                                setToolBar(R.id.toolbar, options);
////                                getToolBar().setNavigationIcon(null);
////                                registerLayoutAccount.setVisibility(View.GONE);
////                                registerLayoutPhone.setVisibility(View.GONE);
////                                loginLayout.setVisibility(View.VISIBLE);
////                                verificationLayout.setVisibility(View.GONE);
//                                login1(phone, pass);
//                            } else {
//                                Toast.makeText(RegisterActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
                }
            }
        });
//        账号注册
        btnCommitAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                account = etAccount.getText().toString().trim();
                name1 = etName1.getText().toString().trim();
                pass1 = etPwd1.getText().toString().trim();
                passConfirm = etPwdConfirm.getText().toString().trim();
                String nickRegex = "^[-_\u4E00-\u9FA5]{1,10}$";
                String passRegex = "^[0-9A-Za-z\\\\S]{6,}$";
                String accountRegex = "^[a-zA-Z]{1,20}$";
                if (!account.matches(accountRegex)) {
                    Toast.makeText(RegisterActivity.this, "账号限20位字母", Toast.LENGTH_SHORT).show();
                } else if (!name1.matches(nickRegex)) {
                    Toast.makeText(RegisterActivity.this, "姓名为中文", Toast.LENGTH_SHORT).show();
                } else if (!pass1.matches(passRegex)) {
                    Toast.makeText(RegisterActivity.this, "密码至少为6位字母或者数字", Toast.LENGTH_SHORT).show();
                } else if (!pass1.equals(passConfirm)) {
                    Toast.makeText(RegisterActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("access", account);
                    map.put("name", name1);
                    map.put("token", pass1);
                    HttpManager.getInstance().post(ContantValue.REGISTER_ACCOUNT, map, new StringCallback() {
                        @Override
                        public void onBefore(Request request, int id) {
                            super.onBefore(request, id);
                            DialogMaker.showProgressDialog(RegisterActivity.this, null, "正在注册", true, new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    DialogMaker.dismissProgressDialog();
                                }
                            }).setCanceledOnTouchOutside(false);
                        }

                        @Override
                        public void onError(Call call, Exception e, int id) {
                            Toast.makeText(RegisterActivity.this, "网络有误，请稍后重试", Toast.LENGTH_SHORT)
                                    .show();
                            DialogMaker.dismissProgressDialog();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d(TAG, response);
                            JSONObject jsonObject = JSON.parseObject(response);
                            if (jsonObject.getString("code").equals("200")) {
                                DialogMaker.dismissProgressDialog();
                                Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                                etUserName.setText(account);
//                                etUserPwd.setText(userPwd);
//                                ToolBarOptions options = new ToolBarOptions();
//                                options.isNeedNavigate = false;
//                                setTitle("登录");
//                                setToolBar(R.id.toolbar, options);
//                                getToolBar().setNavigationIcon(null);
//                                registerLayoutAccount.setVisibility(View.GONE);
//                                registerLayoutPhone.setVisibility(View.GONE);
//                                loginLayout.setVisibility(View.VISIBLE);
//                                verificationLayout.setVisibility(View.GONE);
                                login1(account, pass1);
                            } else {
                                DialogMaker.dismissProgressDialog();
                                Toast.makeText(RegisterActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
//                    OkHttpUtils.get().url(ContantValue.REGISTER_ACCOUNT)
//                            .addParams("access", account)
//                            .addParams("name", name1)
//                            .addParams("token", pass1)
//                            .build().execute(new StringCallback() {
//                        @Override
//                        public void onBefore(Request request, int id) {
//                            super.onBefore(request, id);
//                            DialogMaker.showProgressDialog(RegisterActivity.this, null, "正在注册", true, new DialogInterface.OnCancelListener() {
//                                @Override
//                                public void onCancel(DialogInterface dialog) {
//                                    DialogMaker.dismissProgressDialog();
//                                }
//                            }).setCanceledOnTouchOutside(false);
//                        }
//
//                        @Override
//                        public void onError(Call call, Exception e, int id) {
//                            Toast.makeText(RegisterActivity.this, "网络有误，请稍后重试", Toast.LENGTH_SHORT)
//                                    .show();
//                            DialogMaker.dismissProgressDialog();
//                        }
//
//                        @Override
//                        public void onResponse(String response, int id) {
//                            Log.d(TAG, response);
//                            JSONObject jsonObject = JSON.parseObject(response);
//                            if (jsonObject.getString("code").equals("200")) {
//                                DialogMaker.dismissProgressDialog();
//                                Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
//                                etUserName.setText(account);
////                                etUserPwd.setText(userPwd);
////                                ToolBarOptions options = new ToolBarOptions();
////                                options.isNeedNavigate = false;
////                                setTitle("登录");
////                                setToolBar(R.id.toolbar, options);
////                                getToolBar().setNavigationIcon(null);
////                                registerLayoutAccount.setVisibility(View.GONE);
////                                registerLayoutPhone.setVisibility(View.GONE);
////                                loginLayout.setVisibility(View.VISIBLE);
////                                verificationLayout.setVisibility(View.GONE);
//                                login1(account, pass1);
//                            } else {
//                                DialogMaker.dismissProgressDialog();
//                                Toast.makeText(RegisterActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
                }
            }
        });
//        手机号注册
        btnCommitPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = etName.getText().toString().trim();
                phone = etPhone.getText().toString().trim();
                pass = etPwd.getText().toString().trim();
                String nickRegex = "^[-_\u4E00-\u9FA5]{1,10}$";
                String phoneRegex = "[1][34578]\\d{9}";
                String passRegex = "^[0-9A-Za-z\\\\S]{6,}$";
                if (!name.matches(nickRegex)) {
                    Toast.makeText(RegisterActivity.this, "名字为中文", Toast.LENGTH_SHORT).show();
                } else if (!phone.matches(phoneRegex)) {
                    Toast.makeText(RegisterActivity.this, "手机号不正确", Toast.LENGTH_SHORT).show();
                } else if (!pass.matches(passRegex)) {
                    Toast.makeText(RegisterActivity.this, "密码至少为6位字母或者数字", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("mobile", phone);
                    map.put("name", name);
                    HttpManager.getInstance().post(ContantValue.GET_VERIFICATION, map, new StringCallback() {
                        @Override
                        public void onBefore(Request request, int id) {
                            super.onBefore(request, id);
                            DialogMaker.showProgressDialog(RegisterActivity.this, null, "正在提交", true, new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    DialogMaker.dismissProgressDialog();
                                }
                            }).setCanceledOnTouchOutside(false);
                        }

                        @Override
                        public void onError(Call call, Exception e, int id) {
                            Toast.makeText(RegisterActivity.this, "网络有误，请稍后重试", Toast.LENGTH_SHORT)
                                    .show();
                            DialogMaker.dismissProgressDialog();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            final JSONObject jsonObject = JSON.parseObject(response);
                            if (jsonObject.getString("code").equals("200")) {
                                countDownTimer = new CountDownTimer(60000, 1000) {
                                    @Override
                                    public void onTick(long l) {
                                        tvTimer.setText("接收短信大约需要" + l / 1000 + "秒钟");
                                    }

                                    @Override
                                    public void onFinish() {
                                        OkHttpUtils.get().addParams("mobile", phone)
                                                .addParams("name", name)
                                                .url(ContantValue.GET_VERIFICATION).build().execute(new StringCallback() {
                                            @Override
                                            public void onError(Call call, Exception e, int id) {

                                            }

                                            @Override
                                            public void onResponse(String response, int id) {
                                                JSONObject jsonObject1 = JSON.parseObject(response);
                                                if (jsonObject1.getString("code").equals("200")) {
                                                    Toast.makeText(RegisterActivity.this, "验证码已重发", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                };
                                countDownTimer.start();
                                registerLayoutPhone.setVisibility(View.GONE);
                                verificationLayout.setVisibility(View.VISIBLE);
                                setTitle("填写验证码");
                                tvVerificationPhone.setText("+86 " + phone);
                            } else {

                                Toast.makeText(RegisterActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                            }
                            DialogMaker.dismissProgressDialog();
                        }
                    });
//                    OkHttpUtils.get().addParams("mobile", phone)
//                            .addParams("name", name)
//                            .url(ContantValue.GET_VERIFICATION).build().execute(new StringCallback() {
//                        @Override
//                        public void onBefore(Request request, int id) {
//                            super.onBefore(request, id);
//                            DialogMaker.showProgressDialog(RegisterActivity.this, null, "正在提交", true, new DialogInterface.OnCancelListener() {
//                                @Override
//                                public void onCancel(DialogInterface dialog) {
//                                    DialogMaker.dismissProgressDialog();
//                                }
//                            }).setCanceledOnTouchOutside(false);
//                        }
//
//                        @Override
//                        public void onError(Call call, Exception e, int id) {
//                            Toast.makeText(RegisterActivity.this, "网络有误，请稍后重试", Toast.LENGTH_SHORT)
//                                    .show();
//                            DialogMaker.dismissProgressDialog();
//                        }
//
//                        @Override
//                        public void onResponse(String response, int id) {
//                            final JSONObject jsonObject = JSON.parseObject(response);
//                            if (jsonObject.getString("code").equals("200")) {
//                                countDownTimer = new CountDownTimer(60000, 1000) {
//                                    @Override
//                                    public void onTick(long l) {
//                                        tvTimer.setText("接收短信大约需要" + l / 1000 + "秒钟");
//                                    }
//
//                                    @Override
//                                    public void onFinish() {
//                                        OkHttpUtils.get().addParams("mobile", phone)
//                                                .addParams("name", name)
//                                                .url(ContantValue.GET_VERIFICATION).build().execute(new StringCallback() {
//                                            @Override
//                                            public void onError(Call call, Exception e, int id) {
//
//                                            }
//
//                                            @Override
//                                            public void onResponse(String response, int id) {
//                                                JSONObject jsonObject1 = JSON.parseObject(response);
//                                                if (jsonObject1.getString("code").equals("200")) {
//                                                    Toast.makeText(RegisterActivity.this, "验证码已重发", Toast.LENGTH_SHORT).show();
//                                                }
//                                            }
//                                        });
//                                    }
//                                };
//                                countDownTimer.start();
//                                registerLayoutPhone.setVisibility(View.GONE);
//                                verificationLayout.setVisibility(View.VISIBLE);
//                                setTitle("填写验证码");
//                                tvVerificationPhone.setText("+86 " + phone);
//                            } else {
//
//                                Toast.makeText(RegisterActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
//                            }
//                            DialogMaker.dismissProgressDialog();
//                        }
//                    });
                }


            }
        });

        onParseIntent();

    }

    private void login2(final String userName, final String userPwd) {
        Map<String, String> map = new HashMap<>();
        map.put("user", userName);
        HttpManager.getInstance().post(ContantValue.LOGIN, map, new StringCallback() {
            @Override
            public void onBefore(Request request, int id) {
                super.onBefore(request, id);
                DialogMaker.showProgressDialog(RegisterActivity.this, null, getString(R.string.logining), true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (loginRequest != null) {
                            loginRequest.abort();
                            onLoginDone();
                        }
                    }
                }).setCanceledOnTouchOutside(false);
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                Toast.makeText(RegisterActivity.this, "网络有误，请稍后重试", Toast.LENGTH_SHORT)
                        .show();
                onLoginDone();
            }

            @Override
            public void onResponse(String response, int id) {
                Log.d(TAG, response);
                JSONObject jsonObject = JSON.parseObject(response);
                if (jsonObject.getString("code").equals("200")) {
                    final String accid = jsonObject.getJSONObject("data").getString("accid");
                    Log.d(TAG, "accid:" + accid + "pwd:" + userPwd);
                    // 登录
                    loginRequest = NIMClient.getService(AuthService.class).login(new LoginInfo(accid, userPwd));
                    loginRequest.setCallback(new RequestCallback<LoginInfo>() {
                        @Override
                        public void onSuccess(LoginInfo param) {
                            LogUtil.i(TAG, "login success");
                            onLoginDone();
                            DemoCache.setAccount(accid);
                            saveLoginInfo(accid, userPwd, userName);

                            // 初始化消息提醒
                            NIMClient.toggleNotification(UserPreferences.getNotificationToggle());

                            // 初始化免打扰
                            if (UserPreferences.getStatusConfig() == null) {
                                UserPreferences.setStatusConfig(DemoCache.getNotificationConfig());
                            }
                            NIMClient.updateStatusBarNotificationConfig(UserPreferences.getStatusConfig());

                            // 构建缓存
                            DataCacheManager.buildDataCacheAsync();
                            // 进入主界面
                            MainActivity.start(RegisterActivity.this, null);
                            finish();
                        }

                        @Override
                        public void onFailed(int code) {
                            Log.d(TAG, "code:" + code);
                            onLoginDone();
                            if (code == 302 || code == 404) {
                                Toast.makeText(RegisterActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "登录失败: " + code, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onException(Throwable exception) {
                            Log.d(TAG, "异常");
                            Toast.makeText(RegisterActivity.this, R.string.login_exception, Toast.LENGTH_LONG).show();
                            onLoginDone();
                        }
                    });

                } else {
                    onLoginDone();
                    Toast.makeText(RegisterActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                }
            }
        });
//        OkHttpUtils.get().addParams("user", userName).url(ContantValue.LOGIN).build().execute(new StringCallback() {
//            @Override
//            public void onBefore(Request request, int id) {
//                super.onBefore(request, id);
//                DialogMaker.showProgressDialog(RegisterActivity.this, null, getString(R.string.logining), true, new DialogInterface.OnCancelListener() {
//                    @Override
//                    public void onCancel(DialogInterface dialog) {
//                        if (loginRequest != null) {
//                            loginRequest.abort();
//                            onLoginDone();
//                        }
//                    }
//                }).setCanceledOnTouchOutside(false);
//            }
//
//            @Override
//            public void onError(Call call, Exception e, int id) {
//                Toast.makeText(RegisterActivity.this, "网络有误，请稍后重试", Toast.LENGTH_SHORT)
//                        .show();
//                onLoginDone();
//            }
//
//            @Override
//            public void onResponse(String response, int id) {
//                Log.d(TAG, response);
//                JSONObject jsonObject = JSON.parseObject(response);
//                if (jsonObject.getString("code").equals("200")) {
//                    final String accid = jsonObject.getJSONObject("data").getString("accid");
//                    Log.d(TAG, "accid:" + accid + "pwd:" + userPwd);
//                    // 登录
//                    loginRequest = NIMClient.getService(AuthService.class).login(new LoginInfo(accid, userPwd));
//                    loginRequest.setCallback(new RequestCallback<LoginInfo>() {
//                        @Override
//                        public void onSuccess(LoginInfo param) {
//                            LogUtil.i(TAG, "login success");
//                            onLoginDone();
//                            DemoCache.setAccount(accid);
//                            saveLoginInfo(accid, userPwd, userName);
//
//                            // 初始化消息提醒
//                            NIMClient.toggleNotification(UserPreferences.getNotificationToggle());
//
//                            // 初始化免打扰
//                            if (UserPreferences.getStatusConfig() == null) {
//                                UserPreferences.setStatusConfig(DemoCache.getNotificationConfig());
//                            }
//                            NIMClient.updateStatusBarNotificationConfig(UserPreferences.getStatusConfig());
//
//                            // 构建缓存
//                            DataCacheManager.buildDataCacheAsync();
//                            // 进入主界面
//                            MainActivity.start(RegisterActivity.this, null);
//                            finish();
//                        }
//
//                        @Override
//                        public void onFailed(int code) {
//                            onLoginDone();
//                            if (code == 302 || code == 404) {
//                                Toast.makeText(RegisterActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
//                            } else {
//                                Toast.makeText(RegisterActivity.this, "登录失败: " + code, Toast.LENGTH_SHORT).show();
//                            }
//                        }
//
//                        @Override
//                        public void onException(Throwable exception) {
//                            Toast.makeText(RegisterActivity.this, R.string.login_exception, Toast.LENGTH_LONG).show();
//                            onLoginDone();
//                        }
//                    });
//
//                } else {
//                    onLoginDone();
//                    Toast.makeText(RegisterActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

    private void login1(final String userName, final String userPwd) {
        Map<String, String> map = new HashMap<>();
        map.put("user", userName);
        HttpManager.getInstance().post(ContantValue.LOGIN, map, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Toast.makeText(RegisterActivity.this, "网络有误，请稍后重试", Toast.LENGTH_SHORT)
                        .show();

            }

            @Override
            public void onResponse(String response, int id) {
                Log.d(TAG, response);
                JSONObject jsonObject = JSON.parseObject(response);
                if (jsonObject.getString("code").equals("200")) {
                    final String accid = jsonObject.getJSONObject("data").getString("accid");
                    Log.d(TAG, "accid:" + accid + "pwd:" + userPwd);
                    // 登录
                    loginRequest = NIMClient.getService(AuthService.class).login(new LoginInfo(accid, userPwd));
                    loginRequest.setCallback(new RequestCallback<LoginInfo>() {
                        @Override
                        public void onSuccess(LoginInfo param) {
                            LogUtil.i(TAG, "login success");

                            loginRequest = null;
                            DemoCache.setAccount(accid);
                            saveLoginInfo(accid, userPwd, userName);

                            // 初始化消息提醒
                            NIMClient.toggleNotification(UserPreferences.getNotificationToggle());

                            // 初始化免打扰
                            if (UserPreferences.getStatusConfig() == null) {
                                UserPreferences.setStatusConfig(DemoCache.getNotificationConfig());
                            }
                            NIMClient.updateStatusBarNotificationConfig(UserPreferences.getStatusConfig());

                            // 构建缓存
                            DataCacheManager.buildDataCacheAsync();
                            // 进入主界面
                            MainActivity.start(RegisterActivity.this, null);
                            finish();
                        }

                        @Override
                        public void onFailed(int code) {
                            onLoginDone();
                            if (code == 302 || code == 404) {
                                Toast.makeText(RegisterActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "登录失败: " + code, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onException(Throwable exception) {
                            Toast.makeText(RegisterActivity.this, R.string.login_exception, Toast.LENGTH_LONG).show();
                            loginRequest = null;
                        }
                    });

                } else {
                    loginRequest = null;
                    Toast.makeText(RegisterActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                }
            }
        });
//        OkHttpUtils.get().addParams("user", userName).url(ContantValue.LOGIN).build().execute(new StringCallback() {
//
//
//            @Override
//            public void onError(Call call, Exception e, int id) {
//                Toast.makeText(RegisterActivity.this, "网络有误，请稍后重试", Toast.LENGTH_SHORT)
//                        .show();
//
//            }
//
//            @Override
//            public void onResponse(String response, int id) {
//                Log.d(TAG, response);
//                JSONObject jsonObject = JSON.parseObject(response);
//                if (jsonObject.getString("code").equals("200")) {
//                    final String accid = jsonObject.getJSONObject("data").getString("accid");
//                    Log.d(TAG, "accid:" + accid + "pwd:" + userPwd);
//                    // 登录
//                    loginRequest = NIMClient.getService(AuthService.class).login(new LoginInfo(accid, userPwd));
//                    loginRequest.setCallback(new RequestCallback<LoginInfo>() {
//                        @Override
//                        public void onSuccess(LoginInfo param) {
//                            LogUtil.i(TAG, "login success");
//
//                            loginRequest = null;
//                            DemoCache.setAccount(accid);
//                            saveLoginInfo(accid, userPwd, userName);
//
//                            // 初始化消息提醒
//                            NIMClient.toggleNotification(UserPreferences.getNotificationToggle());
//
//                            // 初始化免打扰
//                            if (UserPreferences.getStatusConfig() == null) {
//                                UserPreferences.setStatusConfig(DemoCache.getNotificationConfig());
//                            }
//                            NIMClient.updateStatusBarNotificationConfig(UserPreferences.getStatusConfig());
//
//                            // 构建缓存
//                            DataCacheManager.buildDataCacheAsync();
//                            // 进入主界面
//                            MainActivity.start(RegisterActivity.this, null);
//                            finish();
//                        }
//
//                        @Override
//                        public void onFailed(int code) {
//                            onLoginDone();
//                            if (code == 302 || code == 404) {
//                                Toast.makeText(RegisterActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
//                            } else {
//                                Toast.makeText(RegisterActivity.this, "登录失败: " + code, Toast.LENGTH_SHORT).show();
//                            }
//                        }
//
//                        @Override
//                        public void onException(Throwable exception) {
//                            Toast.makeText(RegisterActivity.this, R.string.login_exception, Toast.LENGTH_LONG).show();
//                            loginRequest = null;
//                        }
//                    });
//
//                } else {
//                    loginRequest = null;
//                    Toast.makeText(RegisterActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

    /**
     * 基本权限管理
     */
    private void requestBasicPermission() {
        MPermission.with(RegisterActivity.this)
                .addRequestCode(BASIC_PERMISSION_REQUEST_CODE)
                .permissions(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .request();
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
//    }

    @OnMPermissionGranted(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionSuccess() {
//        Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
    }

    @OnMPermissionDenied(BASIC_PERMISSION_REQUEST_CODE)
    public void onBasicPermissionFailed() {
        Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
    }

    private void onParseIntent() {
        if (getIntent().getBooleanExtra(KICK_OUT, false)) {
            int type = NIMClient.getService(AuthService.class).getKickedClientType();
            String client;
            switch (type) {
                case ClientType.Web:
                    client = "网页端";
                    break;
                case ClientType.Windows:
                    client = "电脑端";
                    break;
                case ClientType.REST:
                    client = "服务端";
                    break;
                default:
                    client = "移动端";
                    break;
            }
            EasyAlertDialogHelper.showOneButtonDiolag(RegisterActivity.this, getString(R.string.kickout_notify),
                    String.format(getString(R.string.kickout_content), client), getString(R.string.ok), true, null);
        }
    }


    private void onLoginDone() {
        loginRequest = null;
        DialogMaker.dismissProgressDialog();
    }

    private void saveLoginInfo(final String account, final String token, String name) {
        Preferences.saveUserAccount(account);
        Preferences.saveUserToken(token);
        Preferences.saveUserName(name);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (!Preferences.isUpdate()) {
//            SoftUpdate update = new SoftUpdate(this);
//            update.update();
//        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted
                SoftUpdate update = new SoftUpdate(this);
                update.update();
                //TODO 向SD卡写数据
            } else {
                //permission denied
                //TODO 显示对话框告知用户必须打开权限
            }
        }
    }


}
