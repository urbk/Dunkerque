package com.urbik.dunkerque;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.ETRACKING_STATE;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import utils.BackgroundVideoTask;
import utils.FastBlur;
import utils.Utils;
import wifiConnect.AsyncWifiConnect;
import wifiConnect.ContentLoader;


public class Tracking extends ARViewActivity implements MediaPlayer.OnPreparedListener, View.OnLongClickListener, View.OnTouchListener {
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
    private ImageButton leftArrow, rightArrow, cadReset, mImageButton = null, guide,ib;
    private float fingerX, fingerY;
    String connectivity_context = Context.WIFI_SERVICE;
    ImageView iv;

    private enum EState {
        NOTTRACKING,
        TRACKING
    }

    private enum TrackState {
        GPS,
        NOGPS,
        CAD
    }

    private EState mState = EState.NOTTRACKING;
    private TrackState mTrack = TrackState.NOGPS;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        mCallbackHandler = new MetaioSDKCallbackHandler();
        mGUIView.setOnTouchListener(this);
        mGUIView.setOnLongClickListener(this);
        PATH_TO_IMAGE_TRACKING_CONF = AssetsManager.getAbsolutePath() + "/Img/TrackingData_MarkerlessFast.xml";
        PATH_TO_CAD_TRACKING_CONF = "";
        animBounce = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
        final WifiManager wifi = (WifiManager) getSystemService(connectivity_context);

        registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                WifiInfo info = wifi.getConnectionInfo();
                iv = (ImageView) findViewById(R.id.lost_connexion);
                 ib = (ImageButton) findViewById(R.id.retry_co);
//           RSSI BETWEEN 0 (max) & -120 (min)
                if (info.getRssi() <= AsyncWifiConnect.RSSI_STRENGH_REQUIRED) {
                    Toast.makeText(getApplicationContext(), info.getRssi() + " Trop faible", Toast.LENGTH_SHORT).show();
                    iv.setVisibility(View.VISIBLE);
                    ib.setVisibility(View.VISIBLE);
                }
                unregisterReceiver(this);
            }

        }, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));

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
            contentGestion(ct, vv, mMediaPlayer);
            if (mTrack == TrackState.CAD) {
                mTrack = TrackState.NOGPS;
                mState = EState.NOTTRACKING;
                cadReset.setVisibility(View.GONE);
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
    public boolean onLongClick(View v) {
        if (currentTargetName.equals(EnumModel.DUCHESSE_IMG.toString()) && mState == EState.TRACKING) {
            mImageButton = (ImageButton) findViewById(R.id.rotateModel);

            float xOffset = 42;
            float yOffset = 39;
            fingerX = fingerX - xOffset;
            fingerY = fingerY - yOffset;
            mImageButton.setX(fingerX);
            mImageButton.setY(fingerY);
            mImageButton.setAnimation(animFadeIn);

            if (mImageButton.getVisibility() != View.VISIBLE)
                mImageButton.setVisibility(View.VISIBLE);
        }
        return true;
    }

    //
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        Model currentModel = trackingModel.get(currentTargetName).getModel();
        IGeometry mTempModel = currentModel.getIGeometry();
        if (action == MotionEvent.ACTION_DOWN) {
            fingerX = event.getX();
            fingerY = event.getY();
        }
        v.onTouchEvent(event);
        boolean upDown = false;
        boolean leftRight = false;
        if (action == MotionEvent.ACTION_UP && mImageButton != null && mImageButton.getVisibility() == View.VISIBLE) {
            mImageButton.setAnimation(animFadeOut);
            mImageButton.startAnimation(animFadeOut);
            mImageButton.setVisibility(View.GONE);
        }
        if (action == MotionEvent.ACTION_MOVE && mImageButton != null) {
            if (event.getX() > (mImageButton.getX() + 70) && !leftRight) {
                v3d.setX(0.0f);
                v3d.setY(0.01f);
                mImageButton.setImageResource(R.drawable.ic_reload_2);
//            TRUE option allow to concat value of V3d with the previous one
                mTempModel.setRotation(new Rotation(v3d), true);
                upDown = true;
            } else if (event.getX() < (mImageButton.getX() - 70) && !leftRight) {
                mImageButton.setImageResource(R.drawable.ic_reload_2);
                v3d.setX(0.0f);
                v3d.setY(-0.01f);
                mTempModel.setRotation(new Rotation(v3d), true);
                upDown = true;
            }
            if (event.getY() > (mImageButton.getY() + 70) && !upDown) {
                leftRight = true;
                mImageButton.setImageResource(R.drawable.ic_reload_2);
                v3d.setX(0.01f);
                v3d.setY(0.0f);
                mTempModel.setRotation(new Rotation(v3d), true);
            } else if (event.getY() < (mImageButton.getY() - 70) && !upDown) {
                leftRight = true;
                mImageButton.setImageResource(R.drawable.ic_reload_2);
                v3d.setX(-0.01f);
                v3d.setY(0.0f);
                mTempModel.setRotation(new Rotation(v3d), true);
            }
            if (event.getX() <= fingerX + 50 && event.getX() >= fingerX) {
                if (event.getY() >= fingerY && event.getY() <= fingerY + 50) {
                    leftRight = false;
                    upDown = false;
                    mImageButton.setImageResource(R.drawable.ic_reload_green_2);
                }
            }
        }
        if (action == MotionEvent.ACTION_UP && event.getX() <= fingerX + 50 && event.getX() >= fingerX && event.getY() >= fingerY && event.getY() <= fingerY + 50) {
            mTempModel.setRotation(new Rotation(new Vector3d(0.0f, 0.f, 0.f)), false);
        }
        return true;
    }

    //    method call to enable or disabled content
    void contentGestion(ContentType c, VideoView v, MediaPlayer m) {
        if (mTrack == TrackState.GPS) {
            mTrack = TrackState.NOGPS;
            metaioSDK.setTrackingConfiguration(PATH_TO_IMAGE_TRACKING_CONF);

        }
        if (m.isPlaying()) m.stop();
        if (v != null && v.isPlaying()) {
            v.stopPlayback();
            v.setVisibility(View.GONE);
        }
    }

    // Stream audio content
    void audioStream(String ip, String fileName) {
        try {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource("http://" + ip + "/www/" + fileName);
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void onAudio() {
        ct = ContentType.Audio;
        contentGestion(ct, vv, mMediaPlayer);
        mMediaPlayer.reset();
        try {
            if (mState == EState.TRACKING)
                audioStream(AsyncWifiConnect.getIpAdress(), "near.mp3");

        } catch (NullPointerException e) {
            Toast.makeText(this, "Contenu introuvable", Toast.LENGTH_SHORT).show();
        }

    }

    //stream videocontent
    void onVideo(String ip, String fileName) {
//    enum  used in onTracking event
        ct = ContentType.Video;
        contentGestion(ct, vv, mMediaPlayer);
        vv = (VideoView) findViewById(R.id.videoview);
        vv.setVisibility(View.VISIBLE);
        new BackgroundVideoTask(this, vv).execute(AssetsManager.getAbsolutePath() + "/sandettie.mp4");
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
                    onVideo(AsyncWifiConnect.getIpAdress(), "test.mp4");
                } catch (NullPointerException e) {

                }
                break;
            case R.id.audioContent:
                contentL.clearAnimation();
                contentL.setVisibility(View.GONE);
                onAudio();
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
                cadReset = (ImageButton) findViewById(com.urbik.dunkerque.R.id.resetCad);
                cadReset.setVisibility(View.VISIBLE);
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
                ib.setVisibility(View.GONE);
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
        MetaioDebug.log("UnifeyeCallbackHandler.onGeometryTouched: " + geometry);
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
        public void onTrackingEvent(TrackingValuesVector trackingValues) {
            String targetName = trackingValues.get(0).getCosName();
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
                        if (!entry.getKey().equals(EnumModel.SANDETTIE_IMG.toString())) // pour l'image du sandettie on affiche un menu et non un model
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
                        if (currentTargetName.equals(EnumModel.SANDETTIE_IMG.toString()) && mState == EState.TRACKING) {
                            contentL.setVisibility(View.VISIBLE);
                            contentL.startAnimation(animBounce);
                        } else {
                            contentL.clearAnimation();
                            contentL.setVisibility(View.GONE);
                        }
                    } else {
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
