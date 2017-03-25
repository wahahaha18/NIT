package com.aqtx.app;

import com.chad.library.adapter.base.entity.SectionEntity;

/**
 * Created by y11621546 on 2016/12/9.
 */

public class ContactSection extends SectionEntity<Cate.ListBean> {

    public ContactSection(boolean isHeader, String header) {
        super(isHeader, header);
    }

    public ContactSection(Cate.ListBean listBean) {
        super(listBean);
    }

}
