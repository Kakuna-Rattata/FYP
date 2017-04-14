package com.n0499010.fypbeacon;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.n0499010.fypbeacon.Global.beaconRef;
import static com.n0499010.fypbeacon.Global.mFirebaseAuth;
import static com.n0499010.fypbeacon.MyApplication.regionAll;

public class NearbyProductsActivity extends AppCompatActivity {

    private ListView mListView;
    private TextView textViewMsg;

    public static BeaconManager beaconManager;

    private static Map<String, List<String>> PLACES_BY_BEACONS = new HashMap<>();
    private static ArrayList<String> itemArrayList = new ArrayList<String>();
    /* List nested inside a map. The map maps beacon’s <major>:<minor> string) to
     * the list of names of items, pre-sorted by starting with the nearest ones. */
    private static Map<String, List<String>> itemsByBeacons = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_products);

        // Only allow user access if authenticated
        if (mFirebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, SignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        } else {
            mListView   = (ListView) findViewById(R.id.listView_items);
            textViewMsg = (TextView) findViewById(R.id.textView_nearbyMsg);

            beaconRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // For each beacon
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        // add key and ordered values to itemsByBeacons
                        final DataSnapshot beaconKeyNode = dataSnapshot.child(child.getKey());
                        itemsByBeacons.put(child.getKey(), new ArrayList<String>() {{
                            add(beaconKeyNode.child("1").getValue().toString());
                            add(beaconKeyNode.child("2").getValue().toString());
                            add(beaconKeyNode.child("3").getValue().toString());
                        }});
                        PLACES_BY_BEACONS = Collections.unmodifiableMap(itemsByBeacons);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            textViewMsg.setText("");
            final ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_listview, itemArrayList);
            mListView.setAdapter(arrayAdapter);

            beaconManager = new BeaconManager(getApplicationContext());
            beaconManager.setRangingListener(new BeaconManager.RangingListener() {
                @Override
                public void onBeaconsDiscovered(Region region, List<Beacon> list) {

                    if (!list.isEmpty()) {
                        Beacon nearestBeacon = list.get(0);     // List already ordered nearest first
                        List<String> places = Global.itemsNearBeacon(nearestBeacon, PLACES_BY_BEACONS);
                        Log.d("Store", "Nearest places: " + places);

                        // Update UI:
                        arrayAdapter.clear();
                        arrayAdapter.addAll(places);
                        arrayAdapter.notifyDataSetChanged();
                        textViewMsg.setText("");
                    }
                }
            });

            if (arrayAdapter.isEmpty()) {
                textViewMsg.setText(R.string.nearbyproducts_emptylist_msg);
            }
        }
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
}
