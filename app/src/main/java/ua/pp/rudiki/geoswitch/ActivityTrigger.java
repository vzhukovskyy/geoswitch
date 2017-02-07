package ua.pp.rudiki.geoswitch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import ua.pp.rudiki.geoswitch.peripherals.ConversionUtils;
import ua.pp.rudiki.geoswitch.peripherals.Preferences;
import ua.pp.rudiki.geoswitch.trigger.A2BTrigger;
import ua.pp.rudiki.geoswitch.trigger.AreaTrigger;
import ua.pp.rudiki.geoswitch.trigger.GeoArea;
import ua.pp.rudiki.geoswitch.trigger.GeoPoint;
import ua.pp.rudiki.geoswitch.trigger.TriggerType;


public class ActivityTrigger extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    final String TAG = getClass().getSimpleName();
    final int SELECT_COORDINATES_REQUEST_ID = 9001;

    RadioGroup triggerTypeRadioGroup;
    RelativeLayout bidirectionalLayout, unidirectionalLayout;
    EditText latitudeEditBi, longitudeEditBi, radiusEditBi;
    EditText latitudeFromEditUni, longitudeFromEditUni, latitudeToEditUni, longitudeToEditUni, radiusEditUni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger);

        triggerTypeRadioGroup = (RadioGroup) findViewById(R.id.triggerTypeRadioGroup);
        triggerTypeRadioGroup.setOnCheckedChangeListener(this);
        bidirectionalLayout = (RelativeLayout) findViewById(R.id.bidirectionalGroup);
        unidirectionalLayout = (RelativeLayout) findViewById(R.id.unidirectionalGroup);

        latitudeEditBi = (EditText) findViewById(R.id.latitudeEditBi);
        latitudeEditBi.setKeyListener(null); // read-only
        longitudeEditBi = (EditText) findViewById(R.id.longitudeEditBi);
        longitudeEditBi.setKeyListener(null);
        radiusEditBi = (EditText) findViewById(R.id.radiusEditBi);

        latitudeFromEditUni = (EditText) findViewById(R.id.latitudeFromEditUni);
        latitudeFromEditUni.setKeyListener(null);
        longitudeFromEditUni = (EditText) findViewById(R.id.longitudeFromEditUni);
        longitudeFromEditUni.setKeyListener(null);
        latitudeToEditUni = (EditText) findViewById(R.id.latitudeToEditUni);
        latitudeToEditUni.setKeyListener(null);
        longitudeToEditUni = (EditText) findViewById(R.id.longitudeToEditUni);
        longitudeToEditUni.setKeyListener(null);
        radiusEditUni = (EditText) findViewById(R.id.radiusEditUni);
        radiusEditUni.setKeyListener(null);

        loadValuesToUi();
    }

    // UI handlers

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        boolean isArea = (triggerTypeRadioGroup.getCheckedRadioButtonId() == R.id.radioEnterArea);
        bidirectionalLayout.setVisibility(isArea ? View.VISIBLE : View.GONE);
        unidirectionalLayout.setVisibility(isArea ? View.GONE : View.VISIBLE);
    }

    public void onOkClick(View view) {
        boolean validatedOk = storeValues();
        if (validatedOk) {
            Intent resultIndent = new Intent();
            setResult(Activity.RESULT_OK, resultIndent);
            finish();
        } else {
            String message = getString(R.string.trigger_validation_failed);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    public void onCancelClick(View view) {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();
    }

    public void onMapClick(View view) {
        Intent intent = new Intent(this, ActivityMap.class);

        intent.putExtra(Preferences.triggerTypeKey, getSelectedTriggerType().name());
        intent.putExtra(Preferences.latitudeKey, getLatitudeEditValueAsString());
        intent.putExtra(Preferences.longitudeKey, getLongitudeEditValueAsString());
        intent.putExtra(Preferences.latitudeToKey, latitudeToEditUni.getText().toString());
        intent.putExtra(Preferences.longitudeToKey, longitudeToEditUni.getText().toString());
        intent.putExtra(Preferences.radiusKey, radiusEditBi.getText().toString());

        startActivityForResult(intent, SELECT_COORDINATES_REQUEST_ID);
    }

    // return value from the map activity

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode="+requestCode);
        if (requestCode == SELECT_COORDINATES_REQUEST_ID && data != null) {
            String latitude = data.getStringExtra(Preferences.latitudeKey);
            String longitude = data.getStringExtra(Preferences.longitudeKey);
            String radius = data.getStringExtra(Preferences.radiusKey);
            String latitudeTo = data.getStringExtra(Preferences.latitudeToKey);
            String longitudeTo = data.getStringExtra(Preferences.longitudeToKey);
            Log.d(TAG, "Received from map ("+latitude+","+longitude+"), ("+latitudeTo+","+longitudeTo+"), R="+radius);

            if(latitude != null && longitude != null) {
                latitudeEditBi.setText(latitude);
                longitudeEditBi.setText(longitude);
                radiusEditBi.setText(radius);

                latitudeFromEditUni.setText(latitude);
                longitudeFromEditUni.setText(longitude);
                radiusEditUni.setText(radius);
            }
            if(latitudeTo != null && longitudeTo != null) {
                latitudeToEditUni.setText(latitudeTo);
                longitudeToEditUni.setText(longitudeTo);
            }
        }
    }

    // data persistence

    private boolean storeValues() {
        TriggerType triggerType = getSelectedTriggerType();

        if(triggerType == TriggerType.EnterArea) {
            double latitude = ConversionUtils.toDouble(latitudeEditBi.getText().toString());
            double longitude = ConversionUtils.toDouble(longitudeEditBi.getText().toString());
            double radius = ConversionUtils.toDouble(radiusEditBi.getText().toString());
            if(Double.isNaN(latitude) || Double.isNaN(longitude) || Double.isNaN(radius)) {
                return false;
            }

            GeoArea area = new GeoArea(latitude, longitude, radius);
            AreaTrigger areaTrigger = new AreaTrigger(area);

            GeoSwitchApp.getPreferences().storeAreaTrigger(areaTrigger);
        }
        else {
            double latitudeFrom = ConversionUtils.toDouble(latitudeFromEditUni.getText().toString());
            double longitudeFrom = ConversionUtils.toDouble(longitudeFromEditUni.getText().toString());
            double latitudeTo = ConversionUtils.toDouble(latitudeToEditUni.getText().toString());
            double longitudeTo = ConversionUtils.toDouble(longitudeToEditUni.getText().toString());
            double radius = ConversionUtils.toDouble(radiusEditBi.getText().toString());
            if(Double.isNaN(latitudeFrom) || Double.isNaN(longitudeFrom) ||
                    Double.isNaN(latitudeTo) || Double.isNaN(longitudeTo) || Double.isNaN(radius))
            {
                return false;
            }

            GeoPoint pointFrom = new GeoPoint(latitudeFrom, longitudeFrom);
            GeoPoint pointTo = new GeoPoint(latitudeTo, longitudeTo);
            A2BTrigger a2bTrigger = new A2BTrigger(pointFrom, pointTo);

            GeoSwitchApp.getPreferences().storeA2BTrigger(a2bTrigger);
        }

        GeoSwitchApp.getPreferences().storeTriggerType(triggerType);

        return true;
    }

    private void loadValuesToUi() {
        TriggerType storedTriggerType = GeoSwitchApp.getPreferences().getTriggerType();
        boolean isArea = (storedTriggerType != TriggerType.Transition);;
        int radioId = isArea ? R.id.radioEnterArea : R.id.radioFromTo;
        triggerTypeRadioGroup.check(radioId);
        onCheckedChanged(triggerTypeRadioGroup, radioId);

        latitudeEditBi.setText(GeoSwitchApp.getPreferences().getLatitudeAsString());
        longitudeEditBi.setText(GeoSwitchApp.getPreferences().getLongitudeAsString());
        radiusEditBi.setText(GeoSwitchApp.getPreferences().getRadiusAsString());

        latitudeFromEditUni.setText(GeoSwitchApp.getPreferences().getLatitudeAsString());
        longitudeFromEditUni.setText(GeoSwitchApp.getPreferences().getLongitudeAsString());
        latitudeToEditUni.setText(GeoSwitchApp.getPreferences().getLatitudeToAsString());
        longitudeToEditUni.setText(GeoSwitchApp.getPreferences().getLongitudeToAsString());
        radiusEditUni.setText(GeoSwitchApp.getPreferences().getRadiusAsString());

    }

    // accessors

    TriggerType getSelectedTriggerType() {
        boolean isArea = (triggerTypeRadioGroup.getCheckedRadioButtonId() == R.id.radioEnterArea);
        return isArea ? TriggerType.EnterArea : TriggerType.Transition;
    }

    String getLatitudeEditValueAsString() {
        TriggerType type = getSelectedTriggerType();
        if(type == TriggerType.EnterArea)
            return latitudeEditBi.getText().toString();
        else
            return latitudeFromEditUni.getText().toString();
    }

    String getLongitudeEditValueAsString() {
        TriggerType type = getSelectedTriggerType();
        if(type == TriggerType.EnterArea)
            return longitudeEditBi.getText().toString();
        else
            return longitudeFromEditUni.getText().toString();
    }
}
