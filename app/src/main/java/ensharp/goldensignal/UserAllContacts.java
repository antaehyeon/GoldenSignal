package ensharp.goldensignal;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class UserAllContacts extends ActionBarActivity {

    String tag = "cap";
    int count = 0;
    ArrayList<Contacts> contactList;
    ArrayList<Contacts> checkedList;
    ArrayList<Contacts> existList;
    int selectedItemCount;
    int limit = 0;
    MyCustomAdapter dataAdapter = null;
    SharedPreferences pref;
    TextView limitTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_all_contacts);
        contactList = new ArrayList<>();
        existList = new ArrayList<>();
        String name;
        String phoneNumber;
        boolean isExist = false;
        selectedItemCount = 0;
        pref = new SharedPreferences(this);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.settingstoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("연락처 추가");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        for (int i = 0; i < 3; i++) {
            name = pref.getValue(Integer.toString(i), "no", "name");
            phoneNumber = pref.getValue(Integer.toString(i), "no", "phoneNum");
            existList.add(new Contacts(name, phoneNumber, false));
            if (!name.equals("no"))
                count++;
        }
        limit = 3 - count;
        Cursor cursor = getURI();  // 전화번호부 가져오기
        limitTxt = (TextView) findViewById(R.id.limit_num);
        limitTxt.setText(limit+"명");
        String[] bbStr = cursor.getColumnNames();
        for (int i = 0; i < bbStr.length; i++)
            // 각각의 컬럼 이름 확인
            Log.e(tag, "ColumnName " + i + " : " + cursor.getColumnName(i));
        if (cursor.moveToFirst()) {    //항상 처음에서 시작
            do {
                if (!cursor.getString(2).startsWith("01")) // 01로 시작하는 핸펀만
                    continue;
                // 이메일만 있는것은 제외
                // 요소값 얻기
                name = cursor.getString(1);  //이름
                //String  += "\n";
                phoneNumber = cursor.getString(2);
                //count++;
                for (int i = 0; i < 3; i++) {
                    if (name.equals(existList.get(i).name) && phoneNumber.equals(existList.get(i).phoneNum)) {
                        isExist = true;
                        break;
                    } else
                        isExist = false;
                }
                if (!isExist)
                    contactList.add(new Contacts(name, phoneNumber, false));
            } while (cursor.moveToNext());
        }
        cursor.close(); // 반드시 커서 닫고
        displayListView();
        checkButtonClick();

    }

    public static String makePhoneNumber(String phoneNumber) {

        String regEx = "(\\d{3})(\\d{3,4})(\\d{4})";
        if(!Pattern.matches(regEx, phoneNumber)) return null;
        return phoneNumber.replaceAll(regEx, "$1-$2-$3");
    }


    private void displayListView() {

        //create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomAdapter(this,
                R.layout.contact_listview, contactList);
        ListView listView = (ListView) findViewById(R.id.contactlistview);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);


