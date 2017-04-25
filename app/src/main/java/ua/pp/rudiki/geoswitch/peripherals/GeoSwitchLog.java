package ua.pp.rudiki.geoswitch.peripherals;

import android.location.Location;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.telephony.CellLocation;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ua.pp.rudiki.geoswitch.App;
import ua.pp.rudiki.geoswitch.trigger.GeoTrigger;
import ua.pp.rudiki.geoswitch.trigger.TriggerType;

public class GeoSwitchLog {
    private final static String TAG = GeoSwitchLog.class.getSimpleName();

    private final static String LOG_FILENAME = "geoswitch-gps-log.txt";
    private final static String ARCHIVE_FILENAME = "geoswitch-gps-log.01.txt";

    private final static Character LOG_LEVEL_ERROR = 'E';
    private final static Character LOG_LEVEL_INFO = 'I';
    private final static Character LOG_LEVEL_LOCATION = 'L';
    private final static Character LOG_LEVEL_CELL = 'C';
    private final static Character LOG_LEVEL_NETWORK_TYPE = 'N';
    private final static Character LOG_LEVEL_TRIGGER = 'T';
    private final static Character LOG_LEVEL_ACTION = 'A';
    private final static Character LOG_LEVEL_DEBUG = 'D';

    private File fileRoot;
    private File file;
    private FileOutputStream fileStream;
    private OutputStreamWriter fileStreamWriter;

    public GeoSwitchLog() {
        fileRoot = Environment.getExternalStorageDirectory();
        //fileRoot = context.getFilesDir(); - this location is not accessible from another app like
        // text edit and sometimes not visible from computer over USB connection

        openFile();

        Log.i(TAG, "Saving GPS data to file "+file.getAbsolutePath());
    }

    public void logLocation(Location location) {
        String latitude = String.format(Locale.US, "%.8f", location.getLatitude());
        String longitude = String.format(Locale.US, "%.8f", location.getLongitude());
        String accuracy = String.valueOf(Math.round(location.getAccuracy()));

        String message = latitude + " " + longitude + " acc " + accuracy;

        doLog(LOG_LEVEL_LOCATION, "", message);
    }

    public void logCellId(int cellId) {
        String message = "Connected to cell "+cellId;

        doLog(LOG_LEVEL_CELL, "", message);
    }


    public void logNetworkType(int networkType) {
        String message = "Network type "+networkType;

        doLog(LOG_LEVEL_NETWORK_TYPE, "", message);
    }

    public void logTrigger(GeoTrigger trigger) {
        String message = trigger.toString();
        doLog(LOG_LEVEL_TRIGGER, "", message);
    }

    public void logTriggerFired(Location location) {
        String latitude = String.format(Locale.US, "%.8f", location.getLatitude());
        String longitude = String.format(Locale.US, "%.8f", location.getLongitude());
        String message = "Trigger fired at ("+ latitude + "," + longitude + ")";

        doLog(LOG_LEVEL_ACTION, "", message);
    }

    public void error(String tag, String message) {
        doLog(LOG_LEVEL_ERROR, tag, message);
    }

    public void info(String tag, String message) {
        doLog(LOG_LEVEL_INFO, tag, message);
    }

    public void debug(String tag, String message) {
        doLog(LOG_LEVEL_DEBUG, tag, message);
    }

    public void exception(String tag, Throwable exception) {
        String stackTrace = Log.getStackTraceString(exception);
        String text = exception.getMessage() + "\n" + stackTrace;

        doLog(LOG_LEVEL_ERROR, tag, text);
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    public File[] getLogFiles() {
        File[] files = new File[2];
        files[0] = new File(fileRoot, ARCHIVE_FILENAME);
        files[1] = new File(fileRoot, LOG_FILENAME);
        return files;
    }

    // **************************** Private *****************************

    // may be called from different threads at the same time
    private synchronized void doLog(Character logLevel, String tag, String message) {
        rotateFileIfNeeded();

        try {
            Date now = new Date();
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

            String s = new StringBuilder()
                    .append(dt.format(now))
                    .append(" ")
                    .append(logLevel)
                    .append(" ")
                    .append(tag)
                    .append(" ")
                    .append(message)
                    .append("\n")
                    .toString();

            fileStreamWriter.write(s);
            fileStreamWriter.flush();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void openFile() {
        file = new File(fileRoot, LOG_FILENAME);
        try {
            fileStream = new FileOutputStream(file, true);
            fileStreamWriter = new OutputStreamWriter(fileStream, "UTF-8");
        } catch(Exception e) {
            Log.e(TAG, "failed to create log file");
            e.printStackTrace();
        }

        FileUtils.makeVisibleViaUsb(file);
    }


    private void rotateFileIfNeeded() {
        if(file.length() > App.getPreferences().getMaxLogFileSize()) {
            try {
                fileStreamWriter.close();
                fileStream.close();
            }
            catch(IOException e) {
                Log.e(TAG, "exception when closing log file");
            }

            File archiveFile = new File(fileRoot, ARCHIVE_FILENAME);

            boolean success = archiveFile.delete();
            Log.i(TAG, "log file "+ARCHIVE_FILENAME+(success ? " successfully deleted" : " was not deleted"));

            success = file.renameTo(archiveFile);
            if(success) {
                Log.i(TAG, "log file successfully renamed to "+ARCHIVE_FILENAME);
            } else {
                Log.i(TAG, "log file was not renamed");
            }

            FileUtils.makeVisibleViaUsb(file);

            openFile();
        }
    }

}
