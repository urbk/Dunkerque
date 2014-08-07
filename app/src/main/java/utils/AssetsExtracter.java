package utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.metaio.sdk.MetaioDebug;
import com.metaio.tools.io.AssetsManager;
import com.urbik.dunkerque.BuildConfig;

import java.io.IOException;

/**
 * Created by Antoine on 29/07/2014.
 */
public class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean> {
    private final Context mContext;
    public AssetsExtracter(Context c){
        mContext=c;
    }
    @Override
    protected Boolean doInBackground(Integer... params) {
        try {
            // Extract all assets and overwrite existing files if debug build
            AssetsManager.extractAllAssets(mContext, BuildConfig.DEBUG);
        } catch (IOException e) {
            MetaioDebug.printStackTrace(Log.ERROR, e);
            return false;
        }

        return true;
    }
}
