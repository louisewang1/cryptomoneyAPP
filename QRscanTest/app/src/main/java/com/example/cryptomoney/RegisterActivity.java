package com.example.cryptomoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.cryptomoney.utils.Base64Utils;
import com.example.cryptomoney.utils.RSAUtils;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

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
    private KeyPair keypair;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

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

                keypair = RSAUtils.generateRSAKeyPair(512);
                RSAPublicKey publicKey = (RSAPublicKey) keypair.getPublic();
                RSAPrivateKey privateKey = (RSAPrivateKey) keypair.getPrivate();

                // 注册成功后生成密钥对保存在sharedpreference中 //todo:不安全
                String pk_enc = Base64Utils.encode(publicKey.getEncoded());
//                try {
//                    PublicKey pk2 = RSAUtils.loadPublicKey("MDwwDQYJKoZIhvcNAQEBBQADKwAwKAIhAL5J0bDBTANeWs/ITbAUisxsU1TNhx6Aw10w/NK44mulAgMBAAE=");
//                    Log.d("RegisterActivity","pk2= "+pk2);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                String pk_exp = Base64Utils.encode(publicKey.getPublicExponent().toByteArray());
                String sk_exp = Base64Utils.encode(privateKey.getPrivateExponent().toByteArray());
                String modulus = Base64Utils.encode(publicKey.getModulus().toByteArray());

//                pref = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this);
                pref = getSharedPreferences("cryptomoneyAPP", Context.MODE_PRIVATE);
                editor = pref.edit();
                editor.putString("pk",pk_enc);
                editor.putString("pk_exp",pk_exp);
                editor.putString("sk_exp",sk_exp);
                editor.putString("modulus",modulus);
                editor.apply();
                Log.d("RegisterActivity","sharedpreference OK");

                Log.d("RegisternActivity","pk= "+Base64Utils.encode(publicKey.getEncoded()));
                Log.d("RegisterActivity","sk_exp= "+privateKey.getPrivateExponent().toString());
                Log.d("RegisterActivity","pk_exp= "+pk_exp);
                Log.d("RegisterActivity","N from pref= "+pref.getString("modulus",""));
                Log.d("RegisterActivity","sk= "+Base64Utils.encode(privateKey.getEncoded()));

                final String registerRequest = "request="+ URLEncoder.encode("register")+ "&username="+ URLEncoder.encode(username)
                        +"&password=" +URLEncoder.encode(pwd) + "&email=" +URLEncoder.encode(email) +"&cellphone=" +URLEncoder.encode(cellphone)
                        +"&pk=" +URLEncoder.encode(pk_exp)+"&N=" +URLEncoder.encode(modulus);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String response = PostService.Post(registerRequest);
                        if (response != null) {
//                            Log.d("RegiterActivity","response:"+response);
                            int result = Integer.parseInt(response);
                            if (result == 1) {
//                                Log.d("RegiterActivity","response= "+response);
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