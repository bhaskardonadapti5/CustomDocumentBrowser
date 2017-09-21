package com.KryptosTextApp.KryptosText.activity;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.dialog.NotificationDialog;
import com.KryptosTextApp.KryptosText.fragment.HomeFragment;
import com.KryptosTextApp.KryptosText.fragment.PHCContactFragment;
import com.KryptosTextApp.KryptosText.fragment.PHCGroupFragment;
import com.KryptosTextApp.KryptosText.model.GenericRespModel;
import com.KryptosTextApp.KryptosText.receiver.NetworkChangeReceiver;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;
import com.KryptosTextApp.KryptosText.utils.Log;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.gson.Gson;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.KryptosTextApp.KryptosText.fragment.PHCGroupFragment.GROUP_ADD_REQ_CODE;

public class MainDrawerActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    private static final String TAG = "MainDrawerActivity";
    private Context mContext;
    private TextView mToolbarTitle;
    //    private ImageView mComposeMessageImageView;
    private Toolbar mToolbar;
    private Handler mHandler;
    private BroadcastReceiver mReceiver;
    private BroadcastReceiver mReceiverNetworkChange;

    private TextView mTvUserName;
    private TextView mTvPrimaryFacName;
    private ImageView mImageView;

    private NavigationView mNavigationView;

    private RelativeLayout mNetworkBannerView;
    private ImageView mBtnCloseNetworkBanner;
    private FloatingActionButton fab;
    private TextView mAppVersion;
    private String mCurrentFragmentTag;
    private KeyguardManager mKeyguardManager;
    private NetworkChangeReceiver mNetworkChangeReceiver;
    private TabLayout tabLayout;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mContext = com.KryptosTextApp.KryptosText.activity.MainDrawerActivity.this;
        mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

//        getView();
        mHandler = new Handler();
        getView();
//        loadFragment(new PHCHomeFragment()); // To-DO Replace with your home fragment for chetu developed home screen
//        loadFragment(new HomeFragment());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            /*    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                Intent composeMessageIntent = new Intent(mContext, com.KryptosTextApp.KryptosText.activity.ComposeMessageActivity.class);
                startActivity(composeMessageIntent);
            }
        });
        if (getIntent() != null) {
            int fragmentNo = getIntent().getIntExtra("FragmentNo", 0);
            if (fragmentNo > 0) {
                if (mNavigationView != null)
                    mNavigationView.setCheckedItem(R.id.nav_groups);
                loadFragment(new PHCGroupFragment(), "GROUPS");
                updateToolBar(R.id.nav_groups);
            } else
                loadFragment(new HomeFragment(), "HOME");
        }

//        loadFragment(new HomeFragment());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        // prime era code
        //NetworkBanner visible/gone

        mBtnCloseNetworkBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNetworkBannerView.setVisibility(View.GONE);
            }
        });


//        mTvUserName.setText(getApplicationData().getFirstName() + " " + getApplicationData().getLastName());

        mNavigationView.setItemIconTintList(null);
        mNavigationView.setNavigationItemSelectedListener(this);
        displayVersionName();

