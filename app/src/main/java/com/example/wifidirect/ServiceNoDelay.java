package com.example.wifidirect;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.JobIntentService;
import androidx.core.content.ContextCompat;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ServiceNoDelay extends Service {
    Context context;
    SharedPreferences sharedPreferences;
    IntentFilter intentFilter;
    BackgroundBroadcastReceiver mReceiver;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiP2pManager.PeerListListener peerListListener;
    List<WifiP2pDevice> peerList = new ArrayList<>();
    Date date;
    SharedPreferences.Editor myEdit;
    public ServiceNoDelay(Context applicationContext) {
        super();
        context = applicationContext;
        Log.i("HERE", "here service created!");
    }

    public ServiceNoDelay() {}


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(getApplicationContext(), getMainLooper(), null);
        mReceiver = new BackgroundBroadcastReceiver(mManager, mChannel, this);
        peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                peerList.clear();
                peerList.addAll(peers.getDeviceList());
                date = Calendar.getInstance().getTime();

                if (peerList.size() == 0){
                    Log.i("in timer", "cihaz bulanamadi" + date.toString());
                    return;
                }else{
                    for(WifiP2pDevice device : peerList){
                        SharedPreferences.Editor myEdit
                                = sharedPreferences.edit();

                        myEdit.putString(
                                "date",
                                device.deviceName+ "   " + date.toString());
                        myEdit.commit();
                    }
                }
            }
        };
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        Log.i("EXIT", "ondestroy!");
//        Intent broadcastIntent = new Intent("ac.in.ActivityRecognition.RestartSensor");
//        sendBroadcast(broadcastIntent);
//        stoptimertask();
    }


    private Timer timer;
    private TimerTask timerTask;

    public void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                //                checkMyDeviceExist();
                mManager.requestPeers(mChannel,peerListListener);
                date = Calendar.getInstance().getTime();
                Log.i("in timer", "in timer ++++  " + date.toString());
            }
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences("MySharedPref",
                Context.MODE_PRIVATE);
        myEdit = sharedPreferences.edit();
    }
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}