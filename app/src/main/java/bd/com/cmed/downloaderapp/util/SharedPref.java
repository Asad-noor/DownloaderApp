package bd.com.cmed.downloaderapp.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;

    public static final String IS_APP_IN_BACKGROUND = "is_app_in_back";

    public SharedPref(Context context){
        sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREF_KEY, Context.MODE_PRIVATE);
    }

    public void setCommonBooleanValue(String key, boolean value){
        editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getCommonBoolean(String key){
        return sharedPreferences.getBoolean(key, false);
    }
}
