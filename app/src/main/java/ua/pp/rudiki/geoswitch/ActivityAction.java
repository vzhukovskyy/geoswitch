package ua.pp.rudiki.geoswitch;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class ActivityAction extends AppCompatActivity {

    public String TAG = getClass().getSimpleName();

    CheckBox actionEnabledCheckbox, appendSigninCheckbox;
    EditText urlEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);

        actionEnabledCheckbox = (CheckBox)findViewById(R.id.enableActionCheckbox);
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
        GeoSwitchApp.getHttpUtils().sendPostAsync(urlEdit.getText().toString());
    }

    // data persistence

    private void storeValues() {
        GeoSwitchApp.getPreferences().storeAction(
            actionEnabledCheckbox.isChecked(),
            appendSigninCheckbox.isChecked(),
            urlEdit.getText().toString()
        );
    }

    private void loadValuesToUi() {
        boolean actionEnabled = GeoSwitchApp.getPreferences().getActionEnabled();
        boolean appendSignin = GeoSwitchApp.getPreferences().getAppendSignin();
        String url = GeoSwitchApp.getPreferences().getUrl();

        actionEnabledCheckbox.setChecked(actionEnabled);
        appendSigninCheckbox.setChecked(appendSignin);
        urlEdit.setText(url);
    }
}
