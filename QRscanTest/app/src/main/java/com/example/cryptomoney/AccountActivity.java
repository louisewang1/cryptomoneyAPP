package com.example.cryptomoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.cryptomoney.utils.DBHelper;

import org.json.JSONObject;

import java.net.URLEncoder;

public class AccountActivity extends AppCompatActivity {

    // 定义控件和全局变量初始化
    private ImageButton back;
    private TextView username;
    private TextView balance;
    private TextView email;
    private TextView cellphone;
    private TextView ID;
    private Integer account_id;
    private String username_;
    private Double balance_;
    private String cellphone_;
    private String email_;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)  {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        // 接收MainActivity传入数据
        Intent intent = getIntent();
        account_id = intent.getIntExtra("id",0);
        username_ = intent.getStringExtra("username");
        balance_ = intent.getDoubleExtra("balance",0);
        email_ = intent.getStringExtra("email");
        cellphone_ = intent.getStringExtra("cellphone");

        // 绑定控件
        ID = (TextView) findViewById(R.id.id);
        username = (TextView) findViewById(R.id.username);
        balance = (TextView) findViewById(R.id.balance);
        email = (TextView) findViewById(R.id.email);
        cellphone = (TextView) findViewById(R.id.cellphone);

        // 获取输入
        ID.setText(account_id.toString());
        username.setText(username_);
        balance.setText(balance_.toString());
        email.setText(email_);
        cellphone.setText(cellphone_);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresher);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshaccount();
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

    public void refreshaccount(){
        final String accountInfoRequest ="request=" + URLEncoder.encode("accountinfo") +
                "&id="+ URLEncoder.encode(account_id.toString());

        new Thread(new Runnable() {
            @Override
            public void run() {

                final String response = PostService.Post(accountInfoRequest);
                if (response != null) {
                    try {
                        Log.d("AccountActivity","start refreshing");
                        Thread.sleep(1500);
                        JSONObject jsonObject = new JSONObject(response);
                        account_id = jsonObject.getInt("id");
                        username_ = jsonObject.getString("username");
                        balance_ = jsonObject.getDouble("balance");
                        email_ = jsonObject.getString("email");
                        cellphone_ = jsonObject.getString("cellphone");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ID.setText(account_id.toString());
                            username.setText(username_);
                            balance.setText(balance_.toString());
                            email.setText(email_);
                            cellphone.setText(cellphone_);
                            swipeRefresh.setRefreshing(false);
                        }
                    });
                }
            }
        }).start();
    }
}