package com.example.bwp070.foursquaresdk;

import android.content.Context;
import android.util.Log;

public class SDKManager {

    /**
     * <pre>
     * SDKの起動状態を取得するAPI.
     * </pre>
     *
     * @param context
     *         コンテキスト, not {@code null}
     * @return {@code true} 起動中, {@code false} 停止中
     */
    public static boolean isServiceStarted(final Context context) {
        try {
            return SDKPermissionSharedPreferences.getSdkStartFlag(context);
        } catch (Exception e) {
            Log.e("PilgrimSdk", "isServiceStarted:" + e.getMessage());
            return false;
        }
    }
}
