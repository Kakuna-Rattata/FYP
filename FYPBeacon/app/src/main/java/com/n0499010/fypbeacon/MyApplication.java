package com.n0499010.fypbeacon;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.UUID;

/**
 * Created by N0499010 Shannon Hibbett on 06/03/2017
 * Source Code extended from Estimote Android Tutorial (part 2) :
 * http://developer.estimote.com/android/tutorial/part-2-background-monitoring/
 */

/* Application: Base class for those who need to maintain global application state.
*  Required for managing Beacons from any Activity in the app. */
public class MyApplication extends Application {

    private BeaconManager beaconManager;
    private Region regionAll;
    long scanDurInterval = 5000;
    long scanWaitInterval = 5000;


    @Override
    public void onCreate() {
        super.onCreate();

        if(!FirebaseApp.getApps(this).isEmpty()) {

            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        /*  Beacon Ranging :
         */
        regionAll = new Region("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                null,   // Major and Minor values null to target all beacons
                null);



        /*  Beacon Monitoring :
         */

        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setBackgroundScanPeriod(scanDurInterval, scanWaitInterval);   // Set enter/exit event trigger duration and wait time to 5 seconds

        // Create a beacon region defining monitoring geofence :
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new Region(
                        "monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),    // Estimote 'Beetroot' Beacon Unique ID
                        18129,  // iBeacon format Major value
                        1432    //  Minor value
                        //  Target entire groups of beacons by setting the major and/or minor to null.
                ));
            }
        });

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                showNotification(
                        "Welcome to the store", // Title
                        "Check out the latest app-only instore offers."  // Message
                );
            }

            @Override
            public void onExitedRegion(Region region) {
                showNotification(
                        "Thank you for shopping with us",
                        "Check back soon for the latest " +
                                "app instore discounts"
                );
            }
        });

    }

    /* Add a notification to show up whenever
     * user enters the range of our monitored beacon. */
    public void showNotification(String title, String message) {

        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[] {notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

}
