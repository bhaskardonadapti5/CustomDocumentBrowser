package com.KryptosTextApp.KryptosText.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.KryptosTextApp.KryptosText.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

public class ViewImageActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity {
    private ImageView ivProfile;
    private String profileURl;
    private String profileName;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        ivProfile = (ImageView) findViewById(R.id.iv_profile);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        profileURl = getIntent().getStringExtra("profile");
        profileName = getIntent().getStringExtra("profileName");
        Context mContext = com.KryptosTextApp.KryptosText.activity.ViewImageActivity.this;
        updateToolbar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Glide.with(mContext).load(profileURl).asBitmap()
                .placeholder(R.drawable.img_default_group)
                .into(new BitmapImageViewTarget(ivProfile) {
                    @Override
                    protected void setResource(Bitmap resource) {
//                        RoundedBitmapDrawable circularBitmapDrawable =
//                                RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
//                        circularBitmapDrawable.setCircular(true);
//                        ivProfile.setImageDrawable(circularBitmapDrawable);
                        ivProfile.setImageBitmap(resource);
                    }
                });
    }

    private void updateToolbar() {
        toolbar.setTitle(profileName);
        setSupportActionBar(toolbar);
    }

}
