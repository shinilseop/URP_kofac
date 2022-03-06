package com.example.urp_led_contrller;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE = "DEVICE";

    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothLeService mBluetoothLeService;

    private Button btnCh1, btnCh2, btnCh3, btnCh4, btnNL;
    private ImageButton ibOnOff, ibBluetooth, ibConnServer;
    private String ch1Value, ch2Value, ch3Value, ch4Value;
    private ImageView ivConn;
    private TextView tvConn, tvName, tvAddress, tvNow;
    private boolean ledPower = false;
    private boolean bleConnect = true;
    private boolean isConn = false;
    private String SERIAL_NUMBER;

    //    private int ch1[],ch2[],ch3[],ch4[]
    private int ch1[] = {80, 40, 40, 40, 20, 20, 0, 20, 0, 20, 30, 0, 0, 20, 40, 20, 20, 50, 0, 30, 60, 30, 30, 50};
    private int ch2[] = {20, 70, 60, 40, 70, 0, 70, 20, 30, 0, 0, 50, 30, 40, 20, 20, 60, 0, 70, 60, 0, 100, 110, 80};
    private int ch3[] = {110, 90, 50, 50, 0, 140, 50, 80, 120, 100, 70, 70, 120, 30, 0, 80, 0, 30, 80, 30, 60, 0, 20, 70};
    private int ch4[] = {0, 20, 80, 100, 120, 50, 100, 100, 50, 90, 120, 90, 60, 130, 160, 100, 140, 170, 70, 110, 100, 90, 60, 20};
