package ua.pp.rudiki.geoswitch;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

public class ActivityGpsOptions extends AppCompatActivity {
    private final static String TAG = ActivityGpsOptions.class.getSimpleName();

    RadioGroup gpsOptionsRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.getLogger().debug(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_options);

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

    public void onOkClick(View view) {
        storeValues();

        Intent resultIndent = new Intent();
        setResult(Activity.RESULT_OK, resultIndent);
        finish();
    }

    public void onCancelClick(View view) {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();
    }

    // data persistence

    private void storeValues() {
        boolean isOnCharge = (gpsOptionsRadioGroup.getCheckedRadioButtonId() == R.id.radioOnCharge);
        App.getPreferences().storeActivationOptions(isOnCharge);
    }

    private void loadValuesToUi() {
        boolean isOnCharge = App.getPreferences().getActivateOnCharger();
        gpsOptionsRadioGroup.check(isOnCharge ? R.id.radioOnCharge : R.id.radioManual);
    }

}
