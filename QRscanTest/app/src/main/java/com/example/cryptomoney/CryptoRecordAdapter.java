package com.example.cryptomoney;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cryptomoney.utils.Base64Utils;
import com.example.cryptomoney.utils.RSAUtils;

import org.w3c.dom.Text;

import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.List;

public class CryptoRecordAdapter extends RecyclerView.Adapter<CryptoRecordAdapter.ViewHolder> {

    private List<CryptoRecord> mRecordList;
    private String merchant;
    private Integer account_id;
    private String token_enc;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View recordView;
        TextView index;
        TextView value;
        TextView address;
        TextView time;

        public ViewHolder(View view) {
            super(view);
            recordView = view;
            index = (TextView) view.findViewById(R.id.index);
            address = (TextView) view.findViewById(R.id.address);
            value = (TextView) view.findViewById(R.id.value);
            time = (TextView) view.findViewById(R.id.time);
        }
    }

    public CryptoRecordAdapter(List<CryptoRecord> recordList) {
        mRecordList = recordList;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cryptorecord_item,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        holder.recordView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                CryptoRecord record = mRecordList.get(position);
                Intent intent = new Intent(view.getContext(),CryptoModeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intent.putExtra("amount",record.getValue());
                intent.putExtra("address",record.getAddr());
                intent.putExtra("merchant",merchant);

                if (!merchant.equals("None")) {
                    final String encryptRequest = "request=" + URLEncoder.encode("onlyencrypt") +"&merchant="+ URLEncoder.encode(merchant)+
                            "&addr="+ URLEncoder.encode(record.getAddr())+ "&value="+ URLEncoder.encode(record.getValue().toString());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final String serverresponse = PostService.Post(encryptRequest);
                            if  (serverresponse != null && serverresponse.indexOf("token=") == 0) {
                                String enc_str = serverresponse.split("&enc=")[1];
//                                setEnc(enc_str);
                                System.out.println("enc="+enc_str);

                                Message msg = new Message();
                                msg.obj = enc_str;
                                handler.sendMessage(msg);
                            }
                        }
                    }).start();
//                    System.out.println("cccccenc="+getEnc());
                    intent.putExtra("enc",token_enc);
                }

                Log.d("CryptoRecordAdapter","amount= "+record.getValue());
                Log.d("CryptoRecordAdapter","address= "+record.getAddr());
                view.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
//            System.out.println("aaaaaaa "+msg.obj);
//            enc = (String)msg.obj;
            setEnc((String)msg.obj);
            super.handleMessage(msg);
        }
    };


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CryptoRecord record = mRecordList.get(position);
        holder.index.setText(record.getIndex().toString());
        holder.address.setText(record.getAddr());
        holder.value.setText(record.getValue().toString());
        holder.time.setText(record.getTime());
    }

    @Override
    public int getItemCount() {
        return mRecordList.size();
    }

    public void setmerchant(String merchant) {
        this.merchant = merchant;
//        System.out.println("merchant="+merchant);
    }

    public void setid(Integer id) {
        this.account_id=id;
        System.out.println("account_id="+this.account_id);
    }

    public void setEnc(String enc) {
        this.token_enc = enc;
    }

    public String getEnc() {
        return this.token_enc;
    }

}
