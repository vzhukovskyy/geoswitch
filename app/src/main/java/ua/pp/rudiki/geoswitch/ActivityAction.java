package ua.pp.rudiki.geoswitch;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import java.text.Format;

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
        // these two lines replace Enter on multiline editbox keyboard
        urlEdit.setHorizontallyScrolling(false);
        urlEdit.setMaxLines(Integer.MAX_VALUE);
//        urlEdit.setLines(Integer.MAX_VALUE);

        loadValuesToUi();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void onOkClick(View view) {
        boolean validatedOk = validateForm();

        if(!validatedOk) {
            String message = getString(R.string.activity_action_validation_failed);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return;
        }

        if(signinNeeded()) {
            signIn();
            // don't close activity until signed in
            return;
        }

        saveForm();

        closeActivity(Activity.RESULT_OK);
    }

    public void onCancelClick(View view) {
        closeActivity(Activity.RESULT_CANCELED);
    }

    void closeActivity(int resultCode) {
        Intent resultIndent = new Intent();
        setResult(resultCode, resultIndent);
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

    private boolean validateForm() {
        boolean showNotification = showNotificationCheckbox.isChecked();
        boolean playSound = playSoundCheckbox.isChecked();
        boolean speakOut = speakOutCheckbox.isChecked();
        boolean sendPost = sendPostCheckbox.isChecked();
        boolean appendSignin = appendSigninCheckbox.isChecked();
        String url = urlEdit.getText().toString();

        return !(sendPostCheckbox.isChecked() && urlEdit.getText().toString().isEmpty());
    }

    private boolean signinNeeded() {
        return sendPostCheckbox.isChecked() && appendSigninCheckbox.isChecked();
    }

    private void saveForm() {
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
        urlEdit.setEnabled(sendPostCheckbox.isChecked());
        if(playSoundCheckbox.isChecked() && speakOutCheckbox.isChecked()) {
            if(activeView == playSoundCheckbox)
                speakOutCheckbox.setChecked(false);
            else if(activeView == speakOutCheckbox)
                playSoundCheckbox.setChecked(false);
        }
    }

    // Sign in
    private void signIn() {
        GeoSwitchApp.getGoogleApiClient().startSigninForResult(this, RequestCode.ACTIVITY_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // this thread is the UI thread

        if (requestCode == RequestCode.ACTIVITY_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result != null && result.isSuccess()) {
                // useful for the first time sign-in but annoying for subsequent action edits
//                GoogleSignInAccount account = result.getSignInAccount();
//                String format = getString(R.string.activity_action_greetings);
//                String message = String.format(format, account.getDisplayName());
//                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                saveForm();
                closeActivity(Activity.RESULT_OK);
            } else {
                String message = getString(R.string.activity_action_signin_failed);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }

            GeoSwitchApp.getGoogleApiClient().detachGoogleApiClientFromActivity(this);
        }
    }

}