//    private int ch1[] = {50, 30, 60, 90, 50, 20, 30, 70, 30, 20, 40, 80, 40, 70, 30, 40, 60, 60, 50, 20, 80, 60, 40, 70, 30, 60, 50, 40, 70, 50, 40, 30, 70, 50, 60, 0, 30, 20, 60, 30, 50, 40, 20, 40, 30, 30, 40, 0, 0, 50, 80, 40, 60, 20, 30, 0, 20, 20, 20, 20, 40, 20, 20, 0, 20, 0, 30, 50, 40, 30, 0, 40, 30, 30, 30, 50, 20, 20, 0, 60, 30, 0, 20, 20, 30, 40, 20, 50, 0, 20, 40, 50, 20, 20, 0, 20, 0, 30, 0, 20, 30, 60, 20, 40, 30, 30, 20, 0, 0, 0, 20, 40, 0, 0, 0, 30, 0, 0, 0, 0, 30, 0, 40, 0, 30, 0, 0, 0, 0, 50, 20, 0, 30, 20, 20, 40, 0, 0, 40, 0, 0, 20, 30, 20, 20, 50, 0, 50, 30, 0, 0, 0, 20, 0, 0, 20, 50, 20, 0, 20, 20, 20, 30, 20, 40, 0, 0, 50, 0, 0, 40, 0, 0, 0, 40, 30, 20, 0, 20, 20, 0, 0, 20, 20, 0, 20, 20, 0, 30, 20, 20, 30, 0, 0, 30, 30, 0, 30, 0, 20, 0, 40, 20, 30, 20, 0, 0, 0, 0, 40, 0, 40, 0, 0, 0, 0, 40, 0, 0, 30, 40, 30, 40, 0, 0, 30, 0, 40, 0, 0, 40, 40, 0, 40, 40, 0, 20, 0, 0, 0, 0, 0, 0, 20, 20, 0, 0, 0, 0, 0, 0, 20, 0, 0, 20, 0, 0, 30, 30, 30, 20, 20, 0, 0, 0, 0, 0, 20, 20, 0, 0, 20, 0, 20, 0, 0, 20, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 30, 30, 30, 30, 0, 0, 0, 20, 20, 0, 0, 0, 0, 0, 30, 0, 0, 30, 30, 30, 30, 20, 30, 30, 0, 0, 30, 30, 0, 30, 30, 0, 0, 0, 0, 20, 0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 30, 0, 0, 0, 0, 0, 0, 0, 20, 30, 30, 20, 20, 30, 30, 20, 0, 0, 0, 0, 30, 30, 0, 30, 30, 30, 0, 20, 0, 20, 20, 0, 20, 30, 20, 0, 0, 0, 0, 20, 0, 0, 20, 0, 0, 0, 0, 20, 0, 20, 30, 0, 0, 20, 20, 0, 0, 20, 0, 0, 40, 20, 40, 0, 40, 40, 20, 0, 0, 0, 0, 20, 0, 0, 40, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 40, 0, 0, 0, 40, 20, 0, 20, 20, 0, 0, 0, 0, 20, 40, 20, 30, 30, 30, 20, 0, 20, 0, 30, 30, 0, 20, 30, 30, 20, 0, 20, 20, 0, 20, 30, 30, 20, 20, 0, 20, 20, 0, 0, 0, 20, 20, 20, 40, 0, 30, 20, 0, 40, 30, 30, 0, 20, 20, 20, 20, 20, 20, 0, 0, 20, 0, 0, 0, 50, 0, 40, 20, 20, 30, 0, 0, 20, 20, 20, 40, 20, 0, 20, 20, 40, 40, 20, 20, 40, 30, 20, 0, 0, 30, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 30, 0, 20, 0, 20, 20, 20, 30, 30, 0, 30, 50, 20, 0, 30, 20, 0, 30, 20, 20, 0, 0, 0, 30, 20, 20, 50, 40, 20, 30, 0, 30, 0, 0, 0, 20, 30, 30, 20, 20, 0, 40, 40, 20, 40, 50, 20, 30, 0, 30, 0, 20, 30, 0, 30, 0, 30, 0, 30, 40, 20, 30, 20, 30, 40, 40, 40, 40, 50, 40, 30, 20, 0, 40, 30, 20, 0, 50, 60, 30, 50, 70, 20, 40, 40, 40, 0, 50, 20, 50, 20, 20, 60, 30, 50, 30, 20, 0, 80, 0, 50, 20, 0, 60, 30, 40, 0, 50, 60, 50, 60, 60, 70, 30, 50, 60, 80, 70, 70, 60, 50, 40, 20, 50, 70, 60, 20, 30, 0, 30, 50};
//    private int ch2[] = {90, 120, 80, 20, 100, 140, 110, 50, 120, 130, 110, 20, 100, 50, 120, 100, 40, 50, 90, 120, 0, 70, 60, 30, 90, 60, 70, 70, 20, 30, 70, 110, 40, 70, 0, 130, 90, 100, 30, 100, 30, 90, 110, 40, 80, 80, 60, 130, 110, 60, 0, 30, 0, 100, 60, 120, 60, 60, 70, 70, 60, 50, 70, 90, 50, 120, 30, 50, 50, 60, 110, 40, 70, 70, 50, 20, 50, 50, 80, 20, 70, 60, 50, 50, 30, 60, 30, 30, 60, 50, 40, 0, 70, 40, 80, 20, 60, 40, 80, 20, 40, 0, 60, 20, 20, 0, 40, 80, 90, 90, 0, 40, 60, 60, 100, 0, 40, 80, 80, 80, 20, 40, 40, 70, 30, 40, 80, 40, 70, 20, 30, 80, 0, 40, 0, 0, 50, 70, 20, 90, 40, 20, 30, 70, 0, 0, 30, 0, 30, 30, 30, 70, 40, 70, 20, 40, 0, 30, 50, 50, 30, 60, 0, 40, 0, 30, 30, 0, 50, 50, 30, 30, 30, 50, 30, 30, 20, 50, 20, 20, 50, 50, 20, 20, 50, 20, 60, 20, 40, 30, 60, 30, 70, 50, 20, 0, 20, 20, 20, 0, 60, 0, 0, 30, 50, 40, 30, 20, 0, 20, 50, 20, 20, 20, 20, 0, 20, 60, 60, 0, 20, 0, 20, 20, 20, 0, 60, 20, 60, 0, 20, 20, 60, 20, 20, 20, 30, 60, 60, 60, 30, 60, 30, 20, 30, 30, 50, 30, 70, 80, 80, 50, 70, 80, 30, 80, 40, 20, 20, 20, 50, 0, 20, 0, 20, 20, 0, 0, 40, 20, 20, 0, 20, 0, 20, 70, 0, 20, 20, 50, 20, 0, 70, 20, 20, 70, 70, 0, 0, 0, 0, 30, 30, 30, 0, 0, 70, 30, 30, 30, 30, 0, 30, 30, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 0, 0, 50, 30, 30, 30, 0, 0, 30, 30, 20, 0, 0, 50, 50, 50, 30, 30, 0, 30, 30, 30, 0, 30, 0, 20, 20, 20, 30, 30, 30, 0, 30, 30, 0, 0, 30, 0, 0, 0, 20, 20, 30, 30, 0, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 30, 30, 30, 30, 0, 0, 30, 0, 0, 0, 20, 20, 40, 20, 20, 40, 30, 20, 50, 40, 40, 70, 70, 20, 0, 0, 20, 0, 70, 70, 70, 30, 60, 50, 20, 70, 40, 20, 30, 60, 60, 30, 60, 50, 20, 20, 0, 60, 20, 20, 20, 50, 60, 0, 60, 20, 20, 50, 20, 20, 20, 60, 0, 0, 60, 0, 20, 60, 60, 0, 20, 60, 0, 20, 20, 20, 0, 0, 20, 20, 20, 20, 20, 20, 20, 40, 30, 30, 30, 30, 40, 40, 60, 0, 0, 20, 0, 20, 30, 50, 60, 50, 60, 20, 60, 40, 40, 60, 30, 40, 20, 0, 70, 80, 20, 0, 20, 20, 30, 30, 30, 40, 20, 0, 0, 0, 80, 60, 30, 50, 30, 30, 50, 20, 20, 40, 50, 40, 30, 0, 30, 30, 0, 0, 30, 30, 40, 50, 20, 0, 20, 40, 50, 40, 40, 20, 0, 50, 50, 0, 50, 50, 40, 30, 30, 20, 50, 50, 70, 50, 50, 50, 50, 80, 80, 100, 40, 40, 40, 40, 90, 30, 60, 30, 30, 40, 20, 30, 70, 30, 0, 30, 90, 60, 20, 50, 40, 50, 70, 60, 110, 80, 40, 40, 60, 0, 30, 30, 20, 70, 70, 90, 90, 80, 40, 20, 50, 20, 60, 100, 20, 0, 70, 30, 20, 90, 40, 70, 50, 80, 40, 40, 110, 30, 110, 70, 100, 80, 70, 70, 40, 90, 80, 40, 70, 60, 20, 30, 60, 50, 80, 120, 40, 60, 80, 120, 30, 30, 80, 70, 0, 90, 60, 80, 40, 140, 60, 90, 40, 100, 110, 40, 90, 30, 90, 100, 130, 30, 150, 70, 110, 140, 0, 110, 50, 140, 70, 50, 70, 50, 50, 30, 100, 50, 70, 20, 60, 20, 60, 60, 80, 130, 60, 40, 70, 120, 100, 150, 110, 80};
//    private int ch3[] = {40, 30, 60, 50, 20, 0, 60, 60, 60, 50, 0, 90, 40, 40, 30, 30, 100, 90, 40, 70, 80, 0, 100, 80, 70, 50, 70, 70, 100, 120, 90, 20, 20, 50, 130, 40, 40, 50, 90, 20, 110, 0, 0, 130, 40, 40, 70, 0, 70, 20, 20, 120, 110, 0, 100, 30, 130, 130, 110, 100, 50, 130, 80, 90, 140, 20, 130, 20, 40, 60, 30, 70, 40, 40, 70, 60, 100, 110, 90, 30, 20, 120, 90, 90, 100, 0, 140, 40, 130, 80, 50, 80, 40, 110, 90, 140, 130, 80, 80, 130, 60, 30, 60, 80, 110, 130, 80, 70, 20, 20, 160, 20, 110, 110, 0, 120, 150, 60, 50, 50, 90, 130, 0, 80, 60, 130, 50, 130, 80, 0, 100, 50, 100, 60, 140, 80, 120, 70, 30, 0, 140, 110, 60, 0, 140, 20, 150, 20, 50, 150, 150, 60, 60, 60, 150, 60, 30, 70, 110, 30, 70, 0, 90, 50, 50, 140, 140, 0, 100, 100, 0, 130, 130, 90, 0, 30, 80, 100, 80, 80, 90, 90, 80, 80, 90, 80, 0, 160, 0, 70, 0, 30, 40, 90, 50, 90, 130, 50, 130, 110, 70, 40, 110, 30, 20, 110, 130, 150, 170, 0, 80, 0, 150, 150, 150, 170, 0, 60, 60, 70, 0, 70, 0, 150, 150, 70, 60, 0, 60, 170, 0, 0, 60, 0, 0, 140, 50, 50, 50, 50, 120, 50, 120, 70, 50, 120, 80, 120, 20, 0, 0, 0, 20, 0, 50, 0, 90, 40, 40, 40, 0, 100, 130, 170, 130, 130, 170, 100, 20, 130, 130, 100, 130, 100, 130, 30, 100, 130, 130, 70, 130, 100, 30, 130, 130, 30, 30, 70, 70, 70, 70, 110, 110, 110, 100, 100, 30, 110, 110, 110, 110, 70, 110, 110, 70, 60, 60, 60, 90, 60, 60, 160, 160, 60, 60, 110, 60, 60, 70, 120, 120, 120, 90, 160, 120, 120, 30, 160, 160, 70, 70, 70, 120, 120, 160, 120, 120, 120, 160, 120, 160, 130, 130, 30, 120, 120, 120, 160, 120, 120, 160, 160, 120, 160, 160, 160, 30, 30, 120, 120, 160, 70, 160, 160, 160, 90, 60, 60, 90, 90, 60, 60, 90, 110, 110, 110, 110, 70, 70, 110, 70, 70, 70, 130, 70, 90, 70, 70, 90, 50, 40, 0, 90, 90, 20, 20, 70, 170, 170, 70, 170, 20, 20, 20, 50, 50, 0, 40, 20, 90, 70, 50, 50, 50, 50, 50, 80, 0, 70, 40, 60, 0, 0, 70, 80, 60, 170, 60, 70, 140, 80, 0, 70, 140, 60, 170, 170, 60, 170, 150, 60, 60, 70, 0, 60, 170, 150, 0, 80, 170, 100, 80, 150, 150, 150, 150, 80, 0, 30, 20, 20, 20, 50, 100, 40, 70, 90, 90, 130, 110, 50, 30, 0, 60, 0, 0, 160, 0, 0, 0, 0, 70, 120, 80, 120, 50, 20, 150, 120, 80, 80, 0, 140, 30, 50, 160, 50, 100, 100, 20, 0, 70, 30, 70, 80, 20, 150, 150, 60, 100, 130, 150, 20, 140, 0, 130, 130, 50, 160, 140, 40, 120, 150, 50, 70, 120, 70, 70, 40, 80, 40, 40, 80, 0, 40, 130, 150, 60, 90, 110, 120, 80, 120, 110, 130, 120, 50, 60, 0, 150, 150, 50, 50, 20, 120, 110, 120, 120, 80, 110, 80, 90, 90, 50, 130, 50, 30, 130, 140, 80, 80, 20, 130, 0, 90, 70, 110, 60, 80, 60, 140, 120, 120, 0, 70, 70, 90, 110, 140, 60, 150, 80, 50, 100, 130, 60, 80, 60, 0, 100, 120, 80, 100, 150, 90, 20, 140, 30, 50, 50, 30, 20, 90, 110, 40, 20, 100, 0, 50, 130, 90, 60, 120, 60, 30, 100, 80, 90, 50, 90, 40, 30, 0, 70, 50, 70, 30, 130, 0, 30, 90, 80, 60, 20, 40, 30, 110, 40, 60, 40, 0, 0, 50, 40, 30, 140, 20, 110, 40, 40, 70, 70, 80, 80, 80, 50, 90, 0, 40, 20, 100, 30, 90, 60, 20, 100, 50, 40, 70, 70, 40, 50, 70};
//    private int ch4[] = {30, 20, 20, 60, 40, 30, 0, 40, 0, 0, 50, 30, 30, 60, 30, 40, 0, 20, 50, 0, 50, 80, 0, 70, 20, 60, 40, 30, 40, 0, 20, 60, 90, 60, 0, 20, 50, 40, 50, 70, 30, 90, 70, 0, 60, 60, 50, 60, 0, 90, 110, 20, 40, 80, 30, 40, 0, 0, 20, 30, 80, 0, 40, 20, 0, 60, 20, 100, 90, 70, 60, 70, 90, 90, 70, 90, 40, 40, 20, 120, 100, 70, 50, 50, 50, 120, 20, 110, 60, 60, 100, 90, 90, 50, 40, 20, 0, 80, 50, 30, 90, 130, 90, 80, 60, 50, 70, 60, 140, 140, 20, 130, 40, 40, 100, 60, 0, 70, 70, 70, 80, 80, 140, 70, 100, 20, 80, 20, 70, 140, 70, 80, 70, 90, 40, 100, 40, 80, 120, 100, 30, 70, 110, 130, 50, 140, 20, 140, 110, 20, 20, 90, 100, 90, 0, 100, 170, 90, 50, 110, 90, 130, 90, 110, 120, 30, 30, 200, 60, 60, 150, 30, 30, 60, 150, 120, 90, 60, 90, 90, 60, 60, 90, 90, 60, 90, 140, 20, 140, 100, 140, 130, 100, 70, 120, 100, 100, 120, 100, 70, 90, 130, 70, 130, 130, 50, 40, 30, 80, 150, 70, 150, 30, 30, 30, 0, 160, 90, 90, 110, 150, 110, 150, 30, 30, 110, 90, 160, 90, 0, 160, 160, 90, 160, 160, 40, 120, 90, 90, 90, 50, 90, 50, 100, 120, 50, 80, 50, 110, 120, 120, 140, 110, 120, 120, 120, 80, 130, 130, 130, 140, 90, 40, 20, 40, 40, 20, 90, 130, 40, 40, 90, 40, 90, 40, 110, 90, 40, 40, 80, 40, 90, 110, 40, 40, 110, 110, 120, 120, 120, 120, 60, 60, 60, 90, 90, 110, 60, 60, 60, 60, 120, 60, 60, 120, 120, 120, 120, 90, 120, 120, 20, 20, 120, 120, 60, 120, 120, 90, 60, 60, 60, 90, 20, 60, 60, 140, 20, 20, 90, 90, 90, 60, 60, 20, 60, 60, 60, 20, 60, 20, 50, 50, 140, 60, 60, 60, 20, 60, 60, 20, 20, 60, 20, 20, 20, 140, 140, 60, 60, 20, 90, 20, 20, 20, 90, 120, 120, 90, 90, 120, 120, 90, 60, 60, 60, 60, 120, 120, 60, 120, 120, 120, 40, 110, 80, 110, 110, 80, 120, 130, 140, 80, 80, 110, 110, 110, 20, 20, 110, 20, 110, 110, 110, 120, 90, 140, 130, 110, 80, 110, 120, 90, 90, 120, 90, 80, 160, 100, 140, 90, 160, 160, 100, 80, 90, 0, 90, 100, 40, 80, 160, 100, 40, 90, 0, 0, 90, 0, 30, 90, 90, 110, 160, 90, 0, 30, 150, 100, 80, 80, 100, 30, 30, 30, 30, 100, 150, 120, 140, 140, 140, 110, 70, 120, 90, 100, 100, 100, 70, 120, 130, 130, 80, 130, 140, 20, 140, 140, 140, 140, 100, 50, 90, 70, 100, 110, 20, 70, 90, 90, 150, 30, 120, 110, 0, 120, 90, 90, 100, 130, 90, 110, 90, 90, 120, 0, 0, 100, 50, 30, 20, 140, 20, 140, 50, 50, 110, 0, 30, 110, 60, 40, 120, 90, 40, 90, 90, 120, 100, 100, 100, 100, 130, 100, 20, 70, 100, 80, 30, 30, 70, 30, 30, 30, 80, 70, 70, 100, 0, 0, 100, 100, 90, 50, 30, 50, 50, 70, 60, 80, 40, 70, 100, 40, 70, 110, 30, 0, 80, 70, 100, 0, 90, 40, 80, 50, 80, 90, 90, 20, 40, 20, 100, 50, 50, 30, 40, 30, 80, 0, 60, 50, 60, 40, 70, 70, 90, 100, 50, 0, 60, 0, 0, 50, 60, 0, 110, 80, 40, 110, 100, 40, 30, 70, 90, 50, 100, 80, 20, 60, 60, 20, 50, 40, 40, 40, 20, 30, 50, 90, 70, 100, 80, 50, 50, 70, 0, 50, 80, 20, 50, 40, 60, 80, 60, 20, 50, 30, 20, 100, 50, 60, 40, 30, 0, 60, 0, 0, 60, 50, 40, 40, 40, 70, 30, 20, 80, 70, 70, 30, 60, 0, 30, 30, 0, 50, 60, 0, 50, 0, 20, 20};

    private int cct[] = {4116, 4432, 4776, 5069, 5269, 5383, 5497, 5576, 5619, 5652, 5662, 5677, 5680, 5637, 5601, 5576, 5517, 5428, 5294, 5150, 4956, 4710, 4446, 4238};
    private int lux[] = {182, 194, 196, 181, 186, 189, 182, 188, 185, 184, 188, 187, 200, 186, 199, 188, 200, 180, 189, 200, 188, 181, 197, 196};

