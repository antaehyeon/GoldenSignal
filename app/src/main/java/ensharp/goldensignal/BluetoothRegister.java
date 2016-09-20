package ensharp.goldensignal;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BluetoothRegister extends Activity implements RadioGroup.OnCheckedChangeListener {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;
    SharedPreferences pref;
    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect, btnSend;
    private EditText edtMessage;

    private static final int MILLISINFUTURE = 11 * 300;
    private static final int COUNT_DOWN_INTERVAL = 1000;
    int count;
    private CustomDialog mCustomDialog;
    CountDownTimer countDownTimer;
    Vibrator vibrator;
    MediaPlayer mp;
    public static boolean soundOn = false;
    private RelativeLayout checkedLayout;
    private RelativeLayout beforeConnectTxt;
    private RelativeLayout afterConnectTxt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_guide);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnConnectDisconnect = (Button) findViewById(R.id.btn_select);
        checkedLayout = (RelativeLayout) findViewById(R.id.checkedLayout);
        beforeConnectTxt = (RelativeLayout) findViewById(R.id.beforeConnectTxt);
        afterConnectTxt = (RelativeLayout) findViewById(R.id.afterConnectTxt);

        pref = new SharedPreferences(this);

        service_init();


        // Handle Disconnect & Connect button
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {
                    Intent newIntent = new Intent(BluetoothRegister.this, DeviceListActivity.class);
                    startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                }
            }
        });
    }

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }


        }

        public void onServiceDisconnected(ComponentName classname) {
            ////     mService.disconnect(mDevice);
            mService = null;
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        //Handler events that received from UART service
        public void handleMessage(Message msg) {
        }
    };


    public void countDownTimer() {


        countDownTimer = new CountDownTimer(MILLISINFUTURE, COUNT_DOWN_INTERVAL) {

            public void onTick(long millisUntilFinished) {
                playSound();
                CustomDialog.setCountTxt(String.valueOf(count));
                vibrator.vibrate(500);
                count--;

            }

            public void onFinish() {

                CustomDialog.setCountTxt(String.valueOf("신고완료"));
                mp.stop();
                mCustomDialog.dismiss();

                //SMS 및 MMS 보내기
                if (MainActivity.isSendMMS) {
                    //if (!isSendMMS && (0.01 <= mySpeed) && (mySpeed <= 1.0)) { // 속도 범위 추가 (상황에 따라서 다시 바꿀 필요 있음
                    sendSMS("01049122194", reportContent(), false);
                    for (int i = 0; i < 3; i++) {
                        String phoneNumber = pref.getValue(Integer.toString(i), "no", "phoneNum");
                        if (!phoneNumber.equals("no")) {
                            sendSMS(phoneNumber,
                                    "방금 전 " + pref.getValue("이름", "", "user_info") + " 님이 오토바이 사고를 당하셨습니다." + '\n' +
                                            "귀하께서 보호자로 등록되어 119에서 확인전화가 갈 수 있음을 알립니다." + '\n' + '\n' +
                                            "이 문자는 자동신고 서비스에 의해 발송되었습니다."
                                    , true);
                        }
                    }
                    //MainActivity.isSendMMS = false;
                } else {
                    Toast.makeText(MainActivity.mContext, "자동신고 문자가 발송되지 않았습니다.", Toast.LENGTH_SHORT).show();
                }

                //아두이노 LED 신호 보내기
                String message = "1";
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    //listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
                    //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                MainActivity.start.setText("주행 시작");
                MainActivity.data.setRunning(false);
                MainActivity.time.stop();
                MainActivity.time.setText("00:00:00");
                MainActivity.averageSpeed.setText("");
                MainActivity.data = new Data(MainActivity.onGpsServiceUpdate);
                BluetoothRegister.soundOn = false;
                MainActivity.drivingLayout.setVisibility(View.INVISIBLE);
                MainActivity.drivingInfoLayout.setVisibility(View.INVISIBLE);
                MainActivity.waitingInfoLayout.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.mContext, "서비스를 종료합니다", Toast.LENGTH_SHORT).show();
                MainActivity.isSendMMS = false;
                stopService(new Intent(getBaseContext(), GpsServices.class));
                MainActivity.isGetGps = false;
            }
        };
    }

    public String reportContent() {

        String total;
        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String myPhoneNumber = telManager.getLine1Number();
        String name = pref.getValue("이름", "", "user_info");
        String age = pref.getValue("나이", "", "user_info");
        String sex;
        String rhType;
        String bloodType;
        double latitude;
        double longitude;
        String address;

        if (pref.getValue("남", true, "user_info")) {
            sex = "남자";
        } else {
            sex = "여자";
        }

        if (pref.getValue("RH+", true, "user_info")) {
            rhType = "RH+";
        } else {
            rhType = "RH-";
        }

        if (pref.getValue("A", true, "user_info")) {
            bloodType = "A형";
        } else if (pref.getValue("B", true, "user_info")) {
            bloodType = "B형";
        } else if (pref.getValue("AB", true, "user_info")) {
            bloodType = "AB형";
        } else {
            bloodType = "O형";
        }

        if (MainActivity.isGetGps) {
            latitude = MainActivity.myLocation.getLatitude();
            longitude = MainActivity.myLocation.getLongitude();
            address = getAddress(MainActivity.mContext, MainActivity.myLocation.getLatitude(), MainActivity.myLocation.getLongitude());
        } else {
            latitude = 0.0;
            longitude = 0.0;
            address = "-";
        }

        total = "오토바이 사고발생" + '\n' +
                "위도 : " + latitude + '\n' +
                "경도 : " + longitude + '\n' +
                "주소 : " + address + '\n' +
                "사고자 : " + name + '\n' +
                "연락처 : " + myPhoneNumber + '\n' +
                sex + ", " + age + ", " + rhType + ", " + bloodType + '\n' +
                "보호자 연락처 목록" + '\n' +
                "1순위 : " + pref.getValue(Integer.toString(0), "-", "phoneNum") + '\n' +
                "2순위 : " + pref.getValue(Integer.toString(1), "-", "phoneNum") + '\n' +
                "3순위 : " + pref.getValue(Integer.toString(2), "-", "phoneNum") + '\n' + '\n' +
                "이 문자는 자동신고 서비스에 의해 발송되었습니다.";

        return total;
    }

    public static String getAddress(Context mContext, double lat, double lng) {
        String nowAddress = "현재 위치를 확인 할 수 없습니다.";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> address;
        try {
            if (geocoder != null) {
                // 한 좌표에 대해 두개 이상의 이름이 존재할 확률이 있기때문에, 주소배열을 리턴받기 위해 최대갯수 설정
                address = geocoder.getFromLocation(lat, lng, 1);

                if (address != null && address.size() > 0) {
                    // 주소를 받아 오는 부분
                    String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                    nowAddress = currentLocationAddress;
                }
            }
        } catch (IOException e) {
            Toast.makeText(MainActivity.mContext, "주소를 가져 올 수 없습니다.", Toast.LENGTH_SHORT).show();

            e.printStackTrace();
        }
        return nowAddress;

    }

    public void sendSMS(String smsNumber, String total, boolean type) {
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT_ACTION"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.mContext, "SMS 전송 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(MainActivity.mContext, "SMS 전송 실패", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(MainActivity.mContext, "서비스 지역이 아닙니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(MainActivity.mContext, "무선(Radio)가 꺼져있습니다", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(MainActivity.mContext, "PDU Null", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_SENT_ACTION"));

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.mContext, "SMS 도착완료", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(MainActivity.mContext, "SMS 도착안됨", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter("SMS_DELIVERED_ACTION"));

        SmsManager mSmsManager = SmsManager.getDefault();
        if (MainActivity.isSendMMS) {
            // 지정 연락처 사람에게 보내는 신고
            if (type) {
                //String phoneNumber = pref.getValue(Integer.toString(i), "no", "phoneNum");
                ArrayList<String> messageParts = mSmsManager.divideMessage(total);
                mSmsManager.sendMultipartTextMessage(smsNumber, null, messageParts, null, null);
                Toast.makeText(this, "보호자에게 문자전송이 완료되었습니다.", Toast.LENGTH_SHORT).show();


                //mSmsManager.sendTextMessage(smsNumber, null, total, sentIntent, deliveredIntent);
            } else {
                // 지정 글자수 넘어갔을때 mms로 보내도록 119 로 보내는 신고//
                ArrayList<String> messageParts = mSmsManager.divideMessage(total);
                mSmsManager.sendMultipartTextMessage(smsNumber, null, messageParts, null, null);
                Toast.makeText(this, "119 문자신고가 완료되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void playSound() {

        try {
            mp.reset();
            AssetFileDescriptor afd;
            afd = getAssets().openFd("alarmsound.mp3");
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.prepare();
            mp.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initiateAlertDialog() {
        try {
            mCustomDialog = new CustomDialog(MainActivity.mContext, cancelClickListener);
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            mp = new MediaPlayer();
            count = 3;
            countDownTimer();
            countDownTimer.start();
            mCustomDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //팝업창 닫기
    private View.OnClickListener cancelClickListener =
            new View.OnClickListener() {

                public void onClick(View v) {
                    countDownTimer.cancel();
                    mp.stop();
                    mCustomDialog.dismiss();
                    //아두이노 다시 원래대로 돌아오게 신호 보내기
                    String message = "9";
                    byte[] value;
                    try {
                        //send data to service
                        value = message.getBytes("UTF-8");
                        mService.writeRXCharacteristic(value);
                        //Update the log with time stamp
                        //String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        //listAdapter.add("[" + currentDateTimeString + "] TX: " + message);
                        //messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    } catch (UnsupportedEncodingException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            };

    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {


        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            final Intent mIntent = intent;
            //*********************//
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_CONNECT_MSG");
                        btnConnectDisconnect.setText("연결 완료");
                        checkedLayout.setVisibility(View.VISIBLE);
                        beforeConnectTxt.setVisibility(View.INVISIBLE);
                        afterConnectTxt.setVisibility(View.VISIBLE);
                        mState = UART_PROFILE_CONNECTED;
                        btnConnectDisconnect.setEnabled(false);

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(BluetoothRegister.this, MainActivity.class);
                                BluetoothRegister.this.startActivity(intent);
                            }
                        }, 1250);
                    }
                });
            }

            //*********************//
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                        Log.d(TAG, "UART_DISCONNECT_MSG");
                        btnConnectDisconnect.setText("제품 연결");
                        checkedLayout.setVisibility(View.INVISIBLE);
                        afterConnectTxt.setVisibility(View.INVISIBLE);
                        beforeConnectTxt.setVisibility(View.VISIBLE);
                        mState = UART_PROFILE_DISCONNECTED;
                        mService.close();
                        //setUiState();

                    }
                });
            }


            //*********************//
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                mService.enableTXNotification();
            }
            //*********************//
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {

                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            if (soundOn) {
                                initiateAlertDialog();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                });
            }
            //*********************//
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                showMessage("Device doesn't support UART. Disconnecting");
                mService.disconnect();
            }


        }
    };

    private void service_init() {
        Intent bindIntent = new Intent(this, UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        return intentFilter;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        }
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService = null;

    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {


            case REQUEST_SELECT_DEVICE:
                //When the DeviceListActivity return, with the selected device address
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

                    Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                    //((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName() + " - connecting");
                    mService.connect(deviceAddress);

                }
                break;

            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "블루투스가 활성화되었습니다.", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "앱을 종료합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }


    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

   /* @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.popup_title)
                    .setMessage(R.string.popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.popup_no, null)
                    .show();
        }
    }*/
}

