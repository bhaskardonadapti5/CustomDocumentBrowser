package com.KryptosTextApp.KryptosText.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.model.GenericRespModel;
import com.KryptosTextApp.KryptosText.model.PHCLoginDataModel;
import com.KryptosTextApp.KryptosText.model.VerifyOTPRespModel;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetUpdatePasswordActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity implements View.OnClickListener {


    private static final String TAG = "ResetUpdatePasswordAct";
    private Context mContext;
    private EditText mEtNewPass;
    private EditText mEtConfirmPass;
    private Button mBtnSubmit;

    private String mEmailID;
    private boolean mFromLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_update_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mContext = this;
        mEmailID = getIntent().getStringExtra("KEY_EMAIL_ID");
        mFromLogin = getIntent().getBooleanExtra("FROM_LOGIN", false);

        findViews();
        if (mFromLogin) {
            setTitle("Update Password");
            mBtnSubmit.setText("Update");

        } else {
            mBtnSubmit.setText("Reset");
            setTitle("Reset Password");

        }

    }


    /**
     * Find the Views in the layout<br />
     */
    private void findViews() {
        mEtNewPass = (EditText) findViewById(R.id.et_new_pass);
        mEtConfirmPass = (EditText) findViewById(R.id.et_confirm_pass);
        mBtnSubmit = (Button) findViewById(R.id.btn_submit);

        mBtnSubmit.setOnClickListener(this);
    }

    /**
     * Handle button click events<br />
     */
    @Override
    public void onClick(View v) {
        if (v == mBtnSubmit) {
            if (validate()) {
                if (isNetworkAvailable()) {
                    showProgress();
                    try {
                        if (mFromLogin) {
                            updatePasswordRetroFit();
                        } else {
                            resetPasswordRetroFit();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast("Password encryption failed..");
                    }
                } else {
                    showNoNetworkAlert();

                }
            }
        }
    }

    private void resetPasswordRetroFit() {
        PHCLoginDataModel model = new PHCLoginDataModel();
        model.setEmailId(mEmailID);

        model.setPassword(mEtConfirmPass.getText().toString());

        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<VerifyOTPRespModel> call = apiService.updatePassword(model);

        call.enqueue(new Callback<VerifyOTPRespModel>() {
            @Override
            public void onResponse(Call<VerifyOTPRespModel> call, Response<VerifyOTPRespModel> response) {

                VerifyOTPRespModel forgotPassRespModel = response.body();
                if (forgotPassRespModel.getStatusCode() == 200) {
                    showToast(forgotPassRespModel.getStatusMessage());
                    dismissProgress();
                    if (mFromLogin) {
                        Intent intent = new Intent(mContext, com.KryptosTextApp.KryptosText.activity.CreateMPin.class);
                        showSuccessAlert(forgotPassRespModel.getStatusMessage(), intent);
                    } else {
                        showSuccessAlert(forgotPassRespModel.getStatusMessage(), null);

                    }
//                    finish();
                } else {
                    dismissProgress();
//                    showToast(forgotPassRespModel.getStatusMessage());
                    showFailAlert(forgotPassRespModel.getStatusMessage());

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

    private void updatePasswordRetroFit() {
        {
            PHCLoginDataModel model = new PHCLoginDataModel();
            model.setEmailId(getApplicationData().getEmailID());

            model.setPassword(mEtConfirmPass.getText().toString());

            PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
            Call<GenericRespModel> call = apiService.updatePasswordAfterLogin(getApplicationData().getAuthToken(), model);

            call.enqueue(new Callback<GenericRespModel>() {
                @Override
                public void onResponse(Call<GenericRespModel> call, Response<GenericRespModel> response) {
                    dismissProgress();
                    GenericRespModel data = response.body();
                    if (data.getStatusCode() == 200) {
                        showToast(data.getStatusMessage());
                        Intent intent = new Intent(mContext, com.KryptosTextApp.KryptosText.activity.CreateMPin.class);
                        showSuccessAlert(data.getStatusMessage(), intent);
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
    }

    private boolean validate() {
        boolean isValid;
        boolean cancel = false;
        View focusView = null;

        // Check for a valid mobile email.
        if (hasText(mEtNewPass)) {
            focusView = mEtNewPass;
            cancel = true;
        } else if (isPasswordValid(mEtNewPass)) {
            focusView = mEtNewPass;
            cancel = true;
        } else if (hasText(mEtConfirmPass)) {
            focusView = mEtConfirmPass;
            cancel = true;
        } else if (checkPasswordsEqual()) {
            focusView = mEtConfirmPass;
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

    private boolean checkPasswordsEqual() {
        String newPass = mEtNewPass.getText().toString().trim();
        String cnfNewPass = mEtConfirmPass.getText().toString().trim();

        mEtNewPass.setError(null);
        mEtConfirmPass.setError(null);
        // length 0 means there is no text

        if (!newPass.equals(cnfNewPass)) {
            mEtConfirmPass.setError(getString(R.string.error_cnf_pass_match));
//                focusView = mAboutShop;
//                cancel = true;
            return true;
        }


        return false;
    }

}
