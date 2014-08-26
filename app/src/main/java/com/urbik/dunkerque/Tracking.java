package com.urbik.dunkerque;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
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
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;
import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.ETRACKING_STATE;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.LLACoordinate;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import utils.Utils;
import wifiConnect.AsyncWifiConnect;
import wifiConnect.ContentLoader;


public class Tracking extends ARViewActivity implements MediaPlayer.OnPreparedListener, View.OnLongClickListener, View.OnTouchListener {
    private MetaioSDKCallbackHandler mCallbackHandler;
    private ContentType ct = ContentType.None;
    private final MediaPlayer mMediaPlayer = new MediaPlayer();
    private VideoView vv;
    private final ArrayList<ContentModel> mContentModel = new ArrayList<ContentModel>();
    private ContentModel currentContentModel;
    private int indexCurrentModel;
    private static String PATH_TO_IMAGE_TRACKING_CONF;
    private static String PATH_TO_CAD_TRACKING_CONF;
    private final Vector3d v3d = new Vector3d(0.0f, 0.0f, 0.0f);
    private Animation animBounce, animFadeIn, animFadeOut;
    private LinearLayout contentL;
    private ImageButton contentL2, cadReset, mImageButton = null, arrowLeft, arrowRight, arrowUp, arrowDown;
    private float fingerX, fingerY;


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
        PATH_TO_CAD_TRACKING_CONF ="";
        animBounce = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.bounce);
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_in);
        animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_out);


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
        File contentFolder = new File(ContentLoader.ROOT_INTERNAL);
        Utils.deleteDir(contentFolder);
    }

    @Override
    public void onBackPressed() {
        if (currentContentModel != null) {
            contentGestion(ct, vv, mMediaPlayer);
            if (mTrack == TrackState.CAD) {
                mTrack = TrackState.NOGPS;
                mState = EState.NOTTRACKING;
                cadReset.setVisibility(View.GONE);
                new LoadTrackingConfig(this, metaioSDK).execute(PATH_TO_IMAGE_TRACKING_CONF);
            } else if (currentContentModel.getTargetName().equals("Sandettie") && mState == EState.TRACKING) {
                contentL.setVisibility(View.VISIBLE);
                contentL.startAnimation(animBounce);
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (currentContentModel.getTargetName().equals("Duchesse") && mState == EState.TRACKING) {
            mImageButton = (ImageButton) findViewById(R.id.rotateModel);
            arrowUp = (ImageButton) findViewById(R.id.arrow_up);
            arrowDown = (ImageButton) findViewById(R.id.arrow_down);
            arrowLeft = (ImageButton) findViewById(R.id.arrow_left);
            arrowRight = (ImageButton) findViewById(R.id.arrow_right);
            float xOffset = 42;
            float yOffset = 39;
            Log.e("Offset", xOffset + " y :" + yOffset);
            fingerX = fingerX - xOffset;
            fingerY = fingerY - yOffset;
            mImageButton.setX(fingerX);
            mImageButton.setY(fingerY);

            arrowUp.setX(fingerX);
            arrowUp.setY(fingerY - 70);
            arrowDown.setX(fingerX);
            arrowDown.setY(fingerY + 70);
            arrowLeft.setX(fingerX - 70);
            arrowLeft.setY(fingerY);
            arrowRight.setX(fingerX + 70);
            arrowRight.setY(fingerY);
            mImageButton.setAnimation(animFadeIn);
            arrowRight.setAnimation(animFadeIn);
            arrowLeft.setAnimation(animFadeIn);
            arrowDown.setAnimation(animFadeIn);
            arrowUp.setAnimation(animFadeIn);
            mImageButton.startAnimation(animFadeIn);
            arrowRight.startAnimation(animFadeIn);
            arrowLeft.startAnimation(animFadeIn);
            arrowDown.startAnimation(animFadeIn);
            arrowUp.startAnimation(animFadeIn);
            arrowRight.setImageResource(R.drawable.ic_arrow_right);
            arrowLeft.setImageResource(R.drawable.ic_arrow_left);
            arrowUp.setImageResource(R.drawable.ic_arrow_up);
            arrowDown.setImageResource(R.drawable.ic_arrow_down);
            if (mImageButton.getVisibility() != View.VISIBLE) {
                mImageButton.setVisibility(View.VISIBLE);
                arrowRight.setVisibility(View.VISIBLE);
                arrowLeft.setVisibility(View.VISIBLE);
                arrowDown.setVisibility(View.VISIBLE);
                arrowUp.setVisibility(View.VISIBLE);
            }
        }
        return true;
    }

    //
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        ContentModel mTempContentModel = mContentModel.get(indexCurrentModel);
        IGeometry mTempModel = mTempContentModel.getModel();
        if (action == MotionEvent.ACTION_DOWN) {
            fingerX = event.getX();
            fingerY = event.getY();
        }
        v.onTouchEvent(event);
        boolean upDown = false;
        boolean leftRight = false;
        if (action == MotionEvent.ACTION_UP && mImageButton != null && mImageButton.getVisibility() == View.VISIBLE) {
            mImageButton.setAnimation(animFadeOut);
            arrowRight.setAnimation(animFadeOut);
            arrowLeft.setAnimation(animFadeOut);
            arrowDown.setAnimation(animFadeOut);
            arrowUp.setAnimation(animFadeOut);
            mImageButton.startAnimation(animFadeOut);
            arrowRight.startAnimation(animFadeOut);
            arrowLeft.startAnimation(animFadeOut);
            arrowDown.startAnimation(animFadeOut);
            arrowUp.startAnimation(animFadeOut);
            mImageButton.setVisibility(View.GONE);
            arrowLeft.setVisibility(View.GONE);
            arrowRight.setVisibility(View.GONE);
            arrowUp.setVisibility(View.GONE);
            arrowDown.setVisibility(View.GONE);

        }
        if (action == MotionEvent.ACTION_MOVE && mImageButton != null) {
            if (event.getX() > (mImageButton.getX() + 70) && !leftRight) {
                v3d.setX(0.0f);
                v3d.setY(0.01f);
                arrowRight.setImageResource(R.drawable.ic_arrow_right_green);
                arrowLeft.setImageResource(R.drawable.ic_arrow_left);
                arrowUp.setImageResource(R.drawable.ic_arrow_up);
                arrowDown.setImageResource(R.drawable.ic_arrow_down);
                mImageButton.setImageResource(R.drawable.ic_reload);
//            TRUE option allow to concat value of V3d with the previous one
                mTempModel.setRotation(new Rotation(v3d), true);
                upDown = true;
            } else if (event.getX() < (mImageButton.getX() - 70) && !leftRight) {
                arrowRight.setImageResource(R.drawable.ic_arrow_right);
                arrowDown.setImageResource(R.drawable.ic_arrow_down);
                arrowUp.setImageResource(R.drawable.ic_arrow_up);
                mImageButton.setImageResource(R.drawable.ic_reload);
                arrowLeft.setImageResource(R.drawable.ic_arrow_left_green);
                v3d.setX(0.0f);
                v3d.setY(-0.01f);
                mTempModel.setRotation(new Rotation(v3d), true);
                upDown = true;
            }
            if (event.getY() > (mImageButton.getY() + 70) && !upDown) {
                leftRight = true;
                arrowRight.setImageResource(R.drawable.ic_arrow_right);
                arrowLeft.setImageResource(R.drawable.ic_arrow_left);
                arrowUp.setImageResource(R.drawable.ic_arrow_up);
                mImageButton.setImageResource(R.drawable.ic_reload);
                arrowDown.setImageResource(R.drawable.ic_arrow_down_green);
                v3d.setX(0.01f);
                v3d.setY(0.0f);
                mTempModel.setRotation(new Rotation(v3d), true);
            } else if (event.getY() < (mImageButton.getY() - 70) && !upDown) {
                leftRight = true;
                arrowRight.setImageResource(R.drawable.ic_arrow_right);
                arrowLeft.setImageResource(R.drawable.ic_arrow_left);
                arrowDown.setImageResource(R.drawable.ic_arrow_down);
                arrowUp.setImageResource(R.drawable.ic_arrow_up_green);
                mImageButton.setImageResource(R.drawable.ic_reload);
                v3d.setX(-0.01f);
                v3d.setY(0.0f);
                mTempModel.setRotation(new Rotation(v3d), true);
            }
            if (event.getX() <= fingerX + 50 && event.getX() >= fingerX) {
                if (event.getY() >= fingerY && event.getY() <= fingerY + 50) {
                    leftRight = false;
                    upDown = false;
                    arrowRight.setImageResource(R.drawable.ic_arrow_right);
                    arrowLeft.setImageResource(R.drawable.ic_arrow_left);
                    arrowDown.setImageResource(R.drawable.ic_arrow_down);
                    arrowUp.setImageResource(R.drawable.ic_arrow_up);
                    mImageButton.setImageResource(R.drawable.ic_reload_green);
                }
            }
        }
        if (action == MotionEvent.ACTION_UP && event.getX() <= fingerX + 50 && event.getX() >= fingerX) {
            if (event.getY() >= fingerY && event.getY() <= fingerY + 50) {
                mTempModel.setRotation(new Rotation(new Vector3d(0.0f, 0.f, 0.f)), false);
            }
        }

//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//
//////                TEST CAD MODEL
////                metaioSDK.setTrackingConfiguration(PATH_TO_CAD_TRACKING_CONF);
////                cadReset = (ImageButton) findViewById(R.id.resetCad);
////                cadReset.setVisibility(View.VISIBLE);
////                for (ContentModel cm : mContentModel) {
////                    cm.getModel().setVisible(false);
////                    if (cm.getContentType() == ContentType.CAD) {
////                        mTrack = TrackState.CAD;
////                        cm.getModel().setVisible(true);
////                        cm.getModel().setCoordinateSystemID(cm.getCsi());
////                    }
////                }
//                break;
//        }
        return true;
    }


    //Screen shot saved in a folder visible inside the gallery
    void onScreenshot(String pathStorage, String folderName) {
        File f = new File(pathStorage);
        Calendar c = Calendar.getInstance();
        String date = c.get(Calendar.DATE) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.YEAR);
        //Allow android gallery to see folder which contain pictures of the app
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        if (!f.exists()) f.mkdir();
        //serialization of pictures
        int numberOfFile = f.listFiles().length;
        // take a picture using the SDK and save it to external storage
        String imagePath = pathStorage + "/" + folderName + numberOfFile + "-" + date + ".jpg";
        try {
            metaioSDK.requestScreenshot(imagePath);
            Toast.makeText(getApplicationContext(), R.string.ScreenshotSuccess, Toast.LENGTH_SHORT).show();
//            Utils.shareScreenshot(this, imagePath);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), R.string.ScreenshotFail, Toast.LENGTH_SHORT).show();
        }

    }

    //    method call to enable or disabled content
    void contentGestion(ContentType c, VideoView v, MediaPlayer m) {
        if (mTrack == TrackState.GPS) {
            mTrack = TrackState.NOGPS;
            metaioSDK.setTrackingConfiguration(PATH_TO_IMAGE_TRACKING_CONF);
//            mLinearLayoutLLA.setVisibility(View.GONE);
//            mLinearLayoutLLA2.setVisibility(View.GONE);
//            mLinearLayoutLLA3.setVisibility(View.GONE);
        }
        if (m.isPlaying()) m.stop();
        if (v != null && v.isPlaying() && c != ContentType.Video) {
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
            else
                audioStream(AsyncWifiConnect.getIpAdress(), "toofar.mp3");
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
        if (mState == EState.TRACKING) {
            if (!vv.isPlaying()) {
                vv.setVisibility(View.VISIBLE);
//                playing video
                vv.setVideoURI(Uri.parse("http://" + ip + "/www/" + fileName)); // AssetsManager.getAbsolutePath()+"/sandettie.3gp"
                Log.e("eeeee", "http://" + ip + "/www/" + fileName);
                vv.requestFocus();
//                start video when it's buffered
                vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    public void onPrepared(MediaPlayer arg0) {
                        vv.start();
                    }
                });
            } else {
                vv.stopPlayback();
                vv.setVisibility(View.GONE);
            }
        } else if (!vv.isPlaying())
            Toast.makeText(getApplicationContext(), "Essayez près du panneau", Toast.LENGTH_SHORT).show();
        else {
            vv.stopPlayback();
            vv.setVisibility(View.GONE);
        }
    }


    //when sound is ready to be streamed we can start it
    public void onPrepared(MediaPlayer mediaplayer) {
        mediaplayer.start();
    }


    public void onGeoloc(View v) {
//        mLinearLayoutLLA = (LinearLayout) findViewById(R.id.linearLayoutLLA);
//        mLinearLayoutLLA2 = (LinearLayout) findViewById(R.id.linearLayoutLLA2);
//        mLinearLayoutLLA3 = (LinearLayout) findViewById(R.id.linearLayoutLLA3);
//        mLinearLayoutLLA.setVisibility(View.VISIBLE);
//        mLinearLayoutLLA2.setVisibility(View.VISIBLE);
//        mLinearLayoutLLA3.setVisibility(View.VISIBLE);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        else {

            mTrack = TrackState.GPS;
            for (ContentModel cm : mContentModel) {
                if (cm.getContentType() != ContentType.Location)
                    cm.getModel().setVisible(false);
            }
            metaioSDK.setTrackingConfiguration("GPS");
            metaioSDK.setLLAObjectRenderingLimits(5, 200);
            // Set render frustum accordingly
            metaioSDK.setRendererClippingPlaneLimits(10, 220000);
        }
    }

    public void onContentSelect(View v) {
        switch (v.getId()) {
            case R.id.videoContent:
                try {
                    contentL.clearAnimation();
                    contentL.setVisibility(View.GONE);
                    onVideo(AsyncWifiConnect.getIpAdress(), "sandettie.3gp");
                } catch (NullPointerException e) {
                    Toast.makeText(this, "Video introuvable", Toast.LENGTH_SHORT).show();
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
                currentContentModel.getModel().setVisible(true);
                if (currentContentModel.getContentType() == ContentType.Animations)
                    currentContentModel.getModel().startAnimation(currentContentModel.getAnimName(), true);
                break;
            case R.id.trackCadSandettie:
                contentL.clearAnimation();
                contentL.setVisibility(View.GONE);
                PATH_TO_CAD_TRACKING_CONF= AssetsManager.getAbsolutePath() + "/CAD/Sandettie/Tracking.xml";
                metaioSDK.setTrackingConfiguration( PATH_TO_CAD_TRACKING_CONF );
                cadReset = (ImageButton) findViewById(R.id.resetCad);
                cadReset.setVisibility(View.VISIBLE);
                currentContentModel.getModel().setVisible(false);
                for (ContentModel cm : mContentModel) {

                    if (cm.getContentType() == ContentType.CAD  && cm.targetName.equals("CADSandettie")) {
                        mTrack = TrackState.CAD;
                        cm.getModel().setVisible(true);
                        cm.getModel().setCoordinateSystemID(cm.getCsi());
                    }
                }
                break;
            case R.id.guide:
                PATH_TO_CAD_TRACKING_CONF= AssetsManager.getAbsolutePath() + "/CAD/Panneau/Tracking.xml";
                metaioSDK.setTrackingConfiguration(PATH_TO_CAD_TRACKING_CONF );
                cadReset = (ImageButton) findViewById(R.id.resetCad);
                cadReset.setVisibility(View.VISIBLE);
                currentContentModel.getModel().setVisible(false);
                for (ContentModel cm : mContentModel) {
                    if (cm.getContentType() == ContentType.CAD && cm.targetName.equals("CADPanneau")) {
                        Log.e("ok","ok");
                        mTrack = TrackState.CAD;
                        cm.getModel().setVisible(true);
                        cm.getModel().setCoordinateSystemID(cm.getCsi());
                    }
                }
                break;
            case R.id.resetCad:
                metaioSDK.setTrackingConfiguration(PATH_TO_CAD_TRACKING_CONF);
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

        try {
            metaioSDK.setTrackingConfiguration(PATH_TO_IMAGE_TRACKING_CONF);
//            IMAGE TRACKING [Animations & Movable content are in]
            mContentModel.add(new ContentModel("bateau en coupe", "BateauCoupe/test/bateau_01-06.obj", ContentType.Movable, "Duchesse", 1, 1f));
            mContentModel.add(new ContentModel("teapot", "TeaPot/test_anim_teapot.mfbx", ContentType.Animations, "Sandettie", 2, 2.0f));
//            GPS TRACKING
            mContentModel.add(new ContentModel("petit bateau", "PetitBateau/petitBateau.mfbx", ContentType.Location, 100, 51.037684, 2.373176, 0, 0));
//            CAD Tracking
            mContentModel.add(new ContentModel("displayModel", "Bateau/texte.mfbx", ContentType.CAD, "CADSandettie", 1, 0.6f));
            mContentModel.add(new ContentModel("Aide visuel", AssetsManager.getAbsolutePath() + "/CAD/Sandettie/SurfaceModel.obj", ContentType.CAD, "CADSandettie", 2, 1f));

            mContentModel.add(new ContentModel("displayModel", "Panneau/pirate.mfbx", ContentType.CAD, "CADPanneau", 1, 5f));
            mContentModel.add(new ContentModel("Aide visuel", AssetsManager.getAbsolutePath() + "/CAD/Panneau/SurfaceModel.obj", ContentType.CAD, "CADPanneau", 2, 1));

        } catch (Exception e) {
            Log.e("ERREUR", "impossible de charger les contenus");
        }
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
                for (ContentModel cm : mContentModel) {
                    cm.getModel().setVisible(false);
                    if (cm.getContentType() != ContentType.Location) {
                        if (cm.getTargetName().equals(targetName)) {
                            if (!targetName.equals("Sandettie"))
                                cm.getModel().setVisible(true);
                            cm.getModel().setCoordinateSystemID(cm.getCsi());
                            currentContentModel = cm;
                            indexCurrentModel = mContentModel.indexOf(cm);
                        }
                    }
                }
            }
            if (ct == ContentType.Audio) onAudio();
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    contentL = (LinearLayout) findViewById(R.id.contentLayout);
                    contentL2 = (ImageButton) findViewById(R.id.rotateModel);
                    if (mTrack != TrackState.CAD) {
                        if (currentContentModel.getTargetName().equals("Sandettie") && mState == EState.TRACKING) {
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

    //    *****************************************************************************************
//    ***********************************PRIVATE MODEL CLASS***************************************
//    *********************************************************************************************
    private class ContentModel {
        private final String modelName;
        private final IGeometry mModel;
        private String animName = null;
        private String targetName;
        private int modelCsi;
        private final ContentType mContentType;
        private LLACoordinate mLla;

        //CSI : Coordinate system ID
        public ContentModel(String name, String contentToDisplay, ContentType cType, String target, int csi, float scale) {
            if (name.equals("Aide visuel")) {
                mModel = metaioSDK.createGeometry(contentToDisplay);
                mModel.setTransparency(0.8f);
            } else
                mModel = metaioSDK.createGeometry(cType.getLocation() + contentToDisplay);

//****************TEST*********************
            if (name.equals("occlusion")) {
                mModel.setOcclusionMode(true);
            }
//*****************************************
            modelName = name;
            targetName = target;
            modelCsi = csi;
            mContentType = cType;

            if (mModel != null) {
                mModel.setScale(scale);

                if (cType == ContentType.Animations)
                    animName = mModel.getAnimationNames().get(0);
            } else
                MetaioDebug.log(Log.ERROR, "Error loading geometry: " + cType.getLocation() + contentToDisplay);
        }

        public ContentModel(String name, String contentToDisplay, ContentType cType, float scale, double latitude, double longitude, double altitude, double accuracy) {
            mModel = metaioSDK.createGeometry(cType.getLocation() + contentToDisplay);
            modelName = name;
            mContentType = cType;

            mLla = new LLACoordinate(latitude, longitude, altitude, accuracy);

            if (mModel != null) {
                mModel.setTranslationLLA(mLla);
//                mModel.setLLALimitsEnabled(true);
                mModel.setScale(scale);
            } else
                MetaioDebug.log(Log.ERROR, "Error loading geometry: " + cType.getLocation() + contentToDisplay);
        }

        public IGeometry getModel() {
            return mModel;
        }

        public String getAnimName() {
            return animName;
        }

        public String getTargetName() {
            return targetName;
        }

        public int getCsi() {
            return modelCsi;
        }

        public ContentType getContentType() {
            return mContentType;
        }

        public LLACoordinate getLla() {
            return mLla;
        }
    }


    //    ****************************************END**********************************************
//    ***********************************PRIVATE MODEL CLASS***************************************
//    *********************************************************************************************


}
