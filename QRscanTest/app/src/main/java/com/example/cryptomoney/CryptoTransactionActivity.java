package com.example.cryptomoney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.nfc.FormatException;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cryptomoney.utils.DBHelper;
import com.example.cryptomoney.utils.ImgUtils;
import com.example.cryptomoney.utils.NfcUtils;
import com.example.cryptomoney.utils.StringBitmapParameter;
import com.example.cryptomoney.utils.WriteDialog;
import com.google.zxing.util.QrCodeGenerator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.memobird.gtx.GTX;
import cn.memobird.gtx.GTXScripElement;
import cn.memobird.gtx.common.GTXKey;
import cn.memobird.gtx.listener.OnBluetoothFindListener;
import cn.memobird.gtx.listener.OnCodeListener;
import cn.memobird.gtx.listener.OnImageToDitherListener;

import static com.example.cryptomoney.utils.BitmapUtil.StringListtoBitmap;

public class CryptoTransactionActivity extends AppCompatActivity implements WriteDialog.MsgListener {

    private List<CryptoRecord> recordList;
    private SwipeRefreshLayout swipeRefresh;
    private CryptoRecordAdapter adapter;
    private Integer account_id;
    private RecyclerView recyclerView;
//    private Spinner spinner = null;
//    private ArrayAdapter<String> merchantlistAdapter = null;
//    private String[] merchantList;
//    private String merchant_selected;
    private String type;
    private RadioGroup modegroup;
    private String printMode;
    private Button request;
    private Button reset;
    private Dialog mDialog;
    private Bitmap qrimage;
    private String base64ImageString;
    private List<BluetoothDevice> devices = new ArrayList<>();
    private String text;
    private String qrtext;
    private String nfctext;
    private NfcUtils nfcUtils;
    private DialogFragment NFCDialog;
    private Boolean showdialog = false;
    private Bitmap finalbitmap;
    private ImageView qrimg;
    private String addr;
    private String enc;
    private String value;
//    private SharedPreferences pref;
    private String sk_exp;
    private String modulus;
    private static final int REQUEST_CODE_SAVE_IMG = 3 ;
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private ContentValues values;

    private final static int REQ_LOC = 10;
    final String AK = "c6a5a445dc25490183f42088f4b78ccf";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto_transaction);

        GTX.init(getApplicationContext(), AK);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)  {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
//        spinner = (Spinner)findViewById(R.id.spinner);
        mDialog = Common.showLoadingDialog(CryptoTransactionActivity.this);

        dbHelper = new DBHelper(CryptoTransactionActivity.this, "test.db", null, 3);
        db = dbHelper.getWritableDatabase();
        values = new ContentValues();

        recordList =  (List<CryptoRecord>) getIntent().getSerializableExtra("recordList");
        account_id = getIntent().getIntExtra("account_id",0);
        type = getIntent().getStringExtra("type");
        if (recordList == null) return;
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new CryptoRecordAdapter(recordList);
        recyclerView.setAdapter(adapter);
        adapter.setid(account_id);
        request = (Button) findViewById(R.id.request);
        reset = findViewById(R.id.reset);
        qrimg = findViewById(R.id.qrimg);
        qrimg.setVisibility(View.GONE);
        nfcUtils = new NfcUtils(this);
        modegroup = findViewById(R.id.modegroup);

        if (GTX.getConnectDevice() == null) {
            if (ContextCompat.checkSelfPermission(CryptoTransactionActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)  // 检查运行时权限
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CryptoTransactionActivity.this,
                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOC);  // 申请定位权限,MUST BE FINE!
            }else {
                GTX.searchBluetoothDevices(onBluetoothFindListener,mDialog);
            }
        }

        printMode = "QR";

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


