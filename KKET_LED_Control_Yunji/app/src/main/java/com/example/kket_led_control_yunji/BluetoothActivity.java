package com.example.kket_led_control_yunji;

import androidx.annotation.RequiresApi;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothActivity extends Activity {
    BluetoothAdapter mBluetoothAdapter;
    Handler mHandler;

    Set<BluetoothDevice> pairedDevices;
    ArrayList<DeviceData> devicePairingList;
    ArrayList<DeviceData> deviceSearchList;

    MyAdapter myPairingAdapter;
    MyAdapter mySearchAdapter;

    ListView listPairing;
    ListView listSeaching;
    Button btn_search;

    boolean mScanning = false;

    private static final long SCAN_PERIOD = 10000;
    private static final int REQUEST_ENABLE_BT = 1;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.startDiscovery();

        listPairing = (ListView) findViewById(R.id.list_pairing);
        listSeaching = (ListView) findViewById(R.id.list_searching);
        btn_search = (Button) findViewById(R.id.btn_search);

        devicePairingList = new ArrayList<DeviceData>();
        myPairingAdapter = new MyAdapter(this, devicePairingList);
        listPairing.setAdapter(myPairingAdapter);

        deviceSearchList = new ArrayList<DeviceData>();

        mHandler = new Handler();

        searchPairedDevice();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {//???????????? ?????????
            Toast.makeText(this, "?????? ??????????????? BLE??? ???????????? ??? ????????????.", Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {//???????????? ?????????
            Toast.makeText(this, "?????? ??????????????? ??????????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        /* ?????? ?????? ?????? ????????? - ?????? ?????? ??????*/
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mScanning) {
                    deviceSearchList.clear();
                    mySearchAdapter.notifyDataSetChanged();
                    scanLeDevice(true);
                } else {
                    scanLeDevice(false);
                }
            }
        });

        /* BLE ??????????????? ???????????? ??????, ????????? ????????? ?????? ??????????????? ??????????????? ?????? ?????? */
        listPairing.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice device = devicePairingList.get(position).getDevice();
                if (device == null)
                    return;
                final Intent intent = new Intent(BluetoothActivity.this, MainActivity.class);
                intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, device.getName());
                intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                intent.putExtra(MainActivity.START_SECOND_MAIN, "start");
                System.out.println("@@@@@@@@@@@@@@@@?????? ???????????? ?????? ??? ?????? : "+device.getName()+"     "+device.getAddress());
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }
                setResult(RESULT_OK, intent);
                finish();
//                startActivity(intent);
            }
        });

        listSeaching.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice device = deviceSearchList.get(position).getDevice();
                if (device == null)
                    return;
                final Intent intent = new Intent(BluetoothActivity.this, MainActivity.class);
                intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, device.getName());
                intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onResume() {
        super.onResume();

        /* ??????????????? ???????????? ??? ?????? ?????? */
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // ?????? ?????? ???????????? ??????????????????.
        mySearchAdapter = new MyAdapter(this, deviceSearchList);
        listSeaching.setAdapter(mySearchAdapter);
        scanLeDevice(true);
    }

    /* ?????? ???????????? ?????? ?????? ???????????? ????????? */
    public void searchPairedDevice() {
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) { // ???????????? ????????? ??? ??????????????? ?????? ??????
            for (BluetoothDevice device : pairedDevices) {
                devicePairingList.add(new DeviceData(device.getName(), device.getAddress(), device));
                myPairingAdapter.notifyDataSetChanged();
            }
        }
    }

    /* ???????????? ???????????? ?????? ????????? ???????????? ??? */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /* ?????? ???????????? ?????? ?????? - ????????? ??????????????? ????????? ?????? ?????? */
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

    /* ?????? ?????? ?????? ?????? */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (device.getName() == null) {
                        deviceSearchList.add(new DeviceData("null", device.getAddress(), device));
                    } else {
                        deviceSearchList.add(new DeviceData(device.getName(), device.getAddress(), device));
                    }
                    mySearchAdapter.notifyDataSetChanged();
                }
            });
        }
    };
}