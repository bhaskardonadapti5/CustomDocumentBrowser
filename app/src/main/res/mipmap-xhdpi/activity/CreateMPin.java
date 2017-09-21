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
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateMPin extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity implements View.OnClickListener {


    private Context mContext;
    private EditText mEtCreateMpin;
    private EditText mEtCnfMpin;
    private Button mBtnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_mpin);
        mContext = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViews();
    }


    /**
     * Find the Views in the layout<br />
     */
    private void findViews() {
        mEtCreateMpin = (EditText) findViewById(R.id.et_create_mpin);
        mEtCnfMpin = (EditText) findViewById(R.id.et_cnf_mpin);
        mBtnSubmit = (Button) findViewById(R.id.btn_submit);

        mBtnSubmit.setOnClickListener(this);
    }

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2017-02-17 17:12:31 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == mBtnSubmit) {
            if (validate()) {
                if (isNetworkAvailable()) {
                    showProgress("Creating MPin..");
                    try {
                        createMPinRetrofit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    showNoNetworkAlert();
                }
            }
        }
    }

    private void createMPinRetrofit() {
        PHCLoginDataModel model = new PHCLoginDataModel();
        model.setEmailId(getApplicationData().getEmailID());
        final String mPinEncrypted = mEtCnfMpin.getText().toString();
        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<GenericRespModel> call = apiService.createMPin(getApplicationData().getAuthToken(), getApplicationData().getEmailID(), mPinEncrypted);

        call.enqueue(new Callback<GenericRespModel>() {
            @Override
            public void onResponse(Call<GenericRespModel> call, Response<GenericRespModel> response) {
                dismissProgress();
                GenericRespModel data = response.body();
                if (data == null) {
                    showAlert(getResources().getString(R.string.somethig_went_wrong));
                    return;
                }
                if (data.getStatusCode() == 200) {
                    getPrefManager().setMPin(mPinEncrypted);
                    showToast(data.getStatusMessage());
                    Intent intent = new Intent(mContext, SecurityQuestionsActivity.class);
                    startActivity(intent);
                    finish();
//                    showSuccessAlert(data.getStatusMessage(),intent);
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
        if (hasText(mEtCreateMpin)) {
            focusView = mEtCreateMpin;
            cancel = true;
        } else if (hasText(mEtCnfMpin)) {
            focusView = mEtCnfMpin;
            cancel = true;
        } else if (checkMPinEqual()) {
            focusView = mEtCnfMpin;
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

    private boolean checkMPinEqual() {
        String newMPin = mEtCreateMpin.getText().toString().trim();
        String cnfNewMPin = mEtCnfMpin.getText().toString().trim();

        mEtCreateMpin.setError(null);
        mEtCnfMpin.setError(null);
        // length 0 means there is no text

        if (!newMPin.equals(cnfNewMPin)) {
            mEtCnfMpin.setError(getString(R.string.error_cnf_mpin_match));
//                focusView = mAboutShop;
//                cancel = true;
            return true;
        }


        return false;
    }
}
