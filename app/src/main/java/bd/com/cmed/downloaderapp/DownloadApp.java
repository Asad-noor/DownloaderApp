package bd.com.cmed.downloaderapp;

import android.app.Application;
import bd.com.cmed.downloaderapp.util.SharedPref;

public class DownloadApp extends Application{

    public static DownloadApp mInstance;
    private SharedPref sharedPref;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        sharedPref = new SharedPref(mInstance);
    }

    public SharedPref getSharedPref(){
        return sharedPref;
    }

    public static DownloadApp getApp() {
        if (mInstance != null) {
            return mInstance;
        } else {
            mInstance = new DownloadApp();
            mInstance.onCreate();
            return mInstance;
        }
    }
}
