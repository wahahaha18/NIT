package com.aqtx.app.contact.activity;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
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
import com.aqtx.app.RegisterActivity;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.widget.ClearableEditTextWithIcon;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.friend.FriendServiceObserve;
import com.netease.nimlib.sdk.friend.constant.VerifyType;
import com.netease.nimlib.sdk.friend.model.FriendChangedNotify;
import com.netease.nimlib.sdk.msg.SystemMessageObserver;
import com.netease.nimlib.sdk.msg.SystemMessageService;
import com.netease.nimlib.sdk.msg.model.SystemMessage;
import com.netease.nimlib.sdk.team.TeamService;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;

import static android.R.id.message;

/**
 * 添加好友页面
 * Created by huangjun on 2015/8/11.
 */
public class AddFriendActivity extends UI {
    /**
     * 获取库Phon表字段
     **/
    private static final String[] PHONES_PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Photo.PHOTO_ID, ContactsContract.CommonDataKinds.Phone.CONTACT_ID};

    /**
     * 联系人显示名称
     **/
    private static final int PHONES_DISPLAY_NAME_INDEX = 0;

    /**
     * 电话号码
     **/
    private static final int PHONES_NUMBER_INDEX = 1;

    /**
     * 头像ID
     **/
    private static final int PHONES_PHOTO_ID_INDEX = 2;
    private ContactAdapter adater;

    /**
     * 联系人的ID
     **/
    private static final int PHONES_CONTACT_ID_INDEX = 3;
    private SearchView mSearchView;
    private SearchView.SearchAutoComplete mEdit;
    private View layoutEmpty;
    private View layoutList;
    private Button btnJumpContact;
    private Contact contact;

    /**
     * 联系人
     **/
    private ArrayList<String[]> mContactsName = new ArrayList<String[]>();
    private List<Contact> list;

    //---------------------------------------------------//
