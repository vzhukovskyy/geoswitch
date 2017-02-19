package ua.pp.rudiki.geoswitch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

import ua.pp.rudiki.geoswitch.service.GpsServiceActivationListener;
import ua.pp.rudiki.geoswitch.service.GeoSwitchGpsService;
import ua.pp.rudiki.geoswitch.trigger.EnterAreaTrigger;
import ua.pp.rudiki.geoswitch.trigger.ExitAreaTrigger;
import ua.pp.rudiki.geoswitch.trigger.GeoArea;
import ua.pp.rudiki.geoswitch.trigger.GeoPoint;
import ua.pp.rudiki.geoswitch.trigger.GeoTrigger;
import ua.pp.rudiki.geoswitch.trigger.TransitionTrigger;
import ua.pp.rudiki.geoswitch.trigger.TriggerType;


public class ActivityMain extends AppCompatActivity implements GpsServiceActivationListener
{
    private final static String TAG = ActivityMain.class.getSimpleName();

    EditText gpsActivationEdit, triggerEdit, actionEdit;
    TextView statusLabel, substatusLabel;
    Switch gpsActivationSwitch;

    private DateFormat dateFormat = SimpleDateFormat.getTimeInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.getLogger().debug(TAG, "onCreate");
        App.getLogger().info(TAG, "Locale " + Locale.getDefault());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpsActivationEdit = (EditText)findViewById(R.id.gpsActivationDescriptionEdit);
        gpsActivationEdit.setKeyListener(null);
        gpsActivationSwitch = (Switch)findViewById(R.id.gpsActivationSwitch);
        triggerEdit = (EditText)findViewById(R.id.triggerDescriptionEdit);
        triggerEdit.setKeyListener(null);
        actionEdit = (EditText)findViewById(R.id.actionDescriptionEdit);
        actionEdit.setKeyListener(null);
        statusLabel = (TextView)findViewById(R.id.statusLabel);
        substatusLabel = (TextView)findViewById(R.id.substatusLabel);

        loadTriggerToUi();
        loadActionToUi();
        loadGpsActivationToUi();

        updateActivationModeUi();
        updateStatusUi(null);

        App.getGpsServiceActivator().registerListener(this);
        registerServiceMessageReceiver();

        startService(GeoSwitchGpsService.START_REASON_MAIN_ACTIVITY_CREATED);
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
        App.getLogger().debug(TAG, "------------------------------------------------");
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

    public void onConfigureTriggerClick(View view) {
        Intent intent = new Intent(this, ActivityTrigger.class);
        startActivityForResult(intent, RequestCode.MAIN_TRIGGER_ID);
    }

    public void onConfigureActionClick(View view) {
        Intent intent = new Intent(this, ActivityAction.class);
        startActivityForResult(intent, RequestCode.MAIN_ACTION_ID);
    }

    public void onOpenLogButtonClick(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("file://"+ App.getLogger().getAbsolutePath());
        intent.setDataAndType(uri, "text/plain");
        startActivity(intent);
    }

    public void onGpsOptionsClick(View view) {
        Intent intent = new Intent(this, ActivityGpsOptions.class);
        startActivityForResult(intent, RequestCode.MAIN_GPSACTIVATION_ID);
    }

