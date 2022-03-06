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
            System.out.println("onServiceConnected 함수 실행 !");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Toast.makeText(getApplicationContext(), "블루투스를 초기화할 수 없습니다.", Toast.LENGTH_LONG).show();
                finish();
            }
            // 시동 초기화에 성공하면 자동으로 장치에 연결합니다.
            mBluetoothLeService.connect(mDeviceAddress);
            System.out.println("@@@@@@@@@@@@ 서비스 연결 성공??");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            System.out.println("onServiceDisconnected 함수 실행 !");
            mBluetoothLeService = null;
        }
    };

    //    서비스에서 발생한 다양한 이벤트를 처리합니다.
//    작업_GATT_CONNECTED: GATT 서버에 연결되었습니다.
//    작업_GATT_DISCONNECT: GATT 서버에서 연결이 끊어졌습니다.
//    작업_GATT_SERVICES_DISCOVERED: GATT 서비스를 검색했습니다.
//    조치_DATA_ABLE: 디바이스에서 데이터를 수신했습니다. 읽기 또는 알림 작업의 결과일 수 있습니다.
    BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("onReceive 함수 실행 !");
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState("완료");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState("실패");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // 사용자 인터페이스에 지원되는 모든 서비스 및 특성을 표시합니다.
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {

            }
        }
    };

    // 지정된 GATT 특성을 선택한 경우 지원되는 기능을 확인합니다. 이 샘플
    // '읽기' 및 '알림' 기능을 시연합니다. 보시오
    // 완료를 위한 http://d.android.com/reference/android/bluetooth/BluetoothGatt.html
    // 지원되는 특성 기능 목록입니다.
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


    /* String -> 16진수 Byte */
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
        System.out.println("onCreate 함수 실행 !");

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

        /* 시크바 이벤트 */
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
    protected void onResume() { // 이어하기 - 1번
        super.onResume();
        System.out.println("onResume 함수 실행 !");
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            System.out.println("@@@@   Connect request result=" + result);
        } else {
            System.out.println("mBluetoothLeService == null 입니다");
        }
    }

    @Override
    protected void onPause() { // 일시 정지 됐을 때 (다이얼로그 띄운 경우) - 2번
        super.onPause();
        System.out.println("onPause 함수 실행 !");
       // unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy 함수 실행 !");
      //  unbindService(mServiceConnection);
     //   mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /* 메뉴 이벤트 - 블루투스 연결 */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        if (item.getItemId() == R.id.btn_bluetooth) {
            Intent intent = new Intent(this, BluetoothActivity.class);
            startActivityResult.launch(intent);
            System.out.println("블루투스 설정 창으로 이동 ");
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /* BluetoothActivity에서 디바이스 연결을 끝낸 후 돌아온 MainActivity */
    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {

                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void onActivityResult(ActivityResult result) {
                    System.out.println("onActivityResult 함수 실행, 다시 메인화면으로 넘어옴 !");

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(getApplicationContext(), "Bluetooth Connect 완료.", Toast.LENGTH_LONG).show();
                        Intent intent = result.getData();

                        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
                        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
                        System.out.println("@@@@@@@@@@@@@@@mDeviceName : " + mDeviceName + "       Address ; " + mDeviceAddress);

                        text_deviceName.setText(mDeviceName);
                        text_deviceAddress.setText(mDeviceAddress);

//                        mServiceConnection = new ServiceConnection() {
//                            @Override
//                            public void onServiceConnected(ComponentName componentName, IBinder service) {
//                                System.out.println("onServiceConnected 함수 실행 !");
//                                mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
//                                if (!mBluetoothLeService.initialize()) {
//                                    Toast.makeText(getApplicationContext(), "블루투스를 초기화할 수 없습니다.", Toast.LENGTH_LONG).show();
//                                    finish();
//                                }
//                                // 시동 초기화에 성공하면 자동으로 장치에 연결합니다.
//                                mBluetoothLeService.connect(mDeviceAddress);
//                                System.out.println("@@@@@@@@@@@@ 서비스 연결 성공??");
//                            }
//
//                            @Override
//                            public void onServiceDisconnected(ComponentName componentName) {
//                                System.out.println("onServiceDisconnected 함수 실행 !");
//                                mBluetoothLeService = null;
//                            }
//                        };

                        Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);
                        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
                        System.out.println("@@@@@@@@@@  bindService 끝냈음");

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
                if (state.equals("완료")) {
                    img_connect.setImageResource(R.drawable.on);
                } else if (state.equals("실패")) {
                    img_connect.setImageResource(R.drawable.fail);
                }
                text_connect.setText("Bluetooth 연결 " + state);
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