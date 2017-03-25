package com.aqtx.app.contact.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aqtx.app.ContantValue;
import com.aqtx.app.DemoCache;
import com.aqtx.app.HttpManager;
import com.aqtx.app.main.model.Extras;

import com.aqtx.app.contact.constant.UserConstant;
import com.aqtx.app.session.SessionHelper;
import com.netease.nim.uikit.cache.DataCacheManager;
import com.netease.nim.uikit.cache.FriendDataCache;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.ui.dialog.EasyEditDialog;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.widget.SwitchButton;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.friend.FriendService;
import com.netease.nimlib.sdk.friend.FriendServiceObserve;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.AddFriendData;
import com.netease.nimlib.sdk.friend.model.Friend;
import com.netease.nimlib.sdk.friend.model.MuteListChangedNotify;
import com.netease.nimlib.sdk.uinfo.UserServiceObserve;
import com.netease.nimlib.sdk.uinfo.constant.GenderEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * 用户资料页面
 * Created by huangjun on 2015/8/11.
 */
public class UserProfileActivity extends UI {

    private static final String TAG = UserProfileActivity.class.getSimpleName();
    // TODO: 2016/10/14  添加好友验证
    private final boolean FLAG_ADD_FRIEND_DIRECTLY = false; // 是否直接加为好友开关，false为需要好友申请
    private final String KEY_BLACK_LIST = "black_list";
    private final String KEY_MSG_NOTICE = "msg_notice";

    private String account;

    // 基本信息
    private HeadImageView headImageView;
    private TextView nameText;
    private ImageView genderImage;
    private TextView accountText;
    private TextView birthdayText;
    private TextView mobileText;
    private TextView emailText;
    private TextView signatureText;
    private RelativeLayout birthdayLayout;
    private RelativeLayout phoneLayout;
    private RelativeLayout emailLayout;
    private RelativeLayout signatureLayout;
    private RelativeLayout aliasLayout;
    private TextView nickText;

    // 开关
    private ViewGroup toggleLayout;
    private Button addFriendBtn;
    private Button removeFriendBtn;
    private Button chatBtn;
    private SwitchButton blackSwitch;
    private SwitchButton noticeSwitch;
    private Map<String, Boolean> toggleStateMap;

    public static void start(Context context, String account) {
        Intent intent = new Intent();
        intent.setClass(context, UserProfileActivity.class);
        intent.putExtra(Extras.EXTRA_ACCOUNT, account);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.aqtx.app.R.layout.user_profile_activity);

        ToolBarOptions options = new ToolBarOptions();
        options.titleId = com.aqtx.app.R.string.user_profile;
        setToolBar(com.aqtx.app.R.id.toolbar, options);

        account = getIntent().getStringExtra(Extras.EXTRA_ACCOUNT);

        initActionbar();

        findViews();
        registerObserver(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        //        检查对方是否是好友
//        if (!TextUtils.isEmpty(account)) {
//            boolean isMyFriend = NIMClient.getService(FriendService.class).isMyFriend(account);
//            if (isMyFriend) {
//                chatBtn.setVisibility(View.VISIBLE);
////                Toast.makeText(UserProfileActivity.this, "对方已是我的好友", Toast.LENGTH_SHORT).show();
//            } else {
//                chatBtn.setVisibility(View.GONE);
//            }
//        }
        updateUserInfo();
        updateToggleView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObserver(false);
    }

    private void registerObserver(boolean register) {
        FriendDataCache.getInstance().registerFriendDataChangedObserver(friendDataChangedObserver, register);
        NIMClient.getService(FriendServiceObserve.class).observeMuteListChangedNotify(muteListChangedNotifyObserver, register);
    }

    Observer<MuteListChangedNotify> muteListChangedNotifyObserver = new Observer<MuteListChangedNotify>() {
        @Override
        public void onEvent(MuteListChangedNotify notify) {
            setToggleBtn(noticeSwitch, !notify.isMute());
        }
    };

    FriendDataCache.FriendDataChangedObserver friendDataChangedObserver = new FriendDataCache.FriendDataChangedObserver() {
        @Override
        public void onAddedOrUpdatedFriends(List<String> account) {
            updateUserOperatorView();
        }

        @Override
        public void onDeletedFriends(List<String> account) {
            updateUserOperatorView();
        }

        @Override
        public void onAddUserToBlackList(List<String> account) {
            updateUserOperatorView();
        }

        @Override
        public void onRemoveUserFromBlackList(List<String> account) {
            updateUserOperatorView();
        }
    };

