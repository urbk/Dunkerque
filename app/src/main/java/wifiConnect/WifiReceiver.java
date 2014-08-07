package wifiConnect;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;


/**
 * Created by Antoine on 07/08/2014.
 */
public class WifiReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
       WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
       switch (manager.getWifiState()){
           case WifiManager.WIFI_STATE_DISABLED:

       }
    }
}
