package com.n0499010.fypbeacon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.SystemRequirementsChecker;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.n0499010.fypbeacon.Global.ANONYMOUS;
import static com.n0499010.fypbeacon.Global.mFirebaseAuth;
import static com.n0499010.fypbeacon.Global.mFirebaseUser;
import static com.n0499010.fypbeacon.Global.mGoogleApiClient;
import static com.n0499010.fypbeacon.Global.mPhotoUrl;
import static com.n0499010.fypbeacon.Global.mUid;
import static com.n0499010.fypbeacon.Global.mUser;
import static com.n0499010.fypbeacon.Global.mUsername;
import static com.n0499010.fypbeacon.Global.userRef;

/*
 * OnShareClick method code adapted from source :
 * http://stackoverflow.com/questions/9730243/how-to-filter-specific-apps-for-action-send-intent-and-set-a-different-text-for
 */
public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    SharedPreferences preferences;

    Intent myOffersIntent;

    /* Layout Elements */
    private Button buttonAccount;
    private Button buttonWishlist;
    private Button buttonNearbyOffers;
    private Button buttonMyOffers;
    private Button buttonFeedback;
    private Button buttonSignOut;

    /* Estimote SDK Beacon Elements */
    public static BeaconManager beaconManager;
    private String scanId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        myOffersIntent = new Intent(this, MyOffers.class);

        buttonAccount = (Button) findViewById(R.id.button_account);
        buttonWishlist = (Button) findViewById(R.id.button_wishlist);
        buttonNearbyOffers = (Button) findViewById(R.id.button_nearby_offers);
        buttonMyOffers = (Button) findViewById(R.id.button_my_offers);
        buttonFeedback = (Button) findViewById(R.id.button_feedback);
        buttonSignOut = (Button) findViewById(R.id.button_sign_out);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mUsername = ANONYMOUS;
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
            return;
        } else {
            // User authenticated
            mUid = mFirebaseUser.getUid();
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
            mUser.setuID(mUid);
            mUser.setDisplayName(mUsername);
            mUser.setPhotoUrl(mPhotoUrl);
            initialiseAccount();
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        buttonAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Account overview page
            }
        });

        buttonWishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), WishlistActivity.class));
            }
        });

        buttonNearbyOffers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), NearbyProducts.class));
            }
        });

        buttonMyOffers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MyOffers.class));
            }
        });

        buttonFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.onShareClick(getApplicationContext());
            }
        });

        buttonSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    public void initialiseApp() {
        //TODO: initApp method
        // Create offer objects from database
    }

    public void initialiseAccount() {
        // Read database, lookup Uid to see if already exists :
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mUser.getuID())) {
                    if (dataSnapshot.child(mUser.getuID()).hasChild("offers")) {
                        // if Uid and 'offers' nodes already exist, add 'OF_Return' offer:
                        // add as key under user's 'offer's node, set value to 'true'
                        Map<String, String> userData = new HashMap<String, String>();
                        userData.put("OF_Return", "true");

                        ArrayList<String> uList = new ArrayList<String>();
                        uList.add("OF_Return");
                        mUser.setOfferList(uList);

                        DatabaseReference mUserRef = userRef;
                        mUserRef = userRef.child(mUser.getuID()).child("offers");
                        mUserRef.child("OF_Return").setValue("true");
                    }
                } else {
                    // if Uid not present, write value to db as new key under 'user' as root node
                    Map<String,String> userData = new HashMap<String, String>();
                    //TODO: get offers from database, save to global offer list on sign-in
                    userData.put("OF_Welcome", "true");

                    ArrayList<String> uList = new ArrayList<String>();
                    uList.add("OF_Welcome");
                    mUser.setOfferList(uList);

                    DatabaseReference mUserRef = userRef;
                    // Add new UID, under Uid node, add 'offers' node
                    mUserRef = userRef.child(mUser.getuID()).child("offers");
                    // Add new offer under offers node: Key: OF_Welcome, value "true"
                    mUserRef.setValue(userData);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void signOut() {
        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);

        preferences.edit().putBoolean("authenticated", false).apply();
        mUsername = ANONYMOUS;
        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d("MainActivity", "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }
}