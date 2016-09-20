package ensharp.goldensignal;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends ActionBarActivity implements LocationListener, GpsStatus.Listener {

    private SharedPreferences sharedPreferences;
    private LocationManager mLocationManager;
    public static Data data;
    public static Button start;
    private Button reset;
    private TextView status;
    private TextView currentSpeed;
    private Toolbar toolbar;
    public static Data.onGpsServiceUpdate onGpsServiceUpdate;

    private boolean firstfix;

    private String Distance_Long;
    private String Distance_Short;
    private String Speed_Units;
    private double Distance_Multiplier;
    private double Speed_Multiplier;
    private static final String TAG = "myApp";
    public static Location myLocation;
    public static float mySpeed;

    public static RelativeLayout drivingLayout;
    public static double ave_speed;
    String ave;

    private static final int REQUEST_DISCOVERY = 0x1;
    private Handler _handler = new Handler();
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket socket = null;
    private String str;
    private OutputStream outputStream;
    private InputStream inputStream;
    private StringBuffer sbu;
    public static Context mContext;
    ensharp.goldensignal.SharedPreferences pref;
    boolean isBluetoothPairing;
    public static boolean isSendMMS;

    private static final int MILLISINFUTURE = 11 * 2000;
    private static final int COUNT_DOWN_INTERVAL = 1000;

    private TextView averageSpeedName;
    public static TextView averageSpeed;
    private TextView timeName;
    public static Chronometer time;
    public static LinearLayout drivingInfoLayout;
    public static RelativeLayout waitingInfoLayout;
    public static boolean isGetGps = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* HYEON
            Splash Activity ADD */
        //startActivity(new Intent(this, SplashActivity.class));


        pref = new ensharp.goldensignal.SharedPreferences(this);
        if (!pref.getValue("Auto_Login_enabled", false, "user_info")) {
            Intent intent = new Intent(this, PersonalDataRegister.class);
            finish();
            startActivity(intent);
        }
        averageSpeedName = (TextView) findViewById(R.id.averageSpeedTxt);
        averageSpeed = (TextView) findViewById(R.id.averageSpeed);
        timeName = (TextView) findViewById(R.id.timeTxt);
        time = (Chronometer) findViewById(R.id.time);
        Typeface changeFonts = Typeface.createFromAsset(getAssets(), "gothic_font.ttf");
        averageSpeedName.setTypeface(changeFonts);
        averageSpeed.setTypeface(changeFonts);
        timeName.setTypeface(changeFonts);
        time.setTypeface(changeFonts);
        data = new Data(onGpsServiceUpdate);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("홈");
        start = (Button) findViewById(R.id.start);
        //start.setVisibility(View.INVISIBLE); --> 이거 다시 주석해제
        //reset = (Button) findViewById(R.id.reset);
        //reset.setVisibility(View.INVISIBLE);
        drivingLayout = (RelativeLayout) findViewById(R.id.driving);
        drivingLayout.setVisibility(View.INVISIBLE);
        drivingInfoLayout = (LinearLayout) findViewById(R.id.drivingInfoLayout);
        waitingInfoLayout = (RelativeLayout) findViewById(R.id.waitingInfoLayout);

        //reset.setVisibility(View.VISIBLE);
        Speed_Multiplier = 3.5;
        Distance_Long = " Km";
        Distance_Short = " m";
        Speed_Units = " km/h";
        Distance_Multiplier = 1000;
        onGpsServiceUpdate = new Data.onGpsServiceUpdate() {
            @Override
            public void update() {
                //Set the average
                String ave;
                isGetGps = true;
                ave_speed = data.getAverageSpeed();
                ave = String.format("%.1f", ave_speed * Speed_Multiplier) + " " + Speed_Units;
                //Toast.makeText(MainActivity.this, "평균속도 : " + ave_speed + " 변환형 : " + ave, Toast.LENGTH_SHORT).show();
                averageSpeed.setText(ave);
            }
        };
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        currentSpeed = (TextView) findViewById(R.id.currentSpeed);
        drivingLayout.setVisibility(View.INVISIBLE);
        time.setText("00:00:00");
        time.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            boolean isPair = true;

            @Override
            public void onChronometerTick(Chronometer chrono) {
                long time;
                if (data.isRunning()) {
                    time = SystemClock.elapsedRealtime() - chrono.getBase();
                    data.setTime(time);
                } else {
                    time = data.getTime();
                }

                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                String hh = h < 10 ? "0" + h : h + "";
                String mm = m < 10 ? "0" + m : m + "";
                String ss = s < 10 ? "0" + s : s + "";
                chrono.setText(hh + ":" + mm + ":" + ss);

                if (data.isRunning()) {
                    chrono.setText(hh + ":" + mm + ":" + ss);
                } else {
                    if (isPair) {
                        isPair = false;
                        chrono.setText(hh + ":" + mm + ":" + ss);
                    } else {
                        isPair = true;
                        chrono.setText("");
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_personal_data_register) {
            Intent intent = new Intent(this, PersonalDataRegister.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_user_favorite_contacts) {
            Intent intent = new Intent(this, UserFavoriteContacts.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_blutooth_register) {
            Intent intent = new Intent(this, BluetoothRegister.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onStartClick(View v) {
        if (!data.isRunning()) {
            if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "단말기 설정에서 '위치 서비스'사용을 허용해주세요", Toast.LENGTH_SHORT).show();
            } else {
                if (_bluetooth.isEnabled()) {
                    averageSpeed.setText(ave);
                    start.setText("주행 종료");
                    time.setBase(SystemClock.elapsedRealtime() - data.getTime());
                    time.start();
                    data.setFirstTime(true);
                    isSendMMS = true;
                    BluetoothRegister.soundOn = true;
                    startService(new Intent(getBaseContext(), GpsServices.class));
                    Toast.makeText(this, "주행을 시작합니다", Toast.LENGTH_SHORT).show();
                    data.setRunning(true);
                    drivingLayout.setVisibility(View.VISIBLE);
                    drivingInfoLayout.setVisibility(View.VISIBLE);
                    waitingInfoLayout.setVisibility(View.INVISIBLE);
                    SpannableString s = new SpannableString(String.format("%.1f", mySpeed * Speed_Multiplier));
                    currentSpeed.setText(s);
                } else {
                    Toast.makeText(this, "블루투스 연결이 불가합니다", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            start.setText("주행 시작");
            data.setRunning(false);
            time.stop();
            time.setText("00:00:00");
            averageSpeed.setText("");
            data = new Data(onGpsServiceUpdate);
            BluetoothRegister.soundOn = false;
            drivingLayout.setVisibility(View.INVISIBLE);
            drivingInfoLayout.setVisibility(View.INVISIBLE);
            waitingInfoLayout.setVisibility(View.VISIBLE);
            Toast.makeText(this, "서비스를 종료합니다", Toast.LENGTH_SHORT).show();
            isSendMMS = false;
            stopService(new Intent(getBaseContext(), GpsServices.class));
            isGetGps = false;
            //_bluetooth.disable();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //Toast.makeText(this, "onResume 실행", Toast.LENGTH_SHORT).show();
        firstfix = true;
        if (data == null) {
            data = new Data(onGpsServiceUpdate);
        } else {
            data.setOnGpsServiceUpdate(onGpsServiceUpdate);
        }

        if (mLocationManager.getAllProviders().indexOf(LocationManager.GPS_PROVIDER) >= 0) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
        } else {
            Log.w("MainActivity", "No GPS location provider found. GPS data display will not be available.");
        }

        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "단말기 설정에서 '위치 서비스'사용을 허용해주세요", Toast.LENGTH_SHORT).show();
        }

        mLocationManager.addGpsStatusListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);
        mLocationManager.removeGpsStatusListener(this);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        //Toast.makeText(this, "onPause 실행", Toast.LENGTH_SHORT).show();
        prefsEditor.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(new Intent(getBaseContext(), GpsServices.class));
        //Toast.makeText(this, "onDestroy 실행", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onLocationChanged(Location location) {
        //Toast.makeText(this, "onLocationChanged 실행", Toast.LENGTH_SHORT).show();
        SpannableString s;
        if (location.hasAccuracy()) {
            if (firstfix) {
                //drivingLayout.setVisibility(View.VISIBLE);
                //status.setText("");
                //statusLayout.setVisibility(View.GONE);
                //start.setVisibility(View.VISIBLE);
//                if (!data.isRunning()) {
//                    reset.setVisibility(View.VISIBLE);
//                }
                firstfix = false;
            }
        } else {
            firstfix = true;
        }
        if (data.isRunning()) {
            if (location.hasSpeed()) {
                myLocation = location;
                mySpeed = location.getSpeed();
                if (mySpeed >= 100.0) {
                    s = new SpannableString(String.format("%.0f", mySpeed * Speed_Multiplier));
                } else {
                    s = new SpannableString(String.format("%.1f", mySpeed * Speed_Multiplier));
                }
                //s.setSpan(new RelativeSizeSpan(0.25f), s.length() - 3, s.length(), 0);
                currentSpeed.setText(s);
                //Toast.makeText(this, location.getLatitude() + "   " + location.getLongitude() + "   " + getAddress(location.getLatitude(), location.getLongitude()), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                int satsInView = 0;
                int satsUsed = 0;
                Iterable<GpsSatellite> sats = gpsStatus.getSatellites();
                for (GpsSatellite sat : sats) {
                    satsInView++;
                    if (sat.usedInFix()) {
                        satsUsed++;
                    }
                }

                if (satsUsed == 0) {
                    //start.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
                    //start.setText("주행 시작"); --> 이거 해제해야함
                    //drivingLayout.setVisibility(View.INVISIBLE); --> 이거 다시 주석해제
                    //data.setRunning(true);
                    //status.setText("");
                    //statusLayout.setVisibility(View.GONE);
                    stopService(new Intent(getBaseContext(), GpsServices.class));
                    //start.setVisibility(View.INVISIBLE);  --> 이거 다시 주석해제
                    //reset.setVisibility(View.INVISIBLE);
                    //status.setText(getResources().getString(R.string.waiting_for_fix));
                    //statusLayout.setVisibility(View.VISIBLE);
//                    if (data.isRunning()) {
//                        Toast.makeText(this, "현재 GPS 확인이 불가합니다", Toast.LENGTH_SHORT).show();
//                    }
                    isGetGps = false;
                    //drivingLayout.setVisibility(View.INVISIBLE); --> 이거 다시 주석해제
                    //data.setRunning(false); --> 이거 해제해야함
                    firstfix = true;
                }
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast.makeText(this, "단말기 설정에서 '위치 서비스'사용을 허용해주세요", Toast.LENGTH_SHORT).show();
                }
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;
        }
    }

    public static Data getData() {
        return data;
    }

    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }
}