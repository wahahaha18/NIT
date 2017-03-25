package com.aqtx.app.contact.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aqtx.app.ContantValue;
import com.aqtx.app.DemoCache;
import com.aqtx.app.HttpManager;
import com.aqtx.app.R;
import com.aqtx.app.RegisterActivity;
import com.aqtx.app.contact.constant.UserConstant;
import com.aqtx.app.contact.helper.UserUpdateHelper;
import com.netease.nim.uikit.cache.FriendDataCache;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.constant.FriendFieldEnum;
import com.netease.nimlib.sdk.friend.model.Friend;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by hzxuwen on 2015/9/14.
 */
public class UserProfileEditItemActivity extends UI implements View.OnClickListener {

    private static final String EXTRA_KEY = "EXTRA_KEY";
    private static final String EXTRA_DATA = "EXTRA_DATA";
    public static final int REQUEST_CODE = 1000;

    // data
    private int key;
    private String data;
    private int birthYear = 1990;
    private int birthMonth = 10;
    private int birthDay = 10;
    private Map<Integer, UserInfoFieldEnum> fieldMap;

    // VIEW
    private ClearableEditTextWithIcon editText;
    private TextView tvMsg;
    private CheckBox cbPhone;
    //    private TextView toolbarView;
    private int links;
    private LinearLayout layoutSuccess;
    private LinearLayout layoutShow;

    // gender layout
    private RelativeLayout maleLayout;
    private RelativeLayout femaleLayout;
    private RelativeLayout otherLayout;
    private ImageView maleCheck;
    private ImageView femaleCheck;
    private ImageView otherCheck;
    private Button btnCommit;
    private TextView tvHead;
    private View layoutVerification;
    private EditText etCode;
    private TextView tvPhone;
    private TextView tvTimer;
    CountDownTimer countDownTimer;
    private TextView tvBindPhone;
    //是否开启验证手机号
    private boolean isOpen = true;

    // birth layout
    private RelativeLayout birthPickerLayout;
    private TextView birthText;
    private int gender;

    public static final void startActivity(Context context, int key, String data) {
        Intent intent = new Intent();
        intent.setClass(context, UserProfileEditItemActivity.class);
        intent.putExtra(EXTRA_KEY, key);
        intent.putExtra(EXTRA_DATA, data);
        ((Activity) context).startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
        if (key == UserConstant.KEY_NICKNAME || key == UserConstant.KEY_PHONE || key == UserConstant.KEY_EMAIL
                || key == UserConstant.KEY_SIGNATURE || key == UserConstant.KEY_ALIAS || key == UserConstant.KEY_ACCOUNT) {
            setContentView(com.aqtx.app.R.layout.user_profile_edittext_layout);
            findEditText();
        } else if (key == UserConstant.KEY_GENDER) {
            setContentView(com.aqtx.app.R.layout.user_profile_gender_layout);
            findGenderViews();
        } else if (key == UserConstant.KEY_BIRTH) {
            setContentView(com.aqtx.app.R.layout.user_profile_birth_layout);
            findBirthViews();
        }
        ToolBarOptions options = new ToolBarOptions();
        setToolBar(com.aqtx.app.R.id.toolbar, options);
        btnCommit = findView(R.id.btn_commit_click);
        initActionbar();
        setTitles();
    }

    @Override
    public void onBackPressed() {
        showKeyboard(false);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onBackPressed();
    }

    private void parseIntent() {
        key = getIntent().getIntExtra(EXTRA_KEY, -1);
        data = getIntent().getStringExtra(EXTRA_DATA);
    }

