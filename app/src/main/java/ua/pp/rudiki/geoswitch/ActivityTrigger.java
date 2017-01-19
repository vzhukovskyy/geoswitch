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

import ua.pp.rudiki.geoswitch.peripherals.Preferences;


public class ActivityTrigger extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    final String TAG = getClass().getSimpleName();
    final int SELECT_COORDINATES_REQUEST_ID = 9001;

    EditText latitudeEdit, longitudeEdit, radiusEdit;
    Spinner triggerTypeSpinner;
    RelativeLayout bidirectionalLayout, unidirectionalLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger);

        latitudeEdit = (EditText) findViewById(R.id.latitudeEditBi);
        latitudeEdit.setKeyListener(null); // read-only
        longitudeEdit = (EditText) findViewById(R.id.longitudeEditBi);
        longitudeEdit.setKeyListener(null); // read-only
        radiusEdit = (EditText) findViewById(R.id.radiusEditBi);
        triggerTypeSpinner = (Spinner) findViewById(R.id.triggerTypeSpinner);
        triggerTypeSpinner.setOnItemSelectedListener(this);
        bidirectionalLayout = (RelativeLayout) findViewById(R.id.bidirectionalGroup);
        unidirectionalLayout = (RelativeLayout) findViewById(R.id.unidirectionalGroup);

        loadValuesToUi();
    }

    // UI handlers

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String type = (String)triggerTypeSpinner.getSelectedItem();
        boolean bidirectional = ("Bidirectional".equals(type));
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

        intent.putExtra(Preferences.latitudeKey, latitudeEdit.getText().toString());
        intent.putExtra(Preferences.longitudeKey, longitudeEdit.getText().toString());
        intent.putExtra(Preferences.radiusKey, radiusEdit.getText().toString());

        startActivityForResult(intent, SELECT_COORDINATES_REQUEST_ID);
    }

    // return value from the map activity

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode="+requestCode);
        if (requestCode == SELECT_COORDINATES_REQUEST_ID) {
            if (resultCode == RESULT_OK) {
                final double latitude = data.getDoubleExtra(Preferences.latitudeKey, Double.NaN);
                final double longitude = data.getDoubleExtra(Preferences.longitudeKey, Double.NaN);
                Log.e(TAG, "Received from map ("+latitude+","+longitude+")");

                if(latitude != Double.NaN && longitude != Double.NaN) {
                    latitudeEdit.setText(String.valueOf(latitude));
                    longitudeEdit.setText(String.valueOf(longitude));
                }
            }
        }
    }

    // data persistence

    private void storeValues() {
        GeoSwitchApp.getPreferences().storeArea(
            latitudeEdit.getText().toString(),
            longitudeEdit.getText().toString(),
            radiusEdit.getText().toString()
        );
    }

    private void loadValuesToUi() {
        latitudeEdit.setText(GeoSwitchApp.getPreferences().getLatitudeAsString());
        longitudeEdit.setText(GeoSwitchApp.getPreferences().getLongitudeAsString());
        radiusEdit.setText(GeoSwitchApp.getPreferences().getRadiusAsString());
    }

}
