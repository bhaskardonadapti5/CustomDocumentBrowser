package com.KryptosTextApp.KryptosText.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.adapter.PHCSelectMemberAdapter;
import com.KryptosTextApp.KryptosText.interfaces.ListShowingHidingListener;
import com.KryptosTextApp.KryptosText.model.PHCJsonResponseContactDetailModel;
import com.KryptosTextApp.KryptosText.model.PHCSelectMembersModel;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;
import com.KryptosTextApp.KryptosText.utils.Log;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * user :  avinashj
 * date : 4/3/2017
 * description :
 */

public class PHCSelectMembersForGroupActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity {
    private Context mContext;
    private SearchView mSelectMemberSearchView;
    private ExpandableListView mContactExpandableList;
    private TextView mTextError;
    private PHCSelectMemberAdapter mAdapter;
    private String commingFrom;
    private LinearLayout llNoRecords;
    private ImageView iv_retry;
    private ArrayList<PHCSelectMembersModel> mSelectMemberList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phc_select_members_for_group_layout);
        mContext = com.KryptosTextApp.KryptosText.activity.PHCSelectMembersForGroupActivity.this;
        commingFrom = getIntent().getStringExtra("commingFrom");
        getView();
        toolBarCreation();
        setListener();
    }

    private void getView() {
        mContactExpandableList = (ExpandableListView) findViewById(R.id.contactList);
        mTextError = (TextView) findViewById(R.id.textError);
        mSelectMemberSearchView = (SearchView) findViewById(R.id.selectMemberSearchView);
        llNoRecords = (LinearLayout) findViewById(R.id.ll_no_records);
        iv_retry = (ImageView) findViewById(R.id.iv_retry);
    }

    private void toolBarCreation() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mContext.getResources().getString(R.string.select_members));
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setListener() {

        mSelectMemberSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.filter(newText);
                return false;
            }
        });

        iv_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()){
                    getAllMemberData();
                }else {
                    showNoNetworkToast();
                }
            }
        });

    }

    private void getAllMemberData() {
        showProgress();
        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<PHCJsonResponseContactDetailModel> call = apiService.contactData(getApplicationData().getAuthToken(),getApplicationData().getUserID());
        call.enqueue(new Callback<PHCJsonResponseContactDetailModel>() {
            @Override
            public void onResponse(Call<PHCJsonResponseContactDetailModel> call, Response<PHCJsonResponseContactDetailModel> response) {
                PHCJsonResponseContactDetailModel mContactResponseModel = response.body();
                if(mContactResponseModel != null && mContactResponseModel.getStatusCode() == 1) {
                    dismissProgress();
                    mTextError.setVisibility(View.GONE);
                    mAdapter = new PHCSelectMemberAdapter(mContext, mContactResponseModel.getData(),commingFrom, new ListShowingHidingListener() {
                        @Override
                        public void listHideAndShow(boolean isData) {
                            if (isData) {
                                llNoRecords.setVisibility(View.GONE);
                                mContactExpandableList.setVisibility(View.VISIBLE);
                                listUpdate();
                            } else {
                                llNoRecords.setVisibility(View.VISIBLE);
                                mContactExpandableList.setVisibility(View.GONE);
                            }
                        }
                    });
                    mContactExpandableList.setAdapter(mAdapter);
                    try {
                        for(int i=0; i < mAdapter.getGroupCount(); i++) {
                            mContactExpandableList.expandGroup(i);
                        }
                    }catch (Exception e){Log.e("Exception in Expand",""+e);}
                } else {
                    dismissProgress();
                    llNoRecords.setVisibility(View.VISIBLE);
                    mContactExpandableList.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<PHCJsonResponseContactDetailModel> call, Throwable t) {
                dismissProgress();
                llNoRecords.setVisibility(View.VISIBLE);
                mContactExpandableList.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.create_group_menu, menu);
        Drawable drawable = menu.findItem(R.id.done).getIcon();

        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.colorWhite));
        menu.findItem(R.id.done).setIcon(drawable);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done:
                if(commingFrom.equalsIgnoreCase(mContext.getString(R.string.create_group)))
                    PHCCreateGroupActivity.mSelectMemberList = mAdapter.getSelectedMember();
                else
                    mSelectMemberList = mAdapter.getSelectedMember();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void listUpdate() {
        try {
            for(int i=0; i < mAdapter.getGroupCount(); i++) {
                mContactExpandableList.expandGroup(i);
            }
        }catch (Exception e){
            Log.e("Exception in Expand",""+e);}
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isNetworkAvailable()){
            getAllMemberData();
        }else {
            showNoNetworkToast();
            llNoRecords.setVisibility(View.VISIBLE);
            mTextError.setText(getResources().getText(R.string.no_internet_retry));
            mContactExpandableList.setVisibility(View.GONE);
        }
        listUpdate();

    }
}
