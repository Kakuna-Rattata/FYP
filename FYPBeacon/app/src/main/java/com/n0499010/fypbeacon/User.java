package com.n0499010.fypbeacon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by N0499010 Shannon Hibbett on 04/04/2017.
 */

public class User implements Serializable {

    private String uID;
    private String displayName;
    private String photoUrl;

    private List<String> offerList;
    private ArrayList<Item> wishlist;

    private Map<String, String> beaconsVisited;

    public User() {

    }

    public User(String uID) {
        this.uID = uID;
    }

    public String getuID() {
        return uID;
    }

    public void setuID(String uID) {
        this.uID = uID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setWishlist(ArrayList<Item> wishlist) {
        this.wishlist = wishlist;
    }

    public List<String> getOfferList() {
        return offerList;
    }

    public void setOfferList(List<String> offerList) {
        this.offerList = offerList;
    }

    public Map<String, String> getBeaconsVisited() {
        return beaconsVisited;
    }

    public void setBeaconsVisited(Map<String, String> beaconsVisited) {
        this.beaconsVisited = beaconsVisited;
    }
}
