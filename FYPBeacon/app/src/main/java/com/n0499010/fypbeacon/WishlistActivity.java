package com.n0499010.fypbeacon;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
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

        Button buttonRemove;

        DatabaseReference mUserRef = userRef.child(mUid);
        final DatabaseReference wishlistRef = mUserRef.child("wishlist");

        final FirebaseListAdapter<String> firebaseListAdapter = new FirebaseListAdapter<String>
                (this, String.class, R.layout.wishlist_listview_item, wishlistRef) {
/*android.R.layout.simple_list_item_1*/

//            @Override
//            protected String parseSnapshot(DataSnapshot snapshot) {
//                return snapshot.getChildren().iterator().next().getValue(String.class);
//            }

            @Override
            protected void populateView(View v, String itemValue, int position) {

                final DatabaseReference itemRef = getRef(position);
                String itemKey = itemRef.getKey();

                //TextView value = (TextView)v.findViewById(android.R.id.text1);
                TextView value = (TextView)v.findViewById(R.id.label);
                value.setText(itemKey + " " + itemValue);

                //Handle buttons and add onClickListeners
                Button buttonRemove = (Button) v.findViewById(R.id.button_remove);

                buttonRemove.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        //list.remove(position);
                        itemRef.removeValue();

                        //TODO: Update database
                        Toast.makeText(getApplicationContext(), "Item Removed", Toast.LENGTH_SHORT).show();

                        notifyDataSetChanged();
                    }
                });
            }

            @Override
            public View getView(int position, View view, ViewGroup viewGroup) {



                return super.getView(position, view, viewGroup);
            }
        };
        listViewWishlist.setAdapter(firebaseListAdapter);

    }
}
