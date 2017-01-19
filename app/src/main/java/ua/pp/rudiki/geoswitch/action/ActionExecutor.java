package ua.pp.rudiki.geoswitch.action;

import ua.pp.rudiki.geoswitch.GeoSwitchApp;
import ua.pp.rudiki.geoswitch.peripherals.AsyncResultCallback;
import ua.pp.rudiki.geoswitch.peripherals.NetworkUtils;

public class ActionExecutor {
    public static void execute(String url) {
        GeoSwitchApp.getGoogleSignIn().refreshToken(new AsyncResultCallback() {
            @Override
            public void onResult(boolean success) {
                if(success) {
                    String url = GeoSwitchApp.getPreferences().getUrl();
                    GeoSwitchApp.getHttpUtils().sendPostAsync(url, new AsyncResultCallback() {
                        @Override
                        public void onResult(boolean success) {
                            actionFinished(success);
                        }
                    });
                }
                else {
                    actionFinished(false);
                }
            }
        });
    }

    private static void actionFinished(boolean success) {
        String message;
        if(success) {
            message = "Action succeeded";
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
