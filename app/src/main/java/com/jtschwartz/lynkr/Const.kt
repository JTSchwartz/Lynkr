package com.jtschwartz.lynkr

object Commands {
	object Access {
		const val logout = "access|logout"
		const val lock = "access|lock"
	}
	
	object Keystroke {
		const val alpha = "keystroke|alpha"
		const val beta = "keystroke|beta"
		const val gamma = "keystroke|gamma"
		const val delta = "keystroke|delta"
	}
	
	object Media {
		const val next = "media|next"
		const val playPause = "media|playpause"
		const val prev = "media|previous"
	}
	
	object Power {
		const val hibernate = "power|hibernate"
		const val restart = "power|restart"
		const val shutdown = "power|shutdown"
	
	}
	
	object Transfer {
		const val get = "transfer|get"
		const val set = "transfer|set|"
	}
	
	object Volume {
		const val decrease = "volume|decrease"
		const val increase = "volume|increase"
		const val mute = "volume|mute"
		val set = {level: Int -> "volume|$level"}
	
	}
}

object DeepLink {
	const val VOLUME_INCREASE = "/increase"
	const val VOLUME_DECREASE = "/decrease"
	const val MUTE = "/mute"
	const val KEYSTROKE = "/keystroke"
	const val SHUTDOWN = "/shutdown"
	const val RESTART = "/restart"
	const val LOGOUT = "/logout"
	const val LOCK = "/lock"
	
	object Params {
		const val ACTIVITY_TYPE = "keystrokeIdentifier"
		const val ALPHA = "alpha"
		const val BETA = "beta"
		const val GAMMA = "gamma"
		const val DELTA = "delta"
	}
	
	object Actions {
		const val ACTION_TOKEN_EXTRA = "actions.fulfillment.extra.ACTION_TOKEN"
	}
}