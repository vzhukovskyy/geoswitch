package ua.pp.rudiki.geoswitch;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import ua.pp.rudiki.geoswitch.peripherals.ActionExecutor;

public class ActivityAction extends AppCompatActivity {

    public String TAG = getClass().getSimpleName();

    CheckBox showNotificationCheckbox, playSoundCheckbox, speakOutCheckbox, sendPostCheckbox, appendSigninCheckbox;
    EditText urlEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);

        showNotificationCheckbox = (CheckBox)findViewById(R.id.showNotificationCheckbox);
        playSoundCheckbox = (CheckBox)findViewById(R.id.playSoundCheckbox);
        speakOutCheckbox = (CheckBox)findViewById(R.id.speakOutCheckbox);
        sendPostCheckbox = (CheckBox)findViewById(R.id.sendPostCheckbox);
        appendSigninCheckbox = (CheckBox)findViewById(R.id.appendSigninCheckbox);
        urlEdit = (EditText)findViewById(R.id.urlEdit);

        loadValuesToUi();
    }

    public void onOkClick(View view) {
        storeValues();

        Intent resultIndent = new Intent();
        setResult(Activity.RESULT_OK, resultIndent);
        finish();
    }

    public void onCancelClick(View view) {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();
    }

    public void onLaunchActionClick(View view) {
        GeoSwitchApp.getLogger().log("User launched action");

        new ActionExecutor(showNotificationCheckbox.isChecked(),
                playSoundCheckbox.isChecked(),
                speakOutCheckbox.isChecked(),
                sendPostCheckbox.isChecked(),
                appendSigninCheckbox.isChecked(),
                urlEdit.getText().toString()
        ).execute();
    }

    // data persistence

    private void storeValues() {
        GeoSwitchApp.getPreferences().storeAction(
            showNotificationCheckbox.isChecked(),
            playSoundCheckbox.isChecked(),
            speakOutCheckbox.isChecked(),
            sendPostCheckbox.isChecked(),
            appendSigninCheckbox.isChecked(),
            urlEdit.getText().toString()
        );
    }

    private void loadValuesToUi() {
        boolean showNotification = GeoSwitchApp.getPreferences().getShowNotification();
        boolean playSound = GeoSwitchApp.getPreferences().getPlaySound();
        boolean speakOut = GeoSwitchApp.getPreferences().getSpeakOut();
        boolean sendPost = GeoSwitchApp.getPreferences().getSendPost();
        boolean appendSignin = GeoSwitchApp.getPreferences().getAppendToken();
        String url = GeoSwitchApp.getPreferences().getUrl();

        showNotificationCheckbox.setChecked(showNotification);
        playSoundCheckbox.setChecked(playSound);
        speakOutCheckbox.setChecked(speakOut);
        sendPostCheckbox.setChecked(sendPost);
        appendSigninCheckbox.setChecked(appendSignin);
        urlEdit.setText(url);

        ensureCheckboxesCohere(null);
    }

    public void ensureCheckboxesCohere(View activeView) {
        playSoundCheckbox.setEnabled(showNotificationCheckbox.isChecked());
        appendSigninCheckbox.setEnabled(sendPostCheckbox.isChecked());
        if(playSoundCheckbox.isChecked() && speakOutCheckbox.isChecked()) {
            if(activeView == playSoundCheckbox)
                speakOutCheckbox.setChecked(false);
            else if(activeView == speakOutCheckbox)
                playSoundCheckbox.setChecked(false);
        }
    }
}
