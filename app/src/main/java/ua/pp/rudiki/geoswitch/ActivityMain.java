package ua.pp.rudiki.geoswitch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import ua.pp.rudiki.geoswitch.peripherals.GpsLogListener;


public class ActivityMain extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    final String TAG = getClass().getSimpleName();

    private final int RC_SIGN_IN = 9002;
    private final static int CONFIGURE_TRIGGER_ID = 9011;
    private final static int CONFIGURE_ACTION_ID = 9012;

    EditText triggerEdit, actionEdit, appLogEdit, gpsLogEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate");

        triggerEdit = (EditText)findViewById(R.id.triggerDescriptionEdit);
        triggerEdit.setKeyListener(null);
        actionEdit = (EditText)findViewById(R.id.actionDescriptionEdit);
        actionEdit.setKeyListener(null);
        gpsLogEdit = (EditText)findViewById(R.id.gpsLogEdit);
        gpsLogEdit.setKeyListener(null);
        appLogEdit = (EditText)findViewById(R.id.appLogEdit);
        appLogEdit.setKeyListener(null);

        registerLogListener();

        signIn();
        restartService();
    }

    @Override
    public void onStart() {
        super.onResume();

        Log.d(TAG, "onStart");

        loadAreaToUi();
        loadActionToUi();
        loadLogsToUi();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onResume();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    public void onConfigureTriggerClick(View view) {
        Intent intent = new Intent(this, ActivityTrigger.class);
        startActivityForResult(intent, CONFIGURE_TRIGGER_ID);
    }

    public void onConfigureActionClick(View view) {
        Intent intent = new Intent(this, ActivityAction.class);
        startActivityForResult(intent, CONFIGURE_ACTION_ID);
    }

    public void onOpenLogButtonClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("file://"+GeoSwitchApp.getGpsLog().getAbsolutePath());
        intent.setDataAndType(uri, "text/plain");
        startActivity(intent);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode="+requestCode+", resultCode="+resultCode);
        if (requestCode == CONFIGURE_TRIGGER_ID) {
            if (resultCode == RESULT_OK) {
//                String latitude = data.getStringExtra(Preferences.latitudeKey);
//                String longitude = data.getStringExtra(Preferences.longitudeKey);
//                String radius = data.getStringExtra(Preferences.radiusKey);
//                Log.i(TAG, "Configured trigger ("+latitude+","+longitude+") R="+radius);

                loadAreaToUi();
                passValuesToService();
            }
        }
        else if (requestCode == CONFIGURE_ACTION_ID) {
            if (resultCode == RESULT_OK) {
//                Log.i(TAG, "Configured action");
                loadActionToUi();
                passValuesToService();
            }
        }
        else if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    // data persistence

    private void loadAreaToUi() {
        String latitude =  GeoSwitchApp.getPreferences().getLatitudeAsString();
        String longitude = GeoSwitchApp.getPreferences().getLongitudeAsString();
        String radius = GeoSwitchApp.getPreferences().getRadiusAsString();

        String desc;
        if(!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude) && !TextUtils.isEmpty(radius)) {
            desc = "Triggers when entering " + radius + " meters radius area with center in (" + latitude + "," + longitude + ")";
        } else {
            desc = "Trigger not configured";
        }

        triggerEdit.setText(desc);
    }

    private void loadActionToUi() {
        boolean actionEnabled = GeoSwitchApp.getPreferences().getActionEnabled();
        boolean appendSignin = GeoSwitchApp.getPreferences().getAppendToken();
        String url = GeoSwitchApp.getPreferences().getUrl();

        String desc;
        if(!TextUtils.isEmpty(url)) {
            if (!actionEnabled) {
                desc = "Action configured but disabled";
            } else {
//                Uri uri = Uri.parse(url);
//                String server = uri.getAuthority();
                desc = "Send POST request";
                if(appendSignin) {
                    desc += " including Google Sign-In token";
                }
                desc += " to "+url;
            }
        }
        else {
            desc = "Action not configured";
        }

        actionEdit.setText(desc);
    }

    private void loadLogsToUi() {
        gpsLogEdit.setText(GeoSwitchApp.getGpsLog().getShortGpsLog());
        appLogEdit.setText(GeoSwitchApp.getGpsLog().getShortAppLog());
    }

    private void registerLogListener() {
        GeoSwitchApp.getGpsLog().addListener(new GpsLogListener() {
            @Override
            public void onLog(String message) {
                runOnUiThread(new Runnable(){
                    public void run() {
                        appLogEdit.setText(GeoSwitchApp.getGpsLog().getShortAppLog());
                    }
                });
            }

            @Override
            public void onGpsCoordinatesLog(double latitude, double longitude) {
                runOnUiThread(new Runnable(){
                    public void run() {
                        gpsLogEdit.setText(GeoSwitchApp.getGpsLog().getShortGpsLog());
                    }
                });
            }
        });
    }

    // Google sign-in

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

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            Log.i(TAG, "Signed in for foreground activity as "+account.getEmail());
        } else {
            Log.e(TAG, "Sign-in failed");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Connection to google sign-in service failed");
    }

    // service

    private void restartService() {
        Log.d(TAG, "Starting service");
        Intent intent = new Intent(this, GeoSwitchGpsService.class);
        startService(intent);
    }

    private void passValuesToService() {
        restartService(); // not really restart, it just passes parameters if service already started
    }

}
