package com.example.smarthomecontrol;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps gesture display names (shown in the Spinner) to their
 * corresponding gesture labels used for inter-activity communication.
 */
public final class GestureConstants {

    public static final Map<String, String> GESTURE_MAP;

    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Turn on lights", "LightOn");
        map.put("Turn off lights", "LightOff");
        map.put("Turn on fan", "FanOn");
        map.put("Turn off fan", "FanOff");
        map.put("Increase fan speed", "FanSpeedUp");
        map.put("decrease fan speed", "FanSpeedDown");
        map.put("Set Thermostat to specified temperature", "SetThermo");
        map.put("0", "Num0");
        map.put("1", "Num1");
        map.put("2", "Num2");
        map.put("3", "Num3");
        map.put("4", "Num4");
        map.put("5", "Num5");
        map.put("6", "Num6");
        map.put("7", "Num7");
        map.put("8", "Num8");
        map.put("9", "Num9");
        GESTURE_MAP = Collections.unmodifiableMap(map);
    }

    private GestureConstants() {
        // Prevent instantiation
    }
}
