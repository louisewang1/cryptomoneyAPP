package com.example.cryptomoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.cryptomoney.utils.Base64Utils;
import com.example.cryptomoney.utils.Constant;
import com.example.cryptomoney.utils.DBHelper;
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
    private RadioButton customer, merchant;
    private RadioGroup group;
    private int registerMode;
    private String response;

//    private SharedPreferences pref;
//    private SharedPreferences.Editor editor;

    private Connection conn = null;
//    public final static int TYPE_CONN_FAILED = -1;
//    public final static int TYPE_REG_SUCCESS = 1;
//    public final static int TYPE_REG_FAILED = 0;
    public final static int CUSTOMER_MODE = 1;
    public final static int MERCHANT_MODE = 2;

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
        customer = (RadioButton) findViewById(R.id.customer);
        merchant = (RadioButton) findViewById(R.id.merchant);
        group = (RadioGroup) findViewById(R.id.group);

        if (customer.isChecked())  registerMode = CUSTOMER_MODE;
        else if (merchant.isChecked()) registerMode = MERCHANT_MODE;

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()  {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.customer:
                        registerMode = CUSTOMER_MODE;
                        break;
                    case R.id.merchant:
                        registerMode = MERCHANT_MODE;
                        break;
                    default:
                        break;
                }
            }
        });


        register.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                // 获取输入字符串
                final String username = accountEdit.getText().toString();
                final String pwd = pwdEdit.getText().toString();
                final String pwd_again = pwd_againEdit.getText().toString();
                final String email = emailEdit.getText().toString();
                final String cellphone = cellphoneEdit.getText().toString();
                System.out.println(registerMode);
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(username)) {
                    Toast.makeText(RegisterActivity.this, "Username or password can't be empty",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!pwd.equals(pwd_again)) {
                    Toast.makeText(RegisterActivity.this, "Two passwords are inconsistent",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (registerMode == CUSTOMER_MODE) {
//                    System.out.println("into customer mode");
                    final String registerRequest = "request="+ URLEncoder.encode("customerregister")+ "&username="+ URLEncoder.encode(username)
                            +"&password=" +URLEncoder.encode(pwd) + "&email=" +URLEncoder.encode(email) +"&cellphone=" +URLEncoder.encode(cellphone);

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
                                            Toast.makeText(RegisterActivity.this, "Customer account register succeeded", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("username", username);
                                    resultIntent.putExtra("pwd", pwd);
                                    resultIntent.putExtra("type","CUSTOMER");
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

                else if (registerMode == MERCHANT_MODE) {
                    final String registerRequest = "request="+ URLEncoder.encode("merchantregister")+ "&username="+ URLEncoder.encode(username)
                            +"&password=" +URLEncoder.encode(pwd) + "&email=" +URLEncoder.encode(email) +"&cellphone=" +URLEncoder.encode(cellphone);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            response = PostService.Post(registerRequest);  // result=pk
                            System.out.println(response);
                            if (response != null && response.indexOf("pk=") == 0) {
                                String pk_exp = response.split("pk=")[1].split("&N=")[0];
                                String modulus = response.split("&N=")[1];
//                                pref = getSharedPreferences("cryptomoneyAPP", Context.MODE_PRIVATE);
//                                editor = pref.edit();
//                                editor.putString("merchant_pk",pk_exp);
//                                editor.putString("merchant_N",modulus);
//                                editor.apply();
                                // store merchant pk in local db
                                DBHelper dbHelper = new DBHelper(RegisterActivity.this, "test.db", null, 3);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                ContentValues values = new ContentValues();
                                values.put("mer_name", username);
                                values.put("N", modulus);
                                values.put("pk_exp", pk_exp);
                                db.insertWithOnConflict("PkList", null, values,SQLiteDatabase.CONFLICT_REPLACE);
                                values.clear();

                                runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Common.showShortToast(RegisterActivity.this, "Merchant account register succeeded");
                                        }
                                });
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("username", username);
                                resultIntent.putExtra("pwd", pwd);
                                resultIntent.putExtra("type", "MERCHANT");
                                RegisterActivity.this.setResult(RESULT_OK, resultIntent);
                                finish();
                            } else {
                                runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Common.showShortToast(RegisterActivity.this, response);
                                        }
                                    });
                            }
                        }
                    }).start();


                }



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