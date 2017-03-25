package com.aqtx.app;

import android.content.Context;
import android.support.v4.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by y11621546 on 2016/11/30.
 */

public class CustomProvider extends ActionProvider {
    private Context mContext;

    /**
     * Creates a new instance.
     *
     * @param context Context for accessing resources.
     */
    public CustomProvider(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    public View onCreateActionView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.menu_badge_provider, null);
        TextView textView = (TextView) view.findViewById(R.id.tv_badge);
        textView.setText("asdfsaf");
        return view;
    }
}
