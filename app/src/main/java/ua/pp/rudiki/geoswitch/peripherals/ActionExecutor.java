package ua.pp.rudiki.geoswitch.peripherals;

import android.content.Context;

import java.util.Formatter;

import ua.pp.rudiki.geoswitch.GeoSwitchApp;
import ua.pp.rudiki.geoswitch.R;

public class ActionExecutor {

    private Context context;
    private boolean showNotification;
    private boolean playSound;
    private boolean speakOut;
    private boolean sendPost;
    private boolean appendToken;
    private String url;

    public ActionExecutor() {
        this.context = GeoSwitchApp.getAppContext();

        showNotification = GeoSwitchApp.getPreferences().getShowNotification();
        playSound = GeoSwitchApp.getPreferences().getPlaySound();
        speakOut = GeoSwitchApp.getPreferences().getSpeakOut();
        sendPost = GeoSwitchApp.getPreferences().getSendPost();
        appendToken = GeoSwitchApp.getPreferences().getAppendToken();
        url = GeoSwitchApp.getPreferences().getUrl();
    }

    public ActionExecutor(boolean showNotification, boolean playSound,
                          boolean speakOut, boolean sendPost, boolean appendToken, String url)
    {
        this.context = GeoSwitchApp.getAppContext();

        this.showNotification = showNotification;
        this.playSound = playSound;
        this.speakOut = speakOut;
        this.sendPost = sendPost;
        this.appendToken = appendToken;
        this.url = url;
    }

    public void execute() {
        GeoSwitchApp.getLogger().log("Executing action "+toString());

        String notificationMessage = context.getString(R.string.service_trigger_fired);
        String speechMessage = getStringInSpeechLocale(R.string.service_trigger_fired);
        if(sendPost) {
            notificationMessage += context.getString(R.string.service_action_started);
            speechMessage += getStringInSpeechLocale(R.string.service_action_started);
        }
        displayNotification(notificationMessage);
        speakOut(speechMessage);

        if(sendPost) {
            executePost();
        }
    }

    private void executePost() {
        GeoSwitchApp.getGoogleApiClient().refreshToken(new AsyncResultCallback<Boolean>() {
            @Override
            public void onResult(Boolean success) {
                if(success) {
                    String token = null;
                    if(appendToken) {
                        token = GeoSwitchApp.getGoogleApiClient().getToken();
                    }
                    GeoSwitchApp.getHttpUtils().sendPostAsync(url, token, new AsyncResultCallback<HttpUtils.PostResult>() {
                        @Override
                        public void onResult(HttpUtils.PostResult result) {
                            actionFinished(result);
                        }
                    });
                }
                else {
                    refreshTokenFailed();
                }
            }
        });

    }

    private void refreshTokenFailed() {
        String notificationMessage, speechMessage;
        if(NetworkUtils.isConnectedToInternet()) {
            notificationMessage = context.getString(R.string.service_action_failed_refresh_token);
            speechMessage = getStringInSpeechLocale(R.string.service_action_failed_refresh_token);
        }
        else {
            notificationMessage = context.getString(R.string.service_action_failed_no_internet);
            speechMessage = getStringInSpeechLocale(R.string.service_action_failed_no_internet);
        }

        displayNotification(notificationMessage);
        speakOut(speechMessage);
    }

    private void actionFinished(HttpUtils.PostResult result) {
        GeoSwitchApp.getLogger().log("Action finished with http code "+result.responseCode+". Response body: "+result.responseBody);

        String notificationMessage, notificationFormat,
                speechMessage, speechFormat;
        if(result.responseCode == 200) {
            if(StringUtils.isNullOrEmpty(result.responseBody)) {
                notificationMessage = context.getString(R.string.service_action_succeeded);
                speechMessage = getStringInSpeechLocale(R.string.service_action_succeeded);
            } else {
                notificationFormat = context.getString(R.string.service_action_succeeded_with_server_response);
                speechFormat = getStringInSpeechLocale(R.string.service_action_succeeded_with_server_response);
                notificationMessage = new Formatter().format(notificationFormat, result.responseBody).toString();
                speechMessage = new Formatter().format(speechFormat, result.responseBody).toString();
            }
        } else {
            notificationFormat = context.getString(R.string.service_action_failed_with_error_code);
            speechFormat = getStringInSpeechLocale(R.string.service_action_failed_with_error_code);
            notificationMessage = new Formatter().format(notificationFormat, result.responseCode).toString();
            speechMessage = new Formatter().format(speechFormat, result.responseBody).toString();
        }

        displayNotification(notificationMessage);
        speakOut(speechMessage);
    }

    private void displayNotification(String message) {
        if(showNotification) {
            GeoSwitchApp.getNotificationUtils().displayNotification(message, playSound);
        }
    }

    private void speakOut(String message) {
        if(speakOut) {
            GeoSwitchApp.getSpeechUtils().speak(message);
        }
    }

    private String getStringInSpeechLocale(int stringId) {
        return GeoSwitchApp.getResourceUtils().getString(stringId, GeoSwitchApp.getSpeechUtils().getLocale());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        if(showNotification)
            sb.append("Show notification");
        if(playSound) {
            if(sb.length() > 0) sb.append(",");
            sb.append("Play sound");
        }
        if(speakOut) {
            if(sb.length() > 0) sb.append(",");
            sb.append("Speak out");
        }
        if(sendPost) {
            if(sb.length() > 0) sb.append(",");
            sb.append("Send POST");
        }
        if(appendToken) {
            if(sb.length() > 0) sb.append(",");
            sb.append("Incude token");
        }
        if(url != null) {
            if(sb.length() > 0) sb.append(",");
            sb.append("url: ");
            sb.append(url);
        }

        if(sb.length() == 0) {
            sb.append("Empty action");
        }

        return sb.toString();
    }
}
