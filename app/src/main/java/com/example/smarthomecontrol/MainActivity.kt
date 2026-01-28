package com.example.smarthomecontrol

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    private var isInitialSelection = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val spinner = findViewById<Spinner>(R.id.gesture_spinner)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            GestureConstants.gestureDisplayNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (isInitialSelection) {
                    isInitialSelection = false
                    return
                }

                val displayName = GestureConstants.gestureDisplayNames[position]
                val gestureLabel = GestureConstants.gestureLabelMap[displayName] ?: return

                val intent = Intent(this@MainActivity, GestureActivity::class.java)
                intent.putExtra(GestureConstants.EXTRA_GESTURE_LABEL, gestureLabel)
                startActivity(intent)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}
