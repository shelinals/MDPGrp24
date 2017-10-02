package mdpgrp24.ntu.com.acrm;

/**
 * Created by shelinalusandro on 4/9/17.
 */

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ArenaActivity extends Activity implements SensorEventListener {

    //MISSING IMPLEMENTATION
    //EXIT ARENA - check
    //STOPPING
    //ENABLE WAYPOINT AND ROBOTSTART CLICK - check
    //Sensor?

    static ArenaActivity arena;

    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;
    //private String xposText;
    private Button possend;


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

    //NEED TO GO LOOK FURTHER
    private BluetoothChatService mChatService = null;
    private BluetoothConnect BTConn = null;

    //BluetoothConnect BC;
    PixelGridView pgv;

    //FunctionPreference functionPref;
    TextView tvStatus;
    TextView tvTimer;
    Button btnStart;
    Button btnStop; //Stop button
    Button btnReset;
    ToggleButton togglebtnUpdate;
    ToggleButton togglebtnMode;
    Button btnUpdate;

    //CAN BE CHANGED
    long startTime1 = 0;
    long startTime2 = 0;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    boolean flag = false;
    boolean flag2 = false;

    String startpoint = null;
    String waypoint = null;
    boolean startclick = true;
    boolean waypointclick = true;

    boolean exploration = true; //if else then fastest path

    Handler mapHandler = new Handler();
    Runnable mapRunnable = new Runnable() {
        public void run() {
            //btnUpdate.performClick();
            onBtnUpdatePressed(null);

            mapHandler.postDelayed(this, 1000);
        }
    };

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime1;
            int seconds = (int) (millis / 1000.0);
            //int minutes = seconds / 60;
            seconds = seconds % 60;

            tvTimer.setText(String.format("%d:%d", ((int)seconds), ((long)millis%100)));

            timerHandler.postDelayed(this, 0);
        }
    };


    // private ImageView img;

    // private Matrix matrix = new Matrix();
    //private float scale = 1f;
    //private ScaleGestureDetector SGD;

    private void SetupBTService(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BTConn = (BluetoothConnect) getApplication();
        if (BTConn.getBluetoothConnectedThread() != null) {
            mChatService = BTConn.getBluetoothConnectedThread();
            mChatService.setHandler(mHandler);
            mConnectedDeviceName = BTConn.getDeviceName();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        SetupBTService();

        arena = this;

        setContentView(R.layout.activity_arena);
        //img = (ImageView)findViewById(R.id.pixelGridView);
        //SGD = new ScaleGesturtheDetector(this,new ScaleListener());

        mSensorManager = (SensorManager) getSystemService(ArenaActivity.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);


        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        pgv = (PixelGridView) findViewById(R.id.pixelGridView);

        tvStatus = (TextView) findViewById(R.id.tv_status_text_box);
        tvStatus.setMovementMethod(new ScrollingMovementMethod());

        tvTimer = (TextView) findViewById(R.id.tv_timer);

        btnStart = (Button) findViewById(R.id.btn_start);
        btnStop = (Button) findViewById(R.id.btn_stop);
        btnReset = (Button) findViewById(R.id.btn_reset);
        togglebtnUpdate = (ToggleButton) findViewById(R.id.togglebtn_update);
        togglebtnMode = (ToggleButton) findViewById(R.id.togglebtn_mode);

        btnUpdate = (Button) findViewById(R.id.btn_update);

        //functionPref = new FunctionPreference(getApplicationContext());
        btnStop.setEnabled(false);
        btnStop.setVisibility(View.INVISIBLE);
        pgv.setNumColumns(15);
        pgv.setNumRows(20);

        mapHandler.post(new Runnable() {
        	public void run() {
        		//btnUpdate.performClick();
        		onBtnUpdatePressed(null);
        		//mapHandler.postDelayed(this, 30000);
        	}
        });
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

//    @Override
//    public void onStart() {
//        super.onStart();
//        if(D) Log.e(TAG, "++ ON START ++");
//
//        // If BT is not on, request that it be enabled.
//        // setupChat() will then be called during onActivityResult
//        if (!mBluetoothAdapter.isEnabled()) {
//            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
//            // Otherwise, setup the chat session
//        } else {
//            if (mChatService == null) //setupChat();
//            {
//                mChatService = new BluetoothChatService(this, mHandler);
//            }
//        }
//    }
//
//    @Override
//    public synchronized void onResume() {
//        super.onResume();
//
//        if(D) Log.e(TAG, "+ ON RESUME +");
//
//        // Performing this check in onResume() covers the case in which BT was
//        // not enabled during onStart(), so we were paused to enable it...
//        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
//        if (mChatService != null) {
//            // Only if the state is STATE_NONE, do we know that we haven't started already
//            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
//                // Start the Bluetooth chat services
//                mChatService.start();
//            }
//        }
//
//        if (BC.getBluetoothConnectedThread() != null) {
//            //mHandler = BC.getHandler();
//            mChatService = BC.getBluetoothConnectedThread();
//            //mHandler = BC.getHandler();
//            mChatService.setHandler(mHandler);
//            mHandler = BC.getHandler();
//        }
//
//        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
//    }

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
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        // Stop the Bluetooth chat services
//        if (mChatService != null) mChatService.stop();
//
//        if(D) Log.e(TAG, "--- ON DESTROY ---");
//    }

    public static ArenaActivity getInstance(){
        return arena;
    }

    private final void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(subTitle);
    }

    public void onBtnExitPressed(View view){
            onBackPressed();
    }

    // The Handler that gets information back from the BluetoothChatService
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //if there is a connection change in Bluetooth
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
      //                      BC.setDeviceName(mConnectedDeviceName);
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
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Log.d(TAG, writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    tvStatus.setText(readMessage);
                    Log.d(TAG, readMessage);

                    //for robot
                    if (readMessage.contains("GRID=")) {
                        String colRow = readMessage.substring(readMessage.indexOf("=")+1, readMessage.indexOf("|"));
                        //System.out.println("colrow " +colRow);
                        //System.out.println("row " + colRow.substring(colRow.indexOf("?"), colRow.length()));
                        int col = Integer.parseInt(colRow.substring(0, colRow.indexOf("?")));
                        int row = Integer.parseInt(colRow.substring(colRow.indexOf("?")+1, colRow.length()));

                        String direction = readMessage.substring(readMessage.indexOf("|")+1, readMessage.lastIndexOf("|"));

                        String map = readMessage.substring(readMessage.lastIndexOf("|")+1, readMessage.length());
                        System.out.println("map = " + map);

                        pgv.mapInString(map, col, row, direction);
                    }

                    if(readMessage.contains("GRID 5")) {
                        int row = Integer.parseInt(readMessage.substring(readMessage.indexOf("5"), readMessage.indexOf("5")+1));

                        if(readMessage.substring(readMessage.indexOf("5")+2, readMessage.indexOf("5")+3).equals("5")) {
                            int column = Integer.parseInt(readMessage.substring(readMessage.indexOf("5")+2, readMessage.indexOf("5")+3));
                            pgv.setNumColumns(column);
                            pgv.setNumRows(row);

                            String map = readMessage.substring(readMessage.indexOf("5")+12, readMessage.length());
                            pgv.mapInStringAMD(map, column, row);
                        } else {
                            int column = Integer.parseInt(readMessage.substring(readMessage.indexOf("5")+2, readMessage.indexOf("5")+4));
                            pgv.setNumColumns(column);
                            pgv.setNumRows(row);
                            String map = readMessage.substring(readMessage.indexOf("5")+13, readMessage.length());
                            pgv.mapInStringAMD(map, column, row);
                        }
                    } else if (readMessage.contains("GRID 10") ||
                            readMessage.contains("GRID 15") ||
                            readMessage.contains("GRID 20") ||
                            readMessage.contains("GRID 25") ||
                            readMessage.contains("GRID 30")) {
                        int row = Integer.parseInt(readMessage.substring(5, 7));

                        if(readMessage.substring(8,9).equals("5")) {
                            int column = Integer.parseInt(readMessage.substring(8,9));
                            pgv.setNumColumns(column);
                            pgv.setNumRows(row);
                            String map = readMessage.substring(readMessage.indexOf("5")+13, readMessage.length());
                            pgv.mapInStringAMD(map, column, row);
                        } else {
                            int column = Integer.parseInt(readMessage.substring(8, 10));
                            pgv.setNumColumns(column);
                            pgv.setNumRows(row);
                            String map = readMessage.substring(18);

                            pgv.mapInStringAMD(map, column, row);
                        }
                    }
                    //modify add per line
                    if (readMessage.contains("forward")) {
                        tvStatus.setText("Robot Moving Forward");
                    } else if (readMessage.contains("stop")) {
                        tvStatus.setText("Robot Stop");
                    }else if(readMessage.contains("back")){
                        tvStatus.setText("Robot Moving Back");
                    } else if (readMessage.contains("left")) {
                        tvStatus.setText("Robot Going Left");
                    } else if (readMessage.contains("right")) {
                        tvStatus.setText("Robot Going Right");
                    }
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

//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(D) Log.d(TAG, "onActivityResult " + resultCode);
//        switch (requestCode) {
//            case REQUEST_CONNECT_DEVICE_SECURE:
//                // When DeviceListActivity returns with a device to connect
//                if (resultCode == Activity.RESULT_OK) {
//                    connectDevice(data, true);
//                }
//                break;
//            case REQUEST_CONNECT_DEVICE_INSECURE:
//                // When DeviceListActivity returns with a device to connect
//                if (resultCode == Activity.RESULT_OK) {
//                    connectDevice(data, false);
//                }
//                break;
//            case REQUEST_ENABLE_BT:
//                // When the request to enable Bluetooth returns
//                if (resultCode == Activity.RESULT_OK) {
//                    // Bluetooth is now enabled, so set up a chat session
//                    //setupChat();
//                    mChatService = new BluetoothChatService(this, mHandler);
//                } else {
//                    // User did not enable Bluetooth or an error occurred
//                    Log.d(TAG, "BT not enabled");
//                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
//                    finish();
//                }
//        }
//    }

//    private void connectDevice(Intent data, boolean secure) {
//        // Get the device MAC address
//        String address = data.getExtras()
//                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
//        // Get the BluetoothDevice object
//        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//        // Attempt to connect to the device
//        mChatService.connect(device, secure);
//
//
//        System.out.println("BLUETOOTHCONNECT " + BC);
//        if (BC.getBluetoothConnectedThread() == null) {
//            BC.setBluetoothConnectedThread(mChatService, mHandler);
//
//            System.out.println("BLUETOOTHCONNECT " + "entered ");
//        } else {
//            System.out.println("BLUETOOTHCONNECT " + "problem");
//        }
//    }

    //Set the Robot Start Coordinate
    public void onBtnRobotStart(View view)
    {
        if(startclick == true){
            pgv.setEnabledPGV(true);
            pgv.setType("startpoint");
            //Prompt user to select one grid in the pixelgridview
            final Toast toast = Toast.makeText(getApplicationContext(),"Select a grid for the Robot startpoint", Toast.LENGTH_SHORT);
            toast.show();

        }
        else{
            final Toast toast = Toast.makeText(getApplicationContext(),"You have selected the startpoint", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    //Set the Waypoint Coordinate
    public void onBtnWaypoint(View view)
    {
        if(waypointclick == true){
            pgv.setEnabledPGV(true);
            pgv.setType("waypoint");
            //Prompt user to select one grid in the pixelgridview
            final Toast toast = Toast.makeText(getApplicationContext(),"Select a grid for the Robot waypoint", Toast.LENGTH_SHORT);
            toast.show();
        }
        else{
            final Toast toast = Toast.makeText(getApplicationContext(),"You have selected the waypoint", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    public void onBtnLeftPressed(View view) {
        if(startpoint!=null) {
            pgv.moveLeft();
            //actual robot
            //sendMessage("a2");
            //AMD test
            //sendMessage("a");
            final Toast toast = Toast.makeText(this, "Going Left", Toast.LENGTH_SHORT);
            toast.show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 500);
        }
    }

    public void onBtnForwardPressed(View view) {
        if(startpoint!=null) {
            pgv.moveForward();
            //actual robot
            //sendMessage("a1");
            //AMD test
            //sendMessage("w");
            final Toast toast = Toast.makeText(this, "Moving Forward", Toast.LENGTH_SHORT);
            toast.show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 500);
        }
    }

    public void onBtnRightPressed(View view) {
        if(startpoint!=null) {
            pgv.moveRight();
            //actual robot
            //sendMessage("a3");
            //AMD test
            //sendMessage("d");
            final Toast toast = Toast.makeText(this, "Going Right", Toast.LENGTH_SHORT);
            toast.show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 500);
        }
    }

    //for AMD testing > this is down button
    public void onBtnDownPressed(View view) {
        if(startpoint!=null) {
            pgv.moveDown();
            //actual robot
            //sendMessage("a4");
            //AMD test
            //sendMessage("s");
            final Toast toast = Toast.makeText(this, "Moving Back", Toast.LENGTH_SHORT);
            toast.show();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 500);
        }
    }

    public void onBtnStartPressed(View view) {
        //robot
//        sendMessage("pstart:e");
        //amd
        if(exploration){
            sendMessage("beginExplore");
        }
        else {
            sendMessage("beginFastest");
        }
        startTime1 = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
        btnStart.setEnabled(false);
        btnStart.setVisibility(View.INVISIBLE);
        btnStop.setEnabled(true);
        btnStop.setVisibility(View.VISIBLE);
    }

    public void onBtnStopPressed(View view) {
        timerHandler.removeCallbacks(timerRunnable);
        //robot
//        sendMessage("pstop");
        btnStart.setEnabled(true);
        btnStart.setVisibility(View.VISIBLE);
        btnStop.setEnabled(false);
        btnStop.setVisibility(View.INVISIBLE);
    }

    public void onBtnResetPressed(View view) {
        tvTimer.setText(R.string.timer_default);
    }

    public void onTogglebtnUpdatePressed(View view) {
        if(togglebtnUpdate.isChecked()) {
            //isChecked == true == auto
            btnUpdate.setEnabled(false);

            mapHandler.post(mapRunnable);
        } else {
            //isChecked == false == manual
            btnUpdate.setEnabled(true);
            mapHandler.removeCallbacks(mapRunnable);

        }
    }

    public void onTogglebtnModePressed(View view) {
        if(togglebtnMode.isChecked()) {
            //isChecked == true == fpw
            exploration = false;
            ((TextView)findViewById (R.id.tv_exploration_time)).setText (R.string.fpw_time);
            ((Button)findViewById(R.id.btn_start)).setText(R.string.fpw_btn);
        } else {
            //isChecked == false == exploration
            exploration = true;
            ((TextView)findViewById (R.id.tv_exploration_time)).setText (R.string.exploration_time);
            ((Button)findViewById(R.id.btn_start)).setText(R.string.exp_btn);
        }
    }

    public void onBtnUpdatePressed(View view) {
        sendMessage("GRID");
        //request map from rpi/pc
    }


    public void sendMessage(String message) {
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        float z1 = event.values[2];
        float x1 = event.values[0];
        float y1 = event.values[1];

        int x = (int)(x1*1000);
        int y = (int)(y1*1000);
        int z = (int)(z1*1000);

        if (x > 5000) {
            //left
            if (flag == true) {
                //onBtnLeftPressed(null);
                pgv.moveLeft();
                flag = false;
            }
        } else if (x < -5000) {
            //right
            if (flag == true) {
                //onBtnRightPressed(null);
                pgv.moveRight();
                flag = false;
            }
        } else {
            flag = true;
        }

        if (y < 3000 ) {
            if(flag2 == true) {
                //onBtnForwardPressed(null);
                pgv.moveDown();
                flag2 = false;
            }
        } else if (x < -3000) {
            if (flag2 == true) {
                pgv.moveForward();
                flag2 = false;
            }
        }

        flag2 = true;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
	    /*@Override
	    protected void onResume() {
	        // Register a listener for the sensor.
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
	    }*/

//    @Override
//    protected void onPause() {
//        // important to unregister the sensor when the activity pauses.
//        super.onPause();
//        mSensorManager.unregisterListener(this);
//    }

}