//    private ClearableEditTextWithIcon searchEdit;
    private ClearableEditTextWithIcon searchEdit;
    //    通讯录列表
    private RecyclerView rvFriend;

    public static final void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, AddFriendActivity.class);
        context.startActivity(intent);
    }

    private void setSearchView() {
        final float density = getResources().getDisplayMetrics().density;
        mSearchView.setIconified(false);
//        mSearchView.setIconifiedByDefault(false);
        final int closeImgId = getResources().getIdentifier("search_close_btn", "id", getPackageName());
        ImageView closeImg = (ImageView) mSearchView.findViewById(closeImgId);
        if (closeImg != null) {
            LinearLayout.LayoutParams paramsImg = (LinearLayout.LayoutParams) closeImg.getLayoutParams();
            paramsImg.topMargin = (int) (-2 * density);
            closeImg.setVisibility(View.GONE);
            closeImg.setLayoutParams(paramsImg);
        }
        final int editViewId = getResources().getIdentifier("search_src_text", "id", getPackageName());
        mEdit = (SearchView.SearchAutoComplete) mSearchView.findViewById(editViewId);
        if (mEdit != null) {
            mEdit.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        }

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (TextUtils.isEmpty(query)) {
                    Toast.makeText(AddFriendActivity.this, R.string.not_allow_empty, Toast.LENGTH_SHORT).show();
                } else if (query.equals(DemoCache.getAccount())) {
                    Toast.makeText(AddFriendActivity.this, R.string.add_friend_self_tip, Toast.LENGTH_SHORT).show();
                } else {
                    mSearchView.clearFocus();
                    query(query.toLowerCase());
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
        NIMClient.getService(FriendServiceObserve.class).observeFriendChangedNotify(friendChangedNotifyObserver, true);

    }

    /*
    第三方 APP 应在 APP 启动后监听好友关系的变化，当主动添加好友成功、被添加为好友、主动删除好友成功、被对方解好友关系、好友关系更新、登录同步好友关系数据时都会收到通知：
     */
    private Observer<FriendChangedNotify> friendChangedNotifyObserver = new Observer<FriendChangedNotify>() {
        @Override
        public void onEvent(FriendChangedNotify friendChangedNotify) {
            addFriend1();
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        NIMClient.getService(SystemMessageService.class).resetSystemMessageUnreadCount();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.aqtx.app.R.layout.add_friend_activity);
        ToolBarOptions options = new ToolBarOptions();
        options.titleId = R.string.new_friend;
        setToolBar(com.aqtx.app.R.id.toolbar, options);
        findViews();
        mSearchView.setQueryHint("手机号/安信号/姓名");
        mSearchView.setIconified(false);
        mSearchView.clearFocus();
        rvFriend.setLayoutManager(new LinearLayoutManager(this));
        rvFriend.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        rvFriend.addOnItemTouchListener(new OnItemLongClickListener() {
            @Override
            public void onSimpleItemLongClick(BaseQuickAdapter baseQuickAdapter, View view, final int i) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddFriendActivity.this).setTitle("删除通知").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, final int i1) {
                        dialogInterface.dismiss();
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("accid", DemoCache.getAccount());
                        map.put("faccid", list.get(i).getFaccid());
                        map.put("field", list.get(i).getField());
                        HttpManager.getInstance().post(ContantValue.DELETE_TOMOTACHI, map, new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {

                            }

                            @Override
                            public void onResponse(String response, int id) {
                                Log.d("AddFriendActivity", response);
                                JSONObject jsonObject1 = JSON.parseObject(response);
                                if (jsonObject1.getString("code").equals("200")) {
                                    Toast.makeText(AddFriendActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                    list.remove(list.get(i));
                                    adater.notifyItemRemoved(i);
                                    adater.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(AddFriendActivity.this, jsonObject1.getString("msg"), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
//                        OkHttpUtils.get().url(ContantValue.DELETE_TOMOTACHI)
////                                .addParams("table", list.get(i).getTable())
//                                .addParams("accid", DemoCache.getAccount())
//                                .addParams("faccid", list.get(i).getFaccid())
//                                .addParams("field", list.get(i).getField()).build().execute(new StringCallback() {
//                            @Override
//                            public void onError(Call call, Exception e, int id) {
//
//                            }
//
//                            @Override
//                            public void onResponse(String response, int id) {
//                                Log.d("AddFriendActivity", response);
//                                JSONObject jsonObject1 = JSON.parseObject(response);
//                                if (jsonObject1.getString("code").equals("200")) {
//                                    Toast.makeText(AddFriendActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
//                                    list.remove(list.get(i));
//                                    adater.notifyItemRemoved(i);
//                                    adater.notifyDataSetChanged();
//                                } else {
//                                    Toast.makeText(AddFriendActivity.this, jsonObject1.getString("msg"), Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });
        rvFriend.addOnItemTouchListener(new OnItemChildClickListener() {
            @Override
            public void onSimpleItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, final int i) {
                contact = list.get(i);
                switch (view.getId()) {
                    case R.id.btn_accept:
                        if ("0".equals(list.get(i).getTid())) {
                            check(list.get(i).getFaccid(), "3");
                        } else {
                            if ("1".equals(list.get(i).getTp())) {
                                checkGroupMe(list.get(i).getTid(), list.get(i).getFaccid(), "1");
                            } else {
                                checkGroup(list.get(i).getTid(), list.get(i).getFaccid(), "1");
                            }

                        }

                        break;
                    case R.id.btn_add:
//                        query(list.get(i).getFaccid());
                        // TODO: 2016/12/7 添加好友
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("accid", DemoCache.getAccount());
                        map.put("faccid", list.get(i).getFaccid());
                        map.put("type", "2");
                        map.put("msg", "");
                        HttpManager.getInstance().post(ContantValue.MAKE_FRIEND, map, new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                DialogMaker.dismissProgressDialog();
                                Toast.makeText(AddFriendActivity.this, "网络有误，添加失败", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                JSONObject jsonObject = JSON.parseObject(response);
                                if (jsonObject.getString("code").equals("200")) {
                                    contact.setStatus("0");
                                    adater.notifyItemChanged(i);
                                } else {
                                    Toast.makeText(AddFriendActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                                }
                                DialogMaker.dismissProgressDialog();

                            }
                        });
//                        OkHttpUtils.get().addParams("accid", DemoCache.getAccount())
//                                .addParams("faccid", list.get(i).getFaccid())
//                                .addParams("type", "2")
//                                .addParams("msg", "").url(ContantValue.MAKE_FRIEND).build().execute(new StringCallback() {
//                            @Override
//                            public void onError(Call call, Exception e, int id) {
//                                DialogMaker.dismissProgressDialog();
//                                Toast.makeText(AddFriendActivity.this, "网络有误，添加失败", Toast.LENGTH_SHORT).show();
//                            }
//
//                            @Override
//                            public void onResponse(String response, int id) {
//                                JSONObject jsonObject = JSON.parseObject(response);
//                                if (jsonObject.getString("code").equals("200")) {
//                                    contact.setStatus("0");
//                                    adater.notifyItemChanged(i);
//                                } else {
//                                    Toast.makeText(AddFriendActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
//                                }
//                                DialogMaker.dismissProgressDialog();
//
//                            }
//                        });
                        break;
                    case R.id.btn_refuse:
                        if ("0".equals(list.get(i).getTid())) {
                            check(list.get(i).getFaccid(), "4");
                        } else {
                            if ("1".equals(list.get(i).getTp())) {
                                checkGroupMe(list.get(i).getTid(), list.get(i).getFaccid(), "2");
                            } else {
                                checkGroup(list.get(i).getTid(), list.get(i).getFaccid(), "2");
                            }
                        }
                        break;
                }
            }
        });

        initActionbar();
        addFriend1();


    }

    /**
     * 对方加入我的群
     *
     * @param tid
     * @param fromAccount
     * @param type
     */
    private void checkGroupMe(final String tid, String fromAccount, String type) {
        if ("1".equals(type)) {
            // 同意申请
            NIMClient.getService(TeamService.class)
                    .passApply(tid, fromAccount)
                    .setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AddFriendActivity.this, "处理成功", Toast.LENGTH_SHORT).show();
                            contact.setStatus("1");
                            adater.notifyDataSetChanged();

//
                        }

                        @Override
                        public void onFailed(int i) {

                            Toast.makeText(AddFriendActivity.this, "处理失败", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onException(Throwable throwable) {

                        }
                    });
        } else {
            // 拒绝申请，可填写理由
            NIMClient.getService(TeamService.class)
                    .rejectApply(tid, fromAccount, "")
                    .setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
//                            Toast.makeText(AddFriendActivity.this, "处理成功", Toast.LENGTH_SHORT).show();
                            // TODO: 2016/12/12  暂时不支持
                            OkHttpUtils.get().addParams("tid", tid).url(ContantValue.REFUSE_GTOUP).addParams("accid", contact.getFaccid()).addParams("faccid", contact.getAccid()).build().execute(new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e, int id) {
                                    Toast.makeText(AddFriendActivity.this, "处理失败", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onResponse(String response, int id) {
                                    JSONObject jsonObject = JSON.parseObject(response);
                                    if (jsonObject.getString("code").equals("200")) {
                                        Toast.makeText(AddFriendActivity.this, "处理成功", Toast.LENGTH_SHORT).show();
                                        contact.setStatus("2");
                                        adater.notifyDataSetChanged();
                                    }
                                }
                            });

                        }

                        @Override
                        public void onFailed(int i) {
                            Toast.makeText(AddFriendActivity.this, "处理失败", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onException(Throwable throwable) {

                        }
                    });
        }


    }


    /**
     * @param tid
     * @param fromAccount
     * @param type        1 同意加入他人的群    2拒绝加入他人的群
     */
    private void checkGroup(String tid, String fromAccount, String type) {

        if ("1".equals(type)) {
            // 接受邀请
            NIMClient.getService(TeamService.class)
                    .acceptInvite(tid, fromAccount)
                    .setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AddFriendActivity.this, "处理成功", Toast.LENGTH_SHORT).show();
                            contact.setStatus("1");
                            adater.notifyDataSetChanged();
//                            addFriend1();
                        }

                        @Override
                        public void onFailed(int i) {
                            Toast.makeText(AddFriendActivity.this, "处理失败", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onException(Throwable throwable) {

                        }
                    });
        } else {
            // 拒绝邀请,可带上拒绝理由
            NIMClient.getService(TeamService.class)
                    .declineInvite(tid, fromAccount, "")
                    .setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AddFriendActivity.this, "处理成功", Toast.LENGTH_SHORT).show();
                            contact.setStatus("2");
                            adater.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailed(int i) {
                            Toast.makeText(AddFriendActivity.this, "处理失败", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onException(Throwable throwable) {

                        }
                    });

        }


    }

    private void addFriend1() {
        OkHttpUtils.get().url(ContantValue.GET_TOMOTACHI)
                .addParams("accid", DemoCache.getAccount())
//                .addParams("mobile", JSON.toJSONString(mContactsName))
                .build().execute(new StringCallback() {
            @Override
            public void onBefore(Request request, int id) {
                super.onBefore(request, id);
//                DialogMaker.showProgressDialog(AddFriendActivity.this, null, getString(R.string.logining), true, new DialogInterface.OnCancelListener() {
//                    @Override
//                    public void onCancel(DialogInterface dialog) {
//
//                    }
//                }).setCanceledOnTouchOutside(false);
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                layoutList.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
//                DialogMaker.dismissProgressDialog();
            }

            @Override
            public void onResponse(String response, int id) {
                Log.d("AddFriendActivity111", response);
                JSONObject jsonObject = JSON.parseObject(response);
                if (jsonObject.getString("code").equals("200")) {
                    list = JSON.parseArray(jsonObject.getJSONArray("data").toJSONString(), Contact.class);
                    if (list.isEmpty()) {
                        layoutList.setVisibility(View.GONE);
                        layoutEmpty.setVisibility(View.VISIBLE);
                    } else {
                        layoutList.setVisibility(View.VISIBLE);
                        layoutEmpty.setVisibility(View.GONE);
                    }
                    adater = new ContactAdapter(list);
                    rvFriend.setAdapter(adater);
                } else {
//                    Toast.makeText(AddFriendActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                    layoutList.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                }
//                DialogMaker.dismissProgressDialog();
            }
        });
    }


    //审核
    private void check(String faccid, String type) {
        OkHttpUtils.get().addParams("accid", DemoCache.getAccount())
                .addParams("faccid", faccid)
                .addParams("type", type)
                .addParams("msg", "").url(ContantValue.MAKE_FRIEND).build().execute(new StringCallback() {
            @Override
            public void onBefore(Request request, int id) {
                super.onBefore(request, id);
                DialogMaker.showProgressDialog(AddFriendActivity.this, "", true);
            }

            @Override
            public void onError(Call call, Exception e, int id) {
                DialogMaker.dismissProgressDialog();
                Toast.makeText(AddFriendActivity.this, "网络有误，添加失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String response, int id) {
                DialogMaker.dismissProgressDialog();
                Log.d("AddFriendActivity", response);
                JSONObject jsonObject = JSON.parseObject(response);
                if (jsonObject.getString("code").equals("200")) {
//                   刷新列表
                    addFriend1();
                } else {
                    Toast.makeText(AddFriendActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void findViews() {
        searchEdit = findView(com.aqtx.app.R.id.search_friend_edit);
        layoutEmpty = findView(R.id.layout_empty);
        btnJumpContact = findView(R.id.btn_jump_contact);
        layoutList = findView(R.id.layout_list);
        mSearchView = findView(R.id.search);
        setSearchView();
        rvFriend = findView(R.id.tv_friend);
        searchEdit.setDeleteImage(com.aqtx.app.R.drawable.nim_grey_delete_icon);
        btnJumpContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddContactActivity.start(AddFriendActivity.this);
            }
        });
    }

    private void initActionbar() {
        TextView toolbarView = findView(com.aqtx.app.R.id.action_bar_right_clickable_textview);
        toolbarView.setText(com.aqtx.app.R.string.search);
        toolbarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(searchEdit.getText().toString())) {
                    Toast.makeText(AddFriendActivity.this, com.aqtx.app.R.string.not_allow_empty, Toast.LENGTH_SHORT).show();
                } else if (searchEdit.getText().toString().equals(DemoCache.getAccount())) {
                    Toast.makeText(AddFriendActivity.this, com.aqtx.app.R.string.add_friend_self_tip, Toast.LENGTH_SHORT).show();
                } else {
                    query(searchEdit.getText().toString().toLowerCase());
                }
            }
        });
    }

    private void query(String account) {
        DialogMaker.showProgressDialog(this, null, false);
//        final String account = searchEdit.getText().toString().toLowerCase();
        // TODO: 2016/11/24  通过昵称或账号添加好友
        OkHttpUtils.get().url(ContantValue.GET_USER_INFO_INDEX)
                .addParams("keyword", account)
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Toast.makeText(AddFriendActivity.this, "网络有误，请稍候重试", Toast.LENGTH_SHORT).show();
                DialogMaker.dismissProgressDialog();
            }

            @Override
            public void onResponse(String response, int id) {
                Log.d("AddFriendActivity", response);
                JSONObject jsonObject = JSON.parseObject(response);
                DialogMaker.dismissProgressDialog();
                if (jsonObject.getString("code").equals("200")) {
                    UserProfileActivity.start(AddFriendActivity.this, jsonObject.getJSONObject("data").getString("accid").toLowerCase());
                } else {
                    Toast.makeText(AddFriendActivity.this, jsonObject.getString("msg"), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