//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//                // When clicked, show a toast with the TextView text
//                Contacts contacts = (Contacts) parent.getItemAtPosition(position);
//                Toast.makeText(getApplicationContext(),
//                        "Clicked on Row: " + contacts.getName(),
//                        Toast.LENGTH_LONG).show();
//            }
//        });

    }

    private class MyCustomAdapter extends ArrayAdapter<Contacts> {

        private ArrayList<Contacts> contactList;

        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<Contacts> contactList) {
            super(context, textViewResourceId, contactList);
            this.contactList = new ArrayList<Contacts>();
            this.contactList.addAll(contactList);
        }

        private class ViewHolder {
            TextView name;
            TextView phoneNumber;
            CheckBox checkBox;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            String changedPhoneNum;
            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));
            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.contact_listview, null);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.phoneNumber = (TextView) convertView.findViewById(R.id.phonenumber);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
                convertView.setTag(holder);

                holder.checkBox.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox checkBox = (CheckBox) v;
                        Contacts contacts = (Contacts) checkBox.getTag();
                        if (checkBox.isChecked()) {
                            if (selectedItemCount == limit) {
                                checkBox.setChecked(false);
                                Toast.makeText(getApplicationContext(),
                                        "현재" + " " + limit + "개 까지만 추가 가능합니다.",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                selectedItemCount++;
//                                Toast.makeText(getApplicationContext(),
//                                        "Clicked on Checkbox: " + checkBox.getText() +
//                                                " is " + checkBox.isChecked(),
//                                        Toast.LENGTH_LONG).show();
                                contacts.setisChecked(checkBox.isChecked());
                            }
                        } else {
//                            Toast.makeText(getApplicationContext(),
//                                    "Clicked on Checkbox: " + checkBox.getText() +
//                                            " is " + checkBox.isChecked(),
//                                    Toast.LENGTH_LONG).show();
                            contacts.setisChecked(checkBox.isChecked());
                            selectedItemCount--;
                        }
                    }
                });
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Contacts contacts = contactList.get(position);
            holder.name.setText(contacts.getName());
            changedPhoneNum = makePhoneNumber(contacts.getPhoneNum());
            holder.phoneNumber.setText(changedPhoneNum);
            holder.checkBox.setChecked(contacts.getisChecked());
            holder.checkBox.setTag(contacts);
            return convertView;
        }
    }

    private void checkButtonClick() {


        Button myButton = (Button) findViewById(R.id.findSelected);
        checkedList = new ArrayList<>();
        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

//                StringBuffer responseText = new StringBuffer();
//                responseText.append("The following were selected...\n");
                int index = 4 - limit;
                Intent intent = new Intent(UserAllContacts.this, UserFavoriteContacts.class);
                ArrayList<Contacts> contactList = dataAdapter.contactList;
                for (int i = 0; i < contactList.size(); i++) {
                    Contacts contacts = contactList.get(i);
                    if (contacts.getisChecked()) {
                        checkedList.add(new Contacts(contacts.getName(), contacts.getPhoneNum(), true));
                    }
                }
                if (checkedList.size() == 0)
                    Toast.makeText(getApplicationContext(),
                            "저장할 전화번호를 선택하여 주세요.", Toast.LENGTH_LONG).show();
                else {
                    for (int i = 0; i < checkedList.size(); i++) {
                        pref.putValue(Integer.toString(count + i), checkedList.get(i).getName(), "name");
                        pref.putValue(Integer.toString(count + i), checkedList.get(i).getPhoneNum(), "phoneNum");
                    }
//                    Toast.makeText(getApplicationContext(),
//                            "선택하신 전화번호가 추가되었습니다.", Toast.LENGTH_LONG).show();
//                    PhoneNumActivity phoneNumActivity = new PhoneNumActivity();
//                    phoneNumActivity.finish();
                    finish();
                    startActivity(intent);
                }
            }
        });

    }

    // 주소 불러오기 //
    private Cursor getURI() {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String[] projection = new String[]{ // 세개만 프로젝션함
                ContactsContract.Contacts._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        // 정렬방식 설정
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        return managedQuery(uri, projection, null, null, sortOrder);
    }

    public class Contacts {

        String name;
        String phoneNum;
        boolean isChecked;

        public Contacts(String name, String phoneNum, boolean isChecked) {
            this.name = name;
            this.phoneNum = phoneNum;
            this.isChecked = isChecked;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhoneNum() {
            return phoneNum;
        }

        public void setPhoneNum(String phoneNum) {
            this.phoneNum = phoneNum;
        }

        public boolean getisChecked() {
            return isChecked;
        }

        public void setisChecked(boolean isChecked) {
            this.isChecked = isChecked;
        }
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(this, UserFavoriteContacts.class);
        finish();
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

            //int id = item.getItemId();
            //noinspection SimplifiableIfStatement
            //if (id == R.id.action_phonenumber) {
            Intent intent = new Intent(this, UserFavoriteContacts.class);
            startActivity(intent);
            finish();
            return true;
      //  }
       //return super.onOptionsItemSelected(item);
    }

}