package ua.pp.rudiki.geoswitch.peripherals;

import java.util.Formatter;

import ua.pp.rudiki.geoswitch.GeoSwitchApp;
import ua.pp.rudiki.geoswitch.R;

public class ActionExecutor {
    public static void execute(final String url) {
        GeoSwitchApp.getGeoSwitchGoogleApiClient().refreshToken(new AsyncResultCallback<Boolean>() {
            @Override
            public void onResult(Boolean success) {
                if(success) {
                    GeoSwitchApp.getHttpUtils().sendPostAsync(url, new AsyncResultCallback<HttpUtils.PostResult>() {
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

    private static void refreshTokenFailed() {
        String message;
        if(NetworkUtils.isConnectedToInternet()) {
            message = GeoSwitchApp.getAppContext().getString(R.string.action_failed_refresh_token);
        }
        else {
            message = GeoSwitchApp.getAppContext().getString(R.string.action_failed_no_internet);
        }

        reportActionResult(message);
    }

    private static void actionFinished(HttpUtils.PostResult result) {
        String message;
        if(result.responseCode == 200) {
            message = GeoSwitchApp.getAppContext().getString(R.string.action_succeeded_with_server_response);
            message = new Formatter().format(result.responseBody).toString();
        } else {
            message = GeoSwitchApp.getAppContext().getString(R.string.action_failed_with_error_code);
            message = new Formatter().format(String.valueOf(result.responseCode)).toString();
        }

        reportActionResult(message);
    }

    private static void reportActionResult(String message) {
        GeoSwitchApp.getLogger().log(message);
        GeoSwitchApp.getNotificationUtils().displayNotification(message);
        GeoSwitchApp.getSpeechUtils().speak(message);
    }

}
