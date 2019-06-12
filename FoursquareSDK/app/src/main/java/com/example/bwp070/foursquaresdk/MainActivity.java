package com.example.bwp070.foursquaresdk;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.foursquare.api.FoursquareLocation;
import com.foursquare.api.types.Segment;
import com.foursquare.api.types.UserStateList;
import com.foursquare.api.types.Venue;
import com.foursquare.api.types.geofence.GeofenceEvent;
import com.foursquare.pilgrim.Confidence;
import com.foursquare.pilgrim.CurrentLocation;
import com.foursquare.pilgrim.FrequentLocations;
import com.foursquare.pilgrim.LocationType;
import com.foursquare.pilgrim.LogLevel;
import com.foursquare.pilgrim.PilgrimNotificationHandler;
import com.foursquare.pilgrim.PilgrimSdk;
import com.foursquare.pilgrim.PilgrimSdkBackfillNotification;
import com.foursquare.pilgrim.PilgrimSdkGeofenceEventNotification;
import com.foursquare.pilgrim.PilgrimSdkVisitNotification;
import com.foursquare.pilgrim.PilgrimUserInfo;
import com.foursquare.pilgrim.Result;
import com.foursquare.pilgrim.Visit;

import java.util.List;

import static android.icu.text.Normalizer.YES;

public class MainActivity extends AppCompatActivity {

    private SnapToPlaceOpenHelper snapToPlaceHelper;
    private ListView listView;
    private TextView textView;

    private final CompoundButton.OnCheckedChangeListener toggleChangeListener = new CompoundButton.OnCheckedChangeListener() {
        /**
         * ToggleボタンChecked変更イベントリスナー
         */
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            SDKSharedPreferences.setSdkDetectChecked(getApplicationContext(), isChecked);
            setSdkDetect(isChecked);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ログキャット初期化
        FSLog.initFileLogCat(getApplicationContext());

        //　Pilgrim SDKを設定する
        PilgrimSdk.Builder builder = new PilgrimSdk.Builder(this)
                // ClientID, ClientSecret
                .consumer("Q1XC5HXE4Y0YD4JUD0UB4HZBWBVFUCLT2DDZ2ZGZEQPJD4MK", "OLKWEKOG0ANFF1ZKNFFQJCKUXHPEWV243XCQSUHV1N05AFUU")
                .notificationHandler(pilgrimNotificationHandler)
                .logLevel(LogLevel.DEBUG);
        PilgrimSdk.with(builder);

        // DB作成
        snapToPlaceHelper = new SnapToPlaceOpenHelper(getApplicationContext());



        // パーミッションの許可を取得する
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000); //Manifest.permission.ACCESS_COARSE_LOCATION

        //　現在地を取得
        getCurrentLocation();

