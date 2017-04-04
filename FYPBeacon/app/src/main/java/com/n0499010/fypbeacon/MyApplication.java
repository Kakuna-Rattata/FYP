package com.n0499010.fypbeacon;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.Region;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.UUID;

import static com.n0499010.fypbeacon.Global.getActivity;
import static com.n0499010.fypbeacon.Global.scanDurInterval;
import static com.n0499010.fypbeacon.Global.scanWaitInterval;

/**
 * Created by N0499010 Shannon Hibbett on 06/03/2017
 * Source Code extended from Estimote Android Tutorial (part 2) :
 * http://developer.estimote.com/android/tutorial/part-2-background-monitoring/
 */

/* Application: Base class for those who need to maintain global application state.
*  Required for managing Beacons from any Activity in the app. */
public class MyApplication extends Application {

    private String beaconKey;

    public static BeaconManager beaconManager;

    /*  Estimote Region definitions for iBeacon Ranging :   */
    static final Region regionAll = new Region(
            "All beacons",
            UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
            null, null);    //  Target entire groups of beacons by setting the major and/or minor to null.
    static final Region regionBeetroot = new Region(
            "Beetroot beacon",
            UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
            18129, 1432);
    static final Region regionLemon = new Region(
            "Lemon beacon",
            UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
            28651, 37405);  // iBeacon format Major, minor values to identify particular beacon
    static final Region regionCandy = new Region(
            "Candy beacon",
            UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
            17236, 25458);

    @Override
    public void onCreate() {
        super.onCreate();

        /*  Firebase database setup : */
        if(!FirebaseApp.getApps(this).isEmpty()) { FirebaseDatabase.getInstance().setPersistenceEnabled(true); }

        /* Estimote SDK Initialization : */
        EstimoteSDK.initialize(getApplicationContext(), Global.appID, Global.appToken);
        EstimoteSDK.enableDebugLogging(true);   // Optional, debug logging.

        /*  iBeacon Monitoring :    */
        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setBackgroundScanPeriod(scanDurInterval, scanWaitInterval);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(regionAll);
            }
        });

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {

                Log.d("monitoring: enter", region.toString());
                Log.d("region identifier", region.getIdentifier());

                //  Get beacon's MajorMinor key
                beaconKey = String.format("%d:%d", region.getMajor(), region.getMinor());

                if (region == regionAll) {
                    // Display welcome notiiication when discovering any beacon :
                    showNotification(
                            "Welcome to the store",                             // Title
                            "Check out the latest app-only instore offers.",    // Message

                            MainActivity.class                                  // Context
                    );

                    beaconManager.startMonitoring(regionBeetroot);
                    beaconManager.startMonitoring(regionLemon);
                    beaconManager.startMonitoring(regionCandy);

                } else {
                    //  Trigger Overview Activity
                    Intent overviewIntent = new Intent(getApplicationContext(), OverviewActivity.class);
                    overviewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //  Provide region's beacon Major:Minor key
                    overviewIntent.putExtra("beaconKey", beaconKey);

                    //  If app open in foreground, but not on Overview, launch new Overview Activity on top :
                    if (getActivity() != null && getActivity().getClass() != OverviewActivity.class) {

                        startActivity(overviewIntent);

                    } else {

                        if (region == regionCandy) {
                            showNotification(
                                    "Exclusive deals on footwear!",
                                    "Select to view - here only!",
                                    ItemListActivity.class      //TODO: Deal page
                            );
                        }
                    }
                }
            }   //!OnEnteredRegion

            @Override
            public void onExitedRegion(Region region) {
                Log.d("monitoring: exit", region.toString());

                if (region == regionAll) {
                    showNotification(
                            "Thank you for shopping with us",
                            "Check back soon for the latest " +
                                    "app instore discounts",
                            MainActivity.class
                    );
                }
            }
        }); //!setMonitoringListener
    }

    //TODO: Use either this or Global method
    /* Add a notification to show up whenever user enters the range of monitored beacon */
    public void showNotification(String title, String message, Class intentActivityClass) {

        Intent notificationIntent = new Intent(this, intentActivityClass);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[] {notificationIntent}, PendingIntent.FLAG_UPDATE_CURRENT);

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
