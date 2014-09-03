package com.urbik.dunkerque;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.ETRACKING_STATE;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;
import java.util.Hashtable;
import java.util.Map;
import utils.BackgroundVideoTask;
import utils.Utils;
import wifiConnect.AsyncWifiConnect;


public class Tracking extends ARViewActivity implements MediaPlayer.OnPreparedListener, View.OnTouchListener {
    private MetaioSDKCallbackHandler mCallbackHandler;
    private ContentType ct = ContentType.None;
    private final MediaPlayer mMediaPlayer = new MediaPlayer();
    private VideoView vv;
    private final Map<String, EnumModel> trackingModel = new Hashtable<String, EnumModel>();
    private String currentTargetName;
    private static String PATH_TO_IMAGE_TRACKING_CONF, PATH_TO_CAD_TRACKING_CONF;
    private final Vector3d v3d = new Vector3d(0.0f, 0.0f, 0.0f);
    private Animation animBounce, animFadeIn, animFadeOut;
    private LinearLayout contentL;
    private ImageButton leftArrow, rightArrow, cadReset, guide;
    private float fingerX, fingerY;
    String connectivity_context = Context.WIFI_SERVICE;
    ImageView iv;
    TextView tv_welcome;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        mCallbackHandler = new MetaioSDKCallbackHandler();
        mGUIView.setOnTouchListener(this);
//        mGUIView.setOnLongClickListener(this);
        PATH_TO_IMAGE_TRACKING_CONF = AssetsManager.getAbsolutePath() + "/Img/TrackingData_MarkerlessFast.xml";
        PATH_TO_CAD_TRACKING_CONF = "";
        animBounce = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        final WifiManager wifi = (WifiManager) getSystemService(connectivity_context);
//
//        registerReceiver(new BroadcastReceiver() {
//
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                WifiInfo info = wifi.getConnectionInfo();
//                iv = (ImageView) findViewById(R.id.lost_connexion);
//                ib = (ImageButton) findViewById(R.id.retry_co);
////           RSSI BETWEEN 0 (max) & -120 (min)
//                if (info.getRssi() <= AsyncWifiConnect.RSSI_STRENGH_REQUIRED) {
//                    Toast.makeText(getApplicationContext(), info.getRssi() + " Trop faible", Toast.LENGTH_SHORT).show();
//                    iv.setVisibility(View.VISIBLE);
//                    ib.setVisibility(View.VISIBLE);
//                }
//                unregisterReceiver(this);
//            }
//
//        }, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_screenshot:
                String pathStorage = Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DCIM + "/Dunkerque";
                onScreenshot(pathStorage, "Dunkerque");
            case R.id.menu_help:
                break;
            case R.id.menu_reglage:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tracking, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCallbackHandler.delete();
        mCallbackHandler = null;
        WifiManager mWifimanager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mWifimanager.setWifiEnabled(false);
    }

    @Override
    public void onBackPressed() {
        if (currentTargetName != null) {
            contentGestion(ct, vv, mMediaPlayer, PATH_TO_CAD_TRACKING_CONF, mTrack);
            if (mTrack == TrackState.CAD) {
                mTrack = TrackState.NOGPS;
                mState = EState.NOTTRACKING;
                if (cadReset != null) cadReset.setVisibility(View.GONE);
                if (currentTargetName.substring(0, 9).equals("Sandettie") && !currentTargetName.substring(9).equals("-Img")) {
                    leftArrow.setVisibility(View.GONE);
                    rightArrow.setVisibility(View.GONE);
                    guide.setVisibility(View.VISIBLE);
                }
                new LoadTrackingConfig(this, metaioSDK).execute(PATH_TO_IMAGE_TRACKING_CONF);
            } else if (currentTargetName.equals(EnumModel.SANDETTIE_IMG.toString()) && mState == EState.TRACKING) {
                for (Map.Entry<String, EnumModel> entry : trackingModel.entrySet()) {
                    entry.getValue().getModel().getIGeometry().setVisible(false);
                }
                contentL.setVisibility(View.VISIBLE);
                contentL.startAnimation(animBounce);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        super.onTouch(v, event);
        int action = MotionEventCompat.getActionMasked(event);
        if (action == MotionEvent.ACTION_DOWN) {
            fingerX = event.getX();
            fingerY = event.getY();
        }
        v.onTouchEvent(event);
        return true;
    }

    //stream videocontent
    void onVideo(String ip, String fileName) {
//    enum  used in onTracking event
        ct = ContentType.Video;
        contentGestion(ct, vv, mMediaPlayer, PATH_TO_CAD_TRACKING_CONF, mTrack);
        vv = (VideoView) findViewById(R.id.videoview);
        new BackgroundVideoTask(this, vv).execute(AssetsManager.getAbsolutePath() + "/video.mp4"); //AssetsManager.getAbsolutePath()+"/video.mp4"
    }

    //when sound is ready to be streamed we can start it
    public void onPrepared(MediaPlayer mediaplayer) {
        mediaplayer.start();
    }

    public void onContentSelect(View v) {
        Model currentModel = trackingModel.get(currentTargetName).getModel();
        switch (v.getId()) {
            case R.id.videoContent:

                try {
                    contentL.clearAnimation();
                    contentL.setVisibility(View.GONE);
                    onVideo(AsyncWifiConnect.getIpAdress(), "video.mp4");
                } catch (NullPointerException e) {

                }
                break;
            case R.id.audioContent:
                contentL.clearAnimation();
                contentL.setVisibility(View.GONE);
                ct = ContentType.Audio;
                contentGestion(ct, vv, mMediaPlayer, PATH_TO_CAD_TRACKING_CONF, mTrack);
                Utils.audioStream(AsyncWifiConnect.getIpAdress(), "near.mp3", mMediaPlayer, this);
                break;
            case R.id.threeDContent:
                contentL.clearAnimation();
                contentL.setVisibility(View.GONE);
                currentModel.getIGeometry().setVisible(true);
                if (currentModel.getContentType() == ContentType.Animations)
                    currentModel.getIGeometry().startAnimation(currentModel.getIGeometry().getAnimationNames().get(0), true);
                break;
            case R.id.trackCadSandettie:
                contentL.clearAnimation();
                contentL.setVisibility(View.GONE);
                PATH_TO_CAD_TRACKING_CONF = AssetsManager.getAbsolutePath() + "/CAD/Sandettie/Tracking.xml";
                currentModel.getIGeometry().setVisible(false);
                mTrack = TrackState.CAD;
                cadReset = (ImageButton) findViewById(R.id.resetCad);
                leftArrow = (ImageButton) findViewById(R.id.arrow_left);
                rightArrow = (ImageButton) findViewById(R.id.arrow_right);
                cadReset.setVisibility(View.VISIBLE);
                leftArrow.setVisibility(View.VISIBLE);
                rightArrow.setVisibility(View.VISIBLE);
                for (Map.Entry<String, EnumModel> entry : trackingModel.entrySet()) {
                    entry.getValue().getModel().getIGeometry().setVisible(false);
                }
                manageCadTracking(trackingModel, EnumModel.SANDETTIE_CAD_DISPLAY_MODEL_1, EnumModel.SANDETTIE_CAD_VISUAL_HELP, PATH_TO_CAD_TRACKING_CONF, true);
                manageCadTracking(trackingModel, EnumModel.SANDETTIE_CAD_DISPLAY_MODEL_2, EnumModel.SANDETTIE_CAD_VISUAL_HELP, PATH_TO_CAD_TRACKING_CONF, false);
                manageCadTracking(trackingModel, EnumModel.SANDETTIE_CAD_DISPLAY_MODEL_3, EnumModel.SANDETTIE_CAD_VISUAL_HELP, PATH_TO_CAD_TRACKING_CONF, false);
                guide.setVisibility(View.GONE);
                break;
            case R.id.guide:
                PATH_TO_CAD_TRACKING_CONF = AssetsManager.getAbsolutePath() + "/CAD/Panneau/Tracking.xml";
                currentModel.getIGeometry().setVisible(false);
                mTrack = TrackState.CAD;
                for (Map.Entry<String, EnumModel> entry : trackingModel.entrySet()) {
                    entry.getValue().getModel().getIGeometry().setVisible(false);
                }
                manageCadTracking(trackingModel, EnumModel.PANNEAU_CAD_DISPLAY_MODEL, EnumModel.PANNEAU_CAD_VISUAL_HELP, PATH_TO_CAD_TRACKING_CONF, true);
                break;
            case R.id.resetCad:
                metaioSDK.setTrackingConfiguration(PATH_TO_CAD_TRACKING_CONF);
                break;
            case R.id.arrow_left:
                onSwitchDisplayedModel(trackingModel, EnumModel.SANDETTIE_CAD_DISPLAY_MODEL_1, 3, false);
                break;
            case R.id.arrow_right:
                onSwitchDisplayedModel(trackingModel, EnumModel.SANDETTIE_CAD_DISPLAY_MODEL_1, 3, true);
                break;
            case R.id.retry_co:
                WifiManager mWifimanager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                mWifimanager.setWifiEnabled(false);
                iv.setVisibility(View.GONE);
                startActivity(new Intent(this, Configuration.class));
                finish();
                break;
        }
    }


    //******************************************************************************************************
//    ********************************************METAIO****************************************************
//    ******************************************************************************************************

    @Override
    protected int getGUILayout() {
        return R.layout.activity_tracking;
    }

    @Override
    protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
        return mCallbackHandler;
    }

    @Override
    protected void loadContents() {
/*        Key of map it's just to help dev to know which marker is used.
Architecture of Key String : Name of marker + - + Type of Tracking + - + Type of model (D : Display,V: Visual Help) + - + (Optional) id of displayed model
 */

        try {
//            IMAGE TRACKING [Animations & Movable content are in]

            trackingModel.put("Duchesse-Img", EnumModel.DUCHESSE_IMG);
            trackingModel.put("Sandettie-Img", EnumModel.SANDETTIE_IMG);

//            CAD Tracking
            trackingModel.put("Sandettie-CAD-D-1", EnumModel.SANDETTIE_CAD_DISPLAY_MODEL_1);
            trackingModel.put("Sandettie-CAD-D-2", EnumModel.SANDETTIE_CAD_DISPLAY_MODEL_2);
            trackingModel.put("Sandettie-CAD-D-3", EnumModel.SANDETTIE_CAD_DISPLAY_MODEL_3);
            trackingModel.put("Sandettie-CAD-V", EnumModel.SANDETTIE_CAD_VISUAL_HELP);

            trackingModel.put("Panneau-CAD-D", EnumModel.PANNEAU_CAD_DISPLAY_MODEL);
            trackingModel.put("Panneau-CAD-V", EnumModel.PANNEAU_CAD_VISUAL_HELP);

            metaioSDK.setTrackingConfiguration(PATH_TO_IMAGE_TRACKING_CONF);

        } catch (Exception e) {
            Log.e("ERREUR", "SET TRACKING CONFIGURATION ERROR");
        }
        createGeometryModel(EnumModel.values());
    }

    @Override
    protected void onGeometryTouched(final IGeometry geometry) {
        if (geometry.equals(trackingModel.get(EnumModel.DUCHESSE_IMG.toString()).getModel().getIGeometry()))
            rotateModel(fingerX, fingerY, geometry);
        if (geometry.equals(trackingModel.get(EnumModel.PANNEAU_CAD_DISPLAY_MODEL.toString()).getModel().getIGeometry())) {
            mTrack = TrackState.NOGPS;
            mState = EState.NOTTRACKING;
            new LoadTrackingConfig(this, metaioSDK).execute(PATH_TO_IMAGE_TRACKING_CONF);
           if(tv_welcome!=null) tv_welcome.setVisibility(View.GONE);
        }
    }

    private final class MetaioSDKCallbackHandler extends IMetaioSDKCallback {
        @Override
        public void onSDKReady() {
            // show GUI
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mGUIView.setVisibility(View.VISIBLE);
                    guide = (ImageButton) findViewById(R.id.guide);
                    guide.setVisibility(View.VISIBLE);

                }
            });
        }

        @Override
        public void onAnimationEnd(IGeometry geometry, String animationName) {
            MetaioDebug.log("UnifeyeCallbackHandler.onAnimationEnd: " + animationName);
        }

        @Override
        public void onTrackingEvent(final TrackingValuesVector trackingValues) {
            final String targetName = trackingValues.get(0).getCosName();
            if (trackingValues.get(0).getState() == ETRACKING_STATE.ETS_FOUND) {
                mState = EState.TRACKING;
            } else
                mState = EState.NOTTRACKING;
            if (mTrack != TrackState.CAD) {
                //ASSOCIATE RIGHT COS WITH RIGHT MODEL
                for (Map.Entry<String, EnumModel> entry : trackingModel.entrySet()) {
                    entry.getValue().getModel().getIGeometry().setVisible(false);
                }
                for (Map.Entry<String, EnumModel> entry : trackingModel.entrySet()) {
                    if (entry.getKey().equals(targetName)) {
                        if (!entry.getKey().equals(EnumModel.DUCHESSE_IMG.toString())) // pour l'image du sandettie on affiche un menu et non un model
                            entry.getValue().getModel().getIGeometry().setVisible(true);
//                        sinon on affiche le model
                        entry.getValue().getModel().getIGeometry().setCoordinateSystemID(entry.getValue().getModel().getModelCsi());
                        currentTargetName = entry.getKey();
                    }
                }
            }
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    contentL = (LinearLayout) findViewById(R.id.contentLayout);
                    if (mTrack != TrackState.CAD) {
                        if (currentTargetName.equals(EnumModel.DUCHESSE_IMG.toString()) && mState == EState.TRACKING) {
                            contentL.setVisibility(View.VISIBLE);
                            contentL.startAnimation(animBounce);
                        } else {
                            contentL.clearAnimation();
                            contentL.setVisibility(View.GONE);
                        }
                    } else {
//                        **********************************************************ICI ICICICICICICI
                        if (targetName.equals(EnumModel.PANNEAU_CAD_DISPLAY_MODEL.toString())) {
                            tv_welcome=(TextView) findViewById(R.id.tv_welcome);
                            tv_welcome.setVisibility(View.VISIBLE);
                        }
                        Log.e("test",targetName+" "+EnumModel.PANNEAU_CAD_DISPLAY_MODEL.toString());
                        contentL.clearAnimation();
                        contentL.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

//    *******************************************END**********************************************
//    *****************************************METAIO*********************************************
//    ********************************************************************************************
}
