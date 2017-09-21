package com.KryptosTextApp.KryptosText.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.model.ForgotPassOtpValidInput;
import com.KryptosTextApp.KryptosText.model.VerifyOTPRespModel;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;
import com.KryptosTextApp.KryptosText.utils.Log;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOTPActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = "VerifyOTPActivity";
    private Context mContext;
    private EditText mEtEnterOtp;
    private Button mBtnSubmit;
    private Button mBtnResendOtp;
    private String mEmailID;
    private boolean mIsFromForgotMPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEmailID = getIntent().getStringExtra("KEY_EMAIL_ID");
        mIsFromForgotMPin = getIntent().getBooleanExtra("IsFromForgotMPin", false);
        mContext = this;
        findViews();

    }

    /**
     * Find the Views in the layout<br />
     */
    private void findViews() {
        mEtEnterOtp = (EditText) findViewById(R.id.et_enter_otp);
        mBtnSubmit = (Button) findViewById(R.id.btn_submit);
        mBtnResendOtp = (Button) findViewById(R.id.btn_resend_otp);

        mBtnSubmit.setOnClickListener(this);
        mBtnResendOtp.setOnClickListener(this);
    }

    /**
     * Handle button click events<br />
     */
    @Override
    public void onClick(View v) {
        if (v == mBtnSubmit) {
            if (validate()) {
                if (isNetworkAvailable()) {
                    showProgress("Validating OTP..");
                    verifyOTPRetroFit();
                } else {
                    showNoNetworkAlert();

                }
            }
        } else if (v == mBtnResendOtp) {
            // Handle clicks for mBtnResendOtp
            if (isNetworkAvailable()) {
                showProgress("Resending OTP..");
                reSendOTPRetroFit();
            } else {
                showNoNetworkAlert();
            }
        }
    }

    private void reSendOTPRetroFit() {
//        ForgotPassOtpValidInput model = new ForgotPassOtpValidInput(mEmailID,mEtEnterOtp.getText().toString());
        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<VerifyOTPRespModel> call;
        if (mIsFromForgotMPin) {
            call = apiService.resedOTPForMpin(mEmailID);
        } else {
            call = apiService.resedOTP(mEmailID);
        }
        call.enqueue(new Callback<VerifyOTPRespModel>() {
            @Override
            public void onResponse(Call<VerifyOTPRespModel> call, Response<VerifyOTPRespModel> response) {
                Log.d(TAG, " reSendOTPRetroFit Url " + response.raw().request().url());
                Log.d(TAG, " reSendOTPRetroFit Resp " + new Gson().toJson(response.body()));

                VerifyOTPRespModel verifyOTPRespModel = response.body();
                if (verifyOTPRespModel.getStatusCode() == 200) {
                    showToast(verifyOTPRespModel.getStatusMessage());
                    dismissProgress();
                } else {
                    dismissProgress();
//                    showToast(forgotPassRespModel.getStatusMessage());
                    showFailAlert(verifyOTPRespModel.getStatusMessage());

                }
            }

            @Override
            public void onFailure(Call<VerifyOTPRespModel> call, Throwable t) {
                dismissProgress();
//                showToast(mContext.getString(R.string.somethig_went_wrong));
//                showFailAlert(mContext.getString(R.string.somethig_went_wrong));
                showAlert(mContext.getString(R.string.somethig_went_wrong));

            }
        });
    }

    private void verifyOTPRetroFit() {
        ForgotPassOtpValidInput model = new ForgotPassOtpValidInput(mEmailID, mEtEnterOtp.getText().toString());
        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<VerifyOTPRespModel> call;
        if (mIsFromForgotMPin) {
            call = apiService.verifyForgotMPinOTP(model);
        } else {
            call = apiService.verifyForgotPassOTP(model);
        }
        call.enqueue(new Callback<VerifyOTPRespModel>() {
            @Override
            public void onResponse(Call<VerifyOTPRespModel> call, Response<VerifyOTPRespModel> response) {

                VerifyOTPRespModel verifyOTPRespModel = response.body();
                if (verifyOTPRespModel.getStatusCode() == 200) {
                    showToast(verifyOTPRespModel.getStatusMessage());
                    dismissProgress();
                    if (mIsFromForgotMPin) {
                        Intent intent = new Intent(mContext, com.KryptosTextApp.KryptosText.activity.ResetMPinActivity.class);
                        intent.putExtra("KEY_EMAIL_ID", mEmailID);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(mContext, com.KryptosTextApp.KryptosText.activity.ResetUpdatePasswordActivity.class);
                        intent.putExtra("KEY_EMAIL_ID", mEmailID);
                        startActivity(intent);
                    }
                    finish();

                } else {
                    dismissProgress();
//                    showToast(forgotPassRespModel.getStatusMessage());
                    showFailAlert(verifyOTPRespModel.getStatusMessage());

                }
            }

            @Override
            public void onFailure(Call<VerifyOTPRespModel> call, Throwable t) {
                dismissProgress();
//                showToast(mContext.getString(R.string.somethig_went_wrong));
//                showFailAlert(mContext.getString(R.string.somethig_went_wrong));
                showAlert(mContext.getString(R.string.somethig_went_wrong));

            }
        });
    }

    private boolean validate() {
        boolean isValid;
        boolean cancel = false;
        View focusView = null;

        // Check for a valid mobile email.
        if (hasText(mEtEnterOtp)) {
            focusView = mEtEnterOtp;
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


}
