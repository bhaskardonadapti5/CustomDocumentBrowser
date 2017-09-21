package com.KryptosTextApp.KryptosText.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.constant.PHCAppConstant;
import com.KryptosTextApp.KryptosText.model.DriveItem;
import com.KryptosTextApp.KryptosText.model.PHCChatModel;
import com.KryptosTextApp.KryptosText.utils.Log;
import com.KryptosTextApp.KryptosText.utils.PHCDateFormatter;
import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

/**
 * user :  avinashj
 * date : 5/4/2017
 * description :
 */

public class PHCVedioPlayActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity implements EasyVideoCallback {

    private static final String TAG = "PHCVedioPlayActivity";

    private Context mContext;
    //    private VideoView mVideoView;
    private EasyVideoPlayer mEasyVideoPlayer;
    private PHCChatModel model;
    private DriveItem mDriveItem;
    private Toolbar mToolBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.phc_video_activity);
        mContext = com.KryptosTextApp.KryptosText.activity.PHCVedioPlayActivity.this;
        getView();
        model = (PHCChatModel) getIntent().getSerializableExtra("modelData");
        mDriveItem = (DriveItem) getIntent().getSerializableExtra("DriveItemObj");
//        toolBarCreation();
        byte[] byteData;
        if (model == null) {
            byteData = Base64.decode(mDriveItem.getMessageURL(), Base64.DEFAULT);
        } else {
            byteData = Base64.decode(model.getMessageText(), Base64.DEFAULT);
        }

        try {
            String mVideoPath = new String(byteData, "UTF-8");
            Log.d(TAG, "url decoded " + mVideoPath);
            playVideo(mVideoPath);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mToolBar.setVisibility(View.GONE);
    }

    private void toolBarCreation() {
        Uri uri = Uri.parse(model.getThumbnailURL());
        mToolBar.setTitle(new File(String.valueOf(uri)).getName());
        setSupportActionBar(mToolBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
//        mToolBar.setTitle("");
        mToolBar.setTitle(model.getUserName());
        try {
            mToolBar.setSubtitle(PHCDateFormatter.getChatDateFromserverDate(model.getCreatedDate()));
        } catch (ParseException e) {
            e.printStackTrace();
            mToolBar.setSubtitle(model.getCreatedDate());

        }
        setSupportActionBar(mToolBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void getView() {
        mEasyVideoPlayer = (EasyVideoPlayer) findViewById(R.id.videoView);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
    }

    private void playVideo(String videopath) {
        Log.d(TAG, videopath);
        // Defaults to false. Immediately starts playback when the player becomes prepared.
        mEasyVideoPlayer.setAutoPlay(true);

        mEasyVideoPlayer.setAutoFullscreen(true);

       /* Uri uri = Uri.parse(videopath);
        mVideoView.setVideoURI(uri);

        MediaController mediaController = new
                MediaController(mContext);

        mediaController.setAnchorView(mVideoView);
        mVideoView.setMediaController(mediaController);

        mVideoView.start();*/
        // Sets the callback to this Activity, since it inherits EasyVideoCallback
        mEasyVideoPlayer.setCallback(this);

        // Sets the source to the HTTP URL held in the TEST_URL variable.
        // To play files, you can use Uri.fromFile(new File("..."))

        videopath = videopath.replace(PHCAppConstant.SERVER_HOST + "/KPTContent/", "");
        Uri build = Uri.parse(PHCAppConstant.DOCUMENT_VIEW_URL).buildUpon().appendQueryParameter("MediaURL", videopath).appendQueryParameter("KTtoken", getApplicationData().getAuthToken()).appendQueryParameter("Platform", "a").build();
        mEasyVideoPlayer.setSource(build);


    }

    @Override
    public void onPause() {
        super.onPause();
        // Make sure the player stops playing if the user presses the home button.
        mEasyVideoPlayer.pause();
    }

    @Override
    public void onStarted(EasyVideoPlayer player) {

    }

    @Override
    public void onPaused(EasyVideoPlayer player) {

    }

    @Override
    public void onPreparing(EasyVideoPlayer player) {

    }

    @Override
    public void onPrepared(EasyVideoPlayer player) {

    }

    @Override
    public void onBuffering(int percent) {

    }

    @Override
    public void onError(EasyVideoPlayer player, Exception e) {

    }

    @Override
    public void onCompletion(EasyVideoPlayer player) {

    }

    @Override
    public void onRetry(EasyVideoPlayer player, Uri source) {

    }

    @Override
    public void onSubmit(EasyVideoPlayer player, Uri source) {

    }
}
