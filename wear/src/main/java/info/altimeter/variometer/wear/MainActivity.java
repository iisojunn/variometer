package info.altimeter.variometer.wear;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.preference.PreferenceManager;

import info.altimeter.variometer.common.Variometer;
import info.altimeter.variometer.common.VerticalSpeedIndicator;

public class MainActivity extends Activity {

    private final static String TAG = "MainActivity";
    private SharedPreferences pref;
    private Variometer variometer;
    private VerticalSpeedIndicator vsiView;
    static final int REQUEST_CODE_ABOUT = 1;
    static final int REQUEST_CODE_CALIBRATION = 2;
    double[] kB = { 1.0, 1.0, 1.0 };
    double[] kC = { 0.0, 0.0, 0.0 };
    boolean calibrated = false;


    class SpeedListener implements Variometer.VariometerListener {

        @Override
        public void onStateUpdate(float h, float v) {
            vsiView.setVSpeed(v);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;

        switch (item.getItemId()) {
/*
            case R.id.defaults:
                SharedPreferences.Editor editor = pref.edit();
                editor.clear();
                editor.apply();
                recreate();
                break;
*/
            case R.id.calibration:
                intent = new Intent(this, CalibrationActivity.class);
                startActivityForResult(intent, REQUEST_CODE_CALIBRATION);
                break;
/*
            case R.id.about:
                Dialog aDialog = new Dialog(this, R.style.Dialog);
                aDialog.setContentView(R.layout.about);
                aDialog.setTitle(getString(R.string.about));
                aDialog.setCancelable(true);
                aDialog.show();
                break;
*/
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    private void startCalibration() {
        Intent intent;
        intent = new Intent(this, CalibrationActivity.class);
        startActivityForResult(intent, REQUEST_CODE_CALIBRATION);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){

        if (event.getRepeatCount() == 0) {
            if (keyCode == KeyEvent.KEYCODE_STEM_1) {
                startCalibration();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_STEM_2) {
                // Do stuff
                openOptionsMenu();
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_STEM_3) {
                // Do stuff
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        overrideCalibration();
        variometer = new Variometer(true, 10);
        vsiView = findViewById(R.id.vsi);

        variometer.setListener(new SpeedListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        calibrated = pref.getBoolean("calibrated", false);
        if (calibrated) {
            kB[0] = pref.getFloat("k_b_x", 0) + 1.0;
            kB[1] = pref.getFloat("k_b_y", 0) + 1.0;
            kB[2] = pref.getFloat("k_b_z", 0) + 1.0;
            kC[0] = pref.getFloat("k_c_x", 0);
            kC[1] = pref.getFloat("k_c_y", 0);
            kC[2] = pref.getFloat("k_c_z", 0);
        } else {
            startCalibration();
        }
        variometer.setAccelerometerCorrection(kB, kC);
        variometer.start(this);
    }

    void overrideCalibration() {
        Log.d(TAG, "Overriding calibration");
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat("k_b_x", (float) (1.0015595843584697 - 1.0));
        editor.putFloat("k_b_y", (float) (0.9991079216209161 - 1.0));
        editor.putFloat("k_b_z", (float) (0.9902745334796245 - 1.0));
        editor.putFloat("k_c_x", (float) -0.056);
        editor.putFloat("k_c_y", (float) 0.08);
        editor.putFloat("k_c_z", (float) 0.36);
        editor.putBoolean("calibrated", true);
        editor.apply();
    }

    @Override
    protected void onPause() {
        variometer.stop(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        variometer.stop(this);
        super.onDestroy();
    }
}
