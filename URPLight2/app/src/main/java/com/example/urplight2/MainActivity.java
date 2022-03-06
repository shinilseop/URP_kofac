package com.example.urplight2;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE = "DEVICE";

    private String mDeviceName;
    private String mDeviceAddress;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothLeService mBluetoothLeService;

    private ImageView imgConnect;
    private TextView textConnect;
    private ImageButton btnPower;
    private ImageView imgLight;
    private ImageButton btnBLE;
    private TextView textBLE;
    private EditText editSerialNumber;
    private Button btnSerialNumber;

    private boolean ledPower = false;
    private boolean bleConnect = true;
    private String SERIAL_NUMBER;

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
                updateConnectionState("connected");
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                bleConnect = false;
                updateConnectionState("disconnected");
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
        mBluetoothDevice = intent.getExtras().getParcelable(EXTRAS_DEVICE);
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        System.out.println(mDeviceName+"  "+mDeviceAddress);

        imgConnect = (ImageView) findViewById(R.id.img_connect);
        textConnect = (TextView) findViewById(R.id.text_connect);
        btnPower = (ImageButton)findViewById(R.id.btn_power);
        imgLight = (ImageView)findViewById(R.id.img_light);
        btnBLE = (ImageButton)findViewById(R.id.btn_ble);
        textBLE = (TextView)findViewById(R.id.text_ble);
        editSerialNumber = (EditText)findViewById(R.id.edit_serial_number);
        btnSerialNumber = (Button)findViewById(R.id.btn_serial_number);

        getSupportActionBar().setTitle(mDeviceName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        System.out.println("@@@ bindService 호출");
        if(mServiceConnection==null){
            System.out.println("@@@@@@@@@@  mServiceConnection == null");
        }else {
            System.out.println("@@@@@@@@@@  mServiceConnection != null");
        }
        if(mGattUpdateReceiver==null){
            System.out.println("@@@@@@@@@@  mGattUpdateReceiver == null");
        }else {
            System.out.println("@@@@@@@@@@  mGattUpdateReceiver != null");
        }
        if(mBluetoothLeService==null){
            System.out.println("@@@@@@@@@@  mBluetoothLeService == null");
        }else {
            System.out.println("@@@@@@@@@@  mBluetoothLeService != null");
        }


        btnPower.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                String packet;

                if(!ledPower){
                    btnPower.setImageResource(R.drawable.power_on);
                    ledPower = true;
                    changeImgLight("#FFEB3B");
                    packet = "010200FF0000";
                    Toast.makeText(v.getContext(), "조명이 켜졌습니다.", Toast.LENGTH_SHORT).show();

                }else {
                    btnPower.setImageResource(R.drawable.power_off);
                    ledPower = false;
                    changeImgLight("#FFFFFF");
                    packet = "010200000000";
                    Toast.makeText(v.getContext(), "조명이 꺼졌습니다.", Toast.LENGTH_SHORT).show();

                }
                byte[] data = hexStringToByteArray(packet);
                mBluetoothLeService.writeCustomCharacteristic(data);
            }
        });

        btnBLE.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) { // ble 연결 된 경우
                if(bleConnect){
                    mBluetoothLeService.disconnect();
                    mBluetoothLeService.bluetoothDisconnect(mBluetoothDevice);
                    textBLE.setText("재연결");
                }else { // ble 연결 끊긴 경우
                    mBluetoothLeService.connect(mDeviceAddress);
                    textBLE.setText("연결중단");
                }
            }
        });

        btnSerialNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnSerialNumber.getText().equals("입력")) {
                    SERIAL_NUMBER = editSerialNumber.getText().toString();
                    System.out.println("@@@@ 입력받은 시리얼 넘버 :" + SERIAL_NUMBER);
                    editSerialNumber.setEnabled(false);
                    btnSerialNumber.setText("변경");
                }else {
                    editSerialNumber.setEnabled(true);
                    btnSerialNumber.setText("입력");
                }
            }
        });
    }


    /* String -> 16진수 Byte */
    private byte[] hexStringToByteArray(String str) {
        int len = str.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return data;
    }

    public void changeImgLight(String color){
        imgLight.setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_ATOP);
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_connect, menu);
//        if (mConnected) {
//            menu.findItem(R.id.menu_connect).setVisible(false);
//            menu.findItem(R.id.menu_disconnect).setVisible(true);
//        } else {
//            menu.findItem(R.id.menu_connect).setVisible(true);
//            menu.findItem(R.id.menu_disconnect).setVisible(false);
//        }
//        return true;
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_connect:
//                mBluetoothLeService.connect(mDeviceAddress);
//                return true;
//            case R.id.menu_disconnect:
//                mBluetoothLeService.disconnect();
//                mBluetoothLeService.bluetoothDisconnect(mBluetoothDevice);
//                return true;
//            case android.R.id.home:
//                onBackPressed();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void updateConnectionState(String state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state.equals("connected")) {
                    imgConnect.setImageResource(R.drawable.on);
                } else if (state.equals("disconnected")) {
                    imgConnect.setImageResource(R.drawable.fail);
                }
                textConnect.setText("bluetooth device " + state);
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