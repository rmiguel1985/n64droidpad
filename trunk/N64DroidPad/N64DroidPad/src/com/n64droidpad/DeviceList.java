package com.n64droidpad;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class DeviceList extends Activity {
	private Button btnRefresh;
	private Activity act = this;
	private BluetoothAdapter btAdapter;
	private ArrayAdapter<String> DevicesListArrayAdapter;
	private ArrayAdapter<String> pairedDevicesArrayAdapter;
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  
        setContentView(R.layout.activity_devices);
        
        setResult(Activity.RESULT_CANCELED);

        btnRefresh = (Button) findViewById(R.id.device_btn_refresh);
        btnRefresh.setOnClickListener(new Button.OnClickListener() {        
			public void onClick(View v) {
				act.setProgressBarIndeterminateVisibility(true);
				btDiscovery();
			}
        });
        
        DevicesListArrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_devices_row);
        pairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.activity_devices_row);
        
        ListView devicesListView = (ListView) findViewById(R.id.devices_lst);
        devicesListView.setAdapter(DevicesListArrayAdapter);
        devicesListView.setOnItemClickListener(mDeviceClickListener);
        
        ListView pairedListView = (ListView) findViewById(R.id.devices_lst_paired);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        
     // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            //findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
            	pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.no_devices).toString();
            pairedDevicesArrayAdapter.add(noDevices);
        }
	}
	
	@Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (btAdapter != null) {
        	btAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void btDiscovery() {

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(getText(R.string.app_name) + " " + getText(R.string.search_devices));



        // If we're already discovering, stop it
        if (btAdapter.isDiscovering()) {
        	btAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        btAdapter.startDiscovery();
    }

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
        	btAdapter.cancelDiscovery();
        	Log.i("DeviceList", "Item clicked");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            if(!info.toString().equals(getText(R.string.no_devices))){
            	String address = info.substring(info.length() - 17);

                // Create the result Intent and include the MAC address
                Intent intent = new Intent();
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

                // Set result and finish this Activity
                setResult(Activity.RESULT_OK, intent);
            }
            
            finish();
        }
    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                	DevicesListArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.app_name);
                if (DevicesListArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.no_devices).toString();
                    DevicesListArrayAdapter.add(noDevices);
                }
            }
        }
    };
}
