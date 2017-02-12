package ua.pp.rudiki.geoswitch;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

public class ActivityGpsOptions extends AppCompatActivity {

    RadioGroup gpsOptionsRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_options);

        gpsOptionsRadioGroup = (RadioGroup)findViewById(R.id.gpsOptionsRadioGroup);

        loadValuesToUi();
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
        GeoSwitchApp.getPreferences().storeActivationOptions(isOnCharge);
    }

    private void loadValuesToUi() {
        boolean isOnCharge = GeoSwitchApp.getPreferences().getActivateOnCharger();
        gpsOptionsRadioGroup.check(isOnCharge ? R.id.radioOnCharge : R.id.radioManual);
    }

}
