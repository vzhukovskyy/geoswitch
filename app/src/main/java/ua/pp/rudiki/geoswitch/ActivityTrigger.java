package ua.pp.rudiki.geoswitch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

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

    private final static String TAG = ActivityTrigger.class.getSimpleName();

    RadioGroup triggerTypeRadioGroup;
    TextView triggerTypeDescriptionLabel;
    RelativeLayout bidirectionalLayout, unidirectionalLayout;
    EditText latitudeEditBi, longitudeEditBi, radiusEditBi;
    EditText latitudeFromEditUni, longitudeFromEditUni, latitudeToEditUni, longitudeToEditUni, radiusEditUni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.getLogger().debug(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

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

    // UI handlers

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        TriggerType triggerType = radioIdToTriggerType(checkedId);

        String desc;
        boolean isBidirectionalLayout;
        switch(triggerType) {
            case EnterArea:
                isBidirectionalLayout = true;
                desc = getString(R.string.activity_trigger_enter_desc);
                break;
            case ExitArea:
                isBidirectionalLayout = true;
                desc = getString(R.string.activity_trigger_exit_desc);
                break;
            case Transition:
                isBidirectionalLayout = false;
                desc = getString(R.string.activity_trigger_transition_desc);
                break;
            default:
                isBidirectionalLayout = true;
                desc = "";
        }

        bidirectionalLayout.setVisibility(isBidirectionalLayout ? View.VISIBLE : View.GONE);
        unidirectionalLayout.setVisibility(isBidirectionalLayout ? View.GONE : View.VISIBLE);

        triggerTypeDescriptionLabel.setText(desc);
    }

    public void onBackPressed() {
        if(!isFormChanged()) {
            closeActivity(RESULT_CANCELED);
            return;
        }

        if(!validateForm()) {
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

    public void onMapClick(View view) {
        Intent intent = new Intent(this, ActivityMap.class);

        intent.putExtra(Preferences.triggerTypeKey, getSelectedTriggerType().name());
        intent.putExtra(Preferences.latitudeKey, getLatitudeEditboxValue());
        intent.putExtra(Preferences.longitudeKey, getLongitudeEditboxValue());
        intent.putExtra(Preferences.latitudeToKey, getLatitudeToEditboxValue());
        intent.putExtra(Preferences.longitudeToKey, getLongitudeToEditboxValue());
        intent.putExtra(Preferences.radiusKey, getRadiusEditboxValue());

        startActivityForResult(intent, RequestCode.TRIGGER_COORDINATES_ID);
    }

    // return value from the map activity

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.TRIGGER_COORDINATES_ID && data != null) {
            String latitude = data.getStringExtra(Preferences.latitudeKey);
            String longitude = data.getStringExtra(Preferences.longitudeKey);
            String radius = data.getStringExtra(Preferences.radiusKey);
            String latitudeTo = data.getStringExtra(Preferences.latitudeToKey);
            String longitudeTo = data.getStringExtra(Preferences.longitudeToKey);

            App.getLogger().debug(TAG, "Received from map ("+latitude+","+longitude+"), ("+latitudeTo+","+longitudeTo+"), R="+radius);

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

    private boolean isFormChanged() {
        GeoTrigger currentTrigger = App.getPreferences().loadTrigger();
        GeoTrigger newTrigger = getSelectedTrigger();

        boolean changed = !Objects.equals(currentTrigger, newTrigger);
        return changed;
    }

    private boolean validateForm() {
        TriggerType triggerType = getSelectedTriggerType();

        double latitude = ConversionUtils.toDouble(latitudeEditBi.getText().toString());
        double longitude = ConversionUtils.toDouble(longitudeEditBi.getText().toString());
        double radius = ConversionUtils.toDouble(radiusEditBi.getText().toString());
        double latitudeTo = ConversionUtils.toDouble(latitudeToEditUni.getText().toString());
        double longitudeTo = ConversionUtils.toDouble(longitudeToEditUni.getText().toString());

        boolean areaDefined = !Double.isNaN(latitude) && !Double.isNaN(longitude) && !Double.isNaN(radius);
        boolean areaToDefined = !Double.isNaN(latitudeTo) && !Double.isNaN(longitudeTo);

        String message = null;
        if(triggerType == TriggerType.EnterArea || triggerType == TriggerType.ExitArea) {
            if(!areaDefined) {
                message = getString(R.string.activity_trigger_area_invalid);
            }
        }
        else {
            if(!areaDefined || !areaToDefined) {
                message = getString(R.string.activity_trigger_transition_invalid);
            }
        }

        if(message != null) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void storeValues() {
        GeoTrigger trigger = getSelectedTrigger();
        App.getPreferences().storeTrigger(trigger);
    }

    private void loadValuesToUi() {
        GeoTrigger trigger = App.getPreferences().loadTrigger();
        TriggerType triggerType = (trigger != null) ? trigger.getType() : TriggerType.Invalid;

        int radioId;
        switch(triggerType) {
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
            case ExitArea: {
                radioId = R.id.radioExitArea;

                ExitAreaTrigger exitAreaTrigger = (ExitAreaTrigger)trigger;
                GeoArea area = exitAreaTrigger.getArea();

                setLatitudeEditboxValue(area.getLatitude());
                setLongitudeEditboxValue(area.getLongitude());
                setRadiusEditboxValue(area.getRadius());
            }
            break;
            default: {
                radioId = R.id.radioExitArea;
                setRadiusEditboxValue(App.getPreferences().getDefaultRadius());
            }
            break;
        }

        triggerTypeRadioGroup.check(radioId);
        onCheckedChanged(triggerTypeRadioGroup, radioId);
    }

    // form to persisted entities conversion

    private GeoTrigger getSelectedTrigger() {
        TriggerType triggerType = getSelectedTriggerType();

        double latitude = ConversionUtils.toDouble(latitudeEditBi.getText().toString());
        double longitude = ConversionUtils.toDouble(longitudeEditBi.getText().toString());
        double radius = ConversionUtils.toDouble(radiusEditBi.getText().toString());
        double latitudeTo = ConversionUtils.toDouble(latitudeToEditUni.getText().toString());
        double longitudeTo = ConversionUtils.toDouble(longitudeToEditUni.getText().toString());

        if(Double.isNaN(latitude) || Double.isNaN(longitude) || Double.isNaN(radius)) {
            return null;
        }

        GeoArea area = new GeoArea(latitude, longitude, radius);

        GeoTrigger trigger;
        if(triggerType == TriggerType.EnterArea || triggerType == TriggerType.ExitArea) {
            if(triggerType == TriggerType.EnterArea) {
                trigger = new EnterAreaTrigger(area);
            } else {
                trigger = new ExitAreaTrigger(area);
            }

            return trigger;
        }
        else {
            // triggerType == TriggerType.Transition
            if(Double.isNaN(latitudeTo) || Double.isNaN(longitudeTo)) {
                return null;
            }

            GeoPoint pointFrom = new GeoPoint(latitude, longitude);
            GeoPoint pointTo = new GeoPoint(latitudeTo, longitudeTo);
            return new TransitionTrigger(pointFrom, pointTo);
        }
    }

    private TriggerType getSelectedTriggerType() {
        int radioId = triggerTypeRadioGroup.getCheckedRadioButtonId();
        return radioIdToTriggerType(radioId);
    }

    // form field accessors

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
