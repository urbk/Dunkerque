package wifiConnect;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.urbik.dunkerque.Configuration;

import java.io.File;

import utils.Utils;

import static utils.Utils.getWifiStrength;


/**
 * Created by Antoine on 07/08/2014.
 */
public class WifiReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        WifiManager mWifimanager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (!mWifimanager.isWifiEnabled()) {
            Utils.clearApplicationData(context);

        }
    }


}