package com.example.cryptomoney;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
//import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.memobird.gtx.GTX;
import cn.memobird.gtx.GTXScripElement;
import cn.memobird.gtx.common.GTXKey;
import cn.memobird.gtx.listener.OnBluetoothFindListener;
import cn.memobird.gtx.listener.OnCodeListener;
import cn.memobird.gtx.listener.OnImageToDitherListener;
import cn.memobird.gtx.listener.OnImageToFilterListener;
//import cn.memobird.gtxprintdemo.adapter.DeviceListAdapter;
//import cn.memobird.gtxprintdemo.util.Common;
//import cn.memobird.gtxprintdemo.util.FileUtil;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


public class BluetoothActivity extends AppCompatActivity {

    Button btBluetoothSearch, btGetImage, btPrint, btFilterNext, btSetting;
    Switch sWordModel;                      //select=文本打印
    Switch sImageModel;                     //select=图片打印
    Switch sMixingModel;                    //select=图文混编打印
    ImageView ivImageProcess;               //打印图片处理后的显示
    EditText etText;                        //打印文本输入
    TextView tvSelectDevice, tvFilterName;
    ListView lvDevices;                     //搜索到的设备列表
    DeviceListAdapter deviceListAdapter;

    List<BluetoothDevice> devices = new ArrayList<>();
    File imageFile = null;
    String base64ImageString;               //图片base64处理后的结果，用于打印线程传入的参数
    final int RQ_GET_IMAGE = 3;
    int currFilterType = GTXKey.FILTER.TYPE_DEFAULT;
    Bitmap bitmapPreview;
    Context mContext;
    Dialog mDialog;

    private ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

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