//        startService(new Intent(this, PHCChatService.class));
        try {
            getApplicationData().connectWSS();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setListener();
//        setRepeatServiceEveryFifteenMinute();
//        setRepeatSocketServiceEveryTenSeconds();

    }

    private void loadUserInfo() {
        mTvUserName.setText(getApplicationData().getFirstName());
        mTvPrimaryFacName.setText(getApplicationData().getFacilityName());
        refreshProfileImage();
    }


    private void setListener() {
        /*mComposeMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent composeMessageIntent = new Intent(mContext, PHCComposeMessage.class);
                startActivity(composeMessageIntent);
            }
        });*/
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int selectedTabPosition = tabLayout.getSelectedTabPosition();
                if (selectedTabPosition == 0) {
                    updateToolBar(R.id.nav_home);
                    loadFragment(new HomeFragment(), "HOME");
                } else if (selectedTabPosition == 1) {
                    updateToolBar(R.id.nav_contacts);
                    loadFragment(new PHCContactFragment(), "CONTACTS");
                } else if (selectedTabPosition == 2) {
                    updateToolBar(R.id.nav_groups);
                    loadFragment(new PHCGroupFragment(), "GROUPS");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    private void getView() {
//        mComposeMessageImageView = (ImageView) mToolbar.findViewById(R.id.composeGroup);
        mToolbarTitle = (TextView) mToolbar.findViewById(R.id.toolbarTitle);
        mToolbarTitle.setVisibility(View.VISIBLE);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = mNavigationView.getHeaderView(0);
        mAppVersion = (TextView) mNavigationView.findViewById(R.id.app_version);
        mTvUserName = (TextView) header.findViewById(R.id.tv_user_name);
        mTvPrimaryFacName = (TextView) header.findViewById(R.id.tv_primary_fac_name);
        mImageView = (ImageView) header.findViewById(R.id.iv_profile);
        mNetworkBannerView = (RelativeLayout) findViewById(R.id.rl_network_banner);
        mBtnCloseNetworkBanner = (ImageView) findViewById(R.id.btn_close_network_banner);
        fab = (FloatingActionButton) findViewById(R.id.fab_compose_msg);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSettingsActivity();
            }
        });
       /* mToolbarTitle = (TextView) mToolbar.findViewById(R.id.toolbarTitle);
        mComposeMessageImageView = (ImageView) mToolbar.findViewById(R.id.composeGroup);

        View hView =  mNavigationView.getHeaderView(0);
        mUserImageView = (ImageView)hView.findViewById(R.id.userImageView);
        mUserNametext = (TextView) hView.findViewById(R.id.userNametext);
        mToolbarTitle.setVisibility(View.VISIBLE);
        mComposeMessageImageView.setVisibility(View.VISIBLE);
        mUserNametext.setText(getApplicationData().getFirstName() +" "+getApplicationData().getLastName());
        if(getApplicationData().getProfileUrl() != null && !getApplicationData().getProfileUrl().equalsIgnoreCase("")) {
            Glide.with(mContext).load(getApplicationData().getProfileUrl())
                    .thumbnail(0.5f)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(mUserImageView);
        }else{
            mUserImageView.setImageDrawable(mContext.getDrawable(R.drawable.img_user));
        }*/
    }

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        MaterialSearchView meMaterialSearchView = null;
        switch (mCurrentFragmentTag) {
            case "HOME":
                HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("HOME");
                meMaterialSearchView = homeFragment.getMeterialSearchView();
                break;
            case "CONTACTS":
                PHCContactFragment contactFragment = (PHCContactFragment) getSupportFragmentManager().findFragmentByTag("CONTACTS");
                meMaterialSearchView = contactFragment.getMeterialSearchView();
                break;
            case "GROUPS":
                PHCGroupFragment groupFragment = (PHCGroupFragment) getSupportFragmentManager().findFragmentByTag("GROUPS");
                meMaterialSearchView = groupFragment.getMeterialSearchView();
                break;
        }
        if (meMaterialSearchView.isSearchOpen()) {
            meMaterialSearchView.closeSearch();
        } else {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                if (doubleBackToExitPressedOnce) {
                    if (getApplicationData().getWebSocketInstance().isOpen())
                        getApplicationData().getWebSocketInstance().disconnect();
                    super.onBackPressed();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                showToast("Please click BACK again to exit");
//                Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);

            }
        }

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            updateToolBar(id);

//            loadFragment(new HomeFragment()); // To-DO Replace with your home fragment for chetu developed home screen
//            loadFragment(new PHCHomeFragment());
            // Handle the camera action


