package ua.pp.rudiki.geoswitch.peripherals;

import java.util.Formatter;

import ua.pp.rudiki.geoswitch.App;
import ua.pp.rudiki.geoswitch.R;
import ua.pp.rudiki.geoswitch.peripherals.http.HttpUtils;
import ua.pp.rudiki.geoswitch.peripherals.http.PostResult;

public class ActionExecutor {
    private final static String TAG = ActionExecutor.class.getSimpleName();

    private boolean showNotification;
    private boolean playSound;
    private boolean speakOut;
    private boolean sendPost;
    private boolean appendToken;
    private String url;

    public ActionExecutor() {
        showNotification = App.getPreferences().getShowNotification();
        playSound = App.getPreferences().getPlaySound();
        speakOut = App.getPreferences().getSpeakOut();
        sendPost = App.getPreferences().getSendPost();
        appendToken = App.getPreferences().getAppendToken();
        url = App.getPreferences().getUrl();
    }

    public ActionExecutor(boolean showNotification, boolean playSound,
                          boolean speakOut, boolean sendPost, boolean appendToken, String url)
    {
        this.showNotification = showNotification;
        this.playSound = playSound;
        this.speakOut = speakOut;
        this.sendPost = sendPost;
        this.appendToken = appendToken;
        this.url = url;
    }

    public void execute() {
        App.getLogger().info(TAG, "Executing action "+toString());

        String notificationMessage = App.getAppContext().getString(R.string.service_trigger_fired);
        String speechMessage = getStringInSpeechLocale(R.string.service_trigger_fired);
        if(sendPost) {
            notificationMessage += App.getAppContext().getString(R.string.service_action_started);
            speechMessage += getStringInSpeechLocale(R.string.service_action_started);
        }
        displayNotification(notificationMessage);
        speakOut(speechMessage);

        if(sendPost) {
            executePost();
        }
    }

    private void executePost() {
        if(appendToken) {
            App.getGoogleApiClient().refreshToken(new AsyncResultCallback<Boolean>() {
                @Override
                public void onResult(Boolean success) {
                    if (success) {
                        String token = App.getGoogleApiClient().getToken();

                        HttpUtils.sendPostAsync(url, token, new AsyncResultCallback<PostResult>() {
                            @Override
                            public void onResult(PostResult result) {
                                actionFinished(result);
                            }
                        });
                    } else {
                        refreshTokenFailed();
                    }
                }
            });
        }
        else {
            HttpUtils.sendPostAsync(url, null, new AsyncResultCallback<PostResult>() {
                @Override
                public void onResult(PostResult result) {
                    actionFinished(result);
                }
            });
        }

    }

    private void refreshTokenFailed() {
        String notificationMessage, speechMessage;
        if(NetworkUtils.isConnectedToInternet()) {
            notificationMessage = App.getAppContext().getString(R.string.service_action_failed_refresh_token);
            speechMessage = getStringInSpeechLocale(R.string.service_action_failed_refresh_token);
        }
        else {
            notificationMessage = App.getAppContext().getString(R.string.service_action_failed_no_internet);
            speechMessage = getStringInSpeechLocale(R.string.service_action_failed_no_internet);
        }

        displayNotification(notificationMessage);
        speakOut(speechMessage);
    }

    private void actionFinished(PostResult result) {
        App.getLogger().info(TAG, "Action finished with http code "+result.responseCode+". Response body: "+result.responseBody);

        String notificationMessage, notificationFormat,
                speechMessage, speechFormat;
        if(result.responseCode == 200) {
            if(StringUtils.isNullOrEmpty(result.responseBody)) {
                notificationMessage = App.getAppContext().getString(R.string.service_action_succeeded);
                speechMessage = getStringInSpeechLocale(R.string.service_action_succeeded);
            } else {
                notificationFormat = App.getAppContext().getString(R.string.service_action_succeeded_with_server_response);
                speechFormat = getStringInSpeechLocale(R.string.service_action_succeeded_with_server_response);
                notificationMessage = new Formatter().format(notificationFormat, result.responseBody).toString();
                speechMessage = new Formatter().format(speechFormat, result.responseBody).toString();
            }
        } else {
            notificationFormat = App.getAppContext().getString(R.string.service_action_failed_with_error_code);
            speechFormat = getStringInSpeechLocale(R.string.service_action_failed_with_error_code);
            notificationMessage = new Formatter().format(notificationFormat, result.responseCode).toString();
            speechMessage = new Formatter().format(speechFormat, result.responseCode).toString();
        }

        displayNotification(notificationMessage);
        speakOut(speechMessage);
    }

    private void displayNotification(String message) {
        if(showNotification) {
            App.getNotificationUtils().displayNotification(message, playSound);
        }
    }

    private void speakOut(String message) {
        if(speakOut) {
            App.getSpeechUtils().speak(message);
        }
    }

    private String getStringInSpeechLocale(int stringId) {
        return App.getResourceUtils().getString(stringId, App.getSpeechUtils().getLocale());
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
