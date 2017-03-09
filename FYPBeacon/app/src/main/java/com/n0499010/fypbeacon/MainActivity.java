package com.n0499010.fypbeacon;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.eddystone.Eddystone;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //TODO: have the beacon manager in the application class manipulate a data model,
    //TODO: and the Activity observing the changes to the data model instead.

    //  Declare Firebase Database elements:
    private static final String FIREBASE_URL = "https://beacon-fyp-project.firebaseio.com/";

    private DatabaseReference mFirebaseRootRef = FirebaseDatabase.getInstance().getReferenceFromUrl(FIREBASE_URL);
    private DatabaseReference mItemRef = mFirebaseRootRef.child("item");

    //  Declare UI elements :
    private ListView mListView;
    //private ItemListAdapter mItemListAdapter;
    //private String mName;
    private Button button;

    //  Declare Estimote SDK Beacon elements:
    private BeaconManager beaconManager;
    private String scanId;
    private Region region;

    // query db - get data and map beaconMM to list of items


    private static final Map<String, List<String>> PLACES_BY_BEACONS;
    private static ArrayList<String> itemArrayList = new ArrayList<String>();
    /* List nested inside a map. The map maps beacon’s <major>:<minor> string) to
     * the list of names of objects, pre-sorted by starting with the nearest ones. */
    static {
        Map<String, List<String>> itemsByBeacons = new HashMap<>();
        itemsByBeacons.put("18129:1432", new ArrayList<String>() {{
            add("Pastel Heels");
            // "Pastel Heels" is closest to the beacon with major 18129 and minor 1432
            add("Black Boots");
            // "Green & Green Salads" is the next closest
            add("Pretty Bangle");
            // "Mini Panini" is the furthest away
        }});
        itemsByBeacons.put("17236:25458", new ArrayList<String>() {{
            add("Black Boots");
            add("Pretty Bangle");
            add("Pastel Heels");
        }});
        PLACES_BY_BEACONS = Collections.unmodifiableMap(itemsByBeacons);
    }

    /* takes a Beacon object representing the closest beacon, and
     * return a list of all the objects sorted by their distance to the beacon */
    private List<String> itemsNearBeacon(Beacon beacon) {

        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());

        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
            return PLACES_BY_BEACONS.get(beaconKey);
        }
        return Collections.emptyList();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.btn);
        mListView = (ListView) findViewById(R.id.listView_items);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, ItemListActivity.class);
                startActivity(intent);
            }
        });

        FirebaseListAdapter<String> firebaseListAdapter = new FirebaseListAdapter<String>(
                this,
                String.class,
                android.R.layout.simple_list_item_1,
                mItemRef
        ) {
            @Override
            protected void populateView(View v, String model, int position) {

                TextView textView = (TextView) v.findViewById(android.R.id.text1);
                textView.setText(model);
            }
        };

        mListView.setAdapter(firebaseListAdapter);

////////////////////////////////////////////////////////////////////////////////////////////

        final ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, itemArrayList);
        mListView.setAdapter(adapter);

        beaconManager = new BeaconManager(this);
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

        region = new Region("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                null,   // Major and Minor values null to target all beacons
                null);

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0); // List already ordered nearest first
                    List<String> places = itemsNearBeacon(nearestBeacon);

                    Log.d("Store", "Nearest places: " + places);

                    // Update UI:
                    adapter.clear();
                    adapter.addAll(places);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mItemRef.child("1").child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String text = dataSnapshot.getValue(String.class);
                Log.d("text", text);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        beaconManager.disconnect();
    }
}