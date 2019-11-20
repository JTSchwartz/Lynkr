package com.jtschwartz.lynkr

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
	
	companion object {
		lateinit var btAdapter: BluetoothAdapter
		private var btAddress: String? = null
		var btSocket: BluetoothSocket? = null
		var btDevice: BluetoothDevice? = null
		var deviceUUID: UUID = UUID.fromString("d0945fea-97b2-4395-aadb-3e36b776080b")
		const val EXTRA_ADDRESS: String = "Device_Address"
		var isConnected: Boolean = false
		private const val OPEN_SETTINGS = 1
		lateinit var progress: ProgressDialog
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		
		curBtDevice.text = btAddress ?: "No Chosen Bluetooth Device"
//		ConnectToDevice(this).execute()
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
			volume_increase.id -> "i"
			volume_decrease.id -> "d"
			volume_mute.id -> "m"
			else -> null
		}
		sendCommand(event)
		println(event)
	}
	
	fun btnSettings(view: View) {
		println("Settings")
		val openSettings = Intent(this, SettingsActivity::class.java)
		openSettings.putExtra(EXTRA_ADDRESS, btAddress)
		startActivityForResult(openSettings, OPEN_SETTINGS)
	}
	
	fun disconnect() {
		if (btSocket != null) {
			try {
				btSocket!!.close()
				btSocket = null
				isConnected = false
			} catch (e: IOException) {
				e.printStackTrace()
			}
		}
	}
	
	private fun sendCommand(payload: String?) {
		if (btSocket != null){
			try {
				btSocket!!.outputStream.write(payload?.toByteArray() ?: "Unknown".toByteArray())
			} catch (e: IOException) {
				e.printStackTrace()
			}
		}
	}
	
	private class ConnectToDevice(c: Context): AsyncTask<Void, Void, String>() {
		private var connectSuccess: Boolean = true
		private val context: Context = c
		
		override fun onPreExecute() {
			super.onPreExecute()
			progress = ProgressDialog.show(context, "Connecting...", "Please wait")
		}
		
		override fun doInBackground(vararg p0: Void?): String? {
			try {
				if (btSocket == null || !isConnected) {
					btAdapter = BluetoothAdapter.getDefaultAdapter()
					val device: BluetoothDevice = btAdapter.getRemoteDevice(btAddress)
					BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
					
					val socket = device.createInsecureRfcommSocketToServiceRecord(deviceUUID)
					val clazz = socket.remoteDevice.javaClass
					val paramTypes = arrayOf<Class<*>>(Integer.TYPE)
					val m = clazz.getMethod("createRfcommSocket", *paramTypes)
					btSocket = m.invoke(socket.remoteDevice, Integer.valueOf(1)) as BluetoothSocket
					try {
						btSocket!!.connect()
					} catch (e: Exception) {
						e.printStackTrace()
//						Snackbar.make(view, "An error occurred", Snackbar.LENGTH_SHORT).show()
					}
				}
			} catch (e: IOException) {
				connectSuccess = false
				e.printStackTrace()
				
			}
			return null
		}
		
		override fun onPostExecute(result: String?) {
			super.onPostExecute(result)
			if (!connectSuccess) {
				Log.i("data", "Could not connect")
			} else {
				isConnected = true
			}
			
			progress.dismiss()
		}
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
					
					ConnectToDevice(this).execute()
				}
			}
		}
	}
}
