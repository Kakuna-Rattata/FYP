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
import com.firebase.ui.database.FirebaseListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.n0499010.fypbeacon.Global.itemRef;
import static com.n0499010.fypbeacon.Global.mFirebaseAuth;
import static com.n0499010.fypbeacon.MyApplication.regionAll;

public class NearbyProducts extends AppCompatActivity {

    //  Declare UI elements :
    private ListView mListView;
    private Button button;

    public static BeaconManager beaconManager;

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
        setContentView(R.layout.activity_nearby_offers);

        if (mFirebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, SignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        } else {

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

            beaconManager = new BeaconManager(getApplicationContext());

            final ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, itemArrayList);
            mListView.setAdapter(adapter);

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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.sign_out_menu:
//                mFirebaseAuth.signOut();
//                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
//                mUsername = ANONYMOUS;
//
//                startActivity(new Intent(this, SignInActivity.class));
//
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
}
