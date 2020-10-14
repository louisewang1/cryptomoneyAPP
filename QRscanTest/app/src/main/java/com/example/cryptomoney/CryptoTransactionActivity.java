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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class CryptoTransactionActivity extends AppCompatActivity {

    private List<CryptoRecord> recordList;
    private SwipeRefreshLayout swipeRefresh;
    private CryptoRecordAdapter adapter;
    private Integer account_id;
    private RecyclerView recyclerView;
    private Spinner spinner = null;
    private ArrayAdapter<String> merchantlistAdapter = null;
    private String[] merchantList;
    private String merchant_selected;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto_transaction);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)  {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        spinner = (Spinner)findViewById(R.id.spinner);

        recordList =  (List<CryptoRecord>) getIntent().getSerializableExtra("recordList");
        account_id = getIntent().getIntExtra("account_id",0);
        type = getIntent().getStringExtra("type");
        if (recordList == null) return;
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new CryptoRecordAdapter(recordList);
        recyclerView.setAdapter(adapter);
        adapter.setid(account_id);

        final String merchantlistRequest ="request=" + URLEncoder.encode("merchantlist");
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String response = PostService.Post(merchantlistRequest);
//                System.out.println(response)
                if (response != null) {
                    try {
                        List<String> array = new ArrayList<>();
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            array.add(jsonObject.getString("username"));
                        }
                        merchantList = new String[array.size()+1];
                        merchantList[0] = "None";
                        for (int i=1;i<array.size()+1;i++) {
                            merchantList[i] = array.get(i-1);
                        }
//                        System.out.println(merchantList);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                merchantlistAdapter = new ArrayAdapter<String>(CryptoTransactionActivity.this,android.R.layout.simple_spinner_item,merchantList);
                                merchantlistAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(merchantlistAdapter);

                                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                        merchant_selected = (String) ((TextView)view).getText();
                                        System.out.println(merchant_selected);
                                        adapter.setmerchant(merchant_selected);
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> adapterView) {
                                    }
                                });
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresher);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshcryptotransaction();
            }
        });

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

    public void refreshcryptotransaction() {
        final String cryptotransactionRequest ="request=" + URLEncoder.encode("cryptotransaction") + "&id="+ URLEncoder.encode(account_id.toString());

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String response = PostService.Post(cryptotransactionRequest);
                if (response != null) {

                    try {
                        Thread.sleep(1500);
//                        List<Record> recordList = new ArrayList<>();
                        recordList.clear();
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            CryptoRecord record = new CryptoRecord();
                            record.setIndex(jsonObject.getInt("index"));
                            record.setAddr(jsonObject.getString("address"));
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