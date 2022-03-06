package org.techtown.urp.Function;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.techtown.urp.R;

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
        View view = mLayoutInflater.inflate(R.layout.device_item, null);
        String DEVICE_NAME = device.get(position).getName();
        String DEVICE_ADDRESS = device.get(position).getAddress();

        if(DEVICE_NAME == null){
            DEVICE_NAME = "null";
        }

        ImageView imageView = (ImageView)view.findViewById(R.id.imageView);
        TextView textName = (TextView)view.findViewById(R.id.tv_deviceName);
        TextView textAddress = (TextView)view.findViewById(R.id.tv_deviceAddress);

        imageView.setImageResource(R.drawable.ic_light);
        textName.setText(DEVICE_NAME);
        textAddress.setText(DEVICE_ADDRESS);

        return view;
    }

    public void clear() {
        device.clear();
    }
}