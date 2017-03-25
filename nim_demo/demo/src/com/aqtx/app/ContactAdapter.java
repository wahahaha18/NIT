package com.aqtx.app;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.orhanobut.logger.Logger;

import java.util.List;
import java.util.Map;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by y11621546 on 2016/12/7.
 */

public class ContactAdapter extends BaseQuickAdapter<Contact, BaseViewHolder> {
    Map<String, String> map;

    public ContactAdapter(List<Contact> data) {
        super(R.layout.layout_item_contact, data);
        map = JSON.parseObject(com.aqtx.app.config.preference.Preferences.getContact(), Map.class);
        Logger.json(map.toString());
    }


    @Override
    public int getItemCount() {
        return getData().size();
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, Contact contact) {
        if (contact == null) return;
        baseViewHolder.setText(R.id.tv_name, contact.getName())
                .setText(R.id.tv_contact_name, TextUtils.isEmpty(getContactName(map, contact.getElibom())) ? contact.getMsg() : "手机联系人：" + getContactName(map, contact.getElibom()))
                .addOnClickListener(R.id.btn_refuse)
                .addOnClickListener(R.id.btn_accept)
                .addOnClickListener(R.id.btn_add);


        TextView tvState = baseViewHolder.getView(R.id.tv_state);
        Button btnAdd = baseViewHolder.getView(R.id.btn_add);
        Button btnAccept = baseViewHolder.getView(R.id.btn_accept);
        Button btnRefuse = baseViewHolder.getView(R.id.btn_refuse);
        LinearLayout layoutButton = baseViewHolder.getView(R.id.layout_button);
        ImageView imgHead = baseViewHolder.getView(R.id.img_head);


        Glide.with(mContext).load(contact.getIcon()).placeholder(R.drawable.avatar_def).error(R.drawable.avatar_def).bitmapTransform(new CropCircleTransformation(mContext)).into(imgHead);


        switch (contact.getStatus()) {
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
        }

    }


    /**
     * 获取通讯录名字
     *
     * @param key 手机号MD5
     */
    private String getContactName(Map<String, String> map, String key) {
        if (TextUtils.isEmpty(key)) {
            return "";
        }
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
