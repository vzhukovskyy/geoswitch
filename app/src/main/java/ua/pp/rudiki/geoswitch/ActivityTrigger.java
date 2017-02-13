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
import android.widget.TextView;
import android.widget.Toast;

import ua.pp.rudiki.geoswitch.peripherals.ConversionUtils;
import ua.pp.rudiki.geoswitch.peripherals.Preferences;
import ua.pp.rudiki.geoswitch.trigger.ExitAreaTrigger;
import ua.pp.rudiki.geoswitch.trigger.GeoTrigger;
import ua.pp.rudiki.geoswitch.trigger.TransitionTrigger;
import ua.pp.rudiki.geoswitch.trigger.EnterAreaTrigger;
import ua.pp.rudiki.geoswitch.trigger.GeoArea;
import ua.pp.rudiki.geoswitch.trigger.GeoPoint;
import ua.pp.rudiki.geoswitch.trigger.TriggerType;


public class ActivityTrigger extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    final String TAG = getClass().getSimpleName();
    final int SELECT_COORDINATES_REQUEST_ID = 9001;

    RadioGroup triggerTypeRadioGroup;
    TextView triggerTypeDescriptionLabel;
    RelativeLayout bidirectionalLayout, unidirectionalLayout;
    EditText latitudeEditBi, longitudeEditBi, radiusEditBi;
    EditText latitudeFromEditUni, longitudeFromEditUni, latitudeToEditUni, longitudeToEditUni, radiusEditUni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger);

        triggerTypeRadioGroup = (RadioGroup) findViewById(R.id.triggerTypeRadioGroup);
        triggerTypeRadioGroup.setOnCheckedChangeListener(this);
        triggerTypeDescriptionLabel = (TextView) findViewById(R.id.triggerTypeDescriptionLabel);
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
        TriggerType triggerType = radioIdToTriggerType(checkedId);

        String desc;
        boolean isBidirectionalLayout;
        switch(triggerType) {
            case EnterArea:
                isBidirectionalLayout = true;
                desc = "Enter area";
                break;
            case ExitArea:
                isBidirectionalLayout = true;
                desc = "Exit area";
                break;
            case Transition:
                isBidirectionalLayout = false;
                desc = "Transition from one area to another";
                break;
            default:
                isBidirectionalLayout = true;
                desc = "";
        }

        bidirectionalLayout.setVisibility(isBidirectionalLayout ? View.VISIBLE : View.GONE);
        unidirectionalLayout.setVisibility(isBidirectionalLayout ? View.GONE : View.VISIBLE);

        triggerTypeDescriptionLabel.setText(desc);
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
        intent.putExtra(Preferences.latitudeKey, getLatitudeEditboxValue());
        intent.putExtra(Preferences.longitudeKey, getLongitudeEditboxValue());
        intent.putExtra(Preferences.latitudeToKey, getLatitudeToEditboxValue());
        intent.putExtra(Preferences.longitudeToKey, getLongitudeToEditboxValue());
        intent.putExtra(Preferences.radiusKey, getRadiusEditboxValue());

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

    // helpers

    private int triggerTypeToRadioId(TriggerType triggerType) {
        int radioId;
        switch(triggerType) {
            case Transition:
                radioId = R.id.radioTransition;
                break;
            case EnterArea:
                radioId = R.id.radioEnterArea;
                break;
            case ExitArea:
                radioId = R.id.radioExitArea;
                break;
            default:
                radioId = R.id.radioExitArea;
        }

        return radioId;
    }

    private TriggerType radioIdToTriggerType(int radioId) {
        TriggerType triggerType;
        switch(radioId) {
            case R.id.radioTransition:
                triggerType = TriggerType.Transition;
                break;
            case R.id.radioEnterArea:
                triggerType = TriggerType.EnterArea;
                break;
            case R.id.radioExitArea:
                triggerType = TriggerType.ExitArea;
                break;
            default:
                triggerType = TriggerType.ExitArea;
        }

        return triggerType;
    }

    // data persistence

    private boolean storeValues() {
        TriggerType triggerType = getSelectedTriggerType();

        double latitude = ConversionUtils.toDouble(latitudeEditBi.getText().toString());
        double longitude = ConversionUtils.toDouble(longitudeEditBi.getText().toString());
        double radius = ConversionUtils.toDouble(radiusEditBi.getText().toString());
        double latitudeTo = ConversionUtils.toDouble(latitudeToEditUni.getText().toString());
        double longitudeTo = ConversionUtils.toDouble(longitudeToEditUni.getText().toString());

        if(Double.isNaN(latitude) || Double.isNaN(longitude) || Double.isNaN(radius)) {
            return false;
        }

        GeoArea area = new GeoArea(latitude, longitude, radius);

        GeoTrigger trigger;
        if(triggerType == TriggerType.EnterArea || triggerType == TriggerType.ExitArea) {
            if(triggerType == TriggerType.EnterArea) {
                trigger = new EnterAreaTrigger(area);
            } else {
                trigger = new ExitAreaTrigger(area);
            }
        }
        else {
            // triggerType == TriggerType.Transition
            if(Double.isNaN(latitudeTo) || Double.isNaN(longitudeTo)) {
                return false;
            }

            GeoPoint pointFrom = new GeoPoint(latitude, longitude);
            GeoPoint pointTo = new GeoPoint(latitudeTo, longitudeTo);
            trigger = new TransitionTrigger(pointFrom, pointTo);
        }

        GeoSwitchApp.getPreferences().storeTrigger(trigger);
        return true;
    }

    private void loadValuesToUi() {
        GeoTrigger trigger = GeoSwitchApp.getPreferences().loadTrigger();

        int radioId;
        switch(trigger.getType()) {
            case Transition:
                radioId = R.id.radioTransition;

                TransitionTrigger transitionTrigger = (TransitionTrigger)trigger;
                GeoPoint pointA = transitionTrigger.getPointA();
                GeoPoint pointB = transitionTrigger.getPointB();
                double radius = transitionTrigger.getRadius();

                setLatitudeEditboxValue(pointA.getLatitude());
                setLongitudeEditboxValue(pointA.getLongitude());
                setRadiusEditboxValue(radius);
                setLatitudeToEditboxValue(pointB.getLatitude());
                setLongitudeToEditboxValue(pointB.getLongitude());

                break;
            case EnterArea: {
                radioId = R.id.radioEnterArea;

                EnterAreaTrigger enterAreaTrigger = (EnterAreaTrigger)trigger;
                GeoArea area = enterAreaTrigger.getArea();

                setLatitudeEditboxValue(area.getLatitude());
                setLongitudeEditboxValue(area.getLongitude());
                setRadiusEditboxValue(area.getRadius());
            }
            break;
            case ExitArea:
            default: {
                radioId = R.id.radioExitArea;

                ExitAreaTrigger exitAreaTrigger = (ExitAreaTrigger)trigger;
                GeoArea area = exitAreaTrigger.getArea();

                setLatitudeEditboxValue(area.getLatitude());
                setLongitudeEditboxValue(area.getLongitude());
                setRadiusEditboxValue(area.getRadius());
            }
            break;
        }

        triggerTypeRadioGroup.check(radioId);
        onCheckedChanged(triggerTypeRadioGroup, radioId);
    }

    // form field accessors

    TriggerType getSelectedTriggerType() {
        int radioId = triggerTypeRadioGroup.getCheckedRadioButtonId();
        return radioIdToTriggerType(radioId);
    }

    double getLatitudeEditboxValue() {
        TriggerType type = getSelectedTriggerType();

        String text;
        switch(type) {
            case EnterArea:
            case ExitArea:
            default:
                text = latitudeEditBi.getText().toString();
                break;
            case Transition:
                text = latitudeFromEditUni.getText().toString();
                break;
        }

        return ConversionUtils.toDouble(text);
    }

    void setLatitudeEditboxValue(double value) {
        String text = String.valueOf(value);
        latitudeEditBi.setText(text);
        latitudeFromEditUni.setText(text);
    }

    double getLongitudeEditboxValue() {
        TriggerType type = getSelectedTriggerType();

        String text;
        switch(type) {
            case EnterArea:
            case ExitArea:
            default:
                text = longitudeEditBi.getText().toString();
                break;
            case Transition:
                text = longitudeFromEditUni.getText().toString();
                break;
        }

        return ConversionUtils.toDouble(text);
    }

    void setLongitudeEditboxValue(double value) {
        String text = String.valueOf(value);
        longitudeEditBi.setText(text);
        longitudeFromEditUni.setText(text);
    }

    double getLatitudeToEditboxValue() {
        String text = latitudeToEditUni.getText().toString();
        return ConversionUtils.toDouble(text);
    }

    void setLatitudeToEditboxValue(double value) {
        latitudeToEditUni.setText(String.valueOf(value));
    }

    double getLongitudeToEditboxValue() {
        String text = longitudeToEditUni.getText().toString();
        return ConversionUtils.toDouble(text);
    }

    void setLongitudeToEditboxValue(double value) {
        longitudeToEditUni.setText(String.valueOf(value));
    }

    double getRadiusEditboxValue() {
        TriggerType type = getSelectedTriggerType();

        String text;
        switch(type) {
            case EnterArea:
            case ExitArea:
            default:
                text = radiusEditBi.getText().toString();
                break;
            case Transition:
                text = radiusEditUni.getText().toString();
                break;
        }

        return ConversionUtils.toDouble(text);
    }

    void setRadiusEditboxValue(double value) {
        String text = String.valueOf(value);
        radiusEditUni.setText(text);
        radiusEditBi.setText(text);
    }


}
