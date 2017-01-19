package com.framgia.rss_6.ui.activity.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.framgia.rss_6.R;
import com.framgia.rss_6.data.model.ChannelModel;
import com.framgia.rss_6.data.model.DatabaseControl;
import com.framgia.rss_6.data.model.NewsModel;
import com.framgia.rss_6.ui.activity.DetailNewsActivity;
import com.framgia.rss_6.ui.adapter.NewsAdapter;
import com.framgia.rss_6.ultils.Constant;
import com.framgia.rss_6.ultils.NetworkConection;
import com.framgia.rss_6.ultils.XmlParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ListNewsFragment extends Fragment implements NewsAdapter.ItemClickListener {
    private List<NewsModel> mNewsModelList = new ArrayList<NewsModel>();
    private RecyclerView mRecyclerView;
    private NewsAdapter mNewsAdapter;
    private String mRsslink;
    private String mCategory;
    private DatabaseControl mDatabaseControl;
    private String mImageUrl;
    private ChannelModel mChannelModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_news, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstance) {
        super.onActivityCreated(savedInstance);
        getDataFromChannel();
        getDataFromXml();
        initView();
    }

    public void initView() {
        mDatabaseControl = new DatabaseControl(getActivity());
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycle_news);
        mNewsModelList = mDatabaseControl.getAllNewsFromData(mCategory);
        mNewsAdapter = new NewsAdapter(getActivity(), mNewsModelList);
        mNewsAdapter.setClickListener(this);
        mRecyclerView.setAdapter(mNewsAdapter);
        LinearLayoutManager linearLayoutManager =
            new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    private String getXmlFromUrl(String urlString) {
        StringBuilder content = new StringBuilder();
        try {
            URL url = new URL(urlString);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public void getDataFromChannel() {
        Intent intent = getActivity().getIntent();
        Bundle bundle = intent.getBundleExtra(Constant.BUNDLE_DATA);
        mChannelModel = (ChannelModel) bundle.getSerializable(Constant.BUNDLE_CHANNEL);
        mRsslink = mChannelModel.getRssLink();
        mCategory = mChannelModel.getCategory();
    }

    @Override
    public void onClick(View view, int position) {
        NewsModel newsModel = mNewsModelList.get(position);
        startActivity(DetailNewsActivity.getDetailNewsIntent(getActivity(), newsModel));
        newsModel.setAddDate(formatDate(new Date()));
        mDatabaseControl.addHistoryNewsToDatabase(newsModel);
    }

    public void getDataFromXml() {
        if (NetworkConection.isInternetConnected(getActivity())) {
            new ReadXMLAsync().execute(mRsslink);
        } else {
            Toast.makeText(getActivity(), R.string.msg_check_connect,
                Toast.LENGTH_SHORT).show();
        }
    }

    public String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return dateFormat.format(date);
    }

    public String getAuthor(String author) {
        return author.length() == 0 ? "" :
            author.substring(Constant.BEGIN_INDEX_AUTHOR, author.length() - 1);
    }

    class ReadXMLAsync extends AsyncTask<String, Integer, String> {
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setTitle(R.string.action_loading);
            mProgressDialog.setMessage(getActivity().getResources().getString(R.string
                .action_loading));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = getXmlFromUrl(strings[0]);
            String author;
            NewsModel newsModel = new NewsModel();
            XmlParser parser = new XmlParser();
            Document document = parser.getDocument(result);
            NodeList nodeList = document.getElementsByTagName(Constant.ITEM);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                NodeList itemchilds = node.getChildNodes();
                Element e = (Element) nodeList.item(i);
                newsModel.setTitle(parser.getValue(e, Constant.TITLE));
                newsModel.setDescription(parser.getValue(e, Constant.DESCRIPTION));
                newsModel.setPubDate(parser.getValue(e, Constant.PUBLISHDATE).substring
                    (Constant.BEGIN_INDEX_PUBLISHDAY, Constant.END_INDEX_PUBLISHDAY));
                author = parser.getValue(e, Constant.AUTHOR);
                newsModel.setAuthor(getAuthor(author));
                newsModel.setLink(parser.getValue(e, Constant.LINK));
                for (int j = 0; j < itemchilds.getLength(); j++) {
                    Node current = itemchilds.item(j);
                    if (current.getNodeName().equalsIgnoreCase(Constant.ENCLOSURE)) {
                        mImageUrl = current.getAttributes().item(0).getTextContent();
                    }
                }
                newsModel.setAddDate(formatDate(new Date()));
                newsModel.setImage(mImageUrl);
                newsModel.setCategory(mCategory);
                mDatabaseControl.addNewsToDatabase(newsModel);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mProgressDialog.dismiss();
            initView();
        }
    }
}