    private void setTitles() {
        switch (key) {
            case UserConstant.KEY_NICKNAME:
                // TODO: 2016/12/5 修改昵称为姓名2
//                setTitle(com.aqtx.app.R.string.nickname);
                setTitle("姓名");
                tvHead.setText("姓名");
                break;
            case UserConstant.KEY_PHONE:
                setTitle("验证手机号");
                tvHead.setText("手机号");
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case UserConstant.KEY_EMAIL:
                setTitle(com.aqtx.app.R.string.email);
                tvHead.setText("邮箱");
                break;
            case UserConstant.KEY_SIGNATURE:
                setTitle(com.aqtx.app.R.string.signature);
                tvHead.setText("签名");
                break;
            case UserConstant.KEY_GENDER:
                setTitle(com.aqtx.app.R.string.gender);

                break;
            case UserConstant.KEY_BIRTH:
                setTitle(com.aqtx.app.R.string.birthday);
                break;
            case UserConstant.KEY_ALIAS:
                setTitle(com.aqtx.app.R.string.alias);
                tvHead.setText("备注");
                break;
            case UserConstant.KEY_ACCOUNT:
                setTitle("设置安信号");
                tvHead.setText("安信号");
                break;
        }
    }

    private void findEditText() {
        tvHead = findView(R.id.tv_head);
        tvBindPhone = findView(R.id.tv_bind_phone);
        tvTimer = findView(R.id.tv_count_timer);
        tvPhone = findView(R.id.tv_verification_phone);
        etCode = findView(R.id.et_code);
        layoutVerification = findView(R.id.layout_profile_verification);
        cbPhone = findView(R.id.cb_nim_phone);
        layoutSuccess = findView(R.id.layout_success);
        layoutShow = findView(R.id.layoutShow);
        editText = findView(com.aqtx.app.R.id.edittext);
        tvMsg = findView(R.id.tv_nim_msg);
        cbPhone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isOpen = b;
            }
        });
        if (key == UserConstant.KEY_NICKNAME) {
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        } else if (key == UserConstant.KEY_PHONE) {
            cbPhone.setVisibility(View.VISIBLE);
            tvMsg.setVisibility(View.VISIBLE);
            tvMsg.setText("开启后，你的朋友可以通过手机号找到你");

            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(13)});
        } else if (key == UserConstant.KEY_EMAIL || key == UserConstant.KEY_SIGNATURE) {
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        } else if (key == UserConstant.KEY_ALIAS) {
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16)});
        }
        if (key == UserConstant.KEY_ALIAS) {
            Friend friend = FriendDataCache.getInstance().getFriendByAccount(data);
            if (friend != null && !TextUtils.isEmpty(friend.getAlias())) {
                editText.setText(friend.getAlias());
            } else {
                editText.setHint("请输入备注名...");
            }
        } else {
            if (key == UserConstant.KEY_PHONE) {
                if ("未设置".equals(data)) {
                    editText.setHint("请填写手机号");

                } else {
                    editText.setText(data);
                }
                return;
            }
            if (key == UserConstant.KEY_ACCOUNT) {
                tvMsg.setVisibility(View.VISIBLE);
//                toolbarView.setText("下一步");
                if ("未设置".equals(data)) {
//                    toolbarView.setEnabled(true);
                    editText.setHint("账号限20位字母");
                } else {
//                    toolbarView.setEnabled(false);
                    editText.setText(data);

                }
                return;
            }
            editText.setText(data);
        }
        editText.setDeleteImage(com.aqtx.app.R.drawable.nim_grey_delete_icon);
    }

    private void initActionbar() {
//        toolbarView = findView(com.aqtx.app.R.id.action_bar_right_clickable_textview);
        if (key == UserConstant.KEY_ACCOUNT) {
            if ("未设置".equals(data)) {
                editText.setFocusable(true);
            } else {
                editText.setFocusable(false);
            }
            btnCommit.setText(com.aqtx.app.R.string.save);
        } else if (key == UserConstant.KEY_PHONE) {
            btnCommit.setText("下一步");
        } else {
            btnCommit.setText(com.aqtx.app.R.string.save);
        }

        btnCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkUtil.isNetAvailable(UserProfileEditItemActivity.this)) {
                    Toast.makeText(UserProfileEditItemActivity.this, com.aqtx.app.R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
                    return;
                }
                String nickRegex = "^[-_\u4E00-\u9FA5]{1,10}$";
                if (key == UserConstant.KEY_NICKNAME && !editText.getText().toString().trim().matches(nickRegex)) {
                    Toast.makeText(UserProfileEditItemActivity.this, "姓名不合法", Toast.LENGTH_SHORT).show();
                    return;
                }
                // TODO: 2016/12/8 更改安信账号
                String accountRegex = "^[a-zA-Z]{1,20}$";

                if (key == UserConstant.KEY_ACCOUNT && !editText.getText().toString().trim().matches(accountRegex)) {
                    Toast.makeText(UserProfileEditItemActivity.this, "安信账号限20位字母", Toast.LENGTH_SHORT).show();
                    return;
                } else {

                }

//                if (key == UserConstant.KEY_PHONE) {
//                    String text = toolbarView.getText().toString().trim();
//                    if ("下一步".equals(text)) {
//
//                    } else {
//
//                    }
//                }
                if (key == UserConstant.KEY_BIRTH) {
                    update(birthText.getText().toString());
                } else if (key == UserConstant.KEY_GENDER) {
                    update(Integer.valueOf(gender));
                } else {
                    update(editText.getText().toString().trim());
                }
            }
        });
    }

    private void findGenderViews() {
        maleLayout = findView(com.aqtx.app.R.id.male_layout);
        femaleLayout = findView(com.aqtx.app.R.id.female_layout);
        otherLayout = findView(com.aqtx.app.R.id.other_layout);

        maleCheck = findView(com.aqtx.app.R.id.male_check);
        femaleCheck = findView(com.aqtx.app.R.id.female_check);
        otherCheck = findView(com.aqtx.app.R.id.other_check);

        maleLayout.setOnClickListener(this);
        femaleLayout.setOnClickListener(this);
        otherLayout.setOnClickListener(this);

        initGender();
    }

    private void initGender() {
        gender = Integer.parseInt(data);
        genderCheck(gender);
    }

    private void findBirthViews() {
        birthPickerLayout = findView(com.aqtx.app.R.id.birth_picker_layout);
        birthText = findView(com.aqtx.app.R.id.birth_value);
        birthPickerLayout.setOnClickListener(this);
        birthText.setText(data);

        if (!TextUtils.isEmpty(data)) {
            Date date = TimeUtil.getDateFromFormatString(data);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            if (date != null) {
                birthYear = cal.get(Calendar.YEAR);
                birthMonth = cal.get(Calendar.MONTH);
                birthDay = cal.get(Calendar.DATE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case com.aqtx.app.R.id.male_layout:
                gender = GenderEnum.MALE.getValue();
                genderCheck(gender);
                break;
            case com.aqtx.app.R.id.female_layout:
                gender = GenderEnum.FEMALE.getValue();
                genderCheck(gender);
                break;
            case com.aqtx.app.R.id.other_layout:
                gender = GenderEnum.UNKNOWN.getValue();
                genderCheck(gender);
                break;
            case com.aqtx.app.R.id.birth_picker_layout:
                openTimePicker();
                break;
        }
    }

    private void genderCheck(int selected) {
        otherCheck.setBackgroundResource(selected == GenderEnum.UNKNOWN.getValue() ? com.aqtx.app.R.drawable.nim_contact_checkbox_checked_green : com.aqtx.app.R.drawable.nim_checkbox_not_checked);
        maleCheck.setBackgroundResource(selected == GenderEnum.MALE.getValue() ? com.aqtx.app.R.drawable.nim_contact_checkbox_checked_green : com.aqtx.app.R.drawable.nim_checkbox_not_checked);
        femaleCheck.setBackgroundResource(selected == GenderEnum.FEMALE.getValue() ? com.aqtx.app.R.drawable.nim_contact_checkbox_checked_green : com.aqtx.app.R.drawable.nim_checkbox_not_checked);
    }

    private void openTimePicker() {
        MyDatePickerDialog datePickerDialog = new MyDatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                birthYear = year;
                birthMonth = monthOfYear;
                birthDay = dayOfMonth;
                updateDate();

            }
        }, birthYear, birthMonth, birthDay);
        datePickerDialog.show();
    }

    private void updateDate() {
        birthText.setText(TimeUtil.getFormatDatetime(birthYear, birthMonth, birthDay));
    }

    private void update(final Serializable content) {
        RequestCallbackWrapper callback = new RequestCallbackWrapper() {
            @Override
            public void onResult(int code, Object result, Throwable exception) {
                DialogMaker.dismissProgressDialog();
                if (code == ResponseCode.RES_SUCCESS) {
                    onUpdateCompleted();
                } else if (code == ResponseCode.RES_ETIMEOUT) {
                    Toast.makeText(UserProfileEditItemActivity.this, com.aqtx.app.R.string.user_info_update_failed, Toast.LENGTH_SHORT).show();
                }
            }
        };
        if (key == UserConstant.KEY_ALIAS) {
            DialogMaker.showProgressDialog(this, null, true);
            Map<FriendFieldEnum, Object> map = new HashMap<>();
            map.put(FriendFieldEnum.ALIAS, content);
            NIMClient.getService(FriendService.class).updateFriendFields(data, map).setCallback(callback);
        } else if (key == UserConstant.KEY_NICKNAME) {
//            DialogMaker.showProgressDialog(this, "正在修改，请稍候", true);
            final ProgressDialog dialog = new ProgressDialog(UserProfileEditItemActivity.this);
            dialog.setMessage("修改姓名");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.show();
            // TODO: 2016/11/24 设置姓名
            Log.d("UserProfileEditItemActi", DemoCache.getAccount() + "," + content);
            Map<String, String> map = new HashMap<>();
            map.put("accid", DemoCache.getAccount());
            map.put("name", content.toString());
            HttpManager.getInstance().post(ContantValue.UPDATE_USER_INFO, map, new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    Toast.makeText(UserProfileEditItemActivity.this, "网络有误请稍后重试", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

                @Override
                public void onResponse(String response, int id) {
                    Log.d("UserProfileEditItemActi", response);
                    JSONObject jsonObject = JSON.parseObject(response);
                    if (jsonObject.getString("code").equals("200")) {
//                        onUpdateCompleted();
                        dialog.dismiss();
                        Toast.makeText(UserProfileEditItemActivity.this, com.aqtx.app.R.string.user_info_update_success, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(UserProfileEditItemActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            });
//            OkHttpUtils.get().url(ContantValue.UPDATE_USER_INFO)
//                    .addParams("accid", DemoCache.getAccount())
//                    .addParams("name", content.toString())
//                    .build().execute(new StringCallback() {
//                @Override
//                public void onError(Call call, Exception e, int id) {
//                    Toast.makeText(UserProfileEditItemActivity.this, "网络有误请稍后重试", Toast.LENGTH_SHORT).show();
//                    dialog.dismiss();
//                }
//
//                @Override
//                public void onResponse(String response, int id) {
//                    Log.d("UserProfileEditItemActi", response);
//                    JSONObject jsonObject = JSON.parseObject(response);
//                    if (jsonObject.getString("code").equals("200")) {
////                        onUpdateCompleted();
//                        dialog.dismiss();
//                        Toast.makeText(UserProfileEditItemActivity.this, com.aqtx.app.R.string.user_info_update_success, Toast.LENGTH_SHORT).show();
//                        finish();
//                    } else {
//                        Toast.makeText(UserProfileEditItemActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
//                        dialog.dismiss();
//                    }
//                }
//            });

        } else if (key == UserConstant.KEY_PHONE) {

//            DialogMaker.showProgressDialog(this, "正在修改，请稍候", true);
            final ProgressDialog dialog = new ProgressDialog(UserProfileEditItemActivity.this);
            dialog.setMessage("验证手机号");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            String text = btnCommit.getText().toString().trim();

            if ("下一步".equals(text)) {
                if (isOpen) {
                    links = 1;
                } else {
                    links = 0;
                }
                Map<String, String> map = new HashMap<>();
                map.put("mobile", content.toString());
                HttpManager.getInstance().post(ContantValue.SEND_SET_PHONE_VERIFY, map, new StringCallback() {
                    @Override
                    public void onBefore(Request request, int id) {
                        super.onBefore(request, id);
                        dialog.show();
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
                        Toast.makeText(UserProfileEditItemActivity.this, "网络有误请稍后重试", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        JSONObject jsonObject = JSON.parseObject(response);
                        if (jsonObject.getString("code").equals("200")) {
                            Toast.makeText(UserProfileEditItemActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
                            btnCommit.setText("保存");
                            layoutVerification.setVisibility(View.VISIBLE);
                            tvPhone.setText("+86 " + content.toString());
                            countDownTimer = new CountDownTimer(60000, 1000) {
                                @Override
                                public void onTick(long l) {
                                    tvTimer.setText("接收短信大约需要" + l / 1000 + "秒钟");
                                }

                                @Override
                                public void onFinish() {
////                                   重新获取验证码
//                                    Map<String, String> params = new HashMap<String, String>();
//                                    params.put("mobile", content.toString());
//                                    HttpManager.getInstance().post(ContantValue.SEND_SET_PHONE_VERIFY, params, new StringCallback() {
//                                        @Override
//                                        public void onError(Call call, Exception e, int id) {
//
//                                        }
//
//                                        @Override
//                                        public void onResponse(String response, int id) {
//                                            JSONObject jsonObject1 = JSON.parseObject(response);
//                                            if (jsonObject1.getString("code").equals("200")) {
//                                                Toast.makeText(UserProfileEditItemActivity.this, "验证码已重发", Toast.LENGTH_SHORT).show();
//                                            }
//
//                                        }
//                                    });
//                                    OkHttpUtils.get().addParams("mobile", content.toString()).url(ContantValue.SEND_SET_PHONE_VERIFY).build().execute(new StringCallback() {
//                                        @Override
//                                        public void onError(Call call, Exception e, int id) {
//
//                                        }
//
//                                        @Override
//                                        public void onResponse(String response, int id) {
//                                            JSONObject jsonObject1 = JSON.parseObject(response);
//                                            if (jsonObject1.getString("code").equals("200")) {
//                                                Toast.makeText(UserProfileEditItemActivity.this, "验证码已重发", Toast.LENGTH_SHORT).show();
//                                            }
//
//                                        }
//                                    });
                                }
                            };
                            countDownTimer.start();
                            layoutShow.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(UserProfileEditItemActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }

                });
//                OkHttpUtils.get().addParams("mobile", content.toString())
//                        .url(ContantValue.SEND_SET_PHONE_VERIFY).build().execute(new StringCallback() {
//                    @Override
//                    public void onBefore(Request request, int id) {
//                        super.onBefore(request, id);
//                        dialog.show();
//                    }
//
//                    @Override
//                    public void onError(Call call, Exception e, int id) {
//                        Toast.makeText(UserProfileEditItemActivity.this, "网络有误请稍后重试", Toast.LENGTH_SHORT).show();
//                        dialog.dismiss();
//                    }
//
//                    @Override
//                    public void onResponse(String response, int id) {
//                        JSONObject jsonObject = JSON.parseObject(response);
//                        if (jsonObject.getString("code").equals("200")) {
//                            Toast.makeText(UserProfileEditItemActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
//                            btnCommit.setText("保存");
//                            layoutVerification.setVisibility(View.VISIBLE);
//                            tvPhone.setText("+86 " + content.toString());
//                            countDownTimer = new CountDownTimer(60000, 1000) {
//                                @Override
//                                public void onTick(long l) {
//                                    tvTimer.setText("接收短信大约需要" + l / 1000 + "秒钟");
//                                }
//
//                                @Override
//                                public void onFinish() {
////                                   重新获取验证码
//                                    OkHttpUtils.get().addParams("mobile", content.toString()).url(ContantValue.SEND_SET_PHONE_VERIFY).build().execute(new StringCallback() {
//                                        @Override
//                                        public void onError(Call call, Exception e, int id) {
//
//                                        }
//
//                                        @Override
//                                        public void onResponse(String response, int id) {
//                                            JSONObject jsonObject1 = JSON.parseObject(response);
//                                            if (jsonObject1.getString("code").equals("200")) {
//                                                Toast.makeText(UserProfileEditItemActivity.this, "验证码已重发", Toast.LENGTH_SHORT).show();
//                                            }
//
//                                        }
//                                    });
//                                }
//                            };
//                            countDownTimer.start();
//                            layoutShow.setVisibility(View.GONE);
//                        } else {
//                            Toast.makeText(UserProfileEditItemActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
//                        }
//                        dialog.dismiss();
//                    }
//
//                });
//
            } else if ("完成".equals(text)) {
                finish();
            } else {
                String code = etCode.getText().toString().trim();
                if (TextUtils.isEmpty(code)) {
                    Toast.makeText(this, "请填写验证码", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, String> map = new HashMap<>();
                    map.put("accid", DemoCache.getAccount());
                    map.put("mobile", content.toString());
                    map.put("code", code);
                    map.put("links", String.valueOf(links));
                    HttpManager.getInstance().post(ContantValue.UPDATE_USER_INFO, map, new StringCallback() {
                        @Override
                        public void onBefore(Request request, int id) {
                            super.onBefore(request, id);
                            dialog.show();
                        }

                        @Override
                        public void onError(Call call, Exception e, int id) {
                            Toast.makeText(UserProfileEditItemActivity.this, "网络有误请稍后重试", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            Log.d("UserProfileEditItemActi", response);
                            JSONObject jsonObject = JSON.parseObject(response);
                            if (jsonObject.getString("code").equals("200")) {
//                        onUpdateCompleted();
                                if (countDownTimer != null) {
                                    countDownTimer.cancel();
                                }
                                dialog.dismiss();
                                tvBindPhone.setText("+86 " + content.toString());
                                layoutShow.setVisibility(View.GONE);
                                layoutVerification.setVisibility(View.GONE);
                                layoutSuccess.setVisibility(View.VISIBLE);
                                btnCommit.setText("完成");
//                            Toast.makeText(UserProfileEditItemActivity.this, com.aqtx.app.R.string.user_info_update_success, Toast.LENGTH_SHORT).show();
//                            finish();
                            } else {
                                Toast.makeText(UserProfileEditItemActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    });
//                    OkHttpUtils.get().url(ContantValue.UPDATE_USER_INFO)
//                            .addParams("accid", DemoCache.getAccount())
//                            .addParams("mobile", content.toString())
//                            .addParams("code", code)
//                            .addParams("links", String.valueOf(links))
//                            .build().execute(new StringCallback() {
//                        @Override
//                        public void onBefore(Request request, int id) {
//                            super.onBefore(request, id);
//                            dialog.show();
//                        }
//
//                        @Override
//                        public void onError(Call call, Exception e, int id) {
//                            Toast.makeText(UserProfileEditItemActivity.this, "网络有误请稍后重试", Toast.LENGTH_SHORT).show();
//                            dialog.dismiss();
//                        }
//
//                        @Override
//                        public void onResponse(String response, int id) {
//                            Log.d("UserProfileEditItemActi", response);
//                            JSONObject jsonObject = JSON.parseObject(response);
//                            if (jsonObject.getString("code").equals("200")) {
////                        onUpdateCompleted();
//                                if (countDownTimer != null) {
//                                    countDownTimer.cancel();
//                                }
//                                dialog.dismiss();
//                                tvBindPhone.setText("+86 " + content.toString());
//                                layoutShow.setVisibility(View.GONE);
//                                layoutVerification.setVisibility(View.GONE);
//                                layoutSuccess.setVisibility(View.VISIBLE);
//                                btnCommit.setText("完成");
////                            Toast.makeText(UserProfileEditItemActivity.this, com.aqtx.app.R.string.user_info_update_success, Toast.LENGTH_SHORT).show();
////                            finish();
//                            } else {
//                                Toast.makeText(UserProfileEditItemActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
//                                dialog.dismiss();
//                            }
//                        }
//                    });
                }
//                finish();
            }


        } else if (key == UserConstant.KEY_ACCOUNT) {
//            DialogMaker.showProgressDialog(this, "正在修改，请稍候", true);
            final ProgressDialog dialog = new ProgressDialog(UserProfileEditItemActivity.this);
            dialog.setMessage("修改账号");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.show();
            // TODO: 2016/11/24 设置账号
            Log.d("UserProfileEditItemActi", DemoCache.getAccount() + "," + content);
            Map<String, String> map = new HashMap<>();
            map.put("accid", DemoCache.getAccount());
            map.put("access", content.toString());
            HttpManager.getInstance().post(ContantValue.UPDATE_USER_INFO, map, new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    Toast.makeText(UserProfileEditItemActivity.this, "网络有误请稍后重试", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

                @Override
                public void onResponse(String response, int id) {
                    Log.d("UserProfileEditItemActi", response);
                    JSONObject jsonObject = JSON.parseObject(response);
                    if (jsonObject.getString("code").equals("200")) {
//                        onUpdateCompleted();
                        dialog.dismiss();
                        Toast.makeText(UserProfileEditItemActivity.this, com.aqtx.app.R.string.user_info_update_success, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(UserProfileEditItemActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            });
//            OkHttpUtils.get().url(ContantValue.UPDATE_USER_INFO)
//                    .addParams("accid", DemoCache.getAccount())
//                    .addParams("access", content.toString())
//                    .build().execute(new StringCallback() {
//                @Override
//                public void onError(Call call, Exception e, int id) {
//                    Toast.makeText(UserProfileEditItemActivity.this, "网络有误请稍后重试", Toast.LENGTH_SHORT).show();
//                    dialog.dismiss();
//                }
//
//                @Override
//                public void onResponse(String response, int id) {
//                    Log.d("UserProfileEditItemActi", response);
//                    JSONObject jsonObject = JSON.parseObject(response);
//                    if (jsonObject.getString("code").equals("200")) {
////                        onUpdateCompleted();
//                        dialog.dismiss();
//                        Toast.makeText(UserProfileEditItemActivity.this, com.aqtx.app.R.string.user_info_update_success, Toast.LENGTH_SHORT).show();
//                        finish();
//                    } else {
//                        Toast.makeText(UserProfileEditItemActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
//                        dialog.dismiss();
//                    }
//                }
//            });

        } else {
            if (fieldMap == null) {
                fieldMap = new HashMap<>();
//                fieldMap.put(UserConstant.KEY_NICKNAME, UserInfoFieldEnum.Name);
                fieldMap.put(UserConstant.KEY_PHONE, UserInfoFieldEnum.MOBILE);
                fieldMap.put(UserConstant.KEY_SIGNATURE, UserInfoFieldEnum.SIGNATURE);
                fieldMap.put(UserConstant.KEY_EMAIL, UserInfoFieldEnum.EMAIL);
                fieldMap.put(UserConstant.KEY_BIRTH, UserInfoFieldEnum.BIRTHDAY);
                fieldMap.put(UserConstant.KEY_GENDER, UserInfoFieldEnum.GENDER);
            }
            DialogMaker.showProgressDialog(this, null, true);
            UserUpdateHelper.update(fieldMap.get(key), content, callback);
        }
    }

    private void onUpdateCompleted() {
        showKeyboard(false);
        Toast.makeText(UserProfileEditItemActivity.this, com.aqtx.app.R.string.user_info_update_success, Toast.LENGTH_SHORT).show();
        finish();
    }

    private class MyDatePickerDialog extends DatePickerDialog {
        private int maxYear = 2015;
        private int minYear = 1900;
        private int currYear;
        private int currMonthOfYear;
        private int currDayOfMonth;

        public MyDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
            super(context, callBack, year, monthOfYear, dayOfMonth);
            currYear = year;
            currMonthOfYear = monthOfYear;
            currDayOfMonth = dayOfMonth;
        }

        @Override
        public void onDateChanged(DatePicker view, int year, int month, int day) {
            if (year >= minYear && year <= maxYear) {
                currYear = year;
                currMonthOfYear = month;
                currDayOfMonth = day;
            } else {
                if (currYear > maxYear) {
                    currYear = maxYear;
                } else if (currYear < minYear) {
                    currYear = minYear;
                }
                updateDate(currYear, currMonthOfYear, currDayOfMonth);
            }
        }

        public void setMaxYear(int year) {
            maxYear = year;
        }

        public void setMinYear(int year) {
            minYear = year;
        }

        public void setTitle(CharSequence title) {
            super.setTitle("生 日");
        }
    }
}
