package ua.pp.rudiki.geoswitch;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;

import ua.pp.rudiki.geoswitch.peripherals.ActionExecutor;

public class ActivityAction extends AppCompatActivity {

    private static String TAG = ActivityAction.class.getSimpleName();

    CheckBox showNotificationCheckbox, playSoundCheckbox, speakOutCheckbox, sendPostCheckbox, appendSigninCheckbox;
    EditText urlEdit;
    Toast signInToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.getLogger().debug(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        showNotificationCheckbox = (CheckBox)findViewById(R.id.showNotificationCheckbox);
        playSoundCheckbox = (CheckBox)findViewById(R.id.playSoundCheckbox);
        speakOutCheckbox = (CheckBox)findViewById(R.id.speakOutCheckbox);
        sendPostCheckbox = (CheckBox)findViewById(R.id.sendPostCheckbox);
        appendSigninCheckbox = (CheckBox)findViewById(R.id.appendSigninCheckbox);
        urlEdit = (EditText)findViewById(R.id.urlEdit);
        // these two lines replace Enter on multiline editbox keyboard
        urlEdit.setHorizontallyScrolling(false);
        urlEdit.setMaxLines(Integer.MAX_VALUE);

        loadValuesToUi();
    }

    @Override
    public void onStart() {
        super.onStart();
        App.getLogger().debug(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        App.getLogger().debug(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        App.getLogger().debug(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        App.getLogger().debug(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getLogger().debug(TAG, "onDestroy");
    }

    @Override
    public void onBackPressed()
    {
        if(!isFormChanged()) {
            closeActivity(Activity.RESULT_CANCELED);
            return;
        }

        boolean validatedOk = validateForm();

        if(!validatedOk) {
            String message = getString(R.string.activity_action_validation_failed);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return;
        }

        if(signinNeeded()) {
            signIn();
            // don't close activity until signed in; it will be closed in sign-in callback
            return;
        }

        saveForm();

        closeActivity(Activity.RESULT_OK);
    }

    void closeActivity(int resultCode) {
        Intent resultIndent = new Intent();
        setResult(resultCode, resultIndent);
        finish();
    }

    public void onLaunchActionClick(View view) {
        App.getLogger().info(TAG, "User launched action");

        new ActionExecutor(showNotificationCheckbox.isChecked(),
                playSoundCheckbox.isChecked(),
                speakOutCheckbox.isChecked(),
                sendPostCheckbox.isChecked(),
                appendSigninCheckbox.isChecked(),
                urlEdit.getText().toString()
        ).execute();
    }

    private boolean isFormChanged() {
        boolean changed = false;
        changed = changed || showNotificationCheckbox.isChecked() != App.getPreferences().getShowNotification();
        changed = changed || playSoundCheckbox.isChecked() != App.getPreferences().getPlaySound();
        changed = changed || speakOutCheckbox.isChecked() != App.getPreferences().getSpeakOut();
        changed = changed || sendPostCheckbox.isChecked() != App.getPreferences().getSendPost();
        changed = changed || appendSigninCheckbox.isChecked() != App.getPreferences().getAppendToken();
        changed = changed || !urlEdit.getText().toString().equals(App.getPreferences().getUrl());
        return changed;
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
        App.getPreferences().storeAction(
                showNotificationCheckbox.isChecked(),
                playSoundCheckbox.isChecked(),
                speakOutCheckbox.isChecked(),
                sendPostCheckbox.isChecked(),
                appendSigninCheckbox.isChecked(),
                urlEdit.getText().toString()
        );
    }

    private void loadValuesToUi() {
        boolean showNotification = App.getPreferences().getShowNotification();
        boolean playSound = App.getPreferences().getPlaySound();
        boolean speakOut = App.getPreferences().getSpeakOut();
        boolean sendPost = App.getPreferences().getSendPost();
        boolean appendSignin = App.getPreferences().getAppendToken();
        String url = App.getPreferences().getUrl();

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
        String message = getString(R.string.activity_action_verifying_signin);
        signInToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        signInToast.show();

        App.getGoogleApiClient().startSigninForResult(this, RequestCode.ACTIVITY_SIGN_IN);
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

                signInToast.cancel();

                saveForm();
                closeActivity(Activity.RESULT_OK);
            } else {
                String message = getString(R.string.activity_action_signin_failed);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }

            App.getGoogleApiClient().detachGoogleApiClientFromActivity(this);
        }
    }

}
