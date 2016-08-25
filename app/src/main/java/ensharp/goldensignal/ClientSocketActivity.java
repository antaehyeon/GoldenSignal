package ensharp.goldensignal;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ClientSocketActivity extends Activity
{
    private static final String TAG = ClientSocketActivity.class.getSimpleName();
    private static final int REQUEST_DISCOVERY = 0x1;;
    private Handler _handler = new Handler();
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket socket = null;
    private TextView sTextView;
    private EditText sEditText;
    private String str;
    private OutputStream outputStream;
    private InputStream inputStream;
    private StringBuffer sbu;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        setContentView(R.layout.client_socket);
        if (!_bluetooth.isEnabled()) {
            finish();
            return;
        }
        Intent intent = new Intent(this, DiscoveryActivity.class);

		/* Prompted to select a server to connect */
        Toast.makeText(this, "select device to connect", Toast.LENGTH_SHORT).show();

		/* Select device for list */
        startActivityForResult(intent, REQUEST_DISCOVERY);

        sTextView = (TextView)findViewById(R.id.sTextView);
        sEditText = (EditText)findViewById(R.id.sEditText);
    }


    public void onButtonClicksend(View view) throws IOException {

        if(str != null)
        {
            sTextView.setText(str + "--> " + sEditText.getText());
            str += ("--> " + sEditText.getText().toString());
        }
        else
        {
            sTextView.setText("--> " + sEditText.getText());
            str = "--> " + sEditText.getText().toString();
        }
        str += '\n';

        String tmpStr=sEditText.getText().toString();
        byte bytes[] = tmpStr.getBytes();
        outputStream.write(bytes);
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
            };
        }.start();
    }

    protected void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
    }

    protected void connect(BluetoothDevice device) {
        //BluetoothSocket socket = null;
        try {
            //Create a Socket connection: need the server's UUID number of registered
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            socket.connect();
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            int read = -1;
            final byte[] bytes = new byte[2048];
            for (; (read = inputStream.read(bytes)) > -1;) {
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
                        if(str != null)
                        {
                            sTextView.setText(str + "<-- " + sbu);
                            str += ("<-- " + sbu.toString());
                        }
                        else
                        {
                            sTextView.setText("<-- " + sbu);
                            str = "<-- " + sbu.toString();
                        }



                        str += '\n';
                        Uri uri = Uri.parse("content://media/external/images/media/23");
                        Intent it = new Intent(Intent.ACTION_SEND);
                        it.putExtra("sms_body", "오토바이 사고입니다." );
                        it.putExtra(Intent.EXTRA_STREAM, uri);
                        it.setType("image/png");
                        startActivity(it);



                    }
                });
            }

        }

        catch (IOException e) {
            finish();
            return ;
        }

        finally {
            if (socket != null) {
                try
                {
                    socket.close();
                    finish();
                    return ;
                }
                catch (IOException e)
                {
                }
            }
        }
    }
}
