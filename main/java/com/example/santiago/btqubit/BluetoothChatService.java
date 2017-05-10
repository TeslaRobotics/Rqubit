package com.example.santiago.btqubit;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class BluetoothChatService {


    BluetoothAdapter mAdapter;
    Context mContext;

    private Handler mHandler;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //private static String address = "20:16:06:28:07:37";
    BluetoothDevice mDevice = null;
    private BluetoothSocket mSocket = null;
    private ConnectedThread mConnectedThread;


    private ArrayList<String> insBUFFER = new ArrayList<>();
    private StringBuilder sb = new StringBuilder();
    //private String sb = "";
    private String actMsg = "";
    private boolean msgFull = false;

    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    private int mState = STATE_NONE;

    public BluetoothChatService(Context context) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mContext = context;
        mHandler = new Handler();
    }


    public void setHandler(Handler handler){
        mHandler = handler;
    }

    public void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(mAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (mAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((Activity) mContext).startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    public synchronized void connect(String address){
        mDevice = mAdapter.getRemoteDevice(address);

        try {
            mSocket = createBluetoothSocket(mDevice);
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        mAdapter.cancelDiscovery();

        Log.d(TAG, "...Connecting...");
        try {
            mSocket.connect();
            Log.d(TAG, "....Connection ok...");
        } catch (IOException e) {
            try {
                mSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Create Socket...");

        mConnectedThread = new ConnectedThread(mSocket);
        mConnectedThread.start();

    }

    private void errorExit(String title, String message){
        Toast.makeText(mContext, title + " - " + message, Toast.LENGTH_LONG).show();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection",e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }


    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        private final InputStreamReader aReader;
        private final BufferedReader mBufferedReader;


        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            aReader = new InputStreamReader( mmInStream );
            mBufferedReader = new BufferedReader( aReader );
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes = 0;

            while (true) {
                try {

                   /* if(mmInStream.available() > 0){
                        bytes = mmInStream.read(buffer);
                        String strIncom = new String(buffer, 0, bytes);
                        sb.append(strIncom);
                    }

                    for (int i = 0; i < sb.length() ; i++) {
                        char in = sb.charAt(i);
                        sb.deleteCharAt(i);
                        if(in != '\n') actMsg +=in;
                        else {
                            msgFull = true;
                            bytes = i;
                            break;
                        }
                    }

                    if(msgFull){
                        mHandler.obtainMessage(1, bytes, -1, actMsg)
                                .sendToTarget();
                        msgFull = false;
                        actMsg = "";
                    }*/

                    if(mBufferedReader.ready()){
                        String aString = mBufferedReader.readLine();
                        mHandler.obtainMessage(1, bytes, -1, aString)
                                .sendToTarget();
                    }




                    /*if(mmInStream.available() > 0) {

                        bytes = mmInStream.read(buffer);
                        String strIncom = new String(buffer, 0, bytes);
                        sb.append(strIncom);

                        int i = sb.indexOf("\n");                            // determine the end-of-line
                        if (i > 0) {                                            // if end-of-line,
                            Log.d(TAG, "actual sb " + sb + "...");
                            String sbprint = sb.substring(0, i);

                            if (i < sb.length() - 1) sb.delete(0, i + 1);
                            else sb = new StringBuilder();


                            mHandler.obtainMessage(1, bytes, -1, sbprint)
                                    .sendToTarget();
                        }
                    }*/

                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            Log.d(TAG, "...Data to send: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }

        public void write(byte message) {
            Log.d(TAG, "...Data to send: " + message + "...");
            //byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(message);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    public void write(String msg){
        if (mConnectedThread != null) {
            mConnectedThread.write(msg);
        }
    }
    public void write(byte msg){
        mConnectedThread.write(msg);
    }
    public void stop(){
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }




}