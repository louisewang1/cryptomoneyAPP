package com.example.cryptomoney;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingActivity extends AppCompatActivity {

    private Button small;
    private Button large;
    private Button medium;

//    private Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settinglayout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)  {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        small = (Button) findViewById(R.id.Small);
        medium = (Button) findViewById(R.id.Medium);
        large = (Button) findViewById(R.id.Large);

//        logout = (Button) findViewById(R.id.logout);

        small.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                initFontScale(0.8);
                recreate();

            }
        });

        medium.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                initFontScale(1);
                recreate();
            }
        });

        large.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                initFontScale(1.3);
                recreate();
            }
        });

//        logout.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View view) {
//
//                Intent intent = new Intent(SettingActivity.this,LoginActivity.class); // 启动TransferActivity,传入account_id
//                startActivity(intent);
//
//            }
//        });
    }

    private void initFontScale(double ratio) {
        Configuration configuration = getResources().getConfiguration();
        configuration.fontScale = (float) ratio;
        //0.85 小, 1 标准大小, 1.15 大，1.3 超大 ，1.45 特大
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        getBaseContext().getResources().updateConfiguration(configuration, metrics);
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