        mContext = this;
        findViewById();
        setListener();
        mDialog = Common.showLoadingDialog(mContext);
        init();
    }

    private void findViewById() {
        btPrint = findViewById(R.id.bt_print);
        btBluetoothSearch = findViewById(R.id.bt_bluetooth);
        btGetImage = findViewById(R.id.bt_images);
        btFilterNext = findViewById(R.id.bt_filter_next);
        btSetting = findViewById(R.id.bt_setting);
        sWordModel = findViewById(R.id.s_word_model);
        sImageModel = findViewById(R.id.s_image_model);
        sMixingModel = findViewById(R.id.s_mixing_model);
        etText = findViewById(R.id.et_Text);
        tvSelectDevice = findViewById(R.id.tv_select_device);
        tvFilterName = findViewById(R.id.tv_filter_name);
        ivImageProcess = findViewById(R.id.iv_image_process);
        lvDevices = findViewById(R.id.lv_device);

    }

    private void init() {
        deviceListAdapter = new DeviceListAdapter(this, devices);
        lvDevices.setAdapter(deviceListAdapter);
        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                tvSelectDevice.setText("选择的设备：\n" + devices.get(i).getName() + "\n" + devices.get(i).getAddress());
                if (mDialog != null)
                    mDialog.show();
                // 选中设备后，开始连接该设备蓝牙
                GTX.connectBluetoothDevice(onConnectListener, devices.get(i), mDialog);
            }
        });

        // 图片滤镜参数设置，设置为-1则为默认值，详情见文档说明
        GTX.setImageFilterParam(GTXKey.FILTER.CONFIG_CONTRAST, 1.0f);  //设置对比度，参数static存储，不设置则为上一次设置值，对IMAGE_ENHANCE无效
        GTX.setImageFilterParam(GTXKey.FILTER.CONFIG_BRIGHTNESS, -1);  //亮度设置为默认，参数static存储，不设置则为上一次设置值，对IMAGE_ENHANCE无效
        GTX.setImageFilterParam(GTXKey.FILTER.CONFIG_ENHANCE_CONTRAST, -1);  //IMAGE_ENHANCE专属参数，参数static存储，不设置则为上一次设置值
        GTX.setImageFilterParam(GTXKey.FILTER.CONFIG_ENHANCE_EFFECTS, -1);  //IMAGE_ENHANCE专属参数，参数static存储，不设置则为上一次设置值
    }

    private void setListener() {
        sWordModel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sImageModel.setChecked(false);
                    sMixingModel.setChecked(false);
                }
            }
        });
        sImageModel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sWordModel.setChecked(false);
                    sMixingModel.setChecked(false);
                }
            }
        });
        sMixingModel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sImageModel.setChecked(false);
                    sWordModel.setChecked(false);
                }
            }
        });
        btFilterNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currFilterType = Common.getNextFilter();
                reloadImageByFilter();
                tvFilterName.setText("滤镜：" + Common.getFilterName(currFilterType));
            }
        });
        btSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GTX.getConnectDevice() != null) {
                    Intent intent = new Intent(BluetoothActivity.this, SettingActivity.class);
                    startActivity(intent);
                } else {
                    Common.showShortToast(BluetoothActivity.this, "请连接设备！");
                }
            }
        });
        // 开启设备搜索，在当前搜索任务未结束之前，重复点击无效
        btBluetoothSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                devices.clear();
                deviceListAdapter.notifyDataSetChanged();
                if (mDialog != null)
                    mDialog.show();
                // 设备搜素
                GTX.searchBluetoothDevices(onBluetoothFindListener, mDialog);
            }
        });
        // 请求相册权限，请求成功后拉起相册
        btGetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 开启相册
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, RQ_GET_IMAGE);
            }
        });
        // 打印，当前打印任务未完成之前，重复点击无效
        btPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GTX.getConnectDevice() != null) {
                    if (sWordModel.isChecked() || sImageModel.isChecked() || sMixingModel.isChecked()) {
                        print();
                    } else {
                        Common.showShortToast(BluetoothActivity.this, "请选择打印模式！");
                    }

                } else {
                    Common.showShortToast(BluetoothActivity.this, "请连接设备！");
                }
            }
        });
    }

    private void print() {
        String strText = etText.getText().toString();
        if (sWordModel.isChecked()) {
            if (strText != null && strText.length() > 0) {
                // 打印文字
                GTX.printText(onPrintListener, strText, false, false, mDialog);
            } else {
                Common.showShortToast(BluetoothActivity.this, "请输入内容！");
            }
        } else if (sImageModel.isChecked()) {        //图片打印模式
            if (base64ImageString != null && base64ImageString.length() > 0) {
                // 打印图片
                GTX.printImage(onPrintListener, base64ImageString, mDialog);
            } else {
                Common.showShortToast(BluetoothActivity.this, "请选择图片！");
            }
        } else {                                    //图文混编打印模式
            if (strText != null && strText.length() > 0 && base64ImageString != null && base64ImageString.length() > 0) {
                List<GTXScripElement> scripElements = new ArrayList<>();
                scripElements.add(new GTXScripElement(1, strText));
                scripElements.add(new GTXScripElement(5, base64ImageString));
                GTX.printMixing(onPrintListener, scripElements, mDialog);
            } else {
                Common.showShortToast(BluetoothActivity.this, "文本和图片不能为空！");
            }
        }
    }

    private void reloadImageByFilter() {
        if (imageFile != null && imageFile.exists()) {
            Bitmap b = FileUtil.getBitmap(imageFile.getPath());
            if (b != null) {
                // 图片处理
                GTX.doImageToFilter(onDoImageListener, b, currFilterType,
                        mDialog);
            } else Toast.makeText(mContext, "获取图片失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case RQ_GET_IMAGE:
                if (data.getData() != null) {
                    imageFile = FileUtil.getFileByUri(BluetoothActivity.this, data.getData());
                    reloadImageByFilter();
                }
                break;
        }
    }


    // 设备搜索结果监听，每搜索到一台设备则该回调方法会被回调一次
    private OnBluetoothFindListener onBluetoothFindListener = new OnBluetoothFindListener() {
        @Override
        public void returnResult(int taskCode, BluetoothDevice bluetoothDevice, short signal) {
            if (mDialog != null)
                mDialog.cancel();
            if (taskCode == GTXKey.RESULT.FIND_DEVICE_GET_ONE && bluetoothDevice != null) {     //搜索到一台设备
                devices.add(bluetoothDevice);
                deviceListAdapter.setDeviceList(devices);
                deviceListAdapter.notifyDataSetChanged();
                lvDevices.setVisibility(View.VISIBLE);
            } else if (taskCode == GTXKey.RESULT.FIND_DEVICE_FINISH_TASK) {     //搜索任务正常结束
                if (devices.size() == 0) {
                    Common.showShortToast(BluetoothActivity.this, "请确定设备是否开启或是否被其它手机连接！");
                } else {
                    Common.showShortToast(BluetoothActivity.this, "设备搜索结束！");
                }
            } else {                                                            //其它异常
                Common.showShortToast(BluetoothActivity.this, "Error " + taskCode);
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
                if (GTX.getConnectDevice().getName().contains("G4")) {
                    Common.DEFAULT_IMAGE_WIDTH = Common.SIZE_576;
                } else {
                    Common.DEFAULT_IMAGE_WIDTH = Common.SIZE_384;
                }
            } else if (taskCode == GTXKey.RESULT.COMMON_FAIL) {
                Common.showShortToast(BluetoothActivity.this, "连接失败！");
            } else {
                Common.showShortToast(BluetoothActivity.this, "Error " + taskCode);
            }
        }
    };

    // 图像处理结果OnImageToDitherListener
    private OnImageToDitherListener onDitherListener = new OnImageToDitherListener() {
        @Override
        public void returnResult(String base64ImageString, Bitmap bitmap, int taskCode) {
            if (taskCode == GTXKey.RESULT.COMMON_SUCCESS && base64ImageString != null) {
                BluetoothActivity.this.base64ImageString = base64ImageString;
                ivImageProcess.setImageBitmap(bitmap);
            } else {
                Common.showShortToast(BluetoothActivity.this, "Error " + taskCode);
            }
        }
    };
    OnImageToFilterListener onDoImageListener = new OnImageToFilterListener() {
        @Override
        public void returnResult(Bitmap bitmap, int taskCode) {
            if (taskCode == GTXKey.RESULT.COMMON_SUCCESS) {
                bitmapPreview = bitmap;
                ivImageProcess.setImageBitmap(bitmapPreview);
                if (GTX.getConnectDevice()!=null && GTX.getConnectDevice().getName().contains("4G"))
                    Common.DEFAULT_IMAGE_WIDTH = Common.SIZE_576;
                GTX.doImageToDither(onDitherListener, bitmapPreview, mDialog, Common.DEFAULT_IMAGE_WIDTH, true);
            } else {
                Common.showShortToast(BluetoothActivity.this, "Error " + taskCode);
            }
        }
    };

    // 打印结果监听
    private OnCodeListener onPrintListener = new OnCodeListener() {
        @Override
        public void returnResult(int taskCode) {
            if (taskCode == GTXKey.RESULT.COMMON_SUCCESS) {
                Common.showShortToast(BluetoothActivity.this, "打印成功！");
            } else {
                Common.showShortToast(BluetoothActivity.this, "Error " + taskCode);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GTX.resetAllImageFilterParam();     //重置所有滤镜参数
        GTX.onDestroy();
    }

}
