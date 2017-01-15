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

import java.text.SimpleDateFormat;
import java.util.Date;

import ua.pp.rudiki.geoswitch.peripherals.GpsLogListener;


public class ActivityMain extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    final String TAG = getClass().getSimpleName();

    private final int RC_SIGN_IN = 9002;
    private final static int CONFIGURE_TRIGGER_ID = 9011;
    private final static int CONFIGURE_ACTION_ID = 9012;

    EditText triggerEdit, actionEdit, logEdit, gpsLogEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "OnCreate");

        triggerEdit = (EditText)findViewById(R.id.triggerDescriptionEdit);
        triggerEdit.setKeyListener(null);
        actionEdit = (EditText)findViewById(R.id.actionDescriptionEdit);
        actionEdit.setKeyListener(null);
        gpsLogEdit = (EditText)findViewById(R.id.gpsLogEdit);
        gpsLogEdit.setKeyListener(null);
        logEdit = (EditText)findViewById(R.id.logEdit);
        logEdit.setKeyListener(null);

        loadAreaToUi();
        loadActionToUi();
        registerLogListener();

        signIn();
        restartService();
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
        boolean appendSignin = GeoSwitchApp.getPreferences().getAppendSignin();
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

    private void registerLogListener() {
        GeoSwitchApp.getGpsLog().addListener(new GpsLogListener() {
            @Override
            public void onLog(String message) {
                String text = logEdit.getText().toString() + "\n" + now() + " " + message;
                String truncatedText = truncateLog(text, 6);
                logEdit.setText(truncatedText);
            }

            @Override
            public void onGpsCoordinatesLog(double latitude, double longitude) {
                String text = gpsLogEdit.getText().toString() + "\n" + now() + " " + latitude + "," + longitude;
                String truncatedText = truncateLog(text, 6);
                gpsLogEdit.setText(truncatedText);
            }

            private String now() {
                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                return dateFormat.format(date);
            }

            private String truncateLog(String text, int maxLines) {
                int lines = countLines(text);
                for(int i=0; i<lines-maxLines; i++) {
                    int lineEnd = text.indexOf('\n');
                    if(lineEnd > 0) {
                        text = text.substring(lineEnd+1);
                    }
                }
                return text;
            }

            private int countLines(String text) {
                int lines = 0;

                int pos;
                while((pos = text.indexOf('\n')) > 0) {
                    text = text.substring(pos+1);
                    lines++;
                }
                lines++;

                return lines;
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
