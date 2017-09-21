package com.KryptosTextApp.KryptosText.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.KryptosTextApp.KryptosText.BuildConfig;
import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.application.ChatApplication;
import com.KryptosTextApp.KryptosText.database.PreferenceManager;
import com.KryptosTextApp.KryptosText.model.PHCLoginResponseData;
import com.KryptosTextApp.KryptosText.utils.ImageUtils;
import com.KryptosTextApp.KryptosText.utils.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.KryptosTextApp.KryptosText.R.layout.bottom_choose_picker_dialog;


/**
 * Common Activity class for default functionality in all activities
 * Created by KTirumalsetty on 07/02/2017.
 */

public abstract class BaseAppCompatActivity extends AppCompatActivity {

    private static final String TAG = "BaseAppCompatActivity";
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 15;

    private ProgressDialog mProgressDialog;
    private PreferenceManager mPreferenceManager;
    private Context mContext;
    private static final int STORAGE_PERMISSION_GALLERY_CONSTANT = 1001;
    private static final int STORAGE_PERMISSION_CAMERA_CONSTANT = 1002;
    private OnImageResult onImageResult;
    private final int PICK_FROM_CAMERA = 1003;
    private final int PICK_FROM_GALLERY = 1004;
    private final int CROP_IMAGE = 1005;
    private static Uri captureImageUri;
    private File captureImageFile;

    public interface OnImageResult {
        void getImageInfo(Uri imageUri, Bitmap captureImageBitmap, File captureImageFile);

        void deleteProfile();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mContext = this;
        mPreferenceManager = new PreferenceManager(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            hideKeyboard(this);
            finish();

        }

        return super.onOptionsItemSelected(item);
    }

    Context getContext() {
        return this.mContext;
    }

    protected ChatApplication getApplicationData() {
        return ((ChatApplication) getApplication());
    }

    protected void showProgress() {
        showProgress("Please wait..");
    }

    void showProgress(String msg) {
        if (mProgressDialog != null && mProgressDialog.isShowing())
            dismissProgress();

        mProgressDialog = ProgressDialog.show(this, getResources().getString(R.string.app_name), msg);
        mProgressDialog.setCancelable(false);
//        mProgressDialog.setProgressStyle(Theme_Material_Light);
    }

    void dismissProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    // Preference manager to save or get any kind of references
    PreferenceManager getPrefManager() {
        if (mPreferenceManager != null) {
            return mPreferenceManager;
        } else {
            mPreferenceManager = new PreferenceManager(this);
        }
        return mPreferenceManager;
    }

