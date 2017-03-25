package com.aqtx.app.chatroom.helper;

import com.netease.nim.uikit.common.ui.imageview.ImageViewEx;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hzxuwen on 2016/1/19.
 */
public class ChatRoomHelper {
    private static final int[] imageRes = {com.aqtx.app.R.drawable.room_cover_36, com.aqtx.app.R.drawable.room_cover_37, com.aqtx.app.R.drawable.room_cover_49,
            com.aqtx.app.R.drawable.room_cover_50, com.aqtx.app.R.drawable.room_cover_57, com.aqtx.app.R.drawable.room_cover_58, com.aqtx.app.R.drawable.room_cover_64,
            com.aqtx.app.R.drawable.room_cover_72};

    private static Map<String, Integer> roomCoverMap = new HashMap<>();
    private static int index = 0;

    public static void init() {
        ChatRoomMemberCache.getInstance().clear();
        ChatRoomMemberCache.getInstance().registerObservers(true);
    }

    public static void logout() {
        ChatRoomMemberCache.getInstance().registerObservers(false);
        ChatRoomMemberCache.getInstance().clear();
    }

    public static void setCoverImage(String roomId, ImageViewEx coverImage) {
        if (roomCoverMap.containsKey(roomId)) {
            coverImage.setImageResource(roomCoverMap.get(roomId));
        } else {
            if (index > imageRes.length) {
                index = 0;
            }
            roomCoverMap.put(roomId, imageRes[index]);
            coverImage.setImageResource(imageRes[index]);
            index++;
        }
    }
}
