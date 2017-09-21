package com.KryptosTextApp.KryptosText.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.model.DriveItem;

public class DriveAcivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity implements View.OnClickListener{

    private Button mBtnMedia;
    private Button mBtnDocs;
    private boolean isChatActivity=false;

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive_acivity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        isChatActivity=getIntent().getBooleanExtra("isChatActivity",false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViews();
      /*  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }


    /**
     * Find the Views in the layout<br />
     */
    private void findViews() {
        mBtnMedia = (Button)findViewById( R.id.btn_media );
        mBtnDocs = (Button)findViewById( R.id.btn_docs );

        mBtnMedia.setOnClickListener( this );
        mBtnDocs.setOnClickListener( this );
    }

    /**
     * Handle button click events<br />
          */
    @Override
    public void onClick(View v) {
        if ( v == mBtnMedia ) {
            if(isChatActivity){
                Intent intent=new Intent(this, com.KryptosTextApp.KryptosText.activity.DriveMediaTabbedActivity.class);
                intent.putExtra("isChatActivity",isChatActivity);
                startActivityForResult(intent,REQUEST_CODE_DRIVE_MEDIA_DOC);
                return;
            }
            // Handle clicks for mBtnMedia
            startActivity(new Intent(this, com.KryptosTextApp.KryptosText.activity.DriveMediaTabbedActivity.class));
        } else if ( v == mBtnDocs ) {
            // Handle clicks for mBtnDocs
            if(isChatActivity){
                Intent intent=new Intent(this, com.KryptosTextApp.KryptosText.activity.DriveDocsTabbedActivity.class);
                intent.putExtra("isChatActivity",isChatActivity);
                startActivityForResult(intent,REQUEST_CODE_DRIVE_MEDIA_DOC);
                return;
            }
            startActivity(new Intent(this, com.KryptosTextApp.KryptosText.activity.DriveDocsTabbedActivity.class));

        }
    }
    private final int REQUEST_CODE_DRIVE_MEDIA_DOC = 51;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_DRIVE_MEDIA_DOC && resultCode == Activity.RESULT_OK ){
            DriveItem url=(DriveItem)data.getSerializableExtra("result");
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result",url);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }
//        if(requestCode == REQUEST_CODE_DRIVE_DOC && resultCode == Activity.RESULT_OK ){
//            DriveItem url=(DriveItem)data.getSerializableExtra("result");
//            Intent returnIntent = new Intent();
//            returnIntent.putExtra("result",url);
//            setResult(Activity.RESULT_OK,returnIntent);
//            finish();
//        }
    }
}
