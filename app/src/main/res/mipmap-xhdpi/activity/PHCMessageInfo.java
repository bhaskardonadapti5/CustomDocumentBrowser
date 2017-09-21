package com.KryptosTextApp.KryptosText.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.adapter.PHCMessageInfoAdapter;
import com.KryptosTextApp.KryptosText.constant.MediaTypeConst;
import com.KryptosTextApp.KryptosText.model.PHCChatModel;
import com.KryptosTextApp.KryptosText.model.PHCInfoList;
import com.KryptosTextApp.KryptosText.model.PHCMessageInfoResponseModel;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;
import com.KryptosTextApp.KryptosText.utils.Log;
import com.KryptosTextApp.KryptosText.utils.PHCDateFormatter;
import com.KryptosTextApp.KryptosText.utils.StringToUnEscape;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;

import java.text.ParseException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * user :  avinashj
 * date : 3/24/2017
 * description :
 */

public class PHCMessageInfo extends BaseAppCompatActivity {

    private static final String TAG = "PHCMessageInfo";
    private Context mContext;
    private ExpandableListView mInfoList;
    private PHCMessageInfoAdapter mMessageInfoAdapter;
    private PHCChatModel mPhcChatModel;
    private TextView mRecalledMsgText, mTxtMsg, mDate;
    private ImageView chatImageView;
    private ImageView documentImageView;
    private RelativeLayout videoLayout;
    private LinearLayout mchatLayout;
    private ImageView vedioImageView;
    private PHCMessageInfoResponseModel mMessageResponseModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_info);
        mContext = com.KryptosTextApp.KryptosText.activity.PHCMessageInfo.this;
        mPhcChatModel = (PHCChatModel) getIntent().getSerializableExtra("messageMode");
        toolBarCreation();
        getView();

        setMessageDetail();

        getInfoList();
    }

    private void getView() {
        mInfoList = (ExpandableListView) findViewById(R.id.infoList);
        mTxtMsg = (TextView) findViewById(R.id.txtMsg);
        mRecalledMsgText = (TextView) findViewById(R.id.recalledMsgText);
        mDate = (TextView) findViewById(R.id.deliverTimeAndStatus);
        chatImageView = (ImageView) findViewById(R.id.chatImageView);
        mchatLayout = (LinearLayout) findViewById(R.id.chatLayout);
        videoLayout = (RelativeLayout) findViewById(R.id.vedioLayout);
        vedioImageView = (ImageView) findViewById(R.id.vedioImageView);

        documentImageView = (ImageView) findViewById(R.id.documentImageView);
        ImageView mIvStatus = (ImageView) findViewById(R.id.iv_msg_status);
        mIvStatus.setVisibility(View.GONE);

    }

    private void toolBarCreation() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mContext.getResources().getString(R.string.message_info));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setMessageDetail() {

        showTextAndMediaData(mPhcChatModel);
        String date = null;
        try {
            if (mPhcChatModel.getCreatedDate() != null)
                date = PHCDateFormatter.getChatDateFromserverDate(mPhcChatModel.getCreatedDate());
            else
                date = "";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (mPhcChatModel.isDelivered()) {
            mDate.setText("Delivered " + date);
        } else if (mPhcChatModel.isRead()) {
            mDate.setText("Read " + date);
        }
//            if(model.isDelivered())
        mDate.setText(PHCDateFormatter.getDateFromserverDate(mPhcChatModel.getCreatedDate()));
    }

    private void getInfoList() {

        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<PHCMessageInfoResponseModel> call = apiService.messageInfoData(getApplicationData().getAuthToken(), mPhcChatModel.getMessageId());
        call.enqueue(new Callback<PHCMessageInfoResponseModel>() {
            @Override
            public void onResponse(Call<PHCMessageInfoResponseModel> call, Response<PHCMessageInfoResponseModel> response) {
                Log.d(TAG, "getMsgInfoList URL " + response.raw().request().url());
                Log.d(TAG, "getMsgInfoList Resp " + new Gson().toJson(response.body()));
                mMessageResponseModel = response.body();
                if (isSessionInvalid(mMessageResponseModel.getStatusCode(),mMessageResponseModel.getStatusMessage())) {
                    return;
                }
                if (mMessageResponseModel != null && mMessageResponseModel.getStatusCode() == 1) {

                    mMessageInfoAdapter = new PHCMessageInfoAdapter(mContext, getMessageInfoList(mMessageResponseModel));
                    mInfoList.setAdapter(mMessageInfoAdapter);

                    // setting the expandable listview height dynamically since it is in ScrollView
                    setListViewHeight(mInfoList);

                }
            }

            @Override
            public void onFailure(Call<PHCMessageInfoResponseModel> call, Throwable t) {

            }
        });
    }

    private ArrayList<PHCInfoList> getMessageInfoList(PHCMessageInfoResponseModel mMessageResponseModel) {
        ArrayList<PHCInfoList> mInfoLists = new ArrayList<>();
        PHCInfoList infoFirstModel = new PHCInfoList();
        PHCInfoList infoSecondModel = new PHCInfoList();
        infoFirstModel.setGroupName("Delivered To");
        infoSecondModel.setGroupName("Read By");
        for (int j = 0; j < mMessageResponseModel.getData().size(); j++) {
            if (!mMessageResponseModel.getData().get(j).isUserStatus()) {
                infoFirstModel.setChildName(mMessageResponseModel.getData().get(j).getUserDetails());
            } else {
                infoSecondModel.setChildName(mMessageResponseModel.getData().get(j).getUserDetails());
            }
        }
        mInfoLists.add(infoFirstModel);
        mInfoLists.add(infoSecondModel);
        return mInfoLists;
    }

    private void showTextAndMediaData(PHCChatModel model) {
        byte[] byteData;
        String text = "";
        if (model.getMessageText() != null && !model.getMessageText().equalsIgnoreCase("")) {
            try {
                byteData = Base64.decode(model.getMessageText(), Base64.DEFAULT);
                text = new String(byteData, "UTF-8");
            } catch (Exception e) {
                text = model.getMessageText();
            }
            if (model.isUrgent()) {
                mTxtMsg.setTextColor(ContextCompat.getColor(mContext, R.color.colorTextBlack));

                if (model.isSender()) {
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        mchatLayout.setBackground(mContext.getResources().getDrawable(R.drawable.sender_urgent));
                    } else {
                        mchatLayout.setBackground(mContext.getResources().getDrawable(R.drawable.sender_urgent));
                    }
                } else {
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        mchatLayout.setBackground(mContext.getResources().getDrawable(R.drawable.receiver_urgent));
                    } else {
                        mchatLayout.setBackground(mContext.getResources().getDrawable(R.drawable.receiver_urgent));
                    }
                }
            } else {
                if (model.isSender()) {
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        mchatLayout.setBackground(ContextCompat.getDrawable(mContext, R.drawable.chat_blue));
                    } else {
                        mchatLayout.setBackground(ContextCompat.getDrawable(mContext, R.drawable.chat_blue));
                    }
                } else {
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        mchatLayout.setBackground(mContext.getResources().getDrawable(R.drawable.chat_gray));
                    } else {
                        mchatLayout.setBackground(mContext.getResources().getDrawable(R.drawable.chat_gray));
                    }
                }
            }

        }
        switch (model.getMessageType()) {
            case 2: //Image

                mTxtMsg.setVisibility(View.GONE);
                chatImageView.setVisibility(View.VISIBLE);
                videoLayout.setVisibility(View.GONE);
                documentImageView.setVisibility(View.GONE);
                if (model.isSender()) {
                    if (model.isRecalled()) {
                        mRecalledMsgText.setVisibility(View.VISIBLE);

                    } else {
                        mRecalledMsgText.setVisibility(View.GONE);
                    }
                    Glide.with(mContext).load(text).placeholder(R.drawable.nodocument2)
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(chatImageView);
                } else {
                    if (model.isRecalled()) {
                        mRecalledMsgText.setVisibility(View.VISIBLE);
                        mRecalledMsgText.setText(Html.fromHtml(StringToUnEscape.unescapeJava(text)));

                    } else {
                        mRecalledMsgText.setVisibility(View.GONE);
                        Glide.with(mContext).load(text).placeholder(R.drawable.nodocument2)
                                .crossFade()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(chatImageView);
                    }
                }


                break;
            case 3:  //Video
                mTxtMsg.setVisibility(View.GONE);
                chatImageView.setVisibility(View.GONE);
                videoLayout.setVisibility(View.VISIBLE);
                documentImageView.setVisibility(View.GONE);

                if (model.isSender()) {
                    if (model.isRecalled()) {
                        mRecalledMsgText.setVisibility(View.VISIBLE);

                    } else {
                        mRecalledMsgText.setVisibility(View.GONE);
                    }
                    Glide.with(mContext).load(model.getThumbnailURL()).placeholder(R.drawable.nodocument2)
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(vedioImageView);
                } else {
                    if (model.isRecalled()) {
                        videoLayout.setVisibility(View.GONE);
                        mRecalledMsgText.setVisibility(View.VISIBLE);
                        mRecalledMsgText.setText(Html.fromHtml(StringToUnEscape.unescapeJava(text)));


                    } else {
                        mRecalledMsgText.setVisibility(View.GONE);
                        Glide.with(mContext).load(model.getThumbnailURL()).placeholder(R.drawable.nodocument2)
                                .crossFade()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(vedioImageView);
                    }
                }

//                    Glide.with(mContext).load(text)
//                            .thumbnail(0.5f)
//                            .crossFade()
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .into(myViewHolder.vedioImageView);
                break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                mTxtMsg.setVisibility(View.GONE);
                chatImageView.setVisibility(View.GONE);
                videoLayout.setVisibility(View.GONE);
                documentImageView.setVisibility(View.VISIBLE);

                if (model.isSender()) {
                    if (model.isRecalled()) {
                        mRecalledMsgText.setVisibility(View.VISIBLE);

                    } else {
                        mRecalledMsgText.setVisibility(View.GONE);
                    }
                } else {
                    if (model.isRecalled()) {
                        documentImageView.setVisibility(View.GONE);
                        mRecalledMsgText.setVisibility(View.VISIBLE);
                        mRecalledMsgText.setText(Html.fromHtml(StringToUnEscape.unescapeJava(text)));
                    } else {
                        mRecalledMsgText.setVisibility(View.GONE);
                    }
                }
//                    Glide.with(mContext).load(text)
//                            .thumbnail(0.5f)
//                            .crossFade()
//                            .diskCacheStrategy(DiskCacheStrategy.ALL)
//                            .into(myViewHolder.documentImageView);
                switch (model.getMediaType()) {
                    case MediaTypeConst.DOC:
                        documentImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_word_document));
                        break;
                    case MediaTypeConst.PDF:
                        documentImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_pdf));
                        break;
                    case MediaTypeConst.EXCEL:
                        documentImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_excel));
                        break;
                    case MediaTypeConst.PPT:
                        documentImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_ppt));
                        break;
                    case MediaTypeConst.TXT:
                        documentImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_txt));
                        break;
                    case MediaTypeConst.CSV:
                        documentImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_csv));
                        break;
                    default:
                        documentImageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.img_otherdoc));
                }
                break;
            default:
                mTxtMsg.setVisibility(View.VISIBLE);
                chatImageView.setVisibility(View.GONE);
                videoLayout.setVisibility(View.GONE);
                documentImageView.setVisibility(View.GONE);
//                    myViewHolder.mTxtMsg.setText(Html.fromHtml(text));
                if (model.isSender()) {
                    if (model.isRecalled()) {
                        mRecalledMsgText.setVisibility(View.VISIBLE);


                    } else {
                        mRecalledMsgText.setVisibility(View.GONE);
                    }
                    mTxtMsg.setText(Html.fromHtml(StringToUnEscape.unescapeJava(text)));
                } else {
                    if (model.isRecalled()) {

                        mTxtMsg.setVisibility(View.GONE);
                        mRecalledMsgText.setVisibility(View.VISIBLE);
                        mRecalledMsgText.setText(Html.fromHtml(StringToUnEscape.unescapeJava(text)));

                    } else {
                        mRecalledMsgText.setVisibility(View.GONE);
                        mTxtMsg.setText(Html.fromHtml(StringToUnEscape.unescapeJava(text)));
                    }
                }


        }
    }


    private void setListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }


}
