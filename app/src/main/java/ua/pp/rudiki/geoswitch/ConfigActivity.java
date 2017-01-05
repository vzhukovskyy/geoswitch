package ua.pp.rudiki.geoswitch;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import ua.pp.rudiki.geoswitch.trigger.GeoArea;

public class ConfigActivity extends AppCompatActivity {

    final String TAG = getClass().getSimpleName();
    final int SELECT_COORDINATES_REQUEST_ID = 9001;

    EditText latitudeEdit, longitudeEdit, radiusEdit;
    TextView draftAlertLabel;
    TextWatcher textWatcher;

    boolean coordinatesChanged, radiusChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "onCreate");

        setContentView(R.layout.activity_config);

        latitudeEdit = (EditText) findViewById(R.id.latitudeEdit);
        latitudeEdit.setKeyListener(null); // read-only
        longitudeEdit = (EditText) findViewById(R.id.longitudeEdit);
        longitudeEdit.setKeyListener(null); // read-only
        radiusEdit = (EditText) findViewById(R.id.radiusEdit);
        registerRadiusEditWatcher();
        radiusEdit.addTextChangedListener(textWatcher);
        draftAlertLabel = (TextView) findViewById(R.id.draftAlertLabel);

        loadValuesToUi();
        restartService();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e(TAG, "onResume");
    }

    private void registerRadiusEditWatcher() {
        textWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                boolean changed = !getCurrentRadius().equals(getStoredRadius());
                setRadiusChanged(changed);
                updateUiDraftState();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
    }

    private void restartService() {
        Log.e(TAG, "starting service");
        Intent intent = new Intent(this, GeoSwitchGpsService.class);
        startService(intent);
    }

    private void passValuesToService() {
        restartService(); // not really restart, it just passes parameters if service already started
    }

    // UI handlers

    public void onApplyClick(View view) {
        storeValues();
        loadValuesToUi();
        passValuesToService();
    }

    public void onRevertClick(View view) {
        loadValuesToUi();
    }

    public void onMapClick(View view) {
        Intent intent = new Intent(this, MapsActivity.class);

        if(!getCurrentLatitude().isEmpty() && !getCurrentLongitude().isEmpty() && !getCurrentRadius().isEmpty()) {
            intent.putExtra(Preferences.latitudeKey, getCurrentLatitude());
            intent.putExtra(Preferences.longitudeKey, getCurrentLongitude());
            intent.putExtra(Preferences.radiusKey, getCurrentRadius());
        }

        startActivityForResult(intent, SELECT_COORDINATES_REQUEST_ID);
    }

    // return value from the map activity

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult");
        if (requestCode == SELECT_COORDINATES_REQUEST_ID) {
            if (resultCode == RESULT_OK) {
                final double latitude = data.getDoubleExtra(Preferences.latitudeKey, Double.NaN);
                final double longitude = data.getDoubleExtra(Preferences.longitudeKey, Double.NaN);
                Log.e(TAG, "Received from map ("+latitude+","+longitude+")");

                if(latitude != Double.NaN && longitude != Double.NaN) {
                    setCurrentLatitude(latitude);
                    setCurrentLongitude(longitude);

                    boolean coordinatesChanged = !getCurrentLatitude().equals(getStoredLatitude()) ||
                                                 !getCurrentLongitude().equals(getStoredLongitude());
                    setCoordinatesChanged(coordinatesChanged);
                    updateUiDraftState();
                }
            }
        }
    }

    // data accessors

    private String getStoredLatitude() {
        Double latitude = GeoSwitchApp.getPreferences().getLatitude();
        return (latitude != null) ? latitude.toString() : "";
    }

    private String getStoredLongitude() {
        Double longitude = GeoSwitchApp.getPreferences().getLongitude();
        return (longitude != null) ? longitude.toString() : "";
    }

    private String getStoredRadius() {
        Double radius = GeoSwitchApp.getPreferences().getRadius();
        return (radius != null) ? radius.toString() : "";
    }

    private String getCurrentLatitude() {
        return latitudeEdit.getText().toString();
    }

    private void setCurrentLatitude(double latitude) {
        latitudeEdit.setText(String.valueOf(latitude));
    }

    private String getCurrentLongitude() {
        return longitudeEdit.getText().toString();
    }

    private void setCurrentLongitude(double longitude) {
        longitudeEdit.setText(String.valueOf(longitude));
    }

    private String getCurrentRadius() {
        return radiusEdit.getText().toString();
    }

    // data persistence

    private void storeValues() {
        GeoSwitchApp.getPreferences().storeArea(
            getCurrentLatitude(),
            getCurrentLongitude(),
            getCurrentRadius()
        );
    }

    private void loadValuesToUi() {
        latitudeEdit.setText(getStoredLatitude());
        longitudeEdit.setText(getStoredLongitude());
        radiusEdit.setText(getStoredRadius());

        setCoordinatesChanged(false);
        setRadiusChanged(false);
        updateUiDraftState();
    }

    // draft state

    private void setCoordinatesChanged(boolean changed) {
        coordinatesChanged = changed;
    }

    private boolean isCoordinatesChanged() {
        return coordinatesChanged;
    }

    private void setRadiusChanged(boolean changed) {
        radiusChanged = changed;
    }

    private boolean isRadiusChanged() {
        return radiusChanged;
    }

    private void updateUiDraftState() {
        boolean draft = isCoordinatesChanged() || isRadiusChanged();
        draftAlertLabel.setVisibility(draft ? View.VISIBLE : View.GONE);
    }

}
