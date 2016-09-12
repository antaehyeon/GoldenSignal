package ensharp.goldensignal;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Semin on 2016-09-10.
 */
public class CustomDialog extends Dialog {

    public static TextView countTxt;
    CountDownTimer countDownTimer;
    Vibrator vibrator;
    MediaPlayer mp;
    private Button btn_cancel;

    private View.OnClickListener mCancelClickListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.activity_countdown_timer);
        countTxt = (TextView) findViewById(R.id.countdown);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(mCancelClickListener);
        setCountTxt("20");
    }

    public static void setCountTxt(String count){
        countTxt.setText(count);
    }

    public CustomDialog(Context context, View.OnClickListener cancelListener) {
        // Dialog 배경을 투명 처리 해준다.
        super(context , android.R.style.Theme_Translucent_NoTitleBar);
        this.mCancelClickListener = cancelListener;
    }


}
