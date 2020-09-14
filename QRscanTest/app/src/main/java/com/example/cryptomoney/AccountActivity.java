package com.example.cryptomoney;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class AccountActivity extends AppCompatActivity {

    // 定义控件和全局变量初始化
    private ImageButton back;
    private TextView username;
    private TextView balance;
    private TextView email;
    private TextView cellphone;
    private TextView ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // hide default toolbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // 添加返回上一层按钮
        back = (ImageButton) findViewById(com.google.zxing.R.id.btn_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 接收MainActivity传入数据
        Intent intent = getIntent();
        Integer account_id = intent.getIntExtra("id",0);
        String username_ = intent.getStringExtra("username");
        Double balance_ = intent.getDoubleExtra("balance",0);
        String email_ = intent.getStringExtra("email");
        String cellphone_ = intent.getStringExtra("cellphone");

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


    }
}