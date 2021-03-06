package in.bugzy.ui.home;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import in.bugzy.data.model.Status;
import in.bugzy.ui.common.AppliedSortAdapter;
import in.bugzy.ui.common.CaseAdapter;
import in.bugzy.ui.common.ErrorView;
import in.bugzy.ui.common.Injectable;
import in.bugzy.ui.common.HomeActivityCallbacks;
import in.bugzy.R;
import in.bugzy.data.model.Case;
import in.bugzy.ui.common.ItemOffsetDecoration;
import in.bugzy.utils.OnItemClickListener;
import com.xiaofeng.flowlayoutmanager.FlowLayoutManager;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyCasesFragment extends Fragment implements Injectable, OnItemClickListener {
    private static final String TAG = MyCasesFragment.class.getName();
    private static final String PARAM_FILTER = "filter";
    private static final String PARAM_FILTER_TEXT = "filter_text";
    private MyCasesViewModel mViewModel;
    private List<Case> mCases;
    private List<String> mAppliedSortOrders;
    private String mFilter;
    private String mFilterText;
    private HomeActivityCallbacks mHomeActivityCallbacks;
    private CaseAdapter mAdapter;
    private AppliedSortAdapter mAppliedSortingsAdapter;
    private Snackbar mSyncSnackbar;
    private Snackbar mRetrySnackbar;


    @Inject
    protected ViewModelProvider.Factory mViewModelFactory;

    @BindView(R.id.recyclerView)
    protected RecyclerView mRecyclerView;

    @BindView(R.id.viewError)
    protected ErrorView mErrorView;

    @BindView(R.id.containerSorting)
    protected LinearLayout mSortingContainer;

    @BindView(R.id.recyclerViewSorting)
    protected RecyclerView mSortingRecyclerView;

    public static MyCasesFragment getInstance(String filter, String filterText) {
        MyCasesFragment fragment = new MyCasesFragment();
        Bundle args = new Bundle();
        args.putString(PARAM_FILTER, filter);
        args.putString(PARAM_FILTER_TEXT, filterText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFilter = getArguments().getString(PARAM_FILTER);
        mFilterText = getArguments().getString(PARAM_FILTER_TEXT);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof HomeActivityCallbacks) {
            mHomeActivityCallbacks = (HomeActivityCallbacks) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mycases, null);
        ButterKnife.bind(this, v);
        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupSortingView();
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(MyCasesViewModel.class);

        if (mHomeActivityCallbacks != null) {
            mHomeActivityCallbacks.onFragmentsActivityCreated(this, mFilterText, getTag());
        }
        this.subscribeToViewModel();


        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity().getApplicationContext(), DividerItemDecoration.VERTICAL));
        mAdapter = new CaseAdapter(mCases, this);
        mRecyclerView.setAdapter(mAdapter);

        mViewModel.loadCases(mFilter);  // Load cases
    }

    public Snackbar getSyncSnackbar() {
        Snackbar bar = Snackbar.make(getView(), "Syncing..", Snackbar.LENGTH_INDEFINITE);
        ViewGroup contentLay = (ViewGroup) bar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        ProgressBar item = new ProgressBar(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(inDp(24), inDp(24));
        params.gravity = Gravity.CENTER_VERTICAL;
        item.setLayoutParams(params);
        contentLay.addView(item,0);
        return bar;
    }

    public Snackbar getRetrySnackbar(String message) {
        Snackbar snackbar = Snackbar
                .make(getView(), message, Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mViewModel.retryClicked();
                    }
                });
        return snackbar;
    }

    public void setupSortingView() {
        mAppliedSortingsAdapter = new AppliedSortAdapter(3);
        mAppliedSortingsAdapter.setItemClickListener((position, view) -> {
            PopupMenu popupMenu = new PopupMenu(getActivity(), view);
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Remove")) {
                    mViewModel.removeSortClicked(position);
                } else {
                    showSortOrderSelectionMenu(view, position);
                }
                return true;
            });
            popupMenu.getMenu().add("Replace");
            popupMenu.getMenu().add("Remove");
            popupMenu.show();
        });
        FlowLayoutManager manager = new FlowLayoutManager();
        manager.setAutoMeasureEnabled(true);
        mSortingRecyclerView.setLayoutManager(manager);
        mSortingRecyclerView.addItemDecoration(new ItemOffsetDecoration(
                (int)TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics()
                )
        ));
        mSortingRecyclerView.setAdapter(mAppliedSortingsAdapter);
        mAppliedSortingsAdapter.setOnAddClickListener(v -> {
            showSortOrderSelectionMenu(v, -1);
       });
    }

    public void showSortOrderSelectionMenu(View v, int replaceWith) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.setOnMenuItemClickListener(item -> {
            mViewModel.onSortSelected(item.getTitle().toString(), replaceWith);
            return true;
        });
        for (String sortOrder : mViewModel.getAvailableSortOrders()) {
            popupMenu.getMenu().add(sortOrder);
        }
        popupMenu.show();
    }

    @Override
    public void onItemClick(int position) {
        if (mCases == null) {
            return;
        }
        if (mHomeActivityCallbacks != null) {
            mHomeActivityCallbacks.onCaseSelected(mCases.get(position));
        }
    }

    private void subscribeToViewModel() {
        mViewModel.getCasesState().observe(this, resourceState -> {
            if (resourceState.data != null) {
                showCases(resourceState.data.getCases());
                showSortOrders(resourceState.data.getAppliedSortOrders());
            }
            if (resourceState.status == Status.LOADING) {
                showLoading();
                return;
            }
            if (resourceState.status == Status.ERROR) {
                showError(resourceState.message);
                return;
            }
            if (resourceState.status == Status.SUCCESS) {
                this.hideLoading();
            }
        });
    }

    protected void showSortOrders(List<String> sortOrders) {
        mAppliedSortOrders = sortOrders;
        if (mAppliedSortOrders != null) {
            mAppliedSortingsAdapter.setData(mAppliedSortOrders);
            mAppliedSortingsAdapter.notifyDataSetChanged();
        }
    }

    @UiThread
    protected void showCases(List<Case> cases) {
        mCases = cases;
        showContent();
        mAdapter.setData(mCases);
        mAdapter.notifyDataSetChanged();
    }

    private void showRetrySnackbar(String message) {
        mRetrySnackbar = getRetrySnackbar(message);
        mRetrySnackbar.show();
    }

    private void hideRetrySnackbar() {
        if (mRetrySnackbar == null) {
            return;
        }
        mRetrySnackbar.dismiss();
        mRetrySnackbar = null;
    }

    private void showSyncProgress() {
        mSyncSnackbar = getSyncSnackbar();
        mSyncSnackbar.show();
    }

    private void hideSyncProgress() {
        if (mSyncSnackbar == null) {
            return;
        }
        mSyncSnackbar.dismiss();
        mSyncSnackbar = null;
    }

    @UiThread
    private void hideLoading() {
        mErrorView.hide();
        hideSyncProgress();
//        hideRetrySnackbar();
    }

    @UiThread
    protected void showLoading() {
        if (mCases != null) {
            showSyncProgress();
            return;
        }
        mSortingRecyclerView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        mErrorView.showProgress("Fetching " + mFilterText + "..." );
        hideRetrySnackbar();
    }

    private int inDp(int dps) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps, getResources().getDisplayMetrics());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRetrySnackbar != null && mRetrySnackbar.isShown()) {
            mRetrySnackbar.dismiss();
        }
        if (mSyncSnackbar != null && mSyncSnackbar.isShown()) {
            mSyncSnackbar.dismiss();
        }
    }

    @UiThread
    protected void showContent() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mSortingRecyclerView.setVisibility(View.VISIBLE);
        mErrorView.hide();
    }

    @UiThread
    private void showError(String message) {
        if (mCases != null) {
            showRetrySnackbar(message);
            return;
        }
        mSortingRecyclerView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        mErrorView.showError(message);
        mErrorView.setOnClickListener(view -> {
            mViewModel.loadCases(mFilter);  // Load cases
            mErrorView.setOnClickListener(null);
        });
    }
}
