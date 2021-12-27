package net.sjlee.serialtestapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

public class MainActivity extends Activity implements View.OnClickListener{

    private final String TAG = "sjlee";
    private Button buttonSerial;

    private boolean mServerError = false;
    private boolean mStopThread = false;

    private static final String device = "ttyS1";

    private SerialPortFinder mSerialPortFinder = new SerialPortFinder();
    private SerialPort mSerialPort = null;

    public OutputStream mOutputStream;
    private InputStream mInputStream;

    private int baudrate = 115200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSerial = (Button)findViewById(R.id.id_btn_serial);
        buttonSerial.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.id_btn_serial:
                Log.d(TAG, "buttonSerial click !!");

                new Thread(){
                    @Override
                    public void run() {

                        runTcpServer();
                    }
                }.start();


                break;
        }
    }

    private void closeSocket(ServerSocket ss, Socket s){


        if (ss != null) {
            try {
                ss.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (s != null){
            try {
                s.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void runTcpServer() {


        try{

            String[] entries = mSerialPortFinder.getAllDevices();
            String[] entryValues = mSerialPortFinder.getAllDevicesPath();
            String path = null;

            for(int i = 0; device != null && entries != null && i < entries.length; i++) {
                Log.i(TAG, "entries["+i+"] : "+entries[i]);
                Log.i(TAG, "entryValues["+i+"] : "+entryValues[i]);
                if(entries[i].contains(device)) {
                    path = entryValues[i];
                    break;
                }
            }

            if(path != null) {

                Log.d(TAG, "path : "+ path);

                mSerialPort = new SerialPort(new File(path), baudrate, 0);

                mOutputStream = mSerialPort.getOutputStream();
                mInputStream = mSerialPort.getInputStream();

                /* Create a receiving thread */
                new Thread(){
                    @Override
                    public void run() {

                        mServerError = false;
                        mStopThread = false;

                        while(!mServerError && !mStopThread){
                            int size = 0;
                            try {
                                byte[] buffer = new byte[4096];

                                if (mInputStream == null) {
                                    Log.d(TAG, "InputStream is null !!");
                                    return;
                                }
                                size = mInputStream.read(buffer);

                                if (size > 0) {
                                    Log.d(TAG, "size : "+size);
                                    onDataReceived(buffer, size);
                                }
                            }catch(IOException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
            }else{
                Log.d(TAG, "path is null !!");
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    protected void onDataReceived(byte[] buffer, int size) {
        Log.d(TAG, "onDataReceived() size : " + size);

        for (int i = 0; i < size; i++) {
            Log.d(TAG, "buffer["+i+"] : " + String.format("0x%02x", buffer[i]));
        }

        String outgoingMsg = "STB_CONNECT\n";

        try {
            byte[] out_buff = outgoingMsg.getBytes();

            mOutputStream.write(out_buff);
        }catch(IOException e){
            e.printStackTrace();
        }

    }




}
