package ensharp.goldensignal;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class PersonalDataRegister extends Activity {
    EditText input_ID, input_Age;
    Button btn_send;

    RadioGroup radioGroup, radioGroup1;
    RadioButton men, women, rh_plus, rh_minus;
    Spinner spinner;

    SharedPreferences setting;
    SharedPreferences.Editor editor;
    ArrayList<String> arraylist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_data_register);

        using_spinner();

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

        setting = getSharedPreferences("user_info", MODE_PRIVATE);
        editor= setting.edit();

        get_userInfo(); // 저장된 정보 불러오기


        btn_send.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                save_userInfo();
            }
        }) ;
    }

    //혈액형을 눌렀을때 토스트창을 띄우기 위해
    //@Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        //Toast.makeText(this, arraylist.get(arg2), Toast.LENGTH_LONG).show();//해당목차눌렸을때
    }

    //@Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    // 입력한 값들을 저장하는 부분
    public  void save_userInfo()
    {
        String ID = input_ID.getText().toString();
        String Age = input_Age.getText().toString();

        editor.putString("이름", ID);
        editor.putString("나이", Age);
        editor.putBoolean("남",men.isChecked());
        editor.putBoolean("여",women.isChecked());
        editor.putBoolean("RH+",rh_plus.isChecked());
        editor.putBoolean("RH-",rh_minus.isChecked());
        editor.putString("혈액형", spinner.getSelectedItem().toString());

        editor.putBoolean("Auto_Login_enabled", true);
        editor.commit();

        Toast.makeText(PersonalDataRegister.this, "저장됨", Toast.LENGTH_SHORT).show();
    }

    //만약 저장된 정보가 아무것도 없을 경우, 임의의 값들로 설정해서 보여줌
    public  void get_userInfo()
    {
        input_ID.setText(setting.getString("이름", ""));
        input_Age.setText(setting.getString("나이", ""));

        men.setChecked(setting.getBoolean("남", true));
        women.setChecked(setting.getBoolean("여", false));

        rh_plus.setChecked(setting.getBoolean("RH+", true));
        rh_minus.setChecked(setting.getBoolean("RH-", false));

    }

    // 스피너 (혈액형 정보) 정보 등록& 선언
    public void using_spinner()
    {
        //스피너에 사용될 아이템 정보
        arraylist = new ArrayList<String>();
        arraylist.add("A");
        arraylist.add("B");
        arraylist.add("AB");
        arraylist.add("O");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, arraylist);
        //스피너 속성
        Spinner sp = (Spinner) this.findViewById(R.id.spinner);
        sp.setPrompt("선택해주세요"); // 스피너 제목
        sp.setAdapter(adapter);
        sp.setOnItemSelectedListener(this);

    }

}
