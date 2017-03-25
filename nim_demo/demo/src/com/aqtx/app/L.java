package com.aqtx.app;

import com.orhanobut.logger.Logger;

/**
 * Created by y11621546 on 2016/12/12.
 */

public class L {
    public static boolean isTest = false;

    public static void d(Object o) {
        if (isTest) {
            Logger.d(o);
        }
    }


}
