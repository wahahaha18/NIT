package com.aqtx.app.contact.activity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.SearchView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
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
import com.bumptech.glide.Glide;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
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
public class QrCodeActivity extends UI {


    private ImageView imageView;
    private NimUserInfo userInfo;

    public static final void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, QrCodeActivity.class);
        context.startActivity(intent);
    }


    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        TelephonyManager phoneMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        ToolBarOptions options = new ToolBarOptions();
        options.titleId = R.string.qrcode;
        setToolBar(R.id.toolbar, options);
        findViews();
        initActionbar();

    }


    private void findViews() {
        imageView = findView(R.id.img_qrcode);
        Map<String, String> map = new HashMap<>();
        map.put("accid", DemoCache.getAccount());
        HttpManager.getInstance().post(ContantValue.GET_QRCODE, map, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                JSONObject jsonObject = JSON.parseObject(response);
                if (jsonObject.getString("code").equals("200")) {
                    Glide.with(QrCodeActivity.this).load(jsonObject.getJSONObject("data").getString("qrcode")).into(imageView);
                }
            }
        });
//        imageView.setImageBitmap(encodeAsBitmap(DemoCache.getAccount()));
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
}