//            loadFragment(new HomeFragment()); // To-DO Replace with your home fragment for chetu developed home screen
            loadFragment(new HomeFragment(), "HOME");


        } else if (id == R.id.nav_contacts) {
            updateToolBar(id);
            loadFragment(new PHCContactFragment(), "CONTACTS");
        } else if (id == R.id.nav_groups) {
            updateToolBar(id);
            loadFragment(new PHCGroupFragment(), "GROUPS");
        } else if (id == R.id.nav_drive) {
//            updateToolBar(id);
//            loadFragment(new PHCDriveFragment());
            startActivity(new Intent(this, DriveAcivity.class));
        } else if (id == R.id.nav_settings) {
//            updateToolBar(id);
            navigateToSettingsActivity();
        } else if (id == R.id.nav_signout) {
            showLogoutAlert();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void navigateToSettingsActivity() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void showLogoutAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to logout?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (isNetworkAvailable()) {
                            showProgress();
                            dialog.dismiss();
                            logoutRetrofit();
                        } else {
                            showNoNetworkToast();
                        }

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void logoutRetrofit() {
        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<GenericRespModel> call = apiService.logout(getApplicationData().getAuthToken(), getPrefManager().getEmailID());

        call.enqueue(new Callback<GenericRespModel>() {
            @Override
            public void onResponse(Call<GenericRespModel> call, Response<GenericRespModel> response) {
                dismissProgress();
                Log.d(TAG, "logoutRetrofit URL " + response.raw().request().url());
                Log.d(TAG, "logoutRetrofit Resp " + new Gson().toJson(response.body()));
                GenericRespModel genericRespModel = response.body();
                if (genericRespModel == null) {
                    showAlert(getResources().getString(R.string.somethig_went_wrong));
                    return;
                }
                if (isSessionInvalid(genericRespModel.getStatusCode() ,genericRespModel.getStatusMessage())) {
                    return;
                }
                if (genericRespModel.getStatusCode() == 200) {
                    try {
                        getPrefManager().logoutUser();
                        getApplicationData().getWebSocketInstance().disconnect();
                        finish();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    mHomeList.setAdapter(mHomeAdapter);
                } else {
                    showToast(genericRespModel.getStatusMessage());

                }
            }

            @Override
            public void onFailure(Call<GenericRespModel> call, Throwable t) {
                dismissProgress();
                showAlert(t.getMessage());
            }
        });
    }


    private void updateToolBar(int fragmentType) {
        switch (fragmentType) {
            case R.id.nav_home:
                mToolbarTitle.setText(getResources().getString(R.string.home));
//                mComposeMessageImageView.setVisibility(View.VISIBLE);
                break;
            case R.id.nav_contacts:
                mToolbarTitle.setText(getResources().getString(R.string.contact));
//                mComposeMessageImageView.setVisibility(View.GONE);
                break;
            case R.id.nav_groups:
                mToolbarTitle.setText(getResources().getString(R.string.groups));
//                mComposeMessageImageView.setVisibility(View.GONE);
                break;
            case R.id.nav_drive:
                mToolbarTitle.setText(getResources().getString(R.string.drive));
//                mComposeMessageImageView.setVisibility(View.GONE);
                break;
            case R.id.nav_settings:
                break;
        }

    }

    private void loadFragment(final Fragment fragment, final String TAG) {
        mCurrentFragmentTag = TAG;
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Bundle bundel = new Bundle();
//                bundel.putString(ARG_BEARER_TOKEN,mAccessToken);
                fragment.setArguments(bundel);

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }
    }

    /*@Override
    public void onFragmentInteraction(Uri uri) {

    }*/

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        loadUserInfo();
        IntentFilter intentFilter = new IntentFilter(
                "com.KryptosTextApp.KryptosText.MESSAGE");
        IntentFilter networkChangeIntent = new IntentFilter(
                "com.KryptosTextApp.KryptosText.NetworkMoniter");

        IntentFilter CONNECTIVITY_CHANGE = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        if (mNetworkChangeReceiver == null)
            mNetworkChangeReceiver = new NetworkChangeReceiver();
        if (mNetworkChangeReceiver != null)
            registerReceiver(mNetworkChangeReceiver, CONNECTIVITY_CHANGE);

        if (mReceiver == null) {

            mReceiver = new BroadcastReceiver() {

                @SuppressLint("WrongConstant")
                @Override
                public void onReceive(Context context, Intent intent) {
                    //extract our message from intent
                    boolean isMessageReceived = intent.getBooleanExtra("IsMessageReceived", false);
                    JSONObject messageObject;
                    try {
                        if (isMessageReceived) {
                            String message = intent.getStringExtra("some_msg");
                            messageObject = new JSONObject(message);
                            if (messageObject.optInt("StatusCode") == 1 && messageObject.has("CaseId")) {

                                if (messageObject.getInt("CaseId") == 101 || messageObject.getInt("CaseId") == 105 || messageObject.getInt("CaseId") != 103 && !messageObject.optBoolean("IsRecalled") && messageObject.getInt("CaseId") == 102 && messageObject.getBoolean("IsUrgent")) {

                                    if (!messageObject.getBoolean("IsSender") && !messageObject.getString("GroupId").equals(String.valueOf(PHCChatActivity.sChatConversationID)) || getApplicationData().isActivityPaused()) {
                                        if (mKeyguardManager.inKeyguardRestrictedInputMode()) {
                                            String messageBody;
                                            //it is locked
                                            Intent mainDrawer = new Intent(com.KryptosTextApp.KryptosText.activity.MainDrawerActivity.this, PHCSplashScreenActivity.class);
                                            mainDrawer.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            PendingIntent pendingIntent = PendingIntent.getActivity(com.KryptosTextApp.KryptosText.activity.MainDrawerActivity.this, 0, mainDrawer,
                                                    PendingIntent.FLAG_ONE_SHOT);

                                            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
                                            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                notificationBuilder.setSmallIcon(R.drawable.ic_notify_1);
                                            } else {
                                                notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                                            }
                                            if (messageObject.getBoolean("IsUrgent")) {
                                                messageBody = "New Urgent Message Received";
                                            } else {
                                                messageBody = "New Message Received";
                                            }

                                            notificationBuilder.setContentTitle("KryptosText")
                                                    .setContentText(messageBody)
                                                    .setAutoCancel(true)
                                                    .setSound(defaultSoundUri)
                                                    .setContentIntent(pendingIntent);

                                            NotificationManager notificationManager =
                                                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                            if (messageObject.getBoolean("IsUrgent")) {
                                                notificationBuilder.setColor(ContextCompat.getColor(context, R.color.colorRed));
                                                String styledText = "<font color='red'>" + messageBody + "</font>";

                                                notificationBuilder.setContentText(Html.fromHtml(messageObject.getString("UserName") + " " + styledText));

                                            }
                                            notificationManager.notify(0, notificationBuilder.build());

                                        } else {
                                            //it is not locked
                                            NotificationDialog.showNotification(mContext, messageObject);
                                            Log.d(TAG, "Push from MAin screen");

                                        }
                                    }
                                }


                            }

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };


        }

        mReceiverNetworkChange = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                Log.d(TAG, "mReceiverNetworkChange onReceive()");

                Boolean isNetworkGone = intent.getBooleanExtra("isNetworkGone", false);
                Log.d(TAG, "isNetworkGone " + isNetworkGone);
                if (isNetworkGone) {
                    mNetworkBannerView.setVisibility(View.VISIBLE);
                } else {
                    mNetworkBannerView.setVisibility(View.GONE);

                }

            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);

        //registering our receiver
//        this.registerReceiver(mReceiver, intentFilter);


        this.registerReceiver(mReceiverNetworkChange, networkChangeIntent);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        //unregister our receiver
        this.unregisterReceiver(this.mReceiverNetworkChange);
    }


    @Override
    protected void onDestroy() {
//        stopService(new Intent(this, PHCChatService.class));
//        alarmManager.cancel(pendingIntent);
        Log.d(TAG, " onDestroy");
        this.unregisterReceiver(this.mNetworkChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mReceiver);
        super.onDestroy();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"onActivityResult...");
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case GROUP_ADD_REQ_CODE:
            switch (mCurrentFragmentTag) {
                case "GROUPS":
                    PHCGroupFragment groupFragment = (PHCGroupFragment) getSupportFragmentManager().findFragmentByTag("GROUPS");
                    groupFragment.onActivityResult(requestCode,resultCode,data);
                    break;
                default:
            }
                break;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void refreshProfileImage() {

        if (getApplicationData().getProfileUrl() != null && !getApplicationData().getProfileUrl().equalsIgnoreCase("")) {
            Log.d(TAG, "imageUrl " + getApplicationData().getProfileUrl());
            Glide.with(getContext()).load(getApplicationData().getProfileUrl()).asBitmap()
                    .centerCrop()
                    .placeholder(R.drawable.ic_deafult_user)

                    .into(new BitmapImageViewTarget(mImageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            mImageView.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        } else {
            mImageView.setImageDrawable(mContext.getDrawable(R.drawable.ic_deafult_user));
        }
    }

    private void displayVersionName() {

        String versionName = "";
        PackageInfo packageInfo;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = "Version " + packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mAppVersion.setText(versionName);
    }


}
