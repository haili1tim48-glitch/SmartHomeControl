package com.example.smarthomecontrol;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

/**
 * Screen 1 – Gesture Selection.
 * Displays a Spinner with 17 gestures. On selection, navigates to
 * ExpertVideoActivity (Screen 2) passing the gesture label as an extra.
 *
 * Privacy: No user face is captured or displayed on this screen.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spinner = findViewById(R.id.gesture_spinner);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean isInitialSelection = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (isInitialSelection) {
                    isInitialSelection = false;
                    return;
                }

                String displayName = parent.getItemAtPosition(position).toString();
                String gestureLabel = GestureConstants.GESTURE_MAP.get(displayName);

                Intent intent = new Intent(MainActivity.this, ExpertVideoActivity.class);
                intent.putExtra("EXTRA_GESTURE_LABEL", gestureLabel);
                startActivity(intent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action required
            }
        });
    }
}
