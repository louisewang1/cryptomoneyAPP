package com.example.cryptomoney;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import android.annotation.SuppressLint;
//import android.support.v7.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.util.Constant;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cn.memobird.gtx.GTX;

public class MainActivity extends AppCompatActivity {

    private Button qrscan;
    private Button print;
    private EditText scanreturn;
    private Button account;
    private Button transfer;

    private Button connsql;

    //_________________________
    private Button button,button_delete,button_insert,button_update;
//    private TextView textView;
//    private static final int TEST_USER_SELECT = 1;
//    int i =0,d=0,z=0;
//    private EditText editText,editText_update;
//    @SuppressLint("HandlerLeak")
//    private Handler handler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            String user;
//            switch (msg.what){
//                case TEST_USER_SELECT:
//                    Test test = (Test) msg.obj;
//                    user = test.getUser();
//                    int id = test.getId();
//                    System.out.println("***********");
//                    System.out.println("***********");
//                    System.out.println("id:"+id);
//                    System.out.println("user:"+user);
//
//                    textView.setText(user);
//                    break;
//            }
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        qrscan = (Button) findViewById(R.id.qrscan);
        print = (Button) findViewById(R.id.print);
        account = (Button)  findViewById(R.id.account_info);
        transfer = (Button) findViewById(R.id.transfer);
        final Intent intent_from_login = getIntent();
        qrscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 二维码扫码
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[] {Manifest.permission.CAMERA},Constant.REQ_PERM_CAMERA);
                }else {
                    openCamera();
                }
            }
        });

        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GTX.init(MainActivity.this,"");
            }
        });

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = intent_from_login.getStringExtra("username");
                Double balance = intent_from_login.getDoubleExtra("balance",0);
                Intent intent_to_account = new Intent(MainActivity.this,AccountActivity.class);
                intent_to_account.putExtra("username",username);
                intent_to_account.putExtra("balance",balance);
                startActivity(intent_to_account);
            }
        });

        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,TransferActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
            Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
            startActivityForResult(intent, Constant.REQ_QR_CODE);
//            startActivzity(intent);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}