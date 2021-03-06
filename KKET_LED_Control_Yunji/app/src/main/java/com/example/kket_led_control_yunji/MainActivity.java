package com.example.kket_led_control_yunji;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    SeekBar[] seekBars;
    TextView[] textNum;
    TextView text_deviceName;
    TextView text_deviceAddress;
    ImageView img_connect;
    TextView text_connect;

    String mDeviceName;
    String mDeviceAddress;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String START_SECOND_MAIN = "START_SECOND_MAIN";

    Button btn_off;

    BluetoothLeService mBluetoothLeService;
    boolean mConnected = false;
    BluetoothGattCharacteristic mNotifyCharacteristic;
//    ServiceConnection mServiceConnection;
//    BroadcastReceiver mGattUpdateReceiver;

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            System.out.println("onServiceConnected ?????? ?????? !");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Toast.makeText(getApplicationContext(), "??????????????? ???????????? ??? ????????????.", Toast.LENGTH_LONG).show();
                finish();
            }
            // ?????? ???????????? ???????????? ???????????? ????????? ???????????????.
            mBluetoothLeService.connect(mDeviceAddress);
            System.out.println("@@@@@@@@@@@@ ????????? ?????? ????????");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            System.out.println("onServiceDisconnected ?????? ?????? !");
            mBluetoothLeService = null;
        }
    };

    //    ??????????????? ????????? ????????? ???????????? ???????????????.
//    ??????_GATT_CONNECTED: GATT ????????? ?????????????????????.
//    ??????_GATT_DISCONNECT: GATT ???????????? ????????? ??????????????????.
//    ??????_GATT_SERVICES_DISCOVERED: GATT ???????????? ??????????????????.
//    ??????_DATA_ABLE: ?????????????????? ???????????? ??????????????????. ?????? ?????? ?????? ????????? ????????? ??? ????????????.
    BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("onReceive ?????? ?????? !");
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState("??????");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState("??????");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // ????????? ?????????????????? ???????????? ?????? ????????? ??? ????????? ???????????????.
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

            }
        }
    };

    // ????????? GATT ????????? ????????? ?????? ???????????? ????????? ???????????????. ??? ??????
    // '??????' ??? '??????' ????????? ???????????????. ?????????
    // ????????? ?????? http://d.android.com/reference/android/bluetooth/BluetoothGatt.html
    // ???????????? ?????? ?????? ???????????????.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//                    if (mGattCharacteristics != null) {
//                        final BluetoothGattCharacteristic characteristic =
//                                mGattCharacteristics.get(groupPosition).get(childPosition);
                      //  final int charaProp = characteristic.getProperties();
                      //  if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mNotifyCharacteristic = null;
                            }
                          //  mBluetoothLeService.readCharacteristic(characteristic);
                    return true;
                }

                     //   if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                     //       mNotifyCharacteristic = characteristic;
                      //      mBluetoothLeService.setCharacteristicNotification(
                      //              characteristic, true);
                     //   }
                      //  return true;
                  //  }
                //    return false;
              //  }

            };


    /* String -> 16?????? Byte */
    private byte[] hexStringToByteArray(String str) {
        int len = str.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("onCreate ?????? ?????? !");

        seekBars = new SeekBar[4];
        seekBars[0] = (SeekBar) findViewById(R.id.seekBar);
        seekBars[1] = (SeekBar) findViewById(R.id.seekBar2);
        seekBars[2] = (SeekBar) findViewById(R.id.seekBar3);
        seekBars[3] = (SeekBar) findViewById(R.id.seekBar4);

        textNum = new TextView[4];
        textNum[0] = (TextView) findViewById(R.id.text_num1);
        textNum[1] = (TextView) findViewById(R.id.text_num2);
        textNum[2] = (TextView) findViewById(R.id.text_num3);
        textNum[3] = (TextView) findViewById(R.id.text_num4);

        text_deviceName = (TextView) findViewById(R.id.text_deviceName);
        text_deviceAddress = (TextView) findViewById(R.id.text_deviceAddress);
        img_connect = (ImageView) findViewById(R.id.img_connect);
        text_connect = (TextView) findViewById(R.id.text_connect);
        btn_off=findViewById(R.id.btn_off);

        /* ????????? ????????? */
        for (int i = 0; i < seekBars.length; i++) {
            int index = i;
            seekBars[index].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    textNum[index].setText(String.format("%03d", progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
        }

        btn_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String packet = "0211FF0000000003";
                byte[] data = hexStringToByteArray(packet);
                System.out.print("packet:");
                for (int i=0;i<data.length;i++){
                    System.out.print(data[i]+" ");
                }
                System.out.println();
                mBluetoothLeService.writeCustomCharacteristic(data);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onResume() { // ???????????? - 1???
        super.onResume();
        System.out.println("onResume ?????? ?????? !");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            System.out.println("@@@@   Connect request result=" + result);
        } else {
            System.out.println("mBluetoothLeService == null ?????????");
        }
    }

    @Override
    protected void onPause() { // ?????? ?????? ?????? ??? (??????????????? ?????? ??????) - 2???
        super.onPause();
        System.out.println("onPause ?????? ?????? !");
       // unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy ?????? ?????? !");
      //  unbindService(mServiceConnection);
     //   mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /* ?????? ????????? - ???????????? ?????? */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        if (item.getItemId() == R.id.btn_bluetooth) {
            Intent intent = new Intent(this, BluetoothActivity.class);
            startActivityResult.launch(intent);
            System.out.println("???????????? ?????? ????????? ?????? ");
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /* BluetoothActivity?????? ???????????? ????????? ?????? ??? ????????? MainActivity */
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {

                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onActivityResult(ActivityResult result) {
                    System.out.println("onActivityResult ?????? ??????, ?????? ?????????????????? ????????? !");

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(getApplicationContext(), "Bluetooth Connect ??????.", Toast.LENGTH_LONG).show();
                        Intent intent = result.getData();

                        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
                        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
                        System.out.println("@@@@@@@@@@@@@@@mDeviceName : " + mDeviceName + "       Address ; " + mDeviceAddress);

                        text_deviceName.setText(mDeviceName);
                        text_deviceAddress.setText(mDeviceAddress);

//                        mServiceConnection = new ServiceConnection() {
//                            @Override
//                            public void onServiceConnected(ComponentName componentName, IBinder service) {
//                                System.out.println("onServiceConnected ?????? ?????? !");
//                                mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
//                                if (!mBluetoothLeService.initialize()) {
//                                    Toast.makeText(getApplicationContext(), "??????????????? ???????????? ??? ????????????.", Toast.LENGTH_LONG).show();
//                                    finish();
//                                }
//                                // ?????? ???????????? ???????????? ???????????? ????????? ???????????????.
//                                mBluetoothLeService.connect(mDeviceAddress);
//                                System.out.println("@@@@@@@@@@@@ ????????? ?????? ????????");
//                            }
//
//                            @Override
//                            public void onServiceDisconnected(ComponentName componentName) {
//                                System.out.println("onServiceDisconnected ?????? ?????? !");
//                                mBluetoothLeService = null;
//                            }
//                        };

                        Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);
                        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
                        System.out.println("@@@@@@@@@@  bindService ?????????");

                        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

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



                    }
                }
            }
    );

    private void updateConnectionState(String state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state.equals("??????")) {
                    img_connect.setImageResource(R.drawable.on);
                } else if (state.equals("??????")) {
                    img_connect.setImageResource(R.drawable.fail);
                }
                text_connect.setText("Bluetooth ?????? " + state);
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