package org.techtown.urp.Function;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.techtown.urp.MainActivity;
import org.techtown.urp.R;

import java.util.ArrayList;

public class BluetoothScanActivity extends Activity {
    BluetoothAdapter mBluetoothAdapter;
    Handler mHandler;

    ArrayList<DeviceData> deviceSearchList;

    MyAdapter mySearchAdapter;

    private ListView listSeaching;
    private Button btn_search, btn_end;
    private TextView tvName, tvAddress;
    private EditText etSerial;

    boolean mScanning = false;


    boolean insert_serial=false;
    boolean ble_selected = false;
    private String mBluetoothName;
    private String mBluetoothAddress;

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

        listSeaching = (ListView) findViewById(R.id.list_ble);
        btn_search = (Button) findViewById(R.id.btn_scan);
        btn_end = findViewById(R.id.btn_end);
        tvName=findViewById(R.id.text_name);
        tvAddress=findViewById(R.id.text_address);


        etSerial = (EditText)findViewById(R.id.et_serial);

        deviceSearchList = new ArrayList<DeviceData>();

        mHandler = new Handler();

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

        /* ?????? ?????? ?????? ????????? - ?????? ?????? ??????*/
        btn_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ble_selected) {
                    Toast.makeText(v.getContext(), "????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }

                final Intent intent = new Intent(BluetoothScanActivity.this, MainActivity.class);
                intent.putExtra(MainActivity.EXTRAS_DEVICE_NAME, mBluetoothName);
                intent.putExtra(MainActivity.EXTRAS_DEVICE_ADDRESS, mBluetoothAddress);
                String serial_tmp=etSerial.getText()+"";
                System.out.println("SERIAL TMP : "+serial_tmp);
                intent.putExtra("SerialNumber", serial_tmp);
                if (mScanning) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = false;
                }
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        listSeaching.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BluetoothDevice device = deviceSearchList.get(position).getDevice();
                if (device == null)
                    return;
                mBluetoothName=device.getName();
                mBluetoothAddress=device.getAddress();
                tvName.setText("Selected Device  : "+mBluetoothName);
                tvAddress.setText("Selected Address : "+mBluetoothAddress);
                ble_selected =true;
                if(insert_serial){
                    btn_end.setEnabled(true);
                }
            }
        });

        etSerial.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ???????????? ????????? ?????? ??? ??????
                insert_serial=true;
                if(ble_selected){
                    btn_end.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // ????????? ????????? ??? ??????
                insert_serial=true;
                if(ble_selected){
                    btn_end.setEnabled(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ???????????? ?????? ??????
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
                    btn_search.setEnabled(true);
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            btn_search.setEnabled(false);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            btn_search.setEnabled(true);
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