package com.KryptosTextApp.KryptosText.activity;

import android.os.Bundle;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.fragment.ImageFragment;
import com.KryptosTextApp.KryptosText.model.DriveItem;

public class ImageViewerActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity {

    private DriveItem mDriveItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_viewer);
        mDriveItem = (DriveItem) getIntent().getSerializableExtra("DriveItemObj");

        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.frame, ImageFragment.newInstance(mDriveItem), "ImageViewer");
        fragmentTransaction.commitAllowingStateLoss();

    }
}
