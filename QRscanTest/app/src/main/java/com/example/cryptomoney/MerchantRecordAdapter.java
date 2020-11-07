package com.example.cryptomoney;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MerchantRecordAdapter extends RecyclerView.Adapter<MerchantRecordAdapter.ViewHolder> {

    private List<CryptoRecord> mRecordList;
    private String merchant;
    private Integer account_id;
    private String token_enc;
    private int selectedIndex = -1;
    private boolean selected = false;
    private String value;
    private String ciphertext;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View recordView;
        TextView index;
        TextView value;
        TextView merchant;
        TextView time;

        public ViewHolder(View view) {
            super(view);
            recordView = view;
            index = (TextView) view.findViewById(R.id.index);
            merchant = (TextView) view.findViewById(R.id.merchant);
            value = (TextView) view.findViewById(R.id.value);
            time = (TextView) view.findViewById(R.id.time);
        }
    }

    public MerchantRecordAdapter(List<CryptoRecord> recordList) {
        mRecordList = recordList;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.merchantrecord_item,parent,false);
        final ViewHolder holder = new ViewHolder(view);
        holder.recordView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                CryptoRecord record = mRecordList.get(position);
//                System.out.println(record.getAddr());
                selectedIndex = position;
                merchant = record.getMerchant();
                ciphertext = record.getCiphertext();
                value = record.getValue().toString();
                selected = true;

//                System.out.println("selectedindex="+selectedIndex);
                notifyDataSetChanged();

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
        holder.merchant.setText(record.getMerchant());
        holder.value.setText(record.getValue().toString());
        holder.time.setText(record.getTime());

        if (position == selectedIndex) {
            holder.recordView.setBackgroundColor(Color.GRAY);
        }
        else {
            holder.recordView.setBackgroundColor(Color.WHITE);
        }

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

    public String getMerchant() {
        return merchant;
    }

//    public void setaddr(String addr) {
//        this.addr = addr;
//    }

    public boolean getselected() {
        return selected;
    }

    public String getvalue() {
        return value;
    }

    public String getCiphertext() {
        return ciphertext;
    }

    public void setSelectedIndex(int position) {

        selectedIndex = position;
        if (selectedIndex == -1) {
            selected = false;
        }
    }
}
