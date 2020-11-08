//package com.example.cryptomoney;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.ActionBar;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.Button;
//
//import cn.memobird.gtx.GTX;
//
//public class MerchantMainActivity extends AppCompatActivity {
//
//    private Button verify;
//    private Integer account_id;
//    private String type;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_merchant_main);
//
//        verify = (Button) findViewById(R.id.verify);
//
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null)  {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setDisplayShowTitleEnabled(false);
//        }
//
//        Intent intent_from_login = getIntent();
//        account_id = intent_from_login.getIntExtra("account_id",0);
//        type = intent_from_login.getStringExtra("type");
//
//        verify.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent_to_mode = new Intent(MerchantMainActivity.this,ReceiveActivity.class);
//                intent_to_mode.putExtra("account_id",account_id);
//                intent_to_mode.putExtra("type",type);
//                startActivity(intent_to_mode);
//            }
//        });
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                onBackPressed();
//            default:
//        }
//        return true;
//    }
//
//    @Override
//    public void onBackPressed() {
//        AlertDialog.Builder dialog = new AlertDialog.Builder(MerchantMainActivity.this);
//        dialog.setTitle("Exit confirmation");
//        dialog.setMessage("Are you sure to exit?");
//        dialog.setCancelable(true);
//        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                GTX.exitApp();
//                finish();
//            }
//        });
//        dialog.setNegativeButton("No", null);
//        dialog.show();
//    }
//}