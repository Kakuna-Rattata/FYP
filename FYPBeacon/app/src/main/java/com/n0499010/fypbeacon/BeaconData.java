package com.n0499010.fypbeacon;

import com.estimote.sdk.Region;

/**
 * Created by N0499010 Shannon Hibbett on 05/04/2017.
 */

public class BeaconData {

    private String mmKey;
    private Region region;

    private String category;

    private String noVisits;
    private String timeSpent;

    private long tStart;
    private long elapsedTime;

    public BeaconData() {

    }

    public BeaconData(String mmKey, Region region) {
        this.mmKey = mmKey;
        this.region = region;
    }

    public BeaconData(String mmKey, String category) {
        this.mmKey = mmKey;
        this.category = category;
    }

    public BeaconData(String mmKey) {
        this.mmKey = mmKey;
    }

    public String getMmKey() {
        return mmKey;
    }

    public void setMmKey(String mmKey) {
        this.mmKey = mmKey;
    }

    public String getNoVisits() {
        return noVisits;
    }

    public void setNoVisits(String noVisits) {
        this.noVisits = noVisits;
    }

    public String getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(String timeSpent) {
        this.timeSpent = timeSpent;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public long gettStart() {
        return tStart;
    }

    public void settStart(long tStart) {
        this.tStart = tStart;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
