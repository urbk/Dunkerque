package wifiConnect;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.urbik.dunkerque.R;

import java.util.List;


/**
 * Created by Antoine on 25/06/2014.
 */
public class WifiConnect extends Thread {
    private final Activity mContext;
    private static WifiManager manager;
    private final WifiConfiguration wconf;
    private String ssid;
    private final ConnectivityManager connManager;
    private final NetworkInfo networkInfo;
    private static String ip;
    private boolean b = false;
    private final ImageView mImageView;
    private final Animation animRotate;

    public WifiConnect(Activity mContext) {
        this.ssid = null;
        this.mContext = mContext;
        manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        this.connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        this.wconf = new WifiConfiguration();
        wconf.status = WifiConfiguration.Status.ENABLED;
        wconf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wconf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wconf.SSID = "";
        mImageView = (ImageView) mContext.findViewById(R.id.ic_wifi_work);
        animRotate = AnimationUtils.loadAnimation(mContext,
                R.anim.rotate);
        mImageView.startAnimation(animRotate);
        if (!manager.isWifiEnabled()) {
            manager.setWifiEnabled(true);
        }

    }

    public void run() {
//SI SSID PAS OK SINON
        if (checkSsid(manager.getConnectionInfo().getSSID()))
            onReceive();
        else {
            mImageView.clearAnimation();
            mImageView.setImageResource(R.drawable.ic_check);
            new ContentLoader(mContext, getIpAdress()).execute();
        }
    }


    //        check if network ssid is a good one (if not null)
    private boolean checkSsid(String anSsid) {
        //if app is connected to a network
        if (ssid != null) {
            // compare current ssid with allowed ssid
            for (Network n : Network.values()) {
                if (('"' + n.toString() + '"').equals(anSsid))
                    return false;
            }
            return true;
        } else
            return true;
    }



    private void onReceive() {
        manager.startScan();
        IntentFilter intent = new IntentFilter();
        intent.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<ScanResult> scanRes = manager.getScanResults();
                for (ScanResult s : scanRes) {
                    if (!b) {
//                        if device is connect to a wrong network search if a right network is near
                        for (Network n : Network.values()) {
                            if ((n.toString()).equals(s.SSID) && getWifiStrength() > 79) {
                                wconf.SSID = '"' + s.SSID + '"';
                                int netId = manager.addNetwork(wconf);
                                manager.updateNetwork(wconf);
                                manager.enableNetwork(netId, true);
                                b = true;
                                while (manager.getConnectionInfo().getSupplicantState() != SupplicantState.COMPLETED || checkSsid(ssid)) {
                                    ssid = manager.getConnectionInfo().getSSID();
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                ip = getIpAdress();
                                mImageView.clearAnimation();
                                mImageView.setImageResource(R.drawable.ic_check);
//                                AsyncTask To Download needed content
                                new ContentLoader(mContext, getIpAdress()).execute();
                            } else {
                                Toast.makeText(mContext, "Trop eloigné du réseau", Toast.LENGTH_SHORT);
                            }
                        }
                    }

                }
//                unable to find an appropriate network
                if (!b)
                    Toast.makeText(mContext.getApplicationContext(), R.string.noNetworkAvailable, Toast.LENGTH_SHORT).show();
                else
                    mContext.unregisterReceiver(this);
            }
        };
        mContext.registerReceiver(receiver, intent);
    }

    //    Get ip of the router
    public static String getIpAdress() {
        int ip = manager.getDhcpInfo().gateway;
        return String.format(
                "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));
    }

    //    return power of signal
    private int getWifiStrength() {
        try {
            int rssi = manager.getConnectionInfo().getRssi();
            return WifiManager.calculateSignalLevel(rssi, 100);
        } catch (Exception e) {
            return 0;
        }
    }
}
