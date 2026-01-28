package com.example.smarthomecontrol

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val spinner = findViewById<Spinner>(R.id.gesture_spinner)
        val btnConfirm = findViewById<Button>(R.id.btn_confirm_gesture)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            GestureConstants.gestureDisplayNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        btnConfirm.setOnClickListener {
            val displayName = spinner.selectedItem as? String ?: return@setOnClickListener
            val gestureLabel = GestureConstants.gestureLabelMap[displayName] ?: return@setOnClickListener

            val intent = Intent(this, GestureActivity::class.java)
            intent.putExtra(GestureConstants.EXTRA_GESTURE_LABEL, gestureLabel)
            startActivity(intent)
        }
    }
}
