package com.example.urplight2;

import android.bluetooth.BluetoothDevice;
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
    ArrayList<BluetoothDevice> device;

    public MyAdapter(Context context, ArrayList<BluetoothDevice> data){
        mContext = context;
        device = data;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return device.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return device.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.list_device, null);
        String DEVICE_NAME = device.get(position).getName();
        String DEVICE_ADDRESS = device.get(position).getAddress();

        if(DEVICE_NAME == null){
            DEVICE_NAME = "null";
        }

        TextView textName = (TextView)view.findViewById(R.id.device_name);
        TextView textAddress = (TextView)view.findViewById(R.id.device_address);

        textName.setText(DEVICE_NAME);
        textAddress.setText(DEVICE_ADDRESS);

        return view;
    }

    public void clear() {
        device.clear();
    }
}
