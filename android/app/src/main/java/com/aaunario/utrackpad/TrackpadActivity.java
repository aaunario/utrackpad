package com.aaunario.utrackpad;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ToggleButton;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static android.text.TextUtils.substring;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class TrackpadActivity extends AppCompatActivity implements SensorEventListener{
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private static final String HOST = "192.168.47.10";
    private static final int PORT = 50047;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            //mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnClickListener onToggleButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ToggleButton toggleBtn = findViewById(R.id.toggle_button);
            setMouseUpdatesEnabled(!toggleBtn.isChecked());
        }

    };

    private void setMouseUpdatesEnabled(boolean enabled) {
        if (!enabled) {
            Intent test = new Intent(this, NetworkService.class);
            Uri data = Uri.parse("test msg from ANDROID");
            test.setData(data);
            startService(test);
            mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
        else {

           System.out.println("closing connection");
            mSensorManager.unregisterListener(this);
            Intent test = new Intent(this, NetworkService.class);
            Uri data = Uri.parse("close mouse functionality from ANDROID");
                test.setData(data);
            startService(test);
        }
    }

    private Display mDisplay;

    private SensorManager mSensorManager;
    private WindowManager mWindowManager;
    private Sensor mAccelerometer;
/*

    private Socket mSocket;
    private DataOutputStream mStreamWriter;
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_trackpad);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();


        findViewById(R.id.toggle_button).setOnClickListener(onToggleButtonClick);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        System.out.println("delayedHide()");
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent stopNetworkServiceIntent = new Intent(this, NetworkService.class);
        stopNetworkServiceIntent.setData(Uri.parse("close"));
        startService(stopNetworkServiceIntent);
/*        if (mSocket != null) {
            try {
                if (mSocket.isConnected())
                    mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    @Override
    public void onSensorChanged(final SensorEvent event) {
        float [] values = event.values;
        final float rotX = -1.00f * values[0],
                    rotY = /*-1.00f **/ values[1];

        //reduce precision
        String xStr = String.valueOf(rotX);
        String yStr = String.valueOf(rotY);
        int decimalIndex = xStr.indexOf('.');
        xStr = substring(xStr, 0, decimalIndex + 2);
        decimalIndex = yStr.indexOf('.');
        yStr = substring(yStr, 0, decimalIndex + 2);
        String cmd = "p mv " + xStr + " " + yStr;

        sendMessage(cmd);

    }

    private void sendMessage(final String msg)
    {
        Intent sendIntent = new Intent(this, NetworkService.class);
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setData(Uri.parse(msg));
        startService(sendIntent);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onMoveButtonClick(View view) {

        System.out.println("onMOVEbuttoncclicked");
        sendMessage("p mv 100 100");
    }

    public void onUpClick(View view) {
        sendMessage("p mv 0 -1");
    }

    public void onLeftClick(View view) {
        sendMessage("p mv -1 0");
    }

    public void onDownClick(View view) {
        sendMessage("p mv 0 1");
    }

    public void onRightClick(View view) {

        sendMessage("p mv 1 0");
    }

}
