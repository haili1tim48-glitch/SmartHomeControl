package com.example.smarthomecontrol

data class Gesture(
    val name: String,
    val displayName: String,
    val videoFileName: String
) {
    companion object {
        // All 17 gestures
        val ALL_GESTURES = listOf(
            // Action gestures
            Gesture("LightOn", "Light On", "lighton"),
            Gesture("LightOff", "Light Off", "lightoff"),
            Gesture("FanOn", "Fan On", "fanon"),
            Gesture("FanOff", "Fan Off", "fanoff"),
            Gesture("FanUp", "Fan Up", "fanup"),
            Gesture("FanDown", "Fan Down", "fandown"),
            Gesture("SetThermo", "Set Thermo", "setthermo"),
            // Number gestures
            Gesture("Num0", "Number 0", "num0"),
            Gesture("Num1", "Number 1", "num1"),
            Gesture("Num2", "Number 2", "num2"),
            Gesture("Num3", "Number 3", "num3"),
            Gesture("Num4", "Number 4", "num4"),
            Gesture("Num5", "Number 5", "num5"),
            Gesture("Num6", "Number 6", "num6"),
            Gesture("Num7", "Number 7", "num7"),
            Gesture("Num8", "Number 8", "num8"),
            Gesture("Num9", "Number 9", "num9")
        )

        fun getByName(name: String): Gesture? {
            return ALL_GESTURES.find { it.name == name }
        }
    }
}
