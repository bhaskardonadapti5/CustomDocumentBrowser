package com.KryptosTextApp.KryptosText.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.KryptosTextApp.KryptosText.R;

public class NewFingerprintAuthenticationActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity implements View.OnClickListener {


    private Button mBtnForgotMpin;
    ImageView iv_mpin_1, iv_mpin_2, iv_mpin_3, iv_mpin_4;
    GridView gridViewKeypad;
    String[] keys = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", new String(new int[]{0x232b}, 0, 1), "0", new String(new int[]{0x2713}, 0, 1)};
    String mpin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_fingerprint_authentication);
        iv_mpin_1 = (ImageView) findViewById(R.id.iv_mpin_1);
        iv_mpin_2 = (ImageView) findViewById(R.id.iv_mpin_2);
        iv_mpin_3 = (ImageView) findViewById(R.id.iv_mpin_3);
        iv_mpin_4 = (ImageView) findViewById(R.id.iv_mpin_4);

        gridViewKeypad = (GridView) findViewById(R.id.grid_keypad);
        gridViewKeypad.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return keys.length;
            }

            @Override
            public String getItem(int position) {
                return keys[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            class Holder {
                TextView tv_title;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.item_keypad_grid, null);
                Holder holder = new Holder();
                holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                holder.tv_title.setText(keys[position]);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (position) {
                            case 9:
                                if (mpin.length() > 0) {
                                    mpin = mpin.substring(0, mpin.length() - 1);
                                }
                                changeMPinBackground(false);

                                break;
                            case 11:
                                if (mpin.length() == 4)
                                    loginMPin();
                                break;
                            default:
                                if (mpin.trim().length() < 4) {
                                    mpin = mpin + keys[position];
                                    changeMPinBackground(true);
                                } else if (mpin.trim().length() == 4)
                                    loginMPin();
                                break;
                        }
                    }
                });
                return convertView;
            }
        });
    }

    private void changeMPinBackground(boolean isAdd) {

        if (isAdd) {
            Drawable drawable = getResources().getDrawable(R.drawable.custom_mpin_et_bg_fill);
            switch (mpin.trim().length()) {
                case 1:
                    iv_mpin_1.setImageDrawable(drawable);
                    break;
                case 2:
                    iv_mpin_2.setImageDrawable(drawable);
                    break;
                case 3:
                    iv_mpin_3.setImageDrawable(drawable);
                    break;
                case 4:
                    iv_mpin_4.setImageDrawable(drawable);
                    break;
            }
        } else {
            Drawable drawable = getResources().getDrawable(R.drawable.custom_mpin_edit_text_bg);
            switch (mpin.trim().length()) {
                case 0:
                    iv_mpin_1.setImageDrawable(drawable);
                    iv_mpin_2.setImageDrawable(drawable);
                    iv_mpin_3.setImageDrawable(drawable);
                    iv_mpin_4.setImageDrawable(drawable);
                    break;
                case 1:
                    iv_mpin_2.setImageDrawable(drawable);
                    iv_mpin_3.setImageDrawable(drawable);
                    iv_mpin_4.setImageDrawable(drawable);
                    break;
                case 2:
                    iv_mpin_3.setImageDrawable(drawable);
                    iv_mpin_4.setImageDrawable(drawable);
                    break;
                case 3:
                    iv_mpin_4.setImageDrawable(drawable);
                    break;

            }
        }

    }

    private void loginMPin() {
        Toast.makeText(this, "login" + mpin, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {

    }
}
