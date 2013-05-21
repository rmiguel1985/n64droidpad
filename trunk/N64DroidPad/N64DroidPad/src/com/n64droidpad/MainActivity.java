package com.n64droidpad;

import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.MobileAnarchy.Android.Widgets.Joystick.JoystickMovedListener;
import com.MobileAnarchy.Android.Widgets.Joystick.JoystickView;

public class MainActivity extends Activity implements OnClickListener {
	
	// Message types sent from the BluetoothRfcommClient Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
	
 // Key names received from the BluetoothRfcommClient Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
	
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
	
    // Name of the connected device
    private String connectedDevice = null;
    // Local Bluetooth adapter
    private BluetoothAdapter btAdapter = null;
    // Member object for the RFCOMM services
    private BluetoothRfcommClient rfcommClient = null;
    
 // timer task
  	private Timer mUpdateTimer;
  	private int mTimeoutCounter = 0;
  	private int mMaxTimeoutCount; // actual timeout = count * updateperiod 
  	private long mUpdatePeriod;
  	private String jtPosition=null;
	
    JoystickView joystick;
	private Button btnL;
	private Button btnStart;
	private Button btnZ;
	private Button btnDD;
	private Button btnDR;
	private Button btnDL;
	private Button btnA;
	private Button btnB;
	private Button btnDU;
	private Button btnR;
	private Button btnCU;
	private Button btnCL;
	private Button btnCR;
	private Button btnCD;
	
	private Context cx = this;
	private String TAG = "Test";
	private ImageButton btnOptions;
	private boolean vibration = false;
	Vibrator btnVibration = null;
	private int VIBRATION_STRENGTH = 0;
	private boolean jtCenter = true;
	
	private boolean blockJT = false;
	int interval = 1000;
	int dot = 1; //one millisecond of vibration
	int short_gap = 1; //one millisecond of break - could be more to weaken the vibration
	 
           
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_main);
        
        joystick = (JoystickView)findViewById(R.id.joystickView);
        btnL = (Button) findViewById(R.id.main_btn_l);
        btnStart = (Button) findViewById(R.id.main_btn_start);
        btnR = (Button) findViewById(R.id.main_btn_r);
        btnZ = (Button) findViewById(R.id.main_btn_z);
        btnDU = (Button) findViewById(R.id.main_btn_du);
        btnDL = (Button) findViewById(R.id.main_btn_dl);
        btnDR = (Button) findViewById(R.id.main_btn_dr);
        btnDD = (Button) findViewById(R.id.main_btn_dd);
        btnB = (Button) findViewById(R.id.main_btn_b);
        btnA = (Button) findViewById(R.id.main_btn_a);
        btnCU = (Button) findViewById(R.id.main_btn_cu);
        btnCL = (Button) findViewById(R.id.main_btn_cl);
        btnCR = (Button) findViewById(R.id.main_btn_cr);
        btnCD = (Button) findViewById(R.id.main_btn_cd);
        btnOptions = (ImageButton) findViewById(R.id.main_btn_options);
        
        btnL.setOnClickListener(this);
        btnStart.setOnClickListener(this); 
        btnR.setOnClickListener(this);
        btnZ.setOnClickListener(this);
        btnDU.setOnClickListener(this);
        btnDL.setOnClickListener(this);
        btnDR.setOnClickListener(this);
        btnDD.setOnClickListener(this);
        btnB.setOnClickListener(this);
        btnA.setOnClickListener(this);
        btnOptions.setOnClickListener(this);
        
        
        btnCU.setOnClickListener(this);
        btnCL.setOnClickListener(this);
        btnCR.setOnClickListener(this);
        btnCD.setOnClickListener(this);
               
        joystick.setOnJostickMovedListener(_listener);
        
     // Get local Bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        
        // If the adapter is null, then Bluetooth is not supported
        if (btAdapter == null) {
            Toast.makeText(this, getText(R.string.error_bt_available), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // If BT is not on, request that it be enabled.
    	if (!btAdapter.isEnabled()){
    		Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    		startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    	}
    	
    	// Initialize the BluetoothRfcommClient to perform bluetooth connections
        rfcommClient = new BluetoothRfcommClient(cx, mHandler);
        rfcommClient.start();
        
     // fix me: use Runnable class instead
        mUpdatePeriod =  20;
        mMaxTimeoutCount = 20;
        //mDataFormat = Integer.parseInt(prefs.getString( "data_format", "5" ));
        
        mUpdateTimer = new Timer();
        mUpdateTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					UpdateMethod();
				}
			}, 2000, mUpdatePeriod);
        btnVibration = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE) ;
    }
    
    @TargetApi(11)
	@Override
    public void onResume() {
        super.onResume();
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        
        if(!settings.getBoolean("op1", false)) {
            Log.i("Options","Vibration DISABLED");
            vibration = false;
        } else {
        	btnVibration = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE) ;
    		if(btnVibration.hasVibrator()){
    			vibration = true;
    			Log.i("Options","Vibration ENABLED" + settings.getString("op2", ""));
    			int strength = Integer.valueOf(settings.getString("op2", "0"));
    			if(strength == 0){
    				VIBRATION_STRENGTH = 10;
    			}else if(strength == 1){
    				VIBRATION_STRENGTH = 40;
    			}else{
    				VIBRATION_STRENGTH = 100;  
    			}
    			
    			/*long[] pattern = {
    			        0,  // Start immediately
    			        VIBRATION_STRENGTH, 
    			        interval,
    			        // 15 vibrations and 15 gaps = 30millis
    			        dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap, //yeah I know it doesn't look good, but it's just an example. you can write some code to generate such pattern. 
    			    };*/
    			//btnVibration.vibrate(pattern , 3);
    			
    			
    		}else{
    			Log.e("ERROR","Vibration not supported");
    		}
        	
        	
        	
        }
        
        if(!settings.getBoolean("op3", false)){
        	Log.i("Options","PAD DISABLED");
    	}else{
    		Log.i("Options","PAD ENABLED");    		
    	}
        
        if(!settings.getBoolean("op4", false)){
        	Log.i("Options","JOYSTICK DISABLED");
    	}else{
    		Log.i("Options","JOYSTICK ENABLED");    		
    	}
        
        if (rfcommClient != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (rfcommClient.getState() == BluetoothRfcommClient.STATE_NONE) {
              // Start the Bluetooth  RFCOMM services
              rfcommClient.start();
            }
        }
    }
    
    
    @Override
    public void onPause() {
        super.onPause();
        if (btnVibration!= null) btnVibration.cancel();
    }
    
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	mUpdateTimer.cancel();
    	// Stop the Bluetooth RFCOMM services
        if (rfcommClient != null) rfcommClient.stop();
        if (btnVibration!= null) btnVibration.cancel();
        
    }
    
    @Override
    public void onBackPressed() {
    	new AlertDialog.Builder(this)
    	.setTitle(getText(R.string.app_name))
    	.setMessage(getText(R.string.main_close))
    	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				btAdapter.disable();
				if (btnVibration!= null) btnVibration.cancel();
				finish();				
			}
		})
		.setNegativeButton("No", null)
		.show();
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		
    	switch (item.getItemId()){
    	case R.id.action_settings:
    		startActivity(new Intent(cx, SettingsActivity.class));
    		break;
    	case R.id.action_connect:
    		Intent serverIntent = new Intent(this, DeviceList.class);
        	startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    		break;
    	case R.id.action_exit:
    		finish();
    		break;    		
    	default:
    		return super.onContextItemSelected(item);
    	}
    	
		return false;	    
	}

	private JoystickMovedListener _listener = new JoystickMovedListener() {
		
	
	            public void OnMoved(int pan, int tilt) {	            	
	            	
	    	     	String tmpPosition =  Integer.toString(pan) + "," + Integer.toString(tilt);
		           	boolean first = true; 
					
					while(tmpPosition.length() <20){
						if(first){
							tmpPosition += "/";
							first = false;
						}else{
							tmpPosition += "q";					
						}
					}
					
					jtPosition = tmpPosition;
		           	 		           	 
		           	 /*try {
		       			//Log.e("bytes", String.valueOf(jtPosition.getBytes("UTF-8").length));
		       		} catch (UnsupportedEncodingException e) {
		       			// TODO Auto-generated catch block
		       			e.printStackTrace();
		       		}*/
		           	//sendMessage(string);
		                //Log.i("X",Integer.toString(pan));
		                //Log.i("Y",Integer.toString(tilt));
		             jtCenter = false;
	            }
	
	            public void OnReleased() {
	            	Log.i("X","released");
	            	Log.i("Y","released");
	            }
	           
	            public void OnReturnedToCenter() {
	            	Log.i("X","stopped");
	            	Log.i("Y","stopped");
	            	jtCenter = true;
	            };
	};
	private String lastPosition;
	
	
	private void sendMessage(String message){
    	// Check that we're actually connected before trying anything
    	if (rfcommClient.getState() != BluetoothRfcommClient.STATE_CONNECTED) {
    		// Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
    		return;
    	}
    	// Check that there's actually something to send
    	if (message.length() > 0) {
    		// Get the message bytes and tell the BluetoothRfcommClient to write
    		byte[] send = message.getBytes();
    		rfcommClient.write(send);
    	}
    }
	
	public void onClick(View v) {
		String button;
		String tmpPosition = null;
		boolean first = true;
		lastPosition = jtPosition;
		
		if(vibration)			
			btnVibration.vibrate(VIBRATION_STRENGTH);
				
		switch (v.getId()){
		case R.id.main_btn_a:
			first = true; 
			tmpPosition ="a";
			while(tmpPosition.length() <20){
				if(first){
					tmpPosition += ";";
					first = false;
				}else{
					tmpPosition += "q";					
				}
			}
			//jtPosition = tmpPosition;
			blockJT = true;
			sendMessage(tmpPosition);
			blockJT = false;
			//Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_b:
			first = true;
			tmpPosition ="b";
			while(tmpPosition.length() <20){
				if(first){
					tmpPosition += ";";
					first = false;
				}else{
					tmpPosition += "q";					
				}
			}
			jtPosition = tmpPosition;
			//sendMessage(button);
			//Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_z:
			first = true;
			tmpPosition ="z";
			while(tmpPosition.length() <20){
				if(first){
					tmpPosition += ";";
					first = false;
				}else{
					tmpPosition += "q";					
				}
			}
			jtPosition = tmpPosition;
			//sendMessage(button);
			//Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_start:
			first = true;
			tmpPosition ="start";
			while(tmpPosition.length() <20){
				if(first){
					tmpPosition += ";";
					first = false;
				}else{
					tmpPosition += "q";					
				}
			}
			jtPosition = tmpPosition;
			//sendMessage(button);
			//Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_l:
			first = true;
			tmpPosition ="l";
			while(tmpPosition.length() <20){
				if(first){
					tmpPosition += ";";
					first = false;
				}else{
					tmpPosition += "q";					
				}
			}
			jtPosition = tmpPosition;
			//sendMessage(button);
			//Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_r:
			first = true;
			tmpPosition ="r";
			while(tmpPosition.length() <20){
				if(first){
					tmpPosition += ";";
					first = false;
				}else{
					tmpPosition += "q";					
				}
			}
			jtPosition = tmpPosition;
			//sendMessage(button);
			//Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_du:
			first = true;
			tmpPosition ="du";
			while(tmpPosition.length() <20){
				if(first){
					tmpPosition += ";";
					first = false;
				}else{
					tmpPosition += "q";					
				}
			}
			jtPosition = tmpPosition;
			//sendMessage(button);
			//Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_dr:
			first = true;
			tmpPosition ="dr";
			while(tmpPosition.length() <20){
				if(first){
					tmpPosition += ";";
					first = false;
				}else{
					tmpPosition += "q";					
				}
			}
			jtPosition = tmpPosition;
			//sendMessage(button);
			//Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_dl:
			first = true;
			tmpPosition ="dl";
			while(tmpPosition.length() <20){
				if(first){
					tmpPosition += ";";
					first = false;
				}else{
					tmpPosition += "q";					
				}
			}
			//jtPosition = tmpPosition;
			blockJT = true;
			sendMessage(tmpPosition);
			blockJT = false;
		case R.id.main_btn_dd:
			first = true;
			tmpPosition ="dd";
			while(tmpPosition.length() <20){
				if(first){
					tmpPosition += ";";
					first = false;
				}else{
					tmpPosition += "q";					
				}
			}
			jtPosition = tmpPosition;
			//sendMessage(button);
			//Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_cu:
			first = true;
			tmpPosition ="cu";
			while(tmpPosition.length() <20){
				if(first){
					tmpPosition += ";";
					first = false;
				}else{
					tmpPosition += "q";					
				}
			}
			//jtPosition = tmpPosition;
			blockJT = true;
			sendMessage(tmpPosition);
			blockJT = false;
			//sendMessage(button);
			//Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_cd:
			first = true;
			tmpPosition ="cd";
			while(tmpPosition.length() <20){
				if(first){
					tmpPosition += ";";
					first = false;
				}else{
					tmpPosition += "q";					
				}
			}
			jtPosition = tmpPosition;
			//sendMessage(button);
			//Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_cr:
			first = true;
			tmpPosition ="cr";
			while(tmpPosition.length() <20){
				if(first){
					tmpPosition += ";";
					first = false;
				}else{
					tmpPosition += "q";					
				}
			}
			//jtPosition = tmpPosition;
			blockJT = true;
			sendMessage(tmpPosition);
			blockJT = false;
			//sendMessage(button);
			//Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_cl:
			first = true;
			tmpPosition ="cl";
			while(tmpPosition.length() <20){
				if(first){
					tmpPosition += ";";
					first = false;
				}else{
					tmpPosition += "q";					
				}
			}
			//jtPosition = tmpPosition;
			blockJT = true;
			sendMessage(tmpPosition);
			blockJT = false;
			//sendMessage(button);
			//Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_options:
			openOptionsMenu();
			break;
		}
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "Activity result");
    	switch (requestCode){
    	case REQUEST_CONNECT_DEVICE:
    		Log.i(TAG, "Activity result coonect");
    		// When DeviceListActivity returns with a device to connect
    		if (resultCode == Activity.RESULT_OK) {
    			// Get the device MAC address
    			String address = data.getExtras().getString(DeviceList.EXTRA_DEVICE_ADDRESS);
    			// Get the BLuetoothDevice object
                BluetoothDevice device = btAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                rfcommClient.connect(device);
    		}
    		break;
    	case REQUEST_ENABLE_BT:
    		Log.i(TAG, "Activity result enable");
    		// When the request to enable Bluetooth returns
    		if (resultCode != Activity.RESULT_OK) {
            	// User did not enable Bluetooth or an error occurred
                Toast.makeText(this, R.string.error_bt_enable, Toast.LENGTH_SHORT).show();
                finish();
            }
    		break;
    	}
    }
    
 // The Handler that gets information back from the BluetoothRfcommClient
    private final Handler mHandler = new Handler() {
    	@Override
        public void handleMessage(Message msg) {
    		switch (msg.what) {
    		case MESSAGE_STATE_CHANGE:
    			switch (msg.arg1) {
    			case BluetoothRfcommClient.STATE_CONNECTED:
    				/*Toast.makeText(getApplicationContext(), "Connected to "
                            + connectedDevice, Toast.LENGTH_SHORT).show();
    				mTxtStatus.setText(R.string.connected);
    				mTxtStatus.append(" " + connectedDevice);*/
    				break;
    			case BluetoothRfcommClient.STATE_CONNECTING:
    				/*mTxtStatus.setText(R.string.connected);*/
    				break;
    			case BluetoothRfcommClient.STATE_NONE:
    				/*mTxtStatus.setText(R.string.error_connection);*/
    				break;
    			}
    			break;
    			
    		case MESSAGE_READ:
    			// byte[] readBuf = (byte[]) msg.obj;
    			// int data_length = msg.arg1;
    			break;
    		case MESSAGE_DEVICE_NAME:
    			// save the connected device's name
                connectedDevice= msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                        + connectedDevice, Toast.LENGTH_SHORT).show();
    			break;
    		case MESSAGE_TOAST:
    			Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                        Toast.LENGTH_SHORT).show();
    			break;
    		}
    	}
    };
    
private void UpdateMethod() {
    	
    	// if either of the joysticks is not on the center, or timeout occurred
    	//if((!jtCenter)) {    		
	    	if(jtPosition != null && !blockJT){
	    		sendMessage(jtPosition);
	    		if (!jtPosition.contains("/")){
	    			jtPosition = lastPosition;
	    		}
	    	//}
	    	mTimeoutCounter = 0;
    	}else{
    		if( mMaxTimeoutCount>-1 )
    			mTimeoutCounter++;
    	}	
    }
}
