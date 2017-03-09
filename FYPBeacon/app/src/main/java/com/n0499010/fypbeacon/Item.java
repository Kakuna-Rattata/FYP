package com.n0499010.fypbeacon;

/**
 * Created by N0499010 Shannon Hibbett on 08/03/2017.
 */

public class Item {

    private String name;
    private String beaconMM;

    // Required default constructor for Firebase object mapping
    @SuppressWarnings("unused")
    private Item() {
    }

    Item(String name, String beaconMM) {
        this.name = name;
        this.beaconMM = beaconMM;
    }

    public String getName() {
        return name;
    }

    public String getBeaconMM() {
        return beaconMM;
    }
}
