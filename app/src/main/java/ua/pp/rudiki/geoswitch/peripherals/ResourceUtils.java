package ua.pp.rudiki.geoswitch.peripherals;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class ResourceUtils {

    Context context;

    public ResourceUtils(Context context) {
        this.context = context;
    }

    public String getString(int stringId, Locale locale){
        Resources ttsLocaleResources = getLocalizedResources(context, locale);
        String s = ttsLocaleResources.getString(stringId);
        return s;
    }

    private Resources getLocalizedResources(Context context, Locale desiredLocale) {
        Configuration conf = context.getResources().getConfiguration();
        conf = new Configuration(conf);
        conf.setLocale(desiredLocale);
        Context localizedContext = context.createConfigurationContext(conf);
        return localizedContext.getResources();
    }
}
