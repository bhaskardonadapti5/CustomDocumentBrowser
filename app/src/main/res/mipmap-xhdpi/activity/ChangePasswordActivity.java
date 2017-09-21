package com.KryptosTextApp.KryptosText.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.model.ChangePasswordDataModel;
import com.KryptosTextApp.KryptosText.model.GenericRespModel;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;
import com.KryptosTextApp.KryptosText.utils.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ChangePasswordActivity";

    private EditText mEtOldPass;
    private EditText mEtNewPass;
    private EditText mEtConfirmPass;
    private Button mBtnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViews();

    }

    /**
     * Find the Views in the layout<br />
     */
    private void findViews() {
        mEtOldPass = (EditText) findViewById(R.id.et_old_pass);
        mEtNewPass = (EditText) findViewById(R.id.et_new_pass);
        mEtConfirmPass = (EditText) findViewById(R.id.et_confirm_pass);
        mBtnSubmit = (Button) findViewById(R.id.btn_submit);

        mBtnSubmit.setOnClickListener(this);

        mEtNewPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isPasswordValid(mEtNewPass)) {
//                    mEtNewPass.setBackground(getResources().getDrawable(R.drawable.valid_password_et_bg));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mEtNewPass.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_green_dark)));
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mEtNewPass.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark)));
                }
            }
        });
        mEtConfirmPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!checkPasswordsEqual()) {
//                    mEtNewPass.setBackground(getResources().getDrawable(R.drawable.valid_password_et_bg));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mEtConfirmPass.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_green_dark)));
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mEtConfirmPass.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark)));
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
            if (validate()) {
                if (isNetworkAvailable()) {
                    showProgress("Updating password..");
                    try {
                        updatePasswordRetroFit();
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

    private void updatePasswordRetroFit() {
        {
            hideKeyboard(this);
            ChangePasswordDataModel model = new ChangePasswordDataModel();
            model.setEmailId(getApplicationData().getEmailID());
            model.setOldPassword(mEtOldPass.getText().toString());
            model.setNewPassword(mEtNewPass.getText().toString());

            PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
            Call<GenericRespModel> call = apiService.changePassword(getApplicationData().getAuthToken(), model);

            call.enqueue(new Callback<GenericRespModel>() {
                @Override
                public void onResponse(Call<GenericRespModel> call, Response<GenericRespModel> response) {
                    dismissProgress();
                    GenericRespModel data = response.body();
                    if (data.getStatusCode() == 200) {
                        Intent i = new Intent(com.KryptosTextApp.KryptosText.activity.ChangePasswordActivity.this, com.KryptosTextApp.KryptosText.activity.LoginActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        showSuccessAlert(data.getStatusMessage(), i); //Intent null means current activity finishes on dialog close
                        getPrefManager().clearPrefs();
                        getApplicationData().getWebSocketInstance().disconnect();
//                        showSuccessAlert(data.getStatusMessage(), null);
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
        if (hasText(mEtOldPass)) {
            focusView = mEtOldPass;
            cancel = true;
        } else if (hasText(mEtNewPass)) {
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

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }
}
