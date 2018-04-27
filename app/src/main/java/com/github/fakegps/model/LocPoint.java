package com.github.fakegps.model;

import java.io.Serializable;

/**
 * Created by tiger on 7/23/16.
 */
public class LocPoint implements Serializable {
    static final long serialVersionUID = -1770575152720897533L;

    private double mLatitude;
    private double mLongitude;
    private float mBearing;

    public LocPoint(LocPoint locPoint) {
        mLatitude = locPoint.getLatitude();
        mLongitude = locPoint.getLongitude();
        mBearing = locPoint.getBearing();
    }

    public LocPoint(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public float getBearing() { return mBearing; }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public void setBearing(float bearing) { mBearing = bearing; }

    @Override
    public String toString() {
        return "(" + mLatitude + " , " + mLongitude + ")";
    }
}
