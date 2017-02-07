package ua.pp.rudiki.geoswitch.peripherals;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class SpeechUtils {
    private final String TAG = getClass().getSimpleName();

    private TextToSpeech tts;
    private boolean ttsReady = false;

    public SpeechUtils(Context context) {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "This language is not supported");
                    }
                    else {
                        ttsReady = true;
                    }

                } else {
                    Log.e(TAG, "TTS initialization failed");
                }
            }
        });
    }

    public void speak(String text){
        if(ttsReady) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }
}
