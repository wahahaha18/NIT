package com.aqtx.app.chatroom.viewholder;

import android.view.View;
import android.widget.TextView;

import com.aqtx.app.chatroom.adapter.ChatRoomAdapter;
import com.aqtx.app.chatroom.helper.ChatRoomHelper;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.imageview.ImageViewEx;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomInfo;

public class ChatRoomViewHolder extends TViewHolder {
    private final static int COUNT_LIMIT = 10000;
    private ImageViewEx coverImage;
    private TextView nameText;
    private TextView onlineCountText;

    private ChatRoomInfo room;

    public void refresh(Object item) {
        room = (ChatRoomInfo) item;
        updateBackground();
        ChatRoomHelper.setCoverImage(room.getRoomId(), coverImage);
        nameText.setText(room.getName());
        setOnlineCount();
    }

    protected int getResId() {
        return com.aqtx.app.R.layout.chat_room_item;
    }

    public void inflate() {
        this.coverImage = findView(com.aqtx.app.R.id.cover_image);
        this.nameText = findView(com.aqtx.app.R.id.tv_name);
        this.onlineCountText = findView(com.aqtx.app.R.id.tv_online_count);

        coverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ChatRoomAdapter)getAdapter()).getEventListener().onItemClick(room.getRoomId());
            }
        });
    }

    private void updateBackground() {
        view.setBackgroundResource(com.aqtx.app.R.drawable.list_item_bg_selecter);
    }

    private void setOnlineCount() {
        if (room.getOnlineUserCount() < COUNT_LIMIT) {
            onlineCountText.setText(String.valueOf(room.getOnlineUserCount()));
        } else if (room.getOnlineUserCount() >= COUNT_LIMIT) {
            onlineCountText.setText(String.format("%.1f", room.getOnlineUserCount() / (float) COUNT_LIMIT) + "万");
        }
    }
}
