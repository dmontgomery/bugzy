package com.bluestacks.bugzy.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bluestacks.bugzy.BaseActivity;
import com.bluestacks.bugzy.net.FogbugzApiService;
import com.bluestacks.bugzy.utils.AppExecutors;
import com.bluestacks.bugzy.R;
import com.bluestacks.bugzy.common.Const;
import com.bluestacks.bugzy.models.resp.Case;
import com.bluestacks.bugzy.models.resp.CaseEvent;
import com.bluestacks.bugzy.utils.PrefsHelper;
import com.bumptech.glide.Glide;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CaseDetailsActivity extends BaseActivity {
    @BindView(R.id.main_container)
    protected LinearLayout mContainer;

    @BindView(R.id.priority_indicator)
    protected LinearLayout mPriorityIndicator;

    @BindView(R.id.recyclerView)
    protected RecyclerView mRecyclerView;

    @BindView(R.id.progressBar)
    protected ProgressBar mProgress;

    @BindView(R.id.textview_bug_id)
    protected TextView mBugId;

    @BindView(R.id.textview_bug_title)
    protected TextView mBugTitle;

    @BindView(R.id.textview_active_status)
    protected TextView mActiveStatus;

    @BindView(R.id.textview_assigned_to)
    protected TextView mAssignedTo;

    @BindView(R.id.textview_milestone)
    protected TextView mMileStone;

    @BindView(R.id.textview_required_merge)
    protected TextView mRequiredMerge;

    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;

    private LinearLayoutManager mLinearLayoutManager;
    private Case mCase;
    private String mFogBugzId;
    public static String mToken;
    private RecyclerAdapter mAdapter;

    @Inject
    PrefsHelper mPrefs;

    @Inject
    FogbugzApiService mApiClient;

    @Inject
    AppExecutors mAppExecutors;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_details);
        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        mFogBugzId = extras.getString("bug_id");
        mCase = (Case) extras.getSerializable("bug");

        setupToolbar();

        mToken = mPrefs.getString(PrefsHelper.Key.ACCESS_TOKEN);
        showLoading();
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        showCaseInfo(mCase);
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Case: " + mFogBugzId);
    }

    @UiThread
    protected void showCaseInfo(Case caseEvents) {
        showContent();

        // TODO: Show actionIcons
        // TODO show title    mcase.getIxBugId

        List<CaseEvent> evs = mCase.getCaseevents();
        Collections.reverse(evs);
        mAdapter = new RecyclerAdapter(evs);
        mRecyclerView.setAdapter(mAdapter);
        mBugId.setText(String.valueOf(mCase.getIxBug()));
        mBugTitle.setText(String.valueOf(mCase.getTitle()));
        mAssignedTo.setText(String.valueOf(mCase.getPersonAssignedTo()));
        mMileStone.setText(String.valueOf(mCase.getFixFor()));
        mActiveStatus.setText(String.valueOf(mCase.getStatus()));
        Case bug = mCase;
        Log.d(Const.TAG," " + mCase.getFixFor());
        Log.d(Const.TAG," " + mCase.getProjectArea());
        if(bug.getPriority() == 3){
            mPriorityIndicator.setBackgroundColor(Color.parseColor("#e74c3c"));
        }
        else if(bug.getPriority() == 5) {
            mPriorityIndicator.setBackgroundColor(Color.parseColor("#ddb65b"));
        }
        else if(bug.getPriority() == 4) {
            mPriorityIndicator.setBackgroundColor(Color.parseColor("#95a5a6"));
        }
        else if(bug.getPriority() == 7) {
            mPriorityIndicator.setBackgroundColor(Color.parseColor("#bdc3c7"));
        }
        else {
            mPriorityIndicator.setBackgroundColor(Color.parseColor("#ecf0f1"));
        }
    }

    @UiThread
    protected void showLoading() {
        mProgress.setVisibility(View.VISIBLE);
        mContainer.setVisibility(View.GONE);
    }

    @UiThread
    protected void showContent() {
        mProgress.setVisibility(View.GONE);
        mContainer.setVisibility(View.VISIBLE);
    }

    public class RecyclerAdapter extends RecyclerView.Adapter<BugHolder> {

        private List<CaseEvent> mBugs;
        public RecyclerAdapter(List<CaseEvent> bugs) {
            mBugs = bugs ;
        }
        @Override
        public BugHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflatedView;
            switch(viewType) {
                case 0:
                    inflatedView =LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.bug_event_row_begin, parent, false);
                    break;

                case 2:
                    inflatedView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.bug_event_row_end, parent, false);
                    break;

                default:
                    inflatedView = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.bug_event_row, parent, false);
                    break;
            }
            return new BugHolder(inflatedView, CaseDetailsActivity.this);
        }

        @Override
        public void onBindViewHolder(BugHolder holder, int position) {
            CaseEvent bug = mBugs.get(position);
            holder.bindData(bug);
        }

        @Override
        public int getItemCount() {
            return mBugs.size();
        }

        @Override
        public int getItemViewType(int position) {
            if(position == 0) {
                return 0;
            }
            else if(position == mBugs.size()-1) {
                return 2;
            }
            return 1;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public static class BugHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mItemDate;
        private TextView mItemDescription;
        private TextView mChanges;
        private TextView mChangesContent;
        private ImageView mImageAttachment;
        private CaseEvent mBug;
        private CaseDetailsActivity mContext;

        //4
        public BugHolder (View v, CaseDetailsActivity context) {
            super(v);
            mItemDate = (TextView) v.findViewById(R.id.item_id);
            mItemDescription = (TextView) v.findViewById(R.id.item_description);
            mChanges = (TextView) v.findViewById(R.id.changes);
            mChangesContent = (TextView) v.findViewById(R.id.change_content);
            mImageAttachment = (ImageView) v.findViewById(R.id.attachment);
            mContext = context;
            v.setOnClickListener(this);
        }

        //5
        @Override
        public void onClick(View v) {
            Log.d("RecyclerView", "CLICK!");
        }

        public void bindData(CaseEvent bug) {
            mBug = bug;
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            DateFormat format2 = new SimpleDateFormat("MMMM dd, yyyy, hh:mm a", Locale.US);
            try {
                Date d = formatter.parse(String.valueOf(bug.getDate()));
                mItemDate.setText(format2.format(d));
            }catch(ParseException e) {
                mItemDate.setText(String.valueOf(bug.getDate()));
                Log.d(Const.TAG,"Failed to parse tags");
            }

            mItemDescription.setText(Html.fromHtml( bug.getEventDescription()));
            if(!TextUtils.isEmpty(bug.getContentHtml())) {
                mChangesContent.setText(Html.fromHtml(bug.getContentHtml()));
            }
            else if(!TextUtils.isEmpty(bug.getContent())) {
                mChangesContent.setText(Html.fromHtml(bug.getContent()));
            }
            else {
                mChangesContent.setVisibility(View.GONE);
            }

            if(!TextUtils.isEmpty(bug.getsChanges())) {
                mChanges.setText(Html.fromHtml(bug.getsChanges()));
            }
            else {
                mChanges.setVisibility(View.GONE);
            }

            if(bug.getsAttachments().size()>0) {
                Log.d(Const.TAG,bug.getsAttachments().get(0).getUrl());
                if(bug.getsAttachments().get(0).getFilename().endsWith(".png") || bug.getsAttachments().get(0).getFilename().endsWith(".jpg") ) {
                    mImageAttachment.setVisibility(View.VISIBLE);
                    final String img_path = ("https://bluestacks.fogbugz.com/" + bug.getsAttachments().get(0).getUrl() + "&token=" + mToken).replaceAll("&amp;","&");
                    Log.d(Const.TAG,img_path);
                    Glide.with(mContext).load(img_path)
                            .thumbnail(Glide.with(mContext).load(R.drawable.loading_ring))
                            .into(mImageAttachment);

                    mImageAttachment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Bundle arg = new Bundle();
                            arg.putString("img_path", img_path);
                            Intent i  = new Intent(mContext, FullScreenImageActivity.class);
                            i.putExtras(arg);
                            mContext.startActivity(i);
                        }
                    });
                }
            }
            else {
                mImageAttachment.setVisibility(View.GONE);
            }
        }
    }
}
