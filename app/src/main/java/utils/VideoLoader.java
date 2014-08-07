package utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.VideoView;

/**
 * Created by Antoine on 16/07/2014.
 */
class VideoLoader extends AsyncTask<String, Uri, Void> {
    private ProgressDialog dialog;
    private final Context mContext;
    private final VideoView mVideo;

    public VideoLoader(Context c, VideoView video) {
        mContext = c;
        mVideo = video;
    }

    protected void onPreExecute() {
        dialog = new ProgressDialog(mContext);
        dialog.setMessage("Chargement, Veuillez patienter...");
        dialog.setCancelable(true);
        dialog.show();
    }

    protected void onProgressUpdate(final Uri... uri) {


        try {

            mVideo.setVideoURI(uri[0]);
            mVideo.requestFocus();
            mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer arg0) {
                    mVideo.start();
                    dialog.dismiss();
                }
            });

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected Void doInBackground(String... params) {
        try {
            Uri uri = Uri.parse(params[0]);
            publishProgress(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
