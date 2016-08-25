package ensharp.goldensignal;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

public class PersonalDataRegister extends Activity {
    EditText input_ID, input_Age;
    Button btn_send;

    RadioGroup radioGroup, radioGroup1;
    RadioButton men, women, rh_plus, rh_minus;
    Spinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_data_register);

        //layout 사용할 변수 선언
        input_ID = (EditText) findViewById(R.id.input_ID);
        input_Age = (EditText) findViewById(R.id.input_Age);
        radioGroup = (RadioGroup)findViewById(R.id.radioGroup);
        radioGroup1 = (RadioGroup)findViewById(R.id.radioGroup1);
        men = (RadioButton)findViewById(R.id.Men);
        women = (RadioButton)findViewById(R.id.Women);
        rh_plus = (RadioButton)findViewById(R.id.rh_plus);
        rh_minus = (RadioButton)findViewById(R.id.rh_minus);
        spinner = (Spinner)findViewById(R.id.spinner);
        btn_send = (Button) findViewById(R.id.btn_send);
    }

    //혈액형을 눌렀을때 토스트창을 띄우기 위해
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        //Toast.makeText(this, arraylist.get(arg2), Toast.LENGTH_LONG).show();//해당목차눌렸을때
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

}
