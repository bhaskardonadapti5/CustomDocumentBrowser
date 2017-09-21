package com.KryptosTextApp.KryptosText.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.adapter.PHCGroupSelectedMemberAdapter;
import com.KryptosTextApp.KryptosText.constant.PHCAppConstant;
import com.KryptosTextApp.KryptosText.interfaces.PHCRecyclerClickListener;
import com.KryptosTextApp.KryptosText.model.PHCAddGroupResponseModel;
import com.KryptosTextApp.KryptosText.model.PHCGroupInfoResponseModel;
import com.KryptosTextApp.KryptosText.model.PHCSelectMembersModel;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;
import com.KryptosTextApp.KryptosText.utils.Log;
import com.KryptosTextApp.KryptosText.utils.PHCRecyclerTouchListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * user :  avinashj
 * date : 3/30/2017
 * description :
 */

public class PHCCreateGroupActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity implements com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity.OnImageResult {
    private static final String TAG = "PHCCreateGroupActivity";

    private Context mContext;
    private ImageView mProfileImageView, mEditProfile;
    private EditText mGroupNameEditText, mDescriptionEditText;
    private Button mAddMemberButton, mAddMoreMemberButton;
    private LinearLayout mSelectedMemberLayout;
    private RecyclerView mSelectedMemberRecyclerView;
    private PHCGroupSelectedMemberAdapter mSelectedMemberAdapter;
    private PHCGroupInfoResponseModel responseModel;
    private Toolbar toolbar;

    private String comeFrom;
    private File mImageCaptureFile;
    private File mediaFile;

