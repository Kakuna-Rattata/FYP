package com.n0499010.fypbeacon;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;

import static com.n0499010.fypbeacon.Global.mUid;
import static com.n0499010.fypbeacon.Global.userRef;

public class WishlistActivity extends AppCompatActivity {

    private static final String TAG = "WishlistActivity";
    private ListView listViewWishlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        listViewWishlist = (ListView) findViewById(R.id.listView_wishlist);

        DatabaseReference mUserRef = userRef.child(mUid);
        final DatabaseReference wishlistRef = mUserRef.child("wishlist");

        final FirebaseListAdapter<String> firebaseListAdapter = new FirebaseListAdapter<String>
                (this, String.class, R.layout.wishlist_listview_item, wishlistRef) {
            @Override
            protected void populateView(View v, String itemValue, int position) {

                final DatabaseReference itemRef = getRef(position);
                String itemKey = itemRef.getKey();

                TextView value = (TextView)v.findViewById(R.id.label);
                value.setText(itemKey + " " + itemValue);

                //Handle buttons and add onClickListeners
                Button buttonRemove = (Button) v.findViewById(R.id.button_remove);

                buttonRemove.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {

                        itemRef.removeValue();

                        Toast.makeText(getApplicationContext(), R.string.toast_text_wishlist_remove, Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
                    }
                });
            }
        };
        listViewWishlist.setAdapter(firebaseListAdapter);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.sign_out_menu:
//
//                mFirebaseAuth.signOut();
//                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
//                mUsername = ANONYMOUS;
//
//                startActivity(new Intent(this, SignInActivity.class));
//
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
}
