package ensharp.goldensignal;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class Send_MMS extends ActionBarActivity {
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send__mms);

        mContext = Send_MMS.this;

    }

    public void sendSMS(View v){

        String strName = "사고자 이름";
        String myPhoneNumber = "01048862255";


        //String strPhone = phone.getText().toString();
        //String strAddress = address.getText().toString();

        String total = "사고자 : " + strName + '\n' + "연락처 : " + myPhoneNumber + '\n' + "사고가 났습니다. 도와주세요.";

        if (total.length()>0 ){ //smsNum.length()>0 &&smsText.length()>0
            sendSMS("01048862255", total);
        }else{
            Toast.makeText(this, "모두 입력해 주세요", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendSMS(String smsNumber, String total){
        PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT_ACTION"), 0);
        PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED_ACTION"), 0);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch(getResultCode()){
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
                switch (getResultCode()){
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
}
