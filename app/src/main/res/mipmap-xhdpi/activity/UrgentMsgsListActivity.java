package com.KryptosTextApp.KryptosText.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.adapter.PHCChatListAdapter;
import com.KryptosTextApp.KryptosText.model.PHCChatModel;
import com.KryptosTextApp.KryptosText.model.PHCChatResponseModel;
import com.KryptosTextApp.KryptosText.model.PHCChatSendingData;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;
import com.KryptosTextApp.KryptosText.utils.Log;
import com.google.gson.Gson;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UrgentMsgsListActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity {

    private static final String TAG = "UrgentMsgsListActivity";
    private Context mContext;
    private Toolbar toolbar;
    private RecyclerView recyclerView_Chats;
    private LinearLayout ll_chats;
    private LinearLayout ll_no_records;
    private ImageView iv_retry;
    private TextView mTextError;
    private int mGroupId;
    private ArrayList<PHCChatModel> mChatList;
    private PHCChatListAdapter mChatListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_urgent_msgs_list);
        mContext = com.KryptosTextApp.KryptosText.activity.UrgentMsgsListActivity.this;
        mGroupId = getIntent().getIntExtra("GroupId", -1);
        updateToolbar();
        loadViews();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUrgentMsgs();
    }

    private void loadUrgentMsgs() {
        if (isNetworkAvailable()) {
            getUrgentChats();
        } else {
            showNoNetworkToast();
            ll_no_records.setVisibility(View.VISIBLE);
            mTextError.setText(getResources().getText(R.string.no_internet_retry));
            ll_chats.setVisibility(View.GONE);
        }
    }


    private void setListeners() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                onBackPressed();
                finish();
            }
        });
        iv_retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrgentMsgs();
            }
        });
    }

    private void loadViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView_Chats = (RecyclerView) findViewById(R.id.chatList);
        ll_chats = (LinearLayout) findViewById(R.id.ll_chats);
        ll_no_records = (LinearLayout) findViewById(R.id.ll_no_records);
        iv_retry = (ImageView) findViewById(R.id.iv_retry);
        mTextError = (TextView) findViewById(R.id.textError);
    }

    private void updateToolbar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("Urgent Msgs");
        setSupportActionBar(toolbar);
    }

    private void getUrgentChats() {
        if (isNetworkAvailable()) {
            mChatList = new ArrayList<>();
            showProgress("Getting Chat History...");
            PHCChatSendingData data = new PHCChatSendingData();
            data.setGroupId(mGroupId);
            data.setUserId(getApplicationData().getUserID());
            data.setStartIndex(0);
            data.setEndIndex(0);

            PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
            Call<PHCChatResponseModel> call = apiService.chatData(getApplicationData().getAuthToken(), data);
            call.enqueue(new Callback<PHCChatResponseModel>() {
                @Override
                public void onResponse(Call<PHCChatResponseModel> call, Response<PHCChatResponseModel> response) {
                    Log.d(TAG, " getUrgentChatMessage URL " + response.raw().request().url());
                    Log.d(TAG, " getUrgentChatMessage Resp " + new Gson().toJson(response.body()));
                    PHCChatResponseModel mResponseModel = response.body();
                    if (mResponseModel != null) {
                        dismissProgress();
                        if (!isSessionInvalid(mResponseModel.getStatusCode(),mResponseModel.getStatusMessage())) {
                            return;
                        }
                        if (mResponseModel != null && mResponseModel.getStatusCode() == 1) {

                            if (mResponseModel.getData() != null && mResponseModel.getData().size() > 0) {
                                ll_no_records.setVisibility(View.GONE);
                                ll_chats.setVisibility(View.VISIBLE);
                                mChatList.addAll(mResponseModel.getData());

                                if (mChatListAdapter == null) {
                                    mChatListAdapter = new PHCChatListAdapter(mContext, mChatList);
                                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                                    mLayoutManager.setReverseLayout(true);
                                    recyclerView_Chats.setLayoutManager(mLayoutManager);
                                    recyclerView_Chats.setItemAnimator(new DefaultItemAnimator());
                                    recyclerView_Chats.setAdapter(mChatListAdapter);
                                    recyclerView_Chats.scrollToPosition(0);
//                                        sendDataToSocketAndUpdateList(103, null, null, 0, null, false, 0);
                                } else {
                                    mChatListAdapter.refreshList(mChatList);
                                    mChatListAdapter.notifyDataSetChanged();
                                }

                            } else {
                                /*if (mSwipeRefreshLayout.isRefreshing())
                                    mSwipeRefreshLayout.setRefreshing(false);*/
                                Toast.makeText(mContext, "No data found", Toast.LENGTH_SHORT).show();
                            }
//                            mChatListView.scrollToPosition(0);
                        } else {
                            dismissProgress();
                            Toast.makeText(mContext, "No chat history..", Toast.LENGTH_SHORT).show();
                        }
                    } else
                        Log.e("Chat History Response", "Null");
                }

                @Override
                public void onFailure(Call<PHCChatResponseModel> call, Throwable t) {
                    Log.d(TAG, "onFailure");
                    dismissProgress();
                    t.printStackTrace();
                }
            });
        } else {
            showNoNetworkAlert();
        }
    }

    public void retry(View view) {
    }
}
