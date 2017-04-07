package com.n0499010.fypbeacon;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Shannon Hibbett (N0499010) on 07/04/2017.
 */

public class OfferMap implements Serializable {

    public static Map<String,Offer> offerMap;

    public OfferMap() {
        offerMap = new HashMap<>();
    }
}
