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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cryptomoney.utils.Base64Utils;
import com.example.cryptomoney.utils.RSAUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CryptoActivity extends AppCompatActivity {

    private EditText amount;
    private Button request;
    private TextView response;

    private Integer account_id;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String value;
    private String pk_exp;
    private String sk_exp;
    private String modulus;
    public final static int ADDR_LENGTH = 20;
    private KeyPair keypair;
    private Spinner spinner = null;
    private ArrayAdapter<String> adapter = null;
    private String[] merchantList;
    private String merchant_selected;
    private String addr;
    private String enc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto);

        account_id = getIntent().getIntExtra("account_id",0);
        Log.d("CryptoActivity","id= "+account_id);
        amount = (EditText) findViewById(R.id.amount);
        request = (Button) findViewById(R.id.request);
        response = (TextView) findViewById(R.id.response);
        spinner = (Spinner)findViewById(R.id.spinner);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)  {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        // get all merchant lists
        final String merchantlistRequest ="request=" + URLEncoder.encode("merchantlist");
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String response = PostService.Post(merchantlistRequest);
//                System.out.println(response)
                if (response != null) {
                    try {
                        List<String> array = new ArrayList<>();
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            array.add(jsonObject.getString("username"));
                        }
                        merchantList = new String[array.size()+1];
                        merchantList[0] = "None";
                        for (int i=1;i<array.size()+1;i++) {
                            merchantList[i] = array.get(i-1);
                        }
//                        System.out.println(merchantList);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter = new ArrayAdapter<String>(CryptoActivity.this,android.R.layout.simple_spinner_item,merchantList);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(adapter);

                                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                        merchant_selected = (String) ((TextView)view).getText();
//                System.out.println(merchant_selected);
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> adapterView) {
                                    }
                                });
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                value = amount.getText().toString();
//                Log.d("CryptoActivity","value= "+value);
                boolean isdouble = isNumeric(value);
                if (isdouble != true) Common.showShortToast(CryptoActivity.this,"Invalid input");

                else {
//                    generate RSA key pairs
                    keypair = RSAUtils.generateRSAKeyPair(512);
                    RSAPublicKey publicKey = (RSAPublicKey) keypair.getPublic();
                    RSAPrivateKey privateKey = (RSAPrivateKey) keypair.getPrivate();

                    pk_exp = Base64Utils.encode(publicKey.getPublicExponent().toByteArray());
                    sk_exp = Base64Utils.encode(privateKey.getPrivateExponent().toByteArray());
                    modulus = Base64Utils.encode(publicKey.getModulus().toByteArray());

                    final String cryptoRequest ="request=" + URLEncoder.encode("crypto") +"&merchant="+ URLEncoder.encode(merchant_selected)+
                            "&id="+ URLEncoder.encode(account_id.toString()) +"&value="+ URLEncoder.encode(value) +
                            "&N="+ URLEncoder.encode(modulus)+"&pk="+URLEncoder.encode(pk_exp);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final String serverresponse = PostService.Post(cryptoRequest);
                            if (serverresponse != null && (serverresponse.indexOf("token=") == 0 || serverresponse.length() == ADDR_LENGTH)) {
                                if  (serverresponse.indexOf("token=") == 0) {
                                    addr = serverresponse.split("token=")[1].split("&enc=")[0];
                                    enc = serverresponse.split("&enc=")[1];
                                }
                                else if (serverresponse.length() == ADDR_LENGTH) {
                                    addr = serverresponse;
                                }

                                pref = getSharedPreferences("cryptomoneyAPP", Context.MODE_PRIVATE);
                                editor = pref.edit();
                                editor.putString(addr + "_skexp", sk_exp);
                                editor.putString(addr + "_modulus", modulus);
                                editor.apply();

                                Log.d("CryptoActivity", "response= " + serverresponse);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        response.setText("token address: " + addr);
                                        Toast.makeText(CryptoActivity.this, "Crypto transfer succeeded", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(CryptoActivity.this, CryptoModeActivity.class);
                                        intent.putExtra("amount", Double.parseDouble(value));
                                        intent.putExtra("address", addr);
                                        intent.putExtra("merchant", merchant_selected);
                                        if (serverresponse.indexOf("token=") == 0) {
                                            intent.putExtra("enc", enc);
                                        }
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                            else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        response.setText(serverresponse);
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

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]+[.]{0,1}[0-9]*[dD]{0,1}");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }

}