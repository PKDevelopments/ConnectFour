package kevin.park.connectfour;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
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

import kevin.park.connectfour.ui.GameBoard;
import kevin.park.connectfour.ui.MessageAdapter;
import kevin.park.connectfour.ui.NothingSelectedSpinnerAdapter;

public class MainActivity extends AppCompatActivity {
    //Main Menu Stuff
    Button start1p_button;
    Button start2p_button;
    //Note: ConnectButton is shared here

    //Gesture Stuff
    GestureDetector gestureDetector;

    //Debug variables
    boolean troubleshooting = false;

    //Connection Stuff
    TextView titleView;
    ListView listView;
    Spinner spinner;
    Button searchButton;
    Button hostButton;
    Button connectButton;
    Button backButton;
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

    //Game Stuff
    Button checkButton;
    Button resetButton;
    TextView statusView;
    GameBoard gb;
    boolean p1gamestarted = false;
    boolean p2gamestarted = false;
    int turn; int player_id = 1; //Currently locked at 1
    boolean game_initialized = false;
    boolean winner;
    int next_turn;
    //Overridden Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        InitializeMainMenu();
        //Initialize();
        /*
        if(savedInstanceState != null){
            messages = (List<String>) savedInstanceState.getSerializable("MESSAGELOG");
            adapter = new MessageAdapter(getApplicationContext(), messages);
            listView.setAdapter(adapter);
        } */
        //r.run();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if(game_initialized) {
            return gestureDetector.onTouchEvent(e);
        }
        return false;
    }

    GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if(game_initialized && !winner && (player_id == turn || p1gamestarted)){ //Add a turn == player_id function for multiplayer games..
                winner = checkForWin(playChecker(checkForColumn(e.getX(), e.getY())));
            }
            if(winner){Log.d("HELPME WINNNN","SOMEONE WON");
                resetButton.setVisibility(View.VISIBLE);
                if(p1gamestarted){
                    if(turn == 2){
                        statusView.setText("RED WINS");
                    }
                    if(turn == 1){
                        statusView.setText("BLACK WINS");
                    }
                }
                if(p2gamestarted){
                    if(turn == player_id){
                        statusView.setText("YOU WIN"); next_turn = 1;
                    }
                    if(turn != player_id){
                        statusView.setText("YOU LOSE"); next_turn = 2;
                    }
                }
            }
            return super.onDoubleTap(e);
        }
    };

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
            if(p1gamestarted){checkforp1move();}
        }
    };

    //Bluetooth and Menu Methods

    public void backtomain(){
        setContentView(R.layout.main_menu);
        InitializeMainMenu();
    }

    public void checkForMessages() {
        //This is text chat stuff and not applicable right now
        if (initialized && !game_initialized) {
            String message = infoThread.getInfoHandler().getOutput();
            if(message != "" && message != null){
                messages.add(message);
                adapter = new MessageAdapter(getApplicationContext(), messages);
                listView.setAdapter(adapter);
            }
        }
        if(initialized && game_initialized && p2gamestarted){
            String message = infoThread.getInfoHandler().getOutput();
            String coord = "";
            boolean reset = false;
            Log.d("HELPMEACT",message);
            int left = 0; int right = 0;
            if(message.contains("RESET")){
                reset = true;
                gb.resetGame();
                resetButton.setVisibility(View.INVISIBLE);
                turn = next_turn; //Winner Goes First / Is Red
                winner = false;
                if(p1gamestarted && !winner){
                    if(turn == 1){statusView.setText("RED turn.");}
                    else{statusView.setText("BLACK Turn.");}}
                if(p2gamestarted && !winner){
                    if(turn == player_id){statusView.setText("Your turn.");}
                    else{statusView.setText("Other Player's Turn.");}}
            }
            if(!reset && message != null && message.length() > 0){
                left = message.indexOf("(");
                right = message.indexOf(")");
                coord = message.substring(left+1, right);
                int col = Integer.parseInt(coord)%7;
                if(col == 0){col = 7;}
                if(player_id != turn){
                    winner = checkForWin(playChecker(col));
                }
                if(winner){Log.d("HELPME WINNNN","SOMEONE WON");
                    resetButton.setVisibility(View.VISIBLE);
                    if(p1gamestarted){
                        if(turn == 1){
                            statusView.setText("RED WINS");
                        }
                        if(turn != 2){
                            statusView.setText("BLACK WINS");
                        }
                    }
                    if(p2gamestarted){
                        if(turn == player_id){
                            statusView.setText("YOU WIN"); next_turn = 2;
                        }
                        if(turn != player_id){
                            statusView.setText("YOU LOSE"); next_turn = 1;
                        }
                    }
                }
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

    public void connect(){
        setContentView(R.layout.connect_menu);
        Initialize();
        r.run();
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
        backButton = findViewById(R.id.back_button);
        clearButton = findViewById(R.id.clearButton);
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
                player_id = 1;
            }
        });
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mDevice != null){
                Toast.makeText(getApplicationContext(),"Attempting to connect...",Toast.LENGTH_SHORT).show();
                new AsyncTask(1, mDevice, infoThread, mSocket).execute();
                player_id = 2;}
                else{Toast.makeText(getApplicationContext(),"Select a device to connect to first.",Toast.LENGTH_SHORT).show();}
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backtomain();
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

    public void InitializeMainMenu(){
        start1p_button = findViewById(R.id.start1pgame);
        start2p_button = findViewById(R.id.start2pgame);
        connectButton = findViewById(R.id.connectmenu);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });
        start2p_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_2pgame();
            }
        });
        start1p_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_1pgame();
            }
        });
    }

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

    public void sendBoardData(String message){
        Message msg = new Message();
        msg.obj = message;
        infoThread.getInfoHandler().handleMessage(msg);
    }

    public void sendResetData(String message){
        Message msg = new Message();
        msg.obj = message;
        infoThread.getInfoHandler().handleMessage(msg);
    }

    public void setDevice(){
        if(spinner.getSelectedItemPosition() > 0){mDevice = mAdapter.getRemoteDevice(addresses.get(spinner.getSelectedItemPosition()-1));
        Toast.makeText(getApplicationContext(),mDevice.getName()+" selected",Toast.LENGTH_SHORT).show();
        }
        else{Toast.makeText(getApplicationContext(),"Select a Device then Press \"Find Devices\".",Toast.LENGTH_SHORT).show();}
    }

    //Game Methods

    public int checkForColumn(float x, float y){
        int xoffset = gb.getXOffset();
        int colwdth = 140;
        int colmax = 7;
        int column_selected = -1;
        for(int i = 0; i < colmax; i++){
            if(x >= xoffset+(i)*colwdth && x < xoffset+(i+1)*colwdth){
                column_selected = i+1;
            }
        }
        return column_selected;
    }

    public void checkforp1move(){

    }

    public boolean checkForWin(int played){
        return gb.checkForWin(played, turn);
    }

    public void checkValues(){
        if(troubleshooting){gb.checkValues();}
    }

    public int playChecker(int i){
        int coord = 0;
        coord = gb.playChecker(turn, i);
        String piececoord = "coord("+coord+").c";
        if(p2gamestarted && turn == player_id){sendBoardData(piececoord);}
        if(turn == 1){turn = 2;}
        else if(turn == 2){turn = 1;}
        if(p1gamestarted && !winner){
            if(turn == 1){statusView.setText("RED turn.");}
            else{statusView.setText("BLACK Turn.");}}
        if(p2gamestarted && !winner){
            if(turn == player_id){statusView.setText("Your turn.");}
            else{statusView.setText("Other Player's Turn.");}}
        return coord;
    }

    public void resetGame(){
        gb.resetGame();
        if(p2gamestarted){sendResetData("RESET");}
        winner = false;
        resetButton.setVisibility(View.INVISIBLE);
        if(p1gamestarted && !winner){
            if(turn == 1){statusView.setText("RED turn.");}
            else{statusView.setText("BLACK Turn.");}}
        if(p2gamestarted && !winner){
            if(turn == player_id){statusView.setText("Your turn.");}
            else{statusView.setText("Other Player's Turn.");}}
    }

    public void start_1pgame(){
        setContentView(R.layout.game_map);
        gb = findViewById(R.id.gameboard);
        statusView = findViewById(R.id.statusView);
        checkButton = findViewById(R.id.checkbutton);
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkValues();
            }
        });
        resetButton = findViewById(R.id.resetButton);
        resetButton.setVisibility(View.INVISIBLE);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });
        if(!troubleshooting){checkButton.setVisibility(View.INVISIBLE);}
        gestureDetector = new GestureDetector(this, gestureListener);
        p1gamestarted = true; turn = 1;
        if(p1gamestarted && !winner){
            if(turn == 1){statusView.setText("RED turn.");}
            else{statusView.setText("BLACK Turn.");}}
        if(p2gamestarted && !winner){
            if(turn == player_id){statusView.setText("Your turn.");}
            else{statusView.setText("Other Player's Turn.");}}
        game_initialized = true;
    }

    public void start_2pgame(){
        if(connected){
            setContentView(R.layout.game_map);
            gb = findViewById(R.id.gameboard);
            statusView = findViewById(R.id.statusView);
            checkButton = findViewById(R.id.checkbutton);
            checkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkValues();
                }
            });
            resetButton = findViewById(R.id.resetButton);
            resetButton.setVisibility(View.INVISIBLE);
            resetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetGame();
                }
            });
            if(!troubleshooting){checkButton.setVisibility(View.INVISIBLE);}
            gestureDetector = new GestureDetector(this, gestureListener);
            p2gamestarted = true; turn = 1;
            if(p1gamestarted && !winner){
                if(turn == 1){statusView.setText("RED turn.");}
                else{statusView.setText("BLACK Turn.");}}
            if(p2gamestarted && !winner){
                if(turn == player_id){statusView.setText("Your turn.");}
                else{statusView.setText("Other Player's Turn.");}}
            Log.d("HELPME",""+player_id);
            game_initialized = true;}
        else{Toast.makeText(MainActivity.this, "Connect to another device first.",Toast.LENGTH_SHORT).show();}
    }


}
