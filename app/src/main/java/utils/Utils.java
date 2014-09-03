package utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.Display;
import android.view.View;

import com.urbik.dunkerque.R;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import wifiConnect.Network;

/**
 * Created by Antoine on 09/07/2014.
 */
//au lieu d'une classe utile faire une class Folder (pour deleteDir et unzipFile et une classe wifi pour les trucs Wifi
public class Utils {
    public static void deleteDir(File folder) {
        if (folder.isDirectory()) {
            File[] list = folder.listFiles();

            if (list != null) {
                for (File tmpF : list) {
                    if (tmpF.isDirectory()) {
                        deleteDir(tmpF);
                    }

                    tmpF.delete();
                }
            }
        }
    }

    //YOU NEED TO ADD  "ZIP4J" JAR FILE
    public static void unzipFile(String source, String destination, String pwd) {
        try {
            ZipFile zipFile = new ZipFile(source);
            if (zipFile.isEncrypted())
                zipFile.setPassword(pwd);
            zipFile.extractAll(destination);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    public static void readFromFile(String pathToTheFile, String fileName) {
        try {
            File file = new File(pathToTheFile + "/" + fileName);
            InputStream inputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String receiveString;
            StringBuilder stringBuilder = new StringBuilder();

            while ((receiveString = bufferedReader.readLine()) != null)
                stringBuilder.append(receiveString);
            inputStream.close();
        } catch (FileNotFoundException e) {
            Log.e("FILE NOT FOUND", "" + e);
        } catch (IOException e) {
            Log.e("CAN'T READ FILE", "" + e);
        }
    }

    //    share a picture
    public static void shareScreenshot(Context c, String imagePath) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        File imageFileToShare = new File(imagePath);
        Uri uri = Uri.fromFile(imageFileToShare);
        share.putExtra(Intent.EXTRA_STREAM, uri);
        c.startActivity(Intent.createChooser(share, c.getResources().getString(R.string.onSharing)));
    }

    //get middle screen x & y coordinate
    public static float[] getMiddleScreenCoordonates(Activity a) {
        Display display = a.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float f[] = new float[2];
        f[0] = (size.x) / 2;
        f[1] = (size.y) / 2;
        return f;
    }

    //    Blur a background activity
    public static void blur(Bitmap bkg, View view,Activity ac) {
        float scaleFactor = 1;
        float radius = 20;
        Bitmap overlay = Bitmap.createBitmap((int) (view.getMeasuredWidth()/scaleFactor),
                (int) (view.getMeasuredHeight()/scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft()/scaleFactor, -view.getTop()/scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bkg, 0, 0, paint);
        overlay = FastBlur.doBlur(overlay, (int)radius, true);
        if(android.os.Build.VERSION.SDK_INT<16) {
            view.setBackgroundDrawable(new BitmapDrawable(ac.getResources(), overlay));
        } else {
            view.setBackground(new BitmapDrawable(ac.getResources(), overlay));
        }
    }
    //    return power of signal of current network values [0-100]
    public static  int getWifiStrength(WifiManager mWifiManager) {
        try {
            int rssi = mWifiManager.getConnectionInfo().getRssi();
            return WifiManager.calculateSignalLevel(rssi, 100);

        } catch (Exception e) {
            return 0;
        }
    }

    public static  boolean checkSsid(String anSsid) {
        //if app is connected to a network
        if (anSsid != null) {
            // compare current ssid with allowed ssid
            for (Network n : Network.values()) {
                if (('"' + n.toString() + '"').equals(anSsid))
                    return true;
            }
        }
        return false;
    }
    public static void clearApplicationData(Context context) {
        File cache = context.getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    Utils.deleteDir(new File(appDir, s));
                }
            }
        }
    }
}
