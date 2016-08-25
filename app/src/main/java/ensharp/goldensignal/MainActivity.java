package ensharp.goldensignal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

public class MainActivity extends ActionBarActivity implements LocationListener, GpsStatus.Listener {

    private SharedPreferences sharedPreferences;
    private LocationManager mLocationManager;
    private static Data data;
    private Button start;
    private Button reset;
    private TextView status;
    private TextView currentSpeed;
    private Toolbar toolbar;
    private Chronometer time;
    private Data.onGpsServiceUpdate onGpsServiceUpdate;

    private boolean firstfix;

    private String Distance_Long;
    private String Distance_Short;
    private String Speed_Units;
    private double Distance_Multiplier;
    private double Speed_Multiplier;
    private static final String TAG = "myApp";
    public static Location myLocation;
    public static float mySpeed;
    private RelativeLayout statusLayout;
    private RelativeLayout buttonLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        data = new Data(onGpsServiceUpdate);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        start = (Button) findViewById(R.id.start);
        start.setVisibility(View.INVISIBLE);
        reset = (Button) findViewById(R.id.reset);
        reset.setVisibility(View.INVISIBLE);

        Speed_Multiplier = 3.6;
        Distance_Long = " Km";
        Distance_Short = " m";
        Speed_Units = " km/h";
        Distance_Multiplier = 1000;
        onGpsServiceUpdate = new Data.onGpsServiceUpdate() {
            @Override
            public void update() {
            }
        };

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        status = (TextView) findViewById(R.id.status);
        time = (Chronometer) findViewById(R.id.time);
        currentSpeed = (TextView) findViewById(R.id.currentSpeed);
        statusLayout = (RelativeLayout) findViewById(R.id.status_layout);
        statusLayout.setVisibility(View.VISIBLE);
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
        }
        else if(id == R.id.action_user_favorite_contacts) {
            Intent intent = new Intent(this, UserFavoriteContacts.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onStartClick(View v) {
        if (!data.isRunning()) {
            //start.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
            start.setText("일시 정지");
            data.setRunning(true);
            time.setBase(SystemClock.elapsedRealtime() - data.getTime());
            time.start();
            data.setFirstTime(true);
            startService(new Intent(getBaseContext(), GpsServices.class));
            reset.setVisibility(View.INVISIBLE);
        } else {
            //start.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
            start.setText("주행 시작");
            data.setRunning(false);
            //status.setText("");
            statusLayout.setVisibility(View.GONE);
            stopService(new Intent(getBaseContext(), GpsServices.class));
            reset.setVisibility(View.VISIBLE);
        }
    }

    public void onResetClick(View v) {
        resetData();
        stopService(new Intent(getBaseContext(), GpsServices.class));
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

    public String getAddress(double lat, double lng) {
        String address = null;


        //위치정보를 활용하기 위한 구글 API 객체
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        //주소 목록을 담기 위한 HashMap
        List<Address> list = null;

        try {
            list = geocoder.getFromLocation(lat, lng, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (list == null) {
            Log.e("getAddress", "주소 데이터 얻기 실패");
            return null;
        }

        if (list.size() > 0) {

            Address addr = list.get(0);
            address = addr.getCountryName() + " "
                    + addr.getLocality() + " "
                    + addr.getThoroughfare() + " "
                    + addr.getFeatureName();
        }
        return address;
    }


    @Override
    public void onLocationChanged(Location location) {
        //Toast.makeText(this, "onLocationChanged 실행", Toast.LENGTH_SHORT).show();
        if (location.hasAccuracy()) {
            if (firstfix) {
                //status.setText("");
                statusLayout.setVisibility(View.GONE);
                start.setVisibility(View.VISIBLE);
                if (!data.isRunning()) {
                    reset.setVisibility(View.VISIBLE);
                }
                firstfix = false;
            }
        } else {
            firstfix = true;
        }
        if (data.isRunning()) {
            if (location.hasSpeed()) {
                myLocation = location;
                mySpeed = location.getSpeed();
                SpannableString s = new SpannableString(String.format("%.1f", mySpeed * Speed_Multiplier) + Speed_Units);
                s.setSpan(new RelativeSizeSpan(0.25f), s.length() - 3, s.length(), 0);
                currentSpeed.setText(s);
                //Toast.makeText(this, location.getLatitude() + "   " + location.getLongitude() + "   " + getAddress(location.getLatitude(), location.getLongitude()), Toast.LENGTH_SHORT).show();
            }
        }
    }

//    public void occurAccident()
//    {
//        double myVelocity = mySpeed * Speed_Multiplier;
//        if(myVelocity>=0.0&&myVelocity<=5.0)
//        {
//            call911(myLocation);
//        }
//        else
//            noAccident;
//
//    }


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
                    start.setText("주행 시작");
                    data.setRunning(true);
                    //status.setText("");
                    statusLayout.setVisibility(View.GONE);
                    stopService(new Intent(getBaseContext(), GpsServices.class));
                    start.setVisibility(View.INVISIBLE);
                    reset.setVisibility(View.INVISIBLE);
                    //status.setText(getResources().getString(R.string.waiting_for_fix));
                    statusLayout.setVisibility(View.VISIBLE);
                    data.setRunning(false);
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

    public void resetData() {
        //start.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
        start.setText("주행 시작");
        reset.setVisibility(View.INVISIBLE);
        time.stop();
        time.setText("00:00:00");
        data = new Data(onGpsServiceUpdate);
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