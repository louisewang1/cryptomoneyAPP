package com.example.cryptomoney;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

//import cn.memobird.gtxprintdemo.R;
//import com.example.cryptomoney.R;

public class DeviceListAdapter extends BaseAdapter {

	private Context mContext;
	List<BluetoothDevice> deviceList;


	public DeviceListAdapter(Context context, List<BluetoothDevice> deviceListIn) {
		this.mContext = context;
		this.deviceList = deviceListIn;
	}

	@Override
	public int getCount() {
		return this.deviceList.size();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.dialog_device_item_layout, null);
			holder = new ViewHolder();
			holder.tvDeviceName = (TextView) convertView
					.findViewById(R.id.tv_device_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.tvDeviceName.setText((deviceList.get(position).getName()) + ":\n" + deviceList.get(position).getAddress());
		return convertView;
	}

	class ViewHolder {
		TextView tvDeviceName;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

    public void setDeviceList(List<BluetoothDevice> deviceList) {
        this.deviceList = deviceList;
    }
}
