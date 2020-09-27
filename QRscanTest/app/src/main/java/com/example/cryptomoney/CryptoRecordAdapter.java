package com.example.cryptomoney;

import android.content.Intent;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.List;

public class CryptoRecordAdapter extends RecyclerView.Adapter<CryptoRecordAdapter.ViewHolder> {

    private List<CryptoRecord> mRecordList;

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
                Intent intent = new Intent(view.getContext(),QRgeneratorActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                intent.putExtra("amount",record.getValue());
                intent.putExtra("address",record.getAddr());
                Log.d("CryptoRecordAdapter","amount= "+record.getValue());
                Log.d("CryptoRecordAdapter","address= "+record.getAddr());
                view.getContext().startActivity(intent);
            }
        });
        return holder;
    }

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
}
