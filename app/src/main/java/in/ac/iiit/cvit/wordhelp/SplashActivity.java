/**
 * Created by jerin on 10/11/16.
 */


package in.ac.iiit.cvit.wordhelp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class SplashActivity extends AppCompatActivity {

    private LaunchPreferenceManager launchPreferenceManager;

    private static final int SLEEP = 5;
    private static final String LOGTAG = "Heritage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_splash);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        launchPreferenceManager = new LaunchPreferenceManager(SplashActivity.this);

        Thread background = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(SLEEP*1000);

                    if (!launchPreferenceManager.isFirstTimeLaunch()) {
                        Intent intent_packages_list = new Intent(SplashActivity.this, MainActivity.class);
                        intent_packages_list.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent_packages_list);
                        finish();
                    } else {
                        launchPreferenceManager.setFirstTimeLaunch(false);
                        Intent intent_splash_intro = new Intent(SplashActivity.this, MainActivity.class);
                        intent_splash_intro.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent_splash_intro);
                        finish();
                    }
                } catch (Exception e) {
                    Log.e(LOGTAG, e.toString());
                }
            }
        };
        background.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent_splash = new Intent(Intent.ACTION_MAIN);
        intent_splash.addCategory(Intent.CATEGORY_HOME);
        intent_splash.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent_splash);

        finish();
        System.exit(0);
    }

}
