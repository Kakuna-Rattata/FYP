package com.n0499010.fypbeacon;

import java.io.Serializable;

/**
 * Created by N0499010 Shannon Hibbett on 08/03/2017.
 */

public class Item implements Serializable {

    private String key;

    private String name;
    private String beaconMM;

    private String image;
    private String title;
    private String desc;

    // Required default constructor for Firebase object mapping
    @SuppressWarnings("unused")
    public Item() {
    }

    public Item(String name, String beaconMM, String image, String title, String desc) {
        this.name = name;
        this.beaconMM = beaconMM;

        this.image = image;
        this.title = title;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBeaconMM() {
        return beaconMM;
    }

    public void setBeaconMM(String beaconMM) {
        this.beaconMM = beaconMM;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
