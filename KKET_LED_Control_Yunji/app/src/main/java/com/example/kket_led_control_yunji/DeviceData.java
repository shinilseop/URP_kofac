package com.example.kket_led_control_yunji;

import android.bluetooth.BluetoothDevice;

public class DeviceData {
    private String name;
    private String address;
    private BluetoothDevice device;

    public DeviceData(String name, String address, BluetoothDevice device){
        this.name = name;
        this.address = address;
        this.device = device;
    }

    public String getName(){
        return this.name;
    }

    public String getAddress(){
        return this.address;
    }

    public BluetoothDevice getDevice(){
        return this.device;
    }
}
