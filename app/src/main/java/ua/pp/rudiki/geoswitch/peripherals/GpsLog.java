package ua.pp.rudiki.geoswitch.peripherals;

import android.content.Context;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import ua.pp.rudiki.geoswitch.GeoSwitchApp;

public class GpsLog {
    final String TAG = getClass().getSimpleName();

    private final static String LOG_FILENAME = "geoswitch-gps-log.txt";
    private final static String ARCHIVE_FILENAME = "geoswitch-gps-log.01.txt";

    private File file;
    private GpsLogListener listener;

    private String shortAppLog, shortGpsLog;

    public GpsLog(Context context) {
        //String root = Environment.getExternalStorageDirectory().toString();
        //file = new File(root, LOG_FILENAME);

        File dir = context.getExternalFilesDir(null);
        file = new File(dir, LOG_FILENAME);

        Log.i(TAG, "Saving GPS data to file "+file.getAbsolutePath());

        shortAppLog = GeoSwitchApp.getPreferences().getShortAppLog();
        shortGpsLog = GeoSwitchApp.getPreferences().getShortGpsLog();
    }

    public void log(Location location) {
        String message = location.getLatitude() + " " + location.getLongitude();
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

    private void doLog(String message) {
        rotateFileIfNeeded();

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file, true);

            Date now = new Date();
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String s = dt.format(now) + " " + message + "\n";

            stream.write(s.getBytes());
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
        finally {
            try {
                stream.close();
            }
            catch(Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public void addListener(GpsLogListener listener) {
        this.listener = listener;
    }

    private void rotateFileIfNeeded() {
        if(file.length() > GeoSwitchApp.getPreferences().getMaxLogFileSize()) {
            String root = Environment.getExternalStorageDirectory().toString();

            File archiveFile = new File(root, ARCHIVE_FILENAME);

            boolean success = archiveFile.delete();
            Log.i(TAG, "log file "+ARCHIVE_FILENAME+(success ? " successfully deleted" : " was not deleted"));

            success = file.renameTo(archiveFile);
            Log.i(TAG, "log file "+(success ? " successfully renamed to"+ARCHIVE_FILENAME : " was not renamed"));

            file = new File(root, LOG_FILENAME);
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
