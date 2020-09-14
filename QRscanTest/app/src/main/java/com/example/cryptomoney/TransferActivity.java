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
import android.widget.Toast;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import static java.sql.Types.INTEGER;

public class TransferActivity extends AppCompatActivity {

    // 定义控件和全局变量初始化
    private ImageButton back;
    private Button transfer;
    private int account_id;
    private Connection conn = null;

    public final static int TYPE_CONN_FAILED = -1;
    public final static int TYPE_TR_SUCCESS = 1;
    public final static int TYPE_AMOUNT_FAILED = 0;
    public final static int TYPE_ID_FAILED = -2;
    public final static int TYPE_SELF_FAILED = -3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        Intent intent_from_main = getIntent(); // 获取account_id
        account_id = intent_from_main.getIntExtra("account_id",0);

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

        transfer = (Button) findViewById(R.id.transfer);
        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditText to_account = (EditText) findViewById(R.id.to_account);
                EditText amount = (EditText) findViewById(R.id.amount);

                final int to_account_id = Integer.parseInt(to_account.getText().toString());  // 定义ExeTransaction传参
                final double value = Double.parseDouble(amount.getText().toString());
                final int from_account_id = account_id;
//                Log.d("TransferActivity","to account:" + to_account_id + "amount: " + value);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        conn = (Connection) DBOpenHelper.getConn();
                        int tr_result = ExeTransaction(conn,from_account_id,to_account_id,value);
                        switch (tr_result) {
                            case TYPE_TR_SUCCESS:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(TransferActivity.this,"transfer succeeded",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            case TYPE_ID_FAILED:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(TransferActivity.this,"Account Id doesn't exist",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            case TYPE_SELF_FAILED:
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(TransferActivity.this,"Can't transfer to yourself",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            case TYPE_AMOUNT_FAILED:
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(TransferActivity.this,"Not enough balance",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    break;
                            default:
                                break;
                        }
                        DBOpenHelper.closeConnection(conn);
                    }
                }).start();
            }
        });
    }

    private int ExeTransaction(Connection conn, Integer from_account_id, Integer to_account_id, Double value) {
        if (conn == null) return TYPE_CONN_FAILED;
        CallableStatement cs = null;
        try {
            cs =conn.prepareCall("{call exe_transaction(?,?,?,?)}");
            cs.setInt(1, from_account_id);
            cs.setInt(2, to_account_id);
            cs.setDouble(3, value);
            cs.registerOutParameter(4, INTEGER);
            cs.execute();
//            Log.d("MainActivity","transaction result: " + cs.getInt(4));
            if (cs.getInt(4) == 1) return TYPE_TR_SUCCESS;
            else if (cs.getInt(4) == 0) return TYPE_AMOUNT_FAILED;
            else if (cs.getInt(4) == -1 ) return TYPE_ID_FAILED;
            else if (cs.getInt(4) == -2) return TYPE_SELF_FAILED;
        } catch (Exception e){
            e.printStackTrace();
        }if (cs != null) {
            try {
                cs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return TYPE_CONN_FAILED;
    }
}