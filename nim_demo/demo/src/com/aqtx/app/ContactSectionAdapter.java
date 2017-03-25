package com.aqtx.app;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseSectionQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.orhanobut.logger.Logger;

import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by y11621546 on 2016/12/9.
 */

public class ContactSectionAdapter extends BaseSectionQuickAdapter<ContactSection, BaseViewHolder> {

    Map<String, String> map;

    public ContactSectionAdapter(List<ContactSection> data) {
        super(R.layout.layout_item_contact, R.layout.layout_item_contact_head, data);
        map = JSON.parseObject(com.aqtx.app.config.preference.Preferences.getContact(), Map.class);
//        Logger.json(com.aqtx.app.config.preference.Preferences.getContact());
    }

    @Override
    protected void convertHead(BaseViewHolder baseViewHolder, ContactSection contactSection) {
        baseViewHolder.setText(R.id.tv_type_head, contactSection.header);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, ContactSection contactSection) {
        if (contactSection.t == null) return;
        TextView tvName = baseViewHolder.getView(R.id.tv_name);
        baseViewHolder.setText(R.id.tv_name, (String) contactSection.t.getName())
                .setText(R.id.tv_contact_name, "手机联系人：" + getContactName(map, contactSection.t.getPhone()))
                .addOnClickListener(R.id.btn_refuse)
                .addOnClickListener(R.id.btn_accept)
                .addOnClickListener(R.id.btn_add);

        TextView tvState = baseViewHolder.getView(R.id.tv_state);
        Button btnAdd = baseViewHolder.getView(R.id.btn_add);
        Button btnAccept = baseViewHolder.getView(R.id.btn_accept);
        Button btnRefuse = baseViewHolder.getView(R.id.btn_refuse);
        LinearLayout layoutButton = baseViewHolder.getView(R.id.layout_button);
        ImageView imgHead = baseViewHolder.getView(R.id.img_head);


        Glide.with(mContext).load(contactSection.t.getIcon()).placeholder(R.drawable.avatar_def).error(R.drawable.avatar_def).bitmapTransform(new CropCircleTransformation(mContext)).into(imgHead);

        if (contactSection.t.getStatus() == null)
            return;

        switch (contactSection.t.getStatus()) {
            case "0"://等待验证
                layoutButton.setVisibility(View.GONE);
                tvState.setVisibility(View.VISIBLE);
                tvState.setText("等待验证");
                break;
            case "1"://通过
                layoutButton.setVisibility(View.GONE);
                tvState.setVisibility(View.VISIBLE);
                tvState.setText("已添加");
                break;
            case "2"://拒绝
                layoutButton.setVisibility(View.GONE);
                tvState.setVisibility(View.VISIBLE);
                tvState.setText("已拒绝");
                break;
            case "3"://显示添加按钮
                layoutButton.setVisibility(View.VISIBLE);
                tvState.setVisibility(View.GONE);
                btnAdd.setVisibility(View.VISIBLE);
                btnAdd.setText("添加");
                btnAccept.setVisibility(View.GONE);
                btnRefuse.setVisibility(View.GONE);
                break;
            case "4"://显示通过/拒绝按钮
                layoutButton.setVisibility(View.VISIBLE);
                tvState.setVisibility(View.GONE);
                btnAdd.setVisibility(View.GONE);
                btnAccept.setVisibility(View.VISIBLE);
                btnRefuse.setVisibility(View.VISIBLE);
                break;
            case "5"://通过
                layoutButton.setVisibility(View.GONE);
                tvState.setVisibility(View.VISIBLE);
                tvState.setText("已添加");
                break;
            case "11"://邀请
                tvName.setText("发送短信，邀请好友");
                layoutButton.setVisibility(View.VISIBLE);
                tvState.setVisibility(View.GONE);
                btnAdd.setVisibility(View.VISIBLE);
                btnAdd.setText("邀请");
                btnAccept.setVisibility(View.GONE);
                btnRefuse.setVisibility(View.GONE);
                break;
        }

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
}
