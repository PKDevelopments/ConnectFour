Bluetooth Communications:
NOTE: Your devices must be paired prior to connecting in this application. Do NOT rotate the device while communication is in progress. Designed for Portrait orientation.

This project is a basic theory to practice on establishing Bluetooth communications to send text messages between two Android devices with this application installed. One device will be the "server" and the other will be the "client". 
CLIENT: Select a device to connect to using the spinner. Once the corresponding device name is selected, select the FIND DEVICES button to assign the device's MAC address to a corresponding BluetoothDevice. Once that is completed attempt to connect to the host device with the CONNECT TO HOST button.
HOST: Click the HOST CONNECTION button to attempt to host a connection.
Once connected: Use the EditText to send messages to the other person with the SEND button. To delete your conversation, press the CLEAR ALL MESSAGES button. There is no ScrollView in the ListView that displays the messages and the List<String> of your received messages will NOT persist after the application is closed.
Initial connection setup is completed with AsyncTasks. The UI Thread handler looks for new messages every second, and the Bluetooth messenging thread will look for any changes in the InputStream from the BluetoothSocket every .5 seconds.

Credits: 
-Android Developers sections on Bluetooth: https://developer.android.com/guide/topics/connectivity/bluetooth#Permissions
-Spinner code: https://stackoverflow.com/questions/867518/how-to-make-an-android-spinner-with-initial-text-select-one/12221309#12221309 for NothingSelectedSpinnerAdapter
