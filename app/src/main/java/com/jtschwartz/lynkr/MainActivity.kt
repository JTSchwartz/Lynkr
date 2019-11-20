package com.jtschwartz.lynkr

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {
	
	var btDevice: BluetoothDevice? = null
	var btAddress: String? = null
	val OPEN_SETTINGS = 1
	
	companion object {
		val EXTRA_ADDRESS: String = "Device_Address"
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		
		curBtDevice.text = btAddress ?: "No Chosen Bluetooth Device"
	}
	
	fun btnCamera(view: View) {
		println("Camera")
	}
	
	fun btnKeyStroke(view: View) {
		val event: String? = when (view.id) {
			keystroke_alpha.id -> "Alpha"
			keystroke_beta.id -> "Beta"
			keystroke_gamma.id -> "Gamma"
			keystroke_delta.id -> "Delta"
			else -> null
		}
		
		println(event)
	}
	
	fun btnLock(view: View) {
		println("Lock")
	}
	
	fun btnPowerOptions(view: View) {
		println("Power Options")
	}
	
	fun btnVolume(view: View) {
		val event: String? = when (view.id) {
			volume_increase.id -> "Increase"
			volume_decrease.id -> "Decrease"
			volume_mute.id -> "Mute"
			else -> null
		}
		
		println(event)
	}
	
	fun btnSettings(view: View) {
		println("Settings")
		val openSettings = Intent(this, SettingsActivity::class.java)
		openSettings.putExtra(EXTRA_ADDRESS, btAddress)
		startActivityForResult(openSettings, OPEN_SETTINGS)
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == OPEN_SETTINGS) {
			if (resultCode == Activity.RESULT_OK) {
				val returnedDevice = data?.getStringExtra(SettingsActivity.EXTRA_ADDRESS)
				println("Got Device: $returnedDevice")
				curBtDevice.text = returnedDevice.toString()
				
				if (returnedDevice != null) {
					btAddress = returnedDevice.toString()
					btDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(returnedDevice.toString())
				}
			}
		}
	}
}
