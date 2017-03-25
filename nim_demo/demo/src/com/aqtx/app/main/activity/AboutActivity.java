package com.aqtx.app.main.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aqtx.app.BuildConfig;
import com.aqtx.app.ContantValue;
import com.aqtx.app.R;
import com.aqtx.app.config.preference.Preferences;
import com.aqtx.app.main.helper.AppUtil;
import com.aqtx.app.main.helper.SoftUpdate;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nim.uikit.permission.MPermission;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.File;

import okhttp3.Call;

public class AboutActivity extends UI {
    private static final String url = ContantValue.BaseUrl + "index/androidversion";
    ProgressBar mProgress;
    ProgressDialog mProgressDialog;
    private TextView tvVersion;
    private TextView tvTip;
    private View layoutUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.aqtx.app.R.layout.about_layout);
        ToolBarOptions options = new ToolBarOptions();
        setToolBar(com.aqtx.app.R.id.toolbar, options);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("正在下载");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        findViews();
        initViewData();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void findViews() {
        layoutUpdate = findViewById(R.id.layout_check_update);
        tvVersion = (TextView) findViewById(R.id.tv_version);
        tvTip = (TextView) findViewById(R.id.tv_tip);
        if (Preferences.isUpdate()) {
            tvTip.setVisibility(View.VISIBLE);
        } else {
            tvTip.setVisibility(View.GONE);
        }

//        CustomActions.customButton((Button) findViewById(R.id.about_custom_button_1));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        Toast.makeText(this, "asfasf.T", Toast.LENGTH_SHORT).show();
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //permission granted
                //TODO 向SD卡写数据
                OkHttpUtils.get().url(url).build().execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {

                    }

                    @Override
                    public void onResponse(String response, int id) {
                        final JSONObject result = JSON.parseObject(response);
                        Log.d("SoftUpdate", response);
                        int oldVersion = AppUtil.getAppVersionCode(AboutActivity.this);
                        if (result.getString("currentversion").compareTo(String.valueOf(oldVersion)) > 0) {
//                    提示更新
                            downLoadApk(result.getString("durl"));
                            Preferences.saveIsUpdate(true);
                        } else {
                            Preferences.saveIsUpdate(false);
                            Toast.makeText(AboutActivity.this, "暂无新版本", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            } else {
                //permission denied
                //TODO 显示对话框告知用户必须打开权限
            }
        }
    }

    private void initViewData() {
        // 如果使用的IDE是Eclipse， 将该函数体注释掉。这里使用了Android Studio编译期添加BuildConfig字段的特性
        tvVersion.setText("安信: " + AppUtil.getAppVersionName(this));
        layoutUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(AboutActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    OkHttpUtils.get().url(url).build().execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e, int id) {

                        }

                        @Override
                        public void onResponse(String response, int id) {
                            final JSONObject result = JSON.parseObject(response);
                            Log.d("SoftUpdate", response);
                            int oldVersion = AppUtil.getAppVersionCode(AboutActivity.this);
                            if (result.getString("currentversion").compareTo(String.valueOf(oldVersion)) > 0) {
//                    提示更新
                                showDialog(result.getString("durl"));
                                Preferences.saveIsUpdate(true);
                            } else {
                                Preferences.saveIsUpdate(false);
                                Toast.makeText(AboutActivity.this, "暂无新版本", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                } else {
                    //没有权限,判断是否会弹权限申请对话框
                    boolean shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(AboutActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (shouldShow) {
                        //申请权限
                        ActivityCompat.requestPermissions(AboutActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    } else {
                        //被禁止显示弹窗
                        //TODO 显示对话框告知用户必须打开权限
                        Toast.makeText(AboutActivity.this, "您的权限还没被打开，请去往设置打开", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });
    }


    /**
     * 下载并安装apk
     *
     * @param url
     */
    private void downLoadApk(String url) {
        OkHttpUtils.get().url(url).build().execute(new FileCallBack(Environment.getExternalStorageDirectory().getAbsolutePath(), "nim.apk") {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.d("SoftUpdate", e.getMessage());
                mProgressDialog.dismiss();
            }

            @Override
            public void onResponse(File response, int id) {
                Log.d("SoftUpdate", response.getAbsolutePath());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(response), "application/vnd.android.package-archive");
                startActivity(intent);
                mProgressDialog.dismiss();
            }

            @Override
            public void inProgress(float progress, long total, int id) {
                mProgressDialog.setProgress((int) (progress * 100));
                mProgressDialog.show();
            }
        });
    }

    /**
     * 这是兼容的 AlertDialog
     */
    private void showDialog(final String durl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("版本更新");
        builder.setCancelable(false);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downLoadApk(durl);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
}
