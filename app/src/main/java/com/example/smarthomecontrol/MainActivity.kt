package com.example.smarthomecontrol

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.smarthomecontrol.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedGesture: Gesture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGestureSpinner()
        setupNextButton()
    }

    private fun setupGestureSpinner() {
        val gestureNames = Gesture.ALL_GESTURES.map { it.displayName }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            gestureNames
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.gestureSpinner.adapter = adapter

        binding.gestureSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedGesture = Gesture.ALL_GESTURES[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedGesture = null
            }
        }

        // Select first item by default
        if (Gesture.ALL_GESTURES.isNotEmpty()) {
            binding.gestureSpinner.setSelection(0)
            selectedGesture = Gesture.ALL_GESTURES[0]
        }
    }

    private fun setupNextButton() {
        binding.nextButton.setOnClickListener {
            selectedGesture?.let { gesture ->
                val intent = Intent(this, VideoPlayActivity::class.java).apply {
                    putExtra(EXTRA_GESTURE_NAME, gesture.name)
                }
                startActivity(intent)
            }
        }
    }

    companion object {
        const val EXTRA_GESTURE_NAME = "extra_gesture_name"
    }
}
