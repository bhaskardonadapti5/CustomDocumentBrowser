package com.KryptosTextApp.KryptosText.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.SubtitleCollapsingToolbarLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.adapter.GroupInfoAdapter;
import com.KryptosTextApp.KryptosText.constant.PHCAppConstant;
import com.KryptosTextApp.KryptosText.model.GroupInfoItem;
import com.KryptosTextApp.KryptosText.model.PHCGroupInfoMemberDetailModel;
import com.KryptosTextApp.KryptosText.model.PHCGroupInfoResponseModel;
import com.KryptosTextApp.KryptosText.model.PHCGroupInfoTypeMemberModel;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;
import com.KryptosTextApp.KryptosText.utils.DateFormatter;
import com.KryptosTextApp.KryptosText.utils.SimpleDividerItemDecoration;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupInfoActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity implements View.OnClickListener {
    private static final String TAG = "GroupInfoActivity";
    private RecyclerView membersRecyclerView;
    private ImageView profile_image;
    private Toolbar toolbar;
    private Context mContext;
    private int groupId;
    private PHCGroupInfoResponseModel mResponseModel;
    private ImageView iv_edit_profile;
    private SubtitleCollapsingToolbarLayout collapsingToolbarLayout;
    private boolean isEdit = false;
    private ArrayList<GroupInfoItem> membersList, groupsList, tempList;
    private GroupInfoAdapter groupInfoMembersAdapter;
    LinearLayout ll_process, ll_members;
    //private RecyclerView groupsRecyclerView;
    //private GroupInfoAdapter groupInoGroupsAdapter;
//    private TextView tvMembersCount;
//    private TextView tvGroupsCount;
//    private LinearLayout ll_groups;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        mContext = com.KryptosTextApp.KryptosText.activity.GroupInfoActivity.this;
        groupId = getIntent().getIntExtra("GroupId", 0);

        getView();
        toolBarCreation();
        setListener();
        getAllGroupInfoList();
    }

    private void setListener() {
        iv_edit_profile.setOnClickListener(this);
    }

    private void toolBarCreation() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    private void loadTitle(String title, String subTitle) {
        collapsingToolbarLayout.setTitle(title);
        collapsingToolbarLayout.setSubtitle(subTitle);
    }

    private void getView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ll_process = (LinearLayout) findViewById(R.id.ll_process);
        ll_members = (LinearLayout) findViewById(R.id.ll_members);
        iv_edit_profile = (ImageView) findViewById(R.id.iv_edit_profile);
        profile_image = (ImageView) findViewById(R.id.profile_image);
        collapsingToolbarLayout = (SubtitleCollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        membersRecyclerView = (RecyclerView) findViewById(R.id.recycler_members);
//        groupsRecyclerView = (RecyclerView) findViewById(R.id.recycler_groups);
//        ll_groups = (LinearLayout) findViewById(R.id.ll_groups);
        membersList = new ArrayList<>();
        groupsList = new ArrayList<>();
        tempList = new ArrayList<>();
        groupInfoMembersAdapter = new GroupInfoAdapter(mContext, tempList);
//        groupInoGroupsAdapter = new GroupInfoAdapter(mContext, groupsList, true);
//        tvMembersCount = (TextView) findViewById(R.id.tv_members_count);
//        tvGroupsCount = (TextView) findViewById(R.id.tv_groups_count);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setAutoMeasureEnabled(true);
        SimpleDividerItemDecoration simpleDividerItemDecoration = new SimpleDividerItemDecoration(getContext().getResources().getDrawable(R.drawable.line_divider_transperent));
        membersRecyclerView.addItemDecoration(simpleDividerItemDecoration);
        membersRecyclerView.setLayoutManager(mLayoutManager);
        membersRecyclerView.setItemAnimator(new DefaultItemAnimator());
        membersRecyclerView.setAdapter(groupInfoMembersAdapter);


//        groupsRecyclerView.addItemDecoration(simpleDividerItemDecoration);
//        groupsRecyclerView.setLayoutManager(new com.KryptosTextApp.KryptosText.utils.LinearLayoutManager(mContext));
//        groupsRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        groupsRecyclerView.setAdapter(groupInoGroupsAdapter);


//        ListUtils.setDynamicHeight(membersRecyclerView, groupInfoMembersAdapter);
//        ListUtils.setDynamicHeight(groupsRecyclerView, groupInoGroupsAdapter);
    }

    private void editRecords() {
        Intent groupEditIntent = new Intent(mContext, com.KryptosTextApp.KryptosText.activity.PHCCreateGroupActivity.class);
        groupEditIntent.putExtra("comeFrom", PHCAppConstant.GROUP_EDIT);
        groupEditIntent.putExtra("editResponse", mResponseModel);
        startActivity(groupEditIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.action_edit_group:
//                editRecords();
//                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return true;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_edit_profile:
                editRecords();
                break;
        }
    }

    private void getAllGroupInfoList() {
//        showProgress("Getting group info");
        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<PHCGroupInfoResponseModel> call = apiService.groupInfoData(getApplicationData().getAuthToken(), groupId);
        call.enqueue(new Callback<PHCGroupInfoResponseModel>() {
            @Override
            public void onResponse(Call<PHCGroupInfoResponseModel> call, Response<PHCGroupInfoResponseModel> response) {
                dismissProgress();
                Log.d(TAG, " getAllGroupInfoList URL " + response.raw().request().url());
                Log.d(TAG, " getAllGroupInfoList Resp " + new Gson().toJson(response.body()));
                mResponseModel = response.body();
                if (isSessionInvalid(mResponseModel.getStatusCode(),mResponseModel.getStatusMessage())) {
                    return;
                }
                if (mResponseModel != null && mResponseModel.getData() != null && mResponseModel.getStatusCode() == 1) {

                    if (mResponseModel.getData().getGroupMemberDetail() != null && mResponseModel.getData().getGroupMemberDetail().size() > 0) {
                        membersList.clear();
                        groupsList.clear();
                        for (PHCGroupInfoMemberDetailModel phcGroupInfoItem : mResponseModel.getData().getGroupMemberDetail()) {
                            if (phcGroupInfoItem.getGroupTypeId() == 1)
                                for (PHCGroupInfoTypeMemberModel memberModel : phcGroupInfoItem.getGroupTypeMember()) {
                                    membersList.add(new GroupInfoItem(memberModel.getUserName(), memberModel.getProfileURL(), memberModel.getUserId()).setAdmin(memberModel.isAdmin()));
                                }
                            else if (phcGroupInfoItem.getGroupTypeId() == 2)
                                for (PHCGroupInfoTypeMemberModel memberModel : phcGroupInfoItem.getGroupTypeMember()) {
                                    groupsList.add(new GroupInfoItem(memberModel.getGroupName(), memberModel.getGroupURL(), memberModel.getParticipantGroupId()).setIsGroup());
                                }
                        }
                        tempList.add(new GroupInfoItem("Members", membersList.size() + ""));
                        tempList.addAll(membersList);
                        if (groupsList.size() > 0) {
                            tempList.add(new GroupInfoItem("Groups", groupsList.size() + ""));
                            tempList.addAll(groupsList);
                        }
                        //tvMembersCount.setText(String.format("%s", membersList.size()));
                        //tvGroupsCount.setText(String.format("%s", groupsList.size()));
                        groupInfoMembersAdapter.notifyDataSetChanged();

//                        ll_groups.setVisibility(groupsList.size() > 0 ? View.VISIBLE : View.GONE);
                    } else {
                        Toast.makeText(mContext, "No Participants Found", Toast.LENGTH_SHORT).show();
                    }
                    setGroupInfo();
                } else {
                    dismissProgress();
                    Toast.makeText(mContext, "No Groups found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PHCGroupInfoResponseModel> call, Throwable t) {
                dismissProgress();
                Toast.makeText(mContext, "No Groups found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setGroupInfo() {
        String groupName;
        switch (mResponseModel.getData().getGroupTypeId()) {
            case 1:
            case 2:
                groupName = mResponseModel.getData().getGroupName();
                break;
            case 3:
                groupName = mResponseModel.getData().getGroupAdminName();
                break;
            default:
                groupName = "";
        }
        loadTitle(groupName, "Created on " + DateFormatter.getProperDateFromServerDate(mResponseModel.getData().getCreatedDate()));
        isEdit = isAdmin(mResponseModel);
        changeGroupEditVisiblity();
        String profileImageUrl = setProfileImage(mResponseModel);

        if (profileImageUrl != null && profileImageUrl.trim().length() != 0) {
            Glide.with(mContext).load(profileImageUrl).thumbnail(0.5f).crossFade().placeholder(R.drawable.groups_img).diskCacheStrategy(DiskCacheStrategy.ALL).into(profile_image);
        } else {
            profile_image.setImageResource(R.drawable.groups_img);
        }
    }

    private boolean isAdmin(PHCGroupInfoResponseModel mResponseModel) {
        boolean isAdminPrivilege = false;
        if (mResponseModel.getData().getGroupTypeId() == 2) {
            for (GroupInfoItem groupInfoItem : membersList) {
                if (groupInfoItem.getMemberUserId() == getApplicationData().getUserID() && groupInfoItem.isAdmin())
                    return true;
            }
        }
        return isAdminPrivilege;
    }

    private String setProfileImage(PHCGroupInfoResponseModel mResponseModel) {
        switch (mResponseModel.getData().getGroupTypeId()) {
            case 2:
                return mResponseModel.getData().getGroupURL();
            default:
                if (groupsList.size() == 0 && membersList.size() == 2) {
                    for (GroupInfoItem groupInfoItem : membersList) {
                        if (groupInfoItem.getMemberUserId() != getApplicationData().getUserID()) {
                            return groupInfoItem.getMemberProfileUrl();
                        }
                    }
                }
                break;
        }
        return null;
    }

    public void dismissProgress() {
        ll_process.setVisibility(View.GONE);
        ll_members.setVisibility(View.VISIBLE);
        super.dismissProgress();
    }

    private void changeGroupEditVisiblity() {
        iv_edit_profile.setVisibility(isEdit ? View.VISIBLE : View.GONE);
    }
}
