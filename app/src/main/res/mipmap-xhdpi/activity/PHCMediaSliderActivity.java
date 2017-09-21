package com.KryptosTextApp.KryptosText.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.adapter.NewPagerAdapter;
import com.KryptosTextApp.KryptosText.model.PHCChatModel;
import com.KryptosTextApp.KryptosText.utils.PHCDateFormatter;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * user :  avinashj
 * date : 2/15/2017
 * description :
 */

public class PHCMediaSliderActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity {
    private Toolbar toolbar;
    private TextView mUserNameText,mDateText;
    private ViewPager mMediaSliderPager;
    private ArrayList<PHCChatModel> mChatModelList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phc_activity_media_slider);

        mChatModelList = (ArrayList<PHCChatModel>) getIntent().getSerializableExtra("ChatModel");
        int mediaPosition = getIntent().getIntExtra("mediaPosition", 0);
        getView();
        toolBarCreation(mediaPosition);

//        PHCChatSliderAdapter mChatSliderAdapter = new PHCChatSliderAdapter(mContext, mChatModelList);
        NewPagerAdapter mChatSliderAdapter = new NewPagerAdapter(getSupportFragmentManager(),mChatModelList);
        mMediaSliderPager.setAdapter(mChatSliderAdapter);
        mMediaSliderPager.setOffscreenPageLimit(1);
        mMediaSliderPager.setCurrentItem(mediaPosition);
//        mMediaSliderPager.setOnPageChangeListener(myOnPageChangeListener);
        setListener();

    }


    private void getView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mDateText = (TextView) toolbar.findViewById(R.id.dateText);
        mUserNameText = (TextView) toolbar.findViewById(R.id.userNameText);
        mMediaSliderPager = (ViewPager) findViewById(R.id.mediaSliderPager);
    }

    private void setListener() {
        mMediaSliderPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                toolBarCreation(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });



    }


    private void toolBarCreation(int position) {
        toolbar.setTitle("");
        mUserNameText.setText(mChatModelList.get(position).getUserName());
        try {
            mDateText.setText(PHCDateFormatter.getChatDateFromserverDate(mChatModelList.get(position).getCreatedDate()));
        } catch (ParseException e) {
            e.printStackTrace();
            mDateText.setText(mChatModelList.get(position).getCreatedDate());

        }
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }





    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onStop() {
        super.onStop();

    }
}
