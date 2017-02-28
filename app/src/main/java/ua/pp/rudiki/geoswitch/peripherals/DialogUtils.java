package ua.pp.rudiki.geoswitch.peripherals;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import ua.pp.rudiki.geoswitch.R;

public class DialogUtils {

    public static void displayErrorMessage(final Activity activity, String message) {
        String title = activity.getString(R.string.dialog_error_title);
        String buttonText = activity.getString(R.string.dialog_dismiss_button);
        displayMessage(activity, title, message, buttonText);
    }

    public static void displayMessage(final Activity activity, String title, String message, String buttonText) {

        ScreenOrientationUtils.lockScreenOrientation(activity);

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        ScreenOrientationUtils.unlockScreenOrientation(activity);
                    }
                })
                .create()
                .show();
    }
}
