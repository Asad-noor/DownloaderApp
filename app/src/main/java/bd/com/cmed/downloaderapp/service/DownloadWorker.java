package bd.com.cmed.downloaderapp.service;

import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import androidx.work.WorkManager;
import androidx.work.Worker;
import bd.com.cmed.downloaderapp.DownloadApp;
import bd.com.cmed.downloaderapp.util.Constants;
import bd.com.cmed.downloaderapp.util.SharedPref;

public class DownloadWorker extends Worker{

    private NotificationHelper notificationHelper;
    private int current = 0;


    @NonNull
    @Override
    public Worker.Result doWork() {

        Intent intent = new Intent(Constants.INTENT_LOCAL_BROADCAST);
        notificationHelper = new NotificationHelper(DownloadApp.getApp());

        //Log.d("tttt", " doWork called");

        String folder;

        int count;
        try {
            URL url = new URL(Constants.DOWNLOAD_FILE_URL);
            URLConnection connection = url.openConnection();
            connection.connect();
            // getting file length
            int lengthOfFile = connection.getContentLength();


            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            //External directory path to save file
            folder = Environment.getExternalStorageDirectory() + File.separator + Constants.DOWNLOAD_FOLDER_NAME + "/";

            //Create folder if it does not exist
            File directory = new File(folder);

            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Output stream to write file
            OutputStream output = new FileOutputStream(folder + Constants.DOWNLOAD_FILE_NAME);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                //Log.d("tttt", "Progress: " + (int) ((total * 100) / lengthOfFile));
                int value = (int) ((total * 100) / lengthOfFile);

                intent.putExtra(Constants.INTENT_KEY, value);
                LocalBroadcastManager.getInstance(DownloadApp.getApp()).sendBroadcast(intent);

                //notify user
                showNotification(value);

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            //Log.e("Error: ", e.getMessage());
            WorkManager.getInstance().cancelAllWorkByTag(Constants.TAG_WORKER_THREAD);
            return Worker.Result.FAILURE;
        }

        return Worker.Result.SUCCESS;
    }

    private void showNotification(int percent) {
        boolean isInBack = DownloadApp.getApp().getSharedPref().getCommonBoolean(SharedPref.IS_APP_IN_BACKGROUND);

        if(isInBack && percent > 0 && percent % 5 == 0){
            if(current != percent) {
                notificationHelper.createNotification(Constants.TITLE_NOTIFICATION, percent + "%");
                current = percent;
            }
        }
    }
}
