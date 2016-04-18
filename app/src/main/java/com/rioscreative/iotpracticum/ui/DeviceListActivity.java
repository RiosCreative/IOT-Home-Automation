package com.rioscreative.iotpracticum.ui;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import butterknife.OnClick;

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

    // Timing
    private long lastUpdate = System.currentTimeMillis();

    // Color Saving Strings
    public String currentRGBcolor = "FFFFFF";
    public String currentRGBstripColor = "FFFFFF";
    public static final String colorBlack = "000000";

    // Buttons
    @Bind(R.id.rgbColorButton) Button mRGBColorButton;
    @Bind(R.id.rgbStripColorButton) Button mRGBstripColorButton;

    // LED
    @Bind(R.id.ledSwitch) Switch mLedSwitch;

    // RGB LED
    @Bind(R.id.rgbLedSwitch) Switch mRgbLedSwitch;
    @Bind(R.id.redSeekBar) SeekBar mRedSeekBar;
    @Bind(R.id.greenSeekBar) SeekBar mGreenSeekBar;
    @Bind(R.id.blueSeekBar) SeekBar mBlueSeekBar;
    @Bind(R.id.colorEdit) EditText mRGBcolorEdit;
    public static final int COLOR_LEDRED = 0;
    public static final int COLOR_LEDGREEN = 1;
    public static final int COLOR_LEDBLUE = 2;

    // RGB LED Strip
    @Bind(R.id.ledStripSwitch) Switch mledStripSwitch;
    @Bind(R.id.redStripSeekBar) SeekBar mRedStripSeekBar;
    @Bind(R.id.greenStripSeekBar) SeekBar mGreenStripSeekBar;
    @Bind(R.id.blueStripSeekBar) SeekBar mBlueStripSeekBar;
    @Bind(R.id.colorStripEdit) EditText mRGBcolorStripEdit;
    public static final int COLOR_STRIPRED = 3;
    public static final int COLOR_STRIPGREEN = 4;
    public static final int COLOR_STRIPBLUE = 5;

    // Temperature & Humidity Sensor
    @Bind(R.id.tempHumSensorSwitch) Switch mTempHumSensorSwitch;
    @Bind(R.id.temperatureValue) TextView mTempValue;
    @Bind(R.id.humidityValue) TextView mHumidValue;

    // Camera


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
        setupRGBSeekBar(mRedSeekBar, COLOR_LEDRED);
        setupRGBSeekBar(mGreenSeekBar, COLOR_LEDGREEN);
        setupRGBSeekBar(mBlueSeekBar, COLOR_LEDBLUE);


        // RGB LED Strip
        mledStripSwitch.setOnCheckedChangeListener(this);
        setupStripSeekBar(mRedStripSeekBar, COLOR_STRIPRED);
        setupStripSeekBar(mGreenStripSeekBar, COLOR_STRIPGREEN);
        setupStripSeekBar(mBlueStripSeekBar, COLOR_STRIPBLUE);

        // Temp & Humidity Sensor
        mTempHumSensorSwitch.setOnCheckedChangeListener(this);

        // Button for inputting RGB LED color
        mRGBColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change switch status to "ON" if off
                mRgbLedSwitch.setText(R.string.statusON);
                mRgbLedSwitch.setTextAppearance(R.style.textScheme1_statusGreen);
                mRgbLedSwitch.setChecked(true);
                // Update RGB LED color using inputted value
                String newColor = mRGBcolorEdit.getText().toString();
                ColorConversion rgbConvert = new ColorConversion(newColor);
                rgbClearAndColor(rgbObj, newColor, "RGB", getCallback());
                mRedSeekBar.setProgress(rgbConvert.getRedValue());
                mGreenSeekBar.setProgress(rgbConvert.getGreenValue());
                mBlueSeekBar.setProgress(rgbConvert.getBlueValue());
                currentRGBcolor = rgbConvert.getRGBcolor();
            }
        });

        // Button for inputting RGB LED Strip color
        mRGBstripColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change switch status to "ON" if off
                mledStripSwitch.setText(R.string.statusON);
                mledStripSwitch.setTextAppearance(R.style.textScheme1_statusGreen);
                mledStripSwitch.setChecked(true);
                // Update RGB LED color using inputted value
                String newColor = mRGBcolorStripEdit.getText().toString();
                ColorConversion rgbStripConvert = new ColorConversion(newColor);
                rgbClearAndColor(rgbStripObj, newColor, "STRIP", getCallback());
                mRedStripSeekBar.setProgress(rgbStripConvert.getRedValue());
                mGreenStripSeekBar.setProgress(rgbStripConvert.getGreenValue());
                mBlueStripSeekBar.setProgress(rgbStripConvert.getBlueValue());
                currentRGBstripColor = rgbStripConvert.getRGBcolor();
            }
        });
    }

    public Callback getCallback(){
        // Create new PubNub Callback
        Callback callback = new Callback() {
            public void successCallback(String channel, Object response) {
                System.out.println(response.toString());
            }
            public void errorCallback(String channel, PubnubError error) {
                System.out.println(error.toString());
            }
        };
        return callback;
    }


    private void setupRGBSeekBar(SeekBar seekBar, final int colorID) {
        seekBar.setMax(255); // Seek bar values range from 0-255
        seekBar.setProgress(255); // Set value to 255 to start
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                publishRGB(mRedSeekBar.getProgress(), mGreenSeekBar.getProgress(), mBlueSeekBar.getProgress());

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mRgbLedSwitch.setText(R.string.statusON);
                mRgbLedSwitch.setTextAppearance(R.style.textScheme1_statusGreen);
                mRgbLedSwitch.setChecked(true);
                publishRGB(mRedSeekBar.getProgress(), mGreenSeekBar.getProgress(), mBlueSeekBar.getProgress());
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Do something when the progress changes
                int red = mRedSeekBar.getProgress(); // Get the red value 0-255
                int green = mGreenSeekBar.getProgress(); // Get the green value 0-255
                int blue = mBlueSeekBar.getProgress(); // Get the blue value from 0-255
                updateRGBhexText(red, green, blue); // Change the value of the EditText to the correct Hex value

                long now = System.currentTimeMillis(); // Only allow 1 publish every 100 milliseconds
                if (now - lastUpdate > 100 && fromUser) { // Threshold and only send when user sliding
                    lastUpdate = now;
                    publishRGB(red, green, blue);
                }
            }

        });
    }

    private void publishRGB(int red, int green, int blue) {
        // Create new PubNub Callback
        Callback callback = getCallback();
        ColorConversion rgbConvert = new ColorConversion(red, green, blue);
        rgbClearAndColor(rgbObj, rgbConvert.getRGBcolor(), "RGB", callback);
        currentRGBcolor = rgbConvert.getRGBcolor();
    }

    private void updateRGBhexText(int red, int green, int blue) {
        ColorConversion converter = new ColorConversion(red, green, blue);
        String newColor = converter.getRGBcolor();
        mRGBcolorEdit.setText(newColor);

    }

    private void setupStripSeekBar(SeekBar seekBar, final int colorID) {
        seekBar.setMax(255); // Seek bar values range from 0-255
        seekBar.setProgress(255); // Set value to 255 to start
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                publishStrip(mRedStripSeekBar.getProgress(), mGreenStripSeekBar.getProgress(), mBlueStripSeekBar.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mledStripSwitch.setText(R.string.statusON);
                mledStripSwitch.setTextAppearance(R.style.textScheme1_statusGreen);
                mledStripSwitch.setChecked(true);
                publishStrip(mRedStripSeekBar.getProgress(), mGreenStripSeekBar.getProgress(), mBlueStripSeekBar.getProgress());
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Do something when the progress changes
                int red = mRedStripSeekBar.getProgress(); // Get the red value 0-255
                int green = mGreenStripSeekBar.getProgress(); // Get the green value 0-255
                int blue = mBlueStripSeekBar.getProgress(); // Get the blue value from 0-255
                updateRGBstripHexText(red, green, blue); // Change the value of the EditText to the correct Hex value

                long now = System.currentTimeMillis(); // Only allow 1 publish every 100 milliseconds
                if (now - lastUpdate > 100 && fromUser) { // Threshold and only send when user sliding
                    lastUpdate = now;
                    publishStrip(red, green, blue);
                }
            }

        });
    }

    private void publishStrip(int red, int green, int blue) {
        // Create new PubNub Callback
        Callback callback = getCallback();
        ColorConversion rgbStripConvert = new ColorConversion(red, green, blue);
        rgbClearAndColor(rgbStripObj, rgbStripConvert.getRGBcolor(), "STRIP", callback);
        currentRGBstripColor = rgbStripConvert.getRGBcolor();
    }

    private void updateRGBstripHexText(int red, int green, int blue) {
        ColorConversion converter = new ColorConversion(red, green, blue);
        String newColor = converter.getRGBcolor();
        mRGBcolorStripEdit.setText(newColor);
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

    public void rgbClearAndColor (JSONObject js, String color, String type, Callback callback) {
        ColorConversion convert = new ColorConversion(color);
        int red = convert.getRedValue();
        int green = convert.getGreenValue();
        int blue = convert.getBlueValue();

        try {
            // Remove previous Color status if it exists
            js.remove("Type");
            js.remove("RED");
            js.remove("GREEN");
            js.remove("BLUE");
            // Add new "ON" Status
            js.put("Type", type);
            js.put("RED", red);
            js.put("GREEN", green);
            js.put("BLUE", blue);
            mPubnub.publish(channel1, js, callback);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Create new PubNub Callback
        Callback callback = getCallback();

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
                        // Remove previous "ON" status if it exists
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
                    // Adjust UI style change for Switch Label Status "ON"
                    mRgbLedSwitch.setText(R.string.statusON);
                    mRgbLedSwitch.setTextAppearance(R.style.textScheme1_statusGreen);
                    // Turn RGB LED On (White)
                    rgbClearAndColor(rgbObj, currentRGBcolor, "RGB", callback);
                    // Update Seek Bars
                    ColorConversion rgbConvert = new ColorConversion(currentRGBcolor);
                    mRedSeekBar.setProgress(rgbConvert.getRedValue());
                    mGreenSeekBar.setProgress(rgbConvert.getGreenValue());
                    mBlueSeekBar.setProgress(rgbConvert.getBlueValue());
                    // Update EditText
                    mRGBcolorEdit.setText(currentRGBcolor);
                }
                else {
                    // Adjust UI style change for Switch Label Status "OFF"
                    mRgbLedSwitch.setText(R.string.statusOFF);
                    mRgbLedSwitch.setTextAppearance(R.style.textScheme1_statusRed);
                    // Turn RGB LED OFF (Black)
                    rgbClearAndColor(rgbObj, colorBlack, "RGB", callback);
                    // Update Seek Bars
                    mRedSeekBar.setProgress(0);
                    mGreenSeekBar.setProgress(0);
                    mBlueSeekBar.setProgress(0);
                    // Update EditText
                    mRGBcolorEdit.setText(colorBlack);
                }
                break;

            case R.id.ledStripSwitch:
                Log.i("RGB Strip", isChecked + "");
                if (isChecked==true){
                    // Adjust UI style change for Switch Label Status "ON"
                    mledStripSwitch.setText(R.string.statusON);
                    mledStripSwitch.setTextAppearance(R.style.textScheme1_statusGreen);
                    // Turn On RGB Strip LEDs
                    rgbClearAndColor(rgbStripObj, currentRGBstripColor, "STRIP", callback);
                    // Update Seek Bars
                    ColorConversion rgbStripConvert = new ColorConversion(currentRGBstripColor);
                    mRedStripSeekBar.setProgress(rgbStripConvert.getRedValue());
                    mGreenStripSeekBar.setProgress(rgbStripConvert.getGreenValue());
                    mBlueStripSeekBar.setProgress(rgbStripConvert.getBlueValue());
                    // Update EditText
                    mRGBcolorStripEdit.setText(currentRGBstripColor);

                }
                else {
                    // Adjust UI style change for Switch Label Status "OFF"
                    mledStripSwitch.setText(R.string.statusOFF);
                    mledStripSwitch.setTextAppearance(R.style.textScheme1_statusRed);
                    // Turn Off RGB Strip LEDs
                    rgbClearAndColor(rgbStripObj, colorBlack, "STRIP", callback);
                    // Update Seek Bars
                    mRedStripSeekBar.setProgress(0);
                    mGreenStripSeekBar.setProgress(0);
                    mBlueStripSeekBar.setProgress(0);
                    // Update EditText
                    mRGBcolorStripEdit.setText(colorBlack);
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