    private void findViews() {
        headImageView = findView(com.aqtx.app.R.id.user_head_image);
        nameText = findView(com.aqtx.app.R.id.user_name);
        genderImage = findView(com.aqtx.app.R.id.gender_img);
        accountText = findView(com.aqtx.app.R.id.user_account);
        toggleLayout = findView(com.aqtx.app.R.id.toggle_layout);
        addFriendBtn = findView(com.aqtx.app.R.id.add_buddy);
        chatBtn = findView(com.aqtx.app.R.id.begin_chat);
        removeFriendBtn = findView(com.aqtx.app.R.id.remove_buddy);
        birthdayLayout = findView(com.aqtx.app.R.id.birthday);
        nickText = findView(com.aqtx.app.R.id.user_nick);
        birthdayText = (TextView) birthdayLayout.findViewById(com.aqtx.app.R.id.value);
        phoneLayout = findView(com.aqtx.app.R.id.phone);
        mobileText = (TextView) phoneLayout.findViewById(com.aqtx.app.R.id.value);
        emailLayout = findView(com.aqtx.app.R.id.email);
        emailText = (TextView) emailLayout.findViewById(com.aqtx.app.R.id.value);
        signatureLayout = findView(com.aqtx.app.R.id.signature);
        signatureText = (TextView) signatureLayout.findViewById(com.aqtx.app.R.id.value);
        aliasLayout = findView(com.aqtx.app.R.id.alias);
        ((TextView) birthdayLayout.findViewById(com.aqtx.app.R.id.attribute)).setText(com.aqtx.app.R.string.birthday);
        ((TextView) phoneLayout.findViewById(com.aqtx.app.R.id.attribute)).setText(com.aqtx.app.R.string.phone);
        ((TextView) emailLayout.findViewById(com.aqtx.app.R.id.attribute)).setText(com.aqtx.app.R.string.email);
        ((TextView) signatureLayout.findViewById(com.aqtx.app.R.id.attribute)).setText(com.aqtx.app.R.string.signature);
        ((TextView) aliasLayout.findViewById(com.aqtx.app.R.id.attribute)).setText(com.aqtx.app.R.string.alias);

        addFriendBtn.setOnClickListener(onClickListener);
        chatBtn.setOnClickListener(onClickListener);
        removeFriendBtn.setOnClickListener(onClickListener);
        aliasLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfileEditItemActivity.startActivity(UserProfileActivity.this, UserConstant.KEY_ALIAS, account);
            }
        });
    }

    private void initActionbar() {
        TextView toolbarView = findView(com.aqtx.app.R.id.action_bar_right_clickable_textview);
        if (!DemoCache.getAccount().equals(account)) {
            toolbarView.setVisibility(View.GONE);
            return;
        } else {
            toolbarView.setVisibility(View.VISIBLE);
        }
        toolbarView.setText(com.aqtx.app.R.string.edit);
        toolbarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfileSettingActivity.start(UserProfileActivity.this, account);
            }
        });
    }

    private void addToggleBtn(boolean black, boolean notice) {
        blackSwitch = addToggleItemView(KEY_BLACK_LIST, com.aqtx.app.R.string.black_list, black);
        noticeSwitch = addToggleItemView(KEY_MSG_NOTICE, com.aqtx.app.R.string.msg_notice, notice);
    }

    private void setToggleBtn(SwitchButton btn, boolean isChecked) {
        btn.setCheck(isChecked);
    }

    private void updateUserInfo() {
//        if (NimUserInfoCache.getInstance().hasUser(account)) {
//            updateUserInfoView();
//            return;
//        }

        NimUserInfoCache.getInstance().getUserInfoFromRemote(account, new RequestCallbackWrapper<NimUserInfo>() {
            @Override
            public void onResult(int code, NimUserInfo result, Throwable exception) {
                updateUserInfoView(result);
            }
        });
    }

    private void updateUserInfoView(NimUserInfo result) {
//        accountText.setText("帐号：" + account);
        accountText.setText("账号：" + (TextUtils.isEmpty(result.getExtension()) ? "未设置" : result.getExtension()));
        headImageView.loadBuddyAvatar(account);
        nameText.setText(result.getName());
        if (DemoCache.getAccount().equals(account)) {
            nameText.setText(NimUserInfoCache.getInstance().getUserName(account));
        }

        final NimUserInfo userInfo = NimUserInfoCache.getInstance().getUserInfo(account);
        if (userInfo == null) {
            LogUtil.e(TAG, "userInfo is null when updateUserInfoView");
            return;
        }

        if (userInfo.getGenderEnum() == GenderEnum.MALE) {
            genderImage.setVisibility(View.VISIBLE);
            genderImage.setBackgroundResource(com.aqtx.app.R.drawable.nim_male);
        } else if (userInfo.getGenderEnum() == GenderEnum.FEMALE) {
            genderImage.setVisibility(View.VISIBLE);
            genderImage.setBackgroundResource(com.aqtx.app.R.drawable.nim_female);
        } else {
            genderImage.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(userInfo.getBirthday())) {
            birthdayLayout.setVisibility(View.VISIBLE);
            birthdayText.setText(userInfo.getBirthday());
        } else {
            birthdayLayout.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(userInfo.getMobile())) {
            phoneLayout.setVisibility(View.VISIBLE);
            mobileText.setText(userInfo.getMobile());
        } else {
            phoneLayout.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(userInfo.getEmail())) {
            emailLayout.setVisibility(View.VISIBLE);
            emailText.setText(userInfo.getEmail());
        } else {
            emailLayout.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(userInfo.getSignature())) {
            signatureLayout.setVisibility(View.VISIBLE);
            signatureText.setText(userInfo.getSignature());
        } else {
            signatureLayout.setVisibility(View.GONE);
        }

    }

    private void updateUserOperatorView() {
//        //        检查对方是否是好友
//        if (!TextUtils.isEmpty(account)) {
//            boolean isMyFriend = NIMClient.getService(FriendService.class).isMyFriend(account);
//            if (isMyFriend) {
//                chatBtn.setVisibility(View.VISIBLE);
//            } else {
//                chatBtn.setVisibility(View.GONE);
//            }
//        }
        if (NIMClient.getService(FriendService.class).isMyFriend(account)) {
            removeFriendBtn.setVisibility(View.VISIBLE);
            addFriendBtn.setVisibility(View.GONE);
            chatBtn.setVisibility(View.VISIBLE);
            updateAlias(true);
        } else {
            addFriendBtn.setVisibility(View.VISIBLE);
            removeFriendBtn.setVisibility(View.GONE);
            chatBtn.setVisibility(View.GONE);
            updateAlias(false);
        }
    }

    private void updateToggleView() {
        if (DemoCache.getAccount() != null && !DemoCache.getAccount().equals(account)) {
            boolean black = NIMClient.getService(FriendService.class).isInBlackList(account);
            boolean notice = NIMClient.getService(FriendService.class).isNeedMessageNotify(account);
            if (blackSwitch == null || noticeSwitch == null) {
                addToggleBtn(black, notice);
            } else {
                setToggleBtn(blackSwitch, black);
                setToggleBtn(noticeSwitch, notice);
            }
            Log.i(TAG, "black=" + black + ", notice=" + notice);
            updateUserOperatorView();
        }
    }

    private SwitchButton addToggleItemView(String key, int titleResId, boolean initState) {
        ViewGroup vp = (ViewGroup) getLayoutInflater().inflate(com.aqtx.app.R.layout.nim_user_profile_toggle_item, null);
        ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(com.aqtx.app.R.dimen.isetting_item_height));
        vp.setLayoutParams(vlp);

        TextView titleText = ((TextView) vp.findViewById(com.aqtx.app.R.id.user_profile_title));
        titleText.setText(titleResId);

        SwitchButton switchButton = (SwitchButton) vp.findViewById(com.aqtx.app.R.id.user_profile_toggle);
        switchButton.setCheck(initState);
        switchButton.setOnChangedListener(onChangedListener);
        switchButton.setTag(key);

        toggleLayout.addView(vp);

        if (toggleStateMap == null) {
            toggleStateMap = new HashMap<>();
        }
        toggleStateMap.put(key, initState);
        return switchButton;
    }

    private void updateAlias(boolean isFriend) {
        if (isFriend) {
            aliasLayout.setVisibility(View.VISIBLE);
            aliasLayout.findViewById(com.aqtx.app.R.id.arrow_right).setVisibility(View.VISIBLE);
            Friend friend = FriendDataCache.getInstance().getFriendByAccount(account);
            if (friend != null && !TextUtils.isEmpty(friend.getAlias())) {
                nickText.setVisibility(View.VISIBLE);
                nameText.setText(friend.getAlias());
                nickText.setText("昵称：" + NimUserInfoCache.getInstance().getUserName(account));
            } else {
                nickText.setVisibility(View.GONE);
                nameText.setText(NimUserInfoCache.getInstance().getUserName(account));
            }
        } else {
            aliasLayout.setVisibility(View.GONE);
            aliasLayout.findViewById(com.aqtx.app.R.id.arrow_right).setVisibility(View.GONE);
            nickText.setVisibility(View.GONE);
            nameText.setText(NimUserInfoCache.getInstance().getUserName(account));
        }
    }

    private SwitchButton.OnChangedListener onChangedListener = new SwitchButton.OnChangedListener() {
        @Override
        public void OnChanged(View v, final boolean checkState) {
            final String key = (String) v.getTag();
            if (!NetworkUtil.isNetAvailable(UserProfileActivity.this)) {
                Toast.makeText(UserProfileActivity.this, com.aqtx.app.R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
                if (key.equals(KEY_BLACK_LIST)) {
                    blackSwitch.setCheck(!checkState);
                } else if (key.equals(KEY_MSG_NOTICE)) {
                    noticeSwitch.setCheck(!checkState);
                }
                return;
            }

            updateStateMap(checkState, key);

            if (key.equals(KEY_BLACK_LIST)) {
                if (checkState) {
                    NIMClient.getService(FriendService.class).addToBlackList(account).setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            Toast.makeText(UserProfileActivity.this, "加入黑名单成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailed(int code) {
                            if (code == 408) {
                                Toast.makeText(UserProfileActivity.this, com.aqtx.app.R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(UserProfileActivity.this, "on failed：" + code, Toast.LENGTH_SHORT).show();
                            }
                            updateStateMap(!checkState, key);
                            blackSwitch.setCheck(!checkState);
                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
                } else {
                    NIMClient.getService(FriendService.class).removeFromBlackList(account).setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            Toast.makeText(UserProfileActivity.this, "移除黑名单成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailed(int code) {
                            if (code == 408) {
                                Toast.makeText(UserProfileActivity.this, com.aqtx.app.R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(UserProfileActivity.this, "on failed:" + code, Toast.LENGTH_SHORT).show();
                            }
                            updateStateMap(!checkState, key);
                            blackSwitch.setCheck(!checkState);
                        }

                        @Override
                        public void onException(Throwable exception) {

                        }
                    });
                }
            } else if (key.equals(KEY_MSG_NOTICE)) {
                NIMClient.getService(FriendService.class).setMessageNotify(account, checkState).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        if (checkState) {
                            Toast.makeText(UserProfileActivity.this, "开启消息提醒成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UserProfileActivity.this, "关闭消息提醒成功", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        if (code == 408) {
                            Toast.makeText(UserProfileActivity.this, com.aqtx.app.R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UserProfileActivity.this, "on failed:" + code, Toast.LENGTH_SHORT).show();
                        }
                        updateStateMap(!checkState, key);
                        noticeSwitch.setCheck(!checkState);
                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });
            }
        }
    };

    private void updateStateMap(boolean checkState, String key) {
        if (toggleStateMap.containsKey(key)) {
            toggleStateMap.put(key, checkState);  // update state
            Log.i(TAG, "toggle " + key + "to " + checkState);


        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == addFriendBtn) {
                if (FLAG_ADD_FRIEND_DIRECTLY) {
                    doAddFriend(null, true);  // 直接加为好友
                } else {
                    onAddFriendByVerify(); // 发起好友验证请求
                }
            } else if (v == removeFriendBtn) {
                onRemoveFriend();
            } else if (v == chatBtn) {
                onChat();
            }
        }
    };

    /**
     * 通过验证方式添加好友
     */
    private void onAddFriendByVerify() {
        final EasyEditDialog requestDialog = new EasyEditDialog(this);
        requestDialog.setEditTextMaxLength(32);
        requestDialog.setTitle(getString(com.aqtx.app.R.string.add_friend_verify_tip));
        requestDialog.addNegativeButtonListener(com.aqtx.app.R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestDialog.dismiss();
            }
        });
        requestDialog.addPositiveButtonListener(com.aqtx.app.R.string.send, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestDialog.dismiss();
                String msg = requestDialog.getEditMessage();
                doAddFriend(msg, false);
            }
        });
        requestDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });
        requestDialog.show();
    }

    private void doAddFriend(String msg, boolean addDirectly) {
        if (!NetworkUtil.isNetAvailable(this)) {
            Toast.makeText(UserProfileActivity.this, com.aqtx.app.R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
            return;
        }


        if (!TextUtils.isEmpty(account) && account.equals(DemoCache.getAccount())) {
            Toast.makeText(UserProfileActivity.this, "不能加自己为好友", Toast.LENGTH_SHORT).show();
            return;
        }
        final VerifyType verifyType = addDirectly ? VerifyType.DIRECT_ADD : VerifyType.VERIFY_REQUEST;
        DialogMaker.showProgressDialog(this, "", true);
        // TODO: 2016/12/7 添加好友
        Map<String, String> map = new HashMap<>();
        map.put("accid", DemoCache.getAccount());
        map.put("faccid", account);
        map.put("type", "2");
        map.put("msg", msg);
        HttpManager.getInstance().post(ContantValue.MAKE_FRIEND, map, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                DialogMaker.dismissProgressDialog();
                Toast.makeText(UserProfileActivity.this, "网络有误，添加失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response, int id) {
                JSONObject jsonObject = JSON.parseObject(response);
                if (jsonObject.getString("code").equals("200")) {
                    updateUserOperatorView();
                    if (VerifyType.DIRECT_ADD == verifyType) {
                        Toast.makeText(UserProfileActivity.this, "添加好友成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(UserProfileActivity.this, "添加好友请求发送成功", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UserProfileActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                }
                DialogMaker.dismissProgressDialog();

            }
        });
//        OkHttpUtils.get().addParams("accid", DemoCache.getAccount())
//                .addParams("faccid", account)
//                .addParams("type", "2")
//                .addParams("msg", msg).url(ContantValue.MAKE_FRIEND).build().execute(new StringCallback() {
//            @Override
//            public void onError(Call call, Exception e, int id) {
//                DialogMaker.dismissProgressDialog();
//                Toast.makeText(UserProfileActivity.this, "网络有误，添加失败", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onResponse(String response, int id) {
//                JSONObject jsonObject = JSON.parseObject(response);
//                if (jsonObject.getString("code").equals("200")) {
//                    updateUserOperatorView();
//                    if (VerifyType.DIRECT_ADD == verifyType) {
//                        Toast.makeText(UserProfileActivity.this, "添加好友成功", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(UserProfileActivity.this, "添加好友请求发送成功", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(UserProfileActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
//                }
//                DialogMaker.dismissProgressDialog();
//
//            }
//        });
//        NIMClient.getService(FriendService.class).addFriend(new AddFriendData(account, verifyType, msg))
//                .setCallback(new RequestCallback<Void>() {
//                    @Override
//                    public void onSuccess(Void param) {
//                        DialogMaker.dismissProgressDialog();
//                        updateUserOperatorView();
//                        if (VerifyType.DIRECT_ADD == verifyType) {
//                            Toast.makeText(UserProfileActivity.this, "添加好友成功", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(UserProfileActivity.this, "添加好友请求发送成功", Toast.LENGTH_SHORT).show();
//
//
//                        }
//                    }
//
//                    @Override
//                    public void onFailed(int code) {
//                        DialogMaker.dismissProgressDialog();
//                        if (code == 408) {
//                            Toast.makeText(UserProfileActivity.this, com.aqtx.app.R.string.network_is_not_available, Toast
//                                    .LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(UserProfileActivity.this, "on failed:" + code, Toast
//                                    .LENGTH_SHORT).show();
//                        }
//                    }
//
//                    @Override
//                    public void onException(Throwable exception) {
//                        DialogMaker.dismissProgressDialog();
//                    }
//                });

        Log.i(TAG, "onAddFriendByVerify");
    }

    private void onRemoveFriend() {
        Log.i(TAG, "onRemoveFriend");
        if (!NetworkUtil.isNetAvailable(this)) {
            Toast.makeText(UserProfileActivity.this, com.aqtx.app.R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
            return;
        }
        // TODO: 2016/12/10 删除好友

        EasyAlertDialog dialog = EasyAlertDialogHelper.createOkCancelDiolag(this, getString(com.aqtx.app.R.string.remove_friend),
                getString(com.aqtx.app.R.string.remove_friend_tip), true,
                new EasyAlertDialogHelper.OnDialogActionListener() {

                    @Override
                    public void doCancelAction() {

                    }

                    @Override
                    public void doOkAction() {
                        DialogMaker.showProgressDialog(UserProfileActivity.this, "", true);
                        Map<String,String> map=new HashMap<String, String>();
                        map.put("accid", DemoCache.getAccount());
                        map.put("faccid", account);
                        HttpManager.getInstance().post(ContantValue.DELETE_FRIEND, map, new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                DialogMaker.dismissProgressDialog();
//                                if (code == 408) {
                                Toast.makeText(UserProfileActivity.this, com.aqtx.app.R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
//                                } else {
//                                    Toast.makeText(UserProfileActivity.this, "on failed:" + code, Toast.LENGTH_SHORT).show();
//                                }
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                JSONObject jsonObject = JSON.parseObject(response);
                                if (jsonObject.getString("code").equals("200")) {
                                    Toast.makeText(UserProfileActivity.this, com.aqtx.app.R.string.remove_friend_success, Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(UserProfileActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                                }
                                DialogMaker.dismissProgressDialog();

                            }
                        });
//                        OkHttpUtils.get().url(ContantValue.DELETE_FRIEND)
//                                .addParams("accid", DemoCache.getAccount())
//                                .addParams("faccid", account).build().execute(new StringCallback() {
//                            @Override
//                            public void onError(Call call, Exception e, int id) {
//                                DialogMaker.dismissProgressDialog();
////                                if (code == 408) {
//                                Toast.makeText(UserProfileActivity.this, com.aqtx.app.R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
////                                } else {
////                                    Toast.makeText(UserProfileActivity.this, "on failed:" + code, Toast.LENGTH_SHORT).show();
////                                }
//                            }
//
//                            @Override
//                            public void onResponse(String response, int id) {
//                                JSONObject jsonObject = JSON.parseObject(response);
//                                if (jsonObject.getString("code").equals("200")) {
//                                    Toast.makeText(UserProfileActivity.this, com.aqtx.app.R.string.remove_friend_success, Toast.LENGTH_SHORT).show();
//                                    finish();
//                                } else {
//                                    Toast.makeText(UserProfileActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
//                                }
//                                DialogMaker.dismissProgressDialog();
//
//                            }
//                        });

//                        NIMClient.getService(FriendService.class).deleteFriend(account).setCallback(new RequestCallback<Void>() {
//                            @Override
//                            public void onSuccess(Void param) {
//                                DialogMaker.dismissProgressDialog();
//                                Toast.makeText(UserProfileActivity.this, com.aqtx.app.R.string.remove_friend_success, Toast.LENGTH_SHORT).show();
//                                finish();
//                            }
//
//                            @Override
//                            public void onFailed(int code) {
//                                DialogMaker.dismissProgressDialog();
//                                if (code == 408) {
//                                    Toast.makeText(UserProfileActivity.this, com.aqtx.app.R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
//                                } else {
//                                    Toast.makeText(UserProfileActivity.this, "on failed:" + code, Toast.LENGTH_SHORT).show();
//                                }
//                            }
//
//                            @Override
//                            public void onException(Throwable exception) {
//                                DialogMaker.dismissProgressDialog();
//                            }
//                        });
                    }
                });
        if (!isFinishing() && !isDestroyedCompatible()) {
            dialog.show();
        }
    }

    private void onChat() {
        Log.i(TAG, "onChat");
        SessionHelper.startP2PSession(this, account);
    }
}