    public void onGpsActivateButtonClick(View view) {
        boolean checked = gpsActivationSwitch.isChecked();
        App.getLogger().debug(TAG, "onGpsActivateButtonClick checked="+checked);

        App.getPreferences().storeGpsManuallyActivated(checked);
        updateStatusUi(null);

        if(checked)
            App.getGpsServiceActivator().switchedOnManually();
        else
            App.getGpsServiceActivator().switchedOffManually();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.MAIN_TRIGGER_ID) {
            if (resultCode == RESULT_OK) {
                loadTriggerToUi();
                startService(GeoSwitchGpsService.START_REASON_USER_CHANGED_TRIGGER);
            }
        }
        else if (requestCode == RequestCode.MAIN_ACTION_ID) {
            if (resultCode == RESULT_OK) {
                loadActionToUi();
                startService(GeoSwitchGpsService.START_REASON_USER_CHANGED_ACTION);
            }
        }
        else if (requestCode == RequestCode.MAIN_GPSACTIVATION_ID) {
            if (resultCode == RESULT_OK) {
                loadGpsActivationToUi();
                updateActivationModeUi();
                updateStatusUi(null);

                App.getGpsServiceActivator().activationModeChanged(); // starts service
            }
        }
    }

    private void updateStatusUi(Date gpsFixTime) {
        boolean active = App.getGpsServiceActivator().isOn();
        boolean activateOnCharger = App.getPreferences().getActivateOnCharger();

        String status, substatus;
        if(active) {
            status = getString(R.string.activity_main_status_active);
            if(gpsFixTime != null) {
                substatus = getString(R.string.activity_main_substatus_gps_time) + dateFormat.format(gpsFixTime);
            } else {
                substatus = getString(R.string.activity_main_substatus_waiting_gps);
            }
        } else {
            status = getString(R.string.activity_main_status_inactive);
            if(activateOnCharger) {
                substatus = getString(R.string.activity_main_substatus_bycharger_inactive);
            } else {
                substatus = getString(R.string.activity_main_substatus_manual_inactive);
            }
        }

        statusLabel.setText(status);
        substatusLabel.setText(substatus);

        gpsActivationSwitch.setVisibility(activateOnCharger ? View.GONE : View.VISIBLE);
        gpsActivationSwitch.setChecked(active);
    }

    private void updateActivationModeUi() {
        boolean activateOnCharger = App.getPreferences().getActivateOnCharger();
        boolean manuallyActivated = App.getPreferences().getGpsManuallyActivated();

        gpsActivationSwitch.setVisibility(activateOnCharger ? View.GONE : View.VISIBLE);
        gpsActivationSwitch.setChecked(manuallyActivated);
    }

    // data persistence

    private void loadTriggerToUi() {
        GeoTrigger trigger = App.getPreferences().loadTrigger();
        TriggerType triggerType = (trigger != null) ? trigger.getType() : TriggerType.Invalid;

        String desc, format;
        switch(triggerType) {
            case EnterArea: {
                GeoArea area = ((EnterAreaTrigger) trigger).getArea();
                String latitude = String.valueOf(area.getLatitude());
                String longitude = String.valueOf(area.getLongitude());
                long roundedRadius = Math.round(area.getRadius());

                format = getString(R.string.activity_main_trigger_desc_enter_area);
                desc = new Formatter().format(format, roundedRadius, latitude, longitude).toString();
            }
            break;
            case ExitArea: {
                GeoArea area = ((ExitAreaTrigger) trigger).getArea();
                String latitude = String.valueOf(area.getLatitude());
                String longitude = String.valueOf(area.getLongitude());
                long roundedRadius = Math.round(area.getRadius());

                format = getString(R.string.activity_main_trigger_desc_exit_area);
                desc = new Formatter().format(format, roundedRadius, latitude, longitude).toString();
            }
            break;
            case Transition: {
                TransitionTrigger transitionTrigger = (TransitionTrigger) trigger;
                GeoPoint pointA = transitionTrigger.getPointA();
                GeoPoint pointB = transitionTrigger.getPointB();
                String latitude = String.valueOf(pointA.getLatitude());
                String longitude = String.valueOf(pointA.getLongitude());
                String latitudeTo = String.valueOf(pointB.getLatitude());
                String longitudeTo = String.valueOf(pointB.getLongitude());
                long roundedRadius = Math.round(transitionTrigger.getRadius());

                format = getString(R.string.activity_main_trigger_desc_transition);
                desc = new Formatter().format(format, roundedRadius, latitude, longitude,
                        roundedRadius, latitudeTo, longitudeTo).toString();
            }
            break;
            default:
                desc = getString(R.string.activity_main_trigger_invalid);
        }

        triggerEdit.setText(desc);
    }

    private void loadActionToUi() {
        boolean showNotification = App.getPreferences().getShowNotification();
        boolean playSound = App.getPreferences().getPlaySound();
        boolean speakOut = App.getPreferences().getSpeakOut();
        boolean sendPost = App.getPreferences().getSendPost();
        boolean appendSignin = App.getPreferences().getAppendToken();
        String url = App.getPreferences().getUrl();

        String desc = "";
        if (showNotification) {
            desc = appendActionDescription(desc, getString(R.string.activity_main_action_display_notification));
            if(playSound)
                desc = appendActionDescription(desc, getString(R.string.activity_main_action_play_sound));
        }
        if (speakOut) {
            desc = appendActionDescription(desc, getString(R.string.activity_main_action_speak_out));
        }

        if (sendPost) {
            String format;
            if (appendSignin) {
                format = getString(R.string.activity_main_action_post_with_token);
            } else {
                format = getString(R.string.activity_main_action_post);
            }
            String postActionDesc = new Formatter().format(format, url).toString();
            desc = appendActionDescription(desc, postActionDesc);
        }

        actionEdit.setText(desc);
    }

    private void loadGpsActivationToUi() {
        boolean activateOnCharger = App.getPreferences().getActivateOnCharger();

        String desc = "";
        if (activateOnCharger) {
            desc = getString(R.string.activity_main_gps_charging);
        } else {
            desc = getString(R.string.activity_main_gps_manual);
        }

        gpsActivationEdit.setText(desc);
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

    // service

    private void startService(String reason) {
        App.getLogger().info(TAG, "Starting service");

        Intent intent = new Intent(this, GeoSwitchGpsService.class);
        intent.putExtra(GeoSwitchGpsService.START_REASON_KEY, reason);
        startService(intent);
    }

    private void registerServiceMessageReceiver() {
        IntentFilter filter = new IntentFilter(GeoSwitchGpsService.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(GeoSwitchGpsService.BROADCAST_ACTION)) {
                Date date = null;
                long timestamp = intent.getLongExtra(GeoSwitchGpsService.BROADCAST_GPSFIXTIMESTAMP_KEY, 0);
                if(timestamp != 0) {
                    date = new Date(timestamp);
                }
                double latitude = intent.getDoubleExtra(GeoSwitchGpsService.BROADCAST_LATITUDE_KEY, Double.NaN);
                double longitude = intent.getDoubleExtra(GeoSwitchGpsService.BROADCAST_LONGITUDE_KEY, Double.NaN);

                onServiceUpdateReceived(latitude, longitude, date);
            }
        }
    };

    private void onServiceUpdateReceived(double latitude, double longitude, Date date)
    {
        //Log.e(TAG, "fake message");

        updateStatusUi(date);

        if(App.getPreferences().getTriggerType() == TriggerType.Invalid) {
            // First run. Callback most probably with last know location from Fused location API.
            // Set up initial trigger
            if(!Double.isNaN(latitude) && !Double.isNaN(longitude)){
                createInitialTrigger(latitude, longitude);
                loadTriggerToUi();
                startService(GeoSwitchGpsService.START_REASON_INITIAL_CONFIGURATION_DONE);
            }
        }
    }

    private void createInitialTrigger(double latitude, double longitude) {
        double radius = App.getPreferences().getDefaultRadius();
        GeoArea area = new GeoArea(latitude, longitude, radius);
        ExitAreaTrigger trigger = new ExitAreaTrigger(area);
        App.getPreferences().storeTrigger(trigger);
    }

    // GpsServiceActivationListener implementation

    @Override
    public void onActivated() {
        updateActivationModeUi();
    }

    @Override
    public void onDeactivated() {
        updateActivationModeUi();
    }
}
