package com.example.cryptomoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.nfc.FormatException;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.cryptomoney.utils.Base64Utils;
import com.example.cryptomoney.utils.Logger;
import com.example.cryptomoney.utils.NfcUtils;
import com.example.cryptomoney.utils.RSAUtils;
import com.example.cryptomoney.utils.WriteDialog;
import com.google.zxing.activity.CaptureActivity;
import com.google.zxing.util.Constant;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

import static java.lang.Math.min;

public class RequestActivity extends AppCompatActivity{

    private static final int RESULT_CHANGED = 2;
    private Button request;
    private Integer account_id;
    private Connection conn = null;

    private Double value;
    private NfcUtils nfcUtils;
    private WriteDialog NFCDialog;
    private String qrstring;
    private String nfcstring;
    private String fullstring;

    public RSAPublicKey contract_pk;
    public RSAPrivateKey contract_sk;
    private String contract_addr;
    private Boolean scanfinish = false;
    private Boolean showDialog = false;
    private String response;
    private String contract_pk_exp;
    private String contract_sk_exp;
    private String contract_modulus;
    public final static int MAX_ENCRYPT_BLOCK = 32;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_transfer);

        Intent intent_from_main = getIntent(); // 获取account_id
        account_id = intent_from_main.getIntExtra("account_id", 0);
        nfcUtils = new NfcUtils(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        request = (Button) findViewById(R.id.request);
        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get the amount to be transfered
                EditText amount = (EditText) findViewById(R.id.amount);

                boolean isdouble = isNumeric(amount.getText().toString());
                //invalid input
                if (isdouble != true)
                    Common.showShortToast(RequestActivity.this, "Invalid input. Only number is allowed");

                    //input valid
                else {
                    // request amount
                    value = Double.parseDouble(amount.getText().toString());

//                    Intent intent = new Intent(RequestActivity.this,RcvModeActivity.class); // 启动TransferActivity,传入account_id
//                    intent.putExtra("account_id",account_id);
//                    intent.putExtra("value",value);
//                    startActivityForResult(intent, 100);

                    // generate key pairs for a new contract;
                    KeyPair keypair = RSAUtils.generateRSAKeyPair(512);
                    contract_pk = (RSAPublicKey) keypair.getPublic();
                    contract_sk = (RSAPrivateKey) keypair.getPrivate();

                    contract_pk_exp = Base64Utils.encode(contract_pk.getPublicExponent().toByteArray());
                    contract_sk_exp = Base64Utils.encode(contract_sk.getPrivateExponent().toByteArray());
                    contract_modulus = Base64Utils.encode(contract_pk.getModulus().toByteArray());

                    // send account_id, request_amount, contract_pk to server
                    final String newContractRequest ="request=" + URLEncoder.encode("newcontract") +"&id="+ URLEncoder.encode(account_id.toString())+
                            "&value="+ URLEncoder.encode(value.toString()) + "&N="+ URLEncoder.encode(contract_modulus)+"&pk="+URLEncoder.encode(contract_pk_exp);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // server return contract address
                            contract_addr = PostService.Post(newContractRequest);
                            if (contract_addr == null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Common.showShortToast(RequestActivity.this,"request error");
                                    }
                                });
                            }

                            // start receiving tokens
                            else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        System.out.println("contract addr= "+contract_addr);
//                                        Common.showShortToast(RequestActivity.this,"new constract established, start receiving first token");
//                                        showSaveDialog();
                                        openCamera();
                                    }
                                });
                            }

                        }
                    }).start();



                }

            }
        });
    }

//    @Override
//    protected void onResumeFragments() {
//        System.out.println("into onresumefragments");
//        super.onResumeFragments();
//        if (showDialog) {
//            showDialog = false;
//            showSaveDialog();
//        }
//    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        // QR scan result call back
        // cancel contract
        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_CANCELED) {
            finish();
        }

        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_CHANGED) {
            value = Double.parseDouble(data.getStringExtra("new_amount"));
            openCamera();
        }

        if (requestCode == Constant.REQ_QR_CODE && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            qrstring = bundle.getString(Constant.INTENT_EXTRA_KEY_QR_SCAN);
            System.out.println("qrstring= "+qrstring);

            // only QR
            if (qrstring.indexOf("N=") == 0) {
                fullstring = qrstring;
                scanfinish = true;
                sendtokenaddr();
            }

            // if qr string not begin with "N=" but begin with "N", then NFC is needed, otherwise wrong string
            else if (qrstring.indexOf("N=") != 0 && qrstring.indexOf("N") == 0 ) {
                showDialog = true;
                showSaveDialog();
            }
//
            else {
                Common.showShortToast(RequestActivity.this, "Invalid message,scan again");
                openCamera();
            }
        }

//        super.onActivityResult(requestCode, resultCode, data);
//        final String sqloperation;
//        final String sqloperation2;

