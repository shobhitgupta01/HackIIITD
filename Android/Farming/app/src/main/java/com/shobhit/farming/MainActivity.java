package com.shobhit.farming;


import android.app.Dialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.IOException;

import java.io.OutputStream;
import java.util.Calendar;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private Handler handler;

    EditText editText;
    ImageButton imageButton;
    Button btn1,btn2,btn3;
    BluetoothDevice mmDevice;
    //making bluetooth adapter

    BluetoothSocket mmSocket;
    public OutputStream mmOutputStream;
    int timeRemaining=0;
    TextView timer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //------- Initialising the UI Compos -----------
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        editText = (EditText)findViewById(R.id.editText);
        imageButton = (ImageButton)findViewById(R.id.imageButton);
        btn1=(Button)findViewById(R.id.button1);
        btn2=(Button)findViewById(R.id.button2);
        btn3=(Button)findViewById(R.id.button3);
        timer = (TextView)findViewById(R.id.textView_timer);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(1);
            }
        });







        //--------- HANDLER ---------------
        handler = new Handler();

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                timeRemaining=timeRemaining-1000; //decrementing the timer
                String msg = "1\n";

                try{
                    mmOutputStream.write(msg.getBytes());  //sending out the data
                }
                catch (IOException e){
                    e.printStackTrace();
                }

                //updating the timer
                int remainingSeconds = timeRemaining/1000;
                int remainingMins = remainingSeconds/60;
                remainingSeconds = remainingSeconds%60;
                int remainingHours = remainingMins/60;
                remainingMins=remainingMins%60;




                if(timeRemaining>=0) {
                    timer.setText(remainingHours+":"+remainingMins+":"+remainingSeconds);
                    handler.postDelayed(this, 1000);
                }
            }
        };


        //Calling the timer thread using handler object
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.postDelayed(runnable,1000);
            }
        });

        //Stopping th transmission
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.setText("00:00:00");
                editText.setText("");
            }
        });

        if (mBluetoothAdapter == null) {
                // Device does not support Bluetooth
            Toast.makeText(getBaseContext(),"Bluetooth not supported",Toast.LENGTH_SHORT).show();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mmDevice = device;
            }
        }

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID

        try{
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        try{
            mmSocket.connect();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        try{
            mmOutputStream = mmSocket.getOutputStream();
        }
        catch (IOException e){
            e.printStackTrace();
        }





    } //end of OnCreate



    protected Dialog onCreateDialog(int id) {
        Dialog dialog=null;
        Calendar c = Calendar.getInstance();

        TimePickerDialog tp;

        if (id == 1) {
            tp = new TimePickerDialog(this, timeListener, c.get(Calendar.HOUR), c.get(Calendar.MINUTE), false); //false for 12 hour clock
            tp.setTitle("Select Time");
            dialog=tp;
        }
        return dialog;

    } //end of OnCreateDialog


    //TimeListener

    private TimePickerDialog.OnTimeSetListener timeListener= new TimePickerDialog.OnTimeSetListener(){

        @Override
        public void onTimeSet(TimePicker v, int hour, int minute){
            editText.setText(hour+":"+minute);
            //--------- Getting the current time
            Calendar c = Calendar.getInstance();
            int curHour = c.get(Calendar.HOUR);
            int curMin = c.get(Calendar.MINUTE);
            timeRemaining = ((Math.abs(hour-curHour))*60)+Math.abs(minute-curMin);
            timeRemaining=timeRemaining*60*1000; //converting into milliseconds
            Toast.makeText(getBaseContext(),"Chosen time "+timeRemaining,Toast.LENGTH_SHORT).show();
        }

    };
}
