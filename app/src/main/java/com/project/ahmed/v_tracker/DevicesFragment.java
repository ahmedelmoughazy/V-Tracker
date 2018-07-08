package com.project.ahmed.v_tracker;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static com.project.ahmed.v_tracker.ConnectingThread.bluetoothSocket;
import static com.project.ahmed.v_tracker.SearchingDevice.connectingThread;

/**
 * Created by Ahmed on 11/17/2017.
 */

public class DevicesFragment extends Fragment implements LocationListener {

    private Handler mHandler = new Handler();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public TextView statusTextView;
    public ImageView statusImageView;
    public ImageView sound;
    public Button disconnectButton;
    double latitude;
    double longitude;
    private FusedLocationProviderClient mFusedLocationClient;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Toast.makeText(getContext(), "Disconnected", Toast.LENGTH_LONG).show();
                //give notification to the user
                addNotification();
                connectingThread.cancel();

                statusTextView.setText("No Devices Are Connected");
                statusImageView.setImageResource(R.drawable.disconnected);                                  // change it later

                sound.setVisibility(View.GONE);
                disconnectButton.setVisibility(View.GONE);

                //save last known location

                if (ActivityCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]
                            {android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                    return;
                }
                mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {

                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                            //double to long
                            long lat = Double.doubleToRawLongBits(latitude);
                            long lng = Double.doubleToRawLongBits(longitude);

                            SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
                            prefs.edit().putLong("latitude", lat).apply();
                            prefs.edit().putLong("longitude", lng).apply();
                        }
                    }
                });


            }
        }
    };
    private View rootView;
    private char state = '1';

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_devices, container, false);

        //IntentFilter filter = new IntentFilter();
        //filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        //getActivity().registerReceiver(broadcastReceiver,filter);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        //handle disconnect button
        disconnectButton = rootView.findViewById(R.id.disconnect_button);
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (statusTextView.getText().equals("No Devices Are Connected")) {
                    Toast.makeText(getActivity(), "You Have To Be Connected First !", Toast.LENGTH_LONG).show();
                } else {
                    connectingThread.cancel();
                    statusTextView.setText("No Devices Are Connected");
                    statusImageView.setImageResource(R.drawable.disconnected);
                    disconnectButton.setVisibility(View.GONE);
                    sound.setVisibility(View.GONE);

                }
            }
        });

        sound = rootView.findViewById(R.id.sound);
        sound.setVisibility(View.GONE);
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!statusTextView.getText().equals("No Devices Are Connected")) {
                    //ابعت حاجه لو connected
                    ConnectedThread thread = new ConnectedThread(connectingThread.getBluetoothSocket());
                    thread.write();
                }
            }
        });


        FloatingActionButton mFloatingActionButton = rootView.findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), SearchingDevice.class);
                startActivityForResult(i, 1);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String name = data.getStringExtra("name");
                //String mac = data.getStringExtra("mac");

                //manage connection
                BluetoothSocketListener bsl = new BluetoothSocketListener(bluetoothSocket, mHandler);
                Thread messageListener = new Thread(bsl);
                messageListener.start();

                statusTextView = rootView.findViewById(R.id.status_text);
                statusTextView.setText("Connected To " + name);

                statusImageView = rootView.findViewById(R.id.status_image);
                statusImageView.setImageResource(R.drawable.connected);                           // change it later

                sound.setVisibility(View.VISIBLE);
                disconnectButton.setVisibility(View.VISIBLE);
            }

            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }

    }

    private void addNotification() {

        Bitmap bmp = BitmapFactory.decodeResource(getResources(),R.drawable.find);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.tracker);
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(1000);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent intent = new Intent(getActivity(),HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("trackNotification", "trackNotification");
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(),0,intent,0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("Device Disconnected")
                .setTicker("Don't Forget Your Device")
                .setContentText("Tap To Find")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true) //to remove after the user clicks it
                .setSound(soundUri)
                .setLargeIcon(bitmap).setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(bmp)
                        .bigLargeIcon(null));

        NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, mBuilder.build());
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        getActivity().registerReceiver(broadcastReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        //getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    private class ConnectedThread extends Thread {

        private BluetoothSocket mSocket;
        private InputStream mInputStream;
        private OutputStream mOutputStream;

        public ConnectedThread(BluetoothSocket socket) {

            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            mSocket = socket;

            try {
                tmpIn = mSocket.getInputStream();
            } catch (IOException ie) {
                ie.printStackTrace();
            }

            try {
                tmpOut = mSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mInputStream = tmpIn;
            mOutputStream = tmpOut;
        }


        public void write() {

            byte[] buffer = new byte[1024];
            buffer[0]=(byte)state;//prepare data like this

            try {
                mOutputStream = mSocket.getOutputStream();
                mOutputStream.write(buffer);
                Log.e("message", buffer.toString()+" sent");
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }

            if (state=='1') {
                state = '0';
                sound.setImageResource(R.drawable.nosound);
            }else if (state=='0') {
                state = '1';
                sound.setImageResource(R.drawable.sound);
            }
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private class MessagePoster implements Runnable {
        private String message;

        private Uri soundUri = Uri.parse("android.resource://com.project.ahmed.v_tracker/raw/rr");
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        public MessagePoster(String message) {
            this.message = message;
        }
        public void run() {
            RingtoneManager.getRingtone(getContext(), soundUri).play();
            //vibrator.vibrate(1000);
        }
    }

    private class BluetoothSocketListener implements Runnable {
        private BluetoothSocket socket;
        private Handler handler;

        public BluetoothSocketListener(BluetoothSocket socket, Handler handler) {
            this.socket = socket;
            this.handler = handler;
        }

        public void run() {
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            try {
                InputStream instream = socket.getInputStream();
                int bytesRead = -1;
                String message;
                while (true) {
                    message = "";

                    bytesRead = instream.read(buffer);
                    if (bytesRead != -1) {
                        while ((bytesRead == bufferSize) && (buffer[bufferSize - 1] != 0)) {
                            message = message + new String(buffer, 0, bytesRead);
                            bytesRead = instream.read(buffer);
                        }
                        message = message + new String(buffer, 0, bytesRead - 1);
                        handler.post(new MessagePoster(message));
                        socket.getInputStream();
                        //break;
                    }
                }
            } catch (IOException e) {
                Log.d("BLUETOOTH_COMMS", e.getMessage());
            }
        }
    }
}
