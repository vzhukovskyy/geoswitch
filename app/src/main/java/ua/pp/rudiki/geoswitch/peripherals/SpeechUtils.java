package ua.pp.rudiki.geoswitch.peripherals;

import android.os.Build;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

import ua.pp.rudiki.geoswitch.App;

public class SpeechUtils {
    private static final String TAG = SpeechUtils.class.getSimpleName();

    private TextToSpeech tts;
    private boolean ttsReady = false;
    private Locale ttsLocale;

    public SpeechUtils() {
        App.getLogger().debug(TAG, "Initializing");

        tts = new TextToSpeech(App.getAppContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Locale currentLocale = App.getAppContext().getResources().getConfiguration().locale;
                    int result = tts.setLanguage(currentLocale);
                    if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        ttsLocale = currentLocale;
                        ttsReady = true;
                        App.getLogger().info(TAG, "Using TTS for resource locale " + currentLocale);
                        return;
                    }

                    App.getLogger().info(TAG, "TTS for resource locale " + currentLocale + " is not available");

                    result = tts.setLanguage(Locale.US);
                    if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        ttsLocale = Locale.US;
                        ttsReady = true;
                        App.getLogger().info(TAG, "Using TTS for default locale " + Locale.US);
                        return;
                    }

                    App.getLogger().info(TAG, "TTS for default locale "+Locale.US+" is not available. Speech will be disabled.");
                } else {
                    App.getLogger().info(TAG, "TTS initialization failed. Speech will be disabled.");
                }
            }
        });
    }

    public Locale getLocale() {
        return ttsLocale;
    }

    public void speak(String text){
        if(ttsReady) {
            App.getLogger().info(TAG, "Speaking text: "+text);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(text, TextToSpeech.QUEUE_ADD, null, null);
            } else {
                tts.speak(text, TextToSpeech.QUEUE_ADD, null);
            }
        }
        else {
            App.getLogger().error(TAG, "Failed to speak because TTS not ready. Text: "+text);
        }
    }
}
