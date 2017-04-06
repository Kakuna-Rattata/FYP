package com.n0499010.fypbeacon;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;

/**
 * Created by Shannon Hibbett (N0499010) on 08/03/2017.
 */

public class ItemListAdapter extends FirebaseListAdapter<Item> {


    public ItemListAdapter(Activity activity, Class<Item> modelClass, int modelLayout, com.google.firebase.database.Query ref) {
        super(activity, modelClass, modelLayout, ref);
    }

    @Override
    protected void populateView(View v, Item model, int position) {

        String name = model.getName();
        TextView nameText = (TextView) v.findViewById(R.id.label);
        nameText.setText(name);
    }
}
