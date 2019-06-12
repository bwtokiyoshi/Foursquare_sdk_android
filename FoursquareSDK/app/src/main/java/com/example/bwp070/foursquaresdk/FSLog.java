package com.example.bwp070.foursquaresdk;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.Request;
import okhttp3.Response;

/**
 * <pre>
 * ログ出力を取り纏めたクラス.
 * 基本的にログ出力を行う場合、各実装でandroid.util.Logを直接呼ばずにこのクラスを必ず経由してください.
 * </pre>
 */
public class FSLog {

    /** デフォルトタグ. */
    private static final String DEFAULT_TAG = "PilgrimSdk";

    /** LogCatの書き込み制御. */
    public static final boolean IS_LOGCAT_LOGGING = true;

    /** LogCatのファイルの書き込み制御デフォルトはオフ. */
    public static boolean IS_LOGCAT_FILE_LOGGING = false;

    /** ファイルで保存する場合に、ファイルの保存先. */
    public static String LOGCAT_FILE_DIR;

    /** アプリへのデバッグログレベル通知制御 */
    // アプリ側から制御させたい
    public static final int FS_DEBUG_LOG_LEVEL = 1;

    /**
     * ログ保存プリファレンス名
     */
    public static final String FS_SDK_LOG_PREF = "fs_sdk_log_pref";

    /**
     * <pre>
     * debug用：ファイルでログキャットを初期化します。
     * </pre>
     *
     * @param context コンテキスト
     */
    public static void initFileLogCat(final Context context) {
        // ログキャットフラグ
        if (!IS_LOGCAT_LOGGING) {
            return;
        }

        // 初期化済みの場合に、2回目初期化しない。
        if (IS_LOGCAT_FILE_LOGGING) {
            return;
        }

        // ログのSDカードの保存権限のチェック
        ApplicationInfo ai = null;
        try {
            ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            android.util.Log.i(DEFAULT_TAG, "Can not get meta data.");
            return;
        }
        if ((null == ai) || (null == ai.metaData)) {
            android.util.Log.i(DEFAULT_TAG, "ai is null or meta data.");
            return;
        }

        String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
        int res = context.checkCallingOrSelfPermission(permission);
        if (res != PackageManager.PERMISSION_GRANTED) {
            android.util.Log.i(DEFAULT_TAG, "no permission to " + "save to external storage.");
            return;
        }

        IS_LOGCAT_FILE_LOGGING = true;
        LOGCAT_FILE_DIR = Environment.getExternalStorageDirectory() + "/debug/log/" + context.getPackageName() + "/";
    }

    /**
     * アプリケーションに情報通知が発生した場合にロギングする.
     *
     * @param message
     *            メッセージ
     */
    public static void i(final String message) {
        if ((FS_DEBUG_LOG_LEVEL <= 1) && IS_LOGCAT_LOGGING) {
            android.util.Log.i(DEFAULT_TAG, DEFAULT_TAG + ":" + message);
            appendLog(DEFAULT_TAG + ":" + message);
        }
    }

    /**
     * アプリケーションにDEBUG情報が発生した場合にロギングする.
     *
     * @param message
     *            メッセージ
     */
    public static void d(final String message) {
        if ((FS_DEBUG_LOG_LEVEL <= 2) && IS_LOGCAT_LOGGING) {
            android.util.Log.d(DEFAULT_TAG, DEFAULT_TAG + ":" + message);
            appendLog(DEFAULT_TAG + ":" + message);
        }
    }

    /**
     * アプリケーションに潜在的な問題なエラーが発生した場合にロギングする.
     *
     * @param message
     *            メッセージ
     */
    public static void w(final String message) {
        if ((FS_DEBUG_LOG_LEVEL <= 3) && IS_LOGCAT_LOGGING) {
            android.util.Log.w(DEFAULT_TAG, DEFAULT_TAG + ":" + message);
            appendLog(DEFAULT_TAG + ":" + message);
        }
    }

    /**
     * アプリケーションに致命的なエラーが発生した場合にロギングする.
     *
     * @param message
     *            メッセージ
     */
    public static void e(final String message) {
        if ((FS_DEBUG_LOG_LEVEL <= 4) && IS_LOGCAT_LOGGING) {
            android.util.Log.e(DEFAULT_TAG, message);
            appendLog(DEFAULT_TAG + ":" + message);
        }
    }

    /**
     * SDCardへログを保存する.
     *
     * @param text
     */
    public static void appendLog(final String text) {
        if (!IS_LOGCAT_FILE_LOGGING) {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ", Locale.JAPAN);
        String message = sdf.format(new Date()) + text;
        try {
            // BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = getBufferedWriter();
            if (null != buf) {
                buf.append(message);
                buf.newLine();
                buf.flush();
                buf.close();
            }
        } catch (IOException e) {
            Log.e(DEFAULT_TAG, "IOException", e);
        }
    }

    /**
     * bufferedWriterを取得するメソッド.
     *
     * @return bufferedWriter.
     * @throws IOException
     *             IOエクセプション.
     */
    private static BufferedWriter getBufferedWriter() {
        if (null == LOGCAT_FILE_DIR) {
            return null;
        }

        String fileName = "fs_logcat.txt";
        File outputFile = new File(LOGCAT_FILE_DIR, fileName);
        if (!outputFile.exists()) {
            outputFile.getParentFile().mkdirs();
        }

        if (outputFile.exists() && (outputFile.length() > (2 * 1024 * 1024))) {
            File to = new File(LOGCAT_FILE_DIR, "fs_logcat_" + System.currentTimeMillis() + ".txt");
            outputFile.renameTo(to);
            outputFile = new File(LOGCAT_FILE_DIR, fileName);
        }

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(outputFile, true));
        } catch (FileNotFoundException e) {
            Log.i(DEFAULT_TAG, LOGCAT_FILE_DIR + " FileNotFoundException", e);
        } catch (IOException e) {
            Log.i(DEFAULT_TAG, LOGCAT_FILE_DIR + " IOException", e);
        }

        return bw;
    }
}
