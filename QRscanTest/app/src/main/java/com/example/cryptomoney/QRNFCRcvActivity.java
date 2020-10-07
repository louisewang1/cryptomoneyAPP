package com.example.cryptomoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cryptomoney.utils.Base64Utils;
import com.example.cryptomoney.utils.NfcUtils;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.util.Constant;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.RSAPrivateKeySpec;

import javax.crypto.Cipher;

import static java.lang.Math.min;

public class QRNFCRcvActivity extends AppCompatActivity {

    private Integer account_id;
    private TextView qrstatus;
    private TextView nfcstatus;
    private NfcUtils nfcUtils;
    private String nfcstring;
    private String qrstring;
    private Button scan;
    private Button execute;
    private String fullstring;
    private boolean qrsuccess = false;
    private boolean nfcsuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrnfc_rcv);

        qrstatus = findViewById(R.id.qrstatus);
        nfcstatus = findViewById(R.id.nfcstatus);
        nfcUtils = new NfcUtils(this);
        scan = findViewById(R.id.scan);
        execute = findViewById(R.id.execute);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)  {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        Intent intent_from_rcvmode = getIntent();
        account_id = intent_from_rcvmode.getIntExtra("account",0);

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(QRNFCRcvActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(QRNFCRcvActivity.this,
                            new String[] {Manifest.permission.CAMERA}, Constant.REQ_PERM_CAMERA);
                }else {
                    openCamera();
                }
            }
        });

        execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (qrsuccess == true && nfcsuccess == true) {
                    fullstring = getfullstring(qrstring,nfcstring);
                    System.out.println(fullstring);
                    String N_base64 = fullstring.split("N=")[1].split("&d=")[0];
                    BigInteger N = new BigInteger(Base64Utils.decode(N_base64));
//                Log.d("MainActivity","N= "+N);
                    String d_base64 = fullstring.split("&d=")[1].split("&addr=")[0];
                    BigInteger d = new BigInteger(Base64Utils.decode(d_base64));
//                Log.d("MainActivity","d= "+d);
                    String addr = fullstring.split("&addr=")[1];
//                Log.d("MainActivity","addr= "+addr);

                    try {
//                    sk = RSAUtils.getPrivateKey(N.toString(),d.toString());
                        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                        RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(N,d);
                        PrivateKey sk = keyFactory.generatePrivate(rsaPrivateKeySpec);
//                    Log.d("MainActivity","recovered sk= "+sk);
                        Cipher cipher=Cipher.getInstance("RSA/ECB/PKCS1Padding");
                        cipher.init(Cipher.ENCRYPT_MODE,sk);
//                    Log.d("MainActivity","recovered sk= "+sk_enc);
                        String id = "ACCOUNT="+account_id.toString();
                        byte[]  id_enc = cipher.doFinal(id.getBytes());

                        String id_enc_str = Base64Utils.encode(id_enc);
//                    Log.d("MainActivity","id_enc= "+id_enc_str);
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
                                            Common.showLongToast(QRNFCRcvActivity.this,response);
                                            finish();
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
                    Common.showLongToast(QRNFCRcvActivity.this,"nfc reading or QR scanning failed");
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Logger.e(TAG, "onResume");
        nfcUtils.enableForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Logger.e(TAG, "onPause");
        nfcUtils.disableForegroundDispatch();
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

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        nfcstring = nfcUtils.readMessage(intent);
        Common.showShortToast(this,"Read NFC successfully.");
        System.out.println(nfcstring);
        nfcstatus.setText("Status: reading successfully");
        nfcsuccess = true;
    }

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
            qrstring = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
            System.out.println(qrstring);
            if (qrstring.indexOf("N") != 0 || qrstring.indexOf("=") == 1) {
                Common.showLongToast(QRNFCRcvActivity.this,"QR reading failed, please scan again ");
                qrstatus.setText("Status: failed, please try again");
            }
            else {
                qrstatus.setText("Status: scanning successfully");
                qrsuccess = true;
            }

        }
    }

    private void openCamera() {
        try {
            Intent intent = new Intent(QRNFCRcvActivity.this, CaptureActivity.class);
            startActivityForResult(intent, Constant.REQ_QR_CODE);
        } catch (SecurityException e) {
            e.printStackTrace();
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
}