package com.n0499010.fypbeacon;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.EstimoteSDK;
import com.estimote.sdk.Region;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.n0499010.fypbeacon.Global.OFFER_ACCESSORIES;
import static com.n0499010.fypbeacon.Global.OFFER_FOOTWEAR;
import static com.n0499010.fypbeacon.Global.OFFER_RETURN;
import static com.n0499010.fypbeacon.Global.OFFER_WELCOME;
import static com.n0499010.fypbeacon.Global.OFFER_WISHLIST;
import static com.n0499010.fypbeacon.Global.firebaseRootRef;
import static com.n0499010.fypbeacon.Global.getActivity;
import static com.n0499010.fypbeacon.Global.mBeaconDataMap;
import static com.n0499010.fypbeacon.Global.mFirebaseAuth;
import static com.n0499010.fypbeacon.Global.mFirebaseUser;
import static com.n0499010.fypbeacon.Global.mOfferMap;
import static com.n0499010.fypbeacon.Global.mUser;
import static com.n0499010.fypbeacon.Global.notifyIntent;
import static com.n0499010.fypbeacon.Global.offersRef;
import static com.n0499010.fypbeacon.Global.scanDurInterval;
import static com.n0499010.fypbeacon.Global.scanWaitInterval;
import static com.n0499010.fypbeacon.Global.userRef;

/**
 * Created by N0499010 Shannon Hibbett on 06/03/2017
 * Source Code extended from Estimote Android Tutorial (part 2) :
 * http://developer.estimote.com/android/tutorial/part-2-background-monitoring/
 */

/* Application: Base class for those who need to maintain global application state.
*  Required for managing Beacons from any Activity in the app. */
public class MyApplication extends Application {

    SharedPreferences preferences;
    SharedPreferences.OnSharedPreferenceChangeListener prefListener;

    private static final String TAG = "MyApplication";
    private String beaconKey;

    private long tStart = 0;
    private long elapsedNanoSeconds = 0;
    private long tEnd = 0;
    private long exitDelay = 30;

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
        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        final Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        final Intent nearbyOffersIntent = new Intent(getApplicationContext(), NearbyProductsActivity.class);

        /* Estimote SDK Initialization : */
        EstimoteSDK.initialize(getApplicationContext(), Global.appID, Global.appToken);
        EstimoteSDK.enableDebugLogging(true);   // Optional, debug logging.

        /*  iBeacon Monitoring :    */
        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setBackgroundScanPeriod(scanDurInterval, scanWaitInterval);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    mUser.setuID(mFirebaseUser.getUid());
                    preferences = getSharedPreferences(mUser.getuID(), getApplicationContext().MODE_PRIVATE);

