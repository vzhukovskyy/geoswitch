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
    private File file;

    public GpsLog(Context context) {
        String root = Environment.getExternalStorageDirectory().toString();
        file = new File(root, "geoswitch-gps-log.txt");

        Log.i(TAG, "Saving GPS data to file "+file.getAbsolutePath());
        log("Gps log started");
    }

    public void log(Location location) {
        String message = location.getLatitude() + " " + location.getLongitude();
        log(message);
    }

    public void log(String message) {
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
}
