package utils;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

public class BackgroundVideoTask extends AsyncTask<String, Uri, Void> {

    Activity ac;
    VideoView vid;

    public BackgroundVideoTask(Activity a, VideoView vid) {
        ac = a;
        this.vid = vid;
    }

    protected void onProgressUpdate(final Uri... uri) {

        try {

            MediaController media = new MediaController(ac);
            vid.setVisibility(View.VISIBLE);
            vid.setMediaController(media);
            vid.setVideoURI(uri[0]);
            vid.requestFocus();
            vid.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                public void onPrepared(MediaPlayer arg0) {

                    vid.start();

                    Log.e("ok",""+vid.getBufferPercentage());
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