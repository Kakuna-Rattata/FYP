package com.n0499010.fypbeacon;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;

import com.estimote.sdk.Beacon;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Shannon Hibbett (N0499010) on 29/03/2017.
 *
 * Some code developed during previous Mobile Platform Development module assignment has been reused:
 * showNotification method, getActivity method
 *
 * getActivity() method code source :
 * http://stackoverflow.com/questions/11411395/how-to-get-current-foreground-activity-context-in-android/28423385#28423385
 *
 * image used in launcher/notification icon 'ic_launcher' source :
 *  http://megaicons.net/iconspack-178/5420/
 *  Website used to generate launcher/notification icon 'ic_launcher' :
 *  https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html#foreground.type=image&foreground.space.trim=1&foreground.space.pad=0.2&foreColor=rgba(255%2C%20255%2C%20255%2C%200.97)&backColor=rgb(84%2C%20214%2C%20150)&crop=0&backgroundShape=circle&effects=none&name=ic_launcher
 */

public class Global {

    /**  App Values for Global Access :  **/
    public static final String OFFER_WELCOME        = "OF_Welcome";
    public static final String OFFER_RETURN         = "OF_Return";
    public static final String OFFER_WISHLIST       = "OF_Wishlist";
    public static final String OFFER_FOOTWEAR       = "OF_Footwear";
    public static final String OFFER_ACCESSORIES    = "OF_Accessories";

    /*  Firebase Database : */
    public static final String FIREBASE_URL = "https://beacon-fyp-project.firebaseio.com/";

    public static final DatabaseReference firebaseRootRef   = FirebaseDatabase.getInstance().getReferenceFromUrl(FIREBASE_URL);
    public static final DatabaseReference beaconRef         = firebaseRootRef.child("beacon");
    public static final DatabaseReference offersRef         = firebaseRootRef.child("offers");
    public static final DatabaseReference userRef           = firebaseRootRef.child("user");

    public static final FirebaseStorage storageInstance = FirebaseStorage.getInstance();

    public static GoogleApiClient mGoogleApiClient;

    public static FirebaseAuth mFirebaseAuth;
    public static FirebaseUser mFirebaseUser;

    public static String mUid;
    public static String mUsername;
    public static final String ANONYMOUS = "anonymous";
    public static String mPhotoUrl;

    public static User mUser = new User();

    public static Map<String,Offer> offers;

    public static OfferMap mOfferMap = new OfferMap();

    public static Map<String, BeaconData> mBeaconDataMap = new HashMap<String, BeaconData>();

    /*  Estimote API :   */
    public static final String appID = "fyp-beacon-app-koo";
    public static final String appToken = "0e25759a226c115f659245992c0b97ee";

    public static long scanDurInterval = 5000;
    public static long scanWaitInterval = 3000;


    public static final int NOTIFICATION_PRODUCT = 1;
    public static final int NOTIFICATION_OFFER = 2;

    public static Intent notifyIntent;

    /*  App Global Access Methods :    */
    public static void showNotification(
            String title, String message, Intent notificationIntent, Context context, int notificationID) {

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
                    .setSmallIcon(R.drawable.ic_launcher)
                    .build();
            notification.defaults |= Notification.DEFAULT_SOUND;
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            notificationManager.notify(notificationID, notification);
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

    public static Intent onShareChooser(Context context) {
        Resources resources = context.getResources();

        Intent emailIntent = new Intent();
        emailIntent.setAction(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {resources.getString(R.string.send_intent_email_to)});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.send_intent_email_subject));
        emailIntent.putExtra(Intent.EXTRA_TEXT, resources.getString(R.string.app_feedback_text));
        emailIntent.setType("message/rfc822");

        PackageManager pm = context.getPackageManager();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");

        Intent openInChooser = Intent.createChooser(emailIntent, resources.getString(R.string.share_chooser_text));

        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();

        for (int i = 0; i < resInfo.size(); i++) {
            // Extract the label, append it, and repackage it in a LabeledIntent
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;
            if (packageName.contains("android.email")) {
                emailIntent.setPackage(packageName);
            }
        }

        // convert intentList to array
        LabeledIntent[] extraIntents = intentList.toArray( new LabeledIntent[ intentList.size() ]);

        openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);

        notifyIntent = new Intent();
        notifyIntent = openInChooser;

        notifyIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);

        return openInChooser;
    }

    public static void onShareClick(Context context) {

        Intent openInChooser = onShareChooser(context);

        context.startActivity(openInChooser);
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
