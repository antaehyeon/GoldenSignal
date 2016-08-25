package ensharp.goldensignal;

import android.app.Activity;
import android.os.Bundle;
import android.os.Bundle;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;


public class BluetoothRegister extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_register);
    }
    public void onOpenClientSocketButtonClicked(View view)
    {
        Intent enabler = new Intent(this, ClientSocketActivity.class);
        startActivity(enabler);
    }
}
