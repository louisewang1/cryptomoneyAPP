package com.example.cryptomoney;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import cn.memobird.gtx.GTX;
import cn.memobird.gtx.GTXDeviceInfo;
import cn.memobird.gtx.common.GTXKey;
import cn.memobird.gtx.listener.OnCodeListener;
import cn.memobird.gtx.listener.OnDeviceInfoListener;
//import cn.memobird.gtxprintdemo.util.Common;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {

    private Context mContext;
    private Spinner spinnerLed, spinnerPrintDeep, spinnerAutoShutdown, spinerSize;
    private TextView tvSize;

    private ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

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

        findViewById();
        setListener();
        init();
    }


    private void init() {
        GTX.getDeviceInfo(new OnDeviceInfoListener() {
            @Override
            public void returnResult(int i, GTXDeviceInfo gtxDeviceInfo) {
                if (gtxDeviceInfo != null) {
                    int shutDwon = resolveBluetoothAutoPower(gtxDeviceInfo.getShutDown());
                    int deep = resolveBluetoothDeep(gtxDeviceInfo.getPrintDeep());
                    boolean isOpen = gtxDeviceInfo.isLedOpen();
                    if (isOpen)
                        spinnerLed.setSelection(0);//开启
                    else spinnerLed.setSelection(1);//关闭
                    spinnerPrintDeep.setSelection(deep);
                    spinnerAutoShutdown.setSelection(shutDwon);
                } else {
                    Common.showShortToast(getBaseContext(), "获取设备信息失败");
                }
            }
        });
        if (GTX.getConnectDevice().getName().contains("G4")) {
            Common.DEFAULT_IMAGE_WIDTH = Common.SIZE_576;
        } else Common.DEFAULT_IMAGE_WIDTH = Common.SIZE_384;

        tvSize.setText(Common.DEFAULT_IMAGE_WIDTH + "px");
      /*  if (Common.DEFAULT_IMAGE_WIDTH==Common.SIZE_384)
            spinerSize.setSelection(0);
        else spinerSize.setSelection(1);*/
    }

    private void setListener() {

        spinnerLed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                GTX.setDeviceLed(onSettingListener, position == 0, new Dialog(mContext));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerPrintDeep.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                GTX.setPrintDeep(onSettingListener, getPrintDeepValue(position), new Dialog(mContext));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerAutoShutdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                GTX.setAutoShutdown(onSettingListener, getAutoShutdownValue(position), new Dialog(mContext));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinerSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinerSize.getSelectedItemPosition() == 0)
                    Common.DEFAULT_IMAGE_WIDTH = Common.SIZE_384;
                else Common.DEFAULT_IMAGE_WIDTH = Common.SIZE_576;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void findViewById() {
        mContext = this;
        spinnerLed = findViewById(R.id.spiner_led);
        spinnerPrintDeep = findViewById(R.id.spiner_print_deep);
        spinnerAutoShutdown = findViewById(R.id.spiner_auto_shutdonw);
        spinerSize = findViewById(R.id.spiner_size);
        tvSize = findViewById(R.id.tv_size);
    }

    private OnCodeListener onSettingListener = new OnCodeListener() {
        @Override
        public void returnResult(int i) {
            if (i == GTXKey.RESULT.COMMON_SUCCESS) {
                Common.showShortToast(mContext, "设置成功");
            } else if (i == GTXKey.RESULT.COMMON_FAIL) {
                Common.showShortToast(mContext, "设置失败");
            } else {
                Common.showShortToast(mContext, "其它错误" + i);
            }
        }
    };

    public int resolveBluetoothAutoPower(int m) {
        int str = 0;
        switch (m) {
            case GTXKey.SETTING.VALUE_SHUTDOWN_TIME_10:
                str = 0;
                break;
            case GTXKey.SETTING.VALUE_SHUTDOWN_TIME_30:
                str = 1;
                break;
            case GTXKey.SETTING.VALUE_SHUTDOWN_TIME_60:
                str = 2;
                break;
            case GTXKey.SETTING.VALUE_SHUTDOWN_TIME_120:
                str = 3;
                break;
            case GTXKey.SETTING.VALUE_SHUTDOWN_TIME_180:
                str = 4;
                break;
            case GTXKey.SETTING.VALUE_SHUTDOWN_TIME_240:
                str = 5;
                break;
        }
        return str;
    }

    public int resolveBluetoothDeep(int deep) {
        int str = 0;
        switch (deep) {
            case GTXKey.SETTING.VALUE_DEEP_LIGHT:
                str = 0;
                break;
            case GTXKey.SETTING.VALUE_DEEP_MODERATE:
                str = 1;
                break;
            case GTXKey.SETTING.VALUE_DEEP_DARK:
                str = 2;
                break;
        }
        return str;
    }

    private int getPrintDeepValue(int position) {
        int value = GTXKey.SETTING.VALUE_DEEP_MODERATE;
        if (position == 0) {
            value = GTXKey.SETTING.VALUE_DEEP_LIGHT;
        } else if (position == 1) {
            value = GTXKey.SETTING.VALUE_DEEP_MODERATE;
        } else if (position == 2) {
            value = GTXKey.SETTING.VALUE_DEEP_DARK;
        }
        return value;
    }

    private int getAutoShutdownValue(int position) {
        int value = GTXKey.SETTING.VALUE_SHUTDOWN_TIME_10;
        switch (position) {
            case 0:
                value = GTXKey.SETTING.VALUE_SHUTDOWN_TIME_10;
                break;
            case 1:
                value = GTXKey.SETTING.VALUE_SHUTDOWN_TIME_30;
                break;
            case 2:
                value = GTXKey.SETTING.VALUE_SHUTDOWN_TIME_60;
                break;
            case 3:
                value = GTXKey.SETTING.VALUE_SHUTDOWN_TIME_120;
                break;
            case 4:
                value = GTXKey.SETTING.VALUE_SHUTDOWN_TIME_180;
                break;
            case 5:
                value = GTXKey.SETTING.VALUE_SHUTDOWN_TIME_240;
                break;
        }
        return value;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GTX.onDestroy();
    }
}
