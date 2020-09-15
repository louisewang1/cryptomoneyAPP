package com.example.cryptomoney;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import java.net.URLEncoder;
import java.sql.Connection;


public class LoginActivity extends AppCompatActivity {

    // 定义控件和全局变量初始化
    private EditText accountEdit;  // input username
    private EditText passwordEdit;  // input pwd
    private Button login;  // click login button
    private Connection conn = null; // connection to mysql
    private Button register;
    private TextView showResponse;

    public final static int TYPE_CONN_FAILED = -1;  // identify different error
    public final static int TYPE_LOGIN_FAILED = 0;
    public final static int REG_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 绑定控件
        accountEdit = (EditText) findViewById(R.id.account);
        passwordEdit = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);

        showResponse = (TextView) findViewById(R.id.tv_show_response);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_to_register = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivityForResult(intent_to_register,REG_CODE);
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 获取输入字符串
                final String username = accountEdit.getText().toString();
                final String pwd = passwordEdit.getText().toString();
                final String loginRequest = "request="+ URLEncoder.encode("login")+ "&username="+ URLEncoder.encode(username)+"&password=" +URLEncoder.encode(pwd);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        final String  response = PostService.loginByPost(username,pwd);
                        String response = PostService.Post(loginRequest);
//                        if(response != null){
////                            showResponse(response);
////                        }else{
////                            showResponse("Request Failed");
////                        }
                        if (response != null) {
                            int id = Integer.parseInt(response);
                            Log.d("LoginActivity","id="+id);
                            if (id > 0) {
                                showResponse("id = " +response);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("account_id",id);  //传递account_id
                                startActivity(intent);
                                finish();
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showResponse("Username or password is incorrect");
                                    }
                                });
                            }
                        } else {
                            response = "failed";
                            showResponse(response);
                        }
                    }
                }).start();
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
        }
    }

}