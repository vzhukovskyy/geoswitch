package ua.pp.rudiki.geoswitch.peripherals;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

import ua.pp.rudiki.geoswitch.App;

public class ResourceUtils {

    public ResourceUtils() {
    }

    public String getString(int stringId) {
        return App.getAppContext().getString(stringId);
    }

    public String getString(int stringId, Locale locale){
        Resources ttsLocaleResources = getLocalizedResources(locale);
        String s = ttsLocaleResources.getString(stringId);
        return s;
    }

    private Resources getLocalizedResources(Locale desiredLocale) {
        Configuration conf = App.getAppContext().getResources().getConfiguration();
        conf = new Configuration(conf);
        conf.setLocale(desiredLocale);
        Context localizedContext = App.getAppContext().createConfigurationContext(conf);
        return localizedContext.getResources();
    }
}
