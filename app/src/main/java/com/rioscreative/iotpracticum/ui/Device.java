package com.rioscreative.iotpracticum.ui;

/**
 * Created by Turego on 3/17/16.
 */
public class Device {

    private String mDeviceLabel;
    private boolean mStatus;

    public Device(String label, boolean status){
        mDeviceLabel = label;
        mStatus = status;
    }

    public String getDeviceLabel() {
        return mDeviceLabel;
    }

    public void setDeviceLabel(String deviceLabel) {
        mDeviceLabel = deviceLabel;
    }

    public boolean getStatus() {
        return mStatus;
    }

    public void setStatus(boolean status) {
        mStatus = status;
    }

}
