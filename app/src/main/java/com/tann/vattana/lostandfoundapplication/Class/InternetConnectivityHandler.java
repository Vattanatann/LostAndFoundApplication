package com.tann.vattana.lostandfoundapplication.Class;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetConnectivityHandler {

    public static boolean haveNetworkConnection (Context context) {
        boolean haveWifi = false;
        boolean haveMobileData = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfos = cm.getActiveNetworkInfo();

        if (networkInfos != null) {
            if (networkInfos.getType() == cm.TYPE_WIFI) {
                haveWifi = true;
            } else if (networkInfos.getType() == cm.TYPE_MOBILE) {
                haveMobileData = true;
            }
        }
        return haveWifi || haveMobileData;
    }

}
