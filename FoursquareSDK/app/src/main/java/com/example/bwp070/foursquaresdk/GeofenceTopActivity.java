package com.example.bwp070.foursquaresdk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.foursquare.pilgrim.PilgrimNotificationHandler;

public class GeofenceTopActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence_top);

        MainActivity main = new MainActivity();

    }
}
