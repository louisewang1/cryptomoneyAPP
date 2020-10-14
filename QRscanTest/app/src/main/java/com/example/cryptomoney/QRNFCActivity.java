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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.nfc.FormatException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cryptomoney.utils.NfcUtils;
import com.google.zxing.util.Constant;
import com.google.zxing.util.QrCodeGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import cn.memobird.gtx.GTX;
import cn.memobird.gtx.GTXScripElement;
import cn.memobird.gtx.common.GTXKey;
import cn.memobird.gtx.listener.OnBluetoothFindListener;
import cn.memobird.gtx.listener.OnCodeListener;
import cn.memobird.gtx.listener.OnImageToDitherListener;
import cn.memobird.gtx.listener.OnImageToFilterListener;


public class QRNFCActivity extends AppCompatActivity {

    //    private EditText qrstring;
//    private Button generate;
    private Button search;
    private ImageView qrimg;
    private ImageButton back;
    private ListView lvDevices;                     //搜索到的设备列表
    private DeviceListAdapter deviceListAdapter;
    private Dialog mDialog;
    private Button print;
    private Bitmap qrimage;
    private String base64ImageString;
    private List<BluetoothDevice> devices = new ArrayList<>();
    private Double amount;
    private String address;
    private SharedPreferences pref;
    private String sk_exp;
    private String modulus;
    private String pk_exp;
    private String account_id;
    private String qrtext;
    private String text;
    private String nfctext;
    private NfcUtils nfcUtils;
    private TextView nfcstatus;
    private String mode;
    private String merchant;
    private String enc;

    private final static int REQ_LOC = 10;

//    final String AK = "c6a5a445dc25490183f42088f4b78ccf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_nfc);