//        final String merchantlistRequest ="request=" + URLEncoder.encode("merchantlist");
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
////                final String response = PostService.Post(merchantlistRequest);
////                System.out.println(response)
//                if (response != null) {
//                    try {
//                        List<String> array = new ArrayList<>();
//                        JSONArray jsonArray = new JSONArray(response);
//                        for (int i = 0; i < jsonArray.length(); i++) {
//                            JSONObject jsonObject = jsonArray.getJSONObject(i);
//                            array.add(jsonObject.getString("username"));
//                        }
////                        merchantList = new String[array.size()+1];
////                        merchantList[0] = "None";
////                        for (int i=1;i<array.size()+1;i++) {
////                            merchantList[i] = array.get(i-1);
////                        }
////                        System.out.println(merchantList);
////                        runOnUiThread(new Runnable() {
////                            @Override
////                            public void run() {
//////                                merchantlistAdapter = new ArrayAdapter<String>(CryptoTransactionActivity.this,android.R.layout.simple_spinner_item,merchantList);
//////                                merchantlistAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//////                                spinner.setAdapter(merchantlistAdapter);
////
//////                                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//////                                    @Override
//////                                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//////                                        merchant_selected = (String) ((TextView)view).getText();
//////                                        System.out.println(merchant_selected);
//////                                        adapter.setmerchant(merchant_selected);
//////                                    }
//////
//////                                    @Override
//////                                    public void onNothingSelected(AdapterView<?> adapterView) {
//////                                    }
//////                                });
////                            }
////                        });
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresher);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshcryptotransaction();
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qrimg.setVisibility(View.GONE);
                modegroup.check(R.id.QR);
                printMode = "QR";
                adapter.setSelectedIndex(-1);
                adapter.notifyDataSetChanged();