    void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    void showAlert(String msg) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.app_name))
                .setMessage(msg)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
    }

    void showNoNetworkAlert() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.app_name))
                .setMessage(R.string.alert_no_network)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
    }

    void showNoNetworkToast() {
        showToast(getString(R.string.alert_no_network));
    }

    void showSessionExpireAlert(String msg, final int statusCode) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getResources().getString(R.string.app_name))
                .setMessage(msg)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        getPrefManager().logoutUser();
                        if (statusCode == 402){
                            if (getApplicationData().getWebSocketInstance().isOpen()) {
                                getApplicationData().getWebSocketInstance().disconnect();
                            }
                        }
                       /* if (getApplicationData().getWebSocketInstance().isOpen()) {
                            getApplicationData().getWebSocketInstance().disconnect();
                        }*/

                    }
                }).create().show();
    }

    void showAppUpgradeAlert() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(getResources().getString(R.string.app_name))
                .setMessage(getResources().getString(R.string.app_force_upgrade_alert_message))
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            anfe.printStackTrace();
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        dialogInterface.dismiss();
//                        getPrefManager().logoutUser();
                        if (getApplicationData().getWebSocketInstance().isOpen()) {
                            getApplicationData().getWebSocketInstance().disconnect();
                        }

                    }
                }).create().show();
    }

    boolean hasTextEmail(EditText editText) {

        if (editText.getVisibility() == View.VISIBLE) {

            String text = editText.getText().toString().trim();
            editText.setError(null);
            // length 0 means there is no text

            if (TextUtils.isEmpty(text)) {
                editText.setError(getString(R.string.error_field_required));
                return true;
            } else if (!isValidEmail(text)) {
                editText.setError(getString(R.string.enter_valid_email));
                return true;
            }

        }

        return false;
    }

    private static boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    boolean hasText(EditText editText) {

        if (editText.getVisibility() == View.VISIBLE) {

            String text = editText.getText().toString().trim();
            editText.setError(null);
            // length 0 means there is no text

            if (TextUtils.isEmpty(text)) {
                editText.setError(getString(R.string.error_field_required));
//                focusView = mAboutShop;
//                cancel = true;
                return true;
            }

        }

        return false;
    }

    boolean isPasswordValid(EditText editText) {

        if (editText.getVisibility() == View.VISIBLE) {

            String text = editText.getText().toString().trim();
            editText.setError(null);
            // length 0 means there is no text

            if (text.length() < MIN_PASSWORD_LENGTH || text.length() > MAX_PASSWORD_LENGTH) {
                editText.setError(getString(R.string.error_field_pass_min_max_length));
//                focusView = mAboutShop;
//                cancel = true;
                return true;
            } else if (!isValidPasswordPattern(text)) {
                editText.setError(getString(R.string.error_field_pass_is_valid));
//                focusView = mAboutShop;
//                cancel = true;
                return true;

            }

        }

        return false;
    }

    private boolean isValidPasswordPattern(final String password) {

        Pattern pattern;
        Matcher matcher;

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{4,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

    boolean hasSpinnerSelected(Spinner spinner) {

        if (spinner.getVisibility() == View.VISIBLE) {

            int pos = spinner.getSelectedItemPosition();
            ((TextView) spinner.getSelectedView()).setError(null);
            // length 0 means there is no text

            if (pos == 0) {
                ((TextView) spinner.getSelectedView()).setError("");
//                focusView = mAboutShop;
//                cancel = true;
                return true;
            }

        }

        return false;
    }

    boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    void showFailAlert(String errorMsg) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the
        // dialog layout
//        builder.setTitle(title);
        builder.setCancelable(false);

//        builder.setIcon(R.drawable.galleryalart);
        View view = inflater.inflate(R.layout.failure_alert_dialog, null);
        builder.setView(view);
        TextView text = (TextView) view.findViewById(R.id.tv__failed_descr);
        text.setText(errorMsg);
        final AlertDialog alertDialog = builder.create();

        Button dialogButton = (Button) view.findViewById(R.id.btn_ok);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        // Add action buttons
               /* .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }*/


        alertDialog.show();

       /* final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        final AlertDialog dialog = new AlertDialog(this);
        builder.setView(R.layout.failure_alert_dialog);
//        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.setCancelable(false);
//        builder.setContentView(R.layout.failure_alert_dialog);

        TextView text = (TextView) dialog.findViewById(R.id.tv__failed_descr);
        text.setText(errorMsg);

        Button dialogButton = (Button) dialog.findViewById(R.id.btn_ok);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        builder.show();*/
    }

    void showSuccessAlert(String successMsg, final Intent intent) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        builder.setCancelable(false);

//        builder.setIcon(R.drawable.galleryalart);
        View view = inflater.inflate(R.layout.success_alert_dialog, null);
        builder.setView(view);
        TextView text = (TextView) view.findViewById(R.id.tv__success_descr);
        Spanned msg;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            msg = Html.fromHtml(successMsg, Html.FROM_HTML_MODE_COMPACT);
        else
            msg = Html.fromHtml(successMsg);
        text.setText(msg);

        final AlertDialog alertDialog = builder.create();

        Button dialogButton = (Button) view.findViewById(R.id.btn_ok);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                if (intent != null) {
                    startActivity((intent));
                    ((AppCompatActivity) mContext).finish();
                } else {
                    setResult(RESULT_OK);
                    ((AppCompatActivity) mContext).finish();
                }

            }
        });

        alertDialog.show();

    }
   /* protected void showSnackBarMessage(String message) {

        if (getView() != null) {

            Snackbar.make(getView(),message,Snackbar.LENGTH_SHORT).show();
        }
    }*/

    /**
     * Checking device has camera hardware or not
     */
    boolean isDeviceSupportCamera() {
        return (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA));
    }

    void deleteFiles(String path, boolean isCamera) {
        File dir;
        if (isCamera) {
            dir = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    path);
        } else {
            dir = new File(Environment.getExternalStorageDirectory().getPath(),
                    "PhcFolder/Images");
        }
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String _children : children) {
                new File(dir, _children).delete();
            }
        }
        dir.delete();
    }

    static String findFileFormat(String fileName) {
        String fileFormat;
        if (fileName.contains(".ANI") || fileName.contains(".BMP") || fileName.contains(".CAL") || fileName.contains(".FAX") ||
                fileName.contains(".GIF") || fileName.contains(".IMG") || fileName.contains(".JBG") || fileName.contains(".JPE") ||
                fileName.contains(".JPEG") || fileName.contains(".JPG") || fileName.contains(".MAC") || fileName.contains(".PBM") ||
                fileName.contains(".PCD") || fileName.contains(".PCX") || fileName.contains(".PCT") || fileName.contains(".PGM") ||
                fileName.contains(".PNG") || fileName.contains(".PPM") || fileName.contains(".PSD") || fileName.contains(".RAS") ||
                fileName.contains(".TGA") || fileName.contains(".TIFF") || fileName.contains(".WMF")) {

            fileFormat = "image";
        } else if (fileName.contains(".FLV") || fileName.contains(".AVI") || fileName.contains(".MOV") || fileName.contains(".MP4") ||
                fileName.contains(".MPG") || fileName.contains(".WMV") || fileName.contains(".3GP") || fileName.contains(".ASF") ||
                fileName.contains(".RM") || fileName.contains(".SWF")) {
            fileFormat = "video";
        } else if (fileName.contains(".apk") || fileName.contains(".APK")) {
            fileFormat = "APK";

        } else if (fileName.contains(".zip") || fileName.contains(".ZIP")) {
            fileFormat = "ZIP";

        } else {
            fileFormat = "document";
        }
        return fileFormat;
    }


    boolean isStoragePermission(Context context) {
        int readPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int camera = ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        return (readPermission == PackageManager.PERMISSION_GRANTED && writePermission == PackageManager.PERMISSION_GRANTED && camera == PackageManager.PERMISSION_GRANTED);
    }

    void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void captureImage(Context context) {
        this.onImageResult = (OnImageResult) context;
        if (Build.VERSION.SDK_INT >= 23) {
            takePermissionForMedia(STORAGE_PERMISSION_CAMERA_CONSTANT);
        } else {
            imageCapture();
        }
    }

    private void deleteImage(Context mContext) {
        this.onImageResult = (OnImageResult) mContext;
        this.onImageResult.deleteProfile();
    }

    private void pickImageFromGallery(Context context) {
        this.onImageResult = (OnImageResult) context;
        if (Build.VERSION.SDK_INT >= 23) {
            takePermissionForMedia(STORAGE_PERMISSION_GALLERY_CONSTANT);
        } else {
            imagePick();
        }
    }

    private void imageCapture() {
        if (this.onImageResult != null) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                captureImageFile = createImageFile();
                if (captureImageFile != null) {
                    if (Build.VERSION.SDK_INT >= LOLLIPOP) {
                        captureImageUri = FileProvider.getUriForFile(this,
                                BuildConfig.APPLICATION_ID + ".provider",
                                captureImageFile);
                    } else
                        captureImageUri = Uri.fromFile(captureImageFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, captureImageUri);
                    startActivityForResult(takePictureIntent, PICK_FROM_CAMERA);
                }
            }
        }
    }

    private void imagePick() {
        if (this.onImageResult != null) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), PICK_FROM_GALLERY);
        }
    }

    private void cropImage(Uri imageUriFromBitmap) {
        try {
            if (imageUriFromBitmap != null) {
                Intent cropIntent = new Intent("com.android.camera.action.CROP");
                cropIntent.setDataAndType(imageUriFromBitmap, "image/*");
                cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                cropIntent.putExtra("crop", "true");
                cropIntent.putExtra("aspectX", 1);
                cropIntent.putExtra("aspectY", 1);
                cropIntent.putExtra("outputX", 256);
                cropIntent.putExtra("outputY", 256);
                cropIntent.putExtra("return-data", true);
//                cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, captureImageUri);
                startActivityForResult(cropIntent, CROP_IMAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            cropImage(com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity.this, imageUriFromBitmap);
        }
    }

    private void takePermissionForMedia(int type) {
        if (!isStoragePermission(mContext)) {
            requestStoragePermission(type);
        } else {
            switch (type) {
                case STORAGE_PERMISSION_CAMERA_CONSTANT:
                    imageCapture();
                    break;
                case STORAGE_PERMISSION_GALLERY_CONSTANT:
                    imagePick();
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
//            captureImageBitmap = (Bitmap) data.getExtras().get("data");
//            captureImageUri = getImageUri(captureImageBitmap);
            cropImage(com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity.this, captureImageUri);
        } else if (requestCode == PICK_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            //cropImage(data.getData());
            captureImageUri = data.getData();
            cropImage(com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity.this, captureImageUri);
        } else if (requestCode == CROP_IMAGE) {
            try {
                Bitmap captureImageBitmap;
                if (data != null && data.getExtras() != null) {
                    captureImageBitmap = data.getExtras().getParcelable("data");
                    captureImageUri = getImageUri(captureImageBitmap);
                } else {
                    captureImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), captureImageUri);
                }
                circularBitmap(captureImageBitmap);
            } catch (Exception e) {
                e.printStackTrace();
                cropImage(captureImageUri);
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                captureImageUri = result.getUri();
                circularBitmap(null);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e("Eoor", error.getLocalizedMessage());
            }
        }

    }

    private void cropImage(Activity thisActivity, Uri data) {
        CropImage.activity(data).setRequestedSize(256, 256, CropImageView.RequestSizeOptions.RESIZE_EXACT).setAspectRatio(1, 1).start(thisActivity);
    }

    private void circularBitmap(Bitmap captureImageBitmap) {
        try {
            if (captureImageBitmap == null) {
                try {
                    captureImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), captureImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (onImageResult == null)
                this.onImageResult = (OnImageResult) mContext;
            captureImageFile = persistImage(captureImageBitmap);
            Bitmap circularBitmapWithWhiteBorder = ImageUtils.getCircularBitmapWithWhiteBorder(this, captureImageBitmap);
            this.onImageResult.getImageInfo(captureImageUri, circularBitmapWithWhiteBorder, captureImageFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestStoragePermission(int permissionType) {
        ActivityCompat.requestPermissions(com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, permissionType);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //   super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isGranted = true;
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                isGranted = false;
                break;
            }
        }
        if (isGranted) {
            switch (requestCode) {
                case STORAGE_PERMISSION_CAMERA_CONSTANT:
                    imageCapture();
                    break;
                case STORAGE_PERMISSION_GALLERY_CONSTANT:
                    imagePick();
                    break;
            }
        } else {
            showToast("Permissions Required");
//            requestStoragePermission(requestCode);
        }
    }

    private File createImageFile() {
        File mFileImage = null;
        try {
            mFileImage = new File(Environment
                    .getExternalStorageDirectory(), "kryptos_user_avatar_"
                    + String.valueOf(System.currentTimeMillis()) + ".jpg");
//mFileImage.mkdirs()r();
            // Save a file: path for use with ACTION_VIEW intents
//            mCurrentPhotoPath = "file:" + mFileImage.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mFileImage;
    }

    File persistImage(Bitmap bitmap) {
        File imageFile = new File(getApplicationContext().getFilesDir(), "kryptos_profile_image_cropped.jpg");
        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap");
            e.printStackTrace();
        }
        return imageFile;
    }

    private Uri getImageUri(Bitmap inImage) {
        Uri imageUri = null;
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(mContext.getContentResolver(), inImage, "Title", null);
            imageUri = Uri.parse(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageUri;
    }

    boolean isAppUpdateAvailable(PHCLoginResponseData data) {
       /* try {
            boolean isUpdateAvailable = false;
            if (versionCompare(data.getAndroidVersion(), getAppVersion()) >= 1) { //Update available
                isUpdateAvailable = true;
            }
            return isUpdateAvailable;
//            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }*/
       return data.isAndroidAppUpdate();

    }

    String getAppVersion() {
        try {
            return this.getPackageManager().getPackageInfo(BuildConfig.APPLICATION_ID, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }

    }


    /**
     * Compares two version strings.
     * <p>
     * Use this instead of String.compareTo() for a non-lexicographical
     * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     *
     * @param str1 a string of ordinal numbers separated by decimal points.
     * @param str2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is _numerically_ less than str2.
     * The result is a positive integer if str1 is _numerically_ greater than str2.
     * The result is zero if the strings are _numerically_ equal.
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     */
    private static int versionCompare(String str1, String str2) {
        try {
            String[] vals1 = str1.split("\\.");
            String[] vals2 = str2.split("\\.");
            int i = 0;
            // set index to first non-equal ordinal or length of shortest version string
            while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
                i++;
            }
            // compare first non-equal ordinal number
            if (i < vals1.length && i < vals2.length) {
                int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
                return Integer.signum(diff);
            }
            // the strings are equal or one string is a substring of the other
            // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
            return Integer.signum(vals1.length - vals2.length);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    void chooseImage(final Context _mContext) {
        View choosePickerView = LayoutInflater.from(_mContext).inflate(bottom_choose_picker_dialog, null);
        final BottomSheetDialog choosePickerDialog = new BottomSheetDialog(_mContext);
        choosePickerDialog.setContentView(choosePickerView);
        choosePickerView.findViewById(R.id.tv_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePickerDialog.dismiss();
                deleteImage(_mContext);
            }
        });
        choosePickerView.findViewById(R.id.tv_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePickerDialog.dismiss();
                captureImage(_mContext);
            }
        });
        choosePickerView.findViewById(R.id.tv_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePickerDialog.dismiss();
                pickImageFromGallery(_mContext);
            }
        });
        choosePickerDialog.show();
    }

    void generateNewFCMDeviceToken() {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FirebaseInstanceId.getInstance().deleteInstanceId();
                        FirebaseInstanceId.getInstance().getToken();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isSessionInvalid(int statusCode,String message){
        boolean isSessionValid = false;
        if (statusCode == 401 || statusCode == 402) {
            showSessionExpireAlert(message,statusCode);
            isSessionValid = true;
        }
        return isSessionValid;
    }

}