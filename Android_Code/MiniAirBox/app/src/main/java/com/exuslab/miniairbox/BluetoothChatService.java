package com.exuslab.miniairbox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.SynchronousQueue;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BluetoothChatService {

    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;

    // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothChat";

    // Unique UUID for this application
    /**
     * old UUID:fa87c0d0-afac-11de-8a39-0800200c9a66
     * new UUID:00001101-0000-1000-8000-00805F9B34FB
     */
    //"00001101-0000-1000-8000-00805F9B34FB"
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private Handler incomingHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    public  ConnectedThread mConnectedThread;
    private int mState;

    public static final int STATE_NONE = 0;      // we're doing nothing
    public static final int STATE_LISTEN = 1;    // now listening for incoming connections
    public static final int STATE_CONNECTING = 2;// now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3; // now connected to a remote device


    /***
     *
     * @param context
     * @param handler */
    public BluetoothChatService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    /**
     *
     * @param state */
    private synchronized void setState(int state) {
//        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        String str_State = "NONE";
        switch (mState){
            case STATE_LISTEN:
                str_State = "LISTEN";
                break;
            case STATE_CONNECTING:
                str_State = "CONNECTING";
                break;
            case STATE_CONNECTED:
                str_State = "CONNECTED";
                break;
        }
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state +"  State: "+str_State);
        mState = state;
        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(MessageType.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     *
     * @return */
    public synchronized int getState() {
        return mState;
    }

    /** **/
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread == null) {
//            mAcceptThread = new AcceptThread();
//            mAcceptThread.start();
        }
        setState(STATE_LISTEN); // set STATE_LISTEN
    }


    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING); // set STATE_LISTEN
    }


    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        Message msg = mHandler.obtainMessage(MessageType.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MessageType.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);// set STATE_LISTEN
    }

    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    public void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    private void connectionFailed() {
        setState(STATE_LISTEN);

        Message msg = mHandler.obtainMessage(MessageType.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MessageType.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private void connectionLost() {
        setState(STATE_LISTEN);

        Message msg = mHandler.obtainMessage(MessageType.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MessageType.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Class Accept Thread
     * **/
    private class AcceptThread extends Thread {

        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                if (D) Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
            start();
        }

        public void run() {
            if (D) Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;

            while (mState != STATE_CONNECTED) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    if (D) Log.e(TAG, "accept() failed", e);
                    break;
                }

                if (socket != null) {
                    synchronized (BluetoothChatService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                                break;
                            case STATE_CONNECTING:
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                                break;
                            case STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    if (D) Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            if (D) Log.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                if (D) Log.e(TAG, "close() of server failed", e);
            }
        }

    }// END Accept Thread

    /**
     * Connect Thread
     * **/
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                if (D) Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            if (D) Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            mAdapter.cancelDiscovery();

            try {

                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    if (D) Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                BluetoothChatService.this.start();
                return;
            }

            synchronized (BluetoothChatService.this) {
                mConnectThread = null;
            }

            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                if (D) Log.e(TAG, "close() of connect socket failed", e);
            }
        }

    }// END Class Connect Thread


    /**
     * Class Connected Thread
     * */
    public class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            if (D) Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                if (D) Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        private ByteArrayOutputStream byteArrOutStream = new ByteArrayOutputStream(64);
        private boolean head;
        private boolean tail_1;
        private boolean tail_2;
        private int k = -1;
        private String msg = "";
        public void run() {
            if (D) Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buff = new byte[1];//1024
            while (true) {
                try {

                    while ((k = mmInStream.read(buff, 0, buff.length)) > -1) {
                        System.out.println(String.format("%s %d",(char)buff[0],buff[0] & 0xFF ));
//                        if ((int) buff[0] == 13) tail_1 = true;
                        if ((int) buff[0] == 10) tail_2 = true;
                        if ((int) buff[0] == 69) head   = true;

                        if ( ((int) buff[0] != 10) || ((int) buff[0]) != 69) //|| ((int) buff[0]) != 13
                            byteArrOutStream.write((int) buff[0]);

                        if(head & tail_2) { //tail_1
                            byte[] buffer = byteArrOutStream.toByteArray();
                            for (byte b : buffer) {
                                if (((int) b) == 10 || ((int) b) == 13 || ((int) b) == 69) {
//                                    System.out.println("(int)b) == 10 || ((int)b) == 13  ||((int)b) == 69");
                                } else {
                                    System.out.print((int) b + " ");
                                    msg += (char) b;
                                }
                            }
                            System.out.println();
                            if (msg.length() > 5)
                                mHandler.obtainMessage(MessageType.MESSAGE_READ, 0, 0, msg).sendToTarget();
                            byteArrOutStream.reset();
                            head = false;
                            tail_1 = false;
                            tail_2 = false;
                            msg = "";
                        }
//                        System.out.println(" -= END =- ");

                    /*
                        for(int i = 0 ;i<k;i++) {
//                            System.out.print((char) buff[i]);//(int) buff[i] +

                            if ( ((int) buff[i] != 10) || ((int) buff[i]) != 13 || ((int) buff[i]) != 69)
                                byteArrOutStream.write((int) buff[i]);

                            if ((int) buff[i] == 13) tail_1 = true;
                            if ((int) buff[i] == 10) tail_2 = true;
                            if ((int) buff[i] == 69) head = true;

                        }
                            if(head & tail_1 & tail_2){
                                byte[] buffer = byteArrOutStream.toByteArray();
                                for (byte b : buffer){
                                    if(((int)b) == 10 || ((int)b) == 13  ||((int)b) == 69){
                                        System.out.println("(int)b) == 10 || ((int)b) == 13  ||((int)b) == 69");
                                    }
                                    else{
                                        System.out.print((int) b +" ");
                                        msg += (char) b;
                                    }
                                }
                                System.out.println();
                                if(msg.length()>5)
                                mHandler.obtainMessage(MessageType.MESSAGE_READ,0,0,msg).sendToTarget();
                                byteArrOutStream.reset();
                                head = false;
                                tail_1 = false;
                                tail_2 = false;
                                msg = "";

                        }*/
//                        String msg = new String(buffer,0,k);
//                        Log.d("read",msg);
                        break;
                    }

//                    mHandler.obtainMessage(MessageType.MESSAGE_READ, k, -1, buff).sendToTarget();

                } catch (IOException e) {
                    if (D) Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(1);
                baos.write(2);
                baos.write(3);
                baos.write(buffer);
                mmOutStream.write(baos.toByteArray());

                mHandler.obtainMessage(MessageType.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                if (D) Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                if (D) Log.e(TAG, "close() of connect socket failed", e);
            }
        }

    }// END Class ConnectedThread


}
