package com.KryptosTextApp.KryptosText.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Vibrator;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.database.PreferenceManager;
import com.KryptosTextApp.KryptosText.interfaces.OnMpinCompletedListener;
import com.KryptosTextApp.KryptosText.model.GenericRespModel;
import com.KryptosTextApp.KryptosText.model.PHCLoginResponseData;
import com.KryptosTextApp.KryptosText.model.PHCLoginResponseModel;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;
import com.KryptosTextApp.KryptosText.utils.CustomMPinEditText;
import com.KryptosTextApp.KryptosText.utils.Log;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.gson.Gson;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.hardware.fingerprint.FingerprintManager.FINGERPRINT_ERROR_CANCELED;
import static com.KryptosTextApp.KryptosText.R.id.btn_log_out;
import static com.KryptosTextApp.KryptosText.R.id.tv_user_name;
import static com.KryptosTextApp.KryptosText.constant.PHCAppConstant.FINGERPRINT_AUTH_KEY_NAME;

public class FingerprintAuthenticationActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity implements View.OnClickListener {

    private static final String TAG = "FingerprintAuthActivity";
    private static final int FINGER_PRINT_PERMISSION_CONSTANT = 1;
    private static final int SECURITY_VALID = 100;
    private Button mBtnUseTouchId;
    private Button mBtnEnterMPin;
    private Button mBtnLogout;
    private ImageView mUserProfileImage;
    private Button mBtnForgotMpin;
    private CustomMPinEditText mEtMpin;
    private KeyguardManager keyguardManager;
    private FingerprintManager fingerprintManager;
    private FingerprintHandler mFingerprintHandler;
    private KeyStore keyStore;
    // Variable used for storing the key in the Android Keystore container
    private Cipher cipher;
    private final int TYPE_MPIN = 1;
    private final int TYPE_FINGERPRINT = 2;
    private final int MAX_ATTEMPTS = 5;

