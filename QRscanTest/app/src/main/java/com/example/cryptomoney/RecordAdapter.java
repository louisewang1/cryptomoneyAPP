package com.example.cryptomoney;

import android.text.Layout;
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

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    private List<Record> mRecordList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView index;
        TextView from;
        TextView to ;
        TextView value;
        TextView time;

        public ViewHolder(View view) {
            super(view);
            index = (TextView) view.findViewById(R.id.index);
            from = (TextView) view.findViewById(R.id.from_account);
            to = (TextView) view.findViewById(R.id.to_account);
            value = (TextView) view.findViewById(R.id.value);
            time = (TextView) view.findViewById(R.id.time);
        }
    }

    public RecordAdapter(List<Record> recordList) {
        mRecordList = recordList;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Record record = mRecordList.get(position);
        holder.index.setText(record.getIndex().toString());
        holder.from.setText(record.getFrom().toString());
        holder.to.setText(record.getTo().toString());
        holder.value.setText(record.getValue().toString());
        holder.time.setText(record.getTime());
    }

    @Override
    public int getItemCount() {
        return mRecordList.size();
    }
}
