package com.example.cryptomoney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import com.example.cryptomoney.utils.DBHelper;

import java.net.URLEncoder;
import java.sql.Connection;
import java.util.prefs.PreferenceChangeEvent;

import cn.memobird.gtx.GTX;

import static com.example.cryptomoney.RegisterActivity.CUSTOMER_MODE;
import static com.example.cryptomoney.RegisterActivity.MERCHANT_MODE;


public class LoginActivity extends AppCompatActivity {

    // 定义控件和全局变量初始化
    private EditText accountEdit;  // input username
    private EditText passwordEdit;  // input pwd
    private Button login;  // click login button
    private Connection conn = null; // connection to mysql
    private Button register;
    private TextView showResponse;
    private CheckBox rememberPass;
//    private RadioGroup group;
//    private RadioButton customer, merchant;
//    private int loginMode;

    public final static int TYPE_CONN_FAILED = -1;  // identify different error
    public final static int TYPE_LOGIN_FAILED = 0;
    public final static int REG_CODE = 2;
    final String AK = "c6a5a445dc25490183f42088f4b78ccf";

    // 本地存储记住密码
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        GTX.init(getApplicationContext(), AK);  //初始化

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 绑定控件
        accountEdit = (EditText) findViewById(R.id.account);
        passwordEdit = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);
//        group = (RadioGroup) findViewById(R.id.group);
//        customer = (RadioButton) findViewById(R.id.customer);
//        merchant = (RadioButton) findViewById(R.id.merchant);

        showResponse = (TextView) findViewById(R.id.tv_show_response);

        // 记住密码
//        pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref = getSharedPreferences("cryptomoneyAPP", Context.MODE_PRIVATE);
        rememberPass = (CheckBox) findViewById(R.id.remember_pass);
        boolean isRemember = pref.getBoolean("remember_password",false);
        if (isRemember) {
            // 自动填充
            String account = pref.getString("account","");
            String password = pref.getString("pwd","");
//            String type = pref.getString("type","");
//            if (type.equals("CUSTOMER")) customer.setChecked(true);
//            else if (type.equals("MERCHANT")) {
//                merchant.setChecked(true);
//            }
            accountEdit.setText(account);
            passwordEdit.setText(password);
            rememberPass.setChecked(true);
        }

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_to_register = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivityForResult(intent_to_register,REG_CODE);
            }
        });

//        if (customer.isChecked())  loginMode = CUSTOMER_MODE;
//        else if (merchant.isChecked()) loginMode = MERCHANT_MODE;

//        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()  {
//            @Override
//            public void onCheckedChanged(RadioGroup radioGroup, int i) {
//                switch (i) {
//                    case R.id.customer:
//                        loginMode = CUSTOMER_MODE;
//                        break;
//                    case R.id.merchant:
//                        loginMode = MERCHANT_MODE;
//                        break;
//                    default:
//                        break;
//                }
//            }
//        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 获取输入字符串
                        final String username = accountEdit.getText().toString();
                        final String pwd = passwordEdit.getText().toString();
                        final String loginRequest = "request="+ URLEncoder.encode("login")+ "&username="+ URLEncoder.encode(username)+"&password=" +URLEncoder.encode(pwd);

//                        final String  response = PostService.loginByPost(username,pwd);
                        String response = PostService.Post(loginRequest);
