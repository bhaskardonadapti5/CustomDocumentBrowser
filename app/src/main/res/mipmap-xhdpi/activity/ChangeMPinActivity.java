package com.KryptosTextApp.KryptosText.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.interfaces.OnMpinCompletedListener;
import com.KryptosTextApp.KryptosText.model.ChangeMPinDataModel;
import com.KryptosTextApp.KryptosText.model.GenericRespModel;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;
import com.KryptosTextApp.KryptosText.utils.CustomMPinEditText;
import com.KryptosTextApp.KryptosText.utils.Log;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeMPinActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ChangeMPinActivity";
    private CustomMPinEditText mEtOldMpin, mEtNewMpin, mEtConfirmMpin;
    private Button mBtnSubmit;
    boolean isValidNewPassword = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_mpin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViews();
    }


    /**
     * Find the Views in the layout<br />
     */
    private void findViews() {
        mEtOldMpin = (CustomMPinEditText) findViewById(R.id.et_old_mpin);
        mEtNewMpin = (CustomMPinEditText) findViewById(R.id.et_new_mpin);
        mEtConfirmMpin = (CustomMPinEditText) findViewById(R.id.et_confirm_mpin);
        mBtnSubmit = (Button) findViewById(R.id.btn_submit);
        mBtnSubmit.setOnClickListener(this);
        mEtOldMpin.setOnCompletedListener(new OnMpinCompletedListener() {
            @Override
            public void onMPinCompleted() {
                mEtNewMpin.requestFocus();
            }
        });
        mEtNewMpin.setOnCompletedListener(new OnMpinCompletedListener() {
            @Override
            public void onMPinCompleted() {
                mEtConfirmMpin.requestFocus();
            }
        });
        mEtConfirmMpin.setOnCompletedListener(new OnMpinCompletedListener() {
            @Override
            public void onMPinCompleted() {
                String confirmPin = mEtConfirmMpin.getText();
                String newPin = mEtNewMpin.getText();
                isValidNewPassword = newPin.length() == 4 && confirmPin.length() == 4 && confirmPin.equals(newPin);
                if (!isValidNewPassword) {
                    mEtConfirmMpin.setError(getString(R.string.error_cnf_mpin_match));
                }
            }
        });
    }

    /**
     * Handle button click events<br />
     */
    @Override
    public void onClick(View v) {
        if (v == mBtnSubmit) {
            // Handle clicks for btnSubmit
            if (mEtOldMpin.getText().length() < 4) {
                mEtOldMpin.setError(getString(R.string.error_field_min_length_mpin));
            } else if (mEtNewMpin.getText().length() < 4) {
                mEtNewMpin.setError(getString(R.string.error_field_min_length_mpin));
            } else if (mEtConfirmMpin.getText().length() < 4) {
                mEtConfirmMpin.setError(getString(R.string.error_field_min_length_mpin));
            } else if (!mEtNewMpin.getText().equals(mEtConfirmMpin.getText())) {
                mEtConfirmMpin.setError(getString(R.string.error_cnf_mpin_match));
            } else {
                if (isNetworkAvailable()) {
                    showProgress("Updating MPin..");
                    try {
                        updateMPinRetroFit();
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

    private void updateMPinRetroFit() {
        {
            hideKeyboard(this);
            ChangeMPinDataModel model = new ChangeMPinDataModel();
            model.setEmailId(getApplicationData().getEmailID());
            model.setOldMPIN(mEtOldMpin.getText());
            model.setNewMPIN(mEtNewMpin.getText());

            PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
            Call<GenericRespModel> call = apiService.changeMPin(getApplicationData().getAuthToken(), model);

            call.enqueue(new Callback<GenericRespModel>() {
                @Override
                public void onResponse(Call<GenericRespModel> call, Response<GenericRespModel> response) {
                    Log.d(TAG, " updatePasswordRetroFit URL " + response.raw().request().url());
                    Log.d(TAG, "updateProfileImageRetrofit Resp " + new Gson().toJson(response.body()));

                    dismissProgress();
                    GenericRespModel data = response.body();
                    if (data.getStatusCode() == 200) {
                        Intent i = new Intent(com.KryptosTextApp.KryptosText.activity.ChangeMPinActivity.this, com.KryptosTextApp.KryptosText.activity.LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        showSuccessAlert(data.getStatusMessage(), i); //Intent null means current activity finishes on dialog close
                        getPrefManager().clearPrefs();
                        getApplicationData().getWebSocketInstance().disconnect();

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

    @Override
    protected void onStop() {
        super.onStop();
        hideKeyboard(this);
    }
}
