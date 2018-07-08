package com.project.ahmed.v_tracker;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import static android.content.ContentValues.TAG;
import static com.project.ahmed.v_tracker.HomeActivity.mBluetoothAdapter;

/**
 * Created by Ahmed on 2/22/2018.
 */

public class ConnectingThread extends Thread {


    private final static UUID DEFAULT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static BluetoothSocket bluetoothSocket = null;
    BluetoothDevice mDevice;


    public ConnectingThread(BluetoothDevice device) {

        mDevice = device;
        BluetoothSocket temp = null;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            temp = device.createRfcommSocketToServiceRecord(DEFAULT_UUID);

            /*if (mDevice != null) {
                Log.i(TAG, "Device Name: " + mDevice.getName());
                Log.i(TAG, "Device UUID: " + mDevice.getUuids()[0].getUuid());
                temp = device.createRfcommSocketToServiceRecord(mDevice.getUuids()[0].getUuid());
            } else
                Log.e(TAG, "Device is null.");

        } catch (NullPointerException e) {
            try {
                temp = device.createRfcommSocketToServiceRecord(DEFAULT_UUID);
            } catch (IOException e1) {
                e1.printStackTrace();
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }
        bluetoothSocket = temp;
    }

    @Override
    public void run() {
        // Cancel discovery as it will slow down the connection
        mBluetoothAdapter.cancelDiscovery();

        try {
            // This will block until it succeeds in connecting to the device
            // through the bluetoothSocket or throws an exception
            bluetoothSocket.connect();
        } catch (IOException connectException) {
            connectException.printStackTrace();
            try {
                bluetoothSocket.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();
            }
        }
    }

    public BluetoothSocket getBluetoothSocket() {
        return bluetoothSocket;
    }

    //manageBluetoothConnection
    public void sendData(BluetoothSocket bluetoothSocket) {
        String BUZZ = "1";
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.getOutputStream().write(BUZZ.toString().getBytes());
                Log.v(TAG,"Data Sent");
            }
        } catch (Exception e) {
        }
    }

    // Cancel an open connection and terminate the thread
    public void cancel() {
        try {
            bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
