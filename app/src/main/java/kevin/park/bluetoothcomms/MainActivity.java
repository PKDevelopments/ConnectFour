package kevin.park.bluetoothcomms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import kevin.park.bluetoothcomms.ui.MessageAdapter;
import kevin.park.bluetoothcomms.ui.NothingSelectedSpinnerAdapter;

public class MainActivity extends AppCompatActivity {
    TextView titleView;
    ListView listView;
    Spinner spinner;
    Button searchButton;
    Button hostButton;
    Button connectButton;
    Button sendButton;
    Button clearButton;
    EditText messageInput;
    BluetoothDevice mDevice;
    BluetoothAdapter mAdapter;
    BluetoothSocket mSocket;
    InfoExchangeThread infoThread;
    IntentFilter filter;
    Handler h;
    List<String> messages;
    List<String> devices;
    List<String> addresses;
    boolean initialized = false; boolean connected = false;
    MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Initialize();
        if(savedInstanceState != null){
            messages = (List<String>) savedInstanceState.getSerializable("MESSAGELOG");
            adapter = new MessageAdapter(getApplicationContext(), messages);
            listView.setAdapter(adapter);
        }
        r.run();
    }

    public void checkForMessages() {
        if (initialized) {
            String message = infoThread.getInfoHandler().getOutput();
            if(message != "" && message != null){
                messages.add(message);
                adapter = new MessageAdapter(getApplicationContext(), messages);
                listView.setAdapter(adapter);
            }

        }
    }

    public void checkIfConnected(){
        if(initialized){
            if(!connected){
                if(infoThread.getInfoHandler().getConnected()){
                    connected = true;
                    Toast.makeText(getApplicationContext(),"Connected successfully.",Toast.LENGTH_SHORT).show();
                    titleView.setText("Status: Connected.");
                }
            }
            if(connected){
                if(!infoThread.getInfoHandler().getConnected()){
                    Toast.makeText(getApplicationContext(),"Connection lost.",Toast.LENGTH_SHORT).show();
                    connected = false;
                    titleView.setText("Status: Not Connected.");
                }
            }
        }
    }

    public void clearMessages(){
        messages.clear();
        adapter = new MessageAdapter(getApplicationContext(), messages);
        listView.setAdapter(adapter);
    }

    public void Initialize(){
        messages = new ArrayList<String>();
        devices = new ArrayList<String>();
        addresses = new ArrayList<String>();
        adapter = new MessageAdapter(getApplicationContext(), messages);
        listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);
        titleView = findViewById(R.id.titleView);
        searchButton = findViewById(R.id.findButton);
        hostButton = findViewById(R.id.hostButton);
        connectButton = findViewById(R.id.connectButton);
        sendButton = findViewById(R.id.send_button);
        clearButton = findViewById(R.id.clearButton);
        messageInput = findViewById(R.id.messageSend);
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        spinner = findViewById(R.id.connectSpinner);
        h = new Handler();
        try {
            infoThread = new InfoExchangeThread(titleView, getApplicationContext());
            infoThread.setName("INFOEXCHANGETHREAD");
            infoThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDevice();
            }
        });
        hostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Attempting to host...",Toast.LENGTH_SHORT).show();
                new AsyncTask(0, mDevice, infoThread, mSocket).execute();
            }
        });
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDevice != null){
                Toast.makeText(getApplicationContext(),"Attempting to connect...",Toast.LENGTH_SHORT).show();
                new AsyncTask(1, mDevice, infoThread, mSocket).execute();}
                else{Toast.makeText(getApplicationContext(),"Select a device to connect to first.",Toast.LENGTH_SHORT).show();}
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
                messageInput.setText(" ");
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearMessages();
            }
        });
        queryPairedDevices();
        initialized = true;
    }

    /* Not used in this application
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = mDevice.getName();
                String deviceHardwareAddress = mDevice.getAddress(); // MAC address
            }
        }
    }; */

    public void bluetoothSetup(){
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //registerReceiver(receiver, filter);
        //Shouldn't need to do this unless I actually need to discover devices
        mAdapter.startDiscovery();
    }

    public void queryPairedDevices(){
        Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();
        if(pairedDevices.size() > 0){
            for(BluetoothDevice device : pairedDevices){
                devices.add(device.getName());
                addresses.add(device.getAddress());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, devices);
            spinner.setAdapter(adapter);
            spinner.setAdapter(
                    new NothingSelectedSpinnerAdapter(
                            adapter,
                            R.layout.contact_spinner_row_nothing_selected,
                            this));
        }
    }

    public void sendMessage(){
        String message = messageInput.getText().toString();
        Message msg = new Message();
        msg.obj = message;
        msg.arg1 = message.length();
        infoThread.getInfoHandler().handleMessage(msg);
    }

    public void setDevice(){
        mDevice = mAdapter.getRemoteDevice(addresses.get(spinner.getSelectedItemPosition()-1));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(receiver);

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("MESSAGELOG", (Serializable) messages);
    }

    //Checks for new messages once a second
    Runnable r = new Runnable() {
        @Override
        public void run() {
        h.postDelayed(r, 1000);
        checkIfConnected();
        checkForMessages();
        }
    };

}