//                recordadapter.setSelectedIndex(-1);
            }
        });

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                a past record selected
                if (adapter.getselected()) {
//                    System.out.println("choose a past record");
                    addr = adapter.getaddr();
                    value = adapter.getvalue();
//                    pref = getSharedPreferences("cryptomoneyAPP", Context.MODE_PRIVATE);
//                    sk_exp = pref.getString(addr + "_skexp", "");
//                    modulus = pref.getString(addr + "_modulus", "");
                    String[] str = {addr};
                    Cursor cursor = db.query("TokenSk",null,"addr=?",str,null,null,null);
                    if (cursor.moveToFirst()) {
                        do {
                            modulus = cursor.getString(cursor.getColumnIndex("N"));
                            sk_exp = cursor.getString(cursor.getColumnIndex("sk_exp"));
                        }while(cursor.moveToNext());
                    }
                    cursor.close();

                    text = "N=" + modulus + "&d=" + sk_exp + "&addr=" + addr;
                    qrimage = QrCodeGenerator.getQrCodeImage(text, 200, 200);

                    // convert string to bitmap
                    StringBitmapParameter valuebitpic = new StringBitmapParameter(value);
                    StringBitmapParameter merchantpic = new StringBitmapParameter(" ");
                    ArrayList<StringBitmapParameter> tobitmap = new ArrayList<StringBitmapParameter>();
                    tobitmap.add(valuebitpic);
                    tobitmap.add(merchantpic);
                    Bitmap textbitmap = StringListtoBitmap(CryptoTransactionActivity.this,tobitmap);

//                        requestPermission();

                    if (printMode.equals("QR")) {
                        if (GTX.getConnectDevice() != null) {

                            // merge bitmaps
                            finalbitmap = mergeBitmap_TB(textbitmap,qrimage,true);
                            printQR();
                        } else {
                            Common.showShortToast(CryptoTransactionActivity.this, "No printer found, display QR directly");
                            qrimg.setVisibility(View.VISIBLE);
                            qrimg.setImageBitmap(qrimage);
                            finalbitmap = mergeBitmap_TB(textbitmap,qrimage,true);

                        }
                        // save to album
                        if (ContextCompat.checkSelfPermission(CryptoTransactionActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)  // 检查运行时权限
                                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(CryptoTransactionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)  // 检查运行时权限
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(CryptoTransactionActivity.this,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_SAVE_IMG);  // 申请定位权限,MUST BE FINE!
                        }else {
                            saveImage();
                        }

                    } else if (printMode.equals("QRNFC")) {
                        qrtext = extractodd(text);
                        nfctext = extracteven(text);
                        if (GTX.getConnectDevice() != null) {
                            qrimage = QrCodeGenerator.getQrCodeImage(qrtext, 200, 200);
                            // merge bitmaps
                            finalbitmap = mergeBitmap_TB(textbitmap,qrimage,true);
                            printQR();
                        } else {
                            Common.showShortToast(CryptoTransactionActivity.this, "No printer found, display QR directly");
                            qrimage = QrCodeGenerator.getQrCodeImage(qrtext, 200, 200);
                            qrimg.setVisibility(View.VISIBLE);
                            qrimg.setImageBitmap(qrimage);
                            finalbitmap = mergeBitmap_TB(textbitmap,qrimage,true);
                        }
                        showdialog = true;
                        showSaveDialog();
                        // save to album
                        if (ContextCompat.checkSelfPermission(CryptoTransactionActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)  // 检查运行时权限
                                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(CryptoTransactionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)  // 检查运行时权限
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(CryptoTransactionActivity.this,
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_SAVE_IMG);  // 申请定位权限,MUST BE FINE!
                        }else {
                            saveImage();
                        }
                    } else if (printMode.equals("NFC")) {
                        nfctext = text;
                        showdialog = true;
                        showSaveDialog();
                    }

//                        recordadapter.setSelectedIndex(-1);
//                        amount.setText("");
//                        amountgroup.clearCheck();

//                    } else {
//                        final String encryptRequest = "request=" + URLEncoder.encode("onlyencrypt") + "&merchant=" + URLEncoder.encode(merchant_selected) +
//                                "&addr=" + URLEncoder.encode(addr) + "&value=" + URLEncoder.encode(value);
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                final String serverresponse = PostService.Post(encryptRequest);
//                                if (serverresponse != null && serverresponse.indexOf("token=") == 0) {
//                                    enc = serverresponse.split("&enc=")[1];
//                                    text = enc;
//                                    qrimage = QrCodeGenerator.getQrCodeImage(text, 200, 200);
//                                }
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        // convert string to bitmap
//                                        StringBitmapParameter valuebitpic = new StringBitmapParameter(value);
//                                        StringBitmapParameter merchantpic = new StringBitmapParameter(merchant_selected);
//                                        ArrayList<StringBitmapParameter> tobitmap = new ArrayList<StringBitmapParameter>();
//                                        tobitmap.add(valuebitpic);
//                                        tobitmap.add(merchantpic);
//                                        Bitmap textbitmap = StringListtoBitmap(CryptoTransactionActivity.this,tobitmap);
//
//                                        // merge bitmaps
//                                        finalbitmap = mergeBitmap_TB(textbitmap,qrimage,true);
//
//                                        // save to album
//                                        if (ContextCompat.checkSelfPermission(CryptoTransactionActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)  // 检查运行时权限
//                                                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(CryptoTransactionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)  // 检查运行时权限
//                                                != PackageManager.PERMISSION_GRANTED) {
//                                            ActivityCompat.requestPermissions(CryptoTransactionActivity.this,
//                                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_SAVE_IMG);  // 申请定位权限,MUST BE FINE!
//                                        }else {
//                                            saveImage();
//                                        }
//
//                                        if (printMode.equals("QR")) {
//                                            if (GTX.getConnectDevice() != null) {
//                                                printQR();
//                                            } else {
//                                                Common.showShortToast(CryptoTransactionActivity.this, "No printer found, display QR directly");
////                                                recyclerView.setVisibility(View.GONE);
//                                                qrimg.setVisibility(View.VISIBLE);
//                                                qrimg.setImageBitmap(qrimage);
//
//                                            }
//                                        } else if (printMode.equals("QRNFC")) {
//                                            qrtext = extractodd(text);
//                                            nfctext = extracteven(text);
//                                            if (GTX.getConnectDevice() != null) {
//                                                qrimage = QrCodeGenerator.getQrCodeImage(qrtext, 200, 200);
//                                                printQR();
//                                            } else {
//                                                Common.showShortToast(CryptoTransactionActivity.this, "No printer found, display QR directly");
//                                                qrimg.setVisibility(View.VISIBLE);
//                                                qrimg.setImageBitmap(qrimage);
//                                            }
//                                            showdialog = true;
//                                            showSaveDialog();
//                                        } else if (printMode.equals("NFC")) {
//                                            nfctext = text;
//                                            showdialog = true;
//                                            showSaveDialog();
//                                        }
////                                        recordadapter.setSelectedIndex(-1);
////                                        amount.setText("");
////                                        amountgroup.clearCheck();
//                                    }
//                                });
//                            }
//                        }).start();


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

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        if (showdialog) {
            try {
                nfcUtils.writeNFCToTag(nfctext, intent);
                dissDialog();
                showdialog = false;
                //                    Log.d("NFCRWActivity","finish writing");
                Common.showShortToast(this, "write to NFC successfully.");
//            nfcstatus.setText("Status: writing successfully");
            } catch (IOException e) {
                Common.showShortToast(this, "write to NFC failed.");
                //                    Log.d("NFCRWActivity","write to NFC failed：" + e.getMessage());
            } catch (FormatException e) {
                Common.showShortToast(this, "write to failed.");
                //                    Log.d("NFCRWActivity","write to NFC failed：" + e.getMessage());
            }
        }

    }


    public void refreshcryptotransaction() {
        adapter.setSelectedIndex(-1);
        final String cryptotransactionRequest ="request=" + URLEncoder.encode("cryptotransaction") + "&id="+ URLEncoder.encode(account_id.toString());

        new Thread(new Runnable() {
            @Override
            public void run() {
                final String response = PostService.Post(cryptotransactionRequest);
                if (response != null) {

                    try {
                        Thread.sleep(1500);
//                        List<Record> recordList = new ArrayList<>();
                        recordList.clear();
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            CryptoRecord record = new CryptoRecord();
                            record.setIndex(jsonObject.getInt("index"));
                            record.setAddr(jsonObject.getString("address"));
                            record.setTime(jsonObject.getString( "time"));
                            record.setValue(jsonObject.getDouble("value"));
                            recordList.add(record);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (recordList == null) return;
                            adapter.notifyDataSetChanged();
                            swipeRefresh.setRefreshing(false);
                        }
                    });
                }
            }
        }).start();
    }

    private void printQR() {
        GTX.doImageToDither(onDitherListener,finalbitmap,mDialog, Common.DEFAULT_IMAGE_WIDTH,true);
//        GTX.doImageToDither(onDitherListener,qrimage,mDialog, Common.DEFAULT_IMAGE_WIDTH,true);
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
//                    scripElements.add(new GTXScripElement(1, "mode= "+printMode));

//                    GTXScripElement elementvalue = new GTXScripElement(1, value);
//                    elementvalue.setBold(true);
//                    elementvalue.setFontSize(5);
//                    scripElements.add(elementvalue);
//                    GTXScripElement elementmerchant = new GTXScripElement(1, "merchant: "+merchant_selected);
//                    elementmerchant.setBold(true);
//                    elementmerchant.setFontSize(5);
//                    scripElements.add(elementmerchant);
//                    scripElements.add(new GTXScripElement(1, "to merchant: "+merchant_selected))
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
                CryptoTransactionActivity.this.base64ImageString = base64ImageString;
            } else {
//                Log.d("QRgeneratorActivity","Dither failed");
                Common.showShortToast(CryptoTransactionActivity.this, "Error " + taskCode);
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
        else if (requestCode == REQUEST_CODE_SAVE_IMG && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            saveImage();
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
                    Common.showShortToast(CryptoTransactionActivity.this, "No MEMOBIRD device available");
                } else {
//                    Common.showShortToast(CryptoActivity.this, "device searching finished");
                }
            } else {                                                            //其它异常
                Common.showShortToast(CryptoTransactionActivity.this, "Error " + taskCode);
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
                Common.showShortToast(CryptoTransactionActivity.this,"connected to a printer successfully");
                if (GTX.getConnectDevice().getName().contains("G4")) {
                    Common.DEFAULT_IMAGE_WIDTH = Common.SIZE_576;
                } else {
                    Common.DEFAULT_IMAGE_WIDTH = Common.SIZE_384;
                }
            } else if (taskCode == GTXKey.RESULT.COMMON_FAIL) {
                Common.showShortToast(CryptoTransactionActivity.this, "connection failed");
            } else {
                Common.showShortToast(CryptoTransactionActivity.this, "Error " + taskCode);
            }
        }
    };

    // 打印结果监听
    private OnCodeListener onPrintListener = new OnCodeListener() {
        @Override
        public void returnResult(int taskCode) {
            if (taskCode == GTXKey.RESULT.COMMON_SUCCESS) {
//                Common.showShortToast(CryptoActivity.this, "successfully printed");
            } else {
                Log.d("QRgeneratorActivity","taskCode="+taskCode);
                Common.showShortToast(CryptoTransactionActivity.this, "Error " + taskCode);
            }
        }
    };



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

    private void dissDialog() {
        if (NFCDialog != null &&
                NFCDialog.getDialog() != null &&
                NFCDialog.getDialog().isShowing()) {
            NFCDialog.dismiss();
        }
    }

    private void showSaveDialog() {
        NFCDialog = new WriteDialog();
        NFCDialog.show(getSupportFragmentManager(), "mWriteDialog");
    }

    public static Bitmap mergeBitmap_TB(Bitmap topBitmap, Bitmap bottomBitmap, boolean isBaseMax) {

        if (topBitmap == null || topBitmap.isRecycled()
                || bottomBitmap == null || bottomBitmap.isRecycled()) {
//            JDLog.logError(TAG, "topBitmap=" + topBitmap + ";bottomBitmap=" + bottomBitmap);
            return null;
        }
        int width = 0;
        if (isBaseMax) {
            width = topBitmap.getWidth() > bottomBitmap.getWidth() ? topBitmap.getWidth() : bottomBitmap.getWidth();
        } else {
            width = topBitmap.getWidth() < bottomBitmap.getWidth() ? topBitmap.getWidth() : bottomBitmap.getWidth();
        }
        Bitmap tempBitmapT = topBitmap;
        Bitmap tempBitmapB = bottomBitmap;

        if (topBitmap.getWidth() != width) {
            tempBitmapT = Bitmap.createScaledBitmap(topBitmap, width, (int)(topBitmap.getHeight()*1f/topBitmap.getWidth()*width), false);
        } else if (bottomBitmap.getWidth() != width) {
            tempBitmapB = Bitmap.createScaledBitmap(bottomBitmap, width, (int)(bottomBitmap.getHeight()*1f/bottomBitmap.getWidth()*width), false);
        }

        int height = tempBitmapT.getHeight() + tempBitmapB.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Rect topRect = new Rect(0, 0, tempBitmapT.getWidth(), tempBitmapT.getHeight());
        Rect bottomRect  = new Rect(0, 0, tempBitmapB.getWidth(), tempBitmapB.getHeight());

        Rect bottomRectT  = new Rect(0, tempBitmapT.getHeight(), width, height);

        canvas.drawBitmap(tempBitmapT, topRect, topRect, null);
        canvas.drawBitmap(tempBitmapB, bottomRect, bottomRectT, null);
        return bitmap;
    }


    //保存图片
    private void saveImage() {
        Bitmap bitmap = finalbitmap;
        boolean isSaveSuccess = ImgUtils.saveImageToGallery(CryptoTransactionActivity.this, bitmap);
        if (isSaveSuccess) {
            Toast.makeText(CryptoTransactionActivity.this, "save to system album successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(CryptoTransactionActivity.this, "save to system album failed", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void cancelresult(Boolean iscancel) {
        if (iscancel) {
        }
    }
}