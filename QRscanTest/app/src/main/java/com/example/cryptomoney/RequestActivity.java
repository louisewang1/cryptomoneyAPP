package com.example.cryptomoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.net.URLEncoder;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestActivity extends AppCompatActivity{

    private Button request;
    private Integer account_id;
    private Connection conn = null;

    private Double value;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_transfer);

        Intent intent_from_main = getIntent(); // 获取account_id
        account_id = intent_from_main.getIntExtra("account_id", 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        request = (Button) findViewById(R.id.request);
        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get the amount to be transfered
                EditText amount = (EditText) findViewById(R.id.amount);

                boolean isdouble = isNumeric(amount.getText().toString());
                //invalid input
                if (isdouble != true)
                    Common.showShortToast(RequestActivity.this, "Invalid input. Only number is allowed");

                    //input valid
                else {
                    value = Double.parseDouble(amount.getText().toString());

                    Intent intent = new Intent(RequestActivity.this,RcvModeActivity.class); // 启动TransferActivity,传入account_id
                    intent.putExtra("account_id",account_id);
                    intent.putExtra("value",value);
                    startActivity(intent);



                }

            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        final String sqloperation;
        final String sqloperation2;

        Double amount_received = data.getDoubleExtra("received", 0);
        Integer payer_id = data.getIntExtra("payer", 0);
        final Double reminder = amount_received - value;

        if (amount_received == value){
            sqloperation = "request=" + URLEncoder.encode("addmoney") + "&to_account=" + URLEncoder.encode(account_id.toString())
                + "&value=" + URLEncoder.encode(value.toString());
            run(sqloperation);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Common.showShortToast(RequestActivity.this, "request succeeded");
                    finish();
                }
            });
        }
        else if (amount_received > value){
            sqloperation = "request=" + URLEncoder.encode("addmoney") + "&to_account=" + URLEncoder.encode(account_id.toString())
                    + "&value=" + URLEncoder.encode(value.toString());
            run(sqloperation);
            sqloperation2 = "request=" + URLEncoder.encode("addmoney") + "&to_account=" + URLEncoder.encode(payer_id.toString())
                    + "&value=" + URLEncoder.encode(reminder.toString());
            run(sqloperation2);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Common.showShortToast(RequestActivity.this, "request succeeded");
                    finish();
                }
            });
        }
        else {
            sqloperation2 = "request=" + URLEncoder.encode("addmoney") + "&to_account=" + URLEncoder.encode(payer_id.toString())
                    + "&value=" + URLEncoder.encode(amount_received.toString());
            run(sqloperation2);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Common.showShortToast(RequestActivity.this, "Request failed. Money returned to payer");
                    finish();
                }
            });
        }


    }

    public void run(final String operation) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String response = PostService.Post(operation);
            }
        }).start();
    }

    //helper functions
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]+[.]{0,1}[0-9]*[dD]{0,1}");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

}
