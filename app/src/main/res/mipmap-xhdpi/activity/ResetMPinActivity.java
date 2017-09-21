package com.KryptosTextApp.KryptosText.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.model.GenericRespModel;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;
import com.KryptosTextApp.KryptosText.utils.PHCAesHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetMPinActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity implements View.OnClickListener {

    private EditText mEtNewMpin;
    private EditText mEtConfirmMpin;
    private Button mBtnSubmit;
    private String mEmailID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_mpin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mEmailID = getIntent().getStringExtra("KEY_EMAIL_ID");
        findViews();

    }

    /**
     * Find the Views in the layout<br />
     */
    private void findViews() {
        mEtNewMpin = (EditText) findViewById(R.id.et_new_mpin);
        mEtConfirmMpin = (EditText) findViewById(R.id.et_confirm_mpin);
        mBtnSubmit = (Button) findViewById(R.id.btn_submit);

        mBtnSubmit.setOnClickListener(this);
    }

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2017-03-04 15:43:00 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == mBtnSubmit) {
            // Handle clicks for mBtnSubmit
            if (validate()) {
                if (isNetworkAvailable()) {
                    showProgress();
                    try {
                        resetMPinRetroFit();
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

    private void resetMPinRetroFit() {

//        ChangeMPinDataModel model = new ChangeMPinDataModel();
//        model.setEmailId(getApplicationData().getEmailID());
        PHCAesHelper aesHelper = new PHCAesHelper();
//        model.setNewMPIN(aesHelper.encrypt(mEtNewMpin.getText().toString(), PHCAppConstant.AES_KEY, PHCAppConstant.AES_IV).trim());

        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<GenericRespModel> call = apiService.resetMPin(mEmailID, mEtNewMpin.getText().toString());

        call.enqueue(new Callback<GenericRespModel>() {
            @Override
            public void onResponse(Call<GenericRespModel> call, Response<GenericRespModel> response) {
                dismissProgress();
                GenericRespModel data = response.body();
                if (data.getStatusCode() == 200) {
                    showSuccessAlert(data.getStatusMessage(), null); //Intent null means current activity finishes on dialog close
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

    private boolean validate() {
        boolean isValid;
        boolean cancel = false;
        View focusView = null;

        // Check for a valid mobile email.
        if (hasText(mEtNewMpin)) {
            focusView = mEtNewMpin;
            cancel = true;
        } else if (hasText(mEtConfirmMpin)) {
            focusView = mEtConfirmMpin;
            cancel = true;
        } else if (checkMPinsEqual()) {
            focusView = mEtConfirmMpin;
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

    private boolean checkMPinsEqual() {
        String newPass = mEtNewMpin.getText().toString().trim();
        String cnfNewPass = mEtConfirmMpin.getText().toString().trim();

        mEtNewMpin.setError(null);
        mEtConfirmMpin.setError(null);
        // length 0 means there is no text

        if (!newPass.equals(cnfNewPass)) {
            mEtConfirmMpin.setError(getString(R.string.error_cnf_mpin_match));
//                focusView = mAboutShop;
//                cancel = true;
            return true;
        }


        return false;
    }
}
