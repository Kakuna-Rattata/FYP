package com.n0499010.fypbeacon;

import java.io.Serializable;

/**
 * Created by shann on 04/04/2017.
 */

public class Offer implements Serializable {


    private String offerID;
    private String offerDesc;


    public Offer() {

    }

    public Offer(String offerID, String offerDesc) {
        this.offerID = offerID;
        this.offerDesc = offerDesc;
    }

    public String getOfferID() {
        return offerID;
    }

    public void setOfferID(String offerID) {
        this.offerID = offerID;
    }

    public String getOfferDesc() {
        return offerDesc;
    }

    public void setOfferDesc(String offerDesc) {
        this.offerDesc = offerDesc;
    }
}
