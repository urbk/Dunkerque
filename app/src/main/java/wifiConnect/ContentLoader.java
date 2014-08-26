package wifiConnect;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;


import com.urbik.dunkerque.R;
import com.urbik.dunkerque.Tracking;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import utils.AssetsExtracter;
import utils.Utils;

/**
 * Created by Antoine on 27/06/2014.
 */
public class ContentLoader extends AsyncTask<String, Integer, Integer> {
    private final Activity mContext;
    private ProgressBar pbAsyncBackgroundTraitement;
    private final String url;
    private final static String FILE = "files.zip";
    private final static String SERVER_PATH = "www/";
    private final static String PASSWORD = ".hd161~n=:=_e~X";
    public static String ROOT_INTERNAL;
    private ImageView mImageView;
   private final Animation animRotate;

    public ContentLoader(Activity mContext, String url) {
        super();
        this.mContext = mContext;
        this.url = url;
        animRotate = AnimationUtils.loadAnimation(mContext,
                R.anim.rotate);
    }

    @Override
    protected void onPreExecute() {
        this.pbAsyncBackgroundTraitement = (ProgressBar) mContext.findViewById(R.id.progressBar);
        this.pbAsyncBackgroundTraitement.setVisibility(View.VISIBLE);
        mImageView = (ImageView) mContext.findViewById(R.id.ic_loading_work);
        mImageView.startAnimation(animRotate);
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(String... params) {
        downloadFromUrl(url, FILE, SERVER_PATH);
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        this.pbAsyncBackgroundTraitement.setProgress(values[0]);
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Integer result) {

        mImageView.clearAnimation();
        mImageView.setImageResource(R.drawable.ic_check);
        //METAIO CLASS
        new AssetsExtracter(mContext).execute(0);
        //START YOUR ACTIVITY
        Intent intent = new Intent(mContext, Tracking.class);

        mContext.startActivity(intent);
//        mContext.overridePendingTransition(R.anim.fade_out, R.anim.fade_in);

    }

    void downloadFromUrl(String downloadUrl, String fileName, String folder) {
        try {
            URL url = new URL("http://" + downloadUrl + "/" + folder + "/" + fileName);
            File file = new File(mContext.getCacheDir(), fileName);
            ROOT_INTERNAL = mContext.getCacheDir().getAbsolutePath();
            //connecting to url
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("POST");
            c.setDoOutput(true);
            c.connect();
            //lenghtOfFile is used for calculating download progress
            int lenghtOfFile = c.getContentLength();
            //this is where the file will be seen after the download
            FileOutputStream f = new FileOutputStream(file);
            //file input is from the url
            InputStream in = c.getInputStream();

            //here’s the download code
            byte[] buffer = new byte[1024];
            int len1;
            long total = 0;

            while ((len1 = in.read(buffer)) > 0) {
                total += len1; //total = total + len1
                publishProgress((int) ((total * 100) / lenghtOfFile));
                f.write(buffer, 0, len1);
            }

            //unzip downloaded file
            mContext.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    pbAsyncBackgroundTraitement.setVisibility(View.GONE);
                    mImageView.clearAnimation();
                    mImageView.setImageResource(R.drawable.ic_check);
                    mImageView = (ImageView) mContext.findViewById(R.id.ic_finalization_work);
                    mImageView.startAnimation(animRotate);
                }
            });
            Utils.unzipFile(mContext.getCacheDir() + "/" + FILE, mContext.getCacheDir().getAbsolutePath(), PASSWORD);
            file.delete();
            f.close();

        } catch (IOException e) {
            Log.e("DownloadManager", "Error: " + e);
        }
    }
}