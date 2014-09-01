package com.urbik.dunkerque;

import android.util.Log;

import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.LLACoordinate;

/**
 * Created by Antoine on 26/08/2014.
 */
public class Model {


    private int displayType=-1;
    private int modelCsi;
    private final ContentType mContentType;
    private LLACoordinate mLla;
    private IGeometry model;
    private String contentToDisplay;
    private  float scale;
    public static final int INITIAL_POSE = 1;
    public static final int TRACKING_POSE = 2;
    public static final int DISPLAY_MODEL=1;
    public static final int VISUAL_HELP=2;
    public static final int OCCLUSION=3;
    //CSI : Coordinate system ID
    public Model(String contentToDisplay, ContentType cType, float scale, int csi, int display_type) {
        this.contentToDisplay=contentToDisplay;
        this.scale=scale;
        displayType=display_type;
        modelCsi = csi;
        mContentType = cType;
    }

    public Model(String contentToDisplay, ContentType cType, float scale, int csi) {
        this.contentToDisplay=contentToDisplay;
        this.scale=scale;
        modelCsi = csi;
        mContentType = cType;
    }

    public Model(String contentToDisplay, ContentType cType, float scale, double latitude, double longitude, double altitude, double accuracy) {
        this.contentToDisplay=contentToDisplay;
        this.scale=scale;
        mContentType = cType;
        mLla = new LLACoordinate(latitude, longitude, altitude, accuracy);
    }


    public int getModelCsi() {
        return modelCsi;
    }

    public ContentType getContentType() {
        return mContentType;
    }

    public LLACoordinate getmLla() {
        return mLla;
    }

    public void setIGeometry(IGeometry model) {
        this.model = model;
    }

    public String getContentToDisplay() {
        return contentToDisplay;
    }

    public int getDisplayType() {
        return displayType;
    }

    public IGeometry getIGeometry() {
        return model;
    }

    public float getScale() {
        return scale;
    }


}
