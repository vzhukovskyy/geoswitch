package ua.pp.rudiki.geoswitch;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import ua.pp.rudiki.geoswitch.trigger.GeoArea;

public class ConfigActivity extends AppCompatActivity {

    final String TAG = getClass().getSimpleName();
    final int COORDINATES_REQUEST_ID = 9001;

    EditText latitudeEdit, longitudeEdit, radiusEdit;
    TextView draftAlertLabel;
    TextWatcher textWatcher;

    boolean coordinatesChanged, radiusChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "onCreate");

        setContentView(R.layout.activity_config);

        registerTextWatcher();

        latitudeEdit = (EditText) findViewById(R.id.latitudeEdit);
        latitudeEdit.setKeyListener(null);
        longitudeEdit = (EditText) findViewById(R.id.longitudeEdit);
        longitudeEdit.setKeyListener(null);
        radiusEdit = (EditText) findViewById(R.id.radiusEdit);
        radiusEdit.addTextChangedListener(textWatcher);
        draftAlertLabel = (TextView) findViewById(R.id.draftAlertLabel);
        draftAlertLabel.setVisibility(View.GONE);

        loadValuesToUi();
        restartService();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e(TAG, "onResume");
        //loadValuesToUi();
    }

    private void registerTextWatcher() {
        textWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                GeoArea area = GeoSwitchApp.getPreferences().loadArea();
                double storedRadius = (area != null) ? area.getRadius() : GeoSwitchApp.getPreferences().getDefaultRadius();
                String currentText = radiusEdit.getText().toString();
                radiusChanged = !String.valueOf(storedRadius).equals(currentText);
                setDraftState(coordinatesChanged || radiusChanged);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
    }

    private void restartService() {
        Intent intent = new Intent(this, GeoSwitchGpsService.class);
        GeoArea area = GeoSwitchApp.getPreferences().loadArea();
        if(area != null) {
            intent.putExtra(Preferences.latitudeKey, area.getLatitude());
            intent.putExtra(Preferences.longitudeKey, area.getLongitude());
            intent.putExtra(Preferences.radiusKey, area.getRadius());
        }
        startService(intent);
    }

    private void passValuesService() {
        restartService(); // not really restart, it just passes parameters if service already started
    }

    private void storeValues() {
        GeoSwitchApp.getPreferences().storeArea(
            latitudeEdit.getText().toString(),
            longitudeEdit.getText().toString(),
            radiusEdit.getText().toString()
        );
    }

    private void loadValuesToUi() {
        GeoArea area = GeoSwitchApp.getPreferences().loadArea();
        latitudeEdit.setText(area != null ? String.valueOf(area.getLatitude()) : "");
        longitudeEdit.setText(area != null ? String.valueOf(area.getLongitude()) : "");
        radiusEdit.setText(area != null ? String.valueOf(area.getRadius()) : "");

        setDraftState(false);
    }

    private void setDraftState(boolean isDraft) {
        draftAlertLabel.setVisibility(isDraft ? View.VISIBLE : View.GONE);
    }

    private boolean getDraftState() {
        return (draftAlertLabel.getVisibility() == View.VISIBLE);
    }

    // UI handlers

    public void onApplyClick(View view) {
        storeValues();
        loadValuesToUi();
        passValuesService();
    }

    public void onRevertClick(View view) {
        loadValuesToUi();
    }

    public void onMapClick(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        GeoArea area = GeoSwitchApp.getPreferences().loadArea();
        if(area != null) {
            intent.putExtra(Preferences.latitudeKey, area.getLatitude());
            intent.putExtra(Preferences.longitudeKey, area.getLongitude());
            intent.putExtra(Preferences.radiusKey, area.getRadius());
        }
        startActivityForResult(intent, COORDINATES_REQUEST_ID);
    }

    // return value from the map activity

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult");
        if (requestCode == COORDINATES_REQUEST_ID) {
            if (resultCode == RESULT_OK) {
                final double latitude = data.getDoubleExtra(Preferences.latitudeKey, Double.NaN);
                final double longitude = data.getDoubleExtra(Preferences.longitudeKey, Double.NaN);
                Log.e(TAG, "Received from map ("+latitude+","+longitude+")");

                final String latitudeString = String.valueOf(latitude);
                final String longitudeString = String.valueOf(longitude);
                coordinatesChanged = !latitudeEdit.getText().toString().equals(latitudeString) ||
                                     !longitudeEdit.getText().toString().equals(longitudeString);
                setDraftState(coordinatesChanged || radiusChanged);

//                if(latitude != Double.NaN && longitude != Double.NaN) {
                    latitudeEdit.setText(String.valueOf(latitude));
                    longitudeEdit.setText(String.valueOf(longitude));
//                }
            }
        }
    }
}
