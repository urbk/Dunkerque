package com.urbik.dunkerque;

import wifiConnect.ContentLoader;

/**
 * Created by Antoine on 15/07/2014.
 */
public enum ContentType {
    Audio("Audio"),
    Video("Video"),
    Animations("Animations"),
    Movable("Movable"),
    Location("Location"),
    CAD("CAD"),
    None("None");


    private String ct = "";
    private String location = ContentLoader.ROOT_INTERNAL + "/";

    //Constructeur
    ContentType(String ct) {
        this.ct = ct;
        if (ct.equals("Animations")) {
            location += "Image/Animations/";
        } else if (ct.equals("Movable")) {
            location += "Image/Movable/";
        }else if(ct.equals("CAD")) {
            location += "CAD/";
        }else if(ct.equals("Location")) {
            location += "Location/";
        }
    }

    public String toString() {
        return ct;
    }
    public String getLocation() {return location;}
}
