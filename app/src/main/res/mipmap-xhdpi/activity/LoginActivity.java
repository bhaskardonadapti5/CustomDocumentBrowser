package com.KryptosTextApp.KryptosText.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.database.PreferenceManager;
import com.KryptosTextApp.KryptosText.model.PHCLoginDataModel;
import com.KryptosTextApp.KryptosText.model.PHCLoginResponseData;
import com.KryptosTextApp.KryptosText.model.PHCLoginResponseModel;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;
import com.KryptosTextApp.KryptosText.utils.Log;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private Context mContext;
    private EditText mEtEmail;
    private EditText mEtPassword;
    private Button mBtnSignIn;
    private Button mBtnForgotPass;
    private String mEmailIdFromForgotPass;
    private boolean mIsFromForgotPassScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext = this;
        mEmailIdFromForgotPass = getIntent().getStringExtra("KEY_EMAIL_ID");
        mIsFromForgotPassScreen = getIntent().getBooleanExtra("IsFromForgotPass", false);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        findViews();
        if (getPrefManager().getDeviceToken().equals("")) {
            generateNewFCMDeviceToken();
        }
        /* try {
            FirebaseInstanceId.getInstance().deleteInstanceId();
             String token = FirebaseInstanceId.getInstance().getToken();
             Log.d(TAG,"TOKEN "+token);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    /**
     * Find the Views in the layout<br />
     */
    private void findViews() {
        mEtEmail = (EditText) findViewById(R.id.et_email);
        mEtPassword = (EditText) findViewById(R.id.et_password);
        mBtnSignIn = (Button) findViewById(R.id.btn_sign_in);
        mBtnForgotPass = (Button) findViewById(R.id.btn_forgot_pass);
        TextInputLayout mTextInputLayoutPassword = (TextInputLayout) findViewById(R.id.til_password);

        mBtnSignIn.setOnClickListener(this);
        mBtnForgotPass.setOnClickListener(this);
        //To-Do remove after testing

        if (mIsFromForgotPassScreen) {
            mEtEmail.setText(mEmailIdFromForgotPass);
            mTextInputLayoutPassword.setHint("Enter OTP");
            mEtPassword.setText("");
            mEtPassword.requestFocus();
        } else {
          /*  mEtEmail.setText("nkommuri@phsi.us");
            mEtPassword.setText("Pass123$");*/
//            mEtEmail.setText("kondal.rao58@gmail.com");
//            mEtPassword.setText("Pass123!");
           // mEtEmail.setText("nkommuri@phsi.us");
            //mEtPassword.setText("Pass1234!");
          /*  mEtEmail.setText("avinashj@chetu.com");
            mEtPassword.setText("Chetu@123#");*/
//            mEtEmail.setText("sohan1@chetu.com");
//            mEtPassword.setText("Chetu@123#");
        }

/*        mEtEmail.setText("sohan5@chetu.com");
        mEtPassword.setText("Chetu@123");*/

//        mEtEmail.setText("sohan2@chetu.com");
//        mEtPassword.setText("Chetu@123#");


    }

    /**
     * Handle button click events<br />
     */
    @Override
    public void onClick(View v) {
        if (v == mBtnSignIn) {
            if (validate()) {
                if (isNetworkAvailable()) {
                    showProgress();
                    loginRetroFit();

                } else {
                    showNoNetworkAlert();
                }
            }
        } else if (v == mBtnForgotPass) {
            // Handle clicks for mBtnForgotPass
            startActivity(new Intent(this, ForgotPasswordActivity.class));
//            startActivity(new Intent(this, InfoActivity.class));
        }
    }

    private void loginRetroFit() {
        try {
            PHCLoginDataModel model = new PHCLoginDataModel();
            model.setEmailId(mEtEmail.getText().toString());
//            model.setPassword(getPrefManager().encrypt(mEtPassword.getText().toString()).trim());
            model.setPassword(mEtPassword.getText().toString().trim());
            model.setPNDeviceToken(getPrefManager().getDeviceToken());

            model.setAppVersion(getAppVersion());
            /*byte[] data = mEtPassword.getText().toString().getBytes("UTF-8");
            String passwordEncodedBASE64 = Base64.encodeToString(data, Base64.DEFAULT);
            Log.d(TAG,"encrypted password AES - "+aesHelper.encrypt(mEtPassword.getText().toString(), PHCAppConstant.AES_KEY, PHCAppConstant.AES_IV).trim());
            Log.d(TAG,"passwordEncodedBASE64 -"+passwordEncodedBASE64);*/

//            model.setPassword();
            PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
            Call<PHCLoginResponseModel> call = apiService.logIn(model);
            call.enqueue(new Callback<PHCLoginResponseModel>() {
                @Override
                public void onResponse(Call<PHCLoginResponseModel> call, Response<PHCLoginResponseModel> response) {
                    Log.d(TAG, " loginRetroFit URL " + response.raw().request().url());
                    Log.d(TAG, " loginRetroFit Resp " + new Gson().toJson(response.body()));
                    Log.d(TAG, " Device token ID " + getPrefManager().getDeviceToken());

                    dismissProgress();
                    PHCLoginResponseModel loginResponseModel = response.body();
                    if (loginResponseModel != null) {
                        if (loginResponseModel.getStatusCode() == 200) {
                            final PHCLoginResponseData data = loginResponseModel.getData();
                            //data.setPk(loginResponseModel.getPk());
                            if (isAppUpdateAvailable(data)) {
                                if (data.isAndroidForceUpgrade()) {
                                    showAppUpgradeAlert();
                                    return;
                                } else {
                                    showToast(getString(R.string.app_recom_upgrade_alert_msg));
                                }
                            }
//                            showToast(loginResponseModel.getStatusMessage());
                            setLoginRespDataToPreferences(data);
                            setLoginRespData(data);
//                            new AlertDialog.Builder(getContext()).setMessage(getPrefManager().getMPin() != null ? getPrefManager().getMPin() : "--No----").setNeutralButton("OK", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
                                    if (data.isInitialLogin()) {
                                        if (!data.isTermsAccepted()) {
                                            Intent intent = new Intent(mContext, TermsAndCondActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else if (!data.isPasswordUpdated()) {
                                            Intent updatePass = new Intent(getContext(), ResetUpdatePasswordActivity.class);
                                            updatePass.putExtra("FROM_LOGIN", true);
                                            startActivity(updatePass);
                                            finish();
                                        } else if (!data.isMpinCreated()) {
                                            Intent updatePass = new Intent(mContext, CreateMPin.class);
                                            startActivity(updatePass);
                                            finish();
                                        } else if (!data.isSecQuestEnabled()) {
                                            Intent updatePass = new Intent(mContext, SecurityQuestionsActivity.class);
                                            startActivity(updatePass);
                                            finish();
                                        }

                                    } else {
                                        Intent intent = new Intent(mContext, MainDrawerActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }



                        } else {
                            dismissProgress();
                            showFailAlert(loginResponseModel.getStatusMessage());

//                            showToast(loginResponseModel.getStatusMessage());
//                        showFailAlert(loginResponseModel.getStatusMessage());
//                            mSharedPreference.setInt(mContext, "UserId", loginResponseModel.getData().getUserId());
//                            mSharedPreference.setString(mContext, "UserToken", loginResponseModel.getData().getToken());

//                                Intent homeIntent = new Intent(LoginActivity.this, MainDrawerActivity.class);
//                                startActivity(homeIntent);

                        }
                    } else {
                        showToast(mContext.getString(R.string.somethig_went_wrong));
                    }

                }

                @Override
                public void onFailure(Call<PHCLoginResponseModel> call, Throwable t) {
                    dismissProgress();
//                showToast(mContext.getString(R.string.somethig_went_wrong));
//                showFailAlert(mContext.getString(R.string.somethig_went_wrong));
                    showAlert(mContext.getString(R.string.somethig_went_wrong));
                }
            });

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private boolean validate() {
        boolean isValid;
        boolean cancel = false;
        View focusView = null;

        // Check for a valid mobile email.
        if (hasTextEmail(mEtEmail)) {
            focusView = mEtEmail;
            cancel = true;
        } else if (hasText(mEtPassword)) {
            focusView = mEtPassword;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            isValid = false;
        } else {
            isValid = true;
        }
        return isValid;
    }

    private void setLoginRespData(PHCLoginResponseData data) {
        getApplicationData().setAuthToken(data.getToken());
        getApplicationData().setEmailID(data.getEmailId());
        getApplicationData().setProfileUrl(data.getProfileURL());
        getApplicationData().setUserID(data.getUserId());
        getApplicationData().setFirstName(data.getFirstName());
        getApplicationData().setLastName(data.getLastName());
        getApplicationData().setContactNo(data.getContactNumber());
        getApplicationData().setAboutUser(data.getNotes());
        getApplicationData().setFacilityName(data.getFacilityName());
    }

    private void setLoginRespDataToPreferences(PHCLoginResponseData data) {
//        getPrefManager().setToken(PreferenceManager.encrypt(data.getToken()));
        getPrefManager().setUserName(PreferenceManager.encrypt(data.getFirstName() + " " + data.getLastName()));
        getPrefManager().setUserProfileUrl(PreferenceManager.encrypt(data.getProfileURL()));
        getPrefManager().setEmailID(PreferenceManager.encrypt(data.getEmailId()));
        if (data.getPk() != null)
            getPrefManager().setPK(PreferenceManager.encrypt(data.getPk()));

//                            getPrefManager().setProfileURL(data.getProfileURL());
//        getPrefManager().setUserID(PreferenceManager.encrypt(data.getUserId()));
        if (data.isMpinCreated()) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPrefManager().setMPin(PreferenceManager.encrypt(data.getMPIN()));
//            }
        }
    }


}
