package com.KryptosTextApp.KryptosText.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.database.PreferenceManager;
import com.KryptosTextApp.KryptosText.model.GenericRespModel;
import com.KryptosTextApp.KryptosText.model.PHCImageResponseModel;
import com.KryptosTextApp.KryptosText.model.UserProfileDataModel;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;
import com.KryptosTextApp.KryptosText.utils.CommonUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.gson.Gson;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChangeProfileActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity implements View.OnClickListener, com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity.OnImageResult {

    private static final String TAG = "ChangeProfileActivity";


    private Context mContext;


    private static ImageView mIvProfile;
    private ImageView mIvEditProfile;
    private EditText mEtFirstName;
    private EditText mEtLastName;
    private EditText mEtContactNo;
    private EditText mEtAboutUser;
    private Button mBtnUpdate;

    private static Uri mImageCaptureUri;
    private static File mImageCaptureFile;
    private boolean mProfileImageDeleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mContext = this;
        findViews();
        setProfileData();
        listeners();
    }

    private void listeners() {
        mEtFirstName.addTextChangedListener(new TextWatcher() {
            int charPosition;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                charPosition = start;

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                charPosition = start;

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (text.length() != 0) {
                    boolean validStr = text.matches("^[A-Z a-z|-]+$");
                    if (!validStr) {
                        Toast.makeText(mContext, "Only Allows letters and - Symbol", Toast.LENGTH_SHORT).show();
                        mEtFirstName.setText(CommonUtils.removeChar(mEtFirstName.getText().toString(), charPosition));
                        mEtFirstName.setSelection(mEtFirstName.getText().length());
                    } else {
                        mEtFirstName.setError(null);
                    }
                }
            }
        });

        mEtLastName.addTextChangedListener(new TextWatcher() {
            int charPosition;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.e("Before Change", s.toString() + "---Start" + start + "---count" + count + "---after" + after);
                charPosition = start;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.e("Text Change", s.toString() + "---Start" + start + "---count" + count + "---before" + before);
                charPosition = start;
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                Log.e("After Change", text);
                if (text.length() != 0) {
                    boolean validStr = text.matches("^[A-Z a-z|-]+$");

                    if (!validStr) {
                        Toast.makeText(mContext, "Only Allows letters and - Symbol", Toast.LENGTH_SHORT).show();
                        mEtLastName.setText(CommonUtils.removeChar(mEtLastName.getText().toString(), charPosition));
                        mEtLastName.setSelection(mEtLastName.getText().length());
                    } else {
                        mEtLastName.setError(null);
                    }
                }
            }
        });

    }

    private void setProfileData() {
        mEtFirstName.setText(getApplicationData().getFirstName());
        mEtLastName.setText(getApplicationData().getLastName());
        mEtContactNo.setText(getApplicationData().getContactNo());
        mEtAboutUser.setText(getApplicationData().getAboutUser());
        setProfileImage(getApplicationData().getProfileUrl());
    }

    private void setProfileImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.equals("")) {
            Glide.with(getContext()).
                    load(imageUrl).asBitmap()
                    .placeholder(R.drawable.ic_deafult_user)
                    .centerCrop()

                    .into(new BitmapImageViewTarget(mIvProfile) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            mIvProfile.setImageDrawable(circularBitmapDrawable);
//                            mIvProfile.setImageBitmap(resource);
                        }
                    });
        }
    }


    /**
     * Find the Views in the layout<br />
     */
    private void findViews() {
        mIvProfile = (ImageView) findViewById(R.id.iv_profile);
        mIvEditProfile = (ImageView) findViewById(R.id.iv_edit_profile);
        mEtFirstName = (EditText) findViewById(R.id.et_first_name);
        mEtLastName = (EditText) findViewById(R.id.et_last_name);
        mEtContactNo = (EditText) findViewById(R.id.et_contact_no);
        mEtAboutUser = (EditText) findViewById(R.id.et_about_user);
        mBtnUpdate = (Button) findViewById(R.id.btn_update);

        mBtnUpdate.setOnClickListener(this);
        mIvEditProfile.setOnClickListener(this);
    }

    /**
     * Handle button click events<br />
     */
    @Override
    public void onClick(View v) {
        if (v == mBtnUpdate) {
            // Handle clicks for mBtnUpdate
            if (validate()) {
                if (isNetworkAvailable()) {

                    if (mImageCaptureUri != null) {
                        if (mImageCaptureFile != null) {
                            updateProfileImageRetrofit(mImageCaptureFile);
                        }
                    } else {
                        updateProfileRetrofit();
                    }
                } else {
                    showNoNetworkAlert();
                }
            }
        } else if (v == mIvEditProfile) {
            chooseImage(mContext);
        }
    }

    private void updateProfileImageRetrofit(File file) {
        hideKeyboard(this);
        showProgress("Uploading image..");
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);
        RequestBody emailID = RequestBody.create(MediaType.parse("text/plain"), getPrefManager().getEmailID());

        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<PHCImageResponseModel> call = apiService.updateProfileImage(getApplicationData().getAuthToken(), emailID, body);
        call.enqueue(new Callback<PHCImageResponseModel>() {
            @Override
            public void onResponse(Call<PHCImageResponseModel> call, Response<PHCImageResponseModel> response) {
                Log.d(TAG, "updateProfileImageRetrofit URL " + response.raw().request().url());
                Log.d(TAG, "updateProfileImageRetrofit Resp " + new Gson().toJson(response.body()));
                dismissProgress();
                PHCImageResponseModel mPhcImageResponseModel = response.body();
                if (mPhcImageResponseModel == null) {
                    showAlert("Server error..");
                    return;
                }
                if (isSessionInvalid(mPhcImageResponseModel.getStatusCode(),mPhcImageResponseModel.getStatusMessage())) {
                    return;
                }

                if (mPhcImageResponseModel.getStatusCode() == 200) {

                    String imageUrl = mPhcImageResponseModel.getData().getMediaURL();
                    Log.d(TAG, "imageUrl " + imageUrl);
                    mProfileImageDeleted = false;
                    getApplicationData().setProfileUrl(imageUrl);
                    getPrefManager().setUserProfileUrl(PreferenceManager.encrypt(imageUrl));
                    setProfileImage(imageUrl);
                    updateProfileRetrofit();
                } else {
                    showAlert(mPhcImageResponseModel.getStatusMessage());
                }
            }


            @Override
            public void onFailure(Call<PHCImageResponseModel> call, Throwable t) {
                dismissProgress();
                showAlert("Something went wrong at server");
                showToast(t.getMessage());
            }
        });
    }


    private void updateProfileRetrofit() {
        hideKeyboard(this);
        showProgress("Updating profile..");
        UserProfileDataModel model = new UserProfileDataModel(getApplicationData().getEmailID(), mEtFirstName.getText().toString(), mEtLastName.getText().toString(),
                mEtContactNo.getText().toString(), mEtAboutUser.getText().toString());
        if (mProfileImageDeleted) {
            model.setHasProfileImgDelete(true);

        } else {
            model.setHasProfileImgDelete(false);
        }
        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<GenericRespModel> call = apiService.updateUserProfile(getApplicationData().getAuthToken(), model);

        call.enqueue(new Callback<GenericRespModel>() {
            @Override
            public void onResponse(Call<GenericRespModel> call, Response<GenericRespModel> response) {
                dismissProgress();
                GenericRespModel data = response.body();
                if (isSessionInvalid(data.getStatusCode(),data.getStatusMessage())) {
                    return;
                }
                if (data.getStatusCode() == 200) {
                    showSuccessAlert(data.getStatusMessage(), null);
                    saveProfileData();
                } else {
                    showFailAlert(data.getStatusMessage());

                }
            }

            @Override
            public void onFailure(Call<GenericRespModel> call, Throwable t) {
                dismissProgress();
                showAlert(t.getMessage());

            }
        });

    }

    private void saveProfileData() {
        getApplicationData().setFirstName(mEtFirstName.getText().toString());
        getApplicationData().setLastName(mEtLastName.getText().toString());
        getApplicationData().setContactNo(mEtContactNo.getText().toString());
        getApplicationData().setAboutUser(mEtAboutUser.getText().toString());
        if (mProfileImageDeleted) {
            getApplicationData().setProfileUrl("");

        }

    }

    private boolean validate() {
        boolean isValid;
        boolean cancel = false;
        View focusView = null;

        // Check for a valid mobile email.
        if (hasText(mEtFirstName)) {
            focusView = mEtFirstName;
            cancel = true;
        } else if (hasText(mEtLastName)) {
            focusView = mEtLastName;
            cancel = true;
        }
        if (cancel) {
            // form field with an error.
            focusView.requestFocus();
            isValid = false;
        } else {
            isValid = true;
        }
        return isValid;
    }


    @Override
    public void getImageInfo(Uri imageUri, Bitmap captureImageBitmap, File captureImageFile) {
        mImageCaptureUri = imageUri;
        mImageCaptureFile = captureImageFile;
        Glide.clear(mIvProfile);
        if (captureImageBitmap != null) {
            mIvProfile.setImageBitmap(captureImageBitmap);
        } else if (imageUri != null)
            mIvProfile.setImageURI(imageUri);
    }

    @Override
    public void deleteProfile() {
        mIvProfile.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_deafult_user, null));
        mProfileImageDeleted = true;
        mImageCaptureUri = null;
        mImageCaptureFile = null;
    }
}
