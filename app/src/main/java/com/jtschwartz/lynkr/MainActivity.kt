//*******************************************************************************
//
//      filename:  MainActivity.kt
//
//   description:  Implements the Main UI Activity and all necessary communication functions
//
//        author:  Schwartz, Jacob T.
//       Copyright (c) 2020 Schwartz, Jacob T.
//
//******************************************************************************

package com.jtschwartz.lynkr

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.URLUtil
import android.widget.ProgressBar
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {
	
	companion object {
		lateinit var btAdapter: BluetoothAdapter
		private var btAddress: String? = null
		var btSocket: BluetoothSocket? = null
		var btDevice: BluetoothDevice? = null
		var deviceUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
		const val EXTRA_ADDRESS: String = "Device_Address"
		var isConnected: Boolean = false
		private const val OPEN_SETTINGS = 1
		lateinit var progress: ProgressBar
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		Log.i("log", "Create")
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		progress = findViewById(R.id.progress_bar)
		
		setupUI()
		
		this.getPreferences(Context.MODE_PRIVATE)?.let {
			btAddress = it.getString(getString(R.string.preferences_key), null)
		}
	}
	
	override fun onResume() {
		super.onResume()
		btAddress?.let {
			btDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(btAddress)
			ConnectToDevice(this).execute()
			device_label.text = btDevice!!.name
			sendCommand("init")
		}
	}
	
	override fun onDestroy() {
		super.onDestroy()
		disconnect()
	}
	
	private fun setupUI() {
		device_label.text = if (btAddress != null) "Connected to: ${btAdapter.getRemoteDevice(btAddress)}" else "No Connected Device"
		
		volume_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			var volumeLevel = 0
			
			override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
				val intervaledProgress = if (progress % 2 == 0) progress else (progress + 1)
				volume_label.text = intervaledProgress.toString().padEnd(1, '%')
				volumeLevel = intervaledProgress
			}
			
			override fun onStartTrackingTouch(seekBar: SeekBar?) {}
			
			override fun onStopTrackingTouch(seekBar: SeekBar?) {
				sendCommand(Commands.Volume.set(volumeLevel))
			}
		})
	}
	
	fun btnSimpleCommand(view: View) {
		val event: String? = when (view.id) {
			keystroke_alpha.id -> Commands.Keystroke.alpha
			keystroke_beta.id -> Commands.Keystroke.beta
			keystroke_gamma.id -> Commands.Keystroke.gamma
			keystroke_delta.id -> Commands.Keystroke.delta
			hibernate.id -> Commands.Power.hibernate
			lock.id -> Commands.Access.lock
			shutdown.id -> Commands.Power.shutdown
			next_track.id -> Commands.Media.next
			play_pause.id -> Commands.Media.playPause
			prev_track.id -> Commands.Media.prev
			volume_mute.id -> Commands.Volume.mute
			logout.id -> Commands.Access.logout
			transfer_get.id -> Commands.Transfer.get
			transfer_set.id -> "${Commands.Transfer.set}${(getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).primaryClip?.getItemAt(0)?.text}"
			else               -> null
		}
		
		sendCommand(event)
	}
	
	fun btnSettings(view: View) {
		val openSettings = Intent(this, SettingsActivity::class.java)
		openSettings.putExtra(EXTRA_ADDRESS, btAddress)
		startActivityForResult(openSettings, OPEN_SETTINGS)
	}
	
	fun resetConnection(view: View) {
		disconnect()
		btAddress?.let { ConnectToDevice(this).execute() }
	}
	
	private fun disconnect() {
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
		if (btSocket == null) ConnectToDevice(this).execute()
		
		if (btSocket != null && btSocket!!.outputStream != null) {
			try {
				btSocket!!.outputStream.write("${payload!!}\r\n".toByteArray())
				btSocket!!.outputStream.flush()
				val buffer = ByteArray(512)
				val length = btSocket!!.inputStream.read(buffer)
				val reception = String(buffer, 0, length)
				Log.i("log", "Reception: $reception")
				btSocket!!.outputStream.close()
				btSocket!!.inputStream.close()
				
				val response = reception.split("|")
				when (response[0]) {
					"clipboard" -> {
						val content = reception.substringAfter("|")
						val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
						val clip = ClipData.newPlainText("Copied from ${btDevice?.name ?: "Desktop"}", content)
						clipboard.setPrimaryClip(clip)
						
						if (URLUtil.isValidUrl(content)) {
							val openURL = Intent(Intent.ACTION_VIEW)
							openURL.data = Uri.parse(content)
							startActivity(openURL)
						}
						
					}
					"volume" -> volume_bar.progress = reception.split("|")[1].trim().toInt()
				}
				
				btAdapter = BluetoothAdapter.getDefaultAdapter()
				val device: BluetoothDevice = btAdapter.getRemoteDevice(btAddress)
				btSocket = device.createInsecureRfcommSocketToServiceRecord(deviceUUID)
				BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
				btSocket!!.connect()
				return
			} catch (e: Exception) {
				e.printStackTrace()
				toast("Unable to establish Bluetooth connection")
				ConnectToDevice(this).execute()
			}
		} else {
			toast("Unable to establish Bluetooth connection")
		}
	}

