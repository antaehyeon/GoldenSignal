package ensharp.goldensignal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
    //MyCustomAdapter dataAdapter = null;
    ListView listView;
    View recentView;
    boolean unChecked;
    TextView personName;
    ImageView personFace;
    Bitmap personImage;

    RelativeLayout imageLayout1;
    RelativeLayout imageLayout2;
    RelativeLayout imageLayout3;
    RelativeLayout pushLayout1;
    RelativeLayout pushLayout2;
    RelativeLayout pushLayout3;
    ImageView image;
    Bitmap bitmapImage;
    boolean[] clickable;
    int pushedIndex = 3;

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

        clickable = new boolean[3];
        imageLayout1 = (RelativeLayout) findViewById(R.id.image_layout1);
        imageLayout2 = (RelativeLayout) findViewById(R.id.image_layout2);
        imageLayout3 = (RelativeLayout) findViewById(R.id.image_layout3);
        pushLayout1 = (RelativeLayout) findViewById(R.id.push_layout1);
        pushLayout2 = (RelativeLayout) findViewById(R.id.push_layout2);
        pushLayout3 = (RelativeLayout) findViewById(R.id.push_layout3);

        imageSetting();
        imageButtonClick();
        checkButtonClick();
    }

    public boolean checkMyTeam(String name, int index) {

        if (name.contains("안태현")) {
            personImage = resizeImage("taehyun", 320, 320);
            personFace.setImageBitmap(getCircleBitmap(personImage));
            personName.setText("안태현");
            clickable[index] = true;
            return true;
        } else if (name.contains("성민경")) {
            personImage = resizeImage("minkyung", 320, 320);
            personFace.setImageBitmap(getCircleBitmap(personImage));
            personName.setText("성민경");
            clickable[index] = true;
            return true;
        } else if (name.contains("윤명식")) {
            personImage = resizeImage("myungsik", 320, 320);
            personFace.setImageBitmap(getCircleBitmap(personImage));
            personName.setText("윤명식");
            clickable[index] = true;
            return true;
        } else
            return false;
    }

    public void matchIndex(int index) {

        if (index == 0) {
            personFace = (ImageView) findViewById(R.id.person_image1);
            personName = (TextView) findViewById(R.id.person_nametxt1);
        } else if (index == 1) {
            personFace = (ImageView) findViewById(R.id.person_image2);
            personName = (TextView) findViewById(R.id.person_nametxt2);
        } else if (index == 2) {
            personFace = (ImageView) findViewById(R.id.person_image3);
            personName = (TextView) findViewById(R.id.person_nametxt3);
        }
    }

    public void imageSetting() {

        int i;

        for (i = 0; i < saveList.size(); i++) {
            matchIndex(i);
            if (!checkMyTeam(saveList.get(i).getName(), i)) {
                personImage = resizeImage("default_person_image", 220, 220);
                personFace.setImageBitmap(personImage);
                personName.setText(saveList.get(i).getName());
                clickable[i] = true;
            }
        }
        for (int j = i; j < 3; j++) {
            matchIndex(j);
            personImage = resizeImage("none_select", 220, 220);
            //personImage = BitmapFactory.decodeResource(getResources(), R.drawable.none_select);
            personFace.setImageBitmap(personImage);
            personName.setText("-");
            clickable[j] = false;
            if (j == 0) {
                imageLayout1.setBackgroundResource(R.drawable.circle_black_style);
            } else if (j == 1) {
                imageLayout2.setBackgroundResource(R.drawable.circle_black_style);
            } else if (j == 2) {
                imageLayout3.setBackgroundResource(R.drawable.circle_black_style);
            }
        }
    }

    public Bitmap resizeImage(String iconName, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }


    private void imageButtonClick() {

        if (clickable[0]) {
            imageLayout1.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    image = (ImageView) findViewById(R.id.check_image1);
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            pushLayout2.setVisibility(View.INVISIBLE);
                            pushLayout3.setVisibility(View.INVISIBLE);
                            pushLayout1.setBackgroundResource(R.drawable.circle_push_style);
                            pushLayout1.setVisibility(View.VISIBLE);
                            image.setImageResource(0);
                            break;
                        }
                        case MotionEvent.ACTION_MOVE: {
                            break;
                        }
                        case MotionEvent.ACTION_OUTSIDE: {
                            pushLayout1.setVisibility(View.INVISIBLE);
                            break;
                        }
                        case MotionEvent.ACTION_CANCEL: {
                            pushLayout1.setVisibility(View.INVISIBLE);
                            break;
                        }
                        case MotionEvent.ACTION_UP: {
                            pushedIndex = 0;
                            pushLayout1.setBackgroundResource(R.drawable.check_style);
                            pushLayout1.setVisibility(View.VISIBLE);
                            bitmapImage = BitmapFactory.decodeResource(getResources(), R.drawable.check_sign_icon_red);
                            image.setImageBitmap(bitmapImage);
                            break;
                        }
                    }
                    return true;
                }
            });
            pushLayout1.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            pushLayout1.setBackgroundResource(R.drawable.circle_push_style);
                            break;
                        }
                        case MotionEvent.ACTION_OUTSIDE: {
                            break;
                        }
                        case MotionEvent.ACTION_UP: {
                            pushedIndex = 3;
                            pushLayout1.setVisibility(View.INVISIBLE);
                            break;
                        }
                    }
                    return true;
                }
            });
        }
        if (clickable[1]) {
            imageLayout2.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    image = (ImageView) findViewById(R.id.check_image2);
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            pushLayout1.setVisibility(View.INVISIBLE);
                            pushLayout3.setVisibility(View.INVISIBLE);
                            pushLayout2.setBackgroundResource(R.drawable.circle_push_style);
                            pushLayout2.setVisibility(View.VISIBLE);
                            image.setImageResource(0);
                            break;
                        }
                        case MotionEvent.ACTION_MOVE: {
                            break;
                        }
                        case MotionEvent.ACTION_OUTSIDE: {
                            pushLayout2.setVisibility(View.INVISIBLE);
                            break;
                        }
                        case MotionEvent.ACTION_CANCEL: {
                            pushLayout2.setVisibility(View.INVISIBLE);
                            break;
                        }
                        case MotionEvent.ACTION_UP: {
                            pushedIndex = 1;
                            pushLayout2.setBackgroundResource(R.drawable.check_style);
                            pushLayout2.setVisibility(View.VISIBLE);
                            bitmapImage = BitmapFactory.decodeResource(getResources(), R.drawable.check_sign_icon_red);
                            image.setImageBitmap(bitmapImage);
                            break;
                        }
                    }
                    return true;
                }
            });
            pushLayout2.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            pushLayout2.setBackgroundResource(R.drawable.circle_push_style);
                            break;
                        }
                        case MotionEvent.ACTION_OUTSIDE: {
                            break;
                        }
                        case MotionEvent.ACTION_UP: {
                            pushedIndex = 3;
                            pushLayout2.setVisibility(View.INVISIBLE);
                            break;
                        }
                    }
                    return true;
                }
            });
        }
        if (clickable[2]) {
            imageLayout3.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    image = (ImageView) findViewById(R.id.check_image3);
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            pushLayout1.setVisibility(View.INVISIBLE);
                            pushLayout2.setVisibility(View.INVISIBLE);
                            pushLayout3.setBackgroundResource(R.drawable.circle_push_style);
                            pushLayout3.setVisibility(View.VISIBLE);
                            image.setImageResource(0);
                            break;
                        }
                        case MotionEvent.ACTION_MOVE: {
                            break;
                        }
                        case MotionEvent.ACTION_OUTSIDE: {
                            pushLayout3.setVisibility(View.INVISIBLE);
                            break;
                        }
                        case MotionEvent.ACTION_CANCEL: {
                            pushLayout3.setVisibility(View.INVISIBLE);
                            break;
                        }
                        case MotionEvent.ACTION_UP: {
                            pushedIndex = 2;
                            pushLayout3.setBackgroundResource(R.drawable.check_style);
                            pushLayout3.setVisibility(View.VISIBLE);
                            bitmapImage = BitmapFactory.decodeResource(getResources(), R.drawable.check_sign_icon_red);
                            image.setImageBitmap(bitmapImage);
                            break;
                        }
                    }
                    return true;
                }
            });
            pushLayout3.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            pushLayout3.setBackgroundResource(R.drawable.circle_push_style);
                            break;
                        }
                        case MotionEvent.ACTION_OUTSIDE: {
                            break;
                        }
                        case MotionEvent.ACTION_UP: {
                            pushedIndex = 3;
                            pushLayout3.setVisibility(View.INVISIBLE);
                            break;
                        }
                    }
                    return true;
                }
            });
        }
    }

    private void delete(int index) {

        matchIndex(index);
        personImage = resizeImage("none_select", 220, 220);
        personFace.setImageBitmap(personImage);
        personName.setText("-");
        clickable[index] = false;
        if (index == 0) {
            pushLayout1.setVisibility(View.INVISIBLE);
            imageLayout1.setOnTouchListener(null);
            imageLayout1.setBackgroundResource(R.drawable.circle_black_style);
        } else if (index == 1) {
            pushLayout2.setVisibility(View.INVISIBLE);
            imageLayout2.setOnTouchListener(null);
            imageLayout2.setBackgroundResource(R.drawable.circle_black_style);
        } else if (index == 2) {
            pushLayout3.setVisibility(View.INVISIBLE);
            imageLayout3.setOnTouchListener(null);
            imageLayout3.setBackgroundResource(R.drawable.circle_black_style);
        }
    }

    private void checkButtonClick() {

        Button add = (Button) findViewById(R.id.btn_add);
        final Button delete = (Button) findViewById(R.id.btn_delete);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pushedIndex == 3) {
                    Toast.makeText(getApplicationContext(),
                            "삭제할 전화번호를 선택하여 주세요.", Toast.LENGTH_LONG).show();
                } else {

                    delete(pushedIndex);
                    saveList.remove(pushedIndex);
                    pref.removeAllPreferences("name");
                    pref.removeAllPreferences("phoneNum");
                    for (int i = 0; i < saveList.size(); i++) {
                        pref.putValue(Integer.toString(i), saveList.get(i).getName(), "name");
                        pref.putValue(Integer.toString(i), saveList.get(i).getPhoneNum(), "phoneNum");
                    }
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
