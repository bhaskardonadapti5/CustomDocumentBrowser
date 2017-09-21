package com.KryptosTextApp.KryptosText.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.KryptosTextApp.KryptosText.R;

public class SettingsActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity {

    private ListView mListView;
    private ListView mListViewGeneralInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViews();

        String[] profileItems = getResources().getStringArray(R.array.settings_profile_items);
        ArrayAdapter<String> adp= new ArrayAdapter<>(getBaseContext(),
                android.R.layout.simple_list_item_1, profileItems);
        adp.setDropDownViewResource(android.R.layout.simple_list_item_1);
        mListView.setAdapter(adp);
//        ListUtils.setDynamicHeight(mListView);

        String[] generalItems = getResources().getStringArray(R.array.settings_general_items);
        ArrayAdapter<String> generalItemsAdapter= new ArrayAdapter<>(getBaseContext(),
                android.R.layout.simple_list_item_1, generalItems);
//        adp.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mListViewGeneralInfo.setAdapter(generalItemsAdapter);
//        ListUtils.setDynamicHeight(mListViewGeneralInfo);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        launchActivity(new Intent(getContext(),ChangeProfileActivity.class));
                        break;
                    case 1:
                        launchActivity(new Intent(getContext(),ChangePasswordActivity.class));
                        break;
                    case 2:
                        launchActivity(new Intent(getContext(),ChangeMPinActivity.class));
                        break;
                }
            }
        });

        mListViewGeneralInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
//                        launchActivity(new Intent(getContext(),ChangeProfileActivity.class));
//                        Uri uriUrl = Uri.parse("http://androidbook.blogspot.com/");
//                        Uri uriUrl = Uri.parse(PHCAppConstant.PRIVACY_URL);
                        Intent launchBrowser = new Intent(com.KryptosTextApp.KryptosText.activity.SettingsActivity.this, TermsPrivacyWebViewActivity.class);
                        launchBrowser.putExtra("IsPrivacy",true);
                        startActivity(launchBrowser);

                        break;
                    case 1:
                       /* Uri uriUrl2 = Uri.parse(PHCAppConstant.TERMS_URL);
                        Intent launchBrowser2 = new Intent(Intent.ACTION_VIEW, uriUrl2);*/
                        startActivity(new Intent(com.KryptosTextApp.KryptosText.activity.SettingsActivity.this,TermsPrivacyWebViewActivity.class));
                        break;
                    case 2:
//                        launchActivity(new Intent(getContext(),ChangeProfileActivity.class));
                    /*    Uri uriUserManual = Uri.parse("http://www.sumline.ventures/usermanual/home.html");
                        Intent userManual = new Intent(Intent.ACTION_VIEW, uriUserManual);*/
                        Intent userManual = new Intent(getContext(), com.KryptosTextApp.KryptosText.activity.UserManualActivity.class);
                        startActivity(userManual);


                        break;
                }
            }
        });
    }

    private void findViews() {
        mListView = (ListView) findViewById(R.id.lv_settings);
        mListViewGeneralInfo = (ListView) findViewById(R.id.lv_settings_general_info);
    }

    private void launchActivity(Intent intent){
        startActivity(intent);

    }
}
