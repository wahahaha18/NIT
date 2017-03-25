package com.aqtx.app;

import java.util.List;

/**
 * Created by y11621546 on 2016/12/9.
 */

public class Cate {


    /**
     * f : #
     * list : [{"id":"11","accid":"gaoyu","faccid":"74bd467dc45a2ced3b7d8ad821bdfe88","realname":"11","phone":"131561650","name":null,"access":null,"icon":null,"status":"3"},{"id":"13","accid":"gaoyu","faccid":"crystal","realname":"22","phone":"15900282173","name":null,"access":null,"icon":null,"status":"3"}]
     */

    private String f;
    private List<ListBean> list;

    public String getF() {
        return f;
    }

    public void setF(String f) {
        this.f = f;
    }

    public List<ListBean> getList() {
        return list;
    }

    public void setList(List<ListBean> list) {
        this.list = list;
    }

    public static class ListBean {
        /**
         * id : 11
         * accid : gaoyu
         * faccid : 74bd467dc45a2ced3b7d8ad821bdfe88
         * realname : 11
         * phone : 131561650
         * name : null
         * access : null
         * icon : null
         * status : 3
         */

        private String id;
        private String accid;
        private String faccid;
        private String realname;
        private String phone;
        private Object name;
        private Object access;
        private Object icon;
        private String status;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAccid() {
            return accid;
        }

        public void setAccid(String accid) {
            this.accid = accid;
        }

        public String getFaccid() {
            return faccid;
        }

        public void setFaccid(String faccid) {
            this.faccid = faccid;
        }

        public String getRealname() {
            return realname;
        }

        public void setRealname(String realname) {
            this.realname = realname;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public Object getName() {
            return name;
        }

        public void setName(Object name) {
            this.name = name;
        }

        public Object getAccess() {
            return access;
        }

        public void setAccess(Object access) {
            this.access = access;
        }

        public Object getIcon() {
            return icon;
        }

        public void setIcon(Object icon) {
            this.icon = icon;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
