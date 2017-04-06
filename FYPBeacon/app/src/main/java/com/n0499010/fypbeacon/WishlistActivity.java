package com.n0499010.fypbeacon;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import static com.n0499010.fypbeacon.Global.mUid;
import static com.n0499010.fypbeacon.Global.userRef;

public class WishlistActivity extends AppCompatActivity {

    private ListView listViewWishlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        listViewWishlist = (ListView) findViewById(R.id.listView_wishlist);

        DatabaseReference mUserRef = userRef.child(mUid);
        DatabaseReference wishlistRef = mUserRef.child("wishlist");

        FirebaseListAdapter<String> firebaseListAdapter = new FirebaseListAdapter<String>
                (this, String.class, android.R.layout.simple_list_item_1, wishlistRef) {

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
        listViewWishlist.setAdapter(firebaseListAdapter);
    }
}
