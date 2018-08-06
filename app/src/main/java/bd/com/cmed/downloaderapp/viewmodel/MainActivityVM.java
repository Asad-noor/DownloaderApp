package bd.com.cmed.downloaderapp.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.util.List;
import java.util.UUID;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkStatus;
import bd.com.cmed.downloaderapp.DownloadApp;
import bd.com.cmed.downloaderapp.service.DownloadWorker;
import bd.com.cmed.downloaderapp.util.Constants;

public class MainActivityVM extends ViewModel{

    private WorkManager mWorkManager;
    private OneTimeWorkRequest downloadingWork;

    private MutableLiveData<Long> progressValue = new MutableLiveData<>();

    public MainActivityVM(){
        LocalBroadcastManager.getInstance(DownloadApp.getApp()).registerReceiver(mMessageReceiver,
                new IntentFilter(Constants.INTENT_LOCAL_BROADCAST));
    }

    public LiveData<Long> getDownloadStatus(){
        return progressValue;
    }

    public void startDownload(){
        SetupScheduler();
    }

    public void stopDownload(){
        if(downloadingWork != null){
            UUID id = downloadingWork.getId();
            mWorkManager.cancelWorkById(id);

        }else{
            Log.d("tttt", "downloadingWork is null");
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            long value = intent.getIntExtra(Constants.INTENT_KEY, 0);
            progressValue.postValue(value);
        }
    };

    private void SetupScheduler(){
        mWorkManager = WorkManager.getInstance();
        LiveData<List<WorkStatus>> mSavedWorkStatus = mWorkManager.getStatusesByTag(Constants.TAG_WORKER_THREAD);

        if(mSavedWorkStatus.getValue() != null && !mSavedWorkStatus.getValue().isEmpty()) {

            if (mSavedWorkStatus.getValue().get(0).getState().isFinished()){
                scheduleTask();
            }
        }else {
            scheduleTask();
        }
    }

    private void scheduleTask(){
        downloadingWork =
                new OneTimeWorkRequest.Builder(DownloadWorker.class)
                        .addTag(Constants.TAG_WORKER_THREAD)
                        .build();
        mWorkManager.enqueue(downloadingWork);
    }
}
