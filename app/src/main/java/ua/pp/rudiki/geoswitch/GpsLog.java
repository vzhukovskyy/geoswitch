package ua.pp.rudiki.geoswitch;

import android.content.Context;
import android.location.Location;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GpsLog {
    final String TAG = getClass().getSimpleName();

    private final String LOG_FILENAME = "geoswitch-gps-log.txt";
    private final String ARCHIVE_FILENAME = "geoswitch-gps-log.01.txt";

    private File file;

    public GpsLog(Context context) {
        String root = Environment.getExternalStorageDirectory().toString();
        file = new File(root, LOG_FILENAME);

        Log.i(TAG, "Saving GPS data to file "+file.getAbsolutePath());
    }

    public void log(Location location) {
        String message = location.getLatitude() + " " + location.getLongitude();
        log(message);
    }

    public void log(String message) {
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
}
