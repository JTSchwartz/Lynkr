package com.jtschwartz.lynkr

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
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
		startActivity(openSettings)
	}
}