//                        if(response != null){
////                            showResponse(response);
////                        }else{
////                            showResponse("Request Failed");
////                        }
                        if (response != null) {
                            System.out.println("login response= "+ response);
                            if (response.equals("username or password not correct"))  {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showResponse("Username or password is incorrect");
                                    }
                                });
                            }
                            else {
                                int id = Integer.parseInt(response.split("id=")[1].split("&type=")[0]);
                                Log.d("LoginActivity","id="+id);
                                if (id > 0) {  // 登录成功
                                    // 记住密码
                                    editor = pref.edit();
                                    if (rememberPass.isChecked()) {
                                        editor.putBoolean("remember_password",true);
                                        editor.putString("account",username);
                                        editor.putString("pwd",pwd);
//                                    editor.putString("type","CUSTOMER");
                                    } else {
                                        editor.clear();
                                    }
                                    editor.apply();
//                                showResponse("id = " +response);
                                    if (response.indexOf("type=MERCHANT") != -1) {  //merchant account
                                        // store merchant pk
                                        DBHelper dbHelper = new DBHelper(LoginActivity.this, "test.db", null, 3);
                                        SQLiteDatabase db = dbHelper.getWritableDatabase();

                                        String[] str = {username};
                                        Cursor cursor = db.query("PkList",null,"mer_name=?",str,null,null,null);
                                        int cnt = 0;
                                        if (cursor.moveToFirst()) {
                                            do {
                                                cnt ++;
                                            }while(cursor.moveToNext());
                                        }
                                        cursor.close();
//                                        System.out.println("cnt= "+cnt);
                                        if (cnt == 0) {  // no duplicate
                                            ContentValues values = new ContentValues();
                                            values.put("mer_name", username);
                                            values.put("N", response.split("&N=")[1].split("&pk=")[0]);
                                            values.put("pk_exp", response.split("&pk=")[1]);
                                            db.insertWithOnConflict("PkList", null, values,SQLiteDatabase.CONFLICT_REPLACE);
                                            values.clear();
                                        }
                                    }
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("account_id",id);  //传递account_id
//                                intent.putExtra("type","CUSTOMER");
                                    startActivity(intent);
                                    finish();
                                 }
                            }
//                            else {
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        showResponse("Username or password is incorrect");
//                                    }
//                                });
//                            }
                        } else {
                            response = "failed";
                            showResponse(response);
                        }
                    }
                }).start();
//                }

//                else if (loginMode == MERCHANT_MODE) {
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            final String username = accountEdit.getText().toString();
//                            final String pwd = passwordEdit.getText().toString();
//                            final String loginRequest = "request="+ URLEncoder.encode("merchantlogin")+ "&username="+ URLEncoder.encode(username)+"&password=" +URLEncoder.encode(pwd);
//
////                        final String  response = PostService.loginByPost(username,pwd);
//                            String response = PostService.Post(loginRequest);
////                        if(response != null){
//////                            showResponse(response);
//////                        }else{
//////                            showResponse("Request Failed");
//////                        }
//                            if (response != null) {
//                                int id = Integer.parseInt(response);
//                                Log.d("LoginActivity","id="+id);
//                                if (id > 0) {  // 登录成功
//                                    // 记住密码
//                                    editor = pref.edit();
//                                    if (rememberPass.isChecked()) {
//                                        editor.putBoolean("remember_password",true);
//                                        editor.putString("account",username);
//                                        editor.putString("pwd",pwd);
//                                        editor.putString("type","MERCHANT");
//                                    } else {
//                                        editor.clear();
//                                    }
//                                    editor.apply();
//                                    showResponse("id = " +response);
//                                    Intent intent = new Intent(LoginActivity.this, MerchantMainActivity.class);
//                                    intent.putExtra("account_id",id);  //传递account_id
//                                    intent.putExtra("type","MERCHANT");
//                                    startActivity(intent);
//                                    finish();
//                                } else {
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            showResponse("Username or password is incorrect");
//                                        }
//                                    });
//                                }
//                            } else {
//                                response = "failed";
//                                showResponse(response);
//                            }
//                        }
//                    }).start();
//                }
            }
        });
    }

    private void showResponse(final  String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showResponse.setText(response);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REG_CODE && resultCode == RESULT_OK) {  // 注册结果回调
            accountEdit.setText(data.getStringExtra("username"));
            passwordEdit.setText(data.getStringExtra("pwd"));
//            System.out.println(data.getStringExtra("type"));
//            if (data.getStringExtra("type").equals("CUSTOMER")) {
//                customer.setChecked(true);
//                loginMode = CUSTOMER_MODE;
//            }
//            else if (data.getStringExtra("type").equals("MERCHANT")) {
//                merchant.setChecked(true);
//                loginMode = MERCHANT_MODE;
//            }
        }
    }

}