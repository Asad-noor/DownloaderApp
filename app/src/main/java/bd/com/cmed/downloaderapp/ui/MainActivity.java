package bd.com.cmed.downloaderapp.ui;

import android.Manifest;
import android.app.NotificationManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import bd.com.cmed.downloaderapp.DownloadApp;
import bd.com.cmed.downloaderapp.R;
import bd.com.cmed.downloaderapp.util.SharedPref;
import bd.com.cmed.downloaderapp.viewmodel.MainActivityVM;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonDownload;
    private TextView textViewProgressValue;
    private ProgressBar progressBarDownload;
    private MainActivityVM mainActivityVM;
    //private boolean isDownloading = false;

    private static final int PERMISSION_REQUEST = 100;

    @Override
    protected void onPause() {
        super.onPause();

        DownloadApp.getApp().getSharedPref().setCommonBooleanValue(SharedPref.IS_APP_IN_BACKGROUND, true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        DownloadApp.getApp().getSharedPref().setCommonBooleanValue(SharedPref.IS_APP_IN_BACKGROUND, false);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        buttonDownload = findViewById(R.id.button_download);
        textViewProgressValue = findViewById(R.id.textView_progressValue);
        progressBarDownload = findViewById(R.id.progressBar_download);

        buttonDownload.setOnClickListener(this);
        textViewProgressValue.setText(getResources().getString(R.string.status_idle_text));

        //viewModel initialize
        mainActivityVM = ViewModelProviders.of(this).get(MainActivityVM.class);
        subscribe();
    }

    private void subscribe() {
        final Observer<Long> downloadProgressObserver = new Observer<Long>() {
            @Override
            public void onChanged(@Nullable final Long aLong) {

                int progressValue = 0;
                if (aLong != null) {
                    progressValue = aLong.intValue();
                }
                if (progressValue > 0) {
                    //isDownloading = true;
                    buttonDownload.setClickable(false);
                    String text = getResources().getString(R.string.status_downloaded_text) + " " + progressValue + "%";
                    textViewProgressValue.setText(text);
                }

                if(progressValue == 100){
                    buttonDownload.setClickable(true);
                }

                progressBarDownload.setProgress(progressValue);
            }
        };

        mainActivityVM.getDownloadStatus().observe(this, downloadProgressObserver);
    }

    @Override
    public void onClick(View v) {
        checkForPermission(v);
    }

    private void checkForPermission(View view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Snackbar.make(view, "You need to grant WRITE EXTERNAL STORAGE permission to save downloaded file",
                            Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
                        }
                    }).show();
                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
                }
            } else {
                DownloadButtonClicked();
            }
        } else {
            DownloadButtonClicked();
        }
    }

    private void DownloadButtonClicked() {
        buttonDownload.setClickable(false);
        textViewProgressValue.setText(getResources().getString(R.string.status_downloading_text));
        mainActivityVM.startDownload();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            DownloadButtonClicked();
        }
    }
}
