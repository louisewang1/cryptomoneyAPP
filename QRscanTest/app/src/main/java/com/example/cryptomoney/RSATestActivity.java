package com.example.cryptomoney;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import android.app.Activity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.cryptomoney.utils.Base64Utils;
import com.example.cryptomoney.utils.RSAUtils;

public class RSATestActivity extends AppCompatActivity implements OnClickListener
{
    private Button btn1, btn2;// 加密，解密
    private EditText et1, et2, et3;// 需加密的内容，加密后的内容，解密后的内容
    private KeyPair keyPair;

    /* 密钥内容 base64 code */
    private static String PRIVATE_KEY = "MDwwDQYJKoZIhvcNAQEBBQADKwAwKAIhAKcg7uDfJy3t/auz70JcWtXCprnwzFX/K+0Kn0hfW531AgMBAAE=" ;
    private static String PUBLIC_KEY = "MIHBAgEAMA0GCSqGSIb3DQEBAQUABIGsMIGpAgEAAiEApyDu4N8nLe39q7PvQlxa1cKmufDMVf8r7QqfSF9bnfUCAwEAAQIgCmC3pJwGvUnhbheKvFaqxzBlRPkWvxAIp4QJJc2sy1ECEQDohufD8tgQfzcieRfVInDpAhEAt//3hPft66xEYRPcwkx9LQIQS4mXvPYk7qcFGDPSZDC+EQIQZp3kp022bQdoI1P2fI44CQIQV3NJCGgrA1OchrT9pK7+Ag==";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rsa_test);
        initView();


    }

    private void initView()
    {
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

        et1 = (EditText) findViewById(R.id.et1);
        et2 = (EditText) findViewById(R.id.et2);
        et3 = (EditText) findViewById(R.id.et3);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            // 加密
            case R.id.btn1:
                String source = et1.getText().toString().trim();
                try
                {
                    // 从字符串中得到公钥
                     PublicKey publicKey = RSAUtils.loadPublicKey(PUBLIC_KEY);
                    // 从文件中得到公钥
//                    InputStream inPublic = getResources().getAssets().open("rsa_public_key.pem");
//                    PublicKey publicKey = RSAUtils.loadPublicKey(inPublic);
//                    keyPair = RSAUtils.generateRSAKeyPair(256);
//                    PublicKey publicKey = keyPair.getPublic();
                    // 加密
                    byte[] encryptByte = RSAUtils.encryptData(source.getBytes(), publicKey);
                    // 为了方便观察吧加密后的数据用base64加密转一下，要不然看起来是乱码,所以解密是也是要用Base64先转换
                    String afterencrypt = Base64Utils.encode(encryptByte);
                    et2.setText(afterencrypt);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;
            // 解密
            case R.id.btn2:
                String encryptContent = et2.getText().toString().trim();
                try
                {
                    // 从字符串中得到私钥
                     PrivateKey privateKey = RSAUtils.loadPrivateKey(PRIVATE_KEY);
                    // 从文件中得到私钥
//                    InputStream inPrivate = getResources().getAssets().open("pkcs8_rsa_private_key.pem");
//                    PrivateKey privateKey = RSAUtils.loadPrivateKey(inPrivate);
//                    PrivateKey privateKey = keyPair.getPrivate();
                    // 因为RSA加密后的内容经Base64再加密转换了一下，所以先Base64解密回来再给RSA解密
                    byte[] decryptByte = RSAUtils.decryptData(Base64Utils.decode(encryptContent), privateKey);
                    String decryptStr = new String(decryptByte);
                    et3.setText(decryptStr);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
    }

}
