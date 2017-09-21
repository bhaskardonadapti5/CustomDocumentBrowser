package com.KryptosTextApp.KryptosText.activity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.constant.MediaTypeConst;
import com.KryptosTextApp.KryptosText.constant.PHCAppConstant;
import com.KryptosTextApp.KryptosText.utils.Log;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.shockwave.pdfium.PdfPasswordException;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.KryptosTextApp.KryptosText.R.id.pdfView;
import static com.KryptosTextApp.KryptosText.R.id.webview;

//implements  DownloadFile.Listener

public class MediaDocWebView extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity {

    private static final String TAG = "MediaDocWebView";
    private WebView mWebView;
    private TextView mtvTextView;
    private PDFView mPdfView;
    private String mMediaURL;
    private String mDocType;
    /*RemotePDFViewPager remotePDFViewPager;
    PDFPagerAdapter adapter;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_doc_web_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMediaURL = getIntent().getStringExtra("MEDIA_URL");
        mDocType = getIntent().getStringExtra("MEDIA_TYPE");
        setTitle(getFileNameWithoutMillis(getFileName(mMediaURL)));
        mWebView = (WebView) findViewById(webview);
        mPdfView = (PDFView) findViewById(pdfView);
        mtvTextView = (TextView) findViewById(R.id.tv_text);

        Log.d(TAG, "mMediaURL " + mMediaURL);
        if (mDocType.equals(MediaTypeConst.PDF) || mDocType.equals(MediaTypeConst.TXT) || mDocType.equals(MediaTypeConst.CSV)) {
            mPdfView.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.GONE);
            if (isNetworkAvailable()) {
                showProgress();
                new DownloadAsycTask().execute(mMediaURL);
            } else {
                showNoNetworkToast();
            }

//            remotePDFViewPager = new RemotePDFViewPager(getContext(), mMediaURL, this);

        } else {
            mWebView.setWebViewClient(new MyBrowser());
            mWebView.setInitialScale(1);
            mWebView.getSettings().setLoadWithOverviewMode(true);
            mWebView.getSettings().setUseWideViewPort(true);
            mWebView.getSettings().setSupportZoom(true);
            mWebView.getSettings().setBuiltInZoomControls(true);
            mWebView.getSettings().setDisplayZoomControls(false);
            mWebView.getSettings().setJavaScriptEnabled(true);
//            mPdfView.setVisibility(View.GONE);
//            "http://view.officeapps.live.com/op/view.aspx?src=" +
            mWebView.setVisibility(View.VISIBLE);
            mMediaURL = mMediaURL.replace(PHCAppConstant.SERVER_HOST + "/KPTContent/", "");
            Map<String, String> headers = new HashMap<>();
            headers.put("MediaURL", mMediaURL);
            headers.put("KTtoken", getApplicationData().getAuthToken());
            headers.put("Platform", "a");
            headers.put("IsDocument", "True");

            mWebView.loadUrl(PHCAppConstant.DOCUMENT_VIEW_URL, headers);

        }

//        mWebView.loadUrl("https://docs.google.com/gview?embedded=true&url=" + mMediaURL);
//        mWebView.loadUrl("https://docs.google.com/viewer?embedded=true&url=" + mMediaURL);
        // For static testing of pdf
//        mWebView.loadUrl("http://drive.google.com/viewerng/viewer?embedded=true&url=" + mMediaURL);
//        mWebView.loadUrl(mMediaURL);
//        mWebView.loadData(doc, "text/html", "UTF-8");
//        mWebView.loadUrl(USER_MANUAL_URL);

    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        try {
            return super.onTouchEvent(me);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    private String getFileName(String fileName) {
        return fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
    }

    private String getFileNameWithoutMillis(String fileName) {
        if (fileName.contains("_")) {
            return fileName.substring(0, fileName.lastIndexOf("_"));
        } else {
            return fileName;
        }
    }

    /*@Override
    public void onSuccess(String url, String destinationPath) {
        // That's the positive case. PDF Download went fine
        dismissProgress();
        try {
            PDFPagerAdapter adapter = new PDFPagerAdapter(this, destinationPath);
            remotePDFViewPager.setAdapter(adapter);
            setContentView(remotePDFViewPager);
        } catch (SecurityException e) {
            e.printStackTrace();
            showToast("PDf protected by password");

        }catch (Exception e){
            e.printStackTrace();
            showToast("Error downloading file ");
        }
    }

    @Override
    public void onFailure(Exception e) {
        // This will be called if download fails
        dismissProgress();
        e.printStackTrace();
        showToast("Error downloading file ");
    }

    @Override
    public void onProgressUpdate(int progress, int total) {
        // You will get download progress here
        // Always on UI Thread so feel free to update your views here
        if (progress==100)
        dismissProgress();
    }*/