        amount = getIntent().getDoubleExtra("amount",0);
        address = getIntent().getStringExtra("address");
        mode = getIntent().getStringExtra("mode");
        merchant =getIntent().getStringExtra("merchant");
        nfcUtils = new NfcUtils(this);

//        pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref = getSharedPreferences("cryptomoneyAPP", Context.MODE_PRIVATE);
//        account_id = pref.getString("id","");
//        pk_exp = pref.getString("pk_exp","");
        sk_exp = pref.getString(address+"_skexp","");
        modulus =  pref.getString(address+"_modulus","");


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)  {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

//        qrstring = (EditText) findViewById(R.id.qrstring);
//        generate = (Button) findViewById(R.id.generate);
        search = (Button) findViewById(R.id.search);
        qrimg = (ImageView) findViewById(R.id.qrimg);
        lvDevices = findViewById(R.id.lv_device);
        print = findViewById(R.id.print);
        nfcstatus = findViewById(R.id.nfcstatus);

//        qrstring.setText("&sk="+privatekey+"&addr="+address);
//        text = "N="+modulus+"&d="+sk_exp+"&addr="+address;
        if (merchant.equals("None")) {
            text = "N="+modulus+"&d="+sk_exp+"&addr="+address;
        }
        else {
            enc = getIntent().getStringExtra("enc");
            text = enc;
        }
        System.out.println(text);
        qrtext = extractodd(text);
        nfctext = extracteven(text);
        qrimage = QrCodeGenerator.getQrCodeImage(qrtext,200,200);
        qrimg.setImageBitmap(qrimage);

//        GTX.init(getApplicationContext(), AK);  //初始化

        mDialog = Common.showLoadingDialog(QRNFCActivity.this);

        // 定义蓝牙连接列表
        deviceListAdapter = new DeviceListAdapter(this, devices);
        lvDevices.setAdapter(deviceListAdapter);

        // 搜索蓝牙
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(QRNFCActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)  // 检查运行时权限
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(QRNFCActivity.this,
                            new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOC);  // 申请定位权限,MUST BE FINE!
                }else {
                    devices.clear();
                    deviceListAdapter.notifyDataSetChanged();
                    if (mDialog != null)
                        mDialog.show();
                    //设备搜索
                    Log.d("QRgeneratorActivity","start searching");
                    GTX.searchBluetoothDevices(onBluetoothFindListener, mDialog);
                }

            }
        });

        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                tvSelectDevice.setText("选择的设备：\n" + devices.get(i).getName() + "\n" + devices.get(i).getAddress());
                if (mDialog != null)
                    mDialog.show();
                // 选中设备后，开始连接该设备蓝牙
                GTX.connectBluetoothDevice(onConnectListener, devices.get(i), mDialog);
            }
        });

        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GTX.getConnectDevice() != null) {
                    printQR();
                }
                else {
                    Common.showShortToast(QRNFCActivity.this, "please connect to the printer first");
                }
            }
        });

    }

    //        write in NFC
    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        try {
            nfcUtils.writeNFCToTag(nfctext, intent);
            //                    Log.d("NFCRWActivity","finish writing");
            Common.showShortToast(this, "write to NFC successfully.");
            nfcstatus.setText("Status: writing successfully");
        } catch (IOException e) {
            Common.showShortToast(this, "write to NFC failed.");
            //                    Log.d("NFCRWActivity","write to NFC failed：" + e.getMessage());
        } catch (FormatException e) {
            Common.showShortToast(this, "write to failed.");
            //                    Log.d("NFCRWActivity","write to NFC failed：" + e.getMessage());
        }
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
                if (base64ImageString != null && amount > 0) {
                    List<GTXScripElement> scripElements = new ArrayList<>();
                    scripElements.add(new GTXScripElement(1, "mode= "+mode));
                    scripElements.add(new GTXScripElement(1, "amount= "+amount.toString()));
                    scripElements.add(new GTXScripElement(1, "to merchant: "+merchant));
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
                QRNFCActivity.this.base64ImageString = base64ImageString;
            } else {
//                Log.d("QRgeneratorActivity","Dither failed");
                Common.showShortToast(QRNFCActivity.this, "Error " + taskCode);
            }
        }
    };


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {  //权限请求结果回调
        if (requestCode == REQ_LOC && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("QRgeneratorActivity","permission granted");
            devices.clear();
            deviceListAdapter.notifyDataSetChanged();
            if (mDialog != null)
                mDialog.show();
            GTX.searchBluetoothDevices(onBluetoothFindListener, mDialog);
        }
        else {
            if (mDialog != null)
                mDialog.cancel();
            Log.d("QRgeneratorActivity","permission granted");
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
                    devices.add(bluetoothDevice);
                }
                deviceListAdapter.setDeviceList(devices);
                deviceListAdapter.notifyDataSetChanged();
                lvDevices.setVisibility(View.VISIBLE);
            } else if (taskCode == GTXKey.RESULT.FIND_DEVICE_FINISH_TASK) {     //搜索任务正常结束
                if (devices.size() == 0) {
                    Common.showShortToast(QRNFCActivity.this, "No MEMOBIRD device available");
                } else {
                    Common.showShortToast(QRNFCActivity.this, "device searching finished");
                }
            } else {                                                            //其它异常
                Common.showShortToast(QRNFCActivity.this, "Error " + taskCode);
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
                lvDevices.setVisibility(View.GONE);
                Common.showShortToast(QRNFCActivity.this,"connected successfully");
                if (GTX.getConnectDevice().getName().contains("G4")) {
                    Common.DEFAULT_IMAGE_WIDTH = Common.SIZE_576;
                } else {
                    Common.DEFAULT_IMAGE_WIDTH = Common.SIZE_384;
                }
            } else if (taskCode == GTXKey.RESULT.COMMON_FAIL) {
                Common.showShortToast(QRNFCActivity.this, "connection failed");
            } else {
                Common.showShortToast(QRNFCActivity.this, "Error " + taskCode);
            }
        }
    };

    // 打印结果监听
    private OnCodeListener onPrintListener = new OnCodeListener() {
        @Override
        public void returnResult(int taskCode) {
            if (taskCode == GTXKey.RESULT.COMMON_SUCCESS) {
                Common.showShortToast(QRNFCActivity.this, "successfully printed");
            } else {
                Log.d("QRgeneratorActivity","taskCode="+taskCode);
                Common.showShortToast(QRNFCActivity.this, "Error " + taskCode);
            }
        }
    };


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:
        }
        return true;
    }

    public String extractodd(String text) {
        String oddtext = "";
        for (int i=0; i<text.length();i=i+2) {
            oddtext +=text.charAt(i);
        }
        System.out.println(oddtext);
        return oddtext;
    }

    public String extracteven(String text) {
        String eventext = "";
        for (int i=1; i<text.length();i=i+2) {
            eventext +=text.charAt(i);
        }
        System.out.println(eventext);
        return eventext;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Logger.e(TAG, "onResume");
        nfcUtils.enableForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Logger.e(TAG, "onPause");
        nfcUtils.disableForegroundDispatch();
    }

    protected void onDestroy() {
        super.onDestroy();
        GTX.onDestroy();
    }

}