                    // check database, if uID exists, set newUser to false :
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if ( dataSnapshot.hasChild(mUser.getuID()) ) {
                                preferences.edit().putBoolean("newUser", false).commit();

                                // check each child under user's offer node, set SharedPreferences accordingly :
                                if ( dataSnapshot.child(mUser.getuID()).hasChild("offers") ) {
                                    for (DataSnapshot child : dataSnapshot.child(mUser.getuID()).child("offers").getChildren()) {
                                        preferences.edit().putBoolean(child.getKey(), true).commit();
                                    }
                                }
                                // ^allows for portability of account across devices to avoid unecessary New Offer alerts
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.w("Failed to read value.", databaseError.toException());
                            //TODO: Database onCancelled error handling
                        }
                    });

                    if (preferences.getBoolean("newUser", true)) {
                        preferences.edit().putBoolean(OFFER_WISHLIST, false).commit();
                        preferences.edit().putBoolean(OFFER_RETURN, false).commit();
                        preferences.edit().putBoolean(OFFER_WELCOME, false).commit();
                        preferences.edit().putBoolean(OFFER_FOOTWEAR, false).commit();
                        preferences.edit().putBoolean(OFFER_ACCESSORIES, false).commit();

                        preferences.edit().putBoolean("newUser", false).commit();
                    }

                    // User signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + mFirebaseUser.getUid());

                    beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
                        @Override
                        public void onServiceReady() {
                            beaconManager.startMonitoring(regionAll);
                            beaconManager.startMonitoring(regionBeetroot);
                            beaconManager.startMonitoring(regionLemon);
                            beaconManager.startMonitoring(regionCandy);
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
                                Global.showNotification(
                                        getString(R.string.notify_welcome),                             // Title
                                        getString(R.string.notify_welcome_content), // Message
                                        nearbyOffersIntent,                                 // Notification Intent
                                        getApplicationContext(),                            // Context
                                        Global.NOTIFICATION_PRODUCT                           // Notification Type
                                );
                            } else {
                                // Note time when region entered
                                tStart = System.nanoTime();
                                BeaconData beaconData = new BeaconData(beaconKey, region);
                                beaconData.settStart(tStart);
                                mBeaconDataMap.put(beaconData.getMmKey(), beaconData);

                                Intent overviewIntent = new Intent(getApplicationContext(), OverviewActivity.class);
                                overviewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                //  Provide region's beacon Major:Minor key
                                overviewIntent.putExtra("beaconKey", beaconKey);

                                //  If app open in foreground, but not on Overview, launch new Overview Activity on top :
                                if (getActivity() != null
                                        && getActivity().getClass() != OverviewActivity.class
                                        && getActivity().getClass() != NearbyProductsActivity.class) {

                                    startActivity(overviewIntent);

                                } else {
                                    Global.showNotification(
                                            getString(R.string.notify_product_nearby),
                                            getString(R.string.notify_product_nearby_content),
                                            overviewIntent,
                                            getApplicationContext(),
                                            Global.NOTIFICATION_PRODUCT
                                    );
                                }
                            }
                        }   //!OnEnteredRegion

                        @Override
                        public void onExitedRegion(Region region) {
                            Log.d("monitoring: exit", region.toString());

                            if (region == regionAll) {
                                notifyIntent = Global.onShareChooser(getApplicationContext());
                                Global.showNotification(
                                        getString(R.string.notify_exit),
                                        getString(R.string.notify_exit_content),
                                        notifyIntent,
                                        getApplicationContext(),
                                        Global.NOTIFICATION_PRODUCT
                                );
                            } else {
                                // get duration of visit
                                beaconKey = String.format("%d:%d", region.getMajor(), region.getMinor());
                                BeaconData beaconData = mBeaconDataMap.get(beaconKey);

                                elapsedNanoSeconds = System.nanoTime() - tStart;
                                beaconData.setElapsedTime(elapsedNanoSeconds);
                                // Record user's beacon visit in database :
                                recordBeaconVisit(beaconKey);
                            }
                        }
                    }); //!setMonitoringListener

                    checkWishlist();
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    beaconManager.disconnect();
                }
            }
        });

        // Populate offer criteria container with offer details from database
        DatabaseReference offerRef = firebaseRootRef.child("offers");
        offerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (!(child.getKey().equals("criteria"))) {
                        Offer offer = new Offer(child.getKey(), (String) child.getValue());
                        mOfferMap.offerMap.put(child.getKey(), offer);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /* Get Value from database for provided beacon key, if no value use default */
    private Map<String, String> updateNoVisits(String key, final Map<String, String> dataset, DataSnapshot dataSnapshot) {

        String visits = dataset.get("noVisits");

        dataSnapshot = dataSnapshot.child("beaconVisited").child(key).child("noVisits");
        if (dataSnapshot.getValue() != null) {
            visits = dataSnapshot.getValue().toString();
        }
        dataset.put("noVisits", visits);

        int visitsInt = Integer.parseInt(dataset.get("noVisits"));
        visitsInt++;
        String updatedVisits = String.valueOf(visitsInt);

        dataset.put("noVisits", updatedVisits);

        return dataset;
    }

    private Map<String, String> updateTimeSpent(String key, Map<String, String> dataset, DataSnapshot dataSnapshot) {
        String timeSpent = dataset.get("timeSpent");
        // Get timespent from database
        dataSnapshot = dataSnapshot.child("beaconVisited").child(key).child("timeSpent");
        if (dataSnapshot.getValue() != null) {
            timeSpent = dataSnapshot.getValue().toString();
        }
        dataset.put("timeSpent", timeSpent);

        long timeSpentLong = Long.parseLong(timeSpent);
        //double timeDouble = Double.parseDouble(timeSpent);

        // Calculate elapsed time :
        BeaconData beaconData = mBeaconDataMap.get(key);
        //long tDeltatSeconds = (tDelta / 1000);
        long tDeltatSeconds = TimeUnit.NANOSECONDS.toSeconds(beaconData.getElapsedTime());
        // Estimote beacons have a built in delay of 30 seconds for exit events, subtract to get actual duration
        if (tDeltatSeconds >= 0) {
            tDeltatSeconds -= exitDelay;
        }
        long cumulativeTime = (timeSpentLong + tDeltatSeconds);

        String updatedTimeSpent = String.valueOf(cumulativeTime);

        beaconData.setTimeSpent(updatedTimeSpent);

        dataset.put("timeSpent", updatedTimeSpent);

        return dataset;
    }

    public void checkOfferCriteriaMet(String key) {
        final String bKey = key;

        final DatabaseReference criteriaRef = offersRef.child("criteria");

        criteriaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (Map.Entry<String, String> userEntry : mUser.getBeaconsVisited().entrySet()) {
                    // For each user beaconVisited data entry
                    if (dataSnapshot.hasChild(userEntry.getKey())) {
                        DataSnapshot ref = dataSnapshot.child(userEntry.getKey());

                        for (DataSnapshot child : ref.getChildren()) {
                            if (child.getValue().equals(userEntry.getValue())) {
                                // visit value meets criteria value, update users offers :
                                // get category using beacon key
                                BeaconData beaconData = mBeaconDataMap.get(bKey);
                                String cat = beaconData.getCategory();
                                if (cat != null) {
                                    // create offerID using 'OF_' prefix
                                    String offID = "OF_" + cat;
                                    // get user's current offer list
                                    List<String> offList = mUser.getOfferList();

                                    try {
                                        if ( offList == null ) {
                                            offList = new ArrayList<String>();
                                        }
                                        offList.add(offID); // append to
                                        // set updated offer list
                                        mUser.setOfferList(offList);

                                    } catch (NullPointerException nullPounterException){
                                        Log.w(nullPounterException.toString(), nullPounterException);
                                    }

                                    // write offer list to db
                                    DatabaseReference userOffersRef = userRef.child(mUser.getuID()).child("offers");
                                    userOffersRef.child(offID).setValue("true");

                                    if (preferences.getBoolean(offID, false) == false) {
                                        final Intent myOffersIntent = new Intent(getApplicationContext(), MyOffersActivity.class);
                                        Global.showNotification(
                                                getString(R.string.notification_newoffer_title) + offID,
                                                getString(R.string.notification_newoffer_content),
                                                myOffersIntent,
                                                getApplicationContext(),
                                                Global.NOTIFICATION_OFFER
                                        );

                                        preferences.edit().putBoolean(offID, true).commit();
                                    }
                                }
                            }
                        }
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Failed to read value.", databaseError.toException());
                //TODO: Database onCancelled error handling
            }
        });
    }

    public void checkWishlist() {
        final DatabaseReference mUserRef = userRef.child(mUser.getuID());
        final DatabaseReference userOffersRef = mUserRef.child("offers");

        userOffersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (child.getKey().equals(OFFER_WISHLIST)) {
                        final Intent myOffersIntent = new Intent(getApplicationContext(), MyOffersActivity.class);

                        if (preferences.getBoolean(OFFER_WISHLIST, false) == false) {
                            Global.showNotification(
                                    getString(R.string.notification_newoffer_title) + OFFER_WISHLIST,
                                    getString(R.string.notification_newoffer_content),
                                    myOffersIntent,
                                    getApplicationContext(),
                                    Global.NOTIFICATION_OFFER
                            );
                            preferences.edit().putBoolean(OFFER_WISHLIST, true).commit();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void recordBeaconVisit(String key) {
        final String bKey = key;
        // Record user's beacon visit in database :
        final DatabaseReference mUserRef = userRef.child(mUser.getuID());

        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                DatabaseReference mBeaconVisitRef = mUserRef.child("beaconVisited");
                mBeaconVisitRef.child(bKey);

                Map<String, String> beaconVisitedData = new HashMap<String, String>();

                beaconVisitedData.put("noVisits", "0");
                beaconVisitedData = updateNoVisits(bKey, beaconVisitedData, dataSnapshot);

                int delay = (int) exitDelay;
                beaconVisitedData.put("timeSpent", String.valueOf(delay));
                beaconVisitedData = updateTimeSpent(bKey, beaconVisitedData, dataSnapshot);

                mBeaconVisitRef.child(bKey).setValue(beaconVisitedData);

                mUser.setBeaconsVisited(beaconVisitedData);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Failed to read value.", databaseError.toException());
                //TODO: Database onCancelled error handling
            }
        });

        // Check against offer criteria
        checkOfferCriteriaMet(bKey);
    }
}
