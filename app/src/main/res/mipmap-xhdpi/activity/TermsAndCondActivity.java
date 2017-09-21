package com.KryptosTextApp.KryptosText.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.constant.PHCAppConstant;
import com.KryptosTextApp.KryptosText.model.GenericRespModel;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TermsAndCondActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = "TermsAndCondActivity";

    //    private TextView mTvTermsContent;
    private Button mBtnAccept;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_cond);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        findViews();

    }



    /**
     * Find the Views in the layout<br />
     */
    private void findViews() {
        WebView mWebView = (WebView) findViewById(R.id.webview);
//        mTvTermsContent = (TextView) findViewById(R.id.tv_terms_content);
        mBtnAccept = (Button) findViewById(R.id.btn_accept);
//        mTvTermsContent.setText(getResources().getString(R.string.terms_cond_content));
        mWebView.loadUrl(PHCAppConstant.TERMS_URL);
        mBtnAccept.setOnClickListener(this);
    }

    /**
     * Handle button click events<br />
     */
    @Override
    public void onClick(View v) {
        if (v == mBtnAccept) {
            if (isNetworkAvailable()) {
               showProgress();
                acceptTermsRetrofit();
            } else {
                showNoNetworkAlert();
            }
        }
    }

    private void acceptTermsRetrofit() {

        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<GenericRespModel> call = apiService.acceptTermsAndCondi(getApplicationData().getAuthToken(), getApplicationData().getEmailID(),true);

        call.enqueue(new Callback<GenericRespModel>() {
            @Override
            public void onResponse(Call<GenericRespModel> call, Response<GenericRespModel> response) {
                dismissProgress();
                GenericRespModel forgotPassRespModel = response.body();

                if (forgotPassRespModel.getStatusCode() == 200) {
                    Intent intent = new Intent(getContext(), com.KryptosTextApp.KryptosText.activity.ResetUpdatePasswordActivity.class);
                    intent.putExtra("FROM_LOGIN",true);
                    startActivity(intent);
                    finish();
                } else {
                    dismissProgress();
                    showFailAlert(forgotPassRespModel.getStatusMessage());
//                    Toast.makeText(mContext, mLoginResponseModels.getStatusMessage(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenericRespModel> call, Throwable t) {
                dismissProgress();
//                showFailAlert(mContext.getString(R.string.somethig_went_wrong));
                showAlert(t.getMessage());

            }
        });

    }


}
