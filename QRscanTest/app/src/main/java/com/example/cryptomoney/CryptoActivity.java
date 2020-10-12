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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cryptomoney.utils.Base64Utils;
import com.example.cryptomoney.utils.RSAUtils;

import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)  {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        account_id = getIntent().getIntExtra("account_id",0);
        Log.d("CryptoActivity","id= "+account_id);
        amount = (EditText) findViewById(R.id.amount);
        request = (Button) findViewById(R.id.request);
        response = (TextView) findViewById(R.id.response);
//        pref = PreferenceManager.getDefaultSharedPreferences(this);
//        pref = getSharedPreferences("cryptomoneyAPP", Context.MODE_PRIVATE);
//        modulus = pref.getString("modulus","");
//        Log.d("CryptoActivity","modulus= "+modulus);

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

                    final String cryptoRequest ="request=" + URLEncoder.encode("crypto") + "&id="+ URLEncoder.encode(account_id.toString())
                            +"&value="+ URLEncoder.encode(value) + "&N="+ URLEncoder.encode(modulus)+"&pk="+URLEncoder.encode(pk_exp);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final String addr = PostService.Post(cryptoRequest);
                            Log.d("CryptoActivity","response= "+addr);
                            if (addr != null && addr.length() == ADDR_LENGTH) {  // response return token address
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        response.setText("token address: " +addr);
                                        Toast.makeText(CryptoActivity.this,"Crypto transfer succeeded",Toast.LENGTH_SHORT).show();

//                                        store token address and sk, N in sharedpreference
                                        pref = getSharedPreferences("cryptomoneyAPP", Context.MODE_PRIVATE);
                                        editor = pref.edit();
                                        editor.putString(addr+"_skexp",sk_exp);
                                        editor.putString(addr+"_modulus",modulus);
                                        editor.apply();

                                        Intent intent = new Intent(CryptoActivity.this,CryptoModeActivity.class);
                                        intent.putExtra("amount",Double.parseDouble(value));
                                        intent.putExtra("address",addr);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }
                            else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        response.setText(addr);
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