package com.n0499010.fypbeacon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by N0499010 Shannon Hibbett on 04/04/2017.
 */

public class User implements Serializable {

    private String uID;
    private String displayName;

    //private Map<String,Boolean> offers;
    //private List<Offer> wishlist;

    //private List<Offer> offerList;
    private List<String> offerList;
    private ArrayList<Item> wishlist;

//    private Map<String,Map<String,String>> beaconsVisited;
//    private Map<String,String> noVisitsMap;


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

    public void setWishlist(ArrayList<Item> wishlist) {
        this.wishlist = wishlist;
    }

    public List<String> getOfferList() {
        return offerList;
    }

    public void setOfferList(List<String> offerList) {
        this.offerList = offerList;
    }

//    public List<Offer> getOfferList() {
//        return offerList;
//}
//
//    public void setOfferList(List<Offer> offerList) {
//        this.offerList = offerList;
//    }
}