    public static ArrayList<PHCSelectMembersModel> mSelectMemberList;
    private int mGroupId = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phc_create_group_layout);
        mContext = com.KryptosTextApp.KryptosText.activity.PHCCreateGroupActivity.this;

        mSelectMemberList = new ArrayList<>();
        getView();

        toolBarCreation();
        if (getIntent() != null) {
            comeFrom = getIntent().getStringExtra("comeFrom");
            if (comeFrom.equalsIgnoreCase(PHCAppConstant.GROUP_EDIT)) {
                responseModel = (PHCGroupInfoResponseModel) getIntent().getSerializableExtra("editResponse");
                mGroupId = responseModel.getData().getGroupId();
                setView();
            }
        } /*else {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_gorupone);
            Bitmap mShopImageCircledBitmap = ImageUtils.getCircularBitmapWithWhiteBorder(this, bitmap, 4);
            mProfileImageView.setImageBitmap(mShopImageCircledBitmap);
            mImageCaptureFile = persistImage(bitmap);
            mediaFile = mImageCaptureFile;
        }*/
        setListener();
    }



    private void setView() {
        if (responseModel != null) {
            if (responseModel.getData().getGroupURL() != null && !responseModel.getData().getGroupURL().equalsIgnoreCase("")) {
                Glide.with(mContext).load(responseModel.getData().getGroupURL()).asBitmap().centerCrop().placeholder(R.drawable.img_default_group).into(new BitmapImageViewTarget(mProfileImageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        mProfileImageView.setImageDrawable(circularBitmapDrawable);
                        mImageCaptureFile = persistImage(resource);
                        mediaFile = mImageCaptureFile;
                    }
                });

            }
            mGroupNameEditText.setText(responseModel.getData().getGroupName());
            mDescriptionEditText.setText(responseModel.getData().getGroupDescription());
            for (int i = 0; i < responseModel.getData().getGroupMemberDetail().size(); i++) {
                for (int j = 0; j < responseModel.getData().getGroupMemberDetail().get(i).getGroupTypeMember().size(); j++) {
                    PHCSelectMembersModel selectMembersModel = new PHCSelectMembersModel();
                    selectMembersModel.setUserId(responseModel.getData().getGroupMemberDetail().get(i).getGroupTypeMember().get(j).getUserId());
                    selectMembersModel.setUserName(responseModel.getData().getGroupMemberDetail().get(i).getGroupTypeMember().get(j).getUserName());
                    selectMembersModel.setProfileUrl(responseModel.getData().getGroupMemberDetail().get(i).getGroupTypeMember().get(j).getProfileURL());
                    mSelectMemberList.add(selectMembersModel);
                }
            }

        }
    }

    private void setListener() {
        mEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(mContext);
            }
        });

        mAddMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addMembersIntent = new Intent(mContext, com.KryptosTextApp.KryptosText.activity.PHCSelectMembersForGroupActivity.class);
                addMembersIntent.putExtra("commingFrom", mContext.getResources().getString(R.string.create_group));
                startActivity(addMembersIntent);
            }
        });

        mAddMoreMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addMembersIntent = new Intent(mContext, com.KryptosTextApp.KryptosText.activity.PHCSelectMembersForGroupActivity.class);
                addMembersIntent.putExtra("commingFrom", mContext.getResources().getString(R.string.create_group));
                startActivity(addMembersIntent);
            }
        });

        mSelectedMemberRecyclerView.addOnItemTouchListener(new PHCRecyclerTouchListener(getApplicationContext(), mSelectedMemberRecyclerView, new PHCRecyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
                mSelectMemberList.remove(position);
                mSelectedMemberAdapter.notifyDataSetChanged();
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onLongClick(View view, int position) {

            }

        }));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(com.KryptosTextApp.KryptosText.activity.PHCCreateGroupActivity.this);
                finish();
            }
        });

    }

    private void createGroup() {
        if (isNetworkAvailable()) {
            String progressDialogText;
            if (comeFrom.equalsIgnoreCase(PHCAppConstant.GROUP_EDIT))
                progressDialogText = mContext.getResources().getString(R.string.updating_group);
            else
                progressDialogText = mContext.getResources().getString(R.string.creating_group);
            showProgress(progressDialogText);
            RequestBody groupId;
            RequestBody groupName = RequestBody.create(MediaType.parse("text/plain"), mGroupNameEditText.getText().toString().trim());
            RequestBody userId = RequestBody.create(MediaType.parse("text/plain"), "" + getApplicationData().getUserID());
            RequestBody description = RequestBody.create(MediaType.parse("text/plain"), mDescriptionEditText.getText().toString().trim());
            RequestBody participantId = RequestBody.create(MediaType.parse("text/plain"), getParticipentsId());
            RequestBody isActive = RequestBody.create(MediaType.parse("text/plain"), "true");
            RequestBody groupType = RequestBody.create(MediaType.parse("text/plain"), "2");
            RequestBody fileName = null;
            MultipartBody.Part body = null;
            if (mediaFile != null) {
                fileName = RequestBody.create(MediaType.parse("text/plain"), mediaFile.getName());
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), mediaFile);
                body = MultipartBody.Part.createFormData("GroupURL", mediaFile.getName(), requestFile);
            }
            if (comeFrom.equalsIgnoreCase(PHCAppConstant.GROUP_EDIT)) {
                groupId = RequestBody.create(MediaType.parse("text/plain"), "" + mGroupId);
            } else {
                groupId = null;
            }


            PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
            Call<PHCAddGroupResponseModel> call;
            if (comeFrom.equalsIgnoreCase(PHCAppConstant.GROUP_ADD)) {
                call = apiService.createGroup(getApplicationData().getAuthToken(), groupName,
                        body, fileName, userId, description, participantId, isActive, groupType);
            } else {
                call = apiService.editGroup(getApplicationData().getAuthToken(), groupName,
                        body, fileName, groupId, userId, description, participantId, isActive, groupType);
            }

            call.enqueue(new Callback<PHCAddGroupResponseModel>() {
                @Override
                public void onResponse(Call<PHCAddGroupResponseModel> call, Response<PHCAddGroupResponseModel> response) {
                    Log.d(TAG, " create or edit Group URL " + response.raw().request().url());
                    Log.d(TAG, " create or edit Group Resp " + new Gson().toJson(response.body()));
                    dismissProgress();
                    hideKeyboard(com.KryptosTextApp.KryptosText.activity.PHCCreateGroupActivity.this);
                    PHCAddGroupResponseModel model = response.body();
                    if (model != null && model.getStatusCode() == 1) {

                        String message;
                        Intent i = null;
                        if (comeFrom.equalsIgnoreCase(PHCAppConstant.GROUP_ADD))
                            message = "Group has been created successfully";
                        else {
                            message = "Group has been edited successfully";

                            i = new Intent(mContext, com.KryptosTextApp.KryptosText.activity.MainDrawerActivity.class);
                            i.putExtra("FragmentNo", 4);
                        }
                        showSuccessAlert("<b>" + model.getData().getGroupName() + "</b>" + " " + message, i);
                    } else {

                        Toast.makeText(com.KryptosTextApp.KryptosText.activity.PHCCreateGroupActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<PHCAddGroupResponseModel> call, Throwable t) {
                    dismissProgress();
                    Toast.makeText(com.KryptosTextApp.KryptosText.activity.PHCCreateGroupActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            showNoNetworkAlert();
        }
    }

    private void getView() {
        mEditProfile = (ImageView) findViewById(R.id.iv_edit_profile);
        mProfileImageView = (ImageView) findViewById(R.id.profileImageView);

//        mSelectImage = (ImageView) findViewById(R.id.selctImageView);
        mGroupNameEditText = (EditText) findViewById(R.id.groupNameEditText);
        mDescriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        mAddMemberButton = (Button) findViewById(R.id.addMemberButton);
        mAddMoreMemberButton = (Button) findViewById(R.id.addMoreMemberButton);
        mSelectedMemberLayout = (LinearLayout) findViewById(R.id.selectedMemberLayout);
        mSelectedMemberRecyclerView = (RecyclerView) findViewById(R.id.selectedMemberRecyclerView);

    }

    private void toolBarCreation() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mContext.getResources().getString(R.string.create_group));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void updateToolBar() {
        toolbar.setTitle(mContext.getResources().getString(R.string.edit_group));
    }

    @Override
    protected void onResume() {

        if (mSelectMemberList.size() > 0) {
            if (comeFrom.equalsIgnoreCase(PHCAppConstant.GROUP_EDIT)) {
                updateToolBar();
                removeAdminFromList();
            }
            mAddMemberButton.setVisibility(View.GONE);
            mSelectedMemberLayout.setVisibility(View.VISIBLE);
            mSelectedMemberAdapter = new PHCGroupSelectedMemberAdapter(mContext, mSelectMemberList);

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            mSelectedMemberRecyclerView.setLayoutManager(mLayoutManager);
            mSelectedMemberRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mSelectedMemberRecyclerView.setAdapter(mSelectedMemberAdapter);
        } else {
            mSelectedMemberLayout.setVisibility(View.GONE);
            mAddMemberButton.setVisibility(View.VISIBLE);
        }
        ((com.KryptosTextApp.KryptosText.activity.PHCCreateGroupActivity) mContext).invalidateOptionsMenu();
        super.onResume();
    }

    private void removeAdminFromList() {
        for (int i = 0; i < mSelectMemberList.size(); i++) {
            if (mSelectMemberList.get(i).getUserId() == getApplicationData().getUserID()) {
                mSelectMemberList.remove(i);
            }
        }
    }

    private String getParticipentsId() {
        StringBuilder participentIds = new StringBuilder();
        for (int i = 0; i < mSelectMemberList.size(); i++) {
            participentIds.append("").append(mSelectMemberList.get(i).getUserId());
            if (i < mSelectMemberList.size() - 1) {
                participentIds.append(",");
            }

        }
        participentIds.append(",").append(getApplicationData().getUserID());
        return participentIds.toString();
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
//                if (mediaFile != null) {
                if (!mGroupNameEditText.getText().toString().trim().equalsIgnoreCase("")) {
                    if (!mDescriptionEditText.getText().toString().trim().equalsIgnoreCase("")) {
                        if (mSelectMemberList.size() > 0) {
                            createGroup();
                        } else {
                            Toast.makeText(mContext, "Please select member", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(mContext, "Please enter description", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(mContext, "Please select group name", Toast.LENGTH_SHORT).show();
                }
//                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (mSelectMemberList.size() > 0) {
            menu.findItem(R.id.done).setVisible(true);
        } else {
            menu.findItem(R.id.done).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void getImageInfo(Uri imageUri, Bitmap captureImageBitmap, File captureImageFile) {
        mImageCaptureFile = captureImageFile;
        mediaFile = mImageCaptureFile;
        Glide.clear(mProfileImageView);
        if (captureImageBitmap != null) {
            mProfileImageView.setImageBitmap(captureImageBitmap);
        } else if (imageUri != null)
            mProfileImageView.setImageURI(imageUri);
    }

    @Override
    public void deleteProfile() {
        mProfileImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.img_default_group, null));
        mediaFile = null;
        mImageCaptureFile = null;
    }

}
