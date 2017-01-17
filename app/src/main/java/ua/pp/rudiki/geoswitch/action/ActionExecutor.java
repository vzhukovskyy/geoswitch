package ua.pp.rudiki.geoswitch.action;

import ua.pp.rudiki.geoswitch.GeoSwitchApp;
import ua.pp.rudiki.geoswitch.peripherals.AsyncResultCallback;

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
                            String message = success ? "Action succeeded" : "Action failed";
                            GeoSwitchApp.getGpsLog().log(message);
                            GeoSwitchApp.getNotificationUtils().displayNotification(message, false);
                            GeoSwitchApp.getSpeachUtils().speak(message);
                        }
                    });
                }
            }
        });
    }
}