//	private fun handleDeepLink(data: Uri?) {
//		try {
//			when (data!!.path) {
//				DeepLink.KEYSTROKE -> {
//					when (data.getQueryParameter(DeepLink.Params.ACTIVITY_TYPE).orEmpty()) {
//						DeepLink.Params.ALPHA -> sendCommand(Commands.Keystroke.alpha)
//						DeepLink.Params.BETA -> sendCommand(Commands.Keystroke.beta)
//						DeepLink.Params.GAMMA -> sendCommand(Commands.Keystroke.gamma)
//						DeepLink.Params.DELTA -> sendCommand(Commands.Keystroke.delta)
//					}
//				}
//				DeepLink.VOLUME_INCREASE -> sendCommand(Commands.Volume.increase)
//				DeepLink.VOLUME_DECREASE -> sendCommand(Commands.Volume.decrease)
//				DeepLink.MUTE -> sendCommand(Commands.Volume.mute)
//				DeepLink.SHUTDOWN -> sendCommand(Commands.Power.shutdown)
//				DeepLink.RESTART -> sendCommand(Commands.Power.restart)
//				DeepLink.LOCK -> sendCommand(Commands.Access.lock)
//				DeepLink.LOGOUT -> sendCommand(Commands.Access.logout)
//				else -> {
//					Log.i("d", "Unable to handle path")
//				}
//			}
//		} catch (e: java.lang.Exception) {
//			Log.i("d", "Received null DeepLink")
//		}
//	}
	
	private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
		private var connectSuccess: Boolean = true
		private val context: Context
		
		init {
			this.context = c
		}
		
		override fun onPreExecute() {
			super.onPreExecute()
			progress.visibility = View.VISIBLE;
		}
		
		override fun doInBackground(vararg p0: Void?): String? {
			try {
				Log.i("log", "doInBackground: $isConnected | $btSocket")
				if (btSocket == null || !isConnected) {
					btAdapter = BluetoothAdapter.getDefaultAdapter()
					val device: BluetoothDevice = btAdapter.getRemoteDevice(btAddress)
					btSocket = device.createInsecureRfcommSocketToServiceRecord(deviceUUID)
					BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
					btSocket!!.connect()
					Log.i("log", "Connected")
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
				Log.i("log", "couldn't connect")
			} else {
				connectSuccess = true
			}
			progress.visibility = View.INVISIBLE;
		}
	}
	
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == OPEN_SETTINGS) {
			if (resultCode == Activity.RESULT_OK) {
				val returnedDevice = data?.getStringExtra(SettingsActivity.EXTRA_ADDRESS)
				val returnedDeviceName = data?.getStringExtra(SettingsActivity.EXTRA_NAME)
				Log.i("log", "Got Device: $returnedDevice")
				device_label.text = returnedDeviceName
				
				if (returnedDevice != null) {
					btAddress = returnedDevice.toString()
					btDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(btAddress)
					
					ConnectToDevice(this).execute()
					
					val preferences = this.getPreferences(Context.MODE_PRIVATE) ?: return
					with(preferences.edit()) {
						putString(getString(R.string.preferences_key), btAddress)
						commit()
					}
				}
			}
		}
	}
}
