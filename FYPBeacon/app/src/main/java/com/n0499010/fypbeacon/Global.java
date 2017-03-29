package com.n0499010.fypbeacon;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Shannon Hibbett (N0499010) on 29/03/2017.
 *
 * Code developed during previous Mobile Platform Development module assignment reused:
 * showNotification method, getActivity method
 *
 * TODO: find & cite getActivity method source
 */

public class Global {

    /*  App Values for Global Access :  */

    /*  Firebase Database : */
    public static final String FIREBASE_URL = "https://beacon-fyp-project.firebaseio.com/";

    public static final DatabaseReference firebaseRootRef = FirebaseDatabase.getInstance().getReferenceFromUrl(FIREBASE_URL);
    public static final DatabaseReference itemRef = firebaseRootRef.child("item");
    public static final DatabaseReference beaconRef = firebaseRootRef.child("beacon");

    /*  Estimote API :   */
    public static final String appID = "fyp-beacon-app-koo";
    public static final String appToken = "0e25759a226c115f659245992c0b97ee";

    public static BeaconManager beaconManager;

    public static long scanDurInterval = 5000;
    public static long scanWaitInterval = 3000;


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



    public static Intent notifyIntent;

    /*  App Global Access Methods :    */
    public static void showNotification(String title, String message, Intent notificationIntent, Context context) {

        if ( notificationIntent != null ) {

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivities(
                    context, 0, new Intent[] {notificationIntent}, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new Notification.Builder(context)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    //.setSmallIcon(R.drawable.g_logo)  //TODO: Add app notification icon
                    .build();
            notification.defaults |= Notification.DEFAULT_SOUND;
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, notification);
        }
    }

    /* takes a Beacon object representing the closest beacon,
     * return a list of all the objects sorted by their distance to the beacon */
    public static List<String> itemsNearBeacon(Beacon beacon, Map<String, List<String>> stringListMap) {

        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());

        if (stringListMap.containsKey(beaconKey)) {
            return stringListMap.get(beaconKey);
        }
        return Collections.emptyList();
    }

    public static Activity getActivity() {
        Class activityThreadClass = null;
        try {
            activityThreadClass = Class.forName("android.app.ActivityThread");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Object activityThread = null;
        try {
            activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        Field activitiesField = null;
        try {
            activitiesField = activityThreadClass.getDeclaredField("mActivities");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        activitiesField.setAccessible(true);

        Map<Object, Object> activities = null;
        try {
            activities = (Map<Object, Object>) activitiesField.get(activityThread);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if(activities == null)
            return null;

        for (Object activityRecord : activities.values()) {
            Class activityRecordClass = activityRecord.getClass();
            Field pausedField = null;
            try {
                pausedField = activityRecordClass.getDeclaredField("paused");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            pausedField.setAccessible(true);
            try {
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
