package com.example.smarthomecontrol;

import android.widget.Spinner;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

/**
 * Espresso instrumented test for MainActivity.
 * Verifies the gesture_spinner is visible and contains exactly 17 items.
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void gestureSpinner_isDisplayed() {
        onView(withId(R.id.gesture_spinner))
                .check(matches(isDisplayed()));
    }

    @Test
    public void gestureSpinner_containsExactly17Items() {
        activityRule.getScenario().onActivity(activity -> {
            Spinner spinner = activity.findViewById(R.id.gesture_spinner);
            assertEquals("Spinner should contain exactly 17 items",
                    17, spinner.getAdapter().getCount());
        });
    }
}
