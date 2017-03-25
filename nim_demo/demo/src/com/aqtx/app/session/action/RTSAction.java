package com.aqtx.app.session.action;

import android.widget.Toast;

import com.aqtx.app.rts.activity.RTSActivity;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.session.actions.BaseAction;

/**
 * Created by huangjun on 2015/7/7.
 */
public class RTSAction extends BaseAction {

    public RTSAction() {
        super(com.aqtx.app.R.drawable.message_plus_rts_selector, com.aqtx.app.R.string.input_panel_RTS);
    }

    @Override
    public void onClick() {
        if (NetworkUtil.isNetAvailable(getActivity())) {
            RTSActivity.startSession(getActivity(), getAccount(), RTSActivity.FROM_INTERNAL);
        } else {
            Toast.makeText(getActivity(), com.aqtx.app.R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
        }

    }
}