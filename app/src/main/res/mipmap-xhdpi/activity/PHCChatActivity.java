package com.KryptosTextApp.KryptosText.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.KryptosTextApp.KryptosText.BuildConfig;
import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.adapter.PHCChatListAdapter;
import com.KryptosTextApp.KryptosText.constant.MediaTypeConst;
import com.KryptosTextApp.KryptosText.constant.PHCAppConstant;
import com.KryptosTextApp.KryptosText.dialog.NotificationDialog;
import com.KryptosTextApp.KryptosText.interfaces.PHCRecyclerClickListener;
import com.KryptosTextApp.KryptosText.model.DriveItem;
import com.KryptosTextApp.KryptosText.model.PHCChatModel;
import com.KryptosTextApp.KryptosText.model.PHCChatResponseModel;
import com.KryptosTextApp.KryptosText.model.PHCChatSendingData;
import com.KryptosTextApp.KryptosText.model.PHCImageResponseModel;
import com.KryptosTextApp.KryptosText.model.PHCSelectMembersModel;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;
import com.KryptosTextApp.KryptosText.utils.BadgeDrawable;
import com.KryptosTextApp.KryptosText.utils.BitmapUtils;
import com.KryptosTextApp.KryptosText.utils.DecodeUtil;
import com.KryptosTextApp.KryptosText.utils.Log;
import com.KryptosTextApp.KryptosText.utils.PHCRecyclerTouchListener;
import com.KryptosTextApp.KryptosText.utils.RealPathUtils;
import com.google.gson.Gson;
import com.yovenny.videocompress.MediaController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.KryptosTextApp.KryptosText.R.layout.chat_bottom_choose_picker_dialog;


/**
 * Created by avinashj on 1/25/2017.
 *
 * @Description This activity is using for chat.
 */


public class PHCChatActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity {

    private static final String TAG = "PHCChatActivity";

    private static final String APP_DIR = "KryptosText";

    private static final String COMPRESSED_VIDEOS_DIR = "/Kryptos_Compressed_Videos/";

    private static final String TEMP_DIR = "/Temp/";

    private Context mContext;
    private LinearLayout ll_options;
    private TextView mMessageEditText, copyText, urgentText, unUrgentText, recallText, resendText,
            infoText;
    private View copyVerticalLine, urgentVerticalLine, unUrgentVerticalLine, recallVerticalLine,
            resendVerticalLine;
    private Button mAttachmentImageButton;
    private ImageButton mSendMessageImageButton;
    private CoordinatorLayout mCoordinatorLayout;

    //    private LinearLayout mAttachmentLayout, mGalleryLayout, mCameraLayout, mDriveLinearLayout;
    private ImageView mFabScrollToEnd;
    private TextView tvSendingStatus;
    private LinearLayout mAttachmentLayout;
    private RelativeLayout mGalleryLayout, mCameraLayout, mDriveLinearLayout;

    //    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mChatListView;
    private String mHeaderName;
    private PHCChatListAdapter mChatListAdapter;
    private ArrayList<PHCChatModel> mChatList;
    private int mGroupId;
    private PHCChatResponseModel mResponseModel;
    private PHCImageResponseModel mPhcImageResponseModel;
    private BroadcastReceiver mReceiver;
    private static final int PICK_IMAGE_VIDEO_DOCUMENT = 1;
    private static final int PICK_DOCUMENT = 3;
    private static final int REQUEST_CODE_DRIVE = 51;
    private int startIndex = 0;
    private int lastIndex = 19;
    private static final int STORAGE_PERMISSION_GALLERY_CONSTANT = 1;
    private static final int STORAGE_PERMISSION_CAMERA_CONSTANT = 2;
    private static final int STORAGE_PERMISSION_BROWSER_CONSTANT = 4;
    private static final String IMAGE_DIRECTORY_NAME = "PHC Camera";
    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private int mChatListPostion = 0;
    private Toolbar toolbar;
    private LinearLayout mLlNoRecords;
    private ImageView mRetryImageView;
    private TextView mTextError;
    private boolean isComposeMsg = false;
    private ArrayList<PHCSelectMembersModel> selectedMembers;
    private MenuItem infoMenuItem;
    private LinearLayoutManager mLayoutManager;
    private static final int TOTAL_INITIAL_LOAD = 20;

