package com.example.cryptomoney;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.memobird.gtx.GTX;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
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
    private Integer account_id;

    final String AK = "c68d494c429349baa165fb3725b804d8";  //c6a5a445dc25490183f42088f4b78ccf
    private static String timePattern = "yyyy-MM-dd HH:mm:ss";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        closeAndroidPDialog();
        init();

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
                startActivity(new Intent(MainActivity.this, BluetoothActivity.class));
            }
        });

        account.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                final String accountInfoRequest ="request=" + URLEncoder.encode("accountinfo") + "&id="+ URLEncoder.encode(account_id.toString());

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        final String response = PostService.Post(accountInfoRequest);
                        if (response != null) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                int id = jsonObject.getInt("id");
                                String username = jsonObject.getString("username");
                                Log.d("MainActivity",username);
                                Double balance = jsonObject.getDouble("balance");
                                String email = jsonObject.getString("email");
                                String cellphone = jsonObject.getString("cellphone");

                                Intent intent_to_account = new Intent(MainActivity.this, AccountActivity.class); // 启动AccountActivity传入用户信息
                                intent_to_account.putExtra("id",id);
                                intent_to_account.putExtra("username",username);
                                intent_to_account.putExtra("balance", balance);
                                intent_to_account.putExtra("email",email);
                                intent_to_account.putExtra("cellphone",cellphone);
                                startActivity(intent_to_account);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
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

                final String transactionRequest ="request=" + URLEncoder.encode("transaction") + "&id="+ URLEncoder.encode(account_id.toString());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String response = PostService.Post(transactionRequest);
                        if (response != null) {

                            try {
                                List<Record> recordList = new ArrayList<>();
                                SimpleDateFormat sdf = new SimpleDateFormat(timePattern);
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

                                Intent intent = new Intent(MainActivity.this,TransactionActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("recordList", (Serializable) recordList);
                                intent.putExtras(bundle);
                                startActivity(intent);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
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


    private void init() {
        GTX.init(getApplicationContext(), AK);
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GTX.exitApp();
    }


    /**
     * android9.0 谷歌限制开发者调用非官方公开API 方法或接口(使用@hide注解的系统源码或反射)
     * 解决debug模式下在国内版Android P上的提醒弹窗 (Detected problems with API compatibility)
     */
    private void closeAndroidPDialog() {
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}