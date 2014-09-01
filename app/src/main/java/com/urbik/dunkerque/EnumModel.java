package com.urbik.dunkerque;

import com.metaio.tools.io.AssetsManager;

/**
 * Created by Antoine on 26/08/2014.
 */
public enum EnumModel {

    DUCHESSE_IMG(new Model("BateauCoupe/test/bateau_01-06.obj", ContentType.Movable, 1f, 1),"Duchesse-Img"),

    //    *************
    SANDETTIE_IMG(new Model("TeaPot/test_anim_teapot.mfbx", ContentType.Animations, 2.0f, 2),"Sandettie-Img"),
    //    **************
    SANDETTIE_CAD_DISPLAY_MODEL_1(new Model("Bateau/sandettie_d1.mfbx", ContentType.CAD, 1f, Model.INITIAL_POSE, Model.DISPLAY_MODEL),"Sandettie-CAD-D-1"),
    SANDETTIE_CAD_DISPLAY_MODEL_2(new Model("Bateau/sandettie_d2.mfbx", ContentType.CAD, 1f, Model.INITIAL_POSE, Model.DISPLAY_MODEL),"Sandettie-CAD-D-2"),
    SANDETTIE_CAD_DISPLAY_MODEL_3(new Model("Bateau/sandettie_d3.mfbx", ContentType.CAD, 1f, Model.INITIAL_POSE, Model.DISPLAY_MODEL),"Sandettie-CAD-D-3"),
    SANDETTIE_CAD_VISUAL_HELP(new Model(AssetsManager.getAbsolutePath() + "/CAD/Sandettie/SurfaceModel.obj", ContentType.CAD, 1.0f, Model.TRACKING_POSE, Model.VISUAL_HELP),"Sandettie-CAD-V"),
    //    *************
    PANNEAU_CAD_DISPLAY_MODEL(new Model("Panneau/pirate.mfbx", ContentType.CAD, 5f, Model.INITIAL_POSE, Model.DISPLAY_MODEL),"Panneau-CAD-D"),
    PANNEAU_CAD_VISUAL_HELP(new Model(AssetsManager.getAbsolutePath() + "/CAD/Panneau/SurfaceModel.obj", ContentType.CAD, 1.0f, Model.TRACKING_POSE, Model.VISUAL_HELP),"Panneau-CAD-V");


    private Model model;
private String id;
    EnumModel(Model m,String id) {
        this.model = m;
        this.id=id;
    }

    public Model getModel() {
        return model;
    }

    @Override
    public String toString() {
        return id;
    }
}
