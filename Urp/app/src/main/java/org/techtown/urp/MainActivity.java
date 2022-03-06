package org.techtown.urp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.techtown.urp.Function.BluetoothLeService;
import org.techtown.urp.Function.BluetoothScanActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    boolean power_on_off = false;

    private final static String TAG = MainActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE = "DEVICE";

    private String mDeviceName;
    private String mDeviceAddress;
    private String serialNumber;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothLeService mBluetoothLeService;

    //if power button is pressed, change image and content
    private ImageButton power_button;
    private TextView content_power;

    //if seekbar is moved, modify seekbar content and concurrent status content
    private int cct_sk_min = 2800, lux_sk_min = 80;
    private int cct_tmp = 0, lux_tmp = 0;
    private int seekCCT,seekLux;
    private SeekBar seekbar_cct;
    private SeekBar seekbar_lux;
    private TextView setting_content_cct;
    private TextView setting_content_lux;

    private ImageButton ibOnOff, ibBluetooth, ibConnServer, ibDemo;
    private String ch1Value, ch2Value, ch3Value, ch4Value;
    private ImageView ivConn;
    private TextView tvConn, tvNowCCT, tvNowLux;
    private boolean ledPower = false;
    private boolean bleConnect = true;
    private boolean isConn = false, demoIsOn=false;
    private boolean canCustomChange =true;
    private String SERIAL_NUMBER;
    private int demo_idx;

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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getSupportActionBar().hide();

        final Intent intent = getIntent();

        System.out.println(mDeviceName + "  " + mDeviceAddress);

        //if power button is pressed, change image and content
        power_button = (ImageButton) findViewById(R.id.btn_onoff);
        content_power = (TextView) findViewById(R.id.content_power);

        //if seekbar is moved, modify seekbar content and concurrent status content
        seekbar_cct = (SeekBar) findViewById(R.id.seekbar_cct);
        seekbar_lux = (SeekBar) findViewById(R.id.seekbar_lux);
        setting_content_cct = (TextView) findViewById(R.id.tv_cct);
        setting_content_lux = (TextView) findViewById(R.id.tv_lux);

        ibOnOff = findViewById(R.id.btn_onoff);
        ibBluetooth = findViewById(R.id.btn_bluetooth);
        ibConnServer = findViewById(R.id.ib_nl_conn);
        ch1Value = ch2Value = ch3Value = ch4Value = "00";
        ivConn = findViewById(R.id.img_connect);
        ibDemo = findViewById(R.id.ib_demo);
        tvConn = findViewById(R.id.text_connect);
        tvNowCCT = findViewById(R.id.content_cct);
        tvNowLux = findViewById(R.id.content_lux);
        serialNumber = "kongjuWitlab01";

//        getSupportActionBar().setTitle(mDeviceName);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


        ibConnServer.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                ibOnOff.setEnabled(false);
                ibDemo.setEnabled(false);
                seekbar_cct.setEnabled(false);
                seekbar_lux.setEnabled(false);
//                ibConnServer.setImageResource(R.drawable.on_nl);
                ibOnOff.setImageResource(R.drawable.ic_power_on);
                content_power.setText("ON");
                ledPower = true;
                if (!isConn) {
                    isConn = true;
                    new Thread() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                        @Override
                        public void run() {
                            while (isConn) {
                                try {
                                    URL url = new URL("http://210.102.142.15:8088/real_time_natural_light/");
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                                    connection.setRequestMethod("POST");
                                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                                    outputStream.writeBytes("serialNumber="+serialNumber);
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

                                    System.out.println("SERVER FROM : " +response);
                                    String split_res[] = response.split("&");
                                    if (split_res[0].equals("NOT")){
                                        return;
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (split_res[0] != null) {
                                                int setting_cct=Integer.parseInt(split_res[0]);
                                                int setting_lux=Integer.parseInt(split_res[1]);
                                                tvNowCCT.setText((setting_cct) + "");
                                                tvNowLux.setText((setting_lux) + "");
                                            }
                                        }
                                    });
                                    if (split_res[2].equals("0")) {
                                        ch1Value="00";
                                    } else {
                                        ch1Value=Integer.toHexString(Integer.parseInt(split_res[2]));
                                    }
                                    if (split_res[3].equals("0")) {
                                        ch2Value="00";
                                    } else {
                                        ch2Value=Integer.toHexString(Integer.parseInt(split_res[3]));
                                    }
                                    if (split_res[4].equals("0")) {
                                        ch3Value="00";
                                    } else {
                                        ch3Value=Integer.toHexString(Integer.parseInt(split_res[4]));
                                    }
                                    if (split_res[5].equals("0")) {
                                        ch4Value="00";
                                    } else {
                                        ch4Value=Integer.toHexString(Integer.parseInt(split_res[5]));
                                    }
                                    String packet = "0211FF" + ch1Value + ch2Value + ch3Value + ch4Value + "03";
                                    System.out.println("packet : "+packet);
                                    byte[] data = hexStringToByteArray(packet);
                                    mBluetoothLeService.writeCustomCharacteristic(data);
                                    Thread.sleep(5000);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }.start();
                } else {
                    isConn = false;
                    ibOnOff.setEnabled(true);
                    ibDemo.setEnabled(true);
                    seekbar_cct.setEnabled(true);
                    seekbar_lux.setEnabled(true);
//                    ibConnServer.setImageResource(R.drawable.sun_rise_set);
                    ibOnOff.setImageResource(R.drawable.ic_power_off);
//                    content_power.setText("OFF");
//                    ledPower = false;
//                    new Thread() {
//                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
//                        @Override
//                        public void run() {
//                            try {
//                                URL url = new URL("http://210.102.142.15:8088/power_off/");
//                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//                                connection.setRequestMethod("POST");
//                                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
//                                outputStream.writeBytes("serialNumber="+serialNumber);
//                                outputStream.flush();
//                                outputStream.close();
//
//                                int responseCode = connection.getResponseCode();
//
//                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                                StringBuffer stringBuffer = new StringBuffer();
//                                String inputLine;
//
//                                while ((inputLine = bufferedReader.readLine()) != null) {
//                                    stringBuffer.append(inputLine);
//                                }
//                                bufferedReader.close();
//
//                                String response = stringBuffer.toString();
//                                System.out.println("SERVER FROM : " +response);
//
//                                String split_res[] = response.split("&");
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        if (split_res[0] != null) {
//                                            int setting_cct=Integer.parseInt(split_res[0]);
//                                            int setting_lux=Integer.parseInt(split_res[1]);
//                                            tvNowCCT.setText((setting_cct) + "");
//                                            tvNowLux.setText((setting_lux) + "");
//                                        }
//                                    }
//                                });
//                                String packet = "0211FF0000000003";
//                                byte[] data = hexStringToByteArray(packet);
//                                mBluetoothLeService.writeCustomCharacteristic(data);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }.start();
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
                    ibOnOff.setImageResource(R.drawable.ic_power_on);
                    content_power.setText("ON");
                    ledPower = true;
                    new Thread() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                        @Override
                        public void run() {
                            try {
                                URL url = new URL("http://210.102.142.15:8088/power_on/"+cct_tmp+"/"+lux_tmp);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                                connection.setRequestMethod("POST");
                                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                                outputStream.writeBytes("serialNumber="+serialNumber);
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
                                System.out.println("SERVER FROM : " +response);

                                String split_res[] = response.split("&");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (split_res[0] != null) {
                                            int setting_cct=Integer.parseInt(split_res[0]);
                                            int setting_lux=Integer.parseInt(split_res[1]);
                                            tvNowCCT.setText((setting_cct) + "");
                                            tvNowLux.setText((setting_lux) + "");
                                        }
                                    }
                                });
                                String packet = "0211FF" + ch1Value + ch2Value + ch3Value + ch4Value + "03";
                                byte[] data = hexStringToByteArray(packet);
                                mBluetoothLeService.writeCustomCharacteristic(data);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    Toast.makeText(v.getContext(), "power on", Toast.LENGTH_SHORT).show();
                } else {
                    ibOnOff.setImageResource(R.drawable.ic_power_off);
                    content_power.setText("OFF");
                    ledPower = false;
                    new Thread() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                        @Override
                        public void run() {
                            try {
                                URL url = new URL("http://210.102.142.15:8088/power_off/");
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                                connection.setRequestMethod("POST");
                                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                                outputStream.writeBytes("serialNumber="+serialNumber);
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
                                System.out.println("SERVER FROM : " +response);

                                String split_res[] = response.split("&");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (split_res[0] != null) {
                                            int setting_cct=Integer.parseInt(split_res[0]);
                                            int setting_lux=Integer.parseInt(split_res[1]);
                                            cct_tmp = Integer.parseInt(tvNowCCT.getText()+"");
                                            lux_tmp = Integer.parseInt(tvNowLux.getText()+"");
                                            tvNowCCT.setText((setting_cct) + "");
                                            tvNowLux.setText((setting_lux) + "");
                                        }
                                    }
                                });
                                String packet = "0211FF0000000003";
                                byte[] data = hexStringToByteArray(packet);
                                mBluetoothLeService.writeCustomCharacteristic(data);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                    Toast.makeText(v.getContext(), "power off", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ibDemo.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ibOnOff.setEnabled(false);
                        ibConnServer.setEnabled(false);
                        seekbar_cct.setEnabled(false);
                        seekbar_lux.setEnabled(false);
                        ibOnOff.setImageResource(R.drawable.ic_power_on);
                        content_power.setText("ON");
                        ledPower = true;
                    }
                });
                System.out.println("DEMO :"+demoIsOn);
                if (!demoIsOn) {
                    demoIsOn = true;
                    demo_idx=-1;
                    new Thread() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                        @Override
                        public void run() {
                            while (demoIsOn) {
                                try {
                                    URL url = new URL("http://210.102.142.15:8088/natural_light_demonstration/"+demo_idx);
                                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                                    connection.setRequestMethod("POST");
                                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                                    outputStream.writeBytes("serialNumber="+serialNumber);
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
                                    System.out.println("SERVER FROM : " +response);

                                    String split_res[] = response.split("&");
                                    if(split_res[0].equals("demonstration")){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                demoIsOn=false;
                                                ibOnOff.setEnabled(true);
                                                ibConnServer.setEnabled(true);
                                                seekbar_cct.setEnabled(true);
                                                seekbar_lux.setEnabled(true);
                                            }
                                        });
                                        break;
                                    }
                                    if (split_res[0].equals("NOT")){
                                        return;
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (split_res[0] != null) {
                                                int setting_cct=Integer.parseInt(split_res[0]);
                                                int setting_lux=Integer.parseInt(split_res[1]);
                                                tvNowCCT.setText((setting_cct) + "");
                                                tvNowLux.setText((setting_lux) + "");
                                            }
                                        }
                                    });
                                    if (split_res[2].equals("0")) {
                                        ch1Value="00";
                                    } else {
                                        ch1Value=Integer.toHexString(Integer.parseInt(split_res[2]));
                                    }
                                    if (split_res[3].equals("0")) {
                                        ch2Value="00";
                                    } else {
                                        ch2Value=Integer.toHexString(Integer.parseInt(split_res[3]));
                                    }
                                    if (split_res[4].equals("0")) {
                                        ch3Value="00";
                                    } else {
                                        ch3Value=Integer.toHexString(Integer.parseInt(split_res[4]));
                                    }
                                    if (split_res[5].equals("0")) {
                                        ch4Value="00";
                                    } else {
                                        ch4Value=Integer.toHexString(Integer.parseInt(split_res[5]));
                                    }
                                    String packet = "0211FF" + ch1Value + ch2Value + ch3Value + ch4Value + "03";
                                    System.out.println("packet : "+packet);
                                    byte[] data = hexStringToByteArray(packet);
                                    mBluetoothLeService.writeCustomCharacteristic(data);
                                    demo_idx++;
                                    Thread.sleep(3000);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }.start();
                } else {
                    demo_idx=-1;
                    demoIsOn=false;
                    ibOnOff.setEnabled(true);
                    ibConnServer.setEnabled(true);
                    seekbar_cct.setEnabled(true);
                    seekbar_lux.setEnabled(true);
                }
            }
        });

        /////////////////////////////////////////////////////////////////////////////////////////////

        seekbar_cct.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(canCustomChange){
                    seekBar.setProgress(progress);
                    System.out.println("PLUSING CCT");
                    setting_content_cct.setText(String.format("%d", (cct_sk_min+seekBar.getProgress())));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                setting_content_cct.setText(String.format("%d", (cct_sk_min+seekBar.getProgress())));
                seekCCT=cct_sk_min+seekbar_cct.getProgress();
                seekLux=lux_sk_min+seekbar_lux.getProgress();
                sendCustomSet(seekCCT, seekLux);
            }
        });

        seekbar_lux.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(canCustomChange){
                    seekBar.setProgress(progress);
                    System.out.println("PLUSING LUX");
                    setting_content_lux.setText(String.format("%d", (lux_sk_min+seekBar.getProgress())));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                setting_content_lux.setText(String.format("%d", (lux_sk_min+seekBar.getProgress())));
                seekCCT=cct_sk_min+seekbar_cct.getProgress();
                seekLux=lux_sk_min+seekbar_lux.getProgress();
                sendCustomSet(seekCCT, seekLux);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void sendCustomSet(int cct, int lux){
        System.out.println("custom send "+cct+", "+lux);
        System.out.println("custom packet="+"serialNumber=kongjuWitlab01&cct="+cct+"&lux="+lux);

        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void run() {
                try {
                    canCustomChange=false;
                    URL url = new URL("http://210.102.142.15:8088/kofac_customSet/");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    outputStream.writeBytes("serialNumber=kongjuWitlab01&cct="+cct+"&lux="+lux);
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

                    System.out.println("SERVER:"+response);

                    String split_res[] = response.split("&");
                    if (split_res[0].equals("NOT")){
                        return;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (split_res[0] != null) {
                                int setting_cct=Integer.parseInt(split_res[0]);
                                int setting_lux=Integer.parseInt(split_res[1]);
                                System.out.println("SERVER FROM : " + split_res[0] + "K, " + split_res[1] + "Lux");
                                setting_content_cct.setText((setting_cct) + "");
                                seekbar_cct.setProgress(Integer.parseInt(split_res[0])-cct_sk_min);
                                setting_content_lux.setText((setting_lux) + "");
                                seekbar_lux.setProgress(Integer.parseInt(split_res[1])-lux_sk_min);
                                ibOnOff.setImageResource(R.drawable.ic_power_on);
                                content_power.setText("ON");
                                ledPower = true;

                                tvNowCCT.setText((setting_cct) + "");
                                tvNowLux.setText((setting_lux) + "");
                            }
                        }
                    });
                    if (split_res[2].equals("0")) {
                        ch1Value="00";
                    } else {
                        ch1Value=Integer.toHexString(Integer.parseInt(split_res[2]));
                    }
                    if (split_res[3].equals("0")) {
                        ch2Value="00";
                    } else {
                        ch2Value=Integer.toHexString(Integer.parseInt(split_res[3]));
                    }
                    if (split_res[4].equals("0")) {
                        ch3Value="00";
                    } else {
                        ch3Value=Integer.toHexString(Integer.parseInt(split_res[4]));
                    }
                    if (split_res[5].equals("0")) {
                        ch4Value="00";
                    } else {
                        ch4Value=Integer.toHexString(Integer.parseInt(split_res[5]));
                    }
                    String packet = "0211FF" + ch1Value + ch2Value + ch3Value + ch4Value + "03";
                    System.out.println("packet : "+packet);
                    byte[] data = hexStringToByteArray(packet);
                    mBluetoothLeService.writeCustomCharacteristic(data);
                    Thread.sleep(200);
                    canCustomChange=true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
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
                        serialNumber = intent.getStringExtra("SerialNumber");
                        ibBluetooth.setEnabled(false);
                        ibConnServer.setEnabled(true);
                        ibOnOff.setEnabled(true);
                        ibDemo.setEnabled(true);


                        Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);
                        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

                        String packet = "0211FF0000000003";
                        byte[] data = hexStringToByteArray(packet);
                        mBluetoothLeService.writeCustomCharacteristic(data);
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