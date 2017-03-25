package com.aqtx.app.session.viewholder;

import android.widget.TextView;

import com.aqtx.app.session.extension.RTSAttachment;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderBase;

public class MsgViewHolderRTS extends MsgViewHolderBase {

    private TextView textView;

    @Override
    protected int getContentResId() {
        return com.aqtx.app.R.layout.nim_message_item_rts;
    }

    @Override
    protected void inflateContentView() {
        textView = (TextView) view.findViewById(com.aqtx.app.R.id.rts_text);
    }

    @Override
    protected void bindContentView() {
        RTSAttachment attachment = (RTSAttachment) message.getAttachment();
        textView.setText(attachment.getContent());
    }

    @Override
    protected int leftBackground() {
        return com.aqtx.app.R.drawable.nim_message_left_white_bg;
    }

    @Override
    protected int rightBackground() {
        return com.aqtx.app.R.drawable.nim_message_right_blue_bg;
    }
}