//        Double amount_received = data.getDoubleExtra("received", 0);
//        Integer payer_id = data.getIntExtra("payer", 0);
//        final Double reminder = amount_received - value;
//
//        if (amount_received == value){
//            sqloperation = "request=" + URLEncoder.encode("addmoney") + "&to_account=" + URLEncoder.encode(account_id.toString())
//                + "&value=" + URLEncoder.encode(value.toString());
//            run(sqloperation);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Common.showShortToast(RequestActivity.this, "request succeeded");
//                    finish();
//                }
//            });
//        }
//        else if (amount_received > value){
//            sqloperation = "request=" + URLEncoder.encode("addmoney") + "&to_account=" + URLEncoder.encode(account_id.toString())
//                    + "&value=" + URLEncoder.encode(value.toString());
//            run(sqloperation);
//            sqloperation2 = "request=" + URLEncoder.encode("addmoney") + "&to_account=" + URLEncoder.encode(payer_id.toString())
//                    + "&value=" + URLEncoder.encode(reminder.toString());
//            run(sqloperation2);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Common.showShortToast(RequestActivity.this, "request succeeded");
//                    finish();
//                }
//            });
//        }
//        else {
//            if (amount_received > 0) {
//                sqloperation2 = "request=" + URLEncoder.encode("addmoney") + "&to_account=" + URLEncoder.encode(payer_id.toString())
//                        + "&value=" + URLEncoder.encode(amount_received.toString());
//                run(sqloperation2);
//            }
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Common.showShortToast(RequestActivity.this, "Request failed. Money returned to payer");
//                    finish();
//                }
//            });
//        }


    }

//    public void run(final String operation) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                final String response = PostService.Post(operation);
//            }
//        }).start();
//    }

    //helper functions
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]+[.]{0,1}[0-9]*[dD]{0,1}");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
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

    private void openCamera() {
        try {
            Intent intent = new Intent(RequestActivity.this, CaptureActivity.class);
            intent.putExtra("contract_addr",contract_addr);
            intent.putExtra("contract_sk_exp",contract_sk_exp);
            intent.putExtra("contract_modulus",contract_modulus);
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
            sendtokenaddr();
        }
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

    public void sendtokenaddr() {
        if (scanfinish) {
            // get fullstring and extract sk, addr
            System.out.println("full string= " + fullstring);
            String N_base64 = fullstring.split("N=")[1].split("&d=")[0];
            BigInteger N = new BigInteger(Base64Utils.decode(N_base64));
            String d_base64 = fullstring.split("&d=")[1].split("&addr=")[0];
            BigInteger d = new BigInteger(Base64Utils.decode(d_base64));

            String token_addr = fullstring.split("&addr=")[1];
            System.out.println("token addr= " + token_addr);

            try {
                // extract token sk from N,d
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(N, d);
                PrivateKey token_sk = keyFactory.generatePrivate(rsaPrivateKeySpec);

                // encrypt rcver account id  with token sk -> enc_id
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, token_sk);
                String rcver_id = "ACCOUNT=" + account_id.toString();
                byte[] enc_bytes = cipher.doFinal(rcver_id.getBytes());
                String enc_id = Base64Utils.encode(enc_bytes);

                // encrypt {token_addr, enc_id} with contract sk
                cipher.init(Cipher.ENCRYPT_MODE, contract_sk);
                String plain_str = token_addr + "&ENC_ID=" + enc_id;

                // block cipher
                int inputLength = plain_str.getBytes().length;
//                System.out.println("input length：" + inputLength);
                int offSet = 0;
                byte[] resultBytes = {};
                byte[] cache = {};
                while (inputLength - offSet > 0) {
                    if (inputLength - offSet > MAX_ENCRYPT_BLOCK) {
                        cache = cipher.doFinal(plain_str.getBytes(), offSet, MAX_ENCRYPT_BLOCK);
                        offSet += MAX_ENCRYPT_BLOCK;
                    } else {
                        cache = cipher.doFinal(plain_str.getBytes(), offSet, inputLength - offSet);
                        offSet = inputLength;
                    }
                    resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
                    System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
                }
//                enc_bytes = cipher.doFinal(plain_str.getBytes());
//                System.out.println("output length= "+resultBytes.length);
                String enc_str = Base64Utils.encode(resultBytes);

                // final string sent to server: {contract_addr,{token_addr,{rcver_id}_tokensk_contractsk}}
                final String recordtokenRequest = "request=" + URLEncoder.encode("recordtoken") +
                        "&contract_addr=" + URLEncoder.encode(contract_addr) + "&enc=" + URLEncoder.encode(enc_str);
                System.out.println("recordtokenRequest= "+recordtokenRequest);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // server response will be "enough" or "not enough"
                        response = PostService.Post(recordtokenRequest);
                        System.out.println("response= "+response);
                        if (response != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (response.equals("not enough") || response.equals("expired token")) {
                                        // start rcving next token
                                        Common.showLongToast(RequestActivity.this, response);
                                        openCamera();
                                    } else if (response.equals("enough")) {
                                        // contract succeeded, finish
                                        Common.showLongToast(RequestActivity.this, value + " received successfully");
                                        finish();
                                    }
                                    else {
                                        Common.showShortToast(RequestActivity.this,response);
                                        finish();
                                    }
                                }
                            });

                        }
                    }
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
                Common.showShortToast(RequestActivity.this,response);
            }
        }

    }
}
