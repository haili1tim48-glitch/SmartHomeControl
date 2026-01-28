package com.example.smarthomecontrol;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

/**
 * Unit test for GestureConstants — verifies the GESTURE_MAP contains
 * exactly 17 entries with the correct display-name → label mappings.
 */
public class GestureConstantsTest {

    @Test
    public void gestureMap_containsExactly17Entries() {
        Map<String, String> map = GestureConstants.GESTURE_MAP;
        assertNotNull("GESTURE_MAP should not be null", map);
        assertEquals("GESTURE_MAP should contain 17 entries", 17, map.size());
    }

    @Test
    public void gestureMap_turnOnLights_mapsToLightOn() {
        assertEquals("LightOn", GestureConstants.GESTURE_MAP.get("Turn on lights"));
    }

    @Test
    public void gestureMap_turnOffLights_mapsToLightOff() {
        assertEquals("LightOff", GestureConstants.GESTURE_MAP.get("Turn off lights"));
    }

    @Test
    public void gestureMap_turnOnFan_mapsToFanOn() {
        assertEquals("FanOn", GestureConstants.GESTURE_MAP.get("Turn on fan"));
    }

    @Test
    public void gestureMap_turnOffFan_mapsToFanOff() {
        assertEquals("FanOff", GestureConstants.GESTURE_MAP.get("Turn off fan"));
    }

    @Test
    public void gestureMap_increaseFanSpeed_mapsToFanSpeedUp() {
        assertEquals("FanSpeedUp", GestureConstants.GESTURE_MAP.get("Increase fan speed"));
    }

    @Test
    public void gestureMap_decreaseFanSpeed_mapsToFanSpeedDown() {
        assertEquals("FanSpeedDown", GestureConstants.GESTURE_MAP.get("decrease fan speed"));
    }

    @Test
    public void gestureMap_setThermostat_mapsToSetThermo() {
        assertEquals("SetThermo", GestureConstants.GESTURE_MAP.get("Set Thermostat to specified temperature"));
    }

    @Test
    public void gestureMap_numbers0Through9_mapToNumLabels() {
        for (int i = 0; i <= 9; i++) {
            String key = String.valueOf(i);
            String expected = "Num" + i;
            assertTrue("Map should contain key: " + key,
                    GestureConstants.GESTURE_MAP.containsKey(key));
            assertEquals("Key '" + key + "' should map to '" + expected + "'",
                    expected, GestureConstants.GESTURE_MAP.get(key));
        }
    }

    @Test
    public void gestureMap_allKeysHaveNonNullValues() {
        for (Map.Entry<String, String> entry : GestureConstants.GESTURE_MAP.entrySet()) {
            assertNotNull("Value for key '" + entry.getKey() + "' should not be null",
                    entry.getValue());
            assertTrue("Value for key '" + entry.getKey() + "' should not be empty",
                    entry.getValue().length() > 0);
        }
    }
}
