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

public class GpsLog {
    final String TAG = getClass().getSimpleName();

    private final static String LOG_FILENAME = "geoswitch-gps-log.txt";
    private final static String ARCHIVE_FILENAME = "geoswitch-gps-log.01.txt";

    private Context context;

    private File fileRoot;
    private File file;
    private FileOutputStream fileStream;
    private OutputStreamWriter fileStreamWriter;

    private GpsLogListener listener;

    private String shortAppLog, shortGpsLog;

    public GpsLog(Context context) {
        this.context = context;
        fileRoot = Environment.getExternalStorageDirectory();
        //fileRoot = context.getFilesDir(); - this location is not accessible from another app like
        // text edit and sometimes not visible from computer over USB connection

        openFile();

        Log.i(TAG, "Saving GPS data to file "+file.getAbsolutePath());

        shortAppLog = GeoSwitchApp.getPreferences().getShortAppLog();
        shortGpsLog = GeoSwitchApp.getPreferences().getShortGpsLog();
    }

    public void log(Location location) {
        String latitude = String.format("%.8f", location.getLatitude());
        String longitude = String.format("%.8f", location.getLongitude());
        String accuracy = String.valueOf(Math.round(location.getAccuracy()));

        String message = latitude + " " + longitude + " "+(char)0xB1 + accuracy;

        doLog(message);
        appendToShortGpsLog(message);

        if(listener != null)
            listener.onGpsCoordinatesLog(location.getLatitude(), location.getLongitude());
    }

    public void log(String message) {
        doLog(message);
        appendToShortAppLog(message);

        if(listener != null)
            listener.onLog(message);
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

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

    public void addListener(GpsLogListener listener) {
        this.listener = listener;
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

    //
    // Short log
    //

    public String getShortAppLog() {
        return shortAppLog;
    }

    public String getShortGpsLog() {
        return shortGpsLog;
    }

    private void appendToShortGpsLog(String message) {
        shortGpsLog += "\n" + now() + " " + message;
        shortGpsLog = truncateLog(shortGpsLog, 6);

        GeoSwitchApp.getPreferences().storeShortGpsLog(shortGpsLog);
    }

    private void appendToShortAppLog(String message) {
        shortAppLog += "\n" + now() + " " + message;
        shortAppLog = truncateLog(shortAppLog, 6);

        GeoSwitchApp.getPreferences().storeShortAppLog(shortAppLog);
    }

    private String truncateLog(String text, int maxLines) {
        int lines = countLines(text);
        for(int i=0; i<lines-maxLines; i++) {
            int lineEnd = text.indexOf('\n');
            if(lineEnd > 0) {
                text = text.substring(lineEnd+1);
            }
        }
        return text;
    }

    private int countLines(String text) {
        int lines = 0;

        int pos;
        while((pos = text.indexOf('\n')) > 0) {
            text = text.substring(pos+1);
            lines++;
        }
        lines++;

        return lines;
    }

    private String now() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        return dateFormat.format(date);
    }


}
