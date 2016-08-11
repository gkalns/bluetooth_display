package com.example.gundars.mytestapplication;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.OnDataReceivedListener;
import app.akexorcist.bluetotohspp.library.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;


public class MainActivity extends AppCompatActivity {
    BluetoothSPP bt;
    TextView textStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textStatus = (TextView)findViewById(R.id.textStatus);

        bt = new BluetoothSPP(this);
        if(!bt.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                // process received msg
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothConnectionListener() {
            public void onDeviceDisconnected() {
                textStatus.setText("Status : Not connected");
            }

            public void onDeviceConnectionFailed() {
                textStatus.setText("Status : Connection failed");
            }

            public void onDeviceConnected(String name, String address) {
                textStatus.setText("Status : Connected to " + name);
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            }
        }
    }

    private boolean validTextflag = false;

    public void setup() {
        Button btnSend = (Button)findViewById(R.id.button_send);
        btnSend.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(validTextflag){
                    TextView textview = (TextView)findViewById(R.id.textHex);
                    String tosend = "$0" + textview.getText() + "000000000000000000000";
                    textview.setText(tosend);
                }

//                bt.send("$0123456789abcdef01234567#", false);
            }
        });

        EditText input = (EditText)findViewById(R.id.number_input);
        input.addTextChangedListener(new TextWatcher() {


            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                TextView text = (TextView)findViewById(R.id.textHex);
//                    text.setText(s);
                int number;
                try {
                    number = Integer.parseInt(s.toString());
                } catch (Exception e) {
                    number = 0;
                }
                if(number < 4096){
                    String hex=Integer.toHexString(number);
                    if(hex.length() == 1) hex = "0"+hex;
                    if(hex.length() == 2) hex = "0"+hex;
                    text.setText(hex);
                    validTextflag = true;
                } else{
                    text.setText(R.string.notvalid);
                    validTextflag = false;
                }
            }
        });



    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    public void connect(View v){
        bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);

        Intent intent = new Intent(getApplicationContext(), DeviceList.class);
        startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
    }

}
