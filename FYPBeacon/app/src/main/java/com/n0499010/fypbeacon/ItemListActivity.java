package com.n0499010.fypbeacon;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.n0499010.fypbeacon.Global.beaconRef;

public class ItemListActivity extends AppCompatActivity {

    //  Declare UI elements :
    private ListView mListView;

    private Map<String, List<String>> PLACES_BY_BEACONS;

    private TreeMap<String,List<String>> itemsByBeacons = new TreeMap<>();
    private ArrayList<String> itemArrayList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        mListView = (ListView) findViewById(R.id.listView);

//        FirebaseListAdapter<String> firebaseListAdapter = new FirebaseListAdapter<String>
//                (this, String.class, android.R.layout.simple_list_item_1, itemRef.child("1")
//        ) {
//            @Override
//            protected void populateView(View v, String model, int position) {
//
//                TextView textView = (TextView) v.findViewById(android.R.id.text1);
//                textView.setText(model);
//            }
//        };
//
//        mListView.setAdapter(firebaseListAdapter);
//
//        FirebaseListAdapter<String> firebaseListAdapter1 = new FirebaseListAdapter<String>
//                (this, String.class, android.R.layout.simple_list_item_1, itemRef ) {
//
//            @Override
//            protected String parseSnapshot(DataSnapshot snapshot) {
//                return snapshot.child("name").getValue(String.class);
//            }
//
//            @Override
//            protected void populateView(View v, String s, int position) {
//                TextView menuName = (TextView)v.findViewById(android.R.id.text1);
//                menuName.setText(s);
//            }
//        };
//
//        mListView.setAdapter(firebaseListAdapter1);
        ////////////////////////////////////////////////////////////////////////////////////////////

        Query beaconQuery = beaconRef.orderByKey();
        ValueEventListener valueEventListener = beaconQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                DataSnapshot firstChildren = firstChild.getChildren().iterator().next();

                System.out.println("firstChild.getKey(): " + firstChild.getKey());
                System.out.println("firstChildren.getKey(): " + firstChildren.getKey());
                System.out.println("firstChildren.getValue(): " + firstChildren.getValue());

                final String firstBeaconItemValue = firstChildren.getValue().toString();

                for (final DataSnapshot snapshot : firstChildren.getChildren()) {
                    List<String> put = itemsByBeacons.put(
                            snapshot.getKey().toString(),
                            new ArrayList<String>() {{
                                add(snapshot.getValue().toString());
                            }});
                    //itemArrayList.add(snapshot.getValue().toString());
                }

                // Use TreeMap to sort map based on int keys (order of distance from beacon -> 1 = nearest item)
                //itemsByBeacons = new TreeMap<String, String>(itemsByBeacons);

                // Add ordered entries from treeMap to item ArrayList
                for ( Map.Entry<String,List<String>> entry : itemsByBeacons.entrySet() ) {
                    itemArrayList.add(entry.getValue().toString());
                }


                // Map Beacon to items, closest first
//                Map<String, List<String>> itemsByBeacons = new HashMap<>();
//                itemsByBeacons.put(firstChild.getKey().toString(), itemArrayList);
//
//                itemsByBeacons.put("17236:25458", itemArrayList = new ArrayList<String>() {{
//                    add("Pastel Heels");
//                    add("Pretty Bangle");
//                    add("Black Boots");
//                }});

                PLACES_BY_BEACONS = Collections.unmodifiableMap(itemsByBeacons);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //TODO: OnCancelled
            }
        });

//        FirebaseListAdapter<String> firebaseListAdapter = new FirebaseListAdapter<String>
//                (this, String.class, android.R.layout.simple_list_item_1, itemRef.child("1")
//                ) {
//            @Override
//            protected void populateView(View v, String model, int position) {
//
//                TextView textView = (TextView) v.findViewById(android.R.id.text1);
//                textView.setText(model);
//            }
//        };
//        mListView.setAdapter(firebaseListAdapter);

//        FirebaseListAdapter<String> firebaseListAdapter1 = new FirebaseListAdapter<String>
//                (this, String.class, android.R.layout.simple_list_item_1, itemRef ) {
//
//            @Override
//            protected String parseSnapshot(DataSnapshot snapshot) {
//                return snapshot.child("name").getValue(String.class);
//            }
//
//            @Override
//            protected void populateView(View v, String s, int position) {
//                TextView menuName = (TextView)v.findViewById(android.R.id.text1);
//                menuName.setText(s);
//            }
//        };
//
//        mListView.setAdapter(firebaseListAdapter1);

        FirebaseListAdapter<String> firebaseListAdapter = new FirebaseListAdapter<String>
                (this, String.class, android.R.layout.simple_list_item_1, beaconRef ) {

            @Override
            protected String parseSnapshot(DataSnapshot snapshot) {
                return snapshot.getChildren().iterator().next().getValue(String.class);
            }

            @Override
            protected void populateView(View v, String s, int position) {
                TextView menuName = (TextView)v.findViewById(android.R.id.text1);
                menuName.setText(s);
            }
        };
        mListView.setAdapter(firebaseListAdapter);

//        final ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, itemArrayList);
//        mListView.setAdapter(adapter);
//
//        /*  iBeacon Ranging */
//        beaconManager = new BeaconManager(this);
//
//        region = new Region("ranged region",
//                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
//                null,   // Major and Minor values null to target all beacons
//                null);
//        //TODO: get region data from db
//
//        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
//            @Override
//            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
//                if (!list.isEmpty()) {
//                    Beacon nearestBeacon = list.get(0); // List already ordered nearest first
//                    List<String> places = itemsNearBeacon(nearestBeacon);
//
//                    Log.d("ItemListActivity", "Nearest places: " + places);
//
//                    // Update UI:
//                    adapter.clear();
//                    adapter.addAll(places);
//                    adapter.notifyDataSetChanged();
//                }
//            }
//        });
    }
}
