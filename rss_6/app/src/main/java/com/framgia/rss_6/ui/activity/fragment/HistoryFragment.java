package com.framgia.rss_6.ui.activity.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.framgia.rss_6.R;
import com.framgia.rss_6.data.model.DatabaseControl;
import com.framgia.rss_6.data.model.NewsModel;
import com.framgia.rss_6.ui.activity.DetailNewsActivity;
import com.framgia.rss_6.ui.adapter.NewsAdapter;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment implements NewsAdapter
    .ItemClickListener {
    private List<NewsModel> mModelList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private NewsAdapter mNewsAdapter;
    private DatabaseControl mDatabaseControl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history_news, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstance) {
        super.onActivityCreated(savedInstance);
    }

    public void initView() {
        mDatabaseControl = new DatabaseControl(getActivity());
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycle_history_news);
        mModelList = mDatabaseControl.getAllHistoryNewsFromData();
        mNewsAdapter = new NewsAdapter(getActivity(), mModelList);
        mNewsAdapter.setClickListener(this);
        mRecyclerView.setAdapter(mNewsAdapter);
        LinearLayoutManager linearLayoutManager =
            new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void onClick(View view, int position) {
        NewsModel newsModel = mModelList.get(position);
        startActivity(DetailNewsActivity.getDetailNewsIntent(getActivity(), newsModel));
    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
    }
}
