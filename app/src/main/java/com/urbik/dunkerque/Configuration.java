package com.urbik.dunkerque;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import utils.Utils;
import wifiConnect.AsyncWifiConnect;
import wifiConnect.WifiConnect;

/**
 * Created by Antoine on 27/06/2014.
 */
public class Configuration extends Activity {
    private ProgressBar mProgressBar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        // On récupère les composants de notre layout
//        DECOMMENTER POUR ACTIVER TELECHARGEMENT CONTENU
//        ProgressBar  mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
//        WifiConnect mWifiConnect = new WifiConnect(this);
//        mWifiConnect.start();
new AsyncWifiConnect(this).execute();
//        Intent intent = new Intent(this, Acceuil.class);
//       this.startActivity(intent);

    }



    public void onDestroy() {
        super.onDestroy();
        Utils.deleteDir(this.getCacheDir());
    }


}
