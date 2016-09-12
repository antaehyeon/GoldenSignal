package ensharp.goldensignal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {

    /* 스플래쉬 화면이 뜨는 시간 */
    private final int SPLASH_DISPLAY_LENGTH = 2000;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_activity);
        //pref = new SharedPreferences(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, BluetoothRegister.class);
                SplashActivity.this.startActivity(intent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);

//        Intent intent = new Intent(this, MainActivity.class);
//        finish();
//        startActivity(intent);
    }


}
