package com.urbik.dunkerque;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import utils.Utils;
import wifiConnect.AsyncWifiConnect;


/**
 * Created by Antoine on 27/06/2014.
 */
public class Configuration extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        new AsyncWifiConnect(this).execute();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WifiManager mWifimanager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mWifimanager.setWifiEnabled(false);
    }

    public void onContentSelect(View v) {
        new AsyncWifiConnect(this).execute();
    }

}
