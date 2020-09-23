package com.example.cryptomoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TransactionActivity extends AppCompatActivity {

    private List<Record> recordList;
    private SwipeRefreshLayout swipeRefresh;
    private RecordAdapter adapter;
    private Integer account_id;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)  {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresher);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshtransaction();
            }
        });

        recordList =  (List<Record>) getIntent().getSerializableExtra("recordList");
        account_id = getIntent().getIntExtra("account_id",0);
        if (recordList == null) return;
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecordAdapter(recordList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:
        }
        return true;
    }

    public void refreshtransaction() {
        final String transactionRequest ="request=" + URLEncoder.encode("transaction") + "&id="+ URLEncoder.encode(account_id.toString());

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String response = PostService.Post(transactionRequest);
                if (response != null) {

                    try {
                        Thread.sleep(1500);
//                        List<Record> recordList = new ArrayList<>();
                        recordList.clear();
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Record record = new Record();
                            record.setIndex(jsonObject.getInt("index"));
                            record.setFrom(jsonObject.getInt("from_account"));
                            record.setTo(jsonObject.getInt("to_account"));
                            record.setTime(jsonObject.getString( "time"));
                            record.setValue(jsonObject.getDouble("value"));
                            recordList.add(record);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (recordList == null) return;
                            adapter.notifyDataSetChanged();
                            swipeRefresh.setRefreshing(false);
                        }
                    });
                }
            }
        }).start();
    }
}