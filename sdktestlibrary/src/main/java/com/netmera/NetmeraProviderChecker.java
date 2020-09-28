package com.netmera;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

class NetmeraProviderChecker {
    NetmeraProviderChecker() {
    }

    static boolean isGoogleApiAvailable(Context context) {
        return isPackageInstalled(context, "com.google.android.gms");
    }

    static final boolean isPackageInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (NameNotFoundException var3) {
            return false;
        }
    }
}