    private boolean mIsTouchIDEnable;
    private AlertDialog mAlertDialog;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint_authentication);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findViews();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Initializing both Android Keyguard Manager and Fingerprint Manager
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            // Check whether the device has a Fingerprint sensor.
            if (!fingerprintManager.isHardwareDetected()) {
                /**
                 * An error message will be displayed if the device does not contain the fingerprint hardware.
                 * However if you plan to implement a default authentication method,
                 * you can redirect the user to a default authentication activity from here.
                 * Example:
                 * Intent intent = new Intent(this, DefaultAuthenticationActivity.class);
                 * startActivity(intent);
                 */
//                showToast("Your Device does not have a Fingerprint Sensor");
                launchEnterMPin();
                mBtnUseTouchId.setVisibility(View.GONE);
                mBtnEnterMPin.setVisibility(View.GONE);
                return;
            } else {
                // Checks whether fingerprint permission is set on manifest
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                    showToast("Fingerprint authentication permission not enabled");
                    showPermission();
                } else {
                    // Check whether at least one fingerprint is registered
                    if (!fingerprintManager.hasEnrolledFingerprints()) {
                        showToast("Register at least one fingerprint in Settings");
                    } else {
                        // Checks whether lock screen security is enabled or not
                        if (!keyguardManager.isKeyguardSecure()) {
                            showToast("Lock screen security not enabled in Settings");
                        } else {
                            mIsTouchIDEnable = true;
                            generateKey();
                        /*if (cipherInit()) {
                            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                            mFingerprintHandler = new FingerprintHandler(this);
                            mFingerprintHandler.startAuth(fingerprintManager, cryptoObject);
                        }*/
                        }
                    }
                }
            }
        } else {
            launchEnterMPin();
            mBtnUseTouchId.setVisibility(View.GONE);
            mBtnEnterMPin.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        mFingerprintHandler.startAuth(mCryptoObject);
    }


    @Override
    public void onPause() {
        super.onPause();
//        mFingerprintHandler.stopListening();
    }


    private void showPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_FINGERPRINT}, FINGER_PRINT_PERMISSION_CONSTANT);
    }

    private void launchEnterMPin() {
        mUserProfileImage.setVisibility(View.GONE);
        mBtnForgotMpin.setVisibility(View.VISIBLE);
        mEtMpin.setVisibility(View.VISIBLE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        mEtMpin.requestFocus();
    }


    private void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get KeyGenerator instance", e);
        }
        try {
            keyStore.load(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyGenerator.init(new
                        KeyGenParameterSpec.Builder(FINGERPRINT_AUTH_KEY_NAME,
                        KeyProperties.PURPOSE_ENCRYPT |
                                KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(
                                KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build());
            }
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }


    private boolean cipherInit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                throw new RuntimeException("Failed to get Cipher", e);
            }
            try {
                keyStore.load(null);
                SecretKey key = (SecretKey) keyStore.getKey(FINGERPRINT_AUTH_KEY_NAME,
                        null);
                cipher.init(Cipher.ENCRYPT_MODE, key);
                return true;
            } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException("Failed to init Cipher", e);
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }


    /**
     * Find the Views in the layout<br />
     */
    private void findViews() {
        mBtnUseTouchId = (Button) findViewById(R.id.btn_use_touch_id);
        mBtnEnterMPin = (Button) findViewById(R.id.btn_enter_mpin);
        mBtnLogout = (Button) findViewById(btn_log_out);
        mUserProfileImage = (ImageView) findViewById(R.id.iv_user_profile);
        TextView mUserName = (TextView) findViewById(tv_user_name);
        mEtMpin = (CustomMPinEditText) findViewById(R.id.et_mpin);
        mBtnForgotMpin = (Button) findViewById(R.id.btn_forgot_mpin);
        mBtnUseTouchId.setOnClickListener(this);
        mBtnEnterMPin.setOnClickListener(this);
        mBtnLogout.setOnClickListener(this);
        mBtnForgotMpin.setOnClickListener(this);
        mEtMpin.setOnCompletedListener(new OnMpinCompletedListener() {
            @Override
            public void onMPinCompleted() {
                if (isNetworkAvailable()) {
                    showProgress();
                    loginRetroFit(TYPE_MPIN);
                } else {
                    showNoNetworkAlert();
                }
            }
        });

        try {
            Log.d(TAG, "getPrefManager().getUserProfileURL() " + getPrefManager().getUserProfileURL());
            mUserName.setText(String.format("Welcome %s", getPrefManager().getUserName()));
            if (getPrefManager().getUserProfileURL() != null && !getPrefManager().getUserProfileURL().equalsIgnoreCase("")) {
                Glide.with(getContext()).load(getPrefManager().getUserProfileURL()).asBitmap()
                        .centerCrop()
                        .placeholder(R.drawable.ic_deafult_user)

                        .into(new BitmapImageViewTarget(mUserProfileImage) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                                circularBitmapDrawable.setCircular(true);
                                mUserProfileImage.setImageDrawable(circularBitmapDrawable);
                            }
                        });
            } else {
                mUserProfileImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_deafult_user));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * Handle button click events<br />
     * <br />
     */
    @Override
    public void onClick(View v) {
        if (v == mBtnUseTouchId) {
            // Handle clicks for mBtnLogin
            if (mIsTouchIDEnable) {
                if (cipherInit()) {
                    FingerprintManager.CryptoObject cryptoObject = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        cryptoObject = new FingerprintManager.CryptoObject(cipher);
                    }
                    mFingerprintHandler = new FingerprintHandler(this);
                    mFingerprintHandler.startAuth(fingerprintManager, cryptoObject);
                }
                showFingerPrintDialog();
            } else {
                showAlert("Touch ID not configured for this device");
            }
        } else if (v == mBtnEnterMPin) {
            launchEnterMPin();
        } else if (v == mBtnLogout) {
            // Handle clicks for mBtnForgotMpin
            showLogoutAlert();
        } else if (v == mBtnForgotMpin) {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            intent.putExtra("IsFromForgotMPin", true);
            startActivity(intent);
        }
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
                if (genericRespModel.getStatusCode() == 401) {
                    showSessionExpireAlert(genericRespModel.getStatusMessage(),genericRespModel.getStatusCode());
                    return;
                }
                if (genericRespModel.getStatusCode() == 200) {
                    try {
                        getPrefManager().logoutUser();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINGER_PRINT_PERMISSION_CONSTANT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showFingerPrintDialog();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!fingerprintManager.hasEnrolledFingerprints()) {
                        showAlert("Register at least one fingerprint in Settings");
                    } else {
                        // Checks whether lock screen security is enabled or not
                        if (!keyguardManager.isKeyguardSecure()) {
                            showToast("Lock screen security not enabled in Settings");
                        } else {
                            generateKey();

                            if (cipherInit()) {
                                FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                                FingerprintHandler helper = new FingerprintHandler(this);
                                helper.startAuth(fingerprintManager, cryptoObject);
                            }
                        }
                    }
                }
            }

        }/*else if(requestCode == STORAGE_PERMISSION_CAMERA_CONSTANT){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                goTOGallery(true);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CAMERA_CONSTANT);
            }

        }*/


    }

    private void showFingerPrintDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        builder.setCancelable(false);

