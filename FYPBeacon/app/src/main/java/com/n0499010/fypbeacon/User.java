package com.n0499010.fypbeacon;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by N0499010 Shannon Hibbett on 04/04/2017.
 */

public class User implements Serializable {

    private String uID;

    private String displayName;

    private Map<String,Boolean> offers;
    private List<Offer> wishlist;

//    private Map<String,Map<String,String>> beaconsVisited;
//    private Map<String,String> noVisitsMap;


    public User() {

    }

    public User(String uID, Map<String, Boolean> offers, List<Offer> wishlist) {
        this.uID = uID;

        this.offers = offers;
        this.wishlist = wishlist;
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

    public Map<String, Boolean> getOffers() {
        return offers;
    }

    public void setOffers(Map<String, Boolean> offers) {
        this.offers = offers;
    }

    public List<Offer> getWishlist() {
        return wishlist;
    }

    public void setWishlist(List<Offer> wishlist) {
        this.wishlist = wishlist;
    }

//    public Map<String, Map<String, String>> getBeaconsVisited() {
//        return beaconsVisited;
//    }
//
//    public void setBeaconsVisited(Map<String, Map<String, String>> beaconsVisited) {
//        this.beaconsVisited = beaconsVisited;
//    }
//
//    public Map<String, String> getNoVisitsMap() {
//        return noVisitsMap;
//    }
//
//    public void setNoVisitsMap(Map<String, String> noVisitsMap) {
//        this.noVisitsMap = noVisitsMap;
//    }
}
