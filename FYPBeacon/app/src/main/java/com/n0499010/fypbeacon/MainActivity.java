package com.n0499010.fypbeacon;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.eddystone.Eddystone;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.n0499010.fypbeacon.Global.ANONYMOUS;
import static com.n0499010.fypbeacon.Global.itemRef;
import static com.n0499010.fypbeacon.Global.mFirebaseAuth;
import static com.n0499010.fypbeacon.Global.mFirebaseUser;
import static com.n0499010.fypbeacon.Global.mGoogleApiClient;
import static com.n0499010.fypbeacon.Global.mPhotoUrl;
import static com.n0499010.fypbeacon.Global.mSharedPreferences;
import static com.n0499010.fypbeacon.Global.mUid;
import static com.n0499010.fypbeacon.Global.mUser;
import static com.n0499010.fypbeacon.Global.mUsername;
import static com.n0499010.fypbeacon.Global.userRef;
import static com.n0499010.fypbeacon.MyApplication.regionAll;

//import static com.n0499010.fypbeacon.Global.beaconManager;
//import static com.n0499010.fypbeacon.Global.regionAll;
//import static com.n0499010.fypbeacon.MyApplication.beaconManager;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    //TODO: have the beacon manager in the application class manipulate a data model,
    //TODO: and the Activity observing the changes to the data model instead.

    //  Declare UI elements :
    private ListView mListView;
    private Button button;

    //  Declare Estimote SDK Beacon elements:
    public static BeaconManager beaconManager;
    private String scanId;

    private static final Map<String, List<String>> PLACES_BY_BEACONS;

    //TODO: query db - get data and map beaconMM to list of items
    private static ArrayList<String> itemArrayList = new ArrayList<String>();
    /* List nested inside a map. The map maps beacon’s <major>:<minor> string) to
     * the list of names of items, pre-sorted by starting with the nearest ones. */
    static {
        Map<String, List<String>> itemsByBeacons = new HashMap<>();
        itemsByBeacons.put("18129:1432", new ArrayList<String>() {{
            // "Black Boots") is closest to the beacon with major 18129 and minor 1432 (beetroot)
            add("Black Boots");
            // "Pretty Bangle" is the next closest
            add("Pretty Bangle");
            // "Pastel Heels" is the furthest away
            add("Pastel Heels");
        }});
        itemsByBeacons.put("28651:37405", new ArrayList<String>() {{
            add("Pretty Bangle");
            add("Pastel Heels");
            add("Black Boots");
        }});
        itemsByBeacons.put("17236:25458", new ArrayList<String>() {{
            add("Pastel Heels");
            add("Pretty Bangle");
            add("Black Boots");
        }});
        PLACES_BY_BEACONS = Collections.unmodifiableMap(itemsByBeacons);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mListView = (ListView) findViewById(R.id.listView_items);
        button = (Button) findViewById(R.id.btn);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), ItemListActivity.class);
                startActivity(intent);
            }
        });

        FirebaseListAdapter<String> firebaseListAdapter = new FirebaseListAdapter<String>(
                this,
                String.class,
                android.R.layout.simple_list_item_1,
                itemRef
        ) {
            @Override
            protected void populateView(View v, String model, int position) {

                TextView textView = (TextView) v.findViewById(android.R.id.text1);
                textView.setText(model);
            }
        };

        mListView.setAdapter(firebaseListAdapter);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();


        beaconManager = new BeaconManager(getApplicationContext());

    ////////////////////////////////////////////////////////////////////////////////////////////////

        final ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, itemArrayList);
        mListView.setAdapter(adapter);

        ////////////////////////////////////////////////////////////////////////////////////////////
        /*  Eddystone   */
        beaconManager.setEddystoneListener(new BeaconManager.EddystoneListener() {
            @Override
            public void onEddystonesFound(List<Eddystone> eddystones) {

                Log.d("Eddystone", "Nearby Eddystone beacons: " + eddystones);
            }
        });
        ////////////////////////////////////////////////////////////////////////////////////////////
        /*  iBeacon */

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {

                if (!list.isEmpty()) {

                    Beacon nearestBeacon = list.get(0);     // List already ordered nearest first
                    List<String> places = Global.itemsNearBeacon(nearestBeacon, PLACES_BY_BEACONS);

                    Log.d("Store", "Nearest places: " + places);

                    // Update UI:
                    adapter.clear();
                    adapter.addAll(places);
                    adapter.notifyDataSetChanged();
                }
            }
        });


        mUsername = ANONYMOUS;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
            return;
        } else {
            // User authenticated
            mUid = mFirebaseUser.getUid();
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }

            mSharedPreferences.edit().putBoolean("authenticated", true).apply();

            //TODO: Call initialiseApp method

            //TODO: Call initialiseAccount method
            initialiseAccount(mUid);
        }
    }

    public void initialiseApp() {
        //TODO: initApp method

        // Create offer objects from database
    }

    public void initialiseAccount(String userId) {
        //TODO: initAcc method

        mUser.setuID(userId);

        // Read database, lookup Uid to see if already exists :
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mUser.getuID())) {
                    // if Uid already exists, add 'OF_Return' offer:

                    // add as key under user's 'offer's node, set value to 'true'
                    Map<String,String> userData = new HashMap<String, String>();
                    userData.put("OF_Return", "true");

                    DatabaseReference mUserRef = userRef;
                    mUserRef = userRef.child(mUser.getuID()).child("offers");
                    mUserRef.child("OF_Return").setValue("true");
                } else {
                    // if Uid not present, write value to db as new key under 'user' as root node
                    Map<String,String> userData = new HashMap<String, String>();
                    //TODO: get offers from database, save to global offer list on sign-in
                    userData.put("OF_Welcome", "true");

                    DatabaseReference mUserRef = userRef;
                    // Add new UID, under Uid node, add 'offers' node
                    mUserRef = userRef.child(mUser.getuID()).child("offers");
                    // Add new offer under offers node: Key: OF_Welcome, value "true"
                    mUserRef.setValue(userData);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override public void onServiceReady() {
                scanId = beaconManager.startEddystoneScanning();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        beaconManager.stopEddystoneScanning(scanId);
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* The checkWithDefaultDialogs Helper method will use default system dialogs
        to turn Bluetooth or Location on, ask for ACCESS_COARSE_LOCATION permission. */
        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(regionAll);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(regionAll);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        beaconManager.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* takes a Beacon object representing the closest beacon, and
     * return a list of all the objects sorted by their distance to the beacon */
//    private List<String> itemsNearBeacon(Beacon beacon) {
//
//        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());
//
//        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
//            return PLACES_BY_BEACONS.get(beaconKey);
//        }
//        return Collections.emptyList();
//    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d("MainActivity", "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}