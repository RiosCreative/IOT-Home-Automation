package com.rioscreative.iotpracticum.ui;

        import android.os.Handler;
        import android.os.Message;
        import android.os.PowerManager;

        import com.pubnub.api.Callback;
        import com.pubnub.api.Pubnub;
        import com.pubnub.api.PubnubException;

        import android.app.Service;
        import android.content.Context;
        import android.content.Intent;
        import android.os.IBinder;
        import android.util.Log;
        import android.widget.Toast;

public class PubNubService extends Service {

    String PUBLISH_KEY = "pub-c-5d4c8a5c-b2d4-4974-a11c-4a3fc3c2ad8d";
    String SUBSCRIBE_KEY = "sub-c-d52e3da6-c9e4-11e5-b684-02ee2ddab7fe";
    String channel = "IOTpracticum";
    Pubnub pubnub = new Pubnub(PUBLISH_KEY, SUBSCRIBE_KEY, false);
    PowerManager.WakeLock wl = null;

    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String pnMsg = msg.obj.toString();

            final Toast toast = Toast.makeText(getApplicationContext(), pnMsg, Toast.LENGTH_SHORT);
            toast.show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 200);

        }
    };

    private void notifyUser(Object message) {

        Message msg = handler.obtainMessage();

        try {
            final String obj = (String) message;
            msg.obj = obj;
            handler.sendMessage(msg);
            Log.i("Received msg : ", obj.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "PubnubService created...", Toast.LENGTH_LONG).show();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SubscribeAtBoot");
        if (wl != null) {
            wl.acquire();
            Log.i("PUBNUB", "Partial Wake Lock : " + wl.isHeld());
            Toast.makeText(this, "Partial Wake Lock : " + wl.isHeld(), Toast.LENGTH_LONG).show();
        }

        Log.i("PUBNUB", "PubnubService created...");
        try {
            pubnub.subscribe(new String[] { channel }, new Callback() {
                public void connectCallback(String channel) {
                    notifyUser("CONNECT on channel:" + channel);
                }

                public void disconnectCallback(String channel) {
                    notifyUser("DISCONNECT on channel:" + channel);
                }

                public void reconnectCallback(String channel) {
                    notifyUser("RECONNECT on channel:" + channel);
                }

                @Override
                public void successCallback(String channel, Object message) {
                    notifyUser(channel + " " + message.toString());
                }

                @Override
                public void errorCallback(String channel, Object message) {
                    notifyUser(channel + " " + message.toString());
                }
            });
        } catch (PubnubException e) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wl != null) {
            wl.release();
            Log.i("PUBNUB", "Partial Wake Lock : " + wl.isHeld());
            Toast.makeText(this, "Partial Wake Lock : " + wl.isHeld(), Toast.LENGTH_LONG).show();
            wl = null;
        }
        Toast.makeText(this, "PubnubService destroyed...", Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

}
