package info.altimeter.variometer.wear;

import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Arrays;

import info.altimeter.variometer.common.Variometer;

public class CalibrationActivity extends Activity {

    private static final String TAG = "CalibrationActivity";
    private SharedPreferences pref;
    private TextView textStep;
    private TextView textNext;
    VibrationEffect effect;
    SensorManager manager;
    Sensor accelerometer;
    double[] kA = new double[3];
    double[] weights = new double[3];
    double[] biases = new double[3];
    double[] data;
    int calibrationIndex = 0;
    AccelerationListener listener;
    InternalMessageHandler handler;
    static final int MAX_POINTS = 6;
    static int string_ids[] = {
            -1,
            R.string.calibration_step1,
            R.string.calibration_step2,
            R.string.calibration_step3,
            R.string.calibration_step4,
            R.string.calibration_step5,
            R.string.calibration_step6
    };

    private void returnResult(int resultCode) {
        this.setResult(resultCode);
    }

    private class AccelerationListener implements SensorEventListener {

        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        public void onSensorChanged(SensorEvent event) {
            if (calibrationIndex == 0)
                return;

            for (int k = 0; k < 3; k += 1 ) {
                data[(calibrationIndex - 1) * 3 + k] = event.values[k];
            }
        }
    }

    public class InternalMessageHandler extends Handler {
        @Override
        public void dispatchMessage(Message msg) {
            manager.unregisterListener(listener);
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(effect);
            if (calibrationIndex >= MAX_POINTS) {
                optimize();
                return;
            }
            calibrationIndex += 1;
            textStep.setText(getString(string_ids[calibrationIndex]));
        }
    }

    private void optimize() {
        Log.v(TAG, String.format("Data: %s", Arrays.toString(data)));
        double latitude = pref.getFloat("latitude", 45);
        double g = Variometer.localGravity(latitude);
        boolean calibrated = true;

        Log.v(TAG, String.format("Before kA: %s", Arrays.toString(kA)));
        Log.v(TAG, String.format("Before weights: %s", Arrays.toString(weights)));
        Log.v(TAG, String.format("Before biases: %s", Arrays.toString(biases)));
        Variometer.biasUpdate(weights, biases, data, calibrationIndex * 3, g);
        Log.v(TAG, String.format("After kA: %s", Arrays.toString(kA)));
        Log.v(TAG, String.format("After weights: %s", Arrays.toString(weights)));
        Log.v(TAG, String.format("After biases: %s", Arrays.toString(biases)));

        SharedPreferences.Editor editor = pref.edit();
        if (Double.isNaN(biases[0]) || Double.isNaN(biases[1]) || Double.isNaN(biases[2]))
            calibrated = false;
        editor.putFloat("k_a_x", (float) kA[0]);
        editor.putFloat("k_a_y", (float) kA[1]);
        editor.putFloat("k_a_z", (float) kA[2]);
        editor.putFloat("k_b_x", (float) (weights[0] - 1.0));
        editor.putFloat("k_b_y", (float) (weights[1] - 1.0));
        editor.putFloat("k_b_z", (float) (weights[2] - 1.0));
        editor.putFloat("k_c_x", (float) biases[0]);
        editor.putFloat("k_c_y", (float) biases[1]);
        editor.putFloat("k_c_z", (float) biases[2]);
        editor.putBoolean("calibrated", calibrated);
        editor.apply();
        returnResult(RESULT_OK);
        finish();
    }

    public class DoneButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
        }
    }

    private void buttonClicked() {
        manager.registerListener(listener, accelerometer, 50000);
        handler.sendEmptyMessageDelayed(73, 1000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){

        if (event.getRepeatCount() == 0) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_STEM_1:
                case KeyEvent.KEYCODE_STEM_2:
                case KeyEvent.KEYCODE_STEM_3:
                case KeyEvent.KEYCODE_BACK:
                    buttonClicked();
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        effect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE);

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        listener = new AccelerationListener();
        handler = new InternalMessageHandler();

        textStep = findViewById(R.id.text_step);

        textNext = findViewById(R.id.text_next);

        kA[0] = 0;
        kA[1] = 0;
        kA[2] = 0;
        weights[0] = 1;
        weights[1] = 1;
        weights[2] = 1;
        biases[0] = 0;
        biases[1] = 0;
        biases[2] = 0;
        data = new double[MAX_POINTS * 3];

        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }
}
