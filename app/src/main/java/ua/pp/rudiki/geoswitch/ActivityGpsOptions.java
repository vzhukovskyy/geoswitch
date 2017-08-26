package ua.pp.rudiki.geoswitch;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.RadioGroup;

public class ActivityGpsOptions extends AppCompatActivity {
    private final static String TAG = ActivityGpsOptions.class.getSimpleName();

    public enum GpsActivationType {Charger, Bluetooth, Manual, CarMode};
    RadioGroup gpsOptionsRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.getLogger().debug(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_options);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        gpsOptionsRadioGroup = (RadioGroup)findViewById(R.id.gpsOptionsRadioGroup);

        loadValuesToUi();
    }

    @Override
    public void onStart() {
        super.onStart();
        App.getLogger().debug(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        App.getLogger().debug(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        App.getLogger().debug(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        App.getLogger().debug(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getLogger().debug(TAG, "onDestroy");
    }

    @Override
    public void onBackPressed() {
        if(!isFormChanged()) {
            closeActivity(Activity.RESULT_CANCELED);
            return;
        }

        storeValues();

        closeActivity(Activity.RESULT_OK);
    }

    private void closeActivity(int result) {
        Intent resultIntent = new Intent();
        setResult(result, resultIntent);
        finish();
    }

    // data persistence

    private boolean isFormChanged() {
        GpsActivationType type = App.getPreferences().getGpsActivationOption();
        int radioId = gpsActivationTypeToRadioId(type);
        return gpsOptionsRadioGroup.getCheckedRadioButtonId() != radioId;
    }

    private void storeValues() {
        int radioId = gpsOptionsRadioGroup.getCheckedRadioButtonId();
        GpsActivationType type = radioIdToGpsActivationType(radioId);
        App.getPreferences().storeGpsActivationOption(type);
    }

    private void loadValuesToUi() {
        GpsActivationType type = App.getPreferences().getGpsActivationOption();
        int checkedRadioId = gpsActivationTypeToRadioId(type);
        gpsOptionsRadioGroup.check(checkedRadioId);
    }

    private int gpsActivationTypeToRadioId(GpsActivationType type) {
        switch(type) {
            case Manual:
                return R.id.radioManual;
            case Charger:
                return R.id.radioOnCharge;
            case Bluetooth:
                return R.id.radioOnBluetooth;
            case CarMode:
                return R.id.radioInCarMode;
            default:
                return R.id.radioManual;
        }
    }

    private GpsActivationType radioIdToGpsActivationType(int radioId) {
        switch(radioId) {
            case R.id.radioManual:
                return GpsActivationType.Manual;
            case R.id.radioOnCharge:
                return GpsActivationType.Charger;
            case R.id.radioOnBluetooth:
                return GpsActivationType.Bluetooth;
            case R.id.radioInCarMode:
                return GpsActivationType.CarMode;
            default:
                return GpsActivationType.Manual;
        }
    }
}
