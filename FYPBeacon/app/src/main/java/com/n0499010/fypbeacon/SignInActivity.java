package com.n0499010.fypbeacon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.n0499010.fypbeacon.Global.OFFER_RETURN;
import static com.n0499010.fypbeacon.Global.OFFER_WELCOME;
import static com.n0499010.fypbeacon.Global.mFirebaseAuth;
import static com.n0499010.fypbeacon.Global.mUser;
import static com.n0499010.fypbeacon.Global.userRef;

/**
 * Source code reference: https://firebase.google.com/docs/auth/android/google-signin
 * <p>
 * Copyright Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    SharedPreferences preferences;

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private SignInButton mSignInButton;

    private GoogleApiClient mGoogleApiClient;

    // Firebase instance variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Assign fields
        mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);

        // Set click listeners
        mSignInButton.setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Initialize FirebaseAuth
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign-In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign-In failed
                Log.e(TAG, "Google Sign-In failed.");
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            checkForSignInOffer();
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            finish();
                        }
                    }
                });
    }

    public void checkForSignInOffer() {
        // Read database, lookup Uid to see if already exists :
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if (dataSnapshot.hasChild(mUser.getuID())) {
                preferences = getSharedPreferences(mUser.getuID(), getApplicationContext().MODE_PRIVATE);
                if (dataSnapshot.child(mUser.getuID()).hasChild("offers")) {
                    if (preferences.getBoolean(OFFER_RETURN, false) == false) {
                        // if Uid and 'offers' nodes already exist, add 'OF_Return' offer:
                        // add as key under user's 'offer's node, set value to 'true'
                        Map<String, String> userData = new HashMap<String, String>();
                        userData.put(OFFER_RETURN, "true");

                        ArrayList<String> uList = new ArrayList<String>();
                        uList.add(OFFER_RETURN);
                        mUser.setOfferList(uList);

                        DatabaseReference mUserRef = userRef;
                        mUserRef = userRef.child(mUser.getuID()).child("offers");
                        mUserRef.child(OFFER_RETURN).setValue("true");

                        final Intent myOffersIntent = new Intent(getApplicationContext(), MyOffersActivity.class);
                        Global.showNotification(
                                getString(R.string.notification_newoffer_title) + OFFER_RETURN,
                                getString(R.string.notification_newoffer_content),
                                myOffersIntent,
                                getApplicationContext(),
                                Global.NOTIFICATION_OFFER
                        );

                        preferences.edit().putBoolean(OFFER_RETURN, true).commit();
                    }
                } else {
                    if (preferences.getBoolean(OFFER_WELCOME, false) == false) {
                        // if Uid not present, write value to db as new key under 'user' as root node
                        Map<String, String> userData = new HashMap<String, String>();
                        userData.put(OFFER_WELCOME, "true");

                        ArrayList<String> uList = new ArrayList<String>();
                        uList.add(OFFER_WELCOME);
                        mUser.setOfferList(uList);

                        // Add new UID, under Uid node, add 'offers' node
                        DatabaseReference mUserRef = userRef.child(mUser.getuID()).child("offers");
                        // Add new offer under offers node: Key: OF_Welcome, value "true"
                        mUserRef.setValue(userData);

                        final Intent myOffersIntent = new Intent(getApplicationContext(), MyOffersActivity.class);
                        Global.showNotification(
                                getString(R.string.notification_newoffer_title) + OFFER_WELCOME,
                                getString(R.string.notification_newoffer_content),
                                myOffersIntent,
                                getApplicationContext(),
                                Global.NOTIFICATION_OFFER
                        );

                        preferences.edit().putBoolean(OFFER_WELCOME, true).commit();
                    }
                }
                //}
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Failed to read value.", databaseError.toException());
                //TODO: Database onCancelled error handling
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
