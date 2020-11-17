package com.example.cryptomoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.example.cryptomoney.utils.Base64Utils;
import com.example.cryptomoney.utils.DBHelper;
import com.example.cryptomoney.utils.NfcUtils;
import com.example.cryptomoney.utils.WriteDialog;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.util.Constant;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Connection;
import java.util.Arrays;
import java.util.prefs.PreferenceChangeEvent;

import javax.crypto.Cipher;

import cn.memobird.gtx.GTX;

import static com.example.cryptomoney.RegisterActivity.CUSTOMER_MODE;
import static com.example.cryptomoney.RegisterActivity.MERCHANT_MODE;
import static java.lang.Math.min;


public class LoginActivity extends AppCompatActivity {

    private static final int RESULT_PASSSED = 3 ;
    // 定义控件和全局变量初始化
    private EditText accountEdit;  // input username
    private EditText passwordEdit;  // input pwd
    private Button login;  // click login button
    private Connection conn = null; // connection to mysql
    private Button register;
    private TextView showResponse;
    private CheckBox rememberPass;
    private Spinner spinner = null;
    private ArrayAdapter<String> spinneradapter = null;
    private String[] merchantList;
    private String merchant_selected = "None";
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Button verify;
    private String fullstring;
    private String qrstring;
    private String nfcstring;
    private NfcUtils nfcUtils;
    private WriteDialog NFCDialog;
    private Boolean scanfinish = false;
    private Boolean showDialog = false;
    private String merchant_modulus;
    private String merchant_pk_exp;
    public final static int MAX_DECRYPT_BLOCK = 64;
    public TextView verify_resp;
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
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)  {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        // 绑定控件
        accountEdit = (EditText) findViewById(R.id.account);
        passwordEdit = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        register = (Button) findViewById(R.id.register);
        verify = (Button) findViewById(R.id.verify);
        verify_resp = (TextView) findViewById(R.id.vrf_response);
//        group = (RadioGroup) findViewById(R.id.group);
//        customer = (RadioButton) findViewById(R.id.customer);
//        merchant = (RadioButton) findViewById(R.id.merchant);

        nfcUtils = new NfcUtils(this);

        showResponse = (TextView) findViewById(R.id.tv_show_response);
        spinner = (Spinner)findViewById(R.id.spinner);

        dbHelper = new DBHelper(LoginActivity.this, "test.db", null, 3);
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query("PkList",null,null,null,null,null,null);
        int cnt = 0;
        if (cursor.moveToFirst()) {
            do {
               cnt ++; // count size
            }while(cursor.moveToNext());
        }
        cursor.close();
        merchantList = new String[cnt];
        cursor = db.query("PkList",null,null,null,null,null,null);
        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                merchantList[i] = cursor.getString(cursor.getColumnIndex("mer_name"));
                i ++; // count size
            }while(cursor.moveToNext());
        }
        spinneradapter = new ArrayAdapter<String>(LoginActivity.this,android.R.layout.simple_spinner_item,merchantList);
        spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinneradapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                merchant_selected = (String) ((TextView)view).getText();
                System.out.println("selected merchant= "+merchant_selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("merchant_selected=" + merchant_selected);
                if (!merchant_selected.equals("None")) {
                    if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(LoginActivity.this,
                                new String[] {Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
                    }else {
                        openCamera();
                    }
                }

            }
        });

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
                            response = "connection failed";
                            showResponse(response);
                        }
                    }
                }).start();
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

    private void openCamera() {
        try {
            Intent intent = new Intent(LoginActivity.this, CaptureActivity.class);
            intent.putExtra("from","login");
            startActivityForResult(intent, Constant.REQ_QR_CODE);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
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

        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            qrstring = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
            System.out.println("qrstring= "+qrstring);

            // only QR
            if (qrstring.indexOf("token=") == 0 && qrstring.indexOf("&d=") != -1 && qrstring.indexOf("&N=") != -1) {
                fullstring = qrstring;
                scanfinish = true;
                verifytoken();
            }

            // if qr string not begin with "N=" but begin with "N", then NFC is needed, otherwise wrong string
            else if (qrstring.indexOf("token") == -1 && qrstring.indexOf("tkn") == 0 ) {
                showDialog = true;
                showSaveDialog();
            }
//
            else {
                Common.showShortToast(LoginActivity.this, "Invalid message,scan again");
                openCamera();
            }
        }

        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_PASSSED) {
            qrstring = "";
            showDialog = true;
            showSaveDialog();
        }
    }

    private String getfullstring(String qrstring,String nfcstring) {
        String fullstring = "";
        Integer minlength = min(qrstring.length(),nfcstring.length());
        int i;
        for (i=0; i<minlength; i++) {
            fullstring += qrstring.charAt(i);
            fullstring += nfcstring.charAt(i);
        }
        if (qrstring.length() > nfcstring.length()) {
            fullstring += qrstring.charAt(i);
        }
        else if (qrstring.length() < nfcstring.length()) {
            fullstring += nfcstring.charAt(i);
        }
        return fullstring;
    }

    @Override
    protected void onNewIntent(Intent intent){
        if (showDialog) {
            super.onNewIntent(intent);
            nfcstring = nfcUtils.readMessage(intent);
            dissDialog();
//            Common.showShortToast(this, "NFC reading successfully.");
            fullstring = getfullstring(qrstring,nfcstring);
            scanfinish = true;
            showDialog = false;
            verifytoken();
        }
    }

    private void verifytoken(){
        System.out.println("fullstring in verifytoken= "+fullstring);
        // decode outside layer encryption enc1 with token sk
        if (fullstring.indexOf("token=") == 0 && fullstring.indexOf("&N=") != -1 && fullstring.indexOf("&d=") != -1 ) {
            String enc1 = fullstring.split("token=")[1].split("&N=")[0];
            //restore token sk
            String token_N_base64 =  fullstring.split("&N=")[1].split("&d=")[0];
            String token_d_base64 =  fullstring.split("&d=")[1];
            try {
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                BigInteger token_N = new BigInteger(Base64Utils.decode(token_N_base64));
                BigInteger token_d = new BigInteger(Base64Utils.decode(token_d_base64));
                RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(token_N, token_d);
                PrivateKey token_sk = keyFactory.generatePrivate(rsaPrivateKeySpec);
                // decrypt enc1 with token_sk
                // block decrypt
                cipher.init(Cipher.DECRYPT_MODE,token_sk);
                int inputLen = Base64Utils.decode(enc1).length;
                int offSet = 0;
                byte[] resultBytes = {};
                byte[] cache = {};
                while (inputLen - offSet > 0) {
                    if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                        cache = cipher.doFinal(Base64Utils.decode(enc1), offSet, MAX_DECRYPT_BLOCK);
                        offSet += MAX_DECRYPT_BLOCK;
                    } else {
                        cache = cipher.doFinal(Base64Utils.decode(enc1), offSet, inputLen - offSet);
                        offSet = inputLen;
                    }
                    resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
                    System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
                }
                String enc2 = new String(resultBytes);
                System.out.println("enc2= "+enc2);

                //get merchant N and pk_exp
                String[] str = {merchant_selected};
                Cursor cursor = db.query("PkList",null,"mer_name=?",str,null,null,null);
                if (cursor.moveToFirst()) {
                    do {
                        merchant_pk_exp = cursor.getString(cursor.getColumnIndex("pk_exp"));
                        merchant_modulus = cursor.getString(cursor.getColumnIndex("N"));
                    }while(cursor.moveToNext());
                }
                cursor.close();
                // restore merchant pk
                BigInteger merchant_N = new BigInteger(Base64Utils.decode(merchant_modulus));
                BigInteger merchant_d = new BigInteger(Base64Utils.decode(merchant_pk_exp));
                RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(merchant_N, merchant_d);
                PublicKey merchant_pk = keyFactory.generatePublic(rsaPublicKeySpec);
                // // decrypt enc2 with merchant_pk
                cipher.init(Cipher.DECRYPT_MODE,merchant_pk);
                String dec = new String(cipher.doFinal(Base64Utils.decode(enc2)));
                System.out.println("dec= "+dec);
                if (dec.indexOf("amount=") == 0 && dec.indexOf("&token") != -1) {
                    // check if the token is expired
                    double amount = Double.parseDouble(dec.split("amount=")[1].split("&token=")[0]);
                    String addr = dec.split("&token=")[1];
                    String[] str1 = {addr};
                    cursor = db.query("TokensAll", null, "addr=?", str1, null, null, null);
                    int cnt1 = 0;
                    if (cursor.moveToFirst()) {
                        do {
                            cnt1 ++;
                        }while(cursor.moveToNext());
                    }
                    cursor.close();
                    if (cnt1 == 0) { // not seen before
                        ContentValues values = new ContentValues();
                        values.put("addr", addr);
                        values.put("amount", amount);
//                        db.insert("Tokens", null, values);
                        db.insertWithOnConflict("TokensAll", null, values,SQLiteDatabase.CONFLICT_REPLACE);
                        values.put("N",token_N_base64);
                        values.put("sk_exp",token_d_base64);
                        db.insertWithOnConflict("Tokens", null, values,SQLiteDatabase.CONFLICT_REPLACE);
                        verify_resp.setText("amount= "+amount);
                    }
                    else {
                        verify_resp.setText("expired token");
                    }
                }
                else {
                    verify_resp.setText("invalid token");
                }
            } catch (Exception e) {
                e.printStackTrace();
                verify_resp.setText("invalid token");
            }
        }
        else verify_resp.setText("invalid token");

    }

    private void showSaveDialog() {
//        System.out.println("into showsavedialog");
        if (getSupportFragmentManager().findFragmentByTag("mWriteDialog") == null) {
            NFCDialog = new WriteDialog();
            System.out.println(NFCDialog);
            NFCDialog.show(getSupportFragmentManager(), "mWriteDialog");
//            System.out.println("show nfcdialog");
        }
    }

    @Override
    protected void onResume() {
        System.out.println("into resume");
        super.onResume();
        nfcUtils.enableForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcUtils.disableForegroundDispatch();
    }

    private void dissDialog() {
        if (NFCDialog != null &&
                NFCDialog.getDialog() != null &&
                NFCDialog.getDialog().isShowing()) {
            NFCDialog.dismiss();
        }
    }

}