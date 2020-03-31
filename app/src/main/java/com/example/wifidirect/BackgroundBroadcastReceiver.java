package com.example.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;

public class BackgroundBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel mChannel;
    private ServiceNoDelay mIntentService;

    public BackgroundBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, ServiceNoDelay intentService) {
        super();
        this.p2pManager = manager;
        this.mChannel = channel;
        this.mIntentService = intentService;
    }

    public BackgroundBroadcastReceiver() {

    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
//             Call WifiP2pManager.requestPeers() to get a list of current peers
            if (p2pManager != null){
                p2pManager.requestPeers(mChannel,mIntentService.peerListListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

//            WifiP2pDevice myDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
//            String name = myDevice.deviceName;
//            name = name.replaceAll("'","");
//            mIntentService.myDeviceInfo.name = name;
            // Respond to this device's wifi state changing
//            if (mManager == null) {
//                return;
//            }
//
//            NetworkInfo networkInfo = (NetworkInfo) intent
//                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
//
//            if (networkInfo.isConnected()) {
//
//                // We are connected with the other device, request connection
//                // info to find group owner IP
//
//                mManager.requestConnectionInfo(mChannel, mActivity.con);
//            }
        }
    }
}