//        builder.setIcon(R.drawable.galleryalart);
        View view = inflater.inflate(R.layout.touch_id_auth_alert_dialog, null);
        builder.setView(view);
//        text.setText(successMsg);
        mAlertDialog = builder.create();

        Button dialogButton = (Button) view.findViewById(R.id.btn_ok);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
                mFingerprintHandler.stopListening();
                /*if(intent !=null){
                    startActivity((intent));
                }
                ((AppCompatActivity) mContext).finish();*/
            }
        });

        mAlertDialog.show();
    }

    @SuppressLint("NewApi")
    public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {


        private final Context context;
        CancellationSignal mCancellationSignal;

        // Constructor
        public FingerprintHandler(Context mContext) {
            context = mContext;
        }

        public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
            mCancellationSignal = new CancellationSignal();
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.authenticate(cryptoObject, mCancellationSignal, 0, this, null);
            }
        }

        public void stopListening() {
            if (mCancellationSignal != null) {
                mCancellationSignal.cancel();
                mCancellationSignal = null;
            }
        }


        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
            this.update("Fingerprint Authentication error\n" + errString, false);
            if (errMsgId != FINGERPRINT_ERROR_CANCELED) {
                // As you see below that error code happens when device is locked.
            }
        }


        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            this.update("Fingerprint Authentication help\n" + helpString, false);
        }


        @Override
        public void onAuthenticationFailed() {
            this.update("Fingerprint Authentication failed.", false);
            if (mAlertDialog != null)
                ((ViewGroup) mAlertDialog.getWindow().getDecorView())
                        .getChildAt(0).startAnimation(AnimationUtils.loadAnimation(
                        context, R.anim.shake));
            vibrate(600);
        }


        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            this.update("Fingerprint Authentication succeeded.", true);
        }


        public void update(String e, Boolean success) {
          /*  TextView textView = (TextView) ((Activity)context).findViewById(R.id.errorText);
            textView.setText(e);
*/
            showToast(e);
            if (success) {
//                textView.setTextColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));
                if (mAlertDialog != null)
                    mAlertDialog.dismiss();
                if (isNetworkAvailable()) {
                    showProgress("Logging in..");
                    loginRetroFit(TYPE_FINGERPRINT);
                } else {
                    showNoNetworkAlert();
                }

            }
        }
    }

    private String encryptedMPin;

    private void loginRetroFit(final int loginType) {
        try {
            PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);

            if (loginType == TYPE_MPIN) {
                encryptedMPin = getPrefManager().encryptWithRSA(mEtMpin.getText(), getPrefManager().getPK());
//                encryptedMPin = getPrefManager().encrypt(mEtMpin.getText());
            } else
                encryptedMPin = getPrefManager().getMPin();
            Call<PHCLoginResponseModel> call = apiService.validateLoginMPIN(getPrefManager().getDeviceToken(),getAppVersion(), "a", getPrefManager().getEmailID(), encryptedMPin);
            call.enqueue(new Callback<PHCLoginResponseModel>() {
                @Override
                public void onResponse(Call<PHCLoginResponseModel> call, Response<PHCLoginResponseModel> response) {
                    Log.d(TAG, " loginRetroFit Url " + response.raw().request().url());
                    Log.d(TAG, " loginRetroFit Resp " + new Gson().toJson(response.body()));
                    Log.d(TAG, " Device token ID " + getPrefManager().getDeviceToken());

                    dismissProgress();
                    PHCLoginResponseModel loginResponseModel = response.body();
                    if (loginResponseModel != null) {
                        if (loginResponseModel.getStatusCode() == 200) {
                            PHCLoginResponseData data = loginResponseModel.getData();
                            if (data.isAndroidAppUpdate()) {
                                if (data.isAndroidForceUpgrade()) {
                                    showAppUpgradeAlert();
                                    return;
                                } else {
                                    showToast(getString(R.string.app_recom_upgrade_alert_msg));
                                }
                            }
                            if (loginResponseModel.getAttempts() > MAX_ATTEMPTS) {
                                Intent intent = new Intent(getContext(), ForgotPasswordActivity.class);
                                intent.putExtra("isSecurity", true);
                                intent.putExtra("loginResponse", data);
                                startActivityForResult(intent, SECURITY_VALID);
                            } else {
                                onSuccessfulLogin(data);
                            }
                        } else if (loginResponseModel.getStatusCode() == 401) {
                            showFailAlert("Please login with OTP");
                            getPrefManager().logoutUser();
                            finish();
                        } else {
                            dismissProgress();
                            if (loginType == TYPE_MPIN) {
                                showFailAlert(loginResponseModel.getStatusMessage());
                                if (mEtMpin.getVisibility() == View.VISIBLE)
                                    mEtMpin.clearText();
                            } else if (loginType == TYPE_FINGERPRINT) {
                                showFailAlert("Please Enter Updated Mpin");
                                launchEnterMPin();
                            }
                        }
                    } else {
                        showToast(getContext().getString(R.string.somethig_went_wrong));
                    }

                }

                @Override
                public void onFailure(Call<PHCLoginResponseModel> call, Throwable t) {
                    dismissProgress();
                    showAlert(getContext().getString(R.string.somethig_went_wrong));
                    if (t != null && t.getMessage() != null) {
                        showToast(t.getMessage());

                        Log.e(TAG, t.getMessage());
                    }


                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SECURITY_VALID:
                if (resultCode == RESULT_OK) {
                    PHCLoginResponseData loginResponseData = (PHCLoginResponseData) data.getSerializableExtra("loginResponse");
                    onSuccessfulLogin(loginResponseData);
                } else {
                    showFailAlert("Something wrong at security question. Please contact administrator");
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }


    }

    private void onSuccessfulLogin(PHCLoginResponseData data) {
        if (isAppUpdateAvailable(data)) {
            if (data.isAndroidForceUpgrade()) {
                showAppUpgradeAlert();
                return;
            } else {
                showToast(getString(R.string.app_recom_upgrade_alert_msg));
            }
        }
        // showToast(loginResponseModel.getStatusMessage());
        setLoginRespDataToPreferences(data);
        setLoginRespData(data);
        if (data.isInitialLogin()) {
            if (!data.isTermsAccepted()) {
                Intent intent = new Intent(getContext(), TermsAndCondActivity.class);
                startActivity(intent);
                finish();
            } else if (!data.isPasswordUpdated()) {
                Intent updatePass = new Intent(getContext(), ResetUpdatePasswordActivity.class);
                updatePass.putExtra("FROM_LOGIN", true);
                startActivity(updatePass);
                finish();
            } else if (!data.isMpinCreated()) {
                Intent updatePass = new Intent(getContext(), com.KryptosTextApp.KryptosText.activity.CreateMPin.class);
                startActivity(updatePass);
                finish();
            } else if (!data.isSecQuestEnabled()) {
                Intent updatePass = new Intent(getContext(), com.KryptosTextApp.KryptosText.activity.SecurityQuestionsActivity.class);
                startActivity(updatePass);
                finish();
            }

        } else {
            Intent intent = new Intent(getContext(), com.KryptosTextApp.KryptosText.activity.MainDrawerActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void setLoginRespData(PHCLoginResponseData data) {
        getApplicationData().setAuthToken(data.getToken());
        getApplicationData().setEmailID(data.getEmailId());
        getApplicationData().setProfileUrl(data.getProfileURL());
        getApplicationData().setUserID(data.getUserId());
        getApplicationData().setFirstName(data.getFirstName());
        getApplicationData().setLastName(data.getLastName());
        getApplicationData().setContactNo(data.getContactNumber());
        getApplicationData().setAboutUser(data.getNotes());
        getApplicationData().setFacilityName(data.getFacilityName());

    }

    private void setLoginRespDataToPreferences(PHCLoginResponseData data) {
//        getPrefManager().setToken(PreferenceManager.encrypt(data.getToken()));
        getPrefManager().setUserName(PreferenceManager.encrypt(data.getFirstName() + " " + data.getLastName()));
        getPrefManager().setUserProfileUrl(PreferenceManager.encrypt(data.getProfileURL()));
        getPrefManager().setEmailID(PreferenceManager.encrypt(data.getEmailId()));
        if (encryptedMPin != null && !encryptedMPin.equals("")) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPrefManager().setMPin(PreferenceManager.encrypt(encryptedMPin));
//            }
        }
//                            getPrefManager().setProfileURL(data.getProfileURL());
//        getPrefManager().setUserID(PreferenceManager.encrypt(data.getUserId()));

    }

    private void vibrate(int duration) {
        Vibrator vibs = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibs.vibrate(duration);
    }

}
