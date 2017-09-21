package com.KryptosTextApp.KryptosText.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.KryptosTextApp.KryptosText.R;
import com.KryptosTextApp.KryptosText.adapter.PHCAddGroupAdapter;
import com.KryptosTextApp.KryptosText.adapter.PHCSelectMemberAdapter;
import com.KryptosTextApp.KryptosText.fragment.ComposeMembersFragment;
import com.KryptosTextApp.KryptosText.model.GroupIdModel;
import com.KryptosTextApp.KryptosText.model.GroupIdRespModel;
import com.KryptosTextApp.KryptosText.model.PHCContactInitiateChatResponseModel;
import com.KryptosTextApp.KryptosText.model.PHCSelectMembersModel;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiClient;
import com.KryptosTextApp.KryptosText.serviceManager.PHCApiInterface;
import com.KryptosTextApp.KryptosText.utils.Log;
import com.google.gson.Gson;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ComposeMessageActivity extends com.KryptosTextApp.KryptosText.activity.BaseAppCompatActivity {

    private static final String TAG = "ComposeMessageActivity";

    public static final String MEMBERS_LIST = "Members";
    private static final String GROUPS_LIST = "Groups";

    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fabCompose;
    private ComposeMembersFragment memberFragment, groupFragment;
    public static ArrayList<PHCSelectMembersModel> selectedMembers;
    private Context mContext;
    private MaterialSearchView materialSearchView;
    private LinearLayout ll_process;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_message);
        mContext = com.KryptosTextApp.KryptosText.activity.ComposeMessageActivity.this;
        if (selectedMembers != null && selectedMembers.size() > 0)
            selectedMembers.clear();
        loadViews();
        loadListeners();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_fragment_conversations, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        materialSearchView.setMenuItem(item);
        return true;
    }

    private void loadListeners() {
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        fabCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMembers = new ArrayList<>();
                if (memberFragment != null) {
                    parseRecords(memberFragment.getContactsExpandableListAdapter(), MEMBERS_LIST);
                }
                if (groupFragment != null) {
                    parseRecords(groupFragment.getContactsExpandableListAdapter(), GROUPS_LIST);
                }
                if (selectedMembers.size() == 1) {
                    if (!selectedMembers.get(0).isGroup())
                        initiateContactChat(selectedMembers.get(0).getUserId());
                    else {
                        Intent composeIntent = new Intent(mContext, PHCChatActivity.class);
                        composeIntent.putExtra("isComposeMsg", true);
                        composeIntent.putExtra("headerName", getHeaderName());
                        composeIntent.putExtra("groupId", selectedMembers.get(0).getUserId());
                        composeIntent.putExtra("selectedMembers", selectedMembers);
                        startActivity(composeIntent);
                    }
//                } else if (selectedMembers.size() > 1 || (selectedMembers.size() != 0 && selectedMembers.get(0).isGroup())) {
                } else if (selectedMembers.size() > 1) {

                    getGroupIdBasedParticipants(new GroupIdModel(getParticipantsId(), getParticipantGroupsId()));
//                    Intent composeIntent = new Intent(mContext, PHCChatActivity.class);
//                    composeIntent.putExtra("headerName", getHeaderName());
//                    composeIntent.putExtra("groupId", -1);
//                    composeIntent.putExtra("isComposeMsg", true);
//                    composeIntent.putExtra("selectedMembers", selectedMembers);
//                    startActivity(composeIntent);
                } else {
                    Toast.makeText(mContext, "Select at least one Member/Group", Toast.LENGTH_SHORT).show();
                }
            }
        });

        materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Do some magic
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return false;
            }
        });

        materialSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });

    }

    private void initiateContactChat(Integer participantId) {
//        showProgress();
        String header = getApplicationData().getAuthToken();
        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<PHCContactInitiateChatResponseModel> call = apiService.initiateContactChat(header, participantId, getApplicationData().getUserID());
        call.enqueue(new Callback<PHCContactInitiateChatResponseModel>() {
            @Override
            public void onResponse(Call<PHCContactInitiateChatResponseModel> call, Response<PHCContactInitiateChatResponseModel> response) {
                dismissProgress();
                Log.d(TAG, "initiateContactChat URL " + response.raw().request().url());
                Log.d(TAG, "initiateContactChat Resp " + new Gson().toJson(response.body()));
                PHCContactInitiateChatResponseModel mContactInitiateChatModel = response.body();
                if (mContactInitiateChatModel != null && mContactInitiateChatModel.getStatusCode() == 1 || mContactInitiateChatModel.getStatusCode() ==200) {
                    Intent chatIntent = new Intent(mContext, PHCChatActivity.class);
                    chatIntent.putExtra("headerName", mContactInitiateChatModel.getData().getGroupName());
                    chatIntent.putExtra("groupId", mContactInitiateChatModel.getData().getGroupId());
                    mContext.startActivity(chatIntent);
                }
            }

            @Override
            public void onFailure(Call<PHCContactInitiateChatResponseModel> call, Throwable t) {
                Toast.makeText(mContext, "Something went wrong! Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getGroupIdBasedParticipants(GroupIdModel groupIdModel) {
        Log.e("ParticipantIds", "--" + groupIdModel.getParticipantIds());
        Log.e("Participant Group Ids", "--" + groupIdModel.getParticipantGroupIds());
//        showProgress();
        String header = getApplicationData().getAuthToken();
        PHCApiInterface apiService = PHCApiClient.getClient().create(PHCApiInterface.class);
        Call<GroupIdRespModel> call = apiService.getGroupId(header, groupIdModel);
        call.enqueue(new Callback<GroupIdRespModel>() {
            @Override
            public void onResponse(Call<GroupIdRespModel> call, Response<GroupIdRespModel> response) {
                dismissProgress();
                Log.d(TAG, "initiateContactChat URL " + response.raw().request().url());
                Log.d(TAG, "initiateContactChat Resp " + new Gson().toJson(response.body()));
                GroupIdRespModel genericRespModel = response.body();
                if (genericRespModel != null && genericRespModel.getStatusCode() == 200) {
                    Intent chatIntent = new Intent(mContext, PHCChatActivity.class);
                    chatIntent.putExtra("headerName", getHeaderName());
                    chatIntent.putExtra("groupId", genericRespModel.getData().getGroupId());
                    chatIntent.putExtra("isComposeMsg", true);
                    chatIntent.putExtra("selectedMembers", selectedMembers);
                    mContext.startActivity(chatIntent);
                }
            }

            @Override
            public void onFailure(Call<GroupIdRespModel> call, Throwable t) {
                Toast.makeText(mContext, "Something went wrong! Please try again" + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                dismissProgress();
            }
        });
    }


    private void parseRecords(ExpandableListAdapter contactsExpandableListAdapter, String fragmentType) {

        switch (fragmentType) {
            case MEMBERS_LIST:
                PHCSelectMemberAdapter mAdapter = (PHCSelectMemberAdapter) contactsExpandableListAdapter;
                selectedMembers.addAll(mAdapter.getSelectedMember());
                break;
            case GROUPS_LIST:
                selectedMembers.addAll(((PHCAddGroupAdapter) contactsExpandableListAdapter).getSelectedMember());
                break;
        }
    }

    private void loadViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mToolbarTitle = (TextView) toolbar.findViewById(R.id.toolbarTitle);
        mToolbarTitle.setVisibility(View.VISIBLE);
        mToolbarTitle.setText(getResources().getString(R.string.compose_message));
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        materialSearchView = (MaterialSearchView) findViewById(R.id.search_view);
//        ll_process = (LinearLayout) findViewById(R.id.ll_process);
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        fabCompose = (FloatingActionButton) findViewById(R.id.fab);
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_compose, menu);
//
//        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
//        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
//
//        AutoCompleteTextView searchTextView = (AutoCompleteTextView) search.findViewById(android.support.v7.appcompat.R.id.search_src_text);
//        try {
//            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
//            mCursorDrawableRes.setAccessible(true);
//            mCursorDrawableRes.set(searchTextView, R.drawable.cursor); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
//        } catch (Exception e) {
//        }
//        search.setOnQueryTextListener(this);
//
//        return true;
//    }
//
//    @Override
//    public boolean onQueryTextChange(String newText) {
//        search(newText);
//        return true;
//    }

    private void search(String searchString) {
        if (groupFragment != null && groupFragment.isVisible()) {
            PHCAddGroupAdapter groupsAdapter = (PHCAddGroupAdapter) groupFragment.getContactsExpandableListAdapter();
            groupsAdapter.filter(searchString);
        }
        if (memberFragment != null && memberFragment.isVisible()) {
            PHCSelectMemberAdapter membersAdapter = (PHCSelectMemberAdapter) memberFragment.getContactsExpandableListAdapter();
            membersAdapter.filter(searchString);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return true;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    memberFragment = ComposeMembersFragment.newInstance(MEMBERS_LIST);
                    return memberFragment;
                default:
                    groupFragment = ComposeMembersFragment.newInstance(GROUPS_LIST);
                    return groupFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private String getParticipantsId() {
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

    private String getParticipantGroupsId() {
        StringBuilder participantIds = new StringBuilder();
        for (int i = 0; i < selectedMembers.size(); i++) {
            if (selectedMembers.get(i).isGroup()) {
                participantIds.append("").append(selectedMembers.get(i).getUserId());
                if (i < selectedMembers.size() - 1) {
                    participantIds.append(",");
                }
            }
        }
        return participantIds.toString();
    }

//    public void dismissProgress() {
//        ll_process.setVisibility(View.GONE);
//        super.dismissProgress();
//    }

    private String getHeaderName() {
        StringBuilder participantIds = new StringBuilder();
        for (int i = 0; i < selectedMembers.size(); i++) {
            participantIds.append("").append(selectedMembers.get(i).getUserName());
            if (i < selectedMembers.size() - 1) {
                participantIds.append(",");
            }
        }
        participantIds.append(",").append(getApplicationData().getFirstName());
        return participantIds.toString();
    }
}


