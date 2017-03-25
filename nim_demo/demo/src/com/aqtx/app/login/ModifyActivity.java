package com.aqtx.app.login;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.aqtx.app.ContantValue;
import com.aqtx.app.DemoCache;
import com.aqtx.app.R;
import com.aqtx.app.config.preference.Preferences;
import com.aqtx.app.main.activity.MainActivity;
import com.aqtx.app.main.activity.SettingsActivity;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.auth.AuthService;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;

public class ModifyActivity extends UI {
    EditText et_oldPass, et_newPass, et_confirmPass;
    Button btnSure;
    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify);
        ToolBarOptions options = new ToolBarOptions();
        options.titleId = com.aqtx.app.R.string.modify_pass;
        setToolBar(com.aqtx.app.R.id.toolbar, options);
        et_confirmPass = (EditText) findViewById(R.id.et_confirm_pass);
        et_newPass = (EditText) findViewById(R.id.et_new_pass);
        et_oldPass = (EditText) findViewById(R.id.et_old_pass);
        btnSure = (Button) findViewById(R.id.btnSure);
        btnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressDialog = new ProgressDialog(ModifyActivity.this);
                mProgressDialog.setMessage("正在修改,请稍候...");
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(false);
                // TODO: 2016/11/22 修改密码
                String oldPass = et_oldPass.getText().toString().trim();
                String newPass = et_newPass.getText().toString().trim();
                String confirmPass = et_confirmPass.getText().toString().trim();
                if (!TextUtils.isEmpty(oldPass) && !TextUtils.isEmpty(newPass) && !TextUtils.isEmpty(confirmPass)) {
                    String regex = "[a-zA-Z0-9]{6,}";
                    if (newPass.matches(regex)) {
                        if (newPass.equals(confirmPass)) {
                            mProgressDialog.show();
                            OkHttpUtils.get().url(ContantValue.SET_PASS).addParams("accid", DemoCache.getAccount()).addParams("old_token", oldPass).addParams("new_token", newPass).build().execute(new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e, int id) {
                                    mProgressDialog.dismiss();
                                    Toast.makeText(ModifyActivity.this, "请求失败", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onResponse(String response, int id) {
                                    Log.d("ModifyActivity", response);
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        if (jsonObject.getString("status").equals("1")) {
                                            Toast.makeText(ModifyActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
                                            Preferences.saveUserToken("");
                                            MainActivity.logout(ModifyActivity.this, false);
                                            finish();
                                            NIMClient.getService(AuthService.class).logout();
                                        } else {
                                            Toast.makeText(ModifyActivity.this, "原密码错误", Toast.LENGTH_SHORT).show();
                                        }
                                        mProgressDialog.dismiss();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });

                        } else {
                            Toast.makeText(ModifyActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ModifyActivity.this, "密码至少为6位数字或英文", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(ModifyActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
