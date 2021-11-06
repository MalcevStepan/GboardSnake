package com.example.gboard.activities

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import com.example.gboard.data.Levels
import com.example.gboard.R
import com.example.gboard.players.Snake
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : GboardActivity() {

	private val levels by lazy {
		Levels()
	}

	private var isMultiplayer = false
	private var level = 0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_game)
		val arguments = intent.extras
		if (arguments != null) {
			level = arguments.getInt("Level", 0)
			isMultiplayer = arguments.getBoolean("Multiplayer", false)
		}
		setBackground(level)

		gameView.level = levels.getLevel(0/*level*/)
		gameView.players = arrayOf(Snake())
	}

	private fun setBackground(index: Int) {
		when (index) {
			0 -> game_layout.setBackgroundResource(R.drawable.arctic)
			1 -> game_layout.setBackgroundResource(R.drawable.desert)
			2 -> game_layout.setBackgroundResource(R.drawable.jungle)
		}
	}

	override fun onWindowFocusChanged(hasFocus: Boolean) {
		super.onWindowFocusChanged(hasFocus)
		if (hasFocus) {
			window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				window.setDecorFitsSystemWindows(false)
				window.insetsController!!.hide(WindowInsets.Type.navigationBars())
				window.insetsController!!.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
			}
		} else {
			window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
					or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
					or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					or View.SYSTEM_UI_FLAG_VISIBLE)
		}
	}
}