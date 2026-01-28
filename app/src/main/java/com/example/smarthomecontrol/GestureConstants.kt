package com.example.smarthomecontrol

object GestureConstants {

    const val EXTRA_GESTURE_LABEL = "EXTRA_GESTURE_LABEL"

    val gestureDisplayNames = listOf(
        "Turn on lights",
        "Turn off lights",
        "Turn on fan",
        "Turn off fan",
        "Increase fan speed",
        "Decrease fan speed",
        "Set Thermostat to specified temperature",
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
    )

    val gestureLabelMap = mapOf(
        "Turn on lights" to "LightOn",
        "Turn off lights" to "LightOff",
        "Turn on fan" to "FanOn",
        "Turn off fan" to "FanOff",
        "Increase fan speed" to "FanUp",
        "Decrease fan speed" to "FanDown",
        "Set Thermostat to specified temperature" to "Set Thermo",
        "0" to "Num0",
        "1" to "Num1",
        "2" to "Num2",
        "3" to "Num3",
        "4" to "Num4",
        "5" to "Num5",
        "6" to "Num6",
        "7" to "Num7",
        "8" to "Num8",
        "9" to "Num9"
    )
}
