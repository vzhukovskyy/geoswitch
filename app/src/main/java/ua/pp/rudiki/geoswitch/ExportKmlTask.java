package ua.pp.rudiki.geoswitch;


import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;

import ua.pp.rudiki.geoswitch.kml.Log2Kml;
import ua.pp.rudiki.geoswitch.peripherals.DialogUtils;

public class ExportKmlTask extends DialogFragment {
    private static final String TAG = ExportKmlTask.class.getSimpleName();
    private static final String FRAGMENT_TAG = "EXPORT_KML";

    private Activity activity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        setRetainInstance(true);

        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.activity_main_generating_kml));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    private boolean isDismissible = false;

    @Override
    public void dismiss() {
        try {
            isDismissible = true;
            super.dismiss();
        }
        catch (IllegalStateException ilse) {
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (isDismissible) {
            super.onDismiss(dialog);
        }
    }

    public void execute(Activity activity) {
        this.activity = activity;

        new WorkerTask().execute();

        show(activity.getFragmentManager(), FRAGMENT_TAG);
    }

    private class WorkerTask extends AsyncTask<Void, Integer, Void> {

        File kmlFile;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... ignore) {
            App.getLogger().info(TAG, "Generating KML file");

            kmlFile = new File(Environment.getExternalStorageDirectory(), "geoswitch.kml");
            final long timePeriod = App.getPreferences().getDefaultTimePeriodForKml();
            Log2Kml.log2kml(timePeriod, kmlFile);

            App.getLogger().info(TAG, "KML file successfully generated");

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
        }

        @Override
        protected void onCancelled() {
        }

        @Override
        protected void onPostExecute(Void ignore) {
            dismiss();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse("file://"+ kmlFile.getAbsolutePath());
            intent.setDataAndType(uri, "application/vnd.google-earth.kml+xml");
            try {
                startActivity(intent);
            } catch(ActivityNotFoundException e) {
                App.getLogger().info(TAG, "Google Earth not installed");

                DialogUtils.displayErrorMessage(activity, getString(R.string.activity_main_googleearth_not_installed));
            }

            ExportKmlTask.this.activity = null;
        }
    }
}