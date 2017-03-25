package com.aqtx.app.main.model;

import com.aqtx.app.R;
import com.aqtx.app.main.fragment.ChatRoomListFragment;
import com.aqtx.app.main.fragment.ContactListFragment;
import com.aqtx.app.main.reminder.ReminderId;
import com.aqtx.app.main.fragment.MainTabFragment;
import com.aqtx.app.main.fragment.SessionListFragment;

public enum MainTab {
    RECENT_CONTACTS(0, ReminderId.SESSION, SessionListFragment.class, com.aqtx.app.R.string.main_tab_session, com.aqtx.app.R.layout.session_list),
    CONTACT(1, ReminderId.CONTACT, ContactListFragment.class, com.aqtx.app.R.string.main_tab_contact, com.aqtx.app.R.layout.contacts_list),
    //    CHAT_ROOM(2, ReminderId.INVALID, ChatRoomListFragment.class, com.aqtx.app.R.string.chat_room, com.aqtx.app.R.layout.chat_room_tab);
    CHAT_ROOM(2, ReminderId.INVALID, ChatRoomListFragment.class, com.aqtx.app.R.string.setting, R.layout.settings_activity);

    public final int tabIndex;

    public final int reminderId;

    public final Class<? extends MainTabFragment> clazz;

    public final int resId;

    public final int fragmentId;

    public final int layoutId;

    MainTab(int index, int reminderId, Class<? extends MainTabFragment> clazz, int resId, int layoutId) {
        this.tabIndex = index;
        this.reminderId = reminderId;
        this.clazz = clazz;
        this.resId = resId;
        this.fragmentId = index;
        this.layoutId = layoutId;
    }

    public static final MainTab fromReminderId(int reminderId) {
        for (MainTab value : MainTab.values()) {
            if (value.reminderId == reminderId) {
                return value;
            }
        }

        return null;
    }

    public static final MainTab fromTabIndex(int tabIndex) {
        for (MainTab value : MainTab.values()) {
            if (value.tabIndex == tabIndex) {
                return value;
            }
        }

        return null;
    }
}
