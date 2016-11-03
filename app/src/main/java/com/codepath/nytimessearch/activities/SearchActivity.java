package com.codepath.nytimessearch.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codepath.nytimessearch.EndlessRecyclerViewScrollListener;
import com.codepath.nytimessearch.FilterDialogFragment;
import com.codepath.nytimessearch.R;
import com.codepath.nytimessearch.adapters.ArticleArrayAdapter;
import com.codepath.nytimessearch.models.Article;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity implements FilterDialogFragment.FilterDialogListener{

    EditText etQuery;
    Button btnSearch;
    RecyclerView rvResults;
    ArrayList<Article> articles;
    ArticleArrayAdapter adepter;
    private EndlessRecyclerViewScrollListener scrollListener;
    String url = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
    AsyncHttpClient client;
    private int maxHits;
    String query,beginDate,sortOrder;
    ArrayList<String> newsDesks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setupViews();
        if (!isNetworkAvailable()) {
            Log.e("Error", "onCreate: Network is not available", new Exception("Network is not available"));
        }

        if (!isOnline()) {
            Log.e("Error", "onCreate: Not online", new Exception("Not online."));
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager mgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = mgr.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process iprocess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = iprocess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    public void setupViews(){
        etQuery = (EditText)findViewById(R.id.etQuery);
        btnSearch = (Button)findViewById(R.id.btnSearch);
        rvResults = (RecyclerView) findViewById(R.id.rvResults);
        sortOrder = "Newest";
        newsDesks = new ArrayList<>();
        articles = new ArrayList<>();
        adepter = new ArticleArrayAdapter(this,articles);
        rvResults.setAdapter(adepter);
        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        rvResults.setLayoutManager(gridLayoutManager);
        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                Log.d("DEBUG", "onLoadMore, page " + page + ", totalItemsCount " + totalItemsCount + ", maxHits " + maxHits);
                if (totalItemsCount >= maxHits) {
                    return false;
                }
                loadNextDataFromApi(page);
                return true;
            }
        };
        // Adds the scroll listener to RecyclerView
        rvResults.addOnScrollListener(scrollListener);
        adepter.setOnItemClickListener(new ArticleArrayAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(SearchActivity.this, "Loading....", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(),ArticleActivity.class);
                Article article = articles.get(position);
                i.putExtra("article",article);
                startActivity(i);
            }
        });
    }

        public void loadNextDataFromApi(final int page) {

            Log.d("DEBUG", "loadNextDataFromApi: page " + page);
            query = etQuery.getText().toString();
            Toast.makeText(this,"Loading....",Toast.LENGTH_LONG).show();
            client = new AsyncHttpClient();

            RequestParams params = new RequestParams();
            params.put("api-key","b9610e49bc2a46d6b208b56527249c7d");
            params.put("q",query);
            params.put("page",page);
            params.put("sort",sortOrder);
            if(beginDate != null) {
                params.put("begin_date", beginDate);
            }
            
            if (newsDesks.size() > 0) {
                String newsDeskValue = "";
                for(int i = 0; i < newsDesks.size(); i++) {
                    newsDeskValue = newsDeskValue + "\"" + newsDesks.get(i) + "\" ";
                }
                newsDeskValue = "news_desk:(" + newsDeskValue + ")";
                params.put("fq", newsDeskValue);
            }
            Log.d("DEBUG", params.toString());


            if(page == 0) {
                articles.clear();
                adepter.notifyDataSetChanged();
                scrollListener.resetState();
            }

            client.get(url, params, new JsonHttpResponseHandler(){

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Log.d("Debug", response.toString());
                    JSONArray articleJsonResults = null;
                    try {
                        articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                        if (page == 0) {
                            maxHits = response.getJSONObject("response").getJSONObject("meta").getInt("hits");
                        }
                        articles.addAll(Article.fromJsonArray(articleJsonResults));
                        adepter.notifyDataSetChanged();
                        Log.d("Debug", articles.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                }
            });

        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_filter) {
            showFilterDialog();
             return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onArticleSearch(View view) {
        loadNextDataFromApi(0);
    }

    private void showFilterDialog() {
        FragmentManager fm = getSupportFragmentManager();
        FilterDialogFragment filterDialogFragment = FilterDialogFragment.newInstance("Filter list");
        filterDialogFragment.show(fm, "fragment_filter");
    }


    @Override
    public void onFinishFilterDialog(String date, String sort, ArrayList<String> newsDesk) {
        beginDate = date;
        sortOrder = sort;
        newsDesks = newsDesk;
    }
}
