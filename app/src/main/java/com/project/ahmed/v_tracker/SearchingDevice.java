package com.project.ahmed.v_tracker;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

import static com.project.ahmed.v_tracker.HomeActivity.mBluetoothAdapter;
import static com.project.ahmed.v_tracker.ConnectingThread.bluetoothSocket;

/**
 * Created by Ahmed on 11/17/2017.
 */

public class SearchingDevice extends AppCompatActivity {

    public static ArrayAdapter<String> adapter;
    public static BluetoothSocket temp;
    public static ConnectingThread connectingThread;
    private final int ENABLE_BT_REQUEST_CODE = 1;
    private final int DISCOVERABLE_BT_REQUEST_CODE = 2;
    private final int DISCOVERABLE_DURATION = 300; //discoverable duration in seconds
    private PulsatorLayout mPulsatorLayout;
    private TextView searchingTextView;
    private TextView pairedTextView;
    private ListView devicesListView;
    private ListView pairedDevicesListView;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                // Whenever a remote Bluetooth device is found
                case BluetoothDevice.ACTION_FOUND:

                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    adapter.add(device.getName() + "\n" + device.getAddress());
                    break;

                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:

                    mBluetoothAdapter.cancelDiscovery();

                    mPulsatorLayout.setVisibility(View.GONE);
                    devicesListView.setVisibility(View.VISIBLE);
                    pairedDevicesListView.setVisibility(View.VISIBLE);
                    searchingTextView.setText("Found Devices");
                    pairedTextView.setVisibility(View.VISIBLE);
                    devicesListView.setAdapter(SearchingDevice.adapter);
                    break;

                case BluetoothDevice.ACTION_ACL_CONNECTED:

                    Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();

                    temp = bluetoothSocket;
                    BluetoothDevice bluetoothDevice = temp.getRemoteDevice();

                    String Name = bluetoothDevice.getName();
                    String MAC = bluetoothDevice.getAddress();

                    Intent data = new Intent();
                    data.putExtra("name", Name);
                    data.putExtra("mac", MAC);

                    //لان broadcast receiver عندها method اسمها setResult فابقول انى الى بستخدمها تبع ال class
                    SearchingDevice.this.setResult(RESULT_OK, data);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_searching);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        registerReceiver(broadcastReceiver, filter);

        pairedTextView = findViewById(R.id.paired_text_view);
        pairedTextView.setVisibility(View.GONE);

        devicesListView = findViewById(R.id.list_devices);
        devicesListView.setVisibility(View.GONE);

        //when click the device on the list
        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemValue = (String) devicesListView.getItemAtPosition(position);

                String mac = itemValue.substring(itemValue.length() - 17);
                BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(mac);

                connectingThread = new ConnectingThread(bluetoothDevice);
                connectingThread.start();

            }
        });


        pairedDevicesListView = findViewById(R.id.list_paired_devices);
        pairedDevicesListView.setVisibility(View.GONE);

        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        pairedDevicesAdapter.clear();
        pairedDevicesListView.setAdapter(pairedDevicesAdapter);

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceMAC = device.getAddress(); // MAC address
                pairedDevicesAdapter.add(deviceName+"\n"+deviceMAC);
            }
        }

        //when click the device on the list
        pairedDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemValue = (String) pairedDevicesListView.getItemAtPosition(position);

                String mac = itemValue.substring(itemValue.length() - 17);
                BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(mac);

                connectingThread = new ConnectingThread(bluetoothDevice);
                connectingThread.start();

            }
        });


        mPulsatorLayout = findViewById(R.id.pulsator);
        mPulsatorLayout.start();


        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        //clear before using
        adapter.clear();

        //if device doesn't support bluetooth
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device Doesn't Support Bluetooth", Toast.LENGTH_SHORT).show();
            finish();

        } else {
            //if bluetooth is not enabled ask for permission to enable it
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetoothIntent = new Intent(mBluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, ENABLE_BT_REQUEST_CODE);
            }
            // bluetooth is enabled
            else {
                makeDiscoverable();
                discoverDevices();

                //initiate listening thread here..
                /*ListeningThread listeningThread = new ListeningThread();
                listeningThread.start();*/
            }
        }

    }

    protected void makeDiscoverable() {
        Intent discoverableIntent = new Intent(mBluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(mBluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
        startActivityForResult(discoverableIntent, DISCOVERABLE_BT_REQUEST_CODE);
    }


    protected void discoverDevices() {
        if (mBluetoothAdapter.startDiscovery()) {  //starting discovery for 12 seconds
            searchingTextView = findViewById(R.id.text_view_searching);
            searchingTextView.setText("Searching Devices...");
        } else
            Toast.makeText(getApplicationContext(), "Discovery failed to start.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_BT_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {

                makeDiscoverable();
                discoverDevices();

            } else if (requestCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "mMM Blu doesn't Works", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == DISCOVERABLE_BT_REQUEST_CODE) {
            if (resultCode == DISCOVERABLE_DURATION) {
                Toast.makeText(getApplicationContext(), "Device is discoverable for " + DISCOVERABLE_DURATION + " seconds", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Device is not discoverable", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        this.registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    /*Note:

        Enabling device discoverability will automatically enable Bluetooth if it has not been enabled on the device.

        Run method in thread runs when starting the thread*/


        /*for to Know that the result will be passed to DeviceFragment from activity ListingDevices
        i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);*/

}
