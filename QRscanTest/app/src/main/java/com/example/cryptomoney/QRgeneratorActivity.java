package com.example.cryptomoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.util.Constant;
import com.google.zxing.util.QrCodeGenerator;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cn.memobird.gtx.GTX;
import cn.memobird.gtx.GTXScripElement;
import cn.memobird.gtx.common.GTXKey;
import cn.memobird.gtx.listener.OnBluetoothFindListener;
import cn.memobird.gtx.listener.OnCodeListener;
import cn.memobird.gtx.listener.OnImageToDitherListener;
import cn.memobird.gtx.listener.OnImageToFilterListener;


public class QRgeneratorActivity extends AppCompatActivity {

    private EditText qrstring;
    private Button generate;
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

    private final static int REQ_LOC = 10;

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
        search = (Button) findViewById(R.id.search);
        qrimg = (ImageView) findViewById(R.id.qrimg);
        lvDevices = findViewById(R.id.lv_device);
        print = findViewById(R.id.print);

        GTX.init(getApplicationContext(), AK);  //初始化

        mDialog = Common.showLoadingDialog(QRgeneratorActivity.this);

        // 定义蓝牙连接列表
        deviceListAdapter = new DeviceListAdapter(this, devices);
        lvDevices.setAdapter(deviceListAdapter);

        // 搜索蓝牙
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(QRgeneratorActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)  // 检查运行时权限
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(QRgeneratorActivity.this,
                            new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_LOC);  // 申请定位权限
                }else {
                    devices.clear();
                    deviceListAdapter.notifyDataSetChanged();
                    if (mDialog != null)
                        mDialog.show();
                    //设备搜索
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
                    print();
                }
                else {
                    Common.showShortToast(QRgeneratorActivity.this, "please connect to the printer first");
                }
            }
        });

        // 生成二维码
        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = qrstring.getText().toString();
                qrimage = QrCodeGenerator.getQrCodeImage(text,100,100);
                qrimg.setImageBitmap(qrimage);
            }
        });

    }

    private void print() {
        GTX.doImageToDither(onDitherListener,qrimage,mDialog, Common.DEFAULT_IMAGE_WIDTH,true);
//        GTX.doImageToDither(qrimage,mDialog,Common.DEFAULT_IMAGE_WIDTH,true);
        Log.d("QRgeneratorActivity","string: "+base64ImageString);
        if (base64ImageString != null) {
            GTX.printImage(onPrintListener, base64ImageString, mDialog);
        }
    }

     //图像处理结果OnImageToDitherListener
    private OnImageToDitherListener onDitherListener = new OnImageToDitherListener() {
        @Override
        public void returnResult(String base64ImageString, Bitmap bitmap, int taskCode) {
            if (taskCode == GTXKey.RESULT.COMMON_SUCCESS && base64ImageString != null) {
//                Log.d("QRgeneratorActivity","String in Ditherlistener: "+base64ImageString);
                QRgeneratorActivity.this.base64ImageString = base64ImageString;
            } else {
//                Log.d("QRgeneratorActivity","Dither failed");
                Common.showShortToast(QRgeneratorActivity.this, "Error " + taskCode);
            }
        }
    };


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {  //权限请求结果回调
        if (requestCode == REQ_LOC && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            devices.clear();
            deviceListAdapter.notifyDataSetChanged();
            if (mDialog != null)
                mDialog.show();
            GTX.searchBluetoothDevices(onBluetoothFindListener, mDialog);
        }
        else {
            Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
        }
    }

    // 蓝牙搜索结果回调
    private OnBluetoothFindListener onBluetoothFindListener = new OnBluetoothFindListener() {
        @Override
        public void returnResult(int taskCode, BluetoothDevice bluetoothDevice, short signal) {
//            Log.d("QRgeneratorActivity","taskcode:" +taskCode);
            if (mDialog != null)
                mDialog.cancel();
            if (taskCode == GTXKey.RESULT.FIND_DEVICE_GET_ONE && bluetoothDevice != null) {     //搜索到一台设备
                devices.add(bluetoothDevice);
                deviceListAdapter.setDeviceList(devices);
                deviceListAdapter.notifyDataSetChanged();
                lvDevices.setVisibility(View.VISIBLE);
            } else if (taskCode == GTXKey.RESULT.FIND_DEVICE_FINISH_TASK) {     //搜索任务正常结束
                if (devices.size() == 0) {
                    Common.showShortToast(QRgeneratorActivity.this, "No available device");
                } else {
                    Common.showShortToast(QRgeneratorActivity.this, "device searching finished");
                }
            } else {                                                            //其它异常
                Common.showShortToast(QRgeneratorActivity.this, "Error " + taskCode);
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
                Toast.makeText(QRgeneratorActivity.this,"connected successfully",Toast.LENGTH_SHORT).show();
                if (GTX.getConnectDevice().getName().contains("G4")) {
                    Common.DEFAULT_IMAGE_WIDTH = Common.SIZE_576;
                } else {
                    Common.DEFAULT_IMAGE_WIDTH = Common.SIZE_384;
                }
            } else if (taskCode == GTXKey.RESULT.COMMON_FAIL) {
                Common.showShortToast(QRgeneratorActivity.this, "connection failed");
            } else {
                Common.showShortToast(QRgeneratorActivity.this, "Error " + taskCode);
            }
        }
    };

    // 打印结果监听
    private OnCodeListener onPrintListener = new OnCodeListener() {
        @Override
        public void returnResult(int taskCode) {
            if (taskCode == GTXKey.RESULT.COMMON_SUCCESS) {
                Common.showShortToast(QRgeneratorActivity.this, "successfully printed");
            } else {
                Log.d("QRgeneratorActivity","taskCode="+taskCode);
                Common.showShortToast(QRgeneratorActivity.this, "Error " + taskCode);
            }
        }
    };




    protected void onDestroy() {
        super.onDestroy();
        GTX.onDestroy();
    }



}