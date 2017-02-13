package ua.pp.rudiki.geoswitch.peripherals;

import android.content.Context;

import java.util.Formatter;

import ua.pp.rudiki.geoswitch.GeoSwitchApp;
import ua.pp.rudiki.geoswitch.R;
import ua.pp.rudiki.geoswitch.trigger.TriggerType;

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
        String message = context.getString(R.string.trigger_fired_message);
        if(sendPost) {
            message += context.getString(R.string.action_started);
        }

        textOut(message);

        if(sendPost) {
            executePost();
        }
    }

    private void executePost() {
        GeoSwitchApp.getGeoSwitchGoogleApiClient().refreshToken(new AsyncResultCallback<Boolean>() {
            @Override
            public void onResult(Boolean success) {
                if(success) {
                    String token = null;
                    if(appendToken) {
                        token = GeoSwitchApp.getGeoSwitchGoogleApiClient().getToken();
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
        String message;
        if(NetworkUtils.isConnectedToInternet()) {
            message = GeoSwitchApp.getAppContext().getString(R.string.action_failed_refresh_token);
        }
        else {
            message = GeoSwitchApp.getAppContext().getString(R.string.action_failed_no_internet);
        }

        textOut(message);
    }

    private void actionFinished(HttpUtils.PostResult result) {
        String message, format;
        if(result.responseCode == 200) {
            format = context.getString(R.string.action_succeeded_with_server_response);
            message = new Formatter().format(format, result.responseBody).toString();
        } else {
            format = context.getString(R.string.action_failed_with_error_code);
            message = new Formatter().format(format, result.responseCode).toString();
        }

        textOut(message);
    }

    private void textOut(String message) {
        GeoSwitchApp.getLogger().log(message);
        if(showNotification) {
            GeoSwitchApp.getNotificationUtils().displayNotification(message, playSound);
        }
        if(speakOut) {
            GeoSwitchApp.getSpeechUtils().speak(message);
        }
    }

}
