package com.n0499010.fypbeacon;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ItemListActivity extends AppCompatActivity {

    //  Declare Firebase Database elements:
    private static final String FIREBASE_URL = "https://beacon-fyp-project.firebaseio.com/";


    //  Declare UI elements :
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        mListView = (ListView) findViewById(R.id.listView);

        DatabaseReference mFirebaseRootRef = FirebaseDatabase.getInstance().getReferenceFromUrl(FIREBASE_URL);
        DatabaseReference mItemRef = mFirebaseRootRef.child("item");

        FirebaseListAdapter<String> firebaseListAdapter = new FirebaseListAdapter<String>
                (this, String.class, android.R.layout.simple_list_item_1, mItemRef.child("1")
        ) {
            @Override
            protected void populateView(View v, String model, int position) {

                TextView textView = (TextView) v.findViewById(android.R.id.text1);
                textView.setText(model);
            }
        };

        mListView.setAdapter(firebaseListAdapter);

        FirebaseListAdapter<String> firebaseListAdapter1 = new FirebaseListAdapter<String>
                (this, String.class, android.R.layout.simple_list_item_1, mItemRef ) {

            @Override
            protected String parseSnapshot(DataSnapshot snapshot) {
                return snapshot.child("name").getValue(String.class);
            }

            @Override
            protected void populateView(View v, String s, int position) {
                TextView menuName = (TextView)v.findViewById(android.R.id.text1);
                menuName.setText(s);
            }
        };

        mListView.setAdapter(firebaseListAdapter1);
    }
}
