package com.KryptosTextApp.KryptosText.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.adapter.SecurityQuestionsAllAdapter;
import com.KryptosTextApp.KryptosText.model.CreateSecurityQuesDataModel;
import com.KryptosTextApp.KryptosText.model.GenericRespModel;
import com.KryptosTextApp.KryptosText.model.PHCLoginDataModel;
import com.KryptosTextApp.KryptosText.model.SecurityQuestionModel;
import com.KryptosTextApp.KryptosText.model.SecurityQuestionsModel;
import com.KryptosTextApp.KryptosText.model.SecurityQuestionsRespModel;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;
import com.KryptosTextApp.KryptosText.utils.Log;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SecurityQuestionsActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = "SecurityQuestionsActivity";

    private Context mContext;
    private Spinner mSpinnerSecQuestionOne;
    private EditText mEtSecAnsOne;
    private Spinner mSpinnerSecQuestionTwo;
    private EditText mEtSecAnsTwo;
    private Spinner mSpinnerSecQuestionThree;
    private EditText mEtSecAnsThree;
    private Button mBtnSubmit;

    private List<SecurityQuestionsModel> mSecurityQuestionModels;
    private List<SecurityQuestionsModel> mTempSecQuesList, mTempSecQuesSecList, mTempSecQuesThreeList;
    private Map<Integer, SecurityQuestionsModel> selectedQuestions;
    private LinearLayout ll_no_records;
    private ScrollView sv_questions;
    private SecurityQuestionsAllAdapter secQuesOneAdapter;
    private SecurityQuestionsAllAdapter secQuesTwoAdapter;
    private SecurityQuestionsAllAdapter secQuesThreeAdapter;
    public static final Integer SP_ONE = 1;
    public static final Integer SP_TWO = 2;
    public static final Integer SP_THREE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_questions);
        selectedQuestions = new HashMap<>();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mContext = this;
        findViews();
        if (isNetworkAvailable()) {
            showProgress();
            getSecQuestionsRetrofit();
        } else {
            showNoNetworkAlert();
        }
    }



    /**
     * Find the Views in the layout<br />
     */
    private void findViews() {
        mSpinnerSecQuestionOne = (Spinner) findViewById(R.id.spinner_sec_question_one);
        mEtSecAnsOne = (EditText) findViewById(R.id.et_sec_ans_one);
        mSpinnerSecQuestionTwo = (Spinner) findViewById(R.id.spinner_sec_question_two);
        mEtSecAnsTwo = (EditText) findViewById(R.id.et_sec_ans_two);
        mSpinnerSecQuestionThree = (Spinner) findViewById(R.id.spinner_sec_question_three);
        mEtSecAnsThree = (EditText) findViewById(R.id.et_sec_ans_three);
        mBtnSubmit = (Button) findViewById(R.id.btn_submit);
        ll_no_records = (LinearLayout) findViewById(R.id.ll_no_records);
        sv_questions = (ScrollView) findViewById(R.id.sv_questions);

        mBtnSubmit.setOnClickListener(this);
        mSpinnerSecQuestionOne.setOnItemSelectedListener(this);
        mSpinnerSecQuestionTwo.setOnItemSelectedListener(this);
        mSpinnerSecQuestionThree.setOnItemSelectedListener(this);
//        mSpinnerSecQuestionOne.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                SecurityQuestionsModel model = mSecurityQuestionModels.get(position);
//                mSelectedSecurityQuestionOneID = model.getID();
//                mSelectedSecurityQuestionOne = model.getSecurityQuestion();
//
//               /* if (position>0){
//                    mTempSecQuesList.remove(position);
//                    SecurityQuestionsAllAdapter secQuesOneAdapter = new SecurityQuestionsAllAdapter(mContext,
//                            android.R.layout.simple_spinner_item,
//                            mTempSecQuesList);
//                    secQuesOneAdapter.setDropDownViewResource
//                            (android.R.layout.simple_spinner_dropdown_item);
//
//                    mSpinnerSecQuestionTwo.setAdapter(secQuesOneAdapter);
//                    mSpinnerSecQuestionThree.setAdapter(secQuesOneAdapter);
//                }*/
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

//        mSpinnerSecQuestionTwo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                SecurityQuestionsModel model = mSecurityQuestionModels.get(position);
//                mSelectedSecurityQuestionTwoID = model.getID();
//                mSelectedSecurityQuestionTwo = model.getSecurityQuestion();
//
//               /* if (position>0){
//                    mTempSecQuesList = mSecurityQuestionModels;
//                    mTempSecQuesList.remove(position);
//                    SecurityQuestionsAllAdapter secQuesOneAdapter = new SecurityQuestionsAllAdapter(mContext,
//                            android.R.layout.simple_spinner_item,
//                            mTempSecQuesList);
//                    secQuesOneAdapter.setDropDownViewResource
//                            (android.R.layout.simple_spinner_dropdown_item);
//                    mSpinnerSecQuestionThree.setAdapter(secQuesOneAdapter);
//                }*/
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
//        mSpinnerSecQuestionThree.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                SecurityQuestionsModel model = mSecurityQuestionModels.get(position);
//                mSelectedSecurityQuestionThreeID = model.getID();
//                mSelectedSecurityQuestionThree = model.getSecurityQuestion();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
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
                    showProgress();
                    addSecurityAnswersRetrofit();
                } else {
                    showNoNetworkAlert();
                }
            }
        }
    }

    private void getSecQuestionsRetrofit() {
        PHCLoginDataModel model = new PHCLoginDataModel();
        model.setEmailId(getApplicationData().getEmailID());

        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<SecurityQuestionsRespModel> call = apiService.getAllSecQuestions(getApplicationData().getAuthToken());

        call.enqueue(new Callback<SecurityQuestionsRespModel>() {
            @Override
            public void onResponse(Call<SecurityQuestionsRespModel> call, Response<SecurityQuestionsRespModel> response) {
                Log.d(TAG, " getSecQuestionsRetrofit Resp " + new Gson().toJson(response.body()));
                dismissProgress();
                SecurityQuestionsRespModel data = response.body();
                if (data == null) {
                    showAlert("No Response form the sever.");
                    return;
                }
                if (data.getStatusCode() == 200) {
                    changeVisibity(true);
                    showToast(data.getStatusMessage());
                    mSecurityQuestionModels = data.getSecurityQuestions();
                    mTempSecQuesList = new ArrayList<>();
                    mTempSecQuesSecList = new ArrayList<>();
                    mTempSecQuesThreeList = new ArrayList<>();
                    if (mSecurityQuestionModels != null && mSecurityQuestionModels.size() > 0) {

                        SecurityQuestionsModel model = new SecurityQuestionsModel(0, "- Select Security Question -");
                        mSecurityQuestionModels.add(0, model);
                        mTempSecQuesList.addAll(mSecurityQuestionModels);
                        mTempSecQuesSecList.addAll(mSecurityQuestionModels);
                        mTempSecQuesThreeList.addAll(mSecurityQuestionModels);

                        secQuesOneAdapter = new SecurityQuestionsAllAdapter(mContext,  mTempSecQuesList, SP_ONE);
                        secQuesOneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        secQuesOneAdapter.setSelectedQuestions(selectedQuestions);
                        secQuesTwoAdapter = new SecurityQuestionsAllAdapter(mContext, mTempSecQuesSecList, SP_TWO);
                        secQuesTwoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        secQuesTwoAdapter.setSelectedQuestions(selectedQuestions);
                        secQuesThreeAdapter = new SecurityQuestionsAllAdapter(mContext,mTempSecQuesThreeList, SP_THREE);
                        secQuesThreeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        secQuesThreeAdapter.setSelectedQuestions(selectedQuestions);

                        mSpinnerSecQuestionOne.setAdapter(secQuesOneAdapter);
                        mSpinnerSecQuestionTwo.setAdapter(secQuesTwoAdapter);
                        mSpinnerSecQuestionThree.setAdapter(secQuesThreeAdapter);
                    }
//                    showSuccessAlert(data.getStatusMessage(),intent);
                } else {
                    changeVisibity(false);
                    showFailAlert(data.getStatusMessage());
                }
            }

            @Override
            public void onFailure(Call<SecurityQuestionsRespModel> call, Throwable t) {
                dismissProgress();
                changeVisibity(false);
                showAlert(t.getMessage());

            }
        });
    }

    public void retry(View view) {
        getSecQuestionsRetrofit();
    }


    private void changeVisibity(boolean success) {
        sv_questions.setVisibility(success ? View.VISIBLE : View.GONE);
        ll_no_records.setVisibility(success ? View.GONE : View.VISIBLE);
    }

    private void addSecurityAnswersRetrofit() {

        CreateSecurityQuesDataModel model = new CreateSecurityQuesDataModel();
        model.setEmailId(getApplicationData().getEmailID());
        List<SecurityQuestionModel> securityQuestionModels = new ArrayList<>();
        SecurityQuestionModel modelOne = new SecurityQuestionModel(selectedQuestions.get(SP_ONE).getID(), selectedQuestions.get(SP_ONE).getSecurityQuestion(), mEtSecAnsOne.getText().toString());
        SecurityQuestionModel modelTwo = new SecurityQuestionModel(selectedQuestions.get(SP_TWO).getID(), selectedQuestions.get(SP_TWO).getSecurityQuestion(), mEtSecAnsTwo.getText().toString());
        SecurityQuestionModel modelThree = new SecurityQuestionModel(selectedQuestions.get(SP_THREE).getID(), selectedQuestions.get(SP_THREE).getSecurityQuestion(), mEtSecAnsThree.getText().toString());
        securityQuestionModels.add(modelOne);
        securityQuestionModels.add(modelTwo);
        securityQuestionModels.add(modelThree);

        model.setUserSecurityQuestions(securityQuestionModels);

        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<GenericRespModel> call = apiService.createSecurityAnswers(getApplicationData().getAuthToken(), model);

        call.enqueue(new Callback<GenericRespModel>() {
            @Override
            public void onResponse(Call<GenericRespModel> call, Response<GenericRespModel> response) {
                dismissProgress();
                GenericRespModel data = response.body();
                if (data.getStatusCode() == 200) {
                    showToast(data.getStatusMessage());
                    Intent intent = new Intent(mContext, com.KryptosTextApp.KryptosText.activity.MainDrawerActivity.class);
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
        if (hasSpinnerSelected(mSpinnerSecQuestionOne)) {
            focusView = mSpinnerSecQuestionOne;
            cancel = true;
        } else if (hasText(mEtSecAnsOne)) {
            focusView = mEtSecAnsOne;
            cancel = true;
        } else if (hasSpinnerSelected(mSpinnerSecQuestionTwo)) {
            focusView = mSpinnerSecQuestionTwo;
            cancel = true;
        } else if (hasText(mEtSecAnsTwo)) {
            focusView = mEtSecAnsTwo;
            cancel = true;
        } else if (hasSpinnerSelected(mSpinnerSecQuestionThree)) {
            focusView = mSpinnerSecQuestionThree;
            cancel = true;
        } else if (hasText(mEtSecAnsThree)) {
            focusView = mEtSecAnsThree;
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


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position != 0) {
            switch (parent.getId()) {
                case R.id.spinner_sec_question_one:
                    selectedQuestions.put(SP_ONE, mTempSecQuesList.get(position));
                    mEtSecAnsOne.requestFocus();
                    break;
                case R.id.spinner_sec_question_two:
                    selectedQuestions.put(SP_TWO, mTempSecQuesSecList.get(position));
                    mEtSecAnsTwo.requestFocus();
                    break;
                case R.id.spinner_sec_question_three:
                    selectedQuestions.put(SP_THREE, mTempSecQuesThreeList.get(position));
                    mEtSecAnsThree.requestFocus();
                    break;
            }
//            updateData();
            notifyData();
        }
    }

    private void notifyData() {
        secQuesOneAdapter.setSelectedQuestions(selectedQuestions);
        secQuesOneAdapter.notifyDataSetChanged();
        secQuesTwoAdapter.setSelectedQuestions(selectedQuestions);
        secQuesTwoAdapter.notifyDataSetChanged();
        secQuesThreeAdapter.setSelectedQuestions(selectedQuestions);
        secQuesThreeAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

//    private void updateData() {
//        //Update First Spinner
//        loadTempData(SP_ONE);
//
//        //Update Second Spinner
//        loadTempData(SP_TWO);
//
//        //Update Third Spinner
//        loadTempData(SP_THREE);
//    }

//    private void loadTempData(int spinnerId) {
//        switch (spinnerId) {
//            case 1:
//                mTempSecQuesList.clear();
//                mTempSecQuesList.addAll(mSecurityQuestionModels);
//                secQuesOneAdapter.notifyDataSetChanged();
//                mSpinnerSecQuestionOne.setSelection(0);
//                if (selectedQuestions.containsKey(SP_TWO))
//                    mTempSecQuesList.remove(selectedQuestions.get(SP_TWO).intValue());
//                if (selectedQuestions.containsKey(SP_THREE))
//                    mTempSecQuesList.remove(selectedQuestions.get(SP_THREE).intValue());
//                secQuesOneAdapter.notifyDataSetChanged();
//                if (selectedQuestions.containsKey(SP_ONE))
//                    mSpinnerSecQuestionOne.setSelection(mTempSecQuesList.indexOf(selectedQuestions.get(SP_ONE)));
//                break;
//            case 2:
//                mTempSecQuesSecList.clear();
//                mTempSecQuesSecList.addAll(mSecurityQuestionModels);
//                secQuesTwoAdapter.notifyDataSetChanged();
//                mSpinnerSecQuestionTwo.setSelection(0);
//                if (selectedQuestions.containsKey(SP_ONE))
//                    mTempSecQuesSecList.remove(selectedQuestions.get(SP_ONE).intValue());
//                if (selectedQuestions.containsKey(SP_THREE))
//                    mTempSecQuesSecList.remove(selectedQuestions.get(SP_THREE).intValue());
//                secQuesTwoAdapter.notifyDataSetChanged();
//                if (selectedQuestions.containsKey(SP_TWO))
//                    mSpinnerSecQuestionTwo.setSelection(mTempSecQuesSecList.indexOf(selectedQuestions.get(SP_TWO)));
//                break;
//            case 3:
//                mTempSecQuesThreeList.clear();
//                mTempSecQuesThreeList.addAll(mSecurityQuestionModels);
//                secQuesThreeAdapter.notifyDataSetChanged();
//                mSpinnerSecQuestionThree.setSelection(0);
//                if (selectedQuestions.containsKey(SP_ONE))
//                    mTempSecQuesThreeList.remove(selectedQuestions.get(SP_ONE).intValue());
//                if (selectedQuestions.containsKey(SP_TWO))
//                    mTempSecQuesThreeList.remove(selectedQuestions.get(SP_TWO).intValue());
//                secQuesThreeAdapter.notifyDataSetChanged();
//                if (selectedQuestions.containsKey(SP_THREE))
//                    mSpinnerSecQuestionThree.setSelection(mTempSecQuesThreeList.indexOf(selectedQuestions.get(SP_THREE)));
//                break;
//        }
//    }
}
