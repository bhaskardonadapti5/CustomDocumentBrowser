package com.KryptosTextApp.KryptosText.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.fragment.DriveDocsFragment;
import com.KryptosTextApp.KryptosText.model.DriveItem;
import com.KryptosTextApp.KryptosText.model.DriveItemDataModel;
import com.KryptosTextApp.KryptosText.model.DriveItemsRespModel;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;
import com.KryptosTextApp.KryptosText.utils.Log;
import com.google.gson.Gson;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriveDocsTabbedActivity extends BaseAppCompatActivity {

    private static final String TAG = "DriveDocsTabbedActivity";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private ArrayList<DriveItem> mDriveItemsOwned;
    private ArrayList<DriveItem> mDriveItemsReceived;

    public static boolean isChatActivity = false;
    private LinearLayout ll_process;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_media_tabbed);
        ll_process = (LinearLayout) findViewById(R.id.ll_process);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        isChatActivity = getIntent().getBooleanExtra("isChatActivity", false);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
//        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        if (isNetworkAvailable()) {
            getDocsDriveItemsRetrofit();

        } else {
            showNoNetworkAlert();
        }

    }


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_drive_media_tabbed, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getDocsDriveItemsRetrofit() {
        {
//            showProgress();
//        String header = "bearer "+ mSharedPreference.getString(getActivity(),"UserToken");
            PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
            Call<DriveItemsRespModel> call = apiService.getDriveItems(getApplicationData().getAuthToken(), getApplicationData().getEmailID(), "DOCUMENTS");

            call.enqueue(new Callback<DriveItemsRespModel>() {
                @Override
                public void onResponse(Call<DriveItemsRespModel> call, Response<DriveItemsRespModel> response) {
                    Log.d(TAG, "getDocsDriveItemsRetrofit URL " + response.raw().request().url());
                    Log.d(TAG, "getDocsDriveItemsRetrofit " + new Gson().toJson(response.body()));
                    ll_process.setVisibility(View.GONE);
                    dismissProgress();

                    DriveItemsRespModel mHomeResponseModel = response.body();
                    if (mHomeResponseModel == null) {
                        showAlert(getResources().getString(R.string.somethig_went_wrong));
                        return;
                    }
                    if (isSessionInvalid(mHomeResponseModel.getStatusCode(),mHomeResponseModel.getStatusMessage())) {
                        return;
                    }
                    if (mHomeResponseModel.getStatusCode() == 200) {

//                        mNoGroupFoundText.setVisibility(View.GONE);
                        DriveItemDataModel driveDataModel = mHomeResponseModel.getData();
                        if (driveDataModel != null) {
                            mDriveItemsOwned = driveDataModel.getOwned();
                            mDriveItemsReceived = driveDataModel.getReceived();
                        } else {
                            showToast("No Drive data found");
                        }
                        mViewPager.setAdapter(mSectionsPagerAdapter);
//
                    } else {
                        mViewPager.setAdapter(mSectionsPagerAdapter);

                        showAlert(mHomeResponseModel.getStatusMessage());

                    }
                }

                @Override
                public void onFailure(Call<DriveItemsRespModel> call, Throwable t) {
                    ll_process.setVisibility(View.GONE);
                    dismissProgress();
                    showAlert(t.getMessage());
                }
            });
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return DriveDocsFragment.newInstance("Owned", mDriveItemsOwned);
                case 1:
                    return DriveDocsFragment.newInstance("Received", mDriveItemsReceived);
                /*case 2:
                    TabFragment3 tab3 = new TabFragment3();
                    return tab3;*/
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Owned";
                case 1:
                    return "Received";
              /*  case 2:
                    return "SECTION 3";*/
            }
            return null;
        }
    }


}