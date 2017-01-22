package ua.pp.rudiki.geoswitch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.util.NoSuchElementException;

import ua.pp.rudiki.geoswitch.peripherals.ConversionUtils;
import ua.pp.rudiki.geoswitch.peripherals.Preferences;
import ua.pp.rudiki.geoswitch.trigger.A2BTrigger;
import ua.pp.rudiki.geoswitch.trigger.AreaTrigger;
import ua.pp.rudiki.geoswitch.trigger.GeoArea;
import ua.pp.rudiki.geoswitch.trigger.GeoPoint;
import ua.pp.rudiki.geoswitch.trigger.TriggerType;


public class ActivityTrigger extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    final String TAG = getClass().getSimpleName();
    final int SELECT_COORDINATES_REQUEST_ID = 9001;

    Spinner triggerTypeSpinner;
    RelativeLayout bidirectionalLayout, unidirectionalLayout;
    EditText latitudeEditBi, longitudeEditBi, radiusEditBi;
    EditText latitudeFromEditUni, longitudeFromEditUni, latitudeToEditUni, longitudeToEditUni, radiusEditUni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger);

        triggerTypeSpinner = (Spinner) findViewById(R.id.triggerTypeSpinner);
        triggerTypeSpinner.setOnItemSelectedListener(this);
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int type = triggerTypeSpinner.getSelectedItemPosition();
        boolean bidirectional = (type == 0);
        bidirectionalLayout.setVisibility(bidirectional ? View.VISIBLE : View.GONE);
        unidirectionalLayout.setVisibility(bidirectional ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
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
        if (requestCode == SELECT_COORDINATES_REQUEST_ID) {
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

    private void storeValues() {
        TriggerType triggerType = getSelectedTriggerType();

        GeoSwitchApp.getPreferences().storeTriggerType(triggerType);

        if(triggerType == TriggerType.Bidirectional) {
            double latitude = ConversionUtils.toDouble(latitudeEditBi.getText().toString());
            double longitude = ConversionUtils.toDouble(longitudeEditBi.getText().toString());
            double radius = ConversionUtils.toDouble(radiusEditBi.getText().toString());

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

            GeoPoint pointFrom = new GeoPoint(latitudeFrom, longitudeFrom);
            GeoPoint pointTo = new GeoPoint(latitudeTo, longitudeTo);
            A2BTrigger a2bTrigger = new A2BTrigger(pointFrom, pointTo);

            GeoSwitchApp.getPreferences().storeA2BTrigger(a2bTrigger);
        }
    }

    private void loadValuesToUi() {
        triggerTypeSpinner.setSelection(GeoSwitchApp.getPreferences().getTriggerType().getValue());

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
        int pos = triggerTypeSpinner.getSelectedItemPosition();
        return TriggerType.valueOf(pos);
    }

    String getLatitudeEditValueAsString() {
        TriggerType type = getSelectedTriggerType();
        if(type == TriggerType.Bidirectional)
            return latitudeEditBi.getText().toString();
        else
            return latitudeFromEditUni.getText().toString();
    }

    String getLongitudeEditValueAsString() {
        TriggerType type = getSelectedTriggerType();
        if(type == TriggerType.Bidirectional)
            return longitudeEditBi.getText().toString();
        else
            return longitudeFromEditUni.getText().toString();
    }
}
