package org.sipdroid.sipua.ui;

import android.content.Context;

public class Sipdroid {
    public static final boolean release = true;
    public static final boolean market = false;

    public static boolean on(Context context) {
        return true;
    }

    public static boolean on(Context context, boolean force) {
        return true;
    }

    public static String getVersion() {
        return "7.0";
    }

    public static String getVersion(Context context) {
        return "7.0";
    }
}
