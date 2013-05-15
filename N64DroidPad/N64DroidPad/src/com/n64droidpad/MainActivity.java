package com.n64droidpad;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
	Vibrator btnVibration;
	private int VIBRATION_STRENGTH = 0;
           
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
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
        	btnVibration = (Vibrator) cx.getSystemService(Context.VIBRATOR_SERVICE) ;
    		if(btnVibration.hasVibrator()){
    			vibration = true;
    			Log.i("Options","Vibration ENABLED" + settings.getString("op2", ""));
    			int strength = settings.getInt("", 0);
    			if(strength == 0){
    				VIBRATION_STRENGTH = 50;
    			}else if(strength == 1){
    				VIBRATION_STRENGTH = 90;
    			}else{
    				VIBRATION_STRENGTH = 150;  
    			}
    			
    			btnVibration.vibrate(VIBRATION_STRENGTH);
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
	                Log.i("X",Integer.toString(pan));
	                Log.i("Y",Integer.toString(tilt));
	            }
	
	            public void OnReleased() {
	            	Log.i("X","released");
	            	Log.i("Y","released");
	            }
	           
	            public void OnReturnedToCenter() {
	            	Log.i("X","stopped");
	            	Log.i("Y","stopped");
	            };
	};
	

	public void onClick(View v) {
		String button;
		
		if(vibration)
			btnVibration.vibrate(VIBRATION_STRENGTH);
		
		switch (v.getId()){
		case R.id.main_btn_a:
			button = "a";
			Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_b:
			button = "b";
			Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_z:
			button = "z";
			Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_start:
			button = "start";
			Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_l:
			button = "l";
			Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_r:
			button = "r";
			Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_du:
			button = "du";
			Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_dr:
			button = "dr";
			Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_dl:
			button = "dl";
			Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_dd:
			button = "dd";
			Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_cu:
			button = "cu";
			Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_cd:
			button = "cd";
			Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_cr:
			button = "bcr";
			Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_cl:
			button = "cl";
			Toast.makeText(cx, button + " Pressed", Toast.LENGTH_SHORT).show();
			break;
		case R.id.main_btn_options:
			openOptionsMenu();
			break;
		}
		
	}

}
