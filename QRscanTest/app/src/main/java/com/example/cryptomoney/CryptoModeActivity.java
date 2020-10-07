package com.example.cryptomoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class CryptoModeActivity extends AppCompatActivity {

    private Button QR;
    private Button QRNFC;
    private Button NFC;
    private Double value;
    private String addr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto_mode);

        QR = findViewById(R.id.QR);
        QRNFC = findViewById(R.id.QRNFC);
        NFC = findViewById(R.id.NFC);

        Intent intent_from_main = getIntent();
        value = getIntent().getDoubleExtra("amount",0);
        addr = getIntent().getStringExtra("address");


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)  {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        QR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_to_QR = new Intent(CryptoModeActivity.this,QRgeneratorActivity.class);
                intent_to_QR.putExtra("amount",value);
                intent_to_QR.putExtra("address",addr);
                intent_to_QR.putExtra("mode","QR");
                startActivity(intent_to_QR);
                finish();
            }
        });

        QRNFC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_to_QRNFC = new Intent(CryptoModeActivity.this,QRNFCActivity.class);
                intent_to_QRNFC.putExtra("amount",value);
                intent_to_QRNFC.putExtra("address",addr);
                intent_to_QRNFC.putExtra("mode","QR&NFC");
                startActivity(intent_to_QRNFC);
                finish();
            }
        });

//        NFC.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent_to_NFC = new Intent(CryptoModeActivity.this,NFCRWActivity.class);
//                intent_to_NFC.putExtra("amount",value);
//                intent_to_NFC.putExtra("address",addr);
//                startActivity(intent_to_NFC);
//                finish();
//            }
//        });
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