    private ProgressBar mProgressLoadMore;
    private boolean comingFromNotification;
    public static int sChatConversationID = 0;
    private MenuItem urgentMsgsMenuItem;
    private boolean isLastMsgUrgent;
    private Uri mInitialUri;
    private File mSelectedFile;
    private static Uri captureImageUri;
    private File captureImageFile;
    LinearLayout ll_process;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phc_activity_chat);
        mContext = com.KryptosTextApp.KryptosText.activity.PHCChatActivity.this;
        mChatList = new ArrayList<>();
        getAllView();

        if (getIntent() != null) {
            mHeaderName = getIntent().getStringExtra("headerName");
            mGroupId = getIntent().getIntExtra("groupId", 0);
            isComposeMsg = getIntent().hasExtra("isComposeMsg") && getIntent().getBooleanExtra("isComposeMsg", false);
            comingFromNotification = getIntent().hasExtra("comingFromNotification") && getIntent().getBooleanExtra("comingFromNotification", false);
            isLastMsgUrgent = getIntent().hasExtra("isLastMsgUrgent") && getIntent().getBooleanExtra("isLastMsgUrgent", false);
            if (isComposeMsg && getIntent().hasExtra("selectedMembers"))
                selectedMembers = (ArrayList<PHCSelectMembersModel>) getIntent().getSerializableExtra("selectedMembers");
        }
        sChatConversationID = mGroupId;
        updateToolbar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //   }
        setListener();
        if (isNetworkAvailable()) {
            getChatMessage(false);
        } else {
            showNoNetworkToast();
            mLlNoRecords.setVisibility(View.VISIBLE);
            mTextError.setText(getResources().getText(R.string.no_internet_retry));
            mChatListView.setVisibility(View.GONE);
        }
        mSendMessageImageButton.setEnabled(false);

        visibleUrgentMsgsBadgeVisibilty(isLastMsgUrgent);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mHeaderName = bundle.getString("headerName");
            mGroupId = bundle.getInt("groupId");
            sChatConversationID = mGroupId;
            comingFromNotification = bundle.getBoolean("comingFromNotification");
        }
        Log.d(TAG, "mHeaderNameBUndle " + mHeaderName + " groupId " + mGroupId + " fromNotification " + isComposeMsg);

      /*  if (intent != null) {
            mHeaderName = intent.getStringExtra("headerName");
            mGroupId = intent.getIntExtra("groupId", 0);
            isComposeMsg = intent.hasExtra("isComposeMsg") && getIntent().getBooleanExtra("isComposeMsg", false);
            comingFromNotification = getIntent().hasExtra("comingFromNotification") && getIntent().getBooleanExtra("comingFromNotification", false);
            if (isComposeMsg && getIntent().hasExtra("selectedMembers"))
                selectedMembers = (ArrayList<PHCSelectMembersModel>) getIntent().getSerializableExtra("selectedMembers");
        }*/
        updateToolbar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //   }
        setListener();
        if (isNetworkAvailable()) {
            getChatMessage(false);
        } else {
            showNoNetworkToast();
            mLlNoRecords.setVisibility(View.VISIBLE);
            mTextError.setText(getResources().getText(R.string.no_internet_retry));
            mChatListView.setVisibility(View.GONE);
        }
        mSendMessageImageButton.setEnabled(false);
       /*Intent restart = new Intent(this,PHCChatActivity.class);
        restart.putExtra("groupId", groupId);
        restart.putExtra("headerName", mHeaderNameBundle);
        restart.putExtra("comingFromNotification", fromNotification);
        startActivity(restart);
        finish();*/

    }


    /**
     * Description: This method is used for onclick event of all view.
     */
    private void setListener() {

        mAttachmentImageButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
//                if (mAttachmentLayout.isShown()) {
//                    mAttachmentLayout.setVisibility(View.GONE);
//                } else {
//                    mAttachmentLayout.setVisibility(View.VISIBLE);
//                }
                sendAttachment();
            }
        });
        mSendMessageImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (PHCChatService.mSocketClient.isSocketActive()) {
                if (getApplicationData().getWebSocketInstance().isOpen()) {
                    if (!mMessageEditText.getText().toString().trim().equalsIgnoreCase("")) {
                        mSendMessageImageButton.setEnabled(false);
                        JSONObject messageJson;
                        if (isValidGroupId())
//                            sendDataToSocketAndUpdateList(101, getChatText(mMessageEditText.getText().toString().trim()), null, 1, "text", false, 0);
                            messageJson = sendDataToSocketAndUpdateList(101, mMessageEditText.getText().toString().trim(), null, 1, "text", false, 0);
                        else
//                            createNewGroupAndSendMsg(getChatText(mMessageEditText.getText().toString().trim()));
                            messageJson = createNewGroupAndSendMsg(mMessageEditText.getText().toString().trim());
                        addToList(messageJson);
//                        sendDataToSocketAndUpdateList(101, getChatText(mMessageEditText.getText().toString().trim()), null, 1, "text", false, 0);

                    }
                } else {
//                    stopService(new Intent(mContext, PHCChatService.class));
//                    startService(new Intent(mContext, PHCChatService.class));
                    try {
                        getApplicationData().connectWSS();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    sendDataToSocketAndUpdateList(101, getChatText(mMessageEditText.getText().toString().trim()), null, 1, "text", false, 0);
                }
            }
        });

        mDriveLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(com.KryptosTextApp.KryptosText.activity.PHCChatActivity.this, com.KryptosTextApp.KryptosText.activity.DriveAcivity.class);
                intent.putExtra("isChatActivity", true);
                startActivityForResult(intent, REQUEST_CODE_DRIVE);
            }
        });
        mGalleryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMedia(false);
            }
        });
        mCameraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDeviceSupportCamera()) {
                    goToMedia(true);
                } else {
                    Toast.makeText(mContext, "Camera is not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        mSwipeRefreshLayout.setOnRefreshListener(this);

        //

        mChatListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int lastVisibleItemPosition = mLayoutManager.findLastCompletelyVisibleItemPosition();
                int firstCompletelyVisibleItemPosition = mLayoutManager.findFirstCompletelyVisibleItemPosition();
                if (firstCompletelyVisibleItemPosition > 3)
                    mFabScrollToEnd.setVisibility(View.VISIBLE);
                else
                    mFabScrollToEnd.setVisibility(View.GONE);
                if (lastIndex == lastVisibleItemPosition) {
                    getChatMessage(true);
                }
            }
        });

        mChatListView.addOnItemTouchListener(new PHCRecyclerTouchListener(getApplicationContext(), mChatListView, new PHCRecyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (mChatList.get(position).getMessageType() > 1) {
                    if (mChatList.get(position).isSender() || !mChatList.get(position).isRecalled()) {
                        if (!mChatList.get(position).isMediaUpload()) {
                            if (!mChatList.get(position).getMediaType().equals(MediaTypeConst.IMAGE) && !mChatList.get(position).getMediaType().equals(MediaTypeConst.VIDEO)) {
                                Intent intent = new Intent(getContext(), com.KryptosTextApp.KryptosText.activity.MediaDocWebView.class);
//                    Uri data = Uri.parse(driveItem.getMessageURL());
                                intent.putExtra("MEDIA_URL", DecodeUtil.decodeBase64(mChatList.get(position).getMessageText()));

                                intent.putExtra("MEDIA_TYPE", mChatList.get(position).getMediaType());
                                startActivity(intent);
                            } else {
                                sendSliderView(position);

                            }
                        }
                    }

                }
            }


            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onLongClick(View view, int position) {
              /*  if (mActionMode == null) {
                    mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionModeCallback);
                }

                toggleSelection(position);*/

                mChatListPostion = position;
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                LayoutInflater layoutInflater
                        = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.long_press_popup, null);


                final PopupWindow popupWindow = new PopupWindow(
                        popupView,
                        width - 20,
                        120);

//                popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);


                // Set an elevation value for popup window
                // Call requires API level 21
                if (Build.VERSION.SDK_INT >= 21) {
                    popupWindow.setElevation(5.0f);
                }

                /*final PopupWindow popupWindow = new PopupWindow(
                        popupView, View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);*/
                popupWindow.setOutsideTouchable(true);
                popupWindow.setFocusable(true);
                popupWindow.setBackgroundDrawable(new BitmapDrawable());
                int positionArray[] = new int[2];
                view.getLocationOnScreen(positionArray);

                copyText = (TextView) popupView.findViewById(R.id.copyText);
                urgentText = (TextView) popupView.findViewById(R.id.urgentText);
                ll_options = (LinearLayout) popupView.findViewById(R.id.ll_options);
                unUrgentText = (TextView) popupView.findViewById(R.id.unUrgentText);
                recallText = (TextView) popupView.findViewById(R.id.recallText);
                resendText = (TextView) popupView.findViewById(R.id.resendText);
                infoText = (TextView) popupView.findViewById(R.id.infoText);
                copyVerticalLine = popupView.findViewById(R.id.copyVerticalLine);
                urgentVerticalLine = popupView.findViewById(R.id.urgentVerticalLine);
                unUrgentVerticalLine = popupView.findViewById(R.id.unUrgentVerticalLine);
                recallVerticalLine = popupView.findViewById(R.id.recallVerticalLine);
                resendVerticalLine = popupView.findViewById(R.id.resendVerticalLine);

                longPressListener(mChatListPostion, popupWindow);
                if (mChatList.get(position).isSender()) {
                    if (mChatList.get(position).isDelivered() || mChatList.get(position).isRead()) {
                        if (mChatList.get(position).getMessageType() == 1) {
                            copyText.setVisibility(View.VISIBLE);
                            copyVerticalLine.setVisibility(View.VISIBLE);
                        } else {
                            copyText.setVisibility(View.GONE);
                            copyVerticalLine.setVisibility(View.GONE);
                        }
                        showTextOption(mChatList.get(position).isUrgent(), mChatList.get(position).isRecalled());
                    } else {
                        ll_options.setVisibility(View.GONE);
                        showToast("Message in sending state");
                    }
                } else {
                    if (mChatList.get(position).getMessageType() == 1) {
                        copyText.setVisibility(View.VISIBLE);
                        copyVerticalLine.setVisibility(View.VISIBLE);
                    } else {
                        copyText.setVisibility(View.GONE);
                        copyVerticalLine.setVisibility(View.GONE);
                    }
                    urgentText.setVisibility(View.GONE);
                    unUrgentText.setVisibility(View.GONE);
                    recallText.setVisibility(View.GONE);
                    infoText.setVisibility(View.GONE);
                    copyVerticalLine.setVisibility(View.GONE);
                    if (mChatList.get(position).isRecalled()) {
                        copyText.setVisibility(View.GONE);
                        copyVerticalLine.setVisibility(View.GONE);
                    } else {
                        copyText.setVisibility(View.VISIBLE);
                        copyVerticalLine.setVisibility(View.VISIBLE);
                    }
                }
                if (!(!mChatList.get(position).isSender() && mChatList.get(position).getMessageType() > 1))
                    popupWindow.showAtLocation(view, Gravity.TOP, positionArray[0], positionArray[1] - popupWindow.getHeight());
            }

        }));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(com.KryptosTextApp.KryptosText.activity.PHCChatActivity.this);
                onBackPressed();
                //finish();
            }
        });

        mRetryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    getChatMessage(false);
                } else {
                    showNoNetworkToast();
                }
            }
        });

    }

    private void addToList(JSONObject messageObject) {
        int caseId = 0;
        try {
            caseId = messageObject.getInt("CaseId");
            PHCChatModel chatModel = new PHCChatModel();
            chatModel.setUserId(messageObject.getInt("UserId"));
            chatModel.setMessageId(-1);
            chatModel.setGroupId(messageObject.optInt("GroupId"));
            chatModel.setMessageText(messageObject.getString("MessageText"));
            chatModel.setMessageType(messageObject.getInt("MessageType"));
            chatModel.setProfileURL("");
            chatModel.setCreatedDate("");
            chatModel.setSender(true);
            chatModel.setLocalMsgId(messageObject.getString("LocalMsgId"));
            chatModel.setSender(true);
            chatModel.setSending(true);
            chatModel.setDelivered(false);
            chatModel.setRead(false);
            chatModel.setUrgent(false);
            chatModel.setRecalled(false);
            chatModel.setUserName("You");
            chatModel.setMediaType(null);
            chatModel.setThumbnailURL(null);
            chatModel.setMediaUpload(false);
            switch (caseId) {
                case 105:
                    if (mChatList == null)
                        mChatList = new ArrayList<>();
                case 101:
                    mChatList.add(0, chatModel);
                    mMessageEditText.setText("");
                    lastIndex++;
                    mChatListAdapter.refreshList(mChatList);
                    mChatListAdapter.notifyDataSetChanged();
                    mChatListView.scrollToPosition(0);

                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String getParticipentGroupId() {
        StringBuilder groupIds = new StringBuilder();
        for (int i = 0; i < selectedMembers.size(); i++) {
            if (selectedMembers.get(i).isGroup()) {
                groupIds.append("").append(selectedMembers.get(i).getUserId());
                if (i < selectedMembers.size() - 1) {
                    groupIds.append(",");
                }
            }
        }
        return groupIds.toString().replace(",,", ",");
    }

    private String getParticipantIds() {
        StringBuilder participantIds = new StringBuilder();
        for (int i = 0; i < selectedMembers.size(); i++) {
            if (!selectedMembers.get(i).isGroup()) {
                participantIds.append("").append(selectedMembers.get(i).getUserId());
                if (i < selectedMembers.size() - 1) {
                    participantIds.append(",");
                }
            }
        }
        participantIds.append(",").append(getApplicationData().getUserID());
        return participantIds.toString().replace(",,", ",");
    }

    private JSONObject createNewGroupAndSendMsg(String message) {
        try {
            JSONObject messageObject = new JSONObject();
            messageObject.put("CaseId", "" + 105);
            messageObject.put("ParticipantIds", getParticipantIds());
            messageObject.put("ParticipantGroupIds", getParticipentGroupId());
            byte[] data = message.getBytes("UTF-8");
            String messageText = Base64.encodeToString(data, Base64.DEFAULT);
            messageObject.put("LocalMsgId", getCurrentLocalId());
            messageObject.put("MessageText", messageText.replace("\n", ""));
            messageObject.put("MessageType", 1);
            messageObject.put("SenderUserId", getApplicationData().getUserID());
            messageObject.put("UserId", getApplicationData().getUserID());
            messageObject.put("userToken", getApplicationData().getAuthToken());
            messageObject.put("MediaType", MediaTypeConst.TEXT);

//            PHCChatService.mSocketClient.sendMessage(messageObject.toString());
            Log.d("message: ", messageObject.toString());

            getApplicationData().getWebSocketInstance().sendText(messageObject.toString());
            return messageObject;
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showTextOption(boolean urgent, boolean recalled) {
        if (urgent) {
            urgentText.setVisibility(View.GONE);
            urgentVerticalLine.setVisibility(View.GONE);
            unUrgentText.setVisibility(View.VISIBLE);
            unUrgentVerticalLine.setVisibility(View.VISIBLE);
            recallText.setVisibility(View.GONE);
            recallVerticalLine.setVisibility(View.GONE);

        } else if (recalled) {
            copyText.setVisibility(View.GONE);
            copyVerticalLine.setVisibility(View.GONE);
            urgentText.setVisibility(View.GONE);
            urgentVerticalLine.setVisibility(View.GONE);
            unUrgentText.setVisibility(View.GONE);
            unUrgentVerticalLine.setVisibility(View.GONE);
            recallText.setVisibility(View.GONE);
            recallVerticalLine.setVisibility(View.GONE);
            resendText.setVisibility(View.VISIBLE);
            resendVerticalLine.setVisibility(View.VISIBLE);
        } else {
            /*copyText.setVisibility(View.VISIBLE);
            copyVerticalLine.setVisibility(View.VISIBLE);*/
            urgentText.setVisibility(View.VISIBLE);
            urgentVerticalLine.setVisibility(View.VISIBLE);
            recallText.setVisibility(View.VISIBLE);
            recallVerticalLine.setVisibility(View.VISIBLE);
            infoText.setVisibility(View.VISIBLE);
        }
    }

    private void longPressListener(final int mChatListPosition, final PopupWindow popUpWindow) {
        copyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] byteData;
                String text;
                try {
                    byteData = Base64.decode(mChatList.get(mChatListPosition).getMessageText(), Base64.DEFAULT);
                    text = new String(byteData, "UTF-8");
                } catch (Exception e) {
                    text = mChatList.get(mChatListPosition).getMessageText();
                }
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Phc Copy", text);
                clipboard.setPrimaryClip(clip);
                popUpWindow.dismiss();
            }
        });
        urgentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataToSocketAndUpdateList(102, null, null, 0, mChatList.get(mChatListPosition).getMediaType(), true, mChatList.get(mChatListPosition).getMessageId());
                popUpWindow.dismiss();
            }
        });
        unUrgentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataToSocketAndUpdateList(102, null, null, 0, mChatList.get(mChatListPosition).getMediaType(), false, mChatList.get(mChatListPosition).getMessageId());
                popUpWindow.dismiss();
            }
        });
        recallText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataToSocketAndUpdateList(104, null, null, 0, mChatList.get(mChatListPosition).getMediaType(), true, mChatList.get(mChatListPosition).getMessageId());
                popUpWindow.dismiss();
            }
        });
        infoText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent messageInfo = new Intent(mContext, com.KryptosTextApp.KryptosText.activity.PHCMessageInfo.class);
                messageInfo.putExtra("messageMode", mChatList.get(mChatListPosition));
                startActivity(messageInfo);
                popUpWindow.dismiss();
            }
        });
        resendText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDataToSocketAndUpdateList(104, null, null, 0, mChatList.get(mChatListPosition).getMediaType(), false, mChatList.get(mChatListPosition).getMessageId());
                popUpWindow.dismiss();
            }
        });
    }


    private JSONObject sendDataToSocketAndUpdateList(int caseId, String message, String thumbnailUrl, int messageType, String mediaType, boolean isOption, int messageId) {
        try {
            JSONObject messageObject = new JSONObject();
            messageObject.put("CaseId", "" + caseId);
            messageObject.put("UserId", "" + getApplicationData().getUserID());
            messageObject.put("userToken", getApplicationData().getAuthToken());
            messageObject.put("MediaType", mediaType);
            messageObject.put("LocalMsgId", getCurrentLocalId());
            switch (caseId) {
                case 101:
                    messageObject.put("GroupId", mGroupId);
                    byte[] data = message.getBytes("UTF-8");
                    String messageText = Base64.encodeToString(data, Base64.DEFAULT);
                    messageObject.put("MessageText", messageText.replace("\n", ""));
                    switch (messageType) {
                        case 2:
                        case 3:
                            messageObject.put("ThumbnailURL", thumbnailUrl);
                            break;
                        default:

                    }
                    messageObject.put("MessageType", messageType);
                    messageObject.put("MediaType", mediaType);
                    break;
                case 102:
                    messageObject.put("MessageId", "" + messageId);
                    messageObject.put("isUrgent", isOption);
                    break;
                case 103:
                    messageObject.put("GroupId", mGroupId);
                    messageObject.put("IsRead", true);
                    break;
                case 104:
                    messageObject.put("MessageId", "" + messageId);
                    messageObject.put("isRecalled", isOption);
                    break;


            }
//            PHCChatService.mSocketClient.sendMessage(messageObject.toString());
            Log.d("message: ", messageObject.toString());
            getApplicationData().getWebSocketInstance().sendText(messageObject.toString());
//            Charset characterSet = Charset.forName("US-ASCII");

//            getApplicationData().getWebSocketInstance().sendBinaryMessage(messageObject.toString().getBytes(characterSet));
            return messageObject;
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }


    private long getCurrentLocalId() {
        return System.currentTimeMillis();
    }

    /**
     * @Description: This method is used for getting view id of all view using in this class
     */

    private void getAllView() {
        ll_process = (LinearLayout) findViewById(R.id.ll_process);
        mMessageEditText = (TextView) findViewById(R.id.messageEditText);
        mProgressLoadMore = (ProgressBar) findViewById(R.id.progress_load_more);
        mChatListView = (RecyclerView) findViewById(R.id.chatList);
        mAttachmentImageButton = (Button) findViewById(R.id.attachmentImageButton);
        mSendMessageImageButton = (ImageButton) findViewById(R.id.sendMessageImageButton);
        mAttachmentLayout = (LinearLayout) findViewById(R.id.attachmentLayout);
        mFabScrollToEnd = (ImageView) findViewById(R.id.fab_scroll_end);
        tvSendingStatus = (TextView) findViewById(R.id.tv_sending_status);
      /*  mGalleryLayout = (LinearLayout) findViewById(R.id.galleryLayout);
        mDriveLinearLayout = (LinearLayout) findViewById(R.id.driveLayout);
        mCameraLayout = (LinearLayout) findViewById(R.id.cameraLayout);*/

        mGalleryLayout = (RelativeLayout) findViewById(R.id.galleryLayout);
        mDriveLinearLayout = (RelativeLayout) findViewById(R.id.driveLayout);
        mCameraLayout = (RelativeLayout) findViewById(R.id.cameraLayout);

        mLlNoRecords = (LinearLayout) findViewById(R.id.ll_no_records);
        mRetryImageView = (ImageView) findViewById(R.id.iv_retry);
        mTextError = (TextView) findViewById(R.id.textError);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        changeVisibilityBasedOnGroupId();

        mChatListAdapter = new PHCChatListAdapter(mContext, mChatList);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLayoutManager.setReverseLayout(true);
        mChatListView.setLayoutManager(mLayoutManager);
        mChatListView.setItemAnimator(new DefaultItemAnimator());
        mChatListView.setAdapter(mChatListAdapter);
        mSendMessageImageButton.setEnabled(false);
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged");

                if (s.length() > 0) {
                    mSendMessageImageButton.setEnabled(true);
//                    mSendMessageImageButton.setBackground(ContextCompat.getDrawable(mContext,R.drawable.send));
                } else {
                    mSendMessageImageButton.setEnabled(false);
//                    mSendMessageImageButton.setBackground(ContextCompat.getDrawable(mContext,R.drawable.ic_menu_send));

                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged");

            }
        });
    }

    private void changeVisibilityBasedOnGroupId() {
        if (mAttachmentImageButton != null)
            mAttachmentImageButton.setVisibility(isValidGroupId() ? View.VISIBLE : View.GONE);
        if (infoMenuItem != null)
            infoMenuItem.setVisible(isValidGroupId());
    }

    /**
     * @description This method is using for create header view
     */

    private void updateToolbar() {
        toolbar.setTitle(mHeaderName);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToGroupInfo();
            }
        });
        setSupportActionBar(toolbar);
