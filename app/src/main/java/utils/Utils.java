package utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.util.Log;
import android.view.Display;

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

/**
 * Created by Antoine on 09/07/2014.
 */
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

}
