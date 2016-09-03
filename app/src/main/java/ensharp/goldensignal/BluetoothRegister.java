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
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Semin on 2016-09-01.
 */
public class BluetoothRegister extends ActionBarActivity {

    private Button pairingStart;
    private Toolbar toolbar;
    SharedPreferences pref;
    private static final int REQUEST_DISCOVERY = 0x1;
    private Handler _handler = new Handler();
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket socket = null;
    private String str;
    private OutputStream outputStream;
    private InputStream inputStream;
    private StringBuffer sbu;
    //private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_register);


        pref = new SharedPreferences(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.settingstoolbar);
        getSupportActionBar().setTitle("블루투스 연결");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        pairingStart = (Button) findViewById(R.id.pairingStart);
    }


    public void onBluetoothStartClick(View v) {
        bluetoothPair();
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
                        if (!MainActivity.isSendMMS) {
                            //if (!isSendMMS && (0.01 <= mySpeed) && (mySpeed <= 1.0)) { // 속도 범위 추가 (상황에 따라서 다시 바꿀 필요 있음
                            sendSMS("01048862255", reportContent(), false);
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
                            MainActivity.isSendMMS = true;
                        } else {
                            Toast.makeText(MainActivity.mContext, "자동신고 문자가 발송되지 않았습니다.", Toast.LENGTH_SHORT).show();
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

    public String reportContent() {

        String total;
        TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String myPhoneNumber = telManager.getLine1Number();
        String name = pref.getValue("이름", "", "user_info");
        String age = pref.getValue("나이", "", "user_info");
        String sex;
        String rhType;
        String bloodType;

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

        total = "오토바이 사고발생" + '\n' +
                "위도 : " + MainActivity.myLocation.getLatitude() + '\n' +
                "경도 : " + MainActivity.myLocation.getLongitude() + '\n' +
                "주소 : " + getAddress(MainActivity.mContext, MainActivity.myLocation.getLatitude(), MainActivity.myLocation.getLongitude()) + '\n' +
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
        if (!MainActivity.isSendMMS) {
            // 지정 연락처 사람에게 보내는 신고
            if (type) {
                //String phoneNumber = pref.getValue(Integer.toString(i), "no", "phoneNum");
                ArrayList<String> messageParts = mSmsManager.divideMessage(total);
                mSmsManager.sendMultipartTextMessage(smsNumber, null, messageParts, null, null);
                Toast.makeText(this, "보호자에게 전송완료.", Toast.LENGTH_SHORT).show();


                //mSmsManager.sendTextMessage(smsNumber, null, total, sentIntent, deliveredIntent);
            } else {
                // 지정 글자수 넘어갔을때 mms로 보내도록 119 로 보내는 신고//
                ArrayList<String> messageParts = mSmsManager.divideMessage(total);
                mSmsManager.sendMultipartTextMessage(smsNumber, null, messageParts, null, null);
                Toast.makeText(this, "119 신고완료.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onBackPressed() {

        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_phonenumber) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        return true;
        //  }
        //return super.onOptionsItemSelected(item);
    }

}
