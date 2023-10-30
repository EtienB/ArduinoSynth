package com.example.arduinosynth;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
{

    private static final int PERMISSION_REQUEST_CODE = 200;

    private String deviceName = null;
    private String deviceAddress;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static CreateConnectThread createConnectThread;

    private final static int CONNECTING_STATUS = 1;
    public static int connection = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button requestPermission = findViewById(R.id.buttonPermission);
        Button slanje = findViewById(R.id.buttonSend);
        Button buttonConnect = findViewById(R.id.buttonConnect);
        EditText editTextMusic = findViewById(R.id.editTextMusic);

        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null)
        {
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            buttonConnect.setEnabled(false);
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress);
            createConnectThread.start();
        }

        handler = new Handler(Looper.getMainLooper())
        {
            @Override
            public void handleMessage(Message msg)
            {
                if(msg.toString()=="CONNECTING_STATUS")
                {
                        switch (msg.arg1)
                        {
                            case 1:
                                Toast.makeText(MainActivity.this, "Povezano s " + deviceName, Toast.LENGTH_SHORT).show();
                                buttonConnect.setEnabled(true);
                                break;
                            case -1:
                                Toast.makeText(MainActivity.this, "Neuspješno povezivanje", Toast.LENGTH_SHORT).show();
                                buttonConnect.setEnabled(true);
                                break;
                        }
                }
            }
        };

        buttonConnect.setOnClickListener(view ->
        {
            Intent intent = new Intent(MainActivity.this, SelectDevice.class);
            startActivity(intent);
        });

        slanje.setOnClickListener(view ->
        {
            if(connection==1)
            {
                OutputStream outStream = null;
                String text = String.valueOf(editTextMusic);
                byte[] bytes = text.getBytes();

                try
                {
                    outStream = mmSocket.getOutputStream();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                try
                {
                    outStream.write(bytes);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
        });

        requestPermission.setOnClickListener(view ->
        {
            if (checkPermission())
            {
                Toast.makeText(MainActivity.this, "Dopuštenja odobrena.", Toast.LENGTH_LONG).show();
            }
            else
            {
                requestPermission();
            }
        });
    }

    private boolean checkPermission()
    {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), BLUETOOTH_CONNECT);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0)
                {
                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean bluetoothConnectAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && bluetoothConnectAccepted)
                        Toast.makeText(this, "Dopuštenja odobrena.", Toast.LENGTH_LONG).show();
                    else
                    {
                        Toast.makeText(this, "Dopuštenja odbijena.", Toast.LENGTH_LONG).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                        {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION))
                            {
                                showMessageOKCancel("Morate odobriti pristup dopuštenjima!",
                                        (dialog, which) ->
                                        {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                            {
                                                requestPermissions(new String[]{ACCESS_FINE_LOCATION, BLUETOOTH_CONNECT},
                                                        PERMISSION_REQUEST_CODE);
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener)
    {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public static class CreateConnectThread extends Thread
    {
        @SuppressLint("MissingPermission")
        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address)
        {
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            @SuppressLint("MissingPermission") UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try
            {
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        @SuppressLint("MissingPermission")
        public void run()
        {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try
            {
                mmSocket.connect();
                Log.e("Status", "Uređaj povezan");
                connection=1;
                MainActivity.handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            }
            catch (IOException connectException)
            {
                try
                {
                    MainActivity.mmSocket.close();
                    connection=0;
                    Log.e("Status", "Nemoguće povezivanje s uređajem");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                }
                catch (IOException closeException)
                {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }
        }
    }
}