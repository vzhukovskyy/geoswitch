package ua.pp.rudiki.geoswitch.peripherals;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

import ua.pp.rudiki.geoswitch.GeoSwitchApp;

public class SpeechUtils {
    private final String TAG = getClass().getSimpleName();

    private Context context;
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private Locale ttsLocale;

    public SpeechUtils(final Context context) {
        this.context = context;

        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Locale currentLocale = context.getResources().getConfiguration().locale;
                    int result = tts.setLanguage(currentLocale);
                    if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        ttsLocale = currentLocale;
                        ttsReady = true;
                        GeoSwitchApp.getLogger().log("Using TTS for resource locale " + currentLocale);
                        return;
                    }

                    GeoSwitchApp.getLogger().log("TTS for resource locale " + currentLocale + " is not available");

                    result = tts.setLanguage(Locale.US);
                    if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        ttsLocale = Locale.US;
                        ttsReady = true;
                        GeoSwitchApp.getLogger().log("Using TTS for default locale " + Locale.US);
                        return;
                    }

                    GeoSwitchApp.getLogger().log("TTS for default locale "+Locale.US+" is not available. Speech will be disabled.");
                } else {
                    GeoSwitchApp.getLogger().log("TTS initialization failed. Speech will be disabled.");
                }
            }
        });
    }

    public Locale getLocale() {
        return ttsLocale;
    }

    public void speak(String text){
        if(ttsReady) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(text, TextToSpeech.QUEUE_ADD, null, null);
            } else {
                tts.speak(text, TextToSpeech.QUEUE_ADD, null);
            }
        }
    }
}
