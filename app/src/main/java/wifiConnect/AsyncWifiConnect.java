package wifiConnect;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.urbik.dunkerque.R;

import java.util.List;

/**
 * Created by Antoine on 07/08/2014.
 */
public class AsyncWifiConnect extends AsyncTask<String, Integer, String> {
    private Activity mContext;

    private static WifiManager manager;
    private final WifiConfiguration wconf;
    private String ssid;
    private boolean isConnected = false;
    private final ImageView mImageView;
    private final Animation animRotate;

    public AsyncWifiConnect(Activity context) {
        this.mContext = context;
        manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        this.wconf = new WifiConfiguration();
        mImageView = (ImageView) mContext.findViewById(R.id.ic_wifi_work);
        animRotate = AnimationUtils.loadAnimation(mContext,
                R.anim.rotate);
        wconf.status = WifiConfiguration.Status.ENABLED;
        wconf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wconf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wconf.SSID = "";
        this.ssid = manager.getConnectionInfo().getSSID();
    }

    @Override
    protected void onPreExecute() {

        mImageView.startAnimation(animRotate);
        if (!manager.isWifiEnabled())
            manager.setWifiEnabled(true);

    }

    @Override
    protected String doInBackground(String... params) {
//        While wifi isn't fully enabled
        while (manager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        this sleep allow to put object in List<ScanResult>
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!checkSsid(ssid)) {
            manager.startScan();
//            intent.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            List<ScanResult> scanRes = manager.getScanResults();
            for (ScanResult s : scanRes) {
//      if device is connect to a wrong network search if a right network is near
                if (!isConnected) {
                    for (Network n : Network.values()) {
                        if ((n.toString()).equals(s.SSID)) {
                            wconf.SSID = '"' + s.SSID + '"';
                            int netId = manager.addNetwork(wconf);
                            manager.updateNetwork(wconf);
                            manager.enableNetwork(netId, true);
                            while (manager.getConnectionInfo().getSupplicantState() != SupplicantState.COMPLETED || !checkSsid(ssid) || getIpAdress().equals("0.0.0.0")) {
                                ssid = manager.getConnectionInfo().getSSID();
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (getWifiStrength() > 79)
                                isConnected = true;
                        }
                    }
                }

            }
        } else {
            isConnected = true;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (isConnected) {
            mImageView.clearAnimation();
            mImageView.setImageResource(R.drawable.ic_check);
//          AsyncTask To Download needed content
            new ContentLoader(mContext, getIpAdress()).execute();

        } else {
//            unable to find a appropriate network
            Toast.makeText(mContext.getApplicationContext(), R.string.noNetworkAvailable, Toast.LENGTH_SHORT).show();
        }
        super.onPostExecute(result);
    }


    private boolean checkSsid(String anSsid) {
        //if app is connected to a network
        if (ssid != null) {
            // compare current ssid with allowed ssid
            for (Network n : Network.values()) {
                if (('"' + n.toString() + '"').equals(anSsid))
                    return true;
            }
        }
        return false;
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
