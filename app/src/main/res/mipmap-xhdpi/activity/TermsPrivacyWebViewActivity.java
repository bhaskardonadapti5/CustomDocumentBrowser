package com.KryptosTextApp.KryptosText.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.KryptosTextApp.KryptosText.R;

import static com.KryptosTextApp.KryptosText.constant.PHCAppConstant.PRIVACY_URL;
import static com.KryptosTextApp.KryptosText.constant.PHCAppConstant.TERMS_URL;

/**
 * Created by KTirumalsetty on 5/26/2017.
 */

public class TermsPrivacyWebViewActivity  extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity {

    private WebView mWebView;
    private boolean mIsPrivacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mIsPrivacy = getIntent().getBooleanExtra("IsPrivacy",false);
        mWebView = (WebView) findViewById(R.id.webview);

        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        if (mIsPrivacy){
            setTitle(getString(R.string.title_privacy_policy));
            mWebView.loadUrl(PRIVACY_URL);
        }else {
            setTitle(getString(R.string.title_terms_conditions));
            mWebView.loadUrl(TERMS_URL);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
