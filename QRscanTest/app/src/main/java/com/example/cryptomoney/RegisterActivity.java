package com.example.cryptomoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

import static java.sql.Types.INTEGER;

public class RegisterActivity extends AppCompatActivity {

    // 定义控件和全局变量初始化
    private ImageButton back;
    private EditText accountEdit;
    private EditText pwdEdit;
    private EditText pwd_againEdit;
    private EditText emailEdit;
    private EditText cellphoneEdit;
    private Button register;

    private Connection conn = null;
    public final static int TYPE_CONN_FAILED = -1;
    public final static int TYPE_REG_SUCCESS = 1;
    public final static int TYPE_REG_FAILED = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)  {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        // 绑定控件
        accountEdit = (EditText) findViewById(R.id.account);
        pwdEdit = (EditText) findViewById(R.id.password);
        pwd_againEdit = (EditText) findViewById(R.id.re_password);
        emailEdit = (EditText) findViewById(R.id.email);
        cellphoneEdit = (EditText) findViewById(R.id.cellphone);
        register = (Button) findViewById(R.id.register);


        register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // 获取输入字符串
                final String username = accountEdit.getText().toString();
                final String pwd = pwdEdit.getText().toString();
                final String pwd_again = pwd_againEdit.getText().toString();
                final String email = emailEdit.getText().toString();
                final String cellphone = cellphoneEdit.getText().toString();

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(username)) {
                    Toast.makeText(RegisterActivity.this, "Username or password can't be empty",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!pwd.equals(pwd_again)) {
                    Toast.makeText(RegisterActivity.this, "Two passwords are inconsistent",Toast.LENGTH_SHORT).show();
                    return;
                }

                final String registerRequest = "request="+ URLEncoder.encode("register")+ "&username="+ URLEncoder.encode(username)
                        +"&password=" +URLEncoder.encode(pwd) + "&email=" +URLEncoder.encode(email) +"&cellphone=" +URLEncoder.encode(cellphone);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String response = PostService.Post(registerRequest);
                        if (response != null) {
                            Log.d("RegiterActivity","response:"+response);
                            int result = Integer.parseInt(response);
                            if (result == 1) {
                                Log.d("RegiterActivity","response= "+response);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(RegisterActivity.this, "Register succeeded", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("username", username);
                                resultIntent.putExtra("pwd", pwd);
                                RegisterActivity.this.setResult(RESULT_OK, resultIntent);
                                finish();
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(RegisterActivity.this, "Username already existed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }
                }).start();

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


}