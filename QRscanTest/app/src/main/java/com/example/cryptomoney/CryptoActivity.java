package com.example.cryptomoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cryptomoney.utils.Base64Utils;
import com.example.cryptomoney.utils.RSAUtils;
import com.google.zxing.util.QrCodeGenerator;

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

import cn.memobird.gtx.GTX;
import cn.memobird.gtx.GTXScripElement;
import cn.memobird.gtx.common.GTXKey;
import cn.memobird.gtx.listener.OnBluetoothFindListener;
import cn.memobird.gtx.listener.OnCodeListener;
import cn.memobird.gtx.listener.OnImageToDitherListener;

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
    private RadioButton QR,QRNFC,NFC;
    private RadioButton ten,twenty,fifty,hundred;
    private RadioGroup modegroup;
    private RadioGroup amountgroup;
    private String moneyselect = "";
    private String printMode;
    private ImageView qrimg;
    private ImageButton back;
    private ListView lvDevices;                     //搜索到的设备列表
    private DeviceListAdapter deviceListAdapter;
    private Dialog mDialog;
    private Button print;
    private Bitmap qrimage;
    private String base64ImageString;
    private List<BluetoothDevice> devices = new ArrayList<>();
    private String text;

    private final static int REQ_LOC = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto);

        account_id = getIntent().getIntExtra("account_id",0);
//        Log.d("CryptoActivity","id= "+account_id);
        amount = (EditText) findViewById(R.id.amount);
        request = (Button) findViewById(R.id.request);
        response = (TextView) findViewById(R.id.response);
        spinner = (Spinner)findViewById(R.id.spinner);
        modegroup = findViewById(R.id.modegroup);
        QR = findViewById(R.id.QR);
        QRNFC = findViewById(R.id.QRNFC);
        NFC = findViewById(R.id.NFC);
        amountgroup = findViewById(R.id.amountgroup);
        ten = findViewById(R.id.ten);
        twenty = findViewById(R.id.twenty);
        fifty = findViewById(R.id.fifty);
        hundred = findViewById(R.id.hundred);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)  {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

//        GTX.init(getApplicationContext(), AK);  //初始化

        mDialog = Common.showLoadingDialog(CryptoActivity.this);

        // 定义蓝牙连接列表
//        deviceListAdapter = new DeviceListAdapter(this, devices);
//        lvDevices.setAdapter(deviceListAdapter);
        if (ContextCompat.checkSelfPermission(CryptoActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)  // 检查运行时权限
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(CryptoActivity.this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOC);  // 申请定位权限,MUST BE FINE!
        }else {
            GTX.searchBluetoothDevices(onBluetoothFindListener,mDialog);
        }

        modegroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()  {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.QR:
                        printMode = "QR";
                        break;
                    case R.id.QRNFC:
                        printMode = "QRNFC";
                        break;
                    case R.id.NFC:
                        printMode = "NFC";
                        break;
                    default:
                        break;
                }
            }
        });

        amountgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()  {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.ten:
                        moneyselect = "10";
                        break;
                    case R.id.twenty:
                        moneyselect = "20";
                        break;
                    case R.id.fifty:
                        moneyselect = "50";
                        break;
                    case R.id.hundred:
                        moneyselect = "100";
                        break;
                    default:
                        break;
                }
            }
        });

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
                if (!moneyselect.equals("") && value.equals("")) value = moneyselect;
//                Log.d("CryptoActivity","value= "+value);
                boolean isdouble = isNumeric(value);
                if (!isdouble) Common.showShortToast(CryptoActivity.this,"Invalid input");

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
                                // online payment
                                if  (serverresponse.indexOf("token=") == 0) {
                                    addr = serverresponse.split("token=")[1].split("&enc=")[0];
                                    enc = serverresponse.split("&enc=")[1];
                                }
                                // offline payment
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
                                        Toast.makeText(CryptoActivity.this, "start printing money....", Toast.LENGTH_SHORT).show();

                                        if (merchant_selected.equals("None")) {
                                            text = "N="+modulus+"&d="+sk_exp+"&addr="+addr;
                                            qrimage = QrCodeGenerator.getQrCodeImage(text,200,200);
                                        }
                                        else {
                                            text = enc;
                                            qrimage = QrCodeGenerator.getQrCodeImage(text,200,200);
                                        }

//                                        start printing money
//                                        System.out.println(printMode);
                                        if (printMode.equals("QR")) {
                                            if (GTX.getConnectDevice() != null) {
                                                printQR();
                                            }
                                            else {
                                                Common.showShortToast(CryptoActivity.this, "please connect to the printer first");
                                            }
                                        }

                                        else {
                                            Common.showShortToast(CryptoActivity.this, "please choose a print mode first");
                                        }
//                                        Intent intent = new Intent(CryptoActivity.this, CryptoModeActivity.class);
//                                        intent.putExtra("amount", Double.parseDouble(value));
//                                        intent.putExtra("address", addr);
//                                        intent.putExtra("merchant", merchant_selected);
//                                        if (serverresponse.indexOf("token=") == 0) {
//                                            intent.putExtra("enc", enc);
//                                        }
//                                        startActivity(intent);
//                                        finish();
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

    private void printQR() {
        GTX.doImageToDither(onDitherListener,qrimage,mDialog, Common.DEFAULT_IMAGE_WIDTH,true);
//        GTX.doImageToDither(qrimage,mDialog,Common.DEFAULT_IMAGE_WIDTH,true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("QRgeneratorActivity","string: "+base64ImageString);
                if (base64ImageString != null && Double.parseDouble(value) > 0) {
                    List<GTXScripElement> scripElements = new ArrayList<>();
                    scripElements.add(new GTXScripElement(1, "mode= "+printMode));
                    scripElements.add(new GTXScripElement(1, "amount= "+value));
                    scripElements.add(new GTXScripElement(1, "to merchant: "+merchant_selected));
                    scripElements.add(new GTXScripElement(5, base64ImageString));
                    GTX.printMixing(onPrintListener, scripElements, mDialog);
//                    GTX.printImage(onPrintListener, base64ImageString, mDialog);
                }
            }
        }).start();

    }

    //图像处理结果OnImageToDitherListener
    private OnImageToDitherListener onDitherListener = new OnImageToDitherListener() {
        @Override
        public void returnResult(String base64ImageString, Bitmap bitmap, int taskCode) {
            if (taskCode == GTXKey.RESULT.COMMON_SUCCESS && base64ImageString != null) {
//                Log.d("QRgeneratorActivity","String in Ditherlistener: "+base64ImageString);
               CryptoActivity.this.base64ImageString = base64ImageString;
            } else {
//                Log.d("QRgeneratorActivity","Dither failed");
                Common.showShortToast(CryptoActivity.this, "Error " + taskCode);
            }
        }
    };


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {  //权限请求结果回调
        if (requestCode == REQ_LOC && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            devices.clear();
//            deviceListAdapter.notifyDataSetChanged();
//            if (mDialog != null)
//                mDialog.show();
            GTX.searchBluetoothDevices(onBluetoothFindListener, mDialog);
        }
        else {
//            if (mDialog != null)
//                mDialog.cancel();
//            Log.d("QRgeneratorActivity","permission granted");
            Common.showShortToast(this,"Permission denied");
        }
    }

    // 蓝牙搜索结果回调
    private OnBluetoothFindListener onBluetoothFindListener = new OnBluetoothFindListener() {
        @Override
        public void returnResult(int taskCode, BluetoothDevice bluetoothDevice, short signal) {
            Log.d("QRgeneratorActivity","taskcode:" +taskCode);
            if (mDialog != null)
                mDialog.cancel();
            if (taskCode == GTXKey.RESULT.FIND_DEVICE_GET_ONE && bluetoothDevice != null) {     //搜索到一台设备
                if (bluetoothDevice.getName() != null && bluetoothDevice.getName().equals("MEMOBIRD GO")) {
                    GTX.connectBluetoothDevice(onConnectListener, bluetoothDevice, mDialog);
//                    devices.add(bluetoothDevice);
                }
//                deviceListAdapter.setDeviceList(devices);
//                deviceListAdapter.notifyDataSetChanged();
//                lvDevices.setVisibility(View.VISIBLE);
            } else if (taskCode == GTXKey.RESULT.FIND_DEVICE_FINISH_TASK) {     //搜索任务正常结束
                if (devices.size() == 0) {
                    Common.showShortToast(CryptoActivity.this, "No MEMOBIRD device available");
                } else {
                    Common.showShortToast(CryptoActivity.this, "device searching finished");
                }
            } else {                                                            //其它异常
                Common.showShortToast(CryptoActivity.this, "Error " + taskCode);
            }
        }

    };

    // 设备连接结果监听
    private OnCodeListener onConnectListener = new OnCodeListener() {
        @Override
        public void returnResult(int taskCode) {
            if (mDialog != null)
                mDialog.cancel();
            if (taskCode == GTXKey.RESULT.COMMON_SUCCESS) {              //成功连接设备
//                lvDevices.setVisibility(View.GONE);
                Common.showShortToast(CryptoActivity.this,"connected to a printer successfully");
                if (GTX.getConnectDevice().getName().contains("G4")) {
                    Common.DEFAULT_IMAGE_WIDTH = Common.SIZE_576;
                } else {
                    Common.DEFAULT_IMAGE_WIDTH = Common.SIZE_384;
                }
            } else if (taskCode == GTXKey.RESULT.COMMON_FAIL) {
                Common.showShortToast(CryptoActivity.this, "connection failed");
            } else {
                Common.showShortToast(CryptoActivity.this, "Error " + taskCode);
            }
        }
    };

    // 打印结果监听
    private OnCodeListener onPrintListener = new OnCodeListener() {
        @Override
        public void returnResult(int taskCode) {
            if (taskCode == GTXKey.RESULT.COMMON_SUCCESS) {
                Common.showShortToast(CryptoActivity.this, "successfully printed");
            } else {
                Log.d("QRgeneratorActivity","taskCode="+taskCode);
                Common.showShortToast(CryptoActivity.this, "Error " + taskCode);
            }
        }
    };

}