package com.n0499010.fypbeacon;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.estimote.sdk.SystemRequirementsChecker;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* The checkWithDefaultDialogs Helper method will use default system dialogs
        to turn Bluetooth or Location on, ask for ACCESS_COARSE_LOCATION permission. */
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }
}
