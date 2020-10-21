package com.example.cryptomoney;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
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
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.cryptomoney.utils.Base64Utils;
import com.example.cryptomoney.utils.RSAUtils;
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

import javax.crypto.Cipher;

import cn.memobird.gtx.GTX;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.INTEGER;
import static java.sql.Types.VARCHAR;

public class MainActivity extends AppCompatActivity {

    private Button qrscan;
//    private Button NFC_read;
//    private EditText scanreturn;
    private Button account;
    private Button transfer;
    private Button transaction;
    private Button qrgenerate;
    private Button crypto_tr;
    private Button crypto;
//    private Button execute;
    private PrivateKey sk;
    private PublicKey pk;
    private String sk_enc;
    private SharedPreferences pref;
    private String type;

    private Integer account_id;
    private static String timePattern = "yyyy-MM-dd HH:mm:ss";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)  {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        qrscan = (Button) findViewById(R.id.qrscan);
//        NFC_read = (Button) findViewById(R.id.nfctag);
        account = (Button)  findViewById(R.id.account_info);
        transfer = (Button) findViewById(R.id.transfer);
        transaction = (Button) findViewById(R.id.tr_detail);
//        qrgenerate = (Button) findViewById(R.id.qrgenerate);
        crypto = (Button) findViewById(R.id.crypto);
//        crypto_tr = (Button) findViewById(R.id.cryptotr_detail);
//        execute = (Button) findViewById(R.id.execute);
//        scanreturn = (EditText) findViewById(R.id.scan_result);

        Intent intent_from_login = getIntent();
        account_id = intent_from_login.getIntExtra("account_id",0);
//        type = intent_from_login.getStringExtra("type");

        crypto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String cryptotransactionRequest ="request=" + URLEncoder.encode("cryptotransaction") + "&id="+ URLEncoder.encode(account_id.toString());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final String response = PostService.Post(cryptotransactionRequest);
                        if (response != null) {

                            try {
                                List<CryptoRecord> recordList = new ArrayList<>();
//                                SimpleDateFormat sdf = new SimpleDateFormat(timePattern);
                                JSONArray jsonArray = new JSONArray(response);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    CryptoRecord record = new CryptoRecord();
                                    record.setIndex(jsonObject.getInt("index"));
                                    record.setAddr(jsonObject.getString("address"));
                                    record.setTime(jsonObject.getString( "time"));
                                    record.setValue(jsonObject.getDouble("value"));
                                    recordList.add(record);
                                }

                                Intent intent = new Intent(MainActivity.this,CryptoActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("recordList", (Serializable) recordList);
                                bundle.putInt("account_id",account_id);
//                                bundle.putString("type",type);
                                intent.putExtras(bundle);
                                startActivity(intent);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
//                Intent intent = new Intent(MainActivity.this,CryptoActivity.class); // 启动TransferActivity,传入account_id
//                intent.putExtra("account_id",account_id);
//                startActivity(intent);
            }
        });

//        crypto_tr.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                final String cryptotransactionRequest ="request=" + URLEncoder.encode("cryptotransaction") + "&id="+ URLEncoder.encode(account_id.toString());
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        final String response = PostService.Post(cryptotransactionRequest);
//                        if (response != null) {
//
//                            try {
//                                List<CryptoRecord> recordList = new ArrayList<>();
////                                SimpleDateFormat sdf = new SimpleDateFormat(timePattern);
//                                JSONArray jsonArray = new JSONArray(response);
//                                for (int i = 0; i < jsonArray.length(); i++) {
//                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//                                    CryptoRecord record = new CryptoRecord();
//                                    record.setIndex(jsonObject.getInt("index"));
//                                    record.setAddr(jsonObject.getString("address"));
//                                    record.setTime(jsonObject.getString( "time"));
//                                    record.setValue(jsonObject.getDouble("value"));
//                                    recordList.add(record);
//                                }
//
//                                Intent intent = new Intent(MainActivity.this,CryptoTransactionActivity.class);
//                                Bundle bundle = new Bundle();
//                                bundle.putSerializable("recordList", (Serializable) recordList);
//                                bundle.putInt("account_id",account_id);
//                                bundle.putString("type",type);
//                                intent.putExtras(bundle);
//                                startActivity(intent);
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }).start();
//            }
//        });

//        qrgenerate.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(MainActivity.this, QRgeneratorActivity.class));
//            }
//        });

        qrscan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent_to_mode = new Intent(MainActivity.this,RcvModeActivity.class);
                intent_to_mode.putExtra("account_id",account_id);
                intent_to_mode.putExtra("type",type);
                startActivity(intent_to_mode);

            }
        });

//        NFC_read.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//
//                startActivity(new Intent(MainActivity.this, NFCRWActivity.class));
//            }
//        });

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String accountInfoRequest ="request=" + URLEncoder.encode("accountinfo") +
                        "&id="+ URLEncoder.encode(account_id.toString());

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
                                bundle.putInt("account_id",account_id);
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
        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String qrstring = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);

            if (qrstring.indexOf("N") == 0) {
                String N_base64 = qrstring.split("N=")[1].split("&d=")[0];
                BigInteger N = new BigInteger(Base64Utils.decode(N_base64));
                Log.d("MainActivity","N= "+N);
                String d_base64 = qrstring.split("&d=")[1].split("&addr=")[0];
                BigInteger d = new BigInteger(Base64Utils.decode(d_base64));
                Log.d("MainActivity","d= "+d);
                String addr = qrstring.split("&addr=")[1];
                Log.d("MainActivity","addr= "+addr);

                try {
//                    sk = RSAUtils.getPrivateKey(N.toString(),d.toString());
                    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                    RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(N,d);
                    PrivateKey sk = keyFactory.generatePrivate(rsaPrivateKeySpec);
                    Log.d("MainActivity","recovered sk= "+sk);
                    Cipher cipher=Cipher.getInstance("RSA/ECB/PKCS1Padding");
                    cipher.init(Cipher.ENCRYPT_MODE,sk);
//                    Log.d("MainActivity","recovered sk= "+sk_enc);
                    String id = "ACCOUNT="+account_id.toString();
                    byte[]  id_enc = cipher.doFinal(id.getBytes());

                    String id_enc_str = Base64Utils.encode(id_enc);
                    Log.d("MainActivity","id_enc= "+id_enc_str);
//                    Log.d("MainActivity","encrypted id= "+id_enc);

                    final String cryptomoneyinRequest ="request=" + URLEncoder.encode("getencrypto") +
                            "&id_enc="+ URLEncoder.encode(id_enc_str)+"&addr="+ URLEncoder.encode(addr);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final String response = PostService.Post(cryptomoneyinRequest);
                            if (response != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Common.showLongToast(MainActivity.this,response);
                                    }
                                });
                            }
                        }
                    }).start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            else {
                Common.showLongToast(MainActivity.this,"QR reading failed, please scan again ");
            }
        }
    }

    private void openCamera() {
        try {
            Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
            startActivityForResult(intent, Constant.REQ_QR_CODE);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Exit confirmation");
        dialog.setMessage("Are you sure to exit?");
        dialog.setCancelable(true);
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                GTX.exitApp();
                finish();
            }
        });
        dialog.setNegativeButton("No", null);
        dialog.show();
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