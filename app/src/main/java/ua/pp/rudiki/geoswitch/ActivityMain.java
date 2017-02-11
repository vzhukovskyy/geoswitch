package ua.pp.rudiki.geoswitch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

import ua.pp.rudiki.geoswitch.peripherals.ConversionUtils;
import ua.pp.rudiki.geoswitch.trigger.TriggerType;


public class ActivityMain extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    final String TAG = getClass().getSimpleName();

    private final int RC_SIGN_IN = 9002;
    private final static int CONFIGURE_TRIGGER_ID = 9011;
    private final static int CONFIGURE_ACTION_ID = 9012;

    EditText triggerEdit, actionEdit;
    TextView statusLabel, substatusLabel;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate");

        triggerEdit = (EditText)findViewById(R.id.triggerDescriptionEdit);
        triggerEdit.setKeyListener(null);
        actionEdit = (EditText)findViewById(R.id.actionDescriptionEdit);
        actionEdit.setKeyListener(null);
        statusLabel = (TextView)findViewById(R.id.statusLabel);
        substatusLabel = (TextView)findViewById(R.id.substatusLabel);

        signIn();
        registerServiceMessageReceiver();
        restartService();
    }

    @Override
    public void onStart() {
        super.onResume();

        Log.d(TAG, "onStart");

        loadAreaToUi();
        loadActionToUi();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
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
        Uri uri = Uri.parse("file://"+GeoSwitchApp.getLogger().getAbsolutePath());
        intent.setDataAndType(uri, "text/plain");
        startActivity(intent);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode="+requestCode+", resultCode="+resultCode);
        if (requestCode == CONFIGURE_TRIGGER_ID) {
            if (resultCode == RESULT_OK) {
                loadAreaToUi();
                passValuesToService();
            }
        }
        else if (requestCode == CONFIGURE_ACTION_ID) {
            if (resultCode == RESULT_OK) {
                loadActionToUi();
                passValuesToService();
            }
        }
        else if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void updateUiStatus(boolean active, Date gpsFixTime) {
        String status, substatus;
        if(active) {
            status = getString(R.string.status_active);
            if(gpsFixTime != null) {
                substatus = getString(R.string.substatus_gps_time) + dateFormat.format(gpsFixTime);
            } else {
                substatus = getString(R.string.substatus_waiting_gps);
            }
        } else {
            status = getString(R.string.status_inactive);
            substatus = getString(R.string.substatus_inactive);
        }

        statusLabel.setText(status);
        substatusLabel.setText(substatus);
    }

    // data persistence

    private void loadAreaToUi() {
        TriggerType triggerType = GeoSwitchApp.getPreferences().getTriggerType();
        String latitude = GeoSwitchApp.getPreferences().getLatitudeAsString();
        String longitude = GeoSwitchApp.getPreferences().getLongitudeAsString();
        String radius = GeoSwitchApp.getPreferences().getRadiusAsString();
        String latitudeTo = GeoSwitchApp.getPreferences().getLatitudeToAsString();
        String longitudeTo = GeoSwitchApp.getPreferences().getLongitudeToAsString();

        String desc;
        if(triggerType != TriggerType.Invalid) {
            long roundedRadius = Math.round(ConversionUtils.toDouble(radius));

            if (triggerType == TriggerType.EnterArea) {
                String format = getString(R.string.trigger_area);
                desc = new Formatter().format(format, roundedRadius, latitude, longitude).toString();
            } else {
                String format = getString(R.string.trigger_transition);
                desc = new Formatter().format(format, roundedRadius, latitude, longitude,
                        roundedRadius, latitudeTo, longitudeTo).toString();
            }
        }
        else {
            desc = getString(R.string.trigger_invalid);
        }

        triggerEdit.setText(desc);
    }

    private void loadActionToUi() {
        boolean showNotification = GeoSwitchApp.getPreferences().getShowNotification();
        boolean playSound = GeoSwitchApp.getPreferences().getPlaySound();
        boolean speakOut = GeoSwitchApp.getPreferences().getSpeakOut();
        boolean sendPost = GeoSwitchApp.getPreferences().getSendPost();
        boolean appendSignin = GeoSwitchApp.getPreferences().getAppendToken();
        String url = GeoSwitchApp.getPreferences().getUrl();

        String desc = "";
        if (showNotification) {
            desc = appendActionDescription(desc, getString(R.string.action_display_notification));
            if(playSound)
                desc = appendActionDescription(desc, getString(R.string.action_play_sound));
        }
        if (speakOut) {
            desc = appendActionDescription(desc, getString(R.string.action_speak_out));
        }

        if (sendPost) {
            String format;
            if (appendSignin) {
                format = getString(R.string.action_post_with_token);
            } else {
                format = getString(R.string.action_post);
            }
            String postActionDesc = new Formatter().format(format, url).toString();
            desc = appendActionDescription(desc, postActionDesc);
        }

        actionEdit.setText(desc);
    }

    private String appendActionDescription(String wholeDescription, String actionDescription) {
        if(wholeDescription != null && !wholeDescription.isEmpty()) {
            return wholeDescription + ", " + decapitalize(actionDescription);
        }
        else {
            return capitalize(actionDescription);
        }
    }

    private String capitalize(String message) {
        if(message != null && !message.isEmpty()) {
            return Character.toUpperCase(message.charAt(0)) + message.substring(1);
        } else {
            return message;
        }
    }

    private String decapitalize(String message) {
        return Character.toLowerCase(message.charAt(0)) + message.substring(1);
    }

    // Google sign-in
    // This is initial sign-in into application. After successful sign-in what is needed is refreshing token
    // right before making POST request to the server
    // Here I am using separate googleApiClient other than GeoSwitchGoogleApiClient because sign-in includes user operations,
    // Leveraging automanage facility is clearly better choice than handling it by my own

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

        Log.i(TAG, "Sign-in for foreground activity initiated");
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.i(TAG, "handleSignInResult:" + result.isSuccess());
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
        Log.i(TAG, "Starting service");
        GeoSwitchApp.getLogger().log("Starting service");

        Intent intent = new Intent(this, GeoSwitchGpsService.class);
        startService(intent);
    }

    private void passValuesToService() {
        restartService(); // not really restart, it just passes parameters if service already started
    }

    private void registerServiceMessageReceiver() {
        IntentFilter filter = new IntentFilter(GeoSwitchGpsService.SERVICE_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(GeoSwitchGpsService.SERVICE_BROADCAST_ACTION)) {
                boolean activeMode = intent.getBooleanExtra(GeoSwitchGpsService.SERVICE_BROADCAST_ISACTIVEMODE_KEY, false);
                Date date = null;
                if (activeMode) {
                    long timestamp = intent.getLongExtra(GeoSwitchGpsService.SERVICE_BROADCAST_GPSFIXTIMESTAMP_KEY, 0);
                    if(timestamp != 0) {
                        date = new Date(timestamp);
                    }
                }
                updateUiStatus(activeMode, date);
            }
        }
    };
}
