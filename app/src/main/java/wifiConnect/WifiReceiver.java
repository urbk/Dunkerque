package wifiConnect;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import static utils.Utils.getWifiStrength;


/**
 * Created by Antoine on 07/08/2014.
 */
public class WifiReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
       WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        manager.startScan();
        Toast.makeText(context,getWifiStrength(manager),Toast.LENGTH_SHORT);
        Log.e("RSSI",getWifiStrength(manager)+"");

    }
}
