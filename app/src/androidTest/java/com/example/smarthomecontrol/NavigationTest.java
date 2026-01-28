package com.example.smarthomecontrol;

import android.app.Activity;
import android.app.Instrumentation;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * Espresso instrumented test for navigation from MainActivity to ExpertVideoActivity.
 * Verifies that selecting "Turn on lights" sends an Intent to ExpertVideoActivity
 * with the extra "EXTRA_GESTURE_LABEL" set to "LightOn".
 */
@RunWith(AndroidJUnit4.class)
public class NavigationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void selectTurnOnLights_sendsIntentToExpertVideoActivity() {
        // Stub all intents targeting ExpertVideoActivity so the activity is not actually launched
        intending(hasComponent(ExpertVideoActivity.class.getName()))
                .respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        // Open the spinner dropdown and select "Turn on lights"
        onView(withId(R.id.gesture_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Turn on lights"))).perform(click());

        // Verify the intent was sent to ExpertVideoActivity with the correct gesture label
        intended(allOf(
                hasComponent(ExpertVideoActivity.class.getName()),
                hasExtra("EXTRA_GESTURE_LABEL", "LightOn")
        ));
    }
}
