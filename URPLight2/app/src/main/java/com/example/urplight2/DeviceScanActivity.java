package com.example.urplight2;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class DeviceScanActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;

//    private ArrayList<DeviceData> deviceArrayList;
    private ArrayList<BluetoothDevice> deviceArrayList;
    private MyAdapter myListAdapter;

    private ListView listBLE;
    private Button btnScan;
    private Button btnEnd;
    private TextView textName;
    private TextView textAddress;

    boolean mScanning = false;
    private static final long SCAN_PERIOD = 7000; // 장치검색 시간 7초
    private static final int REQUEST_ENABLE_BT = 1;
    private Intent main_intent;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);
        getSupportActionBar().hide();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        mBluetoothAdapter.startDiscovery();

        listBLE = (ListView)findViewById(R.id.list_ble);
        btnScan = (Button)findViewById(R.id.btn_scan);
        btnEnd = (Button)findViewById(R.id.btn_end);
        btnEnd.setEnabled(false);
        textName = (TextView)findViewById(R.id.text_name);
        textAddress = (TextView)findViewById(R.id.text_address);

        deviceArrayList = new ArrayList<BluetoothDevice>();
        myListAdapter = new MyAdapter(this, deviceArrayList);
        listBLE.setAdapter(myListAdapter);

        mHandler = new Handler();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {//블루투스 없으면
            Toast.makeText(this, "해당 기기에서는 BLE를 작동시킬 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if(mBluetoothAdapter==null){
            System.out.println("@@@@@@@@@@  mBluetoothAdapter2 == null");
        }else {
            System.out.println("@@@@@@@@@@  mBluetoothAdapter2 != null");
        }

        if (mBluetoothAdapter == null) {//블루투스 없으면
            Toast.makeText(this, "해당 기기에서는 블루투스가 지원되지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        /* BLE 장치 검색 버튼 */
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceArrayList.clear();
                if (!mScanning) {
                    myListAdapter.notifyDataSetChanged();
                    scanLeDevice(true);
                } else {
                    scanLeDevice(false);
                }  
            }
        });

        /* 완료 버튼 */
        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(main_intent);
            }
        });

        listBLE.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice device = myListAdapter.getItem(position);

                if(device == null)
                    return;

                main_intent = new Intent(view.getContext(), MainActivity.class);
                main_intent.putExtra(MainActivity.EXTRAS_DEVICE, device);
                main_intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, device.getName());
                main_intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());

                if(device.getName() == null){
                    textName.setText("null");
                }else {
                    textName.setText(device.getName());
                }
                textAddress.setText(device.getAddress());

                if(mScanning){
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }

                btnEnd.setEnabled(true);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onResume() {//블루투스 꺼져있을때 권한요청하는거
        super.onResume();

        // 장치에서 블루투스가 활성화되어 있는지 확인합니다. 현재 블루투스가 활성화되지 않은 경우
        // 사용자에게 사용 권한을 부여하여 대화 상자를 표시하려는 의도를 표시합니다.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // 목록 보기 어댑터를 초기화합니다.
        myListAdapter = new MyAdapter(this, deviceArrayList);
        listBLE.setAdapter(myListAdapter);
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //사용자가 블루투스권한 요청을 거부했을때
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        myListAdapter.clear();
        deviceArrayList.clear();
    }

    /* 주변 블루투스 장치 검색 - 설정한 검색시간이 지나면 검색 중지 */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /* 장치 검색 콜백 함수 */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!deviceArrayList.contains(device)) {
                        deviceArrayList.add(device);
                        myListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };
}