/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.opencsv.CSVWriter;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends FragmentActivity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_RSSI = "DEVICE_RSSI";

    public static String connected = "연결 안됨";
    public static int battery = 100;

    private String mDeviceAddress;
    private String mDeviceName;
    private int mDeviceRssi;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private ExpandableListView exerciseListView;
    private ScrollView exerciseRecordView;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private String[] exerciseNameKo = {"신전 운동", "전방 굴곡 운동", "내회전 운동", "외회전 운동", "주먹 지르기 운동", "끌어당기기 운동"};
    private String[] exerciseNameEn = {"Extension Exercise", "Flexion Exercise", "Internal Rotation Exercise", "External Rotation Exercise", "Punching Exercise", "Pulling Exercise"};

    private MediaPlayer mPlayer;
    private boolean startCounting = false;

    private short rawAx = 0;
    private short rawAy = 0;
    private short rawAz = 0;
    private float datAx = 0;
    private float datAy = 0;
    private float datAz = 0;
    private float preAx = 0;
    private float preAy = 0;
    private float preAz = 0;
    private short rawMx = 0;
    private short rawMy = 0;
    private short rawMz = 0;
    private float datMx = 0;
    private float datMy = 0;
    private float datMz = 0;

    private String state = "";
    private int count = 0;
    private int showCount = 0;
    private int reconnectCounter =0;
    public static Handler dataHandler;

    private int[] voiceArray = {R.raw.voice1, R.raw.voice2, R.raw.voice3, R.raw.voice4, R.raw.voice5};


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            mBluetoothLeService.setReadStarted(false);
            count = 0;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
                connected = "연결됨";
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "디바이스에 연결되었습니다", Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
                connected = "연결 안됨";
                DeviceControlActivity.writeToCSV(new String[]{"DISCONNECTED"});
                Log.e(TAG, "Device disconnected");
                if(reconnectCounter < 5) {
                    reconnectCounter++;
                    Intent gattServiceIntent = new Intent(mContext, BluetoothLeService.class);
                    bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            reconnectCounter = 0;
                            createAlert(context);
                        }
                    });
                }
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                if(intent.getByteArrayExtra("DATA") != null) {
                    sensorDataParsing(intent.getByteArrayExtra("DATA"));
                }
                showCount++;
            }
        }
    };
    private int exerciseNo = -1;

    private void startCollectingData()
    {
        if (mBluetoothLeService != null && mGattCharacteristics != null && !mBluetoothLeService.isReadStarted()) {
            BluetoothGattService snuReporterService = mBluetoothLeService.getmBluetoothGatt().getService(UUID.fromString(SampleGattAttributes.SNU_REPORTER_SERVICE));
            final BluetoothGattCharacteristic characteristic = snuReporterService.getCharacteristic(UUID.fromString(SampleGattAttributes.GATT_SENSOR_DATA_READ_UUID));
            mBluetoothLeService.setCharacteristicNotification(characteristic, true);
        } else if(mGattCharacteristics != null) {
            mBluetoothLeService.setReadStarted(false);
            count = 0;
        }
    }

    private void clearUI() {
        count = 0;
        showCount = 0;
//        exerciseNo = -1;
        BluetoothGattService snuReporterService = mBluetoothLeService.getmBluetoothGatt().getService(UUID.fromString(SampleGattAttributes.SNU_REPORTER_SERVICE));
        if(snuReporterService != null) {
            final BluetoothGattCharacteristic characteristic = snuReporterService.getCharacteristic(UUID.fromString(SampleGattAttributes.GATT_SENSOR_DATA_READ_UUID));
            mBluetoothLeService.setCharacteristicNotification(characteristic, false);
        }
        mBluetoothLeService.setReadStarted(false);
    }

    private void createAlert(final Context context) {
        new AlertDialog.Builder(context)
                .setMessage("연결에 실패하였습니다, 재시도 하시겠습니까?")
                .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onBackPressed();
                    }
                })
                .setPositiveButton("재시도", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBluetoothLeService.connect(mDeviceAddress);
                    }
                })
                .create().show();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exercise_layout);
        mContext = this;
        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mDeviceRssi = intent.getIntExtra(EXTRAS_DEVICE_RSSI, 0);
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        battery = Integer.parseInt(SharedPreferenceInfo.getData(mContext, "battery"));
        info = SharedPreferenceInfo.loadExerciseData(mContext);
        ((TextView)findViewById(R.id.title)).setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Roboto-Medium.ttf"));

        exerciseListView = (ExpandableListView) findViewById(R.id.exercise_list);
        exerciseListView.setDividerHeight(0);
        setExerciseItems();
        final Button exerciseBtn = (Button)findViewById(R.id.exercise_btn);
        exerciseBtn.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "NotoSansCJKkr-Regular.otf"));
        final Button recordBtn = (Button)findViewById(R.id.record_btn);
        recordBtn.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "NotoSansCJKkr-Regular.otf"));
        exerciseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exerciseBtn.setBackground(getResources().getDrawable(R.drawable.tab_selected));
                recordBtn.setBackground(getResources().getDrawable(R.drawable.tab_not_selected));
                exerciseListView.setVisibility(View.VISIBLE);
                exerciseRecordView.setVisibility(View.GONE);
            }
        });
        exerciseRecordView = (ScrollView) findViewById(R.id.excercise_record);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exerciseBtn.setBackground(getResources().getDrawable(R.drawable.tab_not_selected));
                recordBtn.setBackground(getResources().getDrawable(R.drawable.tab_selected));
                exerciseListView.setVisibility(View.GONE);
                exerciseRecordView.setVisibility(View.VISIBLE);
                setExerciseInfo(new Date());
                setupCalendar();
            }
        });
        final Activity act = this;
        findViewById(R.id.settingsBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsFragment settingsFragment = new SettingsFragment();
                FragmentManager fm = act.getFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.add(android.R.id.content, settingsFragment).commit();
            }
        });
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public void onBackPressed() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                mConnectionState.setText(resourceId);
            }
        });
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
                if(currentCharaData.get(LIST_UUID).equals(SampleGattAttributes.GATT_SENSOR_DATA_READ_UUID)) {
                }
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }
    static ArrayList<ExerciseInfoByDay> info;
    ExerciseInfo exerciseInfo;
    Context mContext;
    private void setExerciseItems()
    {
        List<Exercise> exercises = new ArrayList<>();
        for(int i = 0; i < exerciseNameKo.length; i++) {
            Exercise exercise = new Exercise();
            exercise.setNameKo(exerciseNameKo[i]);
            exercise.setNameEn(exerciseNameEn[i]);
            exercises.add(exercise);
        }
        ExerciseListAdapter adapter = new ExerciseListAdapter(this, exercises, new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                int value = ((Integer)msg.obj).intValue();
                if(value >= 0) {
                    exerciseNo = value;
                    ExerciseInfoByDay data;
                    if(info.size() > 0) {
                        data = info.get(info.size() - 1);
                        Calendar cal = Calendar.getInstance();
                        if(data.getExerciseDate().day == cal.get(Calendar.DAY_OF_MONTH)
                            && data.getExerciseDate().month == cal.get(Calendar.MONTH)) {
                            exerciseInfo = null;
                            if(data.getExcerciseInfo().size() > 0) {
                                for(int i = 0; i < data.getExcerciseInfo().size(); i++) {
                                    if(data.getExcerciseInfo().get(i).getExerciseNo() == exerciseNo) {
                                        exerciseInfo = data.getExcerciseInfo().get(i);
                                        break;
                                    }
                                }
                            }
                            if(exerciseInfo == null) {
                                data.getExcerciseInfo().add(new ExerciseInfo());
                                exerciseInfo = data.getExcerciseInfo().get(data.getExcerciseInfo().size()-1);
                            }
                        } else {
                            info.add(new ExerciseInfoByDay(mContext));
                            data = info.get(info.size() - 1);
                            data.setExerciseDate();
                            data.getExcerciseInfo().add(new ExerciseInfo());
                            exerciseInfo = data.getExcerciseInfo().get(0);
                        }
                    } else {
                        info.add(new ExerciseInfoByDay(mContext));
                        data = info.get(info.size() - 1);
                        data.setExerciseDate();
                        data.getExcerciseInfo().add(new ExerciseInfo());
                        exerciseInfo = data.getExcerciseInfo().get(0);
                    }
                    exerciseInfo.setExerciseNo(exerciseNo);
                    exerciseInfo.setBandType(UserInfo.getBandType(mContext));
                    exerciseInfo.getTrialInfo().add(new TrialInfo());
                    mPlayer = MediaPlayer.create(mContext, getExerciseMediaFileId());
                    mPlayer.start();
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            startCounting = true;
                            exerciseGuide();
                            startCollectingData();
                        }
                    });
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                }
                else {
                    mPlayer.stop();
                    startCounting = false;
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    UserInfo.setLastExerciseDate(mContext);
                    SharedPreferenceInfo.saveExerciseData(mContext, info);
                    SharedPreferenceInfo.saveData(mContext, "battery", String.valueOf(battery));
                    exerciseListView.collapseGroup(exerciseNo);
                    clearUI();
                }
                return true;
            }
        }), this);
        exerciseListView.setAdapter(adapter);
        exerciseListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if(mBluetoothLeService == null)
                {
                    Log.e(TAG, "Device disconnected");
                    if(reconnectCounter < 5) {
                        reconnectCounter++;
                        Intent gattServiceIntent = new Intent(mContext, BluetoothLeService.class);
                        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                    } else {
                        reconnectCounter = 0;
                        createAlert(getApplicationContext());
                    }
                } else {
                    if(exerciseNo < 0) {
                        if(parent.isGroupExpanded(groupPosition)) {
                            parent.collapseGroup(groupPosition);
                        }
                        else {
                            parent.expandGroup(groupPosition, true);
                        }
                    }
                }
                return true;
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

    void sensorDataParsing(byte[] bytes) {
        final byte[] data = bytes;
        if ((data[4] + data[5] + data[6] + data[7] + data[8] + data[9]) != 0) {
            preAx = datAx;
            preAy = datAy;
            preAz = datAz;

            rawAx = (short)(((data[5] & 0xFF) << 8) | (data[4] & 0xFF));
            rawAy = (short)(((data[7] & 0xFF) << 8) | (data[6] & 0xFF));
            rawAz = (short)(((data[9] & 0xFF) << 8) | (data[8] & 0xFF));

            if (rawAx > 8191) rawAx = 8191;
            else if (rawAx < -8192) rawAx = -8192;
            if (rawAy > 8191) rawAy = 8191;
            else if (rawAy < -8192) rawAy = -8192;
            if (rawAz > 8191) rawAz = 8191;
            else if (rawAz < -8192) rawAz = -8192;

            float tempAx = (float)rawAx / 1024.0f;
            float tempAy = (float)rawAy / 1024.0f;
            float tempAz = (float)rawAz / 1024.0f;

            tempAx *= 10.0f;
            tempAy *= 10.0f;
            tempAz *= 10.0f;
				/*
				tempAx = Math.round(tempAx);
				tempAy = Math.round(tempAy);
				tempAz = Math.round(tempAz);
				*/
            tempAx /= 10.0f;
            tempAy /= 10.0f;
            tempAz /= 10.0f;

            if (tempAx == 0) tempAx = 0;
            if (tempAy == 0) tempAy = 0;
            if (tempAz == 0) tempAz = 0;

            datAx = tempAx;
            datAy = tempAy;
            datAz = tempAz;

            rawMx = (short)(((data[11] & 0xFF) << 8) | (data[10]) & 0xFF);
            rawMy = (short)(((data[13] & 0xFF) << 8) | (data[12]) & 0xFF);
            rawMz = (short)(((data[15] & 0xFF) << 8) | (data[14]) & 0xFF);
            if (rawMx > 16000) rawMx = 16000;
            else if (rawMx < -16000) rawMx = -16000;
            if (rawMy > 16000) rawMy = 16000;
            else if (rawMy < -16000) rawMy = -16000;
            if (rawMz > 16000) rawMz = 16000;
            else if (rawMz < -16000) rawMz = -16000;

            float tempMx = (rawMx * 0.15f);
            float tempMy = (rawMy * 0.15f);
            float tempMz = (rawMz * 0.15f);

//            tempMx = Math.round(tempMx);
//            tempMy = Math.round(tempMy);
//            tempMz = Math.round(tempMz);


            if (tempMx == 0) tempMx = 0;
            if (tempMy == 0) tempMy = 0;
            if (tempMz == 0) tempMz = 0;

            datMx = tempMx;
            datMy = tempMy;
            datMz = tempMz;

            battery = data[16];

            if(exerciseNo > -1 && startCounting) {
                SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss");
                String date = s.format(new Date());
                processData();
                String[] dataToArray = new String[]{
                        date, String.valueOf(datAx), String.valueOf(datAy),
                        String.valueOf(datAz), String.valueOf(datMx),
                        String.valueOf(datMy), String.valueOf(datMz),
                        String.valueOf(exerciseNo+1), String.valueOf(count / 2),
                        String.valueOf(mDeviceRssi), String.valueOf(battery),
                        mDeviceName
                };
                DeviceControlActivity.writeToCSV(dataToArray);
                Network.sendPostExerciseData(mContext, dataToArray);
            }
        }
    }

    private void processData() {
        reconnectCounter = 0;
        float thresholdReady = 0.2f;
//        Log.e(TAG, "DataX: "+datAx+" DataY: "+datAy+" DataZ: "+datAz);
        Log.e(TAG, "DatmX: "+datMx+" DatmY: "+datMy+" DatmZ: "+datMz);
        if(Math.abs(preAx - datAx) < thresholdReady && Math.abs(preAy - datAy) < thresholdReady && Math.abs(preAz - datAz) < thresholdReady)
        {
            state = "holding";
        }
        else
        {
            if(state == "holding") {
                state = "moving";
                count += 1;
                Message msg = new Message();
                msg.obj = new Integer(count/2);
//                dataHandler.sendMessage(msg);
                exerciseInfo.getTrialInfo().get(exerciseInfo.getTrialInfo().size()-1).setExerciseCount(count / 2);
                if(count%2 == 0) {
//                    mPlayer.stop();
//                    mPlayer = MediaPlayer.create(mContext, R.raw.exercise_method);
//                    mPlayer.start();
                }
            }
        }
    }

    private int getExerciseMediaFileId() {
        switch (exerciseNo) {
            case 0:
                return R.raw.exercise1;
            case 1:
                return R.raw.exercise2;
            case 2:
                return R.raw.exercise3;
            case 3:
                return R.raw.exercise4;
            case 4:
                return R.raw.exercise5;
            case 5:
                return R.raw.exercise6;
            default:
                return R.raw.exercise1;
        }
    }

    public static void writeToCSV(String[] data)
    {
        FileWriter mFileWriter = null;
        CSVWriter writer = null;
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = "WookfitData.csv";
        String filePath = baseDir + File.separator + fileName;
        File file = new File(filePath);

        if(file.exists() && !file.isDirectory()) {
            try {
                mFileWriter = new FileWriter(filePath, true);
                writer = new CSVWriter(mFileWriter);
                writer.writeNext(data);

                writer.close();
            }

            catch (IOException e)
            {
                //error
            }
        }
        else {
            try {
                writer = new CSVWriter(new FileWriter(filePath));
                writer.writeNext(new String[]{"Time", "Acc_X", "Acc_Y", "Acc_Z",
                        "Mag_X", "Mag_Y", "Mag_Z", "Exe_Type", "Basic Algorithm Count",
                        "RSSI", "Battery", "DeviceName"});
                writer.close();
            }

            catch (IOException e)
            {
                //error
            }
        }

    }


    CaldroidFragment caldroidFragment;
    Date selectedDate;
    private void setCustomResourceForDates() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 0);
        selectedDate = cal.getTime();

        if (caldroidFragment != null) {
            for(int i = 0; i < info.size(); i++) {
                ExerciseInfoByDay.ExerciseDate date = info.get(i).getExerciseDate();
                cal = Calendar.getInstance();
                cal.set(date.year, date.month, date.day);
                Date exercisedDate = cal.getTime();
                caldroidFragment.setBackgroundResourceForDate(R.drawable.blue_circle,
                        exercisedDate);
            }
            caldroidFragment.setBackgroundResourceForDate(R.drawable.orange_circle,
                    selectedDate);
        }
    }

    private void setupCalendar() {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");

        caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
        args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, false);

        caldroidFragment.setArguments(args);

        setCustomResourceForDates();

        // Attach to the activity
        android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendar, caldroidFragment);
        t.commit();

        // Setup listener
        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
                caldroidFragment.setBackgroundResourceForDate(R.drawable.orange_circle, date);
                caldroidFragment.setBackgroundResourceForDate(R.color.caldroid_white, selectedDate);
                for(int i = 0; i < info.size(); i++) {
                    ExerciseInfoByDay.ExerciseDate exerDate = info.get(i).getExerciseDate();
                    Calendar cal = Calendar.getInstance();
                    cal.set(exerDate.year, exerDate.month, exerDate.day);
                    Date exercisedDate = cal.getTime();
                    caldroidFragment.setBackgroundResourceForDate(R.drawable.blue_circle,
                            exercisedDate);
                }
                setExerciseInfo(date);
                selectedDate = date;
                caldroidFragment.setBackgroundResourceForDate(R.drawable.orange_circle, selectedDate);
                caldroidFragment.refreshView();
            }


        };

        // Setup Caldroid
        caldroidFragment.setCaldroidListener(listener);
    }

    private int calculateAverage() {
        int total = 0;
        int days = 0;
        for(int i = 0; i < info.size(); i++) {
            ExerciseInfoByDay exerciseInfoByDay = info.get(i);
            for(int j = 0; j < exerciseInfoByDay.getExcerciseInfo().size(); j++) {
                ExerciseInfo exerciseInfo = exerciseInfoByDay.getExcerciseInfo().get(j);
                for(int k = 0; k < exerciseInfo.getTrialInfo().size(); k++) {
                    TrialInfo trialInfo = exerciseInfo.getTrialInfo().get(k);
                    total += trialInfo.getExerciseCount();
                }
            }
            days++;
        }
        return total/days;
    }

    private void setExerciseInfo(Date date) {
        resetLayout();

        ExerciseInfoByDay exerciseInfo = null;
        for(int i = 0; i < info.size(); i++) {
            exerciseInfo = info.get(i);
            ExerciseInfoByDay.ExerciseDate exerciseDate = exerciseInfo.getExerciseDate();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            if(!(exerciseDate.year == cal.get(Calendar.YEAR) && exerciseDate.month == cal.get(Calendar.MONTH) && exerciseDate.day == cal.get(Calendar.DAY_OF_MONTH)))
                exerciseInfo = null;
            else
                break;
        }
        ((TextView)findViewById(R.id.average_count)).setText(calculateAverage()+"회");

        if(exerciseInfo != null) {
            for (int i = 0; i < exerciseInfo.getExcerciseInfo().size(); i++) {
                ExerciseInfo exercise = exerciseInfo.getExcerciseInfo().get(i);
                int total = 0;
                int count = 0;

                ((TextView)findViewById(R.id.bandType)).setText(loadBandType(exercise.getBandType()));

                switch (exercise.getExerciseNo()) {
                    case 0:
                        if (exercise.getTrialInfo().size() > 0) {
                            count = exercise.getTrialInfo().get(0).getExerciseCount();
                            findViewById(R.id.exercise1_trial1_section).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, count));
                            total += count;
                        }
                        if (exercise.getTrialInfo().size() > 1) {
                            count = exercise.getTrialInfo().get(1).getExerciseCount();
                            findViewById(R.id.exercise1_trial2_section).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, count));
                            total += count;
                        }
                        if (exercise.getTrialInfo().size() > 2) {
                            count = exercise.getTrialInfo().get(2).getExerciseCount();
                            findViewById(R.id.exercise1_trial3_section).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, count));
                            total += count;
                        }
                        if (total > 30)
                            findViewById(R.id.exercise1_trial4).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        else
                            findViewById(R.id.exercise1_trial4).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 30 - count));
                        ((TextView) findViewById(R.id.exercise1_total)).setText(total + "회");
                        break;
                    case 1:
                        if (exercise.getTrialInfo().size() > 0) {
                            count = exercise.getTrialInfo().get(0).getExerciseCount();
                            findViewById(R.id.exercise2_trial1_section).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, count));
                            total += count;
                        }
                        if (exercise.getTrialInfo().size() > 1) {
                            count = exercise.getTrialInfo().get(1).getExerciseCount();
                            findViewById(R.id.exercise2_trial2_section).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, count));
                            total += count;
                        }
                        if (exercise.getTrialInfo().size() > 2) {
                            count = exercise.getTrialInfo().get(2).getExerciseCount();
                            findViewById(R.id.exercise2_trial3_section).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, count));
                            total += count;
                        }
                        if (total > 30)
                            findViewById(R.id.exercise2_trial4).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        else
                            findViewById(R.id.exercise2_trial4).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 30 - count));
                        ((TextView) findViewById(R.id.exercise2_total)).setText(total + "회");
                        break;
                    case 2:
                        if (exercise.getTrialInfo().size() > 0) {
                            count = exercise.getTrialInfo().get(0).getExerciseCount();
                            findViewById(R.id.exercise3_trial1_section).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, count));
                            total += count;
                        }
                        if (exercise.getTrialInfo().size() > 1) {
                            count = exercise.getTrialInfo().get(1).getExerciseCount();
                            findViewById(R.id.exercise3_trial2_section).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, count));
                            total += count;
                        }
                        if (exercise.getTrialInfo().size() > 2) {
                            count = exercise.getTrialInfo().get(2).getExerciseCount();
                            findViewById(R.id.exercise3_trial3_section).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, count));
                            total += count;
                        }
                        if (total > 30)
                            findViewById(R.id.exercise3_trial4).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        else
                            findViewById(R.id.exercise3_trial4).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 30 - count));
                        ((TextView) findViewById(R.id.exercise3_total)).setText(total + "회");
                        break;
                    case 3:
                        if (exercise.getTrialInfo().size() > 0) {
                            count = exercise.getTrialInfo().get(0).getExerciseCount();
                            findViewById(R.id.exercise4_trial1_section).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, count));
                            total += count;
                        }
                        if (exercise.getTrialInfo().size() > 1) {
                            count = exercise.getTrialInfo().get(1).getExerciseCount();
                            findViewById(R.id.exercise4_trial2_section).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, count));
                            total += count;
                        }
                        if (exercise.getTrialInfo().size() > 2) {
                            count = exercise.getTrialInfo().get(2).getExerciseCount();
                            findViewById(R.id.exercise4_trial3_section).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, count));
                            total += count;
                        }
                        if (total > 30)
                            findViewById(R.id.exercise4_trial4).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        else
                            findViewById(R.id.exercise4_trial4).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 30 - count));
                        ((TextView) findViewById(R.id.exercise4_total)).setText(total + "회");
                        break;
                    case 4:
                        if (exercise.getTrialInfo().size() > 0) {
                            count = exercise.getTrialInfo().get(0).getExerciseCount();
                            findViewById(R.id.exercise5_trial1_section).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, count));
                            total += count;
                        }
                        if (exercise.getTrialInfo().size() > 1) {
                            count = exercise.getTrialInfo().get(1).getExerciseCount();
                            findViewById(R.id.exercise5_trial2_section).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, count));
                            total += count;
                        }
                        if (exercise.getTrialInfo().size() > 2) {
                            count = exercise.getTrialInfo().get(2).getExerciseCount();
                            findViewById(R.id.exercise5_trial3_section).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, count));
                            total += count;
                        }
                        if (total > 30)
                            findViewById(R.id.exercise5_trial4).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        else
                            findViewById(R.id.exercise5_trial4).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 30 - count));
                        ((TextView) findViewById(R.id.exercise5_total)).setText(total + "회");
                        break;
                    case 5:
                        if (exercise.getTrialInfo().size() > 0) {
                            count = exercise.getTrialInfo().get(0).getExerciseCount();
                            findViewById(R.id.exercise6_trial1_section).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, count));
                            total += count;
                        }
                        if (exercise.getTrialInfo().size() > 1) {
                            count = exercise.getTrialInfo().get(1).getExerciseCount();
                            findViewById(R.id.exercise6_trial2_section).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, count));
                            total += count;
                        }
                        if (exercise.getTrialInfo().size() > 2) {
                            count = exercise.getTrialInfo().get(2).getExerciseCount();
                            findViewById(R.id.exercise6_trial3_section).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, count));
                            total += count;
                        }
                        if (total > 30)
                            findViewById(R.id.exercise6_trial4).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0));
                        else
                            findViewById(R.id.exercise6_trial4).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 30 - count));
                        ((TextView) findViewById(R.id.exercise6_total)).setText(total + "회");
                        break;
                }
            }
        }
    }

    private void resetLayout() {
        ((TextView)findViewById(R.id.bandType)).setText(loadBandType(-1));
        ((TextView)findViewById(R.id.average_count)).setText("0회");

        findViewById(R.id.exercise1_trial1_section).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise1_trial2_section).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise1_trial3_section).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise1_trial4).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise2_trial1_section).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise2_trial2_section).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise2_trial3_section).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise2_trial4).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise3_trial1_section).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise3_trial2_section).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise3_trial3_section).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise3_trial4).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise4_trial1_section).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise4_trial2_section).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise4_trial3_section).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise4_trial4).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise5_trial1_section).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise5_trial2_section).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise5_trial3_section).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise5_trial4).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise6_trial1_section).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise6_trial2_section).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise6_trial3_section).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));
        findViewById(R.id.exercise6_trial4).setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 0));

        ((TextView)findViewById(R.id.exercise1_total)).setText("0회");
        ((TextView)findViewById(R.id.exercise1_total))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Regular.otf"));
        ((TextView)findViewById(R.id.exercise2_total)).setText("0회");
        ((TextView)findViewById(R.id.exercise2_total))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Regular.otf"));
        ((TextView)findViewById(R.id.exercise3_total)).setText("0회");
        ((TextView)findViewById(R.id.exercise3_total))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Regular.otf"));
        ((TextView)findViewById(R.id.exercise4_total)).setText("0회");
        ((TextView)findViewById(R.id.exercise4_total))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Regular.otf"));
        ((TextView)findViewById(R.id.exercise5_total)).setText("0회");
        ((TextView)findViewById(R.id.exercise5_total))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Regular.otf"));
        ((TextView)findViewById(R.id.exercise6_total)).setText("0회");
        ((TextView)findViewById(R.id.exercise6_total))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Regular.otf"));
        ((TextView)findViewById(R.id.exercise_summary))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Medium.otf"));
        ((TextView)findViewById(R.id.bandColor))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Regular.otf"));
        ((TextView)findViewById(R.id.bandType))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Regular.otf"));
        ((TextView)findViewById(R.id.exercise_amount))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Medium.otf"));
        ((TextView)findViewById(R.id.average_count))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Regular.otf"));
        ((TextView)findViewById(R.id.average_exercise))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Regular.otf"));
        ((TextView)findViewById(R.id.exercise1))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Regular.otf"));
        ((TextView)findViewById(R.id.exercise2))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Regular.otf"));
        ((TextView)findViewById(R.id.exercise3))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Regular.otf"));
        ((TextView)findViewById(R.id.exercise4))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Regular.otf"));
        ((TextView)findViewById(R.id.exercise5))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Regular.otf"));
        ((TextView)findViewById(R.id.exercise6))
                .setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "NotoSansCJKkr-Regular.otf"));
    }

    private String loadBandType(int type) {
        switch (type) {
            case 1:
                return "노랑색 밴드";
            case 2:
                return "초록색 밴드";
            case 3:
                return "회색 밴드";
            case 4:
                return "파랑색 밴드";
            case 5:
                return "검정색 밴드";
            case 6:
                return "갈색 밴드";
            default:
                return "설정에서 입력";
        }
    }

    private int voiceCounter = 0;

    private void exerciseGuide() {
        mPlayer = MediaPlayer.create(mContext, R.raw.tut);
        mPlayer.start();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mPlayer = MediaPlayer.create(mContext, voiceArray[voiceCounter]);
                    mPlayer.start();
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            voiceCounter++;
                            if(voiceCounter < voiceArray.length) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        exerciseGuide();
                                    }
                                }, 2000);
                            }
                            else {
                                voiceCounter = 0;
                                startCounting = false;
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                                UserInfo.setLastExerciseDate(mContext);
                                SharedPreferenceInfo.saveExerciseData(mContext, info);
                                SharedPreferenceInfo.saveData(mContext, "battery", String.valueOf(battery));
                                exerciseListView.collapseGroup(exerciseNo);
                                clearUI();
                                exerciseNo++;
                                exercisePrepare();
                            }
                            Message msg = new Message();
                            msg.obj = voiceCounter;
                            dataHandler.sendMessage(msg);
                        }
                    });
                }
            }
        );
    }

    private void exercisePrepare() {
        if(exerciseNo > 0 && exerciseNo < 6) {
            ExerciseInfoByDay data;
            if (info.size() > 0) {
                data = info.get(info.size() - 1);
                Calendar cal = Calendar.getInstance();
                if (data.getExerciseDate().day == cal.get(Calendar.DAY_OF_MONTH)
                        && data.getExerciseDate().month == cal.get(Calendar.MONTH)) {
                    exerciseInfo = null;
                    if (data.getExcerciseInfo().size() > 0) {
                        for (int i = 0; i < data.getExcerciseInfo().size(); i++) {
                            if (data.getExcerciseInfo().get(i).getExerciseNo() == exerciseNo) {
                                exerciseInfo = data.getExcerciseInfo().get(i);
                                break;
                            }
                        }
                    }
                    if (exerciseInfo == null) {
                        data.getExcerciseInfo().add(new ExerciseInfo());
                        exerciseInfo = data.getExcerciseInfo().get(data.getExcerciseInfo().size() - 1);
                    }
                } else {
                    info.add(new ExerciseInfoByDay(mContext));
                    data = info.get(info.size() - 1);
                    data.setExerciseDate();
                    data.getExcerciseInfo().add(new ExerciseInfo());
                    exerciseInfo = data.getExcerciseInfo().get(0);
                }
            } else {
                info.add(new ExerciseInfoByDay(mContext));
                data = info.get(info.size() - 1);
                data.setExerciseDate();
                data.getExcerciseInfo().add(new ExerciseInfo());
                exerciseInfo = data.getExcerciseInfo().get(0);
            }
            exerciseInfo.setExerciseNo(exerciseNo);
            exerciseInfo.setBandType(UserInfo.getBandType(mContext));
            exerciseInfo.getTrialInfo().add(new TrialInfo());
            exerciseListView.expandGroup(exerciseNo);
            exerciseListView.setSelectedChild(exerciseNo, 0, true);
            mPlayer = MediaPlayer.create(mContext, getExerciseMediaFileId());
            mPlayer.start();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    startCounting = true;
                    exerciseGuide();
                    startCollectingData();
                }
            });
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            clearUI();
            exerciseNo = -1;
        }
    }

    public ExpandableListView getExerciseListView() {
        return exerciseListView;
    }
}
