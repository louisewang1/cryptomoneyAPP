package com.example.cryptomoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import java.net.URLEncoder;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

public class CryptoActivity extends AppCompatActivity {

    private EditText amount;
    private Button request;
    private TextView response;

    private Integer account_id;
    private SharedPreferences pref;
    private String modulus;
    private String value;
    public final static int ADDR_LENGTH = 100;

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
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        modulus = pref.getString("modulus","");
        Log.d("CryptoActivity","modulus= "+modulus);

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                value = amount.getText().toString();
//                Log.d("CryptoActivity","value= "+value);
                final String cryptoRequest ="request=" + URLEncoder.encode("crypto") + "&id="+ URLEncoder.encode(account_id.toString())
                        +"&value="+ URLEncoder.encode(value) + "&N="+ URLEncoder.encode(modulus);

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