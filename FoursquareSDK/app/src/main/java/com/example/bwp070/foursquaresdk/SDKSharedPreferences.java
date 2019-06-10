package com.example.bwp070.foursquaresdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

public class SDKSharedPreferences {

    // ファイル名
    private static final String PREF_FILE_NAME = "pref_data";

    // SDK検知On/Offのキー
    private static final String SDK_DETECT_CHECKED_KEY = "sdk_detect_checked";

    /**
     * SharedPreferencesのインスタンスを取得する
     *
     * @param fileName
     *         ファイル名
     * @param context
     *         コンテキスト
     * @return SharedPreferences
     */
    private static SharedPreferences getSharedPreferencesInstance(final String fileName, final Context context) {
        return context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    /**
     * 指定したkeyのプリファレンス（boolean）を取得.
     *
     * @param context
     *            コンテキスト
     * @param pref
     *            プロファイル名
     * @param key
     *            取得するkey
     * @return 値 デフォルト値：false
     */
    public static boolean getBooleanPreference(final Context context,
                                               final String pref, final String key) {

        boolean result = false;
        if ((null == context) || TextUtils.isEmpty(pref)
                || TextUtils.isEmpty(key)) {
            // 処理終了
            Log.d("PilgrimSdk","validation check error");
        } else {
            SharedPreferences sharedPreferences = context.getSharedPreferences(
                    pref, Context.MODE_PRIVATE);
            result = sharedPreferences.getBoolean(key, false);
        }
        return result;
    }

    /**
     * booleanのプリファレンス保存処理（valueにnullを保存可能）.
     *
     * @param context
     *            コンテキスト
     * @param pref
     *            プロファイル名
     * @param key
     *            保存するKEY
     * @param value
     *            保存する値
     */
    public static void setPreference(final Context context, final String pref,
                                     final String key, final boolean value) {

        if ((null == context) || TextUtils.isEmpty(pref)
                || TextUtils.isEmpty(key)) {
            // 処理終了
            Log.d("PilgrimSdk","setPreference: validation check error");
        } else {
            SharedPreferences sharedPreferences = context.getSharedPreferences(
                    pref, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(key, value);
            editor.commit();
        }
    }


    /**
     * SDK検知実行のOn/Offを保存
     *
     * @param context
     *         コンテキスト
     * @param isSdkDetect
     *         {@code true} SDK検知On, {@code false} SDK検知Off
     */
    public static void setSdkDetectChecked(final Context context, final boolean isSdkDetect) {
        if (null == context) {
            Log.d("PilgrimSdk", "setSdkDetectChecked: null == context");
            return;
        }
        getSharedPreferencesInstance(PREF_FILE_NAME, context).edit().putBoolean(SDK_DETECT_CHECKED_KEY,
                isSdkDetect).commit();
    }

    /**
     * SDK検知実行のOn/Offを取得
     *
     * @param context
     *         コンテキスト
     * @return {@code true} SDK検知On, {@code false} SDK検知Off
     */
    public static boolean getSdkDetectChecked(final Context context) {
        if (null == context) {
            return true;
        }
        return getSharedPreferencesInstance(PREF_FILE_NAME, context).getBoolean(SDK_DETECT_CHECKED_KEY,
                true);
    }
}
