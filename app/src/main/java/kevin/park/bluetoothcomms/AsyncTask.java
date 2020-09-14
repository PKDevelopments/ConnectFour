package kevin.park.bluetoothcomms;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class AsyncTask extends android.os.AsyncTask <Integer, Void, BluetoothSocket>{

    BluetoothDevice mDevice;
    BluetoothAdapter mAdapter;
    BluetoothSocket mSocket;
    BluetoothServerSocket mServerSocket;
    UUID id;
    int task_id;
    InfoExchangeThread infoThread;

    public AsyncTask(int designator, BluetoothDevice device, InfoExchangeThread ithread, BluetoothSocket bSocket){
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        infoThread = ithread;
        mDevice = device;
        Initialize(designator);
        task_id = designator;

    }

    private void Initialize(int i) {
        // MY_UUID is the app's UUID string, also used by the client code.
        String uuid = "ac980712-f3da-11ea-adc1-0242ac120002";
        id = UUID.fromString(uuid);
        if(i == 0){initializeHosting();}
        if(i == 1){initializeConnecting();}
    }

    private void initializeHosting(){
        BluetoothServerSocket tmp = null;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        try{
            tmp = mAdapter.listenUsingRfcommWithServiceRecord("FOR FUN", id);
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        }
        mServerSocket = tmp;
    }

    private void initializeConnecting(){
        BluetoothSocket tmp = null;
        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            tmp = mDevice.createRfcommSocketToServiceRecord(id);
        } catch (IOException e) {
            Log.e("HELPME", "Socket's create() method failed", e);
        }
        mSocket = tmp;
    }

    private void host(){
        mSocket = null;
        while(true){
            try{
                Log.d("HELPME", "Attempting to host...");
                mSocket = mServerSocket.accept();
                Log.d("HELPME", "Did it work?");
            } catch (IOException e){
                Log.d("HELPME", "Socket\'s accept method failed", e);
            }
            if(mSocket != null){
                Log.d("HELPME", "Hosting Successful.");
                cancel();
                break;
            }
        }

        infoThread.setSocket(mSocket);
    }

    private void connect(){
        mAdapter.cancelDiscovery();
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            Log.d("HELPME", "Attempting to connect...");
            mSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mSocket.close();
            } catch (IOException closeException) {
                Log.e("HELPME", "Could not close the client socket", closeException);
            }
            return;
        }
        Log.d("HELPME", "Connection Successful.");
    }

    @Override
    protected BluetoothSocket doInBackground(Integer... integers) {
        if(task_id == 0){host();}
        if(task_id == 1){connect();}
        return mSocket;
    }

    //Whatever happens after the doInBackground method completes. This is run on the main thread
    @Override
    protected void onPostExecute(BluetoothSocket socket) {
        super.onPostExecute(socket);
        infoThread.setSocket(socket);
    }

    public void cancel(){
        try{
            mServerSocket.close();
        } catch (IOException e) {
            Log.d("HELPME", "Could not close the connect socket", e);
        }
    }
}

