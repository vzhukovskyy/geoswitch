package ua.pp.rudiki.geoswitch.action;

import ua.pp.rudiki.geoswitch.GeoSwitchApp;
import ua.pp.rudiki.geoswitch.peripherals.AsyncResultCallback;
import ua.pp.rudiki.geoswitch.peripherals.HttpUtils;
import ua.pp.rudiki.geoswitch.peripherals.NetworkUtils;

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
            message = "Action failed. Could not connect to google server.";
        }
        else {
            message = "Action failed. No connection to internet";
        }

        reportActionResult(message);
    }

    private static void actionFinished(HttpUtils.PostResult result) {
        String message;
        if(result.responseCode == 200) {
            message = "Action succeeded. Server response: " + result.responseBody;
        } else {
            message = "Action failed. Error code "+result.responseCode;
        }

        reportActionResult(message);
    }

    private static void reportActionResult(String message) {
        GeoSwitchApp.getGpsLog().log(message);
        GeoSwitchApp.getNotificationUtils().displayNotification(message, false);
        GeoSwitchApp.getSpeachUtils().speak(message);
    }

}
