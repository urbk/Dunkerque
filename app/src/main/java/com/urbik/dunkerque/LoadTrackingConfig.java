package com.urbik.dunkerque;

import android.app.ProgressDialog;
import android.os.AsyncTask;


import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.jni.IMetaioSDKAndroid;

/**
 * Created by Antoine on 30/07/2014.
 */
public class LoadTrackingConfig extends AsyncTask<String, Void,Boolean> {
    ProgressDialog progress;
    IMetaioSDKAndroid metaioSDK;
    ARViewActivity mArView;

    LoadTrackingConfig(ARViewActivity arV, IMetaioSDKAndroid metaio) {
        super();
        mArView = arV;
        metaioSDK = metaio;
    }

    protected void onPreExecute() {
        progress = new ProgressDialog(mArView);
        progress.show();
    }

    @Override
    protected Boolean doInBackground(String... params) {

        boolean result = metaioSDK.setTrackingConfiguration(params[0]);
        if (progress.isShowing()) {
            progress.dismiss();
        }
        return result;
    }


}
