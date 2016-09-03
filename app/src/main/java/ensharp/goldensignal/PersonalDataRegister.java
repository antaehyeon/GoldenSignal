package ensharp.goldensignal;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;

public class PersonalDataRegister extends ActionBarActivity implements AdapterView.OnItemSelectedListener {
    EditText input_ID, input_Age;
    Button btn_send;

    RadioGroup radioGroup, radioGroup1;
    RadioButton men, women, rh_plus, rh_minus, btn_A, btn_B, btn_AB, btn_O;

    //SharedPreferences setting;
    //SharedPreferences.Editor editor;
    ArrayList<String> arraylist;
    ensharp.goldensignal.SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_data_register);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.settingstoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("내 정보");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pref = new ensharp.goldensignal.SharedPreferences(this);
        //layout 사용할 변수 선언
        input_ID = (EditText) findViewById(R.id.input_ID);
        input_Age = (EditText) findViewById(R.id.input_Age);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);
        men = (RadioButton) findViewById(R.id.Men);
        women = (RadioButton) findViewById(R.id.Women);
        rh_plus = (RadioButton) findViewById(R.id.rh_plus);
        rh_minus = (RadioButton) findViewById(R.id.rh_minus);
        btn_A = (RadioButton) findViewById(R.id.btn_a);
        btn_B = (RadioButton) findViewById(R.id.btn_b);
        btn_AB = (RadioButton) findViewById(R.id.btn_ab);
        btn_O = (RadioButton) findViewById(R.id.btn_o);
        //spinner = (Spinner)findViewById(R.id.spinner);
        btn_send = (Button) findViewById(R.id.btn_send);

        //setting = getSharedPreferences("user_info", MODE_PRIVATE);
        //editor= setting.edit();

        get_userInfo(); // 저장된 정보 불러오기


        btn_send.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                save_userInfo();
            }
        });
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

    // 입력한 값들을 저장하는 부분
    public void save_userInfo() {
        String ID = input_ID.getText().toString();
        String Age = input_Age.getText().toString();

        if (ID.equals("") && Age.equals("")) {
            Toast.makeText(PersonalDataRegister.this, "이름과 나이를 입력하십시오.", Toast.LENGTH_SHORT).show();
            input_ID.requestFocus();
        } else if (ID.equals("")) {
            Toast.makeText(PersonalDataRegister.this, "이름을 입력하십시오.", Toast.LENGTH_SHORT).show();
            input_ID.requestFocus();
        } else if (Age.equals("")) {
            Toast.makeText(PersonalDataRegister.this, "나이를 입력하십시오.", Toast.LENGTH_SHORT).show();
            input_Age.requestFocus();
        } else {
            pref.putValue("이름", ID, "user_info");
            pref.putValue("나이", Age, "user_info");
            pref.putValue("남", men.isChecked(), "user_info");
            pref.putValue("여", women.isChecked(), "user_info");
            pref.putValue("RH+", rh_plus.isChecked(), "user_info");
            pref.putValue("RH-", rh_minus.isChecked(), "user_info");
            pref.putValue("A", btn_A.isChecked(), "user_info");
            pref.putValue("B", btn_B.isChecked(), "user_info");
            pref.putValue("AB", btn_AB.isChecked(), "user_info");
            pref.putValue("O", btn_O.isChecked(), "user_info");
            pref.putValue("Auto_Login_enabled", true, "user_info");

            Intent intent = new Intent(this, MainActivity.class);
            Toast.makeText(PersonalDataRegister.this, "정보가 저장되었습니다", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(intent);
        }
    }

    //만약 저장된 정보가 아무것도 없을 경우, 임의의 값들로 설정해서 보여줌
    public void get_userInfo() {
        input_ID.setText(pref.getValue("이름", "", "user_info"));
        input_Age.setText(pref.getValue("나이", "", "user_info"));

        men.setChecked(pref.getValue("남", true, "user_info"));
        women.setChecked(pref.getValue("여", false, "user_info"));

        rh_plus.setChecked(pref.getValue("RH+", true, "user_info"));
        rh_minus.setChecked(pref.getValue("RH-", false, "user_info"));

        btn_A.setChecked(pref.getValue("A", true, "user_info"));
        btn_B.setChecked(pref.getValue("B", false, "user_info"));
        btn_AB.setChecked(pref.getValue("AB", false, "user_info"));
        btn_O.setChecked(pref.getValue("O", false, "user_info"));

    }

    @Override
    public void onBackPressed() {

        //pref.putValue("Auto_Login_enabled", true, "user_info");
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_phonenumber) {
        //pref.putValue("Auto_Login_enabled", true, "user_info");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        return true;
        //  }
        //return super.onOptionsItemSelected(item);
    }

}
