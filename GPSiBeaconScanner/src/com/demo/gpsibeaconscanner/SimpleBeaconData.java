package com.demo.gpsibeaconscanner;

public class SimpleBeaconData {
    private String mUUID;
    private String mMajor;
    private String mMinor;
    public SimpleBeaconData (String uuid, String major, String minor) {
        mUUID = uuid;
        mMajor = major;
        mMinor = minor;
    }
    public String getUUID() { return mUUID;}
    public String getMajor() { return mMajor;}
    public String getMinor() { return mMinor;}

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof SimpleBeaconData))return false;
        SimpleBeaconData otherBeacon = (SimpleBeaconData) other;
        if (this.mUUID.equals(otherBeacon.mUUID) &&
                this.mMajor.equals(otherBeacon.mMajor) &&
                this.mMinor.equals(otherBeacon.mMinor)) {
            return true;
        } else {
            return false;
        }
    }
}