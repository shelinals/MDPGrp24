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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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

    private String jString1;
    private String jString2;


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
    Button btnMDF;
    TextView tvTimer;
    Button btnStart;
    Button btnStop; //Stop button
    Button btnReset;
    ToggleButton togglebtnUpdate;
    ToggleButton togglebtnMode;
    Button btnUpdate;
    Button btnCalibrate;

    private RelativeLayout mRelativeLayout;
    private PopupWindow mPopUpWindow;
    private boolean isPopOut = false;

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

        btnMDF = (Button) findViewById(R.id.btn_mdf);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.rl_arena);
        tvStatus = (TextView) findViewById(R.id.tv_status_text_box);
        tvStatus.setMovementMethod(new ScrollingMovementMethod());

        tvTimer = (TextView) findViewById(R.id.tv_timer);

        btnStart = (Button) findViewById(R.id.btn_start);
        btnStop = (Button) findViewById(R.id.btn_stop);
        btnReset = (Button) findViewById(R.id.btn_reset);
        togglebtnUpdate = (ToggleButton) findViewById(R.id.togglebtn_update);
        togglebtnMode = (ToggleButton) findViewById(R.id.togglebtn_mode);

        btnUpdate = (Button) findViewById(R.id.btn_update);
        btnCalibrate = (Button) findViewById(R.id.btn_calibrate);

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
                    Log.d(TAG, readMessage);

                    //for robot
                    if(readMessage.contains("robotPosition")){
                        System.out.println("contains position!!");
                        try {
                            JSONObject jObject = new JSONObject(readMessage);
                            JSONArray jArray = jObject.getJSONArray("robotPosition");
                            System.out.println(jArray);
                            pgv.robotPosition(jArray.getInt(0), jArray.getInt(1), jArray.getString(2));
                        }catch(JSONException e){
                            Log.e("JSON Parser", "Error parsing data" + e.toString());
                        }
                   }

                    if(readMessage.contains("Explore")){
                        System.out.println("contains exploration!!");
                        try {
                            JSONObject jObject = new JSONObject(readMessage);
                            String jString = jObject.getString("Explore");
                            System.out.println("contains Explore jString!!"+jString);
                            pgv.mapExploration(jString);
                        }catch(JSONException e){
                            Log.e("JSON Parser", "Error parsing data exploration" + e.toString());
                        }
                    }

                    if(readMessage.contains("Grid")){
                        System.out.println("contains Grid!!");
                        try {
                            JSONObject jObject = new JSONObject(readMessage);
                            String jString = jObject.getString("Grid");
                            System.out.println("contains Grid jString!!"+jString);
                            pgv.mapObstacle(jString);
                        }catch(JSONException e){
                            Log.e("JSON Parser", "Error parsing data grid" + e.toString());
                        }
                    }

                    if(readMessage.contains("MapString")){
                        System.out.println("contains MDF!!");
                        tvStatus.setText("Exploration Done");
                        try {
                            JSONObject jObject = new JSONObject(readMessage);
                            jString1 = jObject.getString("MapString1");
                            jString2 = jObject.getString("MapString2");
                            System.out.println("received: "+jString1+" and "+jString2);
                            timerHandler.removeCallbacks(timerRunnable);
                            btnStart.setEnabled(true);
                            btnStart.setVisibility(View.VISIBLE);
                            btnStop.setEnabled(false);
                            btnStop.setVisibility(View.INVISIBLE);

                        }catch(JSONException e){
                            Log.e("JSON Parser", "Error parsing data grid" + e.toString());
                        }
                    }

                    //amd
                    if(readMessage.contains("Testing")){
                        System.out.println("contains grid!!");
                        try {
                            JSONObject jObject = new JSONObject(readMessage);
                            String jString = jObject.getString("grid");
                            System.out.println("contains grid jString!!"+jString);
                            JSONArray jArray = jObject.getJSONArray("robotPosition");
                            System.out.println(jArray);
                            pgv.mapInStringAMD(jString, jArray.getInt(0), jArray.getInt(1), jArray.getString(2));
                        }catch(JSONException e){
                            Log.e("JSON Parser", "Error parsing data" + e.toString());
                        }
                    }

                    if(readMessage.contains("status")){
                        try {
                            JSONObject jObject = new JSONObject(readMessage);
                            String jString = jObject.getString("status");
                            tvStatus.setText(jString);
                        }catch(JSONException e){
                            Log.e("JSON Parser", "Error parsing data" + e.toString());
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

    public void onBtnCalibrate(View view){
        sendMessage("CALIBRATE");
    }

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
        if(exploration){
            sendMessage("E");
        }
        else {
            sendMessage("F");
        }
        startTime1 = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
        btnStart.setEnabled(false);
        btnStart.setVisibility(View.INVISIBLE);
        btnStop.setEnabled(true);
        btnStop.setVisibility(View.VISIBLE);
    }

    public void onBtnStopPressed(View view) {
        //robot
//        sendMessage("pstop");
        timerHandler.removeCallbacks(timerRunnable);
        btnStart.setEnabled(true);
        btnStart.setVisibility(View.VISIBLE);
        btnStop.setEnabled(false);
        btnStop.setVisibility(View.INVISIBLE);
    }

    public void onBtnMDFShow(View view){

        if(isPopOut==false) {

            // Initialize a new instance of LayoutInflater service
            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

            // Inflate the custom layout/view
            View customView = inflater.inflate(R.layout.popup_mdf, null);

            // Initialize a new instance of popup window
            mPopUpWindow = new PopupWindow(
                    customView,
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
            );

            isPopOut = true;

            // Set an elevation value for popup window
            // Call requires API level 21
            if (Build.VERSION.SDK_INT >= 21) {
                mPopUpWindow.setElevation(5.0f);
            }

            // Get a reference for the custom view close button
            ImageButton closeButton = (ImageButton) customView.findViewById(R.id.btn_close);
            TextView textview1 = (TextView) customView.findViewById(R.id.mdf_string_partone);
            TextView textview2 = (TextView) customView.findViewById(R.id.mdf_string_parttwo);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.BELOW, textview1.getId());

            System.out.println("inside popup:"+jString1+" and "+jString2);
            textview1.setText(jString1);
            textview2.setText(jString2);

            // Set a click listener for the popup window close button
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Dismiss the popup window
                    isPopOut = false;
                    mPopUpWindow.dismiss();
                }
            });

            // Finally, show the popup window at the center location of root relative layout
            mPopUpWindow.showAtLocation(mRelativeLayout, Gravity.CENTER, 0, 0);
        }

//        Intent intent = new Intent(getApplicationContext(), MDFActivity.class);
//        Bundle extras = new Bundle();
//        extras.putString("JSTRING1",jString1);
//        extras.putString("JSTRING2",jString2);
//        intent.putExtras(extras);
//        startActivity(intent);
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