        // トグルボタンにSDK検知ON/OFFを設定
        setSdkDetectToggleButton(SDKManager.isServiceStarted(getApplicationContext()));
    }

    /**
     * SDK検知を開始・停止を行う
     *
     * @param isChecked
     *         {@code true} SDK検知開始, {@code false} SDK検知停止
     */
    private void setSdkDetect(final boolean isChecked) {
        if(isChecked) {
            // (起動状態: SDK起動中)の場合
            if (SDKPermissionSharedPreferences.getSdkStartFlag(getApplicationContext())) {
                FSLog.d("setSdkDetect: SDKはすでに起動中です");
                return;
            }
            FSLog.d("setSdkDetect: Pilgrim SDKを起動");
            PilgrimSdk.start(getApplicationContext());
            // (起動状態: SDK検知起動)フラグ保存
            SDKPermissionSharedPreferences.setSdkStartFlag(getApplicationContext());

        } else {
            // (起動状態: SDK停止中)の場合
            if (!SDKPermissionSharedPreferences.getSdkStartFlag(getApplicationContext())) {
                FSLog.d("setSdkDetect: SDKはすでに停止中です");
                return;
            }
            FSLog.d("setSdkDetect: Pilgrim SDKを停止");
            PilgrimSdk.stop(getApplicationContext());
            // (起動状態: SDK検知停止)フラグ保存
            SDKPermissionSharedPreferences.setSdkStopFlag(getApplicationContext());
        }
    }

    /**
     * SDK検知Toggleボタンを設定する.
     *
     * @param isChecked
     *         {@code true} SDK検知開始, {@code false} SDK検知停止
     */
    private void setSdkDetectToggleButton(final boolean isChecked) {
        ToggleButton tb = (ToggleButton) findViewById(R.id.sdk_toggleButton);
        tb.setChecked(isChecked);
        tb.setOnCheckedChangeListener(this.toggleChangeListener);
    }


    private final PilgrimNotificationHandler pilgrimNotificationHandler = new PilgrimNotificationHandler() {
        // Primary visit handler
        // arriveとdepartのイベント受け取る訪問handle
        @Override
        public void handleVisit(Context context, PilgrimSdkVisitNotification notification) {
            // Process the visit however you'd like:
            Visit visit = notification.getVisit();
            FSLog.d("handleVisit: " + visit.toString());

            saveSnapToPlaceData("タイトル", "100");


            // デバイスがarriveした時のタイムスタンプをms単位で取得
            Long arrive = visit.getArrival();
            FSLog.d("handleVisit_arrive: " + arrive.toString());

            // デバイスがarriveした時のタイムスタンプをms単位で取得
            Confidence confidence = visit.getConfidence();
            FSLog.d("handleVisit_confidence: " + confidence.toString());

            // デバイスがdepartした時のタイムスタンプをms単位で取得
            Long departure = visit.getDeparture();
            FSLog.d("handleVisit_departure: " + departure.toString());

            //　visitが作成された場所を取得
            FoursquareLocation location = visit.getLocation();
            FSLog.d("handleVisit_location: " + location.toString());

            // Pilgrimコンソールの設定を有効にして、通知内でユーザーの周囲の場所を取得した場合は、ここでそれらを取得できる。
            List<Venue> otherPossibleVenues = visit.getOtherPossibleVenues();
            FSLog.d("handleVisit_otherPossibleVenues: " + otherPossibleVenues.toString());

            // 現在のvisitIDを取得
            String pilgrimVisitID = visit.getPilgrimVisitId();
            FSLog.d("handleVisit_pilgrimVisitID: " + pilgrimVisitID.toString());

            // ユーザーセグメントを取得するためにPilgrimコンソールの設定を有効にしている場合は、ここでそれらを取得できる。
            List<Segment> segments = visit.getSegments();
            FSLog.d("handleVisit_segments: " + segments.toString());

            // visitのタイプを取得
            LocationType type = visit.getType();
            FSLog.d("handleVisit_type: " + type.toString());

            //
            UserStateList userStates = visit.getUserStates();
            FSLog.d("handleVisit_userStatus: " + userStates.toString());

            //　デバイスが存在する場所の開催地オブジェクト
            Venue venue = visit.getVenue();
            FSLog.d("handleVisit_venue: " + venue.toString());

            //　デバイスがvisitになった時間をms単位で取得
            Long visitLength = visit.getVisitLength();
            FSLog.d("handleVisit_visitLength: " + visitLength.toString());

            //　デバイスがこの場所を離れたかどうかを取得
            Boolean departed = visit.hasDeparted();
            FSLog.d("handleVisit_departed: " + departed.toString());

            // hashCode
            int hashCode = visit.hashCode();
            FSLog.d("handleVisit_hashCode: " + hashCode);

            // 訪問のデータが、イベントがすでに発生した後にSDKがあなたとvisitの完全な情報を通信している「埋め戻し」から来ているかどうかを知ることができる
            Boolean backfill = visit.isBackfill();
            FSLog.d("handleVisit_backfill: " + backfill.toString());
        }


        // Optional: If visit occurred while in Doze mode or without network connectivity
        // ネットワーク接続失敗、または失敗した訪問が再試行された時に発生した訪問を受信
        // 到着の場合、「departureTime」はnull
        @Override
        public void handleBackfillVisit(Context context, PilgrimSdkBackfillNotification notification) {
            // Process the visit however you'd like:
            super.handleBackfillVisit(context, notification);
            Visit visit = notification.getVisit();
            Venue venue = visit.getVenue();
            FSLog.d("handleBackfillVisit: " + visit.toString());
            //Toast.makeText(context , "handleBackfillVisit", Toast.LENGTH_LONG).show();
        }

        // Optional: If visit occurred by triggering a geofence
        // ジオフェンスの訪問を受け取る
        @Override
        public void handleGeofenceEventNotification(Context context, PilgrimSdkGeofenceEventNotification notification) {
            // Process the geofence events however you'd like:
            List<GeofenceEvent> geofenceEvents = notification.getGeofenceEvents();
            for (GeofenceEvent geofenceEvent : geofenceEvents) {
                FSLog.d("handleGeofenceEventNotification: " + geofenceEvent.toString());
            }
            FSLog.d("----");
            //Toast.makeText(context , "handleGeofenceEventNotification", Toast.LENGTH_LONG).show();
        }

//        @Override
//        public void handlePlaceNotification(Context context, PilgrimSdkPlaceNotification notif) {
//            CurrentPlace currentPlace = notif.getCurrentPlace();
//            List<Venue> otherVenues = currentPlace.getOtherPossibleVenues();
//            // Do something with the alternate venues
//        }
    };

    /**
     * Snap-to-Place画面ボタンを表示する.
     *
     * @param view
     *         {@link View}
     */
    public void onClickSnapToPlaceButton(final View view) {
        Intent intent = new Intent(MainActivity.this, SnapToPlaceTopActivity.class);
        startActivity(intent);
    }

    /**
     * ジオフェンス画面ボタンを表示する.
     *
     * @param view
     *         {@link View}
     */
    public void onClickGeofenceButton(final View view) {
        Intent intent = new Intent(MainActivity.this, GeofenceTopActivity.class);
        startActivity(intent);
    }

    /**
     * データを保存する.
     * @param
     */
    public void saveSnapToPlaceData(String title, String score) {
        SQLiteDatabase db = snapToPlaceHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("title", title);
        values.put("score", score);

        db.insert("foursquaredb", null, values);
    }

    //現在地取得
    public void getCurrentLocation () {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    @SuppressLint("MissingPermission") Result<CurrentLocation, Exception> currentLocationResult = PilgrimSdk.get().getCurrentLocation();
                    if (currentLocationResult.isOk()) {
                        final CurrentLocation currentLocation = currentLocationResult.getResult();

                        FSLog.d("getCurrentLocation: Currently at " + currentLocation.getCurrentPlace().toString() + " and inside " + currentLocation.getMatchedGeofences().size() + " geofence(s)");

                        // 変数textViewに表示するテキストビューのidを格納
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView = findViewById(R.id.current_location_id);
                                textView.setText(currentLocation.getCurrentPlace().toString());
                            }
                        });

                    } else {
                        FSLog.e("getCurrentLocation: " + currentLocationResult.getErr().getMessage());
                        //Log.e("PilgrimSdk", "getCurrentLocation: " + currentLocationResult.getErr().getMessage(), currentLocationResult.getErr());

                    }
                }
            }).start();
        }
    }
}
