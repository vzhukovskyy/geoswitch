package ua.pp.rudiki.geoswitch.action;

import ua.pp.rudiki.geoswitch.GeoSwitchApp;
import ua.pp.rudiki.geoswitch.peripherals.AsyncResultCallback;
import ua.pp.rudiki.geoswitch.peripherals.HttpUtils;
import ua.pp.rudiki.geoswitch.peripherals.NetworkUtils;

public class ActionExecutor {
    public static void execute(String url) {
        GeoSwitchApp.getGoogleSignIn().refreshToken(new AsyncResultCallback<Boolean>() {
            @Override
            public void onResult(Boolean success) {
                if(success) {
                    String url = GeoSwitchApp.getPreferences().getUrl();
                    GeoSwitchApp.getHttpUtils().sendPostAsync(url, new AsyncResultCallback<HttpUtils.PostResult>() {
                        @Override
                        public void onResult(HttpUtils.PostResult result) {
                            actionFinished(result);
                        }
                    });
                }
                else {
                    actionFinished(null);
                }
            }
        });
    }

    private static void actionFinished(HttpUtils.PostResult result) {
        String message;
        if(result != null && result.responseCode == 200) {
            message = "Action succeeded. Server response: "+result.responseBody;
        }
        else {
            if(NetworkUtils.isConnectedToInternet()) {
                message = "Action failed. Server not responding.";
            }
            else {
                message = "Action failed. No connection to internet";
            }
        }

        GeoSwitchApp.getGpsLog().log(message);
        GeoSwitchApp.getNotificationUtils().displayNotification(message, false);
        GeoSwitchApp.getSpeachUtils().speak(message);
    }
}
