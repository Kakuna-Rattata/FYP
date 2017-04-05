package com.n0499010.fypbeacon;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import static com.n0499010.fypbeacon.Global.beaconRef;
import static com.n0499010.fypbeacon.Global.mUser;
import static com.n0499010.fypbeacon.Global.storageInstance;
import static com.n0499010.fypbeacon.Global.userRef;
//import static com.n0499010.fypbeacon.Global.retailImageRef;

/**
 * Created by Shannon Hibbett (N0499010) on 29/03/2017.
 */

public class OverviewActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textViewTitle;
    private TextView textViewPrice;
    private TextView textViewDesc;
    private FloatingActionButton fabWishlist;

    String beaconKey;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        imageView = (ImageView) findViewById(R.id.imageView_overview);
        textViewTitle = (TextView) findViewById(R.id.textView_title);
        textViewPrice = (TextView) findViewById(R.id.textView_price);
        textViewDesc = (TextView) findViewById(R.id.textView_desc);
        fabWishlist = (FloatingActionButton) findViewById(R.id.fab_wishlist);

        //  Get beaconKey passed from triggering Intent :
        extras = getIntent().getExtras();
        if (extras != null) {
            beaconKey = extras.getString("beaconKey");
        }
        Log.d("beaconKey is: ", beaconKey);

        final StorageReference imageRootRef = storageInstance.getReference();
        //final StorageReference retailImageRef = imageRootRef.child(STORAGE_RETAIL);
        final StorageReference retailImageRef = imageRootRef.child("Retail_images");
        final Item item = new Item();

        //  Query database for page content to set UI :
        beaconRef.child(beaconKey).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String imgName = null, title = null, desc = null, price = null;
                if (dataSnapshot.getValue() != null) {

                    item.setKey(beaconKey);

                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (child.getKey().equals("image")) {
                            imgName = child.getValue().toString();
                            item.setImage(imgName);
                        }
                        if (child.getKey().equals("title")) {
                            title = child.getValue().toString();
                            item.setTitle(title);
                        }
                        if (child.getKey().equals("desc")) {
                            desc = child.getValue().toString();
                            item.setDesc(desc);
                        }
                        if (child.getKey().equals("price")) {
                            price = child.getValue().toString();
                            item.setPrice(price);
                        }
                    }

                    setTitle(title);

                    Glide.with(getApplicationContext())
                            .using(new FirebaseImageLoader())
                            .load(retailImageRef.child(imgName))
                            .into(imageView);

                    textViewTitle.setText(title);
                    textViewPrice.setText(price);
                    textViewDesc.setText(desc);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Failed to read value.", databaseError.toException());
            }
        });

        fabWishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  Add item to User's Wishlist (Add to database) :
                final DatabaseReference mUserRef = userRef.child(mUser.getuID());

                mUserRef.child("wishlist").child(item.getTitle()).setValue(item.getPrice());

                String toastText = getString(R.string.wishlist_add);
                Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Bundle extras = getIntent().getExtras();
        outState.putAll(extras);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);

        beaconKey = savedInstanceState.getString("beaconKey");
    }
}
