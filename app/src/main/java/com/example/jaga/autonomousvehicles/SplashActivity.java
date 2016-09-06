package com.example.jaga.autonomousvehicles;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SplashActivity extends AppCompatActivity {

    Button btndriver, btnrider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        GPSTracker gps = new GPSTracker(this);

        // check if GPS enabled
        if(gps.canGetLocation()){

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

        }else{
            gps.showSettingsAlert();
        }

        btnrider = (Button)findViewById(R.id.btnrider);
        btndriver= (Button)findViewById(R.id.btndriver);

        btnrider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerUtility.Iscarlogin = false;
                Intent intent = new Intent(getApplicationContext(), AuthActivity.class);
                startActivity(intent);
                finish();
            }
        });

        btndriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerUtility.Iscarlogin = true;
                Intent intent = new Intent(getApplicationContext(), AuthActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }

}
