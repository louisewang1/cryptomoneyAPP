package com.example.cryptomoney;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;


import com.google.zxing.util.Constant;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.sql.SQLException;

import javax.xml.transform.Result;

import static java.sql.Types.INTEGER;


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

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String  response = LoginService.loginByPost(username,pwd);
                        if(response != null){
                            showResponse(response);
                        }else{
                            showResponse("Request Failed");
                        }
                        /*
                        conn = (Connection) DBOpenHelper.getConn(); // 建立与Mysql连接
                        int account_id = LoginCheck(conn,username,pwd);  // 匹配用户名密码，返回用户id
                        if (account_id != TYPE_LOGIN_FAILED && account_id != TYPE_CONN_FAILED) { // 匹配成功，匹配MainActivity，传入account_id
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("account_id",account_id);
                            startActivity(intent);
                            finish();
                        } else if (account_id == TYPE_LOGIN_FAILED){  // account_id=0,匹配失败
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "account or password is invalid", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else if (account_id == TYPE_CONN_FAILED) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "connection failure", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } */
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

/*
        public static int LoginCheck(Connection conn, String username, String pwd) {
//        Log.d("LoginActivity","conn: " + conn);
        if (conn == null) return TYPE_CONN_FAILED;
        CallableStatement cs = null;
        try {
            cs =conn.prepareCall("{call login_check(?,?,?)}"); // 调用login_check(in username, in pwd, out account_id)
            cs.setString(1,username);
            cs.setString(2,pwd);
            cs.registerOutParameter(3, INTEGER);
            cs.execute();
//            Log.d("LoginActivity","id: " + cs.getInt(3));
            if (cs.getInt(3) == 0) return TYPE_LOGIN_FAILED;
            else return cs.getInt(3);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REG_CODE && resultCode == RESULT_OK) {  // 注册结果回调
            accountEdit.setText(data.getStringExtra("username"));
            passwordEdit.setText(data.getStringExtra("pwd"));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //TODO:不同活动共享同一连接，如何传递？
        DBOpenHelper.closeConnection(conn);  // 关闭连接
    } */
}