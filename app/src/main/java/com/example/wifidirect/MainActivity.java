package com.example.wifidirect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.READ_SMS;

public class MainActivity extends AppCompatActivity {

    IntentFilter intentFilter;
    BroadcastReceiver mReceiver;
    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiP2pManager.PeerListListener peerListListener;
    List<WifiP2pDevice> peerList = new ArrayList<>();
    String[] deviceNames;
    WifiP2pDevice[]  devices;
    ListView listView;
    LocationListener locationListener;
    Location lastLocation;
    String _android_id;
    Date date;
    TextView tvAndroidId;
    TextView tvDate;
    TextView tvLocation;
    TextView tvState;
    MyDeviceInfo myDeviceInfo = new MyDeviceInfo();
    FusedLocationProviderClient fusedLocationClient;
    LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new ForegroundBroadcastReciever(mManager, mChannel, this);
        _android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        listView = findViewById(R.id.listView);
        date = Calendar.getInstance().getTime();
        tvAndroidId = findViewById(R.id.androidId);
        tvDate = findViewById(R.id.dateTime);
        tvLocation = findViewById(R.id.location);
        tvState = findViewById(R.id.discoverState);
        tvState = findViewById(R.id.discoverState);


        // Database yazildigini gormek icin
        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String s1 = sh.getString("date", "");
        ///////////////////////

        ServiceNoDelay mSensorService = new ServiceNoDelay(getApplicationContext());
        Intent mServiceIntent = new Intent(getApplicationContext(), mSensorService.getClass());
        if (!isMyServiceRunning(mSensorService.getClass())) {
            startService(mServiceIntent);
        }

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    lastLocation = location;
                }
            }
        };




        // oreo ve ustu location permisson
        boolean permissionAccessCoarseLocationApproved =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        if (permissionAccessCoarseLocationApproved) {
            boolean backgroundLocationPermissionApproved =
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            == PackageManager.PERMISSION_GRANTED;

            if (backgroundLocationPermissionApproved) {
                // App can access location both in the foreground and in the background.
                // Start your service that doesn't have a foreground service type
                // defined.
            } else {
                // App can only access location in the foreground. Display a dialog
                // warning the user that your app must have all-the-time access to
                // location in order to function properly. Then, request background
                // location.
                ActivityCompat.requestPermissions(this, new String[] {
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        1);
            }
        } else {
            // App doesn't have access to the device's location at all. Make full request
            // for permission.
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    },
                    1);
        }


        // oreo ve ustu wifidirect acmak icin
        try {
            Class<?> wifiManager = Class
                    .forName("android.net.wifi.p2p.WifiP2pManager");

            Method method = wifiManager
                    .getMethod(
                            "enableP2p",
                            new Class[] { android.net.wifi.p2p.WifiP2pManager.Channel.class });

            method.invoke(mManager, mChannel);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                tvState.setText("Etkin");
            }

            @Override
            public void onFailure(int reasonCode) {
                tvState.setText("Hata");
            }
        });
        peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {
                if (!peers.getDeviceList().equals(peerList)){
                    peerList.clear();
                    peerList.addAll(peers.getDeviceList());
                    devices = new WifiP2pDevice[peers.getDeviceList().size()];
                    deviceNames = new String[peers.getDeviceList().size()];
                    if (peerList.size() == 0){
                        Toast.makeText(getApplicationContext(),"Cihaz bulunamadi",Toast.LENGTH_SHORT).show();
                        return;
                    }else{
                        int index = 0;
                        for(WifiP2pDevice device : peerList){
                            deviceNames[index] = device.deviceName;
                            devices[index] = device;
                            index++;
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,deviceNames){
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent){
                            View view = super.getView(position, convertView, parent);
                            TextView tv = (TextView) view.findViewById(android.R.id.text1);
                            tv.setTextColor(Color.BLACK);
                            return view;
                        }
                    };

                    listView.setAdapter(adapter);
                }
            }
        };

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("peer", String.valueOf(devices[position]));
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = devices[position].deviceAddress;
                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(),"Baglandi",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(getApplicationContext(),"Hata",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            lastLocation = location;
                        }
                    }
                });

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                updateMyInfo();
            }
        }, 0, 5000);

        getMyDeviceInfo();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (Build.VERSION.SDK_INT >= 23) {
                    control();
                } else {
                    getLocation();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    public class SensorRestarterBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(SensorRestarterBroadcastReceiver.class.getSimpleName(), "Service Stops! Oops!!!!");
            context.startService(new Intent(context, ServiceNoDelay.class));

        }
    }

    private void getMyDeviceInfo(){
        if (ActivityCompat.checkSelfPermission(this, READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, READ_PHONE_NUMBERS) ==
                        PackageManager.PERMISSION_GRANTED ) {
            TelephonyManager tMgr = (TelephonyManager)   this.getSystemService(Context.TELEPHONY_SERVICE);
            myDeviceInfo.phoneNumber = tMgr.getLine1Number();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                myDeviceInfo.meid = tMgr.getMeid();
                myDeviceInfo.imei = tMgr.getImei();
            }else{
                myDeviceInfo.meid = tMgr.getDeviceId();
                myDeviceInfo.imei = tMgr.getDeviceId();
            }
        } else {
            requestPermission();
        }
        WifiInfo info = wifiManager.getConnectionInfo();
        String address = info.getMacAddress();
        myDeviceInfo.macAdress = address;
        myDeviceInfo.androidId = _android_id;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,new String[]{READ_SMS, READ_PHONE_NUMBERS, READ_PHONE_STATE}, 100);
    }

    public void control() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            getLocation();
        }
    }

    public void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 1, locationListener);
            Location locationn = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (locationn == null) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, locationListener);
                locationn = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            lastLocation = locationn;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
        switch (requestCode) {
            case 100:
                getMyDeviceInfo();
        }
    }

    public void  updateMyInfo(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                date = Calendar.getInstance().getTime();
                tvDate.setText(date.toString());
                tvAndroidId.setText(_android_id);
                if (lastLocation != null){
                    tvLocation.setText(lastLocation.getLongitude() + " " + lastLocation.getLatitude());
                }
            }
        });

    }

    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, intentFilter);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }
}
