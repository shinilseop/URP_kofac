package com.example.kket_led_control_yunji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {
    Context mContext = null;
    LayoutInflater mLayoutInflater = null;
    ArrayList<DeviceData> device;

    public MyAdapter(Context context, ArrayList<DeviceData> data){
        mContext = context;
        device = data;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return device.size();
    }

    @Override
    public DeviceData getItem(int position) {
        return device.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.device_list, null);

        TextView textName = (TextView)view.findViewById(R.id.device_name);
        TextView textAddress = (TextView)view.findViewById(R.id.device_address);

        textName.setText(device.get(position).getName());
        textAddress.setText(device.get(position).getAddress());

        return view;
    }
}
