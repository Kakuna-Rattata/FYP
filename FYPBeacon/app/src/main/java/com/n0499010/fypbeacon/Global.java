package com.n0499010.fypbeacon;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.estimote.sdk.Beacon;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Shannon Hibbett (N0499010) on 29/03/2017.
 *
 * Some code developed during previous Mobile Platform Development module assignment has been reused:
 * showNotification method, getActivity method
 *
 * TODO: find & cite getActivity method source
 */

public class Global {

    /*  App Values for Global Access :  */
    public static SharedPreferences mSharedPreferences;

    /*  Firebase Database : */
    public static final String FIREBASE_URL = "https://beacon-fyp-project.firebaseio.com/";
    public static final String STORAGE_RETAIL = "Retail_images";

    public static final DatabaseReference firebaseRootRef = FirebaseDatabase.getInstance().getReferenceFromUrl(FIREBASE_URL);
    public static final DatabaseReference itemRef = firebaseRootRef.child("item");
    public static final DatabaseReference beaconRef = firebaseRootRef.child("beacon");
    public static final DatabaseReference userRef = firebaseRootRef.child("user");

    public static final FirebaseStorage storageInstance = FirebaseStorage.getInstance();
//    public static final StorageReference imageRootRef = storageInstance.getReference();
//    public static final StorageReference retailImageRef = imageRootRef.child("Retail_images");

    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseUser mFirebaseUser;

    public static String mUid;
    public static String mUsername;
    public static final String ANONYMOUS = "anonymous";
    public static String mPhotoUrl;

    public static GoogleApiClient mGoogleApiClient;

    public static Map<String,Offer> offers;

    public static User mUser = new User();

    /*  Estimote API :   */
    public static final String appID = "fyp-beacon-app-koo";
    public static final String appToken = "0e25759a226c115f659245992c0b97ee";

    public static long scanDurInterval = 5000;
    public static long scanWaitInterval = 3000;


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
