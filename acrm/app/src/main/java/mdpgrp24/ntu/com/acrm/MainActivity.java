package mdpgrp24.ntu.com.acrm;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity  {

    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;


    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;


    // Name of the connected device
    private String mConnectedDeviceName = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;

    BluetoothConnect BC;
    FunctionPreference functionPref;

    public ListView mList;
    public Button speakButton;

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    //TextView tvTimer2;

    // private ImageView img;

    // private Matrix matrix = new Matrix();
    //private float scale = 1f;
    //private ScaleGestureDetector SGD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // img = (ImageView)findViewById(R.id.pixelGridView);
        //SGD = new ScaleGestureDetector(this,new ScaleListener());
        voiceinputbuttons();


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BC = (BluetoothConnect) getApplication();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (mBluetoothAdapter == null) {
            Intent intent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(intent, 1);
        }

        functionPref = new FunctionPreference(getApplicationContext());

        // btnStop2.setEnabled(false);

//        mapHandler.post(new Runnable() {
//        	public void run() {
//        		//btnUpdate.performClick();
//        		onBtnUpdatePressed(null);
//
//        		mapHandler.postDelayed(this, 30000);
//        	}
//        });
    }

    public void voiceinputbuttons(){
        speakButton = (Button)findViewById(R.id.btn_Next);
        mList = (ListView)findViewById(R.id.list);
        mList.setVisibility(View.INVISIBLE);
    }

    public void startVoiceRecognitionActivity(){
        Intent intent = new Intent (RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Please speak");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }


   /* protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        System.out.println("Bye");
        canvas.save();
        canvas.scale(scale, scale);
        pgv.onDraw(canvas);
        canvas.restore();
    }*/

    /*@Override
    public boolean onTouchEvent(MotionEvent ev) {
        SGD.onTouchEvent(ev);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.
            SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            System.out.println("Hi");
            scale *= detector.getScaleFactor();
            scale = Math.max(0.1f, Math.min(scale, 5.0f));
           // matrix.setScale(scale, scale);
            //img.setImageMatrix(matrix);

            pgv.onDraw();
            return true;
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bluetooth_connect:
                Intent bluetoothConnectIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(bluetoothConnectIntent, 1);
                return true;
            case R.id.bluetooth_chat:
                Intent bluetoothChatIntent = new Intent(this, BluetoothChat.class);
                startActivity(bluetoothChatIntent);
                return true;
            case R.id.buttons_config:
                Intent functionButtonsConfig = new Intent(this, PreferenceActivity.class);
                startActivity(functionButtonsConfig);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) //setupChat();
            {
                mChatService = new BluetoothChatService(this, mHandler);
            }
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        if (D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }

        if (BC.getBluetoothConnectedThread() != null) {
            //mHandler = BC.getHandler();
            mChatService = BC.getBluetoothConnectedThread();
            //mHandler = BC.getHandler();
            mChatService.setHandler(mHandler);
            mHandler = BC.getHandler();
        }


    }

//	    @Override
//	    public synchronized void onPause() {
//	        super.onPause();
//	        if(D) Log.e(TAG, "- ON PAUSE -");
//	    }

//	    @Override
//	    public void onStop() {
//	        super.onStop();
//	        if(D) Log.e(TAG, "-- ON STOP --");
//	    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if (D) Log.e(TAG, "--- ON DESTROY ---");
    }


    private final void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(subTitle);
    }

    // The Handler that gets information back from the BluetoothChatService
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            BC.setDeviceName(mConnectedDeviceName);
                            //mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    //byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    //String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D) Log.d(TAG, "onActivityResult " + resultCode);
        if(requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK){
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mList.setAdapter(new ArrayAdapter(this,android.R.layout.simple_list_item_1,matches));

            if(matches.contains("proceed")){
                informationMenu();
            }

        }
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    //setupChat();
                    mChatService = new BluetoothChatService(this, mHandler);
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }

        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);


        System.out.println("BLUETOOTHCONNECT " + BC);
        if (BC.getBluetoothConnectedThread() == null) {
            BC.setBluetoothConnectedThread(mChatService, mHandler);

            System.out.println("BLUETOOTHCONNECT " + "entered ");
        } else {
            System.out.println("BLUETOOTHCONNECT " + "problem");
        }
    }


    public void onBtnF1Pressed(View view) {
        String function_pref_string_f1 = functionPref.getFunctionsDetails().get("f1");
        sendMessage(function_pref_string_f1);
    }

    public void onBtnF2Pressed(View view) {
        String function_pref_string_f2 = functionPref.getFunctionsDetails().get("f2");
        sendMessage(function_pref_string_f2);
    }

    public void onBtnToArenaPressed(View view){
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED){
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }
        else {
            Intent intent = new Intent(this, ArenaActivity.class);
            startActivity(intent);
        }

    }

    public void onBtnToNextPressed(View view){

        startVoiceRecognitionActivity();
    }

    public void informationMenu(){
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED){
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }
        else {
            Intent i = new Intent(this, ArenaActivity.class);
            startActivity(i);
        }
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);
        }
    }


}
