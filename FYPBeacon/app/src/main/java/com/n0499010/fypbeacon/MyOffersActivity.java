package com.n0499010.fypbeacon;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.n0499010.fypbeacon.Global.mFirebaseAuth;
import static com.n0499010.fypbeacon.Global.mOfferMap;
import static com.n0499010.fypbeacon.Global.mUser;
import static com.n0499010.fypbeacon.Global.userRef;

public class MyOffersActivity extends AppCompatActivity {

    private static final String TAG = "WishlistActivity";

    private ListView listViewMyOffers;
    private TextView textViewMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_offers);

        if (mFirebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, SignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        } else {
            listViewMyOffers    = (ListView) findViewById(R.id.listView_my_offers);
            textViewMsg         = (TextView) findViewById(R.id.textView_offersMsg);

            final ArrayList<String> offDescList = new ArrayList<String>();

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                    (this, android.R.layout.simple_list_item_1, offDescList);
            listViewMyOffers.setAdapter(arrayAdapter);

            textViewMsg.setText("");
            DatabaseReference mUserRef = userRef.child(mUser.getuID());
            final DatabaseReference myOffersRef = mUserRef.child("offers");
            myOffersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (child.getValue().equals("true")) {

                            if (mOfferMap.offerMap.size() > 0) {
                                Offer offer = mOfferMap.offerMap.get(child.getKey());

                                offDescList.add(offer.getOfferID() + ": " + offer.getOfferDesc());
                                mUser.setOfferList(offDescList);
                            }
                        }
                    }
                    arrayAdapter.notifyDataSetChanged();
                    textViewMsg.setText("");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w("Failed to read value.", databaseError.toException());
                    //TODO: Database onCancelled error handling
                }
            });

            if (arrayAdapter.isEmpty()) {
                textViewMsg.setText("You currently have no offers." +
                        "\n\nOffers can be obtained by regularly shopping instore while using the app!");
            } else {
                textViewMsg.setText("");
            }
        }
    }
}
