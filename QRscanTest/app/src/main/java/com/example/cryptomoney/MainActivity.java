package com.example.cryptomoney;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import android.annotation.SuppressLint;
//import android.support.v7.app.AppCompatActivity;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.Result;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.util.Constant;
import com.mysql.jdbc.PreparedStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.memobird.gtx.GTX;

import static java.sql.Types.DOUBLE;
import static java.sql.Types.INTEGER;
import static java.sql.Types.VARCHAR;

public class MainActivity extends AppCompatActivity {

    // 定义控件和全局变量初始化
    private Button qrscan;
    private Button print;
    private EditText scanreturn;
    private Button account;
    private Button transfer;
    private Button transaction;

    private Connection conn = null;
    private int account_id;

    private static String timePattern = "yyyy-MM-dd HH:mm:ss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 绑定控件
        qrscan = (Button) findViewById(R.id.qrscan);
        print = (Button) findViewById(R.id.print);
        account = (Button)  findViewById(R.id.account_info);
        transfer = (Button) findViewById(R.id.transfer);
        transaction = (Button) findViewById(R.id.tr_detail);

        // 获取从LoginActivity传入的account_id
        Intent intent_from_login = getIntent();
        account_id = intent_from_login.getIntExtra("account_id",0);

        qrscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 二维码扫码
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)  // 检查运行时权限
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[] {Manifest.permission.CAMERA},Constant.REQ_PERM_CAMERA);  // 申请照相权限
                }else {
                    openCamera();  // 已拥有权限，进行二维码扫描
                }
            }
        });

        print.setOnClickListener(new View.OnClickListener() {
            @Override
            //TODO: 需要获得AK后测试
            public void onClick(View view) {
                GTX.init(MainActivity.this,"");
            }
        });

        account.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        conn = (Connection) DBOpenHelper.getConn();
                        Object[] account_info = DisplayInfo(conn,account_id);
                        Intent intent_to_account = new Intent(MainActivity.this, AccountActivity.class); // 启动AccountActivity传入用户信息
                        intent_to_account.putExtra("id",(Integer) account_info[0]);
                        intent_to_account.putExtra("username",(String) account_info[1]);
                        intent_to_account.putExtra("balance", (Double) account_info[2]);
                        intent_to_account.putExtra("email",(String) account_info[3]);
                        intent_to_account.putExtra("cellphone",(String) account_info[4]);
                        startActivity(intent_to_account);
                        DBOpenHelper.closeConnection(conn);  //TODO: 避免每次都关闭连接再重新连接
                    }
                }).start();
            }
        });

        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,TransferActivity.class); // 启动TransferActivity,传入account_id
                intent.putExtra("account_id",account_id);
                startActivity(intent);
            }
        });

        transaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        conn = (Connection) DBOpenHelper.getConn();
                        List<Record> recordList = TranDetail(conn,account_id);
                        Intent intent = new Intent(MainActivity.this,TransactionActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("recordList", (Serializable) recordList);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        DBOpenHelper.closeConnection(conn);
                    }
                }).start();

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {  //权限请求结果回调
        if (requestCode == Constant.REQ_PERM_CAMERA && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
        else {
            Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //扫描结果回调
        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
            //将扫描出的信息显示出来
            scanreturn = (EditText) findViewById(R.id.scan_result);
            scanreturn.setText(scanResult);
        }
    }

    private void openCamera() {
        try {
            Intent intent = new Intent(MainActivity.this, CaptureActivity.class);  // 启动CaptureActivity(开源)
            startActivityForResult(intent, Constant.REQ_QR_CODE);  // 等待CaptureActivity回调扫描结果
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private Object[] DisplayInfo(Connection conn,Integer account_id) {
//        Log.d("LoginActivity","conn: " + conn);
        if (conn == null) return null;
        Object[] account_info = new Object[5];
        account_info[0] = account_id;
        CallableStatement cs = null;
        try {
            cs =conn.prepareCall("{call display_info(?,?,?,?,?)}"); // 调用display_info(in account_id,out username, out balance, out email, out cellphone)
            cs.setInt(1,account_id);
            cs.registerOutParameter(2, VARCHAR);
            cs.registerOutParameter(3, DOUBLE);
            cs.registerOutParameter(4, VARCHAR);
            cs.registerOutParameter(5, VARCHAR);
            cs.execute();
            if (cs.getString(2) == null) return null;  // 无匹配条目
            account_info[1] = cs.getString(2);
            account_info[2] = cs.getDouble(3);
            account_info[3] = cs.getString(4);
            account_info[4] = cs.getString(5);
        } catch (Exception e){
            e.printStackTrace();
        }if (cs != null) {
            try {
                cs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return account_info;
    }

    private List<Record> TranDetail(Connection conn, Integer account_id) {
        if (conn == null) return null;
        List<Record> recordList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat(timePattern);
        CallableStatement cs = null;
        ResultSet rs;
        try {
            cs =conn.prepareCall("{call tran_detail(?)}"); // 调用display_info(in account_id,out username, out balance, out email, out cellphone)
            cs.setInt(1,account_id);
            cs.execute();
            rs = cs.getResultSet();
            int index = 1;
            while (rs.next()) {
                Record record = new Record();
                record.setIndex(index);
                record.setFrom(rs.getInt("tr_from_account"));
                record.setTo(rs.getInt("tr_to_account"));
                record.setTime(sdf.format(rs.getTimestamp("tr_time")));
                record.setValue(rs.getDouble("tr_value"));
                recordList.add(record);
                index ++;
            }

        } catch (Exception e){
            e.printStackTrace();
        }if (cs != null) {
            try {
                cs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return recordList;
    }
}