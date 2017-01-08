package ua.pp.rudiki.geoswitch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import ua.pp.rudiki.geoswitch.trigger.GeoArea;
import ua.pp.rudiki.geoswitch.trigger.HttpUtils;

public class ConfigActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    final String TAG = getClass().getSimpleName();
    final int SELECT_COORDINATES_REQUEST_ID = 9001;
    final int RC_SIGN_IN = 9002;

    EditText latitudeEdit, longitudeEdit, radiusEdit, urlEdit;
    TextView draftAlertLabel;

    boolean coordinatesChanged, radiusChanged, urlChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_config);

        latitudeEdit = (EditText) findViewById(R.id.latitudeEdit);
        latitudeEdit.setKeyListener(null); // read-only
        longitudeEdit = (EditText) findViewById(R.id.longitudeEdit);
        longitudeEdit.setKeyListener(null); // read-only
        radiusEdit = (EditText) findViewById(R.id.radiusEdit);
        radiusEdit.addTextChangedListener(new RadiusEditWatcher());
        urlEdit = (EditText) findViewById(R.id.urlEdit);
        urlEdit.addTextChangedListener(new UrlEditWatcher());
        draftAlertLabel = (TextView) findViewById(R.id.draftAlertLabel);

        loadValuesToUi();
        restartService();

        signIn();
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            Log.i(TAG, "Signed in for foreground activity as "+account.getEmail());
        } else {
            Log.e(TAG, "Sign-in failed");
        }
    }

    void signIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);

        Log.d(TAG, "Sign-in for foreground activity initiated");
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Connection to google sign-in service failed");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");
    }

    private void restartService() {
        Log.d(TAG, "Starting service");
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

    public void onLaunchActionClick(View view) {
        GeoSwitchApp.getHttpUtils().sendPostAsync(getCurrentUrl());
    }

    class RadiusEditWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            boolean changed = !getCurrentRadius().equals(getStoredRadius());
            setRadiusChanged(changed);
            updateUiDraftState();
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    class UrlEditWatcher implements TextWatcher {
        public void afterTextChanged(Editable s) {
            boolean changed = !getCurrentUrl().equals(getStoredUrl());
            setUrlChanged(changed);
            updateUiDraftState();
        }
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
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
                    setCurrentLatitude(latitude);
                    setCurrentLongitude(longitude);

                    boolean coordinatesChanged = !getCurrentLatitude().equals(getStoredLatitude()) ||
                                                 !getCurrentLongitude().equals(getStoredLongitude());
                    setCoordinatesChanged(coordinatesChanged);
                    updateUiDraftState();
                }
            }
        }
        else if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
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

    private String getStoredUrl() {
        return GeoSwitchApp.getPreferences().getUrl();
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

    private String getCurrentUrl() {
        return urlEdit.getText().toString();
    }
    // data persistence

    private void storeValues() {
        GeoSwitchApp.getPreferences().storeValues(
            getCurrentLatitude(),
            getCurrentLongitude(),
            getCurrentRadius(),
            getCurrentUrl()
        );
    }

    private void loadValuesToUi() {
        latitudeEdit.setText(getStoredLatitude());
        longitudeEdit.setText(getStoredLongitude());
        radiusEdit.setText(getStoredRadius());
        urlEdit.setText(getStoredUrl());

        setCoordinatesChanged(false);
        setRadiusChanged(false);
        setUrlChanged(false);
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

    private void setUrlChanged(boolean changed) {
        urlChanged = changed;
    }

    private boolean isUrlChanged() {
        return urlChanged;
    }

    private void updateUiDraftState() {
        boolean draft = isCoordinatesChanged() || isRadiusChanged() || isUrlChanged();
        draftAlertLabel.setVisibility(draft ? View.VISIBLE : View.GONE);
    }

}
