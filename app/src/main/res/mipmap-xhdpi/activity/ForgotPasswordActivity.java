package com.KryptosTextApp.KryptosText.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.model.ForgotPassRespModel;
import com.KryptosTextApp.KryptosText.model.ForgotPassSecurityDataModel;
import com.KryptosTextApp.KryptosText.model.GenericRespModel;
import com.KryptosTextApp.KryptosText.model.PHCLoginDataModel;
import com.KryptosTextApp.KryptosText.model.PHCLoginResponseData;
import com.KryptosTextApp.KryptosText.model.SecurityQuestionModel;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;
import com.KryptosTextApp.KryptosText.utils.Log;
import com.google.gson.Gson;

import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity implements View.OnClickListener {


    private static final String TAG = "ForgotPasswordActivity";
    private Context mContext;
    private EditText mEtEmailId;
    private LinearLayout mLlSecQuestions;
    //    private Spinner mSpinnerSecQuestion;
    private EditText mEtSecAnsOne;
    private TextView mTvSecurityAnswerHintWarning, mTvSecurityQuestion;
    private Button mBtnSubmit;
    //    private SecQuesSpinnerAdapter mSpinnerAdapter;
    private List<SecurityQuestionModel> mSecurityQuestionModels;
    private int mSelectedSecurityQuestionID;
    private boolean isEmailVerified;
    private boolean mIsFromForgotMPin, mIsSecurity;
    private int mFailAttempt = 0;
    private PHCLoginResponseData loginResponseData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mIsFromForgotMPin = getIntent().getBooleanExtra("IsFromForgotMPin", false);
        mIsSecurity = getIntent().hasExtra("isSecurity") && getIntent().getBooleanExtra("isSecurity", false);
        loginResponseData = getIntent().hasExtra("loginResponse") ? (PHCLoginResponseData) getIntent().getSerializableExtra("loginResponse") : null;
        mContext = this;
        findViews();
        if (mIsFromForgotMPin) {
            setTitle("Forgot MPin");
            mEtEmailId.setText(getPrefManager().getEmailID());
            mEtEmailId.setEnabled(false);
            mEtEmailId.setFocusable(false);
        } else if (mIsSecurity) {
            setTitle("Security Questions");
            mEtEmailId.setText(getPrefManager().getEmailID());
            mEtEmailId.setEnabled(false);
            mEtEmailId.setFocusable(false);
            verifyEmailRetroFit();
        }
    }

    /**
     * Find the Views in the layout<br />
     */
    private void findViews() {
        ImageView mImageForgotPass = (ImageView) findViewById(R.id.image_forgot_pass);
        mEtEmailId = (EditText) findViewById(R.id.et_email_id);
        mLlSecQuestions = (LinearLayout) findViewById(R.id.ll_sec_questions);
        mTvSecurityQuestion = (TextView) findViewById(R.id.spinner_sec_question);
        mEtSecAnsOne = (EditText) findViewById(R.id.et_sec_ans_one);
        mTvSecurityAnswerHintWarning = (TextView) findViewById(R.id.tv_answer_hint_warning);
        mBtnSubmit = (Button) findViewById(R.id.btn_submit);
        mBtnSubmit.setText("Verify Email");
        mBtnSubmit.setOnClickListener(this);
        if (mIsFromForgotMPin || mIsSecurity) {
            mImageForgotPass.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_forgotmpin));
        }
    }

    /**
     * Handle button click events<br />
     */
    @Override
    public void onClick(View v) {
        if (v == mBtnSubmit) {
            // Handle clicks for mBtnSubmit
            if (validate()) {
                if (isNetworkAvailable()) {
                    if (isEmailVerified) {
                        showProgress();
                        verifySecQuesRetroFit();
                    } else {
                        showProgress("Authenticating Email..");
                        verifyEmailRetroFit();
                    }

                } else {
                    showAlert("Please check your network..");
                }
            }
        }
    }

    private void verifyEmailRetroFit() {
        PHCLoginDataModel model = new PHCLoginDataModel();
        model.setEmailId(mEtEmailId.getText().toString());
        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<ForgotPassRespModel> call = apiService.verifyEmail(model);

        call.enqueue(new Callback<ForgotPassRespModel>() {
            @Override
            public void onResponse(Call<ForgotPassRespModel> call, Response<ForgotPassRespModel> response) {
                ForgotPassRespModel forgotPassRespModel = response.body();
                Log.d(TAG, " verifyEmailRetroFit URL " + response.raw().request().url());
                Log.d(TAG, " verifyEmailRetroFit Data " + new Gson().toJson(response.body()));

                if (forgotPassRespModel.getStatusCode() == 200) {
//                    sharedPreference.setInt(mContext,"UserId",mLoginResponseModels.getData().getUserId());
//                    sharedPreference.setString(mContext,"UserToken",mLoginResponseModels.getData().getToken());
                    isEmailVerified = true;
                    showToast(forgotPassRespModel.getStatusMessage());
                    dismissProgress();
                    if (!forgotPassRespModel.isHasSecurityQuestions()) {
                        Intent intent = new Intent(mContext, com.KryptosTextApp.KryptosText.activity.LoginActivity.class);
                        intent.putExtra("KEY_EMAIL_ID", mEtEmailId.getText().toString());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("IsFromForgotPass", true);
                        showSuccessAlert(forgotPassRespModel.getStatusMessage(), intent);
                    }
                    mSecurityQuestionModels = forgotPassRespModel.getSecurityQuestionModels();
                    if (mSecurityQuestionModels != null && mSecurityQuestionModels.size() > 0) {
//                        SecurityQuestionModel model = new SecurityQuestionModel(0, "-Select Security Question-", null);
//                        mSecurityQuestionModels.add(0, model);

                        mLlSecQuestions.setVisibility(View.VISIBLE);
                        mEtEmailId.setEnabled(false);
                        mEtEmailId.setInputType(InputType.TYPE_NULL);

                        mBtnSubmit.setText("Submit");

                        int randomNum = new Random().nextInt(mSecurityQuestionModels.size() - 1);
                        SecurityQuestionModel securityQuestionModel = mSecurityQuestionModels.get(randomNum);
                        mTvSecurityQuestion.setText(securityQuestionModel.getSecurityQuestions());
                        mSelectedSecurityQuestionID = securityQuestionModel.getSecQuestionID();
                    }

                } else {
                    dismissProgress();

                    showFailAlert(forgotPassRespModel.getStatusMessage());

//                    showToast(forgotPassRespModel.getStatusMessage());

//                    Toast.makeText(mContext, mLoginResponseModels.getStatusMessage(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ForgotPassRespModel> call, Throwable t) {
                dismissProgress();
//                showToast(mContext.getString(R.string.somethig_went_wrong));
//                showFailAlert(mContext.getString(R.string.somethig_went_wrong));
                showAlert(t.getMessage());

            }
        });
    }

    private void verifySecQuesRetroFit() {
        ForgotPassSecurityDataModel model = new ForgotPassSecurityDataModel(mEtEmailId.getText().toString(), mSelectedSecurityQuestionID, mEtSecAnsOne.getText().toString());
        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<GenericRespModel> call;
        if (mIsFromForgotMPin) {
            call = apiService.verifySecurityQuesForMPin(model);
        } else {
            call = apiService.verifySecurityQues(model);

        }
        call.enqueue(new Callback<GenericRespModel>() {
            @Override
            public void onResponse(Call<GenericRespModel> call, Response<GenericRespModel> response) {
                Log.d(TAG, " verifySecQuesRetroFit URL " + response.raw().request().url());
                Log.d(TAG, " verifySecQuesRetroFit Resp" + new Gson().toJson(response.body()));

                GenericRespModel forgotPassRespModel = response.body();
                if (forgotPassRespModel.getStatusCode() == 200) {
                    dismissProgress();
                    if (mIsSecurity) {
                        Intent intent = new Intent();
                        intent.putExtra("loginResponse", loginResponseData);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        showToast(forgotPassRespModel.getStatusMessage());
                        Intent intent = new Intent(mContext, VerifyOTPActivity.class);
                        intent.putExtra("KEY_EMAIL_ID", mEtEmailId.getText().toString());
                        if (mIsFromForgotMPin) {
                            intent.putExtra("IsFromForgotMPin", true);
                        }
                        startActivity(intent);
                        finish();
                    }
                } else {
                    dismissProgress();
                    if (!mIsSecurity) {
//                    showToast(forgotPassRespModel.getStatusMessage());
                        mFailAttempt++;
                        mTvSecurityAnswerHintWarning.setText(mFailAttempt + " attempt failed out of 3.");
                        mTvSecurityAnswerHintWarning.setTextColor(Color.RED);
                        if (mFailAttempt == 3) {
                            mBtnSubmit.setEnabled(false);
                            showResetAlert(getContext().getResources().getString(R.string.reset_alert_message));
                        } else {
                            showFailAlert(forgotPassRespModel.getStatusMessage());
                        }
                    } else
                        showFailAlert(forgotPassRespModel.getStatusMessage());
                }
            }

            @Override
            public void onFailure(Call<GenericRespModel> call, Throwable t) {
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
        if (hasTextEmail(mEtEmailId)) {
            focusView = mEtEmailId;
            cancel = true;
        } else if (mLlSecQuestions.getVisibility() == View.VISIBLE && hasText(mEtSecAnsOne)) {
            focusView = mEtSecAnsOne;
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


    private void showResetAlert(String errorMsg) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the
        // dialog layout
//        builder.setTitle(title);
        builder.setCancelable(false);

//        builder.setIcon(R.drawable.galleryalart);
        View view = inflater.inflate(R.layout.failure_alert_dialog, null);
        builder.setView(view);
        TextView text = (TextView) view.findViewById(R.id.tv__failed_descr);
        text.setText(errorMsg);
        final AlertDialog alertDialog = builder.create();

        Button dialogButton = (Button) view.findViewById(R.id.btn_ok);
        dialogButton.setText("RESET");
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                resetAccountRetroFit();
            }
        });
        // Add action buttons
               /* .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }*/


        alertDialog.show();

    }

    private void resetAccountRetroFit() {
        showProgress();
        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<GenericRespModel> call = apiService.resetAccount(mEtEmailId.getText().toString());

        call.enqueue(new Callback<GenericRespModel>() {
            @Override
            public void onResponse(Call<GenericRespModel> call, Response<GenericRespModel> response) {
                Log.d(TAG, " resetAccountRetroFit URL " + response.raw().request().url());
                dismissProgress();
                GenericRespModel respModel = response.body();
                if (respModel.getStatusCode() == 200) {
                    getPrefManager().clearPrefs();
                    if (mIsFromForgotMPin) {
                        Intent intent = new Intent(getContext(), com.KryptosTextApp.KryptosText.activity.LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        showSuccessAlert(respModel.getStatusMessage(), intent);
                    } else {
                        showSuccessAlert(respModel.getStatusMessage(), null);
                    }
                } else {
                    dismissProgress();
                    showFailAlert(respModel.getStatusMessage());
//                    showToast(forgotPassRespModel.getStatusMessage());

//                    Toast.makeText(mContext, mLoginResponseModels.getStatusMessage(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericRespModel> call, Throwable t) {
                dismissProgress();
//                showToast(mContext.getString(R.string.somethig_went_wrong));
//                showFailAlert(mContext.getString(R.string.somethig_went_wrong));
                showAlert(t.getMessage());

            }
        });
    }
}
