package com.n0499010.fypbeacon;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import static com.n0499010.fypbeacon.Global.beaconRef;
import static com.n0499010.fypbeacon.Global.mBeaconDataMap;
import static com.n0499010.fypbeacon.Global.mUser;
import static com.n0499010.fypbeacon.Global.storageInstance;
import static com.n0499010.fypbeacon.Global.userRef;
import static java.sql.Types.NULL;
//import static com.n0499010.fypbeacon.Global.retailImageRef;

/*
 * Created by Shannon Hibbett (N0499010) on 29/03/2017.
 */

public class OverviewActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textViewTitle;
    private TextView textViewPrice;
    private TextView textViewDesc;
    private FloatingActionButton fabWishlist;
    private FloatingActionButton fabComment;
    private RatingBar ratingBar;
    private NonScrollListView listViewComments;

    String beaconKey;
    String instanceWishlist = "inWishlist";
    Bundle extras;

    Boolean inWishlist = false;
    float userRating = NULL;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageView = (ImageView) findViewById(R.id.imageView_overview);
        textViewTitle = (TextView) findViewById(R.id.textView_title);
        textViewPrice = (TextView) findViewById(R.id.textView_price);
        textViewDesc = (TextView) findViewById(R.id.textView_desc);
        fabWishlist = (FloatingActionButton) findViewById(R.id.fab_wishlist);
        fabComment = (FloatingActionButton) findViewById(R.id.fab_comment);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        listViewComments = (NonScrollListView) findViewById(R.id.listView_nonScroll_comments);


        //  Get beaconKey passed from triggering Intent :
        extras = getIntent().getExtras();
        if (extras != null) {
            beaconKey = extras.getString("beaconKey");
        }
        Log.d("beaconKey is: ", beaconKey);

        final StorageReference imageRootRef = storageInstance.getReference();
        final StorageReference retailImageRef = imageRootRef.child("Retail_images");

        final Item item = new Item();

        final DatabaseReference mUserRef = userRef.child(mUser.getuID());
        final DatabaseReference wishlistRef = mUserRef.child("wishlist");
        final DatabaseReference itemRatingRef = beaconRef.child(beaconKey).child("userRating");
        final DatabaseReference itemCommentsRef = beaconRef.child(beaconKey).child("userComment");

        //  Query database for page content to set UI :
        beaconRef.child(beaconKey).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String imgName = null, title = null, desc = null, price = null, category = null;
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
                        if (child.getKey().equals("category")) {
                            category = child.getValue().toString();
                            item.setCategory(category);
                            BeaconData beaconDataObj = new BeaconData(beaconKey, category);
                            mBeaconDataMap.put(beaconDataObj.getMmKey(), beaconDataObj);
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

                    mBeaconDataMap.get(beaconKey).setCategory(category);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Failed to read value.", databaseError.toException());
                //TODO: Database onCancelled error handling
            }
        });

        final FirebaseListAdapter<String> firebaseListAdapter = new FirebaseListAdapter<String>
                (this, String.class, R.layout.listview_comment_item, itemCommentsRef) {
            @Override
            protected void populateView(View v, String commentValue, int position) {

                final DatabaseReference commentRef = getRef(position);
                String commentKey = commentRef.getKey();

                TextView name = (TextView) v.findViewById(R.id.labelName);
                TextView comment = (TextView) v.findViewById(R.id.comment);
                name.setText(commentKey + ": ");
                comment.setText(commentValue);

                notifyDataSetChanged();
            }
        };
        listViewComments.setAdapter(firebaseListAdapter);

        // Check database, onDataChange lookup item under 'wishlist' ref using title as key
        wishlistRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ( !(dataSnapshot.equals(null)) ) {
                    if ( dataSnapshot.hasChildren() ) {
                        if ( !(item.equals(null))) {
                            if (dataSnapshot.hasChild(item.getTitle())) {
                                // item in wishlist
                                fabWishlist.setImageResource(android.R.drawable.btn_star_big_on);
                                inWishlist = true;
                            } else {
                                // item not in wishlist
                                fabWishlist.setImageResource(android.R.drawable.star_off);
                                inWishlist = false;
                            }
                        }
                    } else {
                        fabWishlist.setImageResource(android.R.drawable.star_off);
                        inWishlist = false;
                    }
                }

                if (inWishlist) {
                    ArrayList<Item> arrayList = mUser.getWishlist();
                    if ( !(arrayList.contains(item)) ) {
                        arrayList.add(item);
                    }
                    mUser.setWishlist(arrayList);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Failed to read value.", databaseError.toException());
                //TODO: Database onCancelled error handling
            }
        });

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                if (fromUser) {
                    userRating = rating;
                    // Add User DisplayName key and numerical rating as value
                    if (userRating != NULL) {
                        itemRatingRef.child(mUser.getuID()).setValue(String.valueOf(userRating));
                    }
                }
            }
        });

        fabComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OverviewActivity.this);
                builder.setTitle(R.string.dialog_comment);

                final EditText input = new EditText(getApplicationContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setTextColor(getResources().getColor(R.color.colorAccent));
                builder.setView(input);

                builder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (input.getText().length() <= 0 || input.getText() == null) {
                            Toast.makeText(getApplicationContext(),
                                    "Please enter text before pressing 'Post'",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            itemCommentsRef.child(mUser.getDisplayName()).setValue(input.getText().toString());
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        fabWishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inWishlist) {
                    // Remove from wishlist
                    mUserRef.child("wishlist").child(item.getTitle()).removeValue();

                    // Change button graphic to 'unselected' graphic
                    fabWishlist.setImageResource(android.R.drawable.star_off);

                    Toast.makeText(getApplicationContext(),
                            getString(R.string.wishlist_remove),
                            Toast.LENGTH_SHORT).show();

                    inWishlist = false;
                } else {
                    //  Add item (name and price) to User's Wishlist (Add to database) :
                    mUserRef.child("wishlist").child(item.getTitle()).setValue(item.getPrice());

                    // Change button graphic to 'selected' graphic
                    fabWishlist.setImageResource(android.R.drawable.btn_star_big_on);

                    Toast.makeText(getApplicationContext(),
                            getString(R.string.wishlist_add),
                            Toast.LENGTH_SHORT).show();

                    inWishlist = true;
                }
            }
        });

        itemRatingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get user ratings for item from db
                final ArrayList<Float> ratingArray = new ArrayList<Float>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    ratingArray.add(Float.parseFloat(child.getValue().toString()));
                }

                // recalculate average
                item.setRating(calculateAverageRating(ratingArray));

                //  display average
                ratingBar.setRating(item.getRating());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Failed to read value.", databaseError.toException());
                //TODO: Database onCancelled error handling
            }
        });
    }

    float calculateAverageRating(ArrayList<Float> ratings) {
        float result = NULL;

        for (Float rating : ratings) {
            result += rating;
        }
        result /= ratings.size();

        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Bundle extras = getIntent().getExtras();
        outState.putAll(extras);

        outState.putBoolean(instanceWishlist, inWishlist);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);

        inWishlist = savedInstanceState.getBoolean(instanceWishlist);
        beaconKey = savedInstanceState.getString("beaconKey");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        finish();
        startActivity(intent);
    }
}
