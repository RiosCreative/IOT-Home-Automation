package com.rioscreative.iotpracticum.ui;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import org.json.*;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.rioscreative.iotpracticum.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Turego on 3/17/16.
 */
public class DeviceListActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    // PubNub Connection
    private Pubnub mPubnub;

    // Josh's PubNub connection info
    public static final String PUBLISH_KEY = "pub-c-ae8e1c63-7fad-46a3-b5e2-ddd5a799a80b";
    private static final String SUBSCRIBE_KEY = "sub-c-e3b3d664-f0a5-11e5-baae-0619f8945a4f";
    private static final String channel1 = "appOut";
    private static final String channel2 = "piOut";

    // Luis' PubNub connection info
    //private static final String PUBLISH_KEY = "pub-c-5d4c8a5c-b2d4-4974-a11c-4a3fc3c2ad8d";
    //private static final String SUBSCRIBE_KEY = "sub-c-d52e3da6-c9e4-11e5-b684-02ee2ddab7fe";
    //private static final String channel = "IOTpracticum";

    // Objects for Devices
    JSONObject camObj = new JSONObject();
    JSONObject ledObj = new JSONObject();
    JSONObject rgbObj = new JSONObject();
    JSONObject rgbStripObj = new JSONObject();
    JSONObject tempHumObj = new JSONObject();
    JSONObject generalMessage = new JSONObject();
    // Message to get temp/Humidity value
    Object mMessage = new Object();

    // LED
    @Bind(R.id.ledSwitch) Switch mLedSwitch;

    // RGB LED
    @Bind(R.id.rgbLedSwitch) Switch mRgbLedSwitch;
    @Bind(R.id.redSeekBar) SeekBar mRedSeekBar;
    @Bind(R.id.greenSeekBar) SeekBar mGreenSeekBar;
    @Bind(R.id.blueSeekBar) SeekBar mBlueSeekBar;

    // RGB LED Strip
    @Bind(R.id.ledStripSwitch) Switch mledStripSwitch;
    @Bind(R.id.redStripSeekBar) SeekBar mRedStripSeekBar;
    @Bind(R.id.greenStripSeekBar) SeekBar mGreenStripSeekBar;
    @Bind(R.id.blueStripSeekBar) SeekBar mBlueStripSeekBar;

    // Temperature & Humidity Sensor
    @Bind(R.id.tempHumSensorSwitch) Switch mTempHumSensorSwitch;
    @Bind(R.id.temperatureValue) TextView mTempValue;
    @Bind(R.id.humidityValue) TextView mHumidValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        ButterKnife.bind(this);

        initiatePubNub();

        final Intent serviceIntent = new Intent(this, PubNubService.class);
        startService(serviceIntent);

        // LED
        mLedSwitch.setOnCheckedChangeListener(this);

        // RGB LED
        mRgbLedSwitch.setOnCheckedChangeListener(this);

        // RGB LED Strip
        mledStripSwitch.setOnCheckedChangeListener(this);

        // Temp & Humidity Sensor
        mTempHumSensorSwitch.setOnCheckedChangeListener(this);


    }

    public void initiatePubNub(){
        this.mPubnub = new Pubnub(PUBLISH_KEY, SUBSCRIBE_KEY, false);
        subscribe(channel2);
    }

    private void subscribe(String channel) {
        try {
            this.mPubnub.subscribe(channel, new Callback() {
                        @Override
                        public void connectCallback(String channel, Object message) {
                            System.out.println("SUBSCRIBE2 : CONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        @Override
                        public void disconnectCallback(String channel, Object message) {
                            System.out.println("SUBSCRIBE2 : DISCONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        public void reconnectCallback(String channel, Object message) {
                            System.out.println("SUBSCRIBE2 : RECONNECT on channel:" + channel
                                    + " : " + message.getClass() + " : "
                                    + message.toString());
                        }

                        @Override
                        public void successCallback(String channel, Object message) {
                            System.out.println("SUBSCRIBE2 : " + channel + " : "
                                    + message.getClass() + " : " + message.toString());

                            if (message instanceof JSONObject) {
                                generalMessage = (JSONObject) message;
                            }

                        }

                        @Override
                        public void errorCallback(String channel, PubnubError error) {
                            System.out.println("SUBSCRIBE2 : ERROR on channel " + channel
                                    + " : " + error.toString());
                        }
                    }
            );
        } catch (PubnubException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Create new PubNub Callback
        Callback callback = new Callback() {
            public void successCallback(String channel, Object response) {
                System.out.println(response.toString());
            }
            public void errorCallback(String channel, PubnubError error) {
                System.out.println(error.toString());
            }
        };

        switch (buttonView.getId()) {
            case R.id.ledSwitch:
                Log.i("LED", isChecked + "");
                if (isChecked==true){
                    mLedSwitch.setText(R.string.statusON);
                    mLedSwitch.setTextAppearance(R.style.textScheme1_statusGreen);
                    // Turn LED On
                    try {
                        // Remove previous "OFF "status if it exists
                        ledObj.remove("Type");
                        ledObj.remove("Status");
                        // Add new "ON" Status
                        ledObj.put("Type", "LED");
                        ledObj.put("Status", 1);
                        mPubnub.publish(channel1, ledObj, callback);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                else {
                    mLedSwitch.setText(R.string.statusOFF);
                    mLedSwitch.setTextAppearance(R.style.textScheme1_statusRed);
                    try {
                        // Remove previous "ON "status if it exists
                        ledObj.remove("Type");
                        ledObj.remove("Status");
                        // Add new "OFF" Status
                        ledObj.put("Type", "LED");
                        ledObj.put("Status", 0);
                        mPubnub.publish(channel1, ledObj, callback);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                break;

            case R.id.rgbLedSwitch:
                Log.i("RGB LED", isChecked + "");
                if (isChecked==true){
                    mRgbLedSwitch.setText(R.string.statusON);
                    mRgbLedSwitch.setTextAppearance(R.style.textScheme1_statusGreen);
                    // Turn RGB LED On (White)
                    try {
                        // Remove previous "OFF "status if it exists
                        rgbObj.remove("Type");
                        rgbObj.remove("RED");
                        rgbObj.remove("GREEN");
                        rgbObj.remove("BLUE");
                        // Add new "ON" Status
                        rgbObj.put("Type", "RGB");
                        rgbObj.put("RED", 255);
                        rgbObj.put("GREEN", 255);
                        rgbObj.put("BLUE", 255);
                        mPubnub.publish(channel1, rgbObj, callback);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mPubnub.publish(channel1, rgbObj, callback);
                }
                else {
                    mRgbLedSwitch.setText(R.string.statusOFF);
                    mRgbLedSwitch.setTextAppearance(R.style.textScheme1_statusRed);

                    // Turn RGB LED OFF (Black)
                    try {
                        // Remove previous "ON "status if it exists
                        rgbObj.remove("Type");
                        rgbObj.remove("RED");
                        rgbObj.remove("GREEN");
                        rgbObj.remove("BLUE");
                        // Add new "OFF" Status
                        rgbObj.put("Type", "RGB");
                        rgbObj.put("RED", 0);
                        rgbObj.put("GREEN", 0);
                        rgbObj.put("BLUE", 0);
                        mPubnub.publish(channel1, rgbObj, callback);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mPubnub.publish(channel1, rgbObj, callback);
                }
                break;

            case R.id.ledStripSwitch:
                Log.i("RGB Strip", isChecked + "");
                if (isChecked==true){
                    mledStripSwitch.setText(R.string.statusON);
                    mledStripSwitch.setTextAppearance(R.style.textScheme1_statusGreen);
                    // Turn On RGB Strip LEDs
                    try {
                        // Remove previous "OFF "status if it exists
                        rgbStripObj.remove("Type");
                        rgbStripObj.remove("RED");
                        rgbStripObj.remove("GREEN");
                        rgbStripObj.remove("BLUE");
                        // Add new "ON" Status
                        rgbStripObj.put("Type", "STRIP");
                        rgbStripObj.put("RED", 255);
                        rgbStripObj.put("GREEN", 255);
                        rgbStripObj.put("BLUE", 255);
                        mPubnub.publish(channel1, rgbStripObj, callback);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                else {
                    mledStripSwitch.setText(R.string.statusOFF);
                    mledStripSwitch.setTextAppearance(R.style.textScheme1_statusRed);
                    // Turn Off RGB Strip LEDs
                    try {
                        // Remove previous "ON "status if it exists
                        rgbStripObj.remove("Type");
                        rgbStripObj.remove("RED");
                        rgbStripObj.remove("GREEN");
                        rgbStripObj.remove("BLUE");
                        // Add new "OFF" Status
                        rgbStripObj.put("Type", "STRIP");
                        rgbStripObj.put("RED", 0);
                        rgbStripObj.put("GREEN", 0);
                        rgbStripObj.put("BLUE", 0);
                        mPubnub.publish(channel1, rgbStripObj, callback);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                break;

            case R.id.tempHumSensorSwitch:
                Log.i("Temp/Hum Sensor", isChecked + "");
                if (isChecked==true){
                    mTempHumSensorSwitch.setText(R.string.statusON);
                    mTempHumSensorSwitch.setTextAppearance(R.style.textScheme1_statusGreen);
                    try {
                        tempHumObj.remove("Type");
                        tempHumObj.put("Type", "SENS");
                        mPubnub.publish(channel1, tempHumObj, callback);

                        mTempValue.setText(generalMessage.getString("Temperature")+ (char) 0x00B0 + "F");
                        mHumidValue.setText(generalMessage.getString("Humidity")+"%");
                        //if (generalMessage.get("Temperature") != null) {
                            //mTempValue.setText(generalMessage.getString("Temperature")+ R.string.degreeSign);
                            //mHumidValue.setText(generalMessage.getString("Humidity")+"%");
                            //Log.i("SUBSCRIBE2 : TEMP", generalMessage.toString());
                        //}

                        //String tempHumidity = mMessage.toString();
                        //mTempValue.setText(tempHumidity.substring());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                else {
                    mTempHumSensorSwitch.setText(R.string.statusOFF);
                    mTempHumSensorSwitch.setTextAppearance(R.style.textScheme1_statusRed);
                    // Will just change values to ---
                    mTempValue.setText("---" + (char) 0x00B0 + "F");
                    mHumidValue.setText("---%");
                }
                break;

        }

    }

}
