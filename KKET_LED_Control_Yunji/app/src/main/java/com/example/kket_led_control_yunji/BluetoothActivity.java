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

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {//블루투스 없으면
            Toast.makeText(this, "해당 기기에서는 BLE를 작동시킬 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {//블루투스 없으면
            Toast.makeText(this, "해당 기기에서는 블루투스가 지원되지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        /* 장치 검색 버튼 이벤트 - 주변 기기 검색*/
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

        /* BLE 디바이스를 선택하면 이름, 주소를 가지고 메인 액티비티로 넘겨주면서 화면 전환 */
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
                System.out.println("@@@@@@@@@@@@@@@@보낼 디바이스 이름 및 주소 : "+device.getName()+"     "+device.getAddress());
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

        /* 블루투스가 꺼져있을 때 권한 요청 */
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // 목록 보기 어댑터를 초기화합니다.
        mySearchAdapter = new MyAdapter(this, deviceSearchList);
        listSeaching.setAdapter(mySearchAdapter);
        scanLeDevice(true);
    }

    /* 이미 등록되어 있는 기기 리스트에 띄우기 */
    public void searchPairedDevice() {
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) { // 기본으로 페어링 된 디바이스가 있는 경우
            for (BluetoothDevice device : pairedDevices) {
                devicePairingList.add(new DeviceData(device.getName(), device.getAddress(), device));
                myPairingAdapter.notifyDataSetChanged();
            }
        }
    }

    /* 사용자가 블루투스 권한 요청을 거부했을 때 */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
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