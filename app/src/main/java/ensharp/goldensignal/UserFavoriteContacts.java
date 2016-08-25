package ensharp.goldensignal;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Semin on 2016-08-22.
 */
public class UserFavoriteContacts extends ActionBarActivity {

    int count;
    ArrayList<Contacts> saveList;
    SharedPreferences pref;
    MyCustomAdapter dataAdapter = null;
    ListView listView;
    View recentView;
    boolean unChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_favorite_contacts);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.settingstoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        saveList = new ArrayList<>();
        String name;
        String phoneNumber;
        unChecked = true;

        pref = new SharedPreferences(this);
        for (int i = 0; i < 3; i++) {
            name = pref.getValue(Integer.toString(i), "no", "name");
            phoneNumber = pref.getValue(Integer.toString(i), "no", "phoneNum");
            if (!name.equals("no"))
                saveList.add(new Contacts(name, phoneNumber, false));
        }

        displayListView();
        checkButtonClick();
    }

    private void displayListView() {

        //create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomAdapter(this,
                R.layout.contact_listview, saveList);
        listView = (ListView) findViewById(R.id.mylist);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Contacts contacts = (Contacts) parent.getItemAtPosition(position);
//                Toast.makeText(getApplicationContext(),
//                        "Clicked on Row: " + contacts.getName(),
//                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private class MyCustomAdapter extends ArrayAdapter<Contacts> {

        private ArrayList<Contacts> saveList;

        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<Contacts> saveList) {
            super(context, textViewResourceId, saveList);
            this.saveList = new ArrayList<Contacts>();
            this.saveList.addAll(saveList);
        }

        private class ViewHolder {
            TextView name;
            TextView phoneNumber;
            CheckBox checkBox;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

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
                        CheckBox checkBox;
                        Contacts contacts;
                        if (!unChecked) {
                            checkBox = (CheckBox) recentView;
                            contacts = (Contacts) checkBox.getTag();
                            checkBox.toggle();
                            contacts.setisChecked(false);
                        }
                        checkBox = (CheckBox) v;
                        contacts = (Contacts) checkBox.getTag();
//                        Toast.makeText(getApplicationContext(),
//                                "Clicked on Checkbox: " + checkBox.getText() +
//                                        " is " + checkBox.isChecked(),
//                                Toast.LENGTH_LONG).show();
                        contacts.setisChecked(checkBox.isChecked());
                        recentView = v;
                        unChecked = false;
                    }
                });

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Contacts contacts = saveList.get(position);
            holder.name.setText(contacts.getName());
            holder.phoneNumber.setText(contacts.getPhoneNum());
            holder.checkBox.setChecked(contacts.getisChecked());
            holder.checkBox.setTag(contacts);
            return convertView;
        }
    }

    private void checkButtonClick() {

        Button add = (Button) findViewById(R.id.btn_add);
        Button delete = (Button) findViewById(R.id.btn_delete);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(unChecked) {
                    Toast.makeText(getApplicationContext(),
                            "삭제할 전화번호를 선택하여 주세요.", Toast.LENGTH_LONG).show();
                }
                else {
                    count = 0;
                    saveList = dataAdapter.saveList;
                    for (int i = 0; i < saveList.size(); i++) {
                        Contacts contacts = saveList.get(i);
                        if (contacts.getisChecked()) {
                            dataAdapter.remove(contacts);
                            saveList.remove(i);
                            count++;
                        }
                    }
                    dataAdapter.notifyDataSetChanged();

                    pref.removeAllPreferences("name");
                    pref.removeAllPreferences("phoneNum");
                    for (int i = 0; i < saveList.size(); i++) {
                        pref.putValue(Integer.toString(i), saveList.get(i).getName(), "name");
                        pref.putValue(Integer.toString(i), saveList.get(i).getPhoneNum(), "phoneNum");
                    }
                    unChecked = true;
                }

            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserFavoriteContacts.this, UserAllContacts.class);
                finish();
                startActivity(intent);
            }

        });
    }

    @Override
    public void onBackPressed() {
        finish();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