//        mBackImageView.setVisibility(View.VISIBLE);
//        mInfoImageView.setVisibility(View.VISIBLE);
//        mHeaderNameTextView.setText(mHeaderName);
    }

    private void sendAttachment() {
        View choosePickerView = LayoutInflater.from(getContext()).inflate(chat_bottom_choose_picker_dialog, null);
        final BottomSheetDialog choosePickerDialog = new BottomSheetDialog(getContext());
        choosePickerDialog.setContentView(choosePickerView);
        ((TextView) choosePickerView.findViewById(R.id.tv_title)).setText("Select Attachment");
        choosePickerView.findViewById(R.id.tv_documents).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePickerDialog.dismiss();
                if (!isStoragePermission(getContext())) {
                    ActivityCompat.requestPermissions(com.KryptosTextApp.KryptosText.activity.PHCChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_BROWSER_CONSTANT);
                } else {
                    navigateToFileBrowser();
                }
            }
        });
        choosePickerView.findViewById(R.id.tv_drive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePickerDialog.dismiss();
                Intent intent = new Intent(com.KryptosTextApp.KryptosText.activity.PHCChatActivity.this, com.KryptosTextApp.KryptosText.activity.DriveAcivity.class);
                intent.putExtra("isChatActivity", true);
                startActivityForResult(intent, REQUEST_CODE_DRIVE);
            }
        });
        choosePickerView.findViewById(R.id.tv_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePickerDialog.dismiss();
                if (isDeviceSupportCamera()) {
                    goToMedia(true);
                } else {
                    Toast.makeText(mContext, "Camera is not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
        choosePickerView.findViewById(R.id.tv_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosePickerDialog.dismiss();
                goToMedia(false);
            }
        });
        choosePickerDialog.show();
    }

    private void navigateToFileBrowser() {
        Intent intent = new Intent(com.KryptosTextApp.KryptosText.activity.PHCChatActivity.this, com.KryptosTextApp.KryptosText.activity.CustomFileBrowserActivity.class);
        startActivityForResult(intent, PICK_DOCUMENT);
    }

    /**
     * @description: This method is using for getting all the message between user or group
     */
    private void getChatMessage(final boolean isPagination) {
        if (isNetworkAvailable()) {
            if (isValidGroupId()) {
                if (!isPagination) {
                    mChatList = new ArrayList<>();
//                    showProgress("Getting Chat History...");
                } else {
                    mProgressLoadMore.setVisibility(View.VISIBLE);
                    startIndex = lastIndex + 1;
                    lastIndex = lastIndex + 19;
                }
                PHCChatSendingData data = new PHCChatSendingData();
                data.setGroupId(mGroupId);
                data.setUserId(getApplicationData().getUserID());
                data.setStartIndex(startIndex);
                data.setEndIndex(lastIndex);

                PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
                Call<PHCChatResponseModel> call = apiService.chatData(getApplicationData().getAuthToken(), data);
                call.enqueue(new Callback<PHCChatResponseModel>() {
                    @Override
                    public void onResponse(Call<PHCChatResponseModel> call, Response<PHCChatResponseModel> response) {
                        Log.d(TAG, " getChatMessage URL " + response.raw().request().url());
                        Log.d(TAG, " getChatMessage Resp " + new Gson().toJson(response.body()));
                        mResponseModel = response.body();
                        ll_process.setVisibility(View.GONE);
                        if (mResponseModel != null) {
                            if (isPagination)
                                mProgressLoadMore.setVisibility(View.GONE);
                            dismissProgress();
                            if (isSessionInvalid(mResponseModel.getStatusCode(),mResponseModel.getStatusMessage())) {
                                return;
                            }
                            if (mResponseModel != null && mResponseModel.getStatusCode() == 1) {

                                if (mResponseModel.getData() != null && mResponseModel.getData().size() > 0) {
                                    mLlNoRecords.setVisibility(View.GONE);
                                    mChatListView.setVisibility(View.VISIBLE);
//                                mSendMessageImageButton.setEnabled(true);
                                    mChatList.addAll(mResponseModel.getData());

                                    if (mChatListAdapter == null) {
                                        mChatListAdapter = new PHCChatListAdapter(mContext, mChatList);
                                        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                                        mLayoutManager.setReverseLayout(true);
                                        mChatListView.setLayoutManager(mLayoutManager);
                                        mChatListView.setItemAnimator(new DefaultItemAnimator());
                                        mChatListView.setAdapter(mChatListAdapter);
                                        mChatListView.scrollToPosition(0);
//                                        sendDataToSocketAndUpdateList(103, null, null, 0, null, false, 0);
                                    } else {

                                        mChatListAdapter.refreshList(mChatList);
                                        mChatListAdapter.notifyDataSetChanged();
                                    }
                                    if (mChatList != null && mChatList.size() > 0 && !mChatList.get(0).isSender()) {
                                        sendDataToSocketAndUpdateList(103, null, null, 0, null, false, 0);

                                    }
                                } else {
                                /*if (mSwipeRefreshLayout.isRefreshing())
                                    mSwipeRefreshLayout.setRefreshing(false);*/
                                    Toast.makeText(com.KryptosTextApp.KryptosText.activity.PHCChatActivity.this, "No chat history..", Toast.LENGTH_SHORT).show();
                                }
//                            mChatListView.scrollToPosition(0);
                            } else {
                                dismissProgress();
                                Toast.makeText(com.KryptosTextApp.KryptosText.activity.PHCChatActivity.this, "No chat history..", Toast.LENGTH_SHORT).show();
                            }
                        } else
                            Log.e("Chat History Response", "Null");
                    }

                    @Override
                    public void onFailure(Call<PHCChatResponseModel> call, Throwable t) {
                        Log.d(TAG, "onFailure");
                        dismissProgress();
                        t.printStackTrace();
                    }
                });

            }else {
                ll_process.setVisibility(View.GONE);
                mChatListView.setVisibility(View.VISIBLE);

            }
        } else {
            showNoNetworkAlert();
        }
    }

   /* private static String getChatText(String chatText) {
        StringBuilder builder = new StringBuilder();
        try {
            for (int i = 0; i < chatText.length(); i++) {
                char c = chatText.charAt(i);
                if (!Character.isLetterOrDigit(c) && !Character.isSpaceChar(c) && !Character.isWhitespace(c)) {
                    String unicode = String.valueOf(c);
                    int code = (int) c;
                    if (!(code >= 0 && code <= 255)) {
                        unicode = "\\u" + Integer.toHexString(c);
                    }
                    builder.append(unicode);
                } else {
                    builder.append(c);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return builder.toString();
    }*/


    /**
     * @param messageObject
     * @param caseId
     * @throws JSONException
     * @description: This method is using for parsing the data which is getting from socket and notify the list
     */
    private void setListNotifyDataChange(JSONObject messageObject, int caseId) throws JSONException {

        if (caseId != 103) {
            PHCChatModel chatModel = new PHCChatModel();
            chatModel.setUserId(messageObject.getInt("UserId"));
            chatModel.setMessageId(messageObject.optInt("MessageId"));
            chatModel.setGroupId(messageObject.getInt("GroupId"));
            chatModel.setMessageText(messageObject.getString("MessageText"));
            chatModel.setMessageType(messageObject.getInt("MessageType"));
            chatModel.setProfileURL(messageObject.getString("ProfileURL"));
            chatModel.setCreatedDate(messageObject.getString("CreatedDate"));

            boolean isSender = messageObject.optBoolean("IsSender");
            String mediaType = messageObject.getString("MediaType");
            chatModel.setSender(isSender);
            chatModel.setDelivered(true);
            chatModel.setRead(messageObject.getBoolean("IsRead"));
            chatModel.setLocalMsgId(messageObject.getString("LocalMsgId"));
            chatModel.setUrgent(messageObject.optBoolean("IsUrgent"));
            chatModel.setRecalled(messageObject.optBoolean("IsRecalled"));
            chatModel.setUserName(messageObject.getString("UserName"));
            chatModel.setMediaType(mediaType);
            chatModel.setThumbnailURL(messageObject.getString("ThumbnailURL"));
            chatModel.setMediaUpload(false);
            switch (caseId) {
                case 105:
                    if (!mediaType.equalsIgnoreCase("text") || !isSender) {
                        if (mChatList == null)
                            mChatList = new ArrayList<>();
                    }
                case 101:
                    if (!mediaType.equalsIgnoreCase("text") || !isSender) {
                        mChatList.add(0, chatModel);
                        lastIndex++;
                        mChatListAdapter.refreshList(mChatList);
                        mChatListAdapter.notifyDataSetChanged();
                        mChatListView.scrollToPosition(0);
                        mFabScrollToEnd.setVisibility(View.GONE);
                        tvSendingStatus.setVisibility(View.GONE);
                    } else {
                        updateListItem(chatModel);
                    }
                    if (!chatModel.isSender()) {
                        sendDataToSocketAndUpdateList(103, null, null, 0, null, false, 0);
                    }

                    break;
                default:
                    for (int i = 0; i < mChatList.size(); i++) {
                        if (chatModel.getMessageId() == mChatList.get(i).getMessageId()) {
                            mChatList.get(i).setMessageText(chatModel.getMessageText());
                            mChatList.get(i).setRecalled(chatModel.isRecalled());
                            mChatList.get(i).setUrgent(chatModel.isUrgent());

                            mChatListAdapter.refreshList(mChatList);
                            mChatListAdapter.notifyDataSetChanged();
                            mChatListView.scrollToPosition(i);
                            break;
                        }
                    }
            }
        } else {
            if (messageObject.getBoolean("IsRead")) {
                for (int i = 0; i < mChatList.size(); i++) {
                    if (mChatList.get(i).isSender()) {
                        mChatList.get(i).setRead(true);
                        mChatList.get(i).setDelivered(false);
                    }
                }
                mChatListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void updateListItem(PHCChatModel chatModel) {
        for (int i = 0; i < mChatList.size(); i++) {
            if (chatModel.getLocalMsgId() != null && mChatList.get(i).getLocalMsgId() != null && chatModel.getLocalMsgId().equalsIgnoreCase(mChatList.get(i).getLocalMsgId())) {
                mChatList.set(i, chatModel);
                mChatListAdapter.refreshList(mChatList);
                mChatListAdapter.notifyDataSetChanged();
                break;
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DRIVE && resultCode == Activity.RESULT_OK) {
            DriveItem mDriveItem = (DriveItem) data.getSerializableExtra("result");
            byte[] byteData;
            String messageUrl = "";
            if (mDriveItem.getMessageURL() != null && !mDriveItem.getMessageURL().equalsIgnoreCase("")) {
                try {
                    byteData = Base64.decode(mDriveItem.getMessageURL(), Base64.DEFAULT);
                    messageUrl = new String(byteData, "UTF-8");
                } catch (Exception e) {
                    messageUrl = mDriveItem.getMessageURL();
                }
            }
//            PHCChatModel model = new PHCChatModel();
            if (mDriveItem.getMediaType().contains(MediaTypeConst.IMAGE)) {
               /* model.setMessageType(2);
                model.setMessageText(messageUrl);
//                listUpdate(model);*/
                sendMediaToSocket(messageUrl, mDriveItem.getThumbnailUrl(), 2, mDriveItem.getMediaType());
            } else if (mDriveItem.getMediaType().contains(MediaTypeConst.VIDEO)) {
                /*model.setMessageType(3);
                listUpdate(model);*/
                sendMediaToSocket(messageUrl, mDriveItem.getThumbnailUrl(), 3, mDriveItem.getMediaType());
            } else {
                switch (mDriveItem.getMediaType()) {
                    case MediaTypeConst.DOC:
                    case MediaTypeConst.PDF:
                    case MediaTypeConst.EXCEL:
                    case MediaTypeConst.PPT:
                    case MediaTypeConst.TXT:
                    case MediaTypeConst.CSV:
                       /* model.setMessageType(4);
                        model.setMediaType("abc");
                        listUpdate(model);*/
                        sendMediaToSocket(messageUrl, null, 4, mDriveItem.getMediaType());
                        break;
                    default:
                }

            }
        } else if ((requestCode == PICK_IMAGE_VIDEO_DOCUMENT || requestCode == PICK_DOCUMENT) && resultCode == Activity.RESULT_OK) {
            boolean isVideoAttachment = false;
            boolean isImageAttachment = false;
            try {
                if (data != null) {
                    Uri videoUri = null;
                    Uri uri = null;
                    if (data.getData() != null)
                        mInitialUri = Uri.parse(data.getData().toString());
                    else
                        mInitialUri = Uri.parse(data.getStringExtra("resultURI"));
//                    PHCChatModel chatModel = new PHCChatModel();
                    if (mInitialUri.toString().contains("image")) {
                        isImageAttachment = true;
//                        uri = Uri.parse(compressImage(getPath(mContext, mInitialUri)));
                        uri = Uri.parse(getPath(mContext, mInitialUri));
//                        chatModel.setMessageType(2);
//                        chatModel.setMessageText(uri.toString());
                    } else if (mInitialUri.toString().contains("video")) {
//                        chatModel.setMessageType(3);
                        isVideoAttachment = true;

                        uri = Uri.parse(getPath(mContext, mInitialUri));
                        if (data.getData() != null)
                            videoUri = data.getData();
                        else
                            videoUri = Uri.parse(data.getStringExtra("resultURI"));

                    } else {
                        if (mInitialUri.toString().contains("file") || mInitialUri.toString().contains("document")) {
                            uri = Uri.parse(getPath(mContext, mInitialUri));
                            String fileFormat = findFileFormat(getPath(mContext, mInitialUri).toUpperCase());
                            if (fileFormat.equalsIgnoreCase("APK")) {
                                showToast("Can't upload APK files");
                                return;
                            }
                            if (fileFormat.equalsIgnoreCase("ZIP")) {
                                showToast("Can't upload ZIP files");
                                return;
                            }
                            if (fileFormat.equalsIgnoreCase("image")) {
//                                chatModel.setMessageType(2);
//                                chatModel.setMessageText(uri.toString());
                            } else if (fileFormat.equalsIgnoreCase("video")) {
//                                chatModel.setMessageType(3);
                            } else if (fileFormat.equalsIgnoreCase("document")) {
//                                chatModel.setMessageType(4);
//                                chatModel.setMediaType("abc");
                            } else {
                                showToast("Can't upload");
                                return;
                            }
                        }
                    }
//                    chatModel.setSender(true);
//                    chatModel.setMediaUpload(true);
//                    mChatList.add(0, chatModel);
                    //                mChatList.add(chatModel);
                    //                mChatListView.scrollToPosition(0);
//                    mChatListAdapter.refreshList(mChatList);
//                    mChatListAdapter.notifyDataSetChanged();
//                    String filePath = SiliCompressor.with(this).compressVideo(uri.getPath(), uri.getPath());

                    mSelectedFile = new File(uri.getPath());
                    // Get length of file in bytes
                    long fileSizeInBytes = mSelectedFile.length();
                    // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
                    long fileSizeInKB = fileSizeInBytes / 1024;
                    // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
                    long fileSizeInMB = fileSizeInKB / 1024;

                    if (fileSizeInMB > PHCAppConstant.VIDEO_UPLAOD_SIZE_LIMIT) {
                        showAlert("File size exceeded the allowed limit (" + PHCAppConstant.VIDEO_UPLAOD_SIZE_LIMIT + "MB) ");
                        return;
                    }
                    if (isVideoAttachment) {
                        compressVideoFile(videoUri);

                    } else {
                        if (isImageAttachment){

                            try {
                                Bitmap bitmap = BitmapUtils.handleSamplingAndRotationBitmap(this, mInitialUri);
                                File fileWithOrientation = persistImage(bitmap);
//                            Uri uri = getImageUri(mContext, bitmap);
                                sendMediaToServerAndUpdateToSocket(fileWithOrientation, mInitialUri, false, mSelectedFile.getName());
                            } catch (Exception e) {
                                e.printStackTrace();
                                sendMediaToServerAndUpdateToSocket(mSelectedFile, mInitialUri, false, mSelectedFile.getName());
                                return;
                            }
                            return;
                        }
                        sendMediaToServerAndUpdateToSocket(mSelectedFile, mInitialUri, false, mSelectedFile.getName());
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast("No support for this type of files");
            }
        } else if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            try {
               /* if (data!=null && data.getData()!=null) {
                    Uri imageUri = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    File file = persistImage(bitmap);
                    Uri uri = getImageUri(mContext, bitmap);
                }*/
               /* Bundle extras = data.getExtras();
                Bitmap imageBitmap = extras.getParcelable("data");
                File file = persistImage(imageBitmap);
                Uri uri = getImageUri(mContext, imageBitmap);*/
                /*PHCChatModel chatModel = new PHCChatModel();
                chatModel.setSender(true);
                chatModel.setMediaUpload(true);
                chatModel.setMessageText(uri.toString());
                chatModel.setMessageType(2);

                mChatList.add(0, chatModel);
                mChatListAdapter.refreshList(mChatList);
                mChatListAdapter.notifyDataSetChanged();
                mChatListView.scrollToPosition(0);*/
//
               /* Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), captureImageUri);
                File file = persistImage(bitmap);
                Uri uri = getImageUri(mContext, bitmap);*/
                Bitmap bitmap = BitmapUtils.handleSamplingAndRotationBitmap(this, captureImageUri);
                File file = persistImage(bitmap);
                Uri uri = getImageUri(mContext, bitmap);
                sendMediaToServerAndUpdateToSocket(file, uri, true, file.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private File createImageFile() {
        File mFileImage = null;
        try {
            mFileImage = new File(Environment
                    .getExternalStorageDirectory(), "kryptos_user_avatar_"
                    + String.valueOf(System.currentTimeMillis()) + ".jpg");

            // Save a file: path for use with ACTION_VIEW intents
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mFileImage;
    }

    private void listUpdate(PHCChatModel model) {
        model.setSender(true);
        model.setMediaUpload(true);
        mChatList.add(0, model);
        mChatListAdapter.refreshList(mChatList);
        mChatListAdapter.notifyDataSetChanged();
        mChatListView.scrollToPosition(0);
    }


    @Override
    protected void onResume() {
        super.onResume();
        sChatConversationID = mGroupId;
        changeVisibilityBasedOnGroupId();
        if (mReceiver == null) {
            mReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    //extract our message from intent
                    String message = intent.getStringExtra("some_msg");
                    boolean isConnecting = intent.getBooleanExtra("IsConnecting", false);
                    boolean isConnectionLost = intent.getBooleanExtra("IsConnectionLost", false);
                    boolean isConnectionOpen = intent.getBooleanExtra("IsConnectionOpen", false);
                    boolean isMessageReceived = intent.getBooleanExtra("IsMessageReceived", false);
                    JSONObject messageObject;
                    try {
                        if (isConnecting) {
                            Log.d(TAG, "Socket connecting..");
                            showSnackBarToast(getResources().getString(R.string.socket_connecting), false);

                        }
                        if (isConnectionOpen) {
                            Log.d(TAG, "Socket connected");
                            showSnackBarToast(getResources().getString(R.string.socket_connected), false);

                        }
                        if (isConnectionLost) {
                            Log.d(TAG, "Socket disconnected");
                            showSnackBarToast(getResources().getString(R.string.socket_disconnected), true);

                        }

                        if (isMessageReceived) {
                            messageObject = new JSONObject(message);
                            if (isSessionInvalid(messageObject.getInt("StatusCode"),messageObject.optString("StatusMessage"))) {
                                return;
                            }
                            if (messageObject.getInt("StatusCode") == 1 && messageObject.getString("StatusMessage").equalsIgnoreCase("Ok")) {
                                switch (messageObject.getInt("CaseId")) {
                                    case 105:
                                        mHeaderName = messageObject.getString("GroupName");
                                        mGroupId = messageObject.getInt("GroupId");
                                        updateToolbar();
                                        changeVisibilityBasedOnGroupId();
                                        mMessageEditText.setText("");
//                                        mSendMessageImageButton.setEnabled(false);
                                        setListNotifyDataChange(messageObject, messageObject.getInt("CaseId"));
                                        break;
                                    case 101:
                                        if (mGroupId == messageObject.getInt("GroupId")) {
                                            if (messageObject.getBoolean("IsSender"))
                                                mMessageEditText.setText("");
//                                            mSendMessageImageButton.setEnabled(false);
                                            setListNotifyDataChange(messageObject, messageObject.getInt("CaseId"));
                                        } else
                                            NotificationDialog.showNotification(mContext, messageObject);
                                        break;
                                    case 103:
                                        if (mGroupId == messageObject.getInt("GroupId"))
                                            setListNotifyDataChange(messageObject, messageObject.getInt("CaseId"));

                                        break;

                                    default:
                                        if (mGroupId == messageObject.getInt("GroupId")) {
                                            setListNotifyDataChange(messageObject, messageObject.getInt("CaseId"));
                                        } else
                                            NotificationDialog.showNotification(mContext, messageObject);
                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            //registering our receiver

            IntentFilter intentFilter = new IntentFilter("com.KryptosTextApp.KryptosText.MESSAGE");
            LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, intentFilter);
        }

//        this.registerReceiver(mReceiver, intentFilter);

    }

    private Snackbar mSocketSnackBar;

    private void showSnackBarToast(String message, boolean isConLost) {
        if (mSocketSnackBar == null) {
            mSocketSnackBar = Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG);

            View view = mSocketSnackBar.getView();
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
            params.gravity = Gravity.BOTTOM;
            view.setLayoutParams(params);
        } else {
            mSocketSnackBar.setText(message);
        }

        if (isConLost) {
            mSocketSnackBar.setAction("Retry", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    mSocketSnackBar.dismiss();
                    if (isNetworkAvailable())
                        try {
                            getApplicationData().connectWSS();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    else
                        showNoNetworkToast();
                }
            });
            mSocketSnackBar.setDuration(Snackbar.LENGTH_INDEFINITE);
        } else {
            mSocketSnackBar.setAction("", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    mSocketSnackBar.dismiss();
//                    getApplicationData().connect();
                }
            });
            mSocketSnackBar.setDuration(Snackbar.LENGTH_SHORT);

        }

        mSocketSnackBar.show();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        sChatConversationID = 0;
        //unregister our receiver
//        this.unregisterReceiver(this.mReceiver);
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);

    }

    //
//    @Override
//    public void onRefresh() {
//        getChatMessage();
//
//    }

    /**
     * @param context
     * @param uri
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void takePermissionForMedia(boolean isCamera) {
        if (!isStoragePermission(mContext)) {
            if (isCamera)
                ActivityCompat.requestPermissions(com.KryptosTextApp.KryptosText.activity.PHCChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, STORAGE_PERMISSION_CAMERA_CONSTANT);
            else {
                ActivityCompat.requestPermissions(com.KryptosTextApp.KryptosText.activity.PHCChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_GALLERY_CONSTANT);
            }
        } else {
            goTOGallery(isCamera);
        }
    }

    private void goToMedia(boolean isCamera) {
        if (Build.VERSION.SDK_INT >= 23) {
            takePermissionForMedia(isCamera);
        } else {
            goTOGallery(isCamera);
        }
    }

    private void goTOGallery(boolean isCamera) {
        if (isCamera) {

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
                    startActivityForResult(takePictureIntent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                }
            }
            /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile;
                try {
                    photoFile = createImageFile();
                } catch (Exception e) {
                    // Error occurred while creating the File
                    e.printStackTrace();
                    return;
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    startActivityForResult(takePictureIntent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                }*/

        } else {

            if (Build.VERSION.SDK_INT <= 19) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                photoPickerIntent.setType("image/* video/*");
                startActivityForResult(photoPickerIntent, PICK_IMAGE_VIDEO_DOCUMENT);
            } else {
                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
//                photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image", "video","file","application/pdf","application/x-excel"});
                String[] mimetypes = {"image/*", "video/*"};
                photoPickerIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);

                photoPickerIntent.setType("file/*");
                photoPickerIntent.addCategory(Intent.CATEGORY_DEFAULT);

                startActivityForResult(photoPickerIntent, PICK_IMAGE_VIDEO_DOCUMENT);

            }
           /* Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image*//* video*//* file*//*");
            startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_picture)), PICK_IMAGE_VIDEO_DOCUMENT);*/
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CAMERA_CONSTANT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                goTOGallery(true);
            } else {
//                    ActivityCompat.requestPermissions(PHCChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CAMERA_CONSTANT);
                Toast.makeText(mContext, "Camera Permission Required", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (requestCode == STORAGE_PERMISSION_GALLERY_CONSTANT)
                    goTOGallery(false);
                else if (requestCode == STORAGE_PERMISSION_BROWSER_CONSTANT)
                    navigateToFileBrowser();
            } else {
                //ActivityCompat.requestPermissions(PHCChatActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_GALLERY_CONSTANT);
                Toast.makeText(mContext, "Storage Permission Required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * returning image / video
     */

    private static File getOutputMediaFile(int type) {

        // External sdcard location
//        File mediaStorageDir = new File(
//                Environment
//                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
//                IMAGE_DIRECTORY_NAME);
        File mediaStorageDir = new File(
                Environment
                        .getExternalStorageDirectory(),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {

                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "image_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    private void sendMediaToServerAndUpdateToSocket(File file, final Uri initialUri, final boolean isCamera, String fileName) {
        tvSendingStatus.setVisibility(View.VISIBLE);
        RequestBody caption = RequestBody.create(MediaType.parse("text/plain"), "caption");
        RequestBody mediaFileName = RequestBody.create(MediaType.parse("text/plain"), fileName);


        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        MultipartBody.Part body =
                MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);

        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        showProgress("Uploading media..");
        Call<PHCImageResponseModel> call = apiService.sendImage(caption, body, getApplicationData().getAuthToken(), mediaFileName);
        call.enqueue(new Callback<PHCImageResponseModel>() {
            @Override
            public void onResponse(Call<PHCImageResponseModel> call, Response<PHCImageResponseModel> response) {
                dismissProgress();
                Log.d(TAG, " sendMediaToServerAndUpdateToSocket URL " + response.raw().request().url());
                Log.d(TAG, " sendMediaToServerAndUpdateToSocket Resp " + new Gson().toJson(response.body()));
                mPhcImageResponseModel = response.body();

                if (mPhcImageResponseModel != null) {
                    if (isSessionInvalid(mPhcImageResponseModel.getStatusCode(),mPhcImageResponseModel.getStatusMessage())) {
                        return;
                    }
                    if (mPhcImageResponseModel.getStatusCode() == 1) {
//                    if (PHCChatService.mSocketClient.isSocketActive()) {
                        if (getApplicationData().getWebSocketInstance().isOpen()) {
                            if (initialUri.toString().contains("image") || mPhcImageResponseModel.getData().getMediaType().equals(MediaTypeConst.IMAGE)) {
                                if (isCamera) deleteFiles(IMAGE_DIRECTORY_NAME, true);
                                else deleteFiles("", false);
                                sendMediaToSocket(mPhcImageResponseModel.getData().getMediaURL(), mPhcImageResponseModel.getData().getThumbnailURL(), 2, mPhcImageResponseModel.getData().getMediaType());
                            } else if (initialUri.toString().contains("video")) {
                                sendMediaToSocket(mPhcImageResponseModel.getData().getMediaURL(), mPhcImageResponseModel.getData().getThumbnailURL(), 3, mPhcImageResponseModel.getData().getMediaType());
                            } else if (initialUri.toString().contains("file")) {
                                String fileFormat = findFileFormat(mPhcImageResponseModel.getData().getMediaURL().toUpperCase());
                                if (fileFormat.equalsIgnoreCase("image")) {
                                    sendMediaToSocket(mPhcImageResponseModel.getData().getMediaURL(), mPhcImageResponseModel.getData().getThumbnailURL(), 2, mPhcImageResponseModel.getData().getMediaType());
                                } else if (fileFormat.equalsIgnoreCase("video")) {
                                    sendMediaToSocket(mPhcImageResponseModel.getData().getMediaURL(), mPhcImageResponseModel.getData().getThumbnailURL(), 3, mPhcImageResponseModel.getData().getMediaType());
                                } else if (fileFormat.equalsIgnoreCase("document")) {
                                    sendMediaToSocket(mPhcImageResponseModel.getData().getMediaURL(), mPhcImageResponseModel.getData().getThumbnailURL(), 4, mPhcImageResponseModel.getData().getMediaType());
                                }
                            }

                        } else {
//                        stopService(new Intent(mContext, PHCChatService.class));
//                        startService(new Intent(mContext, PHCChatService.class));
                            try {
                                getApplicationData().connectWSS();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        showToast(mPhcImageResponseModel.getStatusMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<PHCImageResponseModel> call, Throwable t) {
                dismissProgress();
                Toast.makeText(com.KryptosTextApp.KryptosText.activity.PHCChatActivity.this, "Something went wrong! Please retry", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMediaToSocket(String mediaUrl, String thumbNailUrl, int messageType, String mediaType) {
        sendDataToSocketAndUpdateList(101, mediaUrl, thumbNailUrl, messageType, mediaType, false, 0);
    }


    private void sendSliderView(int position) {
        int mediaPosition = 0;
        ArrayList<PHCChatModel> list = new ArrayList<>();
        for (int i = 0; i < mChatList.size(); i++) {
            if (mChatList.get(i).getMessageType() > 1 && mChatList.get(i).getMediaType().equals(MediaTypeConst.IMAGE) || mChatList.get(i).getMediaType().equals(MediaTypeConst.VIDEO)) {
                if (mChatList.get(i).getMessageId() == mChatList.get(position).getMessageId())
                    mediaPosition = list.size();
                PHCChatModel chatModel;
                chatModel = mChatList.get(i);
                if (chatModel.isSender() || !chatModel.isRecalled()) {
                    list.add(chatModel);
                }
            }

        }
        Intent sliderIntent = new Intent(mContext, com.KryptosTextApp.KryptosText.activity.PHCMediaSliderActivity.class);
        sliderIntent.putExtra("mediaPosition", mediaPosition);
        sliderIntent.putExtra("ChatModel", list);
        mContext.startActivity(sliderIntent);
    }


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
    }

    @Override
    public void onStop() {
        super.onStop();
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_chat, menu);
        infoMenuItem = menu.findItem(R.id.InfoImageView);
        urgentMsgsMenuItem = menu.findItem(R.id.iv_urgent_msgs);

        infoMenuItem.setVisible(isValidGroupId());
        visibleUrgentMsgsBadgeVisibilty(isLastMsgUrgent);
       /* Drawable drawable = menu.findItem(R.id.InfoImageView).getIcon();

        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this, R.color.colorWhite));
        menu.findItem(R.id.InfoImageView).setIcon(drawable);*/
        return true;

    }

    private static void setBadgeCount(Context context, LayerDrawable icon, boolean isVisible) {

        BadgeDrawable badge;

        // Reuse drawable if possible
        Drawable reuse = icon.findDrawableByLayerId(R.id.ic_badge);
        if (reuse != null && reuse instanceof BadgeDrawable) {
            badge = (BadgeDrawable) reuse;
        } else {
            badge = new BadgeDrawable();
        }

        badge.refreshBadge(isVisible);
        icon.mutate();
        icon.setDrawableByLayerId(R.id.ic_badge, badge);
    }

    private boolean isValidGroupId() {
        return mGroupId != -1;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.InfoImageView:
                navigateToGroupInfo();
                return true;
            case R.id.iv_urgent_msgs:
                navigateToUrgentMsgs();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void navigateToUrgentMsgs() {

        if (isValidGroupId()) {
            Intent infoIntent = new Intent(mContext, com.KryptosTextApp.KryptosText.activity.UrgentMsgsListActivity.class);
            infoIntent.putExtra("GroupId", mGroupId);
            startActivity(infoIntent);
        }
    }

    private void visibleUrgentMsgsBadgeVisibilty(boolean isBadgeVisibile) {
        if (urgentMsgsMenuItem != null) {
            LayerDrawable icon = (LayerDrawable) urgentMsgsMenuItem.getIcon();
            setBadgeCount(this, icon, isBadgeVisibile);
        }
    }

    private void navigateToGroupInfo() {
        if (isValidGroupId()) {
            Intent infoIntent = new Intent(mContext, com.KryptosTextApp.KryptosText.activity.GroupInfoActivity.class);
            infoIntent.putExtra("GroupId", mGroupId);
            infoIntent.putExtra("GroupName", mHeaderName);
            startActivity(infoIntent);
        }
    }


    @Override
    public void onBackPressed() {
//        if (!isPendingMsgs()) {
//            onBack();
//        } else {
//            new AlertDialog.Builder(this).setMessage("Pending msgs are Lost. Do you want to proceed?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                    onBack();
//                }
//            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            }).show();
//        }
        onBack();
    }

    private boolean isPendingMsgs() {
        boolean isPending = false;
        if (mChatList != null)
            for (int i = 0; i < mChatList.size(); i++) {
                if (!mChatList.get(i).isDelivered() && !mChatList.get(i).isRead()) {
                    isPending = true;
                    break;
                }
            }
        return isPending;
    }

    private void onBack() {
        if (isValidGroupId() || comingFromNotification)
            startActivity(new Intent(mContext, com.KryptosTextApp.KryptosText.activity.MainDrawerActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        else
            super.onBackPressed();
    }

    public void scrollToEnd(View view) {
        if (mChatListView != null) {
            mChatListView.smoothScrollToPosition(0);
        }
    }


    String outPath = "";

    public void compressVideoFile(Uri uri) {

//        int sourceFileSize = originalFileSize (sourceFilePath.getText().toString());
        String path = RealPathUtils.getRealPathFromURI(this, uri);
        int width = getVideoWidth(path);

//        Log.d("debug" , "Original file size:" + sourceFileSize );

        Log.d(TAG, "width:: " + width);

        if (width > 640) {
            try2CreateCompressDir();
            /*try2CreateCompressDir();

            compressionElapsedTime.setVisibility(View.VISIBLE);
            compressionElapsedTime.setText("");
            totalTime =0;
            timer = new Timer();
            TimerTask task = new TimerTask() {
                public void run() {
                    totalTime = totalTime + 1;
                    mHandler.obtainMessage(1).sendToTarget();
                }
            };
            timer.scheduleAtFixedRate(task, initialDelay, period);*/

            outPath = "";
            outPath = Environment.getExternalStorageDirectory()
                    + File.separator
                    + APP_DIR
                    + COMPRESSED_VIDEOS_DIR
//                    +"VIDEO_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".mp4";
                    + "Krypt_VIDEO_COMPRESSED" + ".mp4";

            new VideoCompressor().execute(path, outPath);

        } else {
            sendMediaToServerAndUpdateToSocket(mSelectedFile, mInitialUri, false, mSelectedFile.getName());

        }


    }

    class VideoCompressor extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(com.KryptosTextApp.KryptosText.activity.PHCChatActivity.this);
            mProgressDialog.setMessage("Processing...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressNumberFormat(null);
            mProgressDialog.setProgressPercentFormat(null);
//            mProgressDialog.setMax(100);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    cancel(true);
                }
            });
            mProgressDialog.show();

//            showProgress("Compressing..");
            Log.d(TAG, "Start video compression");
        }

        @Override
        protected Boolean doInBackground(String... params) {

            return MediaController.getInstance().convertVideo(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(Boolean compressed) {
            super.onPostExecute(compressed);
            mProgressDialog.dismiss();
            if (compressed) {
                String fileSize = getFileSize(outPath);
                File file = new File(outPath);
                sendMediaToServerAndUpdateToSocket(file, mInitialUri, false, mSelectedFile.getName());
                Log.d(TAG, "Compression successfully!" + fileSize);
            } else {
                showToast("Oops..,video compression failed");
                sendMediaToServerAndUpdateToSocket(mSelectedFile, mInitialUri, false, mSelectedFile.getName());

            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.d("VideoCompressor", "onCancelled..");
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.d("VideoCompressor", "onProgressUpdate.." + values[0]);

//            mProgressDialog.setProgress(values[0]);


        }
    }

    private int getVideoWidth(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path);
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);

        int w = 0;
        if (width != null) {
            w = Integer.parseInt(width);
        }

        return w;
    }

    private String getFileSize(String path) {
        String value = null;
        if (path != null && !path.equalsIgnoreCase("")) {
            File imageFile = new File(path);
            float length = imageFile.length() / 1024f; // Size in KB

            if (length >= 1024)
                value = length / 1024f + " MB";
            else
                value = length + " KB";

        }
        return value;
    }

    public static void try2CreateCompressDir() {
        File f = new File(Environment.getExternalStorageDirectory(), File.separator + APP_DIR);
        f.mkdirs();
        f = new File(Environment.getExternalStorageDirectory(), File.separator + APP_DIR + COMPRESSED_VIDEOS_DIR);
        f.mkdirs();
        f = new File(Environment.getExternalStorageDirectory(), File.separator + APP_DIR + TEMP_DIR);
        f.mkdirs();
    }
}


