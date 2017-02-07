package ua.pp.rudiki.geoswitch.peripherals;

import android.content.Context;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import ua.pp.rudiki.geoswitch.GeoSwitchApp;

public class GeoSwitchLog {
    final String TAG = getClass().getSimpleName();

    private final static String LOG_FILENAME = "geoswitch-gps-log.txt";
    private final static String ARCHIVE_FILENAME = "geoswitch-gps-log.01.txt";

    private Context context;

    private File fileRoot;
    private File file;
    private FileOutputStream fileStream;
    private OutputStreamWriter fileStreamWriter;

    public GeoSwitchLog(Context context) {
        this.context = context;
        fileRoot = Environment.getExternalStorageDirectory();
        //fileRoot = context.getFilesDir(); - this location is not accessible from another app like
        // text edit and sometimes not visible from computer over USB connection

        openFile();

        Log.i(TAG, "Saving GPS data to file "+file.getAbsolutePath());
    }

    public void log(Location location) {
        String latitude = String.format("%.8f", location.getLatitude());
        String longitude = String.format("%.8f", location.getLongitude());
        String accuracy = String.valueOf(Math.round(location.getAccuracy()));

        String message = latitude + " " + longitude + " acc " + accuracy;

        doLog(message);
    }

    public void log(String message) {
        doLog(message);
    }

    public void log(Throwable exception) {
        String stackTrace = Log.getStackTraceString(exception);
        String text = exception.getMessage() + "\n" + stackTrace;

        doLog(text);
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    // may be called from different threads simultaneously
    private synchronized void doLog(String message) {
        rotateFileIfNeeded();

        try {
            Date now = new Date();
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String s = dt.format(now) + " " + message + "\n";

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
            fileStreamWriter = new OutputStreamWriter(fileStream, "ISO-8859-1");
        } catch(Exception e) {
            Log.e(TAG, "failed to create log file");
            e.printStackTrace();
        }
    }


    private void rotateFileIfNeeded() {
        if(file.length() > GeoSwitchApp.getPreferences().getMaxLogFileSize()) {
            try {
                fileStreamWriter.close();
                fileStream.close();
            }
            catch(IOException e) {
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

            openFile();
        }
    }
}