//    private int cct[] = {4116, 4127, 4131, 4133, 4139, 4142, 4153, 4156, 4159, 4166, 4184, 4195, 4203, 4217, 4228, 4245, 4258, 4270, 4281, 4301, 4316, 4328, 4343, 4356, 4374, 4382, 4389, 4396, 4413, 4423, 4432, 4446, 4465, 4473, 4485, 4497, 4512, 4539, 4557, 4574, 4581, 4600, 4618, 4626, 4636, 4636, 4643, 4657, 4665, 4680, 4694, 4702, 4720, 4717, 4724, 4732, 4738, 4738, 4749, 4764, 4776, 4789, 4802, 4819, 4831, 4840, 4850, 4862, 4878, 4888, 4901, 4913, 4935, 4935, 4941, 4946, 4958, 4968, 4984, 4993, 4998, 5009, 5023, 5023, 5024, 5048, 5056, 5052, 5061, 5069, 5071, 5078, 5086, 5093, 5101, 5107, 5109, 5128, 5135, 5144, 5152, 5161, 5176, 5183, 5196, 5199, 5202, 5219, 5222, 5222, 5231, 5246, 5249, 5249, 5257, 5254, 5253, 5262, 5269, 5269, 5284, 5290, 5286, 5296, 5294, 5308, 5312, 5308, 5296, 5313, 5310, 5312, 5316, 5323, 5332, 5331, 5345, 5353, 5363, 5360, 5366, 5369, 5370, 5376, 5388, 5397, 5402, 5397, 5383, 5402, 5402, 5418, 5416, 5418, 5428, 5416, 5423, 5432, 5437, 5435, 5432, 5440, 5461, 5465, 5452, 5473, 5473, 5481, 5484, 5484, 5479, 5485, 5485, 5487, 5479, 5471, 5497, 5484, 5497, 5497, 5487, 5487, 5497, 5497, 5487, 5497, 5517, 5515, 5520, 5508, 5517, 5530, 5545, 5544, 5537, 5540, 5541, 5537, 5541, 5534, 5548, 5547, 5534, 5530, 5529, 5521, 5543, 5583, 5569, 5576, 5565, 5576, 5583, 5583, 5583, 5590, 5601, 5593, 5593, 5586, 5576, 5586, 5576, 5583, 5583, 5586, 5593, 5601, 5593, 5590, 5601, 5601, 5593, 5601, 5601, 5604, 5623, 5619, 5619, 5619, 5613, 5619, 5613, 5607, 5623, 5613, 5611, 5613, 5627, 5625, 5625, 5631, 5627, 5625, 5623, 5625, 5637, 5633, 5633, 5633, 5631, 5650, 5647, 5643, 5647, 5647, 5643, 5650, 5652, 5647, 5647, 5650, 5647, 5650, 5647, 5653, 5650, 5647, 5647, 5644, 5647, 5650, 5653, 5647, 5647, 5653, 5653, 5660, 5660, 5660, 5660, 5662, 5662, 5662, 5650, 5650, 5653, 5662, 5662, 5662, 5662, 5660, 5662, 5662, 5660, 5667, 5667, 5667, 5669, 5667, 5667, 5677, 5677, 5667, 5667, 5662, 5667, 5667, 5674, 5680, 5680, 5680, 5669, 5677, 5680, 5680, 5685, 5677, 5677, 5674, 5674, 5674, 5680, 5680, 5677, 5680, 5680, 5680, 5677, 5680, 5677, 5688, 5688, 5685, 5680, 5680, 5680, 5677, 5680, 5680, 5677, 5677, 5680, 5677, 5677, 5677, 5685, 5685, 5680, 5680, 5677, 5674, 5677, 5677, 5677, 5669, 5667, 5667, 5669, 5669, 5667, 5667, 5669, 5662, 5662, 5662, 5662, 5660, 5660, 5662, 5660, 5660, 5660, 5647, 5640, 5637, 5640, 5640, 5637, 5623, 5633, 5631, 5637, 5637, 5627, 5627, 5640, 5643, 5643, 5640, 5643, 5627, 5627, 5627, 5623, 5619, 5631, 5633, 5627, 5637, 5640, 5623, 5619, 5619, 5623, 5619, 5611, 5601, 5607, 5592, 5593, 5601, 5601, 5607, 5611, 5593, 5590, 5593, 5607, 5604, 5611, 5601, 5607, 5604, 5593, 5590, 5590, 5593, 5590, 5583, 5593, 5593, 5586, 5601, 5593, 5590, 5583, 5576, 5573, 5569, 5572, 5573, 5583, 5583, 5583, 5583, 5573, 5576, 5561, 5551, 5551, 5551, 5562, 5553, 5558, 5548, 5540, 5540, 5541, 5534, 5537, 5530, 5522, 5528, 5522, 5517, 5515, 5517, 5520, 5520, 5517, 5508, 5512, 5497, 5501, 5500, 5506, 5499, 5501, 5497, 5497, 5479, 5473, 5471, 5465, 5459, 5452, 5449, 5449, 5441, 5440, 5432, 5435, 5432, 5433, 5434, 5428, 5428, 5416, 5405, 5403, 5402, 5397, 5391, 5390, 5387, 5387, 5383, 5374, 5366, 5367, 5359, 5356, 5352, 5340, 5345, 5340, 5340, 5343, 5331, 5320, 5320, 5331, 5324, 5320, 5308, 5305, 5294, 5284, 5287, 5292, 5296, 5292, 5287, 5279, 5277, 5269, 5262, 5257, 5253, 5253, 5238, 5238, 5225, 5216, 5214, 5216, 5216, 5202, 5196, 5189, 5178, 5177, 5182, 5166, 5156, 5150, 5144, 5137, 5128, 5131, 5117, 5109, 5105, 5101, 5098, 5093, 5088, 5078, 5059, 5056, 5046, 5036, 5031, 5035, 5035, 5030, 5016, 5018, 5014, 5002, 4994, 4982, 4979, 4974, 4965, 4956, 4946, 4936, 4932, 4926, 4920, 4918, 4908, 4903, 4890, 4880, 4873, 4865, 4853, 4847, 4836, 4829, 4817, 4807, 4800, 4795, 4791, 4776, 4766, 4761, 4754, 4747, 4737, 4732, 4721, 4710, 4695, 4686, 4674, 4669, 4664, 4650, 4647, 4644, 4643, 4634, 4626, 4611, 4598, 4586, 4582, 4572, 4562, 4551, 4536, 4523, 4512, 4504, 4497, 4493, 4492, 4473, 4472, 4457, 4449, 4446, 4433, 4427, 4412, 4403, 4389, 4369, 4369, 4356, 4358, 4347, 4328, 4341, 4340, 4333, 4335, 4330, 4329, 4325, 4320, 4310, 4309, 4301, 4288, 4274, 4247, 4238};
//    private int lux[] = {182, 183, 200, 186, 187, 182, 185, 185, 197, 191, 182, 189, 185, 186, 190, 184, 182, 191, 200, 199, 184, 183, 180, 196, 187, 195, 199, 181, 200, 181, 194, 197, 186, 199, 185, 185, 183, 187, 198, 196, 193, 197, 184, 197, 181, 181, 194, 182, 185, 189, 194, 187, 198, 182, 197, 184, 196, 196, 199, 197, 196, 186, 183, 186, 198, 195, 190, 198, 187, 188, 189, 193, 200, 200, 188, 186, 185, 196, 181, 199, 193, 181, 183, 183, 182, 198, 194, 198, 193, 181, 197, 194, 191, 196, 199, 184, 196, 199, 193, 180, 189, 195, 200, 199, 198, 194, 181, 196, 185, 185, 194, 200, 200, 200, 195, 192, 190, 196, 186, 186, 190, 187, 197, 196, 189, 182, 196, 182, 196, 187, 193, 196, 180, 180, 190, 196, 197, 196, 180, 186, 200, 196, 199, 200, 199, 188, 197, 188, 189, 197, 197, 195, 190, 195, 180, 190, 200, 181, 198, 181, 181, 190, 187, 191, 185, 194, 194, 182, 197, 197, 198, 183, 183, 186, 198, 181, 182, 197, 182, 182, 186, 186, 182, 182, 186, 182, 200, 200, 188, 191, 200, 191, 194, 196, 190, 198, 181, 190, 181, 182, 200, 187, 182, 191, 194, 189, 192, 197, 198, 188, 185, 188, 197, 197, 197, 191, 199, 195, 195, 187, 188, 187, 188, 197, 197, 187, 195, 199, 195, 191, 199, 199, 195, 199, 199, 189, 191, 185, 185, 185, 190, 185, 190, 181, 191, 190, 195, 190, 189, 195, 195, 190, 189, 195, 191, 195, 186, 190, 190, 190, 190, 191, 182, 199, 182, 182, 199, 191, 184, 182, 182, 191, 182, 191, 182, 199, 191, 182, 182, 183, 182, 191, 199, 182, 182, 199, 199, 197, 197, 197, 197, 188, 188, 188, 191, 191, 199, 188, 188, 188, 188, 197, 188, 188, 197, 187, 187, 187, 180, 187, 187, 187, 187, 187, 187, 188, 187, 187, 195, 200, 200, 200, 180, 187, 200, 200, 192, 187, 187, 195, 195, 195, 200, 200, 187, 200, 200, 200, 187, 200, 187, 190, 190, 192, 200, 200, 200, 187, 200, 200, 187, 187, 200, 187, 187, 187, 192, 192, 200, 200, 187, 195, 187, 187, 187, 180, 187, 187, 180, 180, 187, 187, 180, 188, 188, 188, 188, 197, 197, 188, 197, 197, 197, 182, 192, 186, 192, 192, 186, 191, 190, 190, 186, 186, 189, 189, 192, 199, 199, 192, 199, 189, 189, 189, 191, 185, 190, 190, 189, 186, 192, 191, 185, 185, 191, 185, 195, 199, 181, 197, 195, 199, 199, 181, 195, 195, 191, 195, 181, 189, 195, 199, 181, 189, 195, 191, 191, 195, 191, 197, 195, 195, 187, 199, 195, 191, 197, 188, 191, 198, 181, 191, 197, 197, 197, 197, 191, 188, 182, 193, 193, 193, 181, 187, 191, 200, 198, 198, 181, 182, 190, 191, 180, 185, 180, 200, 200, 200, 188, 188, 200, 191, 200, 182, 193, 197, 198, 189, 193, 182, 182, 198, 194, 181, 191, 191, 185, 199, 199, 187, 190, 181, 181, 181, 191, 183, 180, 180, 190, 187, 191, 197, 188, 186, 187, 186, 186, 189, 200, 200, 191, 196, 200, 199, 191, 197, 191, 191, 189, 196, 181, 181, 196, 187, 181, 182, 188, 189, 190, 180, 189, 196, 189, 180, 197, 192, 186, 196, 195, 190, 190, 189, 189, 184, 196, 191, 196, 196, 181, 198, 190, 188, 192, 184, 198, 197, 200, 180, 185, 199, 191, 183, 196, 198, 199, 188, 196, 190, 194, 188, 194, 186, 195, 188, 196, 196, 190, 186, 200, 189, 181, 192, 190, 191, 194, 191, 188, 186, 191, 192, 182, 189, 183, 200, 180, 183, 199, 188, 200, 180, 200, 200, 195, 185, 194, 193, 200, 186, 196, 189, 198, 197, 198, 183, 184, 191, 181, 194, 194, 189, 186, 182, 194, 192, 185, 194, 200, 197, 194, 187, 199, 187, 197, 189, 185, 183, 184, 183, 188, 185, 191, 197, 199, 189, 191, 191, 197, 181, 191, 187, 196, 199, 197, 197, 196, 185, 182, 183, 184, 197, 191, 183, 190, 182, 193, 193, 184, 197, 199, 193, 188, 188, 196};

    // 서비스 라이프사이클을 관리하는 코드입니다.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
                if (!mBluetoothLeService.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }
                // 시동 초기화에 성공하면 자동으로 장치에 연결합니다.
                mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "mBluetoothLeService - connect 성공");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Log.d(TAG, " onServiceDisconnected() 호출");
        }
    };

    //서비스에서 발생한 다양한 이벤트를 처리합니다.
    //작업_GATT_CONNECTED: GATT 서버에 연결되었습니다.
    //작업_GATT_DISCONNECT: GATT 서버에서 연결이 끊어졌습니다.
    //작업_GATT_SERVICES_DISCOVERED: GATT 서비스를 검색했습니다.
    //조치_DATA_ABLE: 디바이스에서 데이터를 수신했습니다. 읽기 또는 알림 작업의 결과일 수 있습니다.

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                bleConnect = true;
                updateConnectionState("연결");
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                bleConnect = false;
                updateConnectionState("연결해제");
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                Log.d("On Receive", "$$$ACTION_gatt_discoverd..");
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        final Intent intent = getIntent();

        System.out.println(mDeviceName + "  " + mDeviceAddress);

        btnCh1 = (Button) findViewById(R.id.btn_1ch);
        btnCh2 = (Button) findViewById(R.id.btn_2ch);
        btnCh3 = (Button) findViewById(R.id.btn_3ch);
        btnCh4 = (Button) findViewById(R.id.btn_4ch);
        btnNL = (Button) findViewById(R.id.btn_nl);
        ibOnOff = (ImageButton) findViewById(R.id.ib_onoff);
        ibBluetooth = (ImageButton) findViewById(R.id.ib_bluetooth);
        ibConnServer = (ImageButton) findViewById(R.id.cloud);

        ch1Value = ch2Value = ch3Value = ch4Value = "00";
        ivConn = (ImageView) findViewById(R.id.img_connect);
        tvConn = (TextView) findViewById(R.id.text_connect);
        tvName = (TextView) findViewById(R.id.tv_deviceName);
        tvAddress = (TextView) findViewById(R.id.tv_deviceAddress);
        tvNow = (TextView) findViewById(R.id.tv_nowcct);

        getSupportActionBar().setTitle(mDeviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        ibConnServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConn) {
                    isConn = true;
                    new Thread() {
                        @Override
                        public void run() {
                            while (isConn) {
                                try {
                                    URL url = new URL("http://210.102.142.15:8088/hakbu_get_cct/");
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                                    connection.setRequestMethod("POST");
                                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                                    outputStream.writeBytes("serialNumber=kongjuWitlab01");
                                    outputStream.flush();
                                    outputStream.close();

                                    int responseCode = connection.getResponseCode();

                                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                    StringBuffer stringBuffer = new StringBuffer();
                                    String inputLine;

                                    while ((inputLine = bufferedReader.readLine()) != null) {
                                        stringBuffer.append(inputLine);
                                    }
                                    bufferedReader.close();

                                    String response = stringBuffer.toString();

                                    String split_res[] = response.split("&");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (split_res[0] != null)
                                                tvNow.setText("NOW : " + split_res[0] + "K, " + split_res[1] + "Lux");
                                        }
                                    });

                                    Thread.sleep(3000);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }.start();
                } else {
                    isConn = false;
                }
            }
        });

        ibBluetooth.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), BluetoothScanActivity.class);
                startActivityResult.launch(intent);
            }
        });
        ibOnOff.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                String packet;
                if (!ledPower) {
                    ledPower = true;
                    packet = "0211FF" + ch1Value + ch2Value + ch3Value + ch4Value + "03";
                    Toast.makeText(v.getContext(), "power on", Toast.LENGTH_SHORT).show();

                } else {
                    ledPower = false;
                    packet = "0211FF0000000003";
                    Toast.makeText(v.getContext(), "power off", Toast.LENGTH_SHORT).show();
                }
                byte[] data = hexStringToByteArray(packet);
                mBluetoothLeService.writeCustomCharacteristic(data);
            }
        });
        btnCh1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                ledPower = true;
                String packet = "0211FFFF00000003";
                ch1Value = "FF";
                ch2Value = ch3Value = ch4Value = "00";
                Toast.makeText(v.getContext(), "ch1", Toast.LENGTH_SHORT).show();
                byte[] data = hexStringToByteArray(packet);
                mBluetoothLeService.writeCustomCharacteristic(data);
            }
        });
        btnCh2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                ledPower = true;
                String packet = "0211FF00FF000003";
                ch2Value = "FF";
                ch1Value = ch3Value = ch4Value = "00";
                Toast.makeText(v.getContext(), "ch2", Toast.LENGTH_SHORT).show();
                byte[] data = hexStringToByteArray(packet);
                mBluetoothLeService.writeCustomCharacteristic(data);
            }
        });
        btnCh3.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                ledPower = true;
                String packet = "0211FF0000FF0003";
                ch3Value = "FF";
                ch2Value = ch1Value = ch4Value = "00";
                Toast.makeText(v.getContext(), "ch3", Toast.LENGTH_SHORT).show();
                byte[] data = hexStringToByteArray(packet);
                mBluetoothLeService.writeCustomCharacteristic(data);
            }
        });
        btnCh4.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                ledPower = true;
                String packet = "0211FF000000FF03";
                ch4Value = "FF";
                ch2Value = ch3Value = ch1Value = "00";
                Toast.makeText(v.getContext(), "ch4", Toast.LENGTH_SHORT).show();
                byte[] data = hexStringToByteArray(packet);
                mBluetoothLeService.writeCustomCharacteristic(data);
            }
        });
        btnNL.setOnClickListener(new View.OnClickListener() {
            int i;

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        for (i = 0; i < ch1.length; i++) {
                            String ch1_h;
                            String ch2_h;
                            String ch3_h;
                            String ch4_h;

                            if (ch1[i] == 0) {
                                ch1_h = "00";
                            } else {
                                ch1_h = Integer.toHexString(ch1[i]);
                            }
                            if (ch2[i] == 0) {
                                ch2_h = "00";
                            } else {
                                ch2_h = Integer.toHexString(ch2[i]);
                            }
                            if (ch3[i] == 0) {
                                ch3_h = "00";
                            } else {
                                ch3_h = Integer.toHexString(ch3[i]);
                            }
                            if (ch4[i] == 0) {
                                ch4_h = "00";
                            } else {
                                ch4_h = Integer.toHexString(ch4[i]);
                            }

                            System.out.println("채널정보");
                            System.out.println(ch1[i] + " " + ch1_h);
                            System.out.println(ch2[i] + " " + ch2_h);
                            System.out.println(ch3[i] + " " + ch3_h);
                            System.out.println(ch4[i] + " " + ch4_h);
                            System.out.println("변환");


                            String packet = "0211ff" + ch1_h + ch2_h + ch3_h + ch4_h + "03";
                            System.out.println(packet);
                            byte[] data = hexStringToByteArray(packet);
                            mBluetoothLeService.writeCustomCharacteristic(data);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "CCT : " + cct[i] + "K, Lux : " + lux[i], Toast.LENGTH_SHORT).show();
                                }
                            });

                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
            }
        });
    }

    /* BluetoothActivity에서 디바이스 연결을 끝낸 후 돌아온 MainActivity */
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {

                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(getApplicationContext(), "Bluetooth Connect 완료.", Toast.LENGTH_LONG).show();
                        Intent intent = result.getData();

                        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
                        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

                        tvName.setText("Connected Device      : " + mDeviceName);
                        tvAddress.setText("Connected Address    : " + mDeviceAddress);

                        Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);
                        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
                    }
                }
            }
    );


    /* String -> 16진수 Byte */
    private byte[] hexStringToByteArray(String str) {
        int len = str.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return data;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        mBluetoothLeService.disconnect();
        mBluetoothLeService.bluetoothDisconnect(mBluetoothDevice);
        Log.d(TAG, "onDestroy() 호출");
    }

    private void updateConnectionState(String state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state.equals("연결")) {
                    ivConn.setImageResource(R.drawable.on);
                } else if (state.equals("연결해제")) {
                    ivConn.setImageResource(R.drawable.fail);
                }
                tvConn.setText("블루투스 장치 " + state);
            }
        });
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

}