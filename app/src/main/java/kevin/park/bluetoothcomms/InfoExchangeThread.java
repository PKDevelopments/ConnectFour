package kevin.park.bluetoothcomms;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InfoExchangeThread extends Thread {

    BluetoothSocket mSocket;
    Context mContext;
    InfoHandler ih;
    Handler h;
    InputStream is;
    OutputStream os;
    boolean connected = false;
    String output; String message_holder;
    TextView textView;
    int message_length;

    public InfoExchangeThread(TextView tv, Context context) throws IOException {
        String name = Thread.currentThread().getName();
        Log.d("HELPME", name+" initialized.");
        textView = tv;
        mContext = context;
        Initialize();
    }

    public void Initialize(){
        ih = new InfoHandler();
        output = " ";
    }

    public void checkStream(){
        while (true) {
            try {
                int numBytes = 0; byte[] buffer = new byte[1024];
                // Read from the InputStream.
                if(is != null) {
                    if (numBytes != -1) {
                        Thread.sleep(100);
                        numBytes = is.read(buffer);
                        output = convertBytes(numBytes, buffer);
                        ih.setOutput(output);
                    }

                }
                else{
                    Thread.sleep(500);
                }
                break;
            } catch (IOException e) {
                Log.d("HELPME", "Input stream was disconnected", e);
                connected = false;
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String convertBytes(int bytes, byte[] buffer){
        for(int i = 0; i < bytes; i++){
            int result = buffer[i];
            char c = (char)result;
            output+=c;
        }
        return output;
    }

    public void sendtoStream(String input){
        if(mSocket != null) {
            try {
                os.write(input.getBytes());
                Log.d("HELPMEwrite", "Writing successful.");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("HELPME", "Writing failed.");
            }
        }
    }

    public void setSocket(BluetoothSocket socket){
        mSocket = socket;
        connected = true;
        try {
            is = socket.getInputStream();
            os = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("HELPME", "Socket Connected.");

    }
    public InfoHandler getInfoHandler(){
        return ih;
    }

    public Handler getHandler(){
        return h;
    }

    @Override
    public void run() {
        Looper.prepare();
        while(true){
            checkStream();
        }
    }

    public class InfoHandler extends Handler {
        String message = "";

        public InfoHandler(){ }

        @Override
        public void handleMessage(@NonNull Message msg) {
            message = msg.obj.toString();
            Log.d("HELPMEWOW", message);
            sendtoStream(message);
            message_length = msg.arg1;
        }

        public String getOutput(){
            String output = message_holder;
            message_holder = "";
            return output;
        }

        public void setOutput(String input){
             message_holder = input;
             output = "";
        }

        public boolean getConnected(){
            return connected;
        }

        public String getMessage(){
            return message;
        }


    }
}
