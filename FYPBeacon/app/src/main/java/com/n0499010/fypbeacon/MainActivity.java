package com.n0499010.fypbeacon;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //TODO: have the beacon manager in the application class manipulate a data model,
    //TODO: and the Activity observing the changes to the data model instead.

    // Declare UI elements :
    private ListView mListView;

    //static String[] itemArray = {"Pastel Heels", "Black Boots", "Pretty Bangle"};

    private BeaconManager beaconManager;
    private Region region;

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
        itemsByBeacons.put("648:12", new ArrayList<String>() {{
            add("Pastel Heels");
            add("Black Boots");
            add("Pretty Bangle");
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

        mListView = (ListView) findViewById(R.id.listView_items);

        final ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, itemArrayList);
        mListView.setAdapter(adapter);

    ////////////////////////////////////////////////////////////////////////////////////////////////

        beaconManager = new BeaconManager(this);
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
}