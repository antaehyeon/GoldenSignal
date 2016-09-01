package ensharp.goldensignal;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

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
    private RelativeLayout drivingLayout;
    //private RelativeLayout waitingLayout;

    private static final int REQUEST_DISCOVERY = 0x1;
    private Handler _handler = new Handler();
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket socket = null;
    private String str;
    private OutputStream outputStream;
    private InputStream inputStream;
    private StringBuffer sbu;
    private Context mContext;
    String myPhoneNumber;
    ensharp.goldensignal.SharedPreferences pref;
    boolean isBluetoothPairing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pref = new ensharp.goldensignal.SharedPreferences(this);

        if (!pref.getValue("Auto_Login_enabled", false, "user_info")) {
            Intent intent = new Intent(this, PersonalDataRegister.class);
            finish();
            startActivity(intent);
        }

        data = new Data(onGpsServiceUpdate);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        start = (Button) findViewById(R.id.start);
        //start.setVisibility(View.INVISIBLE); --> 이거 다시 주석해제
        //reset = (Button) findViewById(R.id.reset);
        //reset.setVisibility(View.INVISIBLE);
        drivingLayout = (RelativeLayout) findViewById(R.id.driving);
        drivingLayout.setVisibility(View.INVISIBLE);
        //reset.setVisibility(View.VISIBLE);
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
        //status = (TextView) findViewById(R.id.status);
        //time = (Chronometer) findViewById(R.id.time);
        currentSpeed = (TextView) findViewById(R.id.currentSpeed);
        statusLayout = (RelativeLayout) findViewById(R.id.status_layout);
        //statusLayout.setVisibility(View.VISIBLE);
        drivingLayout.setVisibility(View.INVISIBLE);
//        time.setText("00:00:00");
//        time.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
//            boolean isPair = true;
//
//            @Override
//            public void onChronometerTick(Chronometer chrono) {
//                long time;
//                if (data.isRunning()) {
//                    time = SystemClock.elapsedRealtime() - chrono.getBase();
//                    data.setTime(time);
//                } else {
//                    time = data.getTime();
//                }
//
//                int h = (int) (time / 3600000);
//                int m = (int) (time - h * 3600000) / 60000;
//                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
//                String hh = h < 10 ? "0" + h : h + "";
//                String mm = m < 10 ? "0" + m : m + "";
//                String ss = s < 10 ? "0" + s : s + "";
//                chrono.setText(hh + ":" + mm + ":" + ss);
//
//                if (data.isRunning()) {
//                    chrono.setText(hh + ":" + mm + ":" + ss);
//                } else {
//                    if (isPair) {
//                        isPair = false;
//                        chrono.setText(hh + ":" + mm + ":" + ss);
//                    } else {
//                        isPair = true;
//                        chrono.setText("");
//                    }
//                }
//            }
//        });
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
        }

        return super.onOptionsItemSelected(item);
    }

    public void onStartClick(View v) {
        if (!data.isRunning()) {
            if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "단말기 설정에서 '위치 서비스'사용을 허용해주세요", Toast.LENGTH_SHORT).show();
            } else {
                if (_bluetooth.isEnabled()) {
                    bluetoothPair();
                    start.setText("주행 종료");
                    data.setRunning(true);
//                time.setBase(SystemClock.elapsedRealtime() - data.getTime());
//                time.start();
                    data.setFirstTime(true);
                    startService(new Intent(getBaseContext(), GpsServices.class));
                    Toast.makeText(this, "주행을 시작합니다", Toast.LENGTH_SHORT).show();
                    //reset.setVisibility(View.INVISIBLE);
                } else {
                    Toast.makeText(this, "블루투스 연결이 불가합니다", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            //start.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
            drivingLayout.setVisibility(View.VISIBLE);
            start.setText("주행 시작");
            data.setRunning(false);
            //status.setText("");
//            statusLayout.setVisibility(View.GONE);
            Toast.makeText(this, "서비스를 종료합니다", Toast.LENGTH_SHORT).show();
            stopService(new Intent(getBaseContext(), GpsServices.class));
//            reset.setVisibility(View.VISIBLE);
        }
    }

    public void bluetoothPair() {
        //start.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
        Intent intent = new Intent(this, DiscoveryActivity.class);
        /* Prompted to select a server to connect */
        Toast.makeText(this, "select device to connect", Toast.LENGTH_SHORT).show();
        /* Select device for list */
        startActivityForResult(intent, REQUEST_DISCOVERY);
    }

    /* after select, connect to device */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_DISCOVERY) {
            finish();
            return;
        }
        if (resultCode != RESULT_OK) {
            finish();
            return;
        }
        final BluetoothDevice device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        new Thread() {
            public void run() {
                connect(device);
            }

            ;
        }.start();
    }

    protected void connect(BluetoothDevice device) {
        BluetoothSocket socket = null;
        try {
            //Create a Socket connection: need the server's UUID number of registered
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            socket.connect();
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            int read = -1;
            final byte[] bytes = new byte[2048];
            for (; (read = inputStream.read(bytes)) > -1; ) {
                final int count = read;
                _handler.post(new Runnable() {
                    public void run() {
                        StringBuilder b = new StringBuilder();
                        for (int i = 0; i < count; ++i) {
                            String s = Integer.toString(bytes[i]);
                            b.append(s);
                            b.append(",");
                        }
                        String s = b.toString();
                        String[] chars = s.split(",");
                        sbu = new StringBuffer();
                        for (int i = 0; i < chars.length; i++) {
                            sbu.append((char) Integer.parseInt(chars[i]));
                        }

                        String strName = "사고자 이름";

                        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                        myPhoneNumber = telManager.getLine1Number();

                        String total = "사고자 : " + strName + '\n' + "연락처 : " + myPhoneNumber + '\n' + "사고가 났습니다. 도와주세요.";

                        if (total.length() > 0) { //smsNum.length()>0 &&smsText.length()>0
                            sendSMS("01048862255", total);
                        } else {
                            Toast.makeText(MainActivity.this, "모두 입력해 주세요", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        } catch (IOException e) {
            finish();
            return;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                    finish();
                    return;
                } catch (IOException e) {
                }
            }
        }
    }

    public void sendSMS(String smsNumber, String total) {
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT_ACTION"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(mContext, "SMS 전송 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(mContext, "SMS 전송 실패", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(mContext, "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(mContext, "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(mContext, "PDU Null", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_SENT_ACTION"));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(mContext, "SMS 도착완료", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(mContext, "SMS 도착안됨", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_DELIVERED_ACTION"));

        SmsManager mSmsManager = SmsManager.getDefault();
        mSmsManager.sendTextMessage("01048862255", null, total, sentIntent, deliveredIntent);

        // 지정 글자수 넘어갔을때 mms로 보내도록 //
        ArrayList<String> messageParts = mSmsManager.divideMessage(total);

        mSmsManager.sendMultipartTextMessage("01048862255", null, messageParts, null, null);

        Toast.makeText(this, "MMS 전송완료.", Toast.LENGTH_SHORT).show();
    }


//    public void onResetClick(View v) {
//        resetData();
//        stopService(new Intent(getBaseContext(), GpsServices.class));
//    }

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
        if (location.hasAccuracy()) {
            if (firstfix) {
                drivingLayout.setVisibility(View.VISIBLE);
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
//            //call911(myLocation);
//        }
//        else
//            //noAccident;
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
                    drivingLayout.setVisibility(View.INVISIBLE);
                    //data.setRunning(true);
                    //status.setText("");
                    //statusLayout.setVisibility(View.GONE);
                    stopService(new Intent(getBaseContext(), GpsServices.class));
                    //start.setVisibility(View.INVISIBLE);  --> 이거 다시 주석해제
                    //reset.setVisibility(View.INVISIBLE);
                    //status.setText(getResources().getString(R.string.waiting_for_fix));
                    //statusLayout.setVisibility(View.VISIBLE);
                    if (data.isRunning()) {
                        Toast.makeText(this, "현재 GPS 확인이 불가합니다", Toast.LENGTH_SHORT).show();
                    }
                    drivingLayout.setVisibility(View.INVISIBLE);
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

//    public void resetData() {
//        //start.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
//        start.setText("주행 시작");
//        reset.setVisibility(View.INVISIBLE);
//        time.stop();
//        time.setText("00:00:00");
//        data = new Data(onGpsServiceUpdate);
//    }

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