package com.rioscreative.iotpracticum.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.*;

import com.pubnub.api.*;
import com.pubnub.api.Callback;
import com.rioscreative.iotpracticum.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    // Josh's PubNub connection info
    public static final String PUBLISH_KEY = "pub-c-ae8e1c63-7fad-46a3-b5e2-ddd5a799a80b";
    private static final String SUBSCRIBE_KEY = "sub-c-e3b3d664-f0a5-11e5-baae-0619f8945a4f";
    private static final String channel = "appOut";

    // Luis' PubNub connection info
    //String PUBLISH_KEY = "pub-c-5d4c8a5c-b2d4-4974-a11c-4a3fc3c2ad8d";
    //String SUBSCRIBE_KEY = "sub-c-d52e3da6-c9e4-11e5-b684-02ee2ddab7fe";
    //String channel = "IOTpracticum";
    Pubnub pubnub = new Pubnub(PUBLISH_KEY, SUBSCRIBE_KEY, false);

    // Objects for connection
    JSONObject piConnectionCheck = new JSONObject();
    Object mMessage = new Object();


    JSONObject ledObj = new JSONObject();

    // Main Activity Layout connections
    @Bind(R.id.InternetConnectivityLabel) TextView mInternetConnectivityLabel;
    @Bind(R.id.InternetStatusLabel) TextView mInternetStatusLabel;
    @Bind(R.id.PubNubConnectivityLabel) TextView mPubNubConnectivityLabel;
    @Bind(R.id.PubNubStatusLabel) TextView mPubNubStatusLabel;
    @Bind(R.id.PiConnectivityLabel) TextView mPiConnectivityLabel;
    @Bind(R.id.PiStatusLabel) TextView mPiStatusLabel;
    @Bind(R.id.showDeviceListButton) Button mDeviceListButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // If network is available, change InternetStatusLabel text to online and textstyle to statusGreen
        if (isNetworkAvailable()){
            mInternetStatusLabel.setText(R.string.connection_online);
            mInternetStatusLabel.setTextAppearance(R.style.textScheme1_statusGreen);
        }

        try {
            // Remove previous "OFF "status if it exists
            ledObj.remove("Type");
            ledObj.remove("Status");
            // Add new "ON" Status
            ledObj.put("Type", "LED");
            ledObj.put("Status", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Intent serviceIntent = new Intent(this, PubNubService.class);
        startService(serviceIntent);

        Callback callback = new Callback() {
            public void successCallback(String channel, Object response) {
                System.out.println(response.toString());
            }
            public void errorCallback(String channel, PubnubError error) {
                System.out.println(error.toString());
            }
        };
        pubnub.time(callback);
        try {
            pubnub.subscribe(channel, new Callback() {

                        @Override
                        public void connectCallback(String channel, Object message) {
                            System.out.println("SUBSCRIBE : CONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        @Override
                        public void disconnectCallback(String channel, Object message) {
                            System.out.println("SUBSCRIBE : DISCONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        public void reconnectCallback(String channel, Object message) {
                            System.out.println("SUBSCRIBE : RECONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        @Override
                        public void successCallback(String channel, Object message) {
                            System.out.println("SUBSCRIBE : " + channel + " : "
                                    + message.getClass() + " : " + message.toString());
                            // Set mMessage equal to a received message from
                            mMessage = message;
                        }

                        @Override
                        public void errorCallback(String channel, PubnubError error) {
                            System.out.println("SUBSCRIBE : ERROR on channel " + channel
                                    + " : " + error.toString());
                        }
                    }
            );
        } catch (PubnubException e) {
            e.printStackTrace();
        }

        // Example publish to device (RGB LED, Camera, or Temp/Humidity sensor);
        // pubnub.publish(channel, rgbObj, callback);
        // pubnub.publish(channel, camObj, callback);
        // pubnub.publish(channel, tempObj, callback);
        //pubnub.publish(channel, ledObj, callback);

        // Check PubNub Connection
        pubnub.publish(channel, piConnectionCheck, callback);
        if (isPubnubAvailable(mMessage)) {
            mPubNubStatusLabel.setText(R.string.connection_online);
            mPubNubStatusLabel.setTextAppearance(R.style.textScheme1_statusGreen);
        }
        pubnub.unsubscribe(channel);
    }

    // If you receive a message, PubNub is available
    private boolean isPubnubAvailable(Object message) {
        boolean isAvailable = false;
        if (message == mMessage) {
            isAvailable = true;
        }
        return isAvailable;
    }

    private boolean isNetworkAvailable() {
                ConnectivityManager manager = (ConnectivityManager)
                        getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = manager.getActiveNetworkInfo();
                boolean isAvailable = false;
                if(networkInfo != null && networkInfo.isConnected()){
                    isAvailable = true;
                }
                return isAvailable;
            }

    // Go to Device List
    @OnClick (R.id.showDeviceListButton)
    public void startDeviceListActivity(View view) {
        Intent intent = new Intent(this, DeviceListActivity.class);
        startActivity(intent);
    }
}
