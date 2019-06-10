package com.example.bwp070.foursquaresdk;

import android.content.Context;

public class SDKPermissionSharedPreferences {

    // プリファレンス名
    public static final String SDK_PERMISSION_PREF_NAME = "SDK_permission_data";

    // サービス開始・停止ステータスのキー.
    private static final String SDK_SERVICE_START_FLAG = "SDK_service_start_flag";

    /**
     * <pre>
     * 起動状態: SDK開始/停止フラグの取得.
     * デフォルト:false
     * </pre>
     *
     * @param context
     *         コンテキスト
     * @return {@code true} SDK検知起動中, {@code false} SDK検知停止中
     */
    public static boolean getSdkStartFlag(final Context context) {
        return SDKSharedPreferences.getBooleanPreference(context, SDK_PERMISSION_PREF_NAME, SDK_SERVICE_START_FLAG);
    }

    /**
     * 起動状態: SDK開始フラグの保存.
     *
     * @param context
     *         コンテキスト
     */
    public static void setSdkStartFlag(final Context context) {
        SDKSharedPreferences.setPreference(context, SDK_PERMISSION_PREF_NAME, SDK_SERVICE_START_FLAG, true);
    }

    /**
     * 起動状態: SDK停止フラグの保存.
     *
     * @param context
     *         コンテキスト
     */
    public static void setSdkStopFlag(final Context context) {
        SDKSharedPreferences.setPreference(context, SDK_PERMISSION_PREF_NAME, SDK_SERVICE_START_FLAG, false);
    }

}
