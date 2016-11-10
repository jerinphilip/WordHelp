package in.ac.iiit.cvit.wordhelp;

/**
 * Created by jerin on 10/11/16.
 */

import android.content.Context;
import android.content.SharedPreferences;

public class LaunchPreferenceManager {

    private int PRIVATE_MODE = 0;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context _context;

    private static final String PREF_NAME = "launch_preferences";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public LaunchPreferenceManager(Context context) {
        this._context = context;
        preferences = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = preferences.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return preferences.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }
}
