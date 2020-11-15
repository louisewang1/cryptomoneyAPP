package com.example.cryptomoney.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.cryptomoney.R;
import com.google.zxing.util.BackDialog;
import com.google.zxing.util.Base64Utils;
import com.google.zxing.util.PostService;

import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.RSAPrivateKeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;


public class ReadDialog extends DialogFragment {

    //    private EditText etWrite;
//    private Button btnCancle;
//    private MsgListener msgListener;
    private Button btnCancle;
    private EditText new_amount;
    private Button change_amount;
    private MsgListener msgListener;
    private String amount;
    private String contract_addr;
    private String contract_sk_exp;
    private String contract_modulus;
    private String enc_cancel;
    private String enc_amount;
    private  Cipher cipher;
    private PrivateKey contract_sk;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_MinWidth);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_nfc_read_dialog, container);
//        etWrite = view.findViewById(R.id.et_write_nfc);
        btnCancle = view.findViewById(R.id.btn_write_nfc_cancle);
        change_amount = view.findViewById(R.id.change_amount);
        new_amount = view.findViewById(com.google.zxing.R.id.new_amount);
//        btnCancle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dismiss();
//                //todo: send cancel message to server
//            }
//        });
//        etWrite.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                if (msgListener != null) {
//                    msgListener.result(editable.toString());
//                }
//            }
//        });
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        msgListener = (ReadDialog.MsgListener) context;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        // get contract detail
//        System.out.println("bundle="+bundle);
        if (bundle != null) {
            contract_addr = bundle.getString("contract_addr");
            contract_modulus = bundle.getString("contract_modulus");
            contract_sk_exp = bundle.getString("contract_sk_exp");
//            System.out.println("conract addr in backdialog= "+contract_addr);
        }

        // restore contrack_sk
        BigInteger N = new BigInteger(com.google.zxing.util.Base64Utils.decode(contract_modulus));
        BigInteger d = new BigInteger(com.google.zxing.util.Base64Utils.decode(contract_sk_exp));

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(N, d);
            contract_sk = keyFactory.generatePrivate(rsaPrivateKeySpec);

            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, contract_sk);
            String plain_str = "CANCEL";
            byte[] enc_bytes = cipher.doFinal(plain_str.getBytes());
            enc_cancel = com.google.zxing.util.Base64Utils.encode(enc_bytes);

        } catch (Exception e) {
            e.printStackTrace();
        }

        change_amount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amount = new_amount.getText().toString();
                if (!isNumeric(amount)) {
                    Toast.makeText(getContext(), "invalid input", Toast.LENGTH_SHORT).show();
                }

                else {
                    try {
                        String plain_str = amount;
                        byte[] enc_bytes = cipher.doFinal(plain_str.getBytes());
                        enc_amount = Base64Utils.encode(enc_bytes);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    final String changeamountRequest = "request=" + URLEncoder.encode("changeamount") +
                            "&contract_addr=" + URLEncoder.encode(contract_addr) + "&enc=" + URLEncoder.encode(enc_amount);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String response = PostService.Post(changeamountRequest);
                            System.out.println("changeamountresponse= "+response);
                            if (response != null) {
                                if (response.equals("amount changed")) {
                                    msgListener.changeamount(true,amount);
//                                    dismiss();
                                    Looper.prepare();
                                    Handler handler = new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            Toast.makeText(getContext(), "amount changed", Toast.LENGTH_SHORT).show();
                                        }
                                    };
                                    handler.sendEmptyMessage(0);
                                    Looper.loop();
                                }

                                else if (response.equals("enough for new amount")) {
                                    System.out.println("enough in backdialog");
                                    msgListener.finishresult(true);
                                    dismiss();
                                    Looper.prepare();
                                    Handler handler = new Handler() {
                                        @Override
                                        public void handleMessage(Message msg) {
                                            Toast.makeText(getContext(), amount+" received  successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    };
                                    handler.sendEmptyMessage(0);
                                    Looper.loop();
                                }
                            }
                        }
                    }).start();
                }
            }
        });

        btnCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String cancelcontractRequest = "request=" + URLEncoder.encode("cancelcontract") +
                        "&contract_addr=" + URLEncoder.encode(contract_addr) + "&enc=" + URLEncoder.encode(enc_cancel);

//                System.out.println(cancelcontractRequest);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String response = PostService.Post(cancelcontractRequest);
                        System.out.println("cancelresponse= "+response);
                        if (response != null) {
                            if (response.equals("cancelled")) {
                                msgListener.cancelresult(true);
                                dismiss();
                                Looper.prepare();
                                Handler handler = new Handler() {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        Toast.makeText(getContext(), "contract cancelled", Toast.LENGTH_SHORT).show();
                                    }
                                };
                                handler.sendEmptyMessage(0);
                                Looper.loop();
                            }
                        }
                    }
                }).start();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        msgListener.dissMiss();
//        msgListener=null;
    }

    /**
     * 消息接口
     */
//    public interface MsgListener {
//        void result(String msg);
//
//        void dissMiss();
//    }
    public interface MsgListener {
        //        void result(String msg);
        void cancelresult(Boolean iscancel);
        void finishresult(Boolean isfinish);
        void changeamount(Boolean ischange, String amount);
//        void dissMiss();
    }

    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]+[.]{0,1}[0-9]*[dD]{0,1}");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

}
