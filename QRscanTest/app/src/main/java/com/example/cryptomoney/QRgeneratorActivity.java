package com.example.cryptomoney;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.zxing.util.QrCodeGenerator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import cn.memobird.gtx.GTX;

import static com.google.zxing.util.QrCodeGenerator.getQrCodeImage;

public class QRgeneratorActivity extends AppCompatActivity {

    private EditText qrstring;
    private Button generate;
    private Button print;
    private ImageView qrimg;
    private ImageButton back;

    final String AK = "c6a5a445dc25490183f42088f4b78ccf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_generator);

        // hide default toolbar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // 添加返回上一层按钮
        back = (ImageButton) findViewById(com.google.zxing.R.id.btn_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        qrstring = (EditText) findViewById(R.id.qrstring);
        generate = (Button) findViewById(R.id.generate);
        print = (Button) findViewById(R.id.print);
        qrimg = (ImageView) findViewById(R.id.qrimg);

        init();
        closeAndroidPDialog();

        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = qrstring.getText().toString();
                Bitmap qrimage = QrCodeGenerator.getQrCodeImage(text,200,200);
                qrimg.setImageBitmap(qrimage);
            }
        });
        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(QRgeneratorActivity.this, BluetoothActivity.class));
            }
        });

    }

    private void init() {
        GTX.init(getApplicationContext(), AK);
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pi = pm.getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * android9.0 谷歌限制开发者调用非官方公开API 方法或接口(使用@hide注解的系统源码或反射)
     * 解决debug模式下在国内版Android P上的提醒弹窗 (Detected problems with API compatibility)
     */
    //TODO: has error, need fix
    private void closeAndroidPDialog() {
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}