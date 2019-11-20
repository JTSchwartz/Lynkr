package com.jtschwartz.lynkr

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*
import org.jetbrains.anko.toast

class SettingsActivity : AppCompatActivity() {
	
	var bluetoothAdapter: BluetoothAdapter? = null
	lateinit var pairedDevices: Set<BluetoothDevice>
	val REQUEST_ENABLE_BLUETOOTH = 1
	var btDevice: BluetoothDevice? = null
	
	companion object {
		const val EXTRA_ADDRESS: String = "BT_Device"
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_settings)
		
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
		if (bluetoothAdapter == null) {
			toast("Device does not support Bluetooth")
			return
		} else if (!bluetoothAdapter!!.isEnabled){
			val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
			startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
		}
		
		refreshBluetoothList()
	}
	
	fun refreshBluetoothList(view: View? = null) {
		pairedDevices = bluetoothAdapter!!.bondedDevices
		val btDeviceList: ArrayList<BluetoothDevice> = ArrayList()
		
		if (pairedDevices.isNotEmpty()) {
			for (device: BluetoothDevice in pairedDevices) {
				btDeviceList.add(device)
			}
		} else {
			toast("No paired bluetooth devices found")
		}
		
		val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, btDeviceList)
		settings_devices.adapter = adapter
		settings_devices.onItemClickListener = AdapterView.OnItemClickListener{_, _, pos, _ ->
			btDevice = btDeviceList[pos]
		}
	}
	
	fun navigateHome(view: View) {
		val result = Intent()
		result.putExtra(EXTRA_ADDRESS, btDevice.toString())
		setResult(Activity.RESULT_OK, result)
		
		finish()
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
			if (resultCode == Activity.RESULT_OK) {
				if (bluetoothAdapter!!.isEnabled) {
					toast("Bluetooth has been enabled")
				} else {
					toast("Bluetooth has been disabled")
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
				toast("Bluetooth enabling has been cancelled")
			}
		}
	}
}
