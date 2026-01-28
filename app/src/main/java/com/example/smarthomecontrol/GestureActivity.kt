package com.example.smarthomecontrol

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity

class GestureActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gestureLabel = intent.getStringExtra(GestureConstants.EXTRA_GESTURE_LABEL) ?: ""

        val textView = TextView(this).apply {
            text = gestureLabel
            textSize = 24f
            setPadding(32, 32, 32, 32)
        }
        setContentView(textView)
    }
}
