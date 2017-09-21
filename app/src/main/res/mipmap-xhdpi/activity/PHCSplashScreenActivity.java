package com.KryptosTextApp.KryptosText.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.utils.Log;

/**
 * user :  avinashj
 * date : 1/19/2017
 * description :
 */

public class PHCSplashScreenActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity {

    private static final String TAG = "PHCSplashScreenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phc_splash_layout);
        Log.d(TAG, "Token " + getPrefManager().getDeviceToken());
//        ShortcutBadger.removeCount(this);

        try {
//            RootBeer rootBeer = new RootBeer(this);
//            if (rootBeer.isRooted()) {
//                //we found indication of root
//                showRootAlert();
//            } else {
            //we didn't find indication of root
            launchMainScreen();

//            }
        } catch (Exception e) {
            e.printStackTrace();
//            launchMainScreen();
        }


//        }
    }

    void showRootAlert() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.app_name))
                .setMessage("App cant be opened on rooted device")
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                }).create().show();

    }


    private void launchMainScreen() {
        int SPLASH_TIME_OUT = 2000;
        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {
                // This method will be executed once the timer is over
//                PHCAesHelper.encryptUsingDummyRSA();
                Intent intent = new Intent(com.KryptosTextApp.KryptosText.activity.PHCSplashScreenActivity.this, getPrefManager().getMPin() != null && !getPrefManager().getMPin().equals("")
                        ? FingerprintAuthenticationActivity.class : com.KryptosTextApp.KryptosText.activity.LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}