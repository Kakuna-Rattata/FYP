package com.n0499010.fypbeacon;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.n0499010.fypbeacon.Global.mFirebaseAuth;
import static com.n0499010.fypbeacon.Global.mOfferMap;
import static com.n0499010.fypbeacon.Global.mUid;
import static com.n0499010.fypbeacon.Global.mUser;
import static com.n0499010.fypbeacon.Global.userRef;

public class MyOffers extends AppCompatActivity {

    private static final String TAG = "WishlistActivity";
    private ListView listViewMyOffers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_offers);

        if (mFirebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, SignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        } else {
            listViewMyOffers = (ListView) findViewById(R.id.listView_my_offers);

            DatabaseReference mUserRef = userRef.child(mUid);
            final DatabaseReference myOffersRef = mUserRef.child("offers");

            final ArrayList<String> offDescList = new ArrayList<String>();

            myOffersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (child.getValue().equals("true")) {
                            Offer offer = mOfferMap.offerMap.get(child.getKey());

                            offDescList.add(offer.getOfferID() + ": " + offer.getOfferDesc());
                            mUser.setOfferList(offDescList);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                    (this, android.R.layout.simple_list_item_1, offDescList);

            listViewMyOffers.setAdapter(arrayAdapter);
        }
    }
}