    class MyBrowser extends WebViewClient {
        ProgressDialog mProgressDialog;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d("MyBrowser", "onPageStarted ");
            if (mProgressDialog == null) {
                mProgressDialog = ProgressDialog.show(view.getContext(), view.getContext().getResources().getString(R.string.app_name), "Loading...");
                mProgressDialog.setCancelable(true);
            }


        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d("MyBrowser", "onPageFinished ");
            if (mProgressDialog != null & mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            try {
                view.loadUrl("javascript:(function() { " +
                        "document.getElementsByClassName('ndfHFb-c4YZDc-GSQQnc-LgbsSe ndfHFb-c4YZDc-to915-LgbsSe VIpgJd-TzA9Ye-eEGnhe ndfHFb-c4YZDc-LgbsSe')[0].style.display='none'; })()");
            } catch (Exception e) {
                Log.d("MyBrowser", "onPageFinished -- Exc: " + e.toString());
            }

        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            //Your code to do
            mProgressDialog.dismiss();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (error.getErrorCode() == ERROR_CONNECT) {
                    view.loadData("<center><br><br><h1>" + mDocType + " Failed to load</h1></br></br> <h1>ERR_CONNECTION_REFUSED</h1></center>", "text/html", "utf-8");
                }
                Log.d("WebViewClient", "error.getErrorCode() " + error.getErrorCode());
            }
//        Toast.makeText(view.getContext(), "Network Error " + error.getDescription() , Toast.LENGTH_LONG).show();
            if (mProgressDialog != null & mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
//        view.loadData("ERR_CONNECTION_REFUSED","text/html", "utf-8");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
       /* if (adapter!=null)
         adapter.close();*/
    }

    private class DownloadAsycTask extends AsyncTask<String, Void, byte[]> {


        @Override
        protected byte[] doInBackground(String... params) {
            byte[] bytes = null;
            InputStream inputStream = null;
            String pdfUrl = params[0];
            try {
                URL url = new URL(pdfUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                if (connection.getResponseCode() == 200) {
                    inputStream = new BufferedInputStream(connection.getInputStream());
                    bytes = IOUtils.toByteArray(inputStream);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bytes;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);

            Log.d(TAG, "onPostExecute ");
            if (bytes != null) {
                if (mDocType.equals(MediaTypeConst.PDF)) {
                    mPdfView.fromBytes(bytes)
                            .enableDoubletap(true)
                            .onError(new OnErrorListener() {
                                @Override
                                public void onError(Throwable t) {
                                    try {
                                        Log.d(TAG, "loadComplete " + t.getMessage());
                                        if (t.getClass().equals(PdfPasswordException.class)) {
                                            showToast("Password protected pdfs can't be opened");
                                        } else {
                                            showToast("Error loading pdf document");
                                        }
                                        dismissProgress();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        dismissProgress();
                                        showToast("Error loading pdf document");
                                    }
                                }
                            })
                            .onLoad(new OnLoadCompleteListener() {
                                @Override
                                public void loadComplete(int nbPages) {
                                    Log.d(TAG, "loadComplete " + nbPages);
                                    dismissProgress();
                                }
                            })
                            .load();
                } else if (mDocType.equals(MediaTypeConst.TXT)) {
                    mPdfView.setVisibility(View.GONE);
                    mWebView.setVisibility(View.GONE);
                    (findViewById(R.id.scroll)).setVisibility(View.VISIBLE);
                    mtvTextView.setText(new String(bytes));
                    dismissProgress();
                } else if (mDocType.equals(MediaTypeConst.CSV)) {
                    mPdfView.setVisibility(View.GONE);
                    mWebView.setVisibility(View.GONE);
                    (findViewById(R.id.scroll)).setVisibility(View.VISIBLE);
                    mtvTextView.setText(new String(bytes));
                    dismissProgress();
                }
            } else {
                dismissProgress();
                showToast("something went wrong with the server");
            }

        }

    }
}
