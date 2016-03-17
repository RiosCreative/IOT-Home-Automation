package com.rioscreative.iotpracticum.ui;

/**
 * Created by Turego on 3/17/16.
 */
public class Connection {

    private String mLabel;
    private boolean mStatus;

    public Connection (String label, boolean status){
        mLabel = label;
        mStatus = status;
    }

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    public boolean getStatus() {
        return mStatus;
    }

    public void setStatus(boolean status) {
        mStatus = status;
    }

}
