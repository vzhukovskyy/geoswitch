package ua.pp.rudiki.geoswitch.peripherals;

import android.location.Location;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

import ua.pp.rudiki.geoswitch.App;
import ua.pp.rudiki.geoswitch.trigger.GeoTrigger;

public class GeoSwitchLog {
    private final static String TAG = GeoSwitchLog.class.getSimpleName();

    private final static String LOGFILE_BASENAME = "geoswitch-gps-log";
    private final static String LOGFILE_EXTENTION = "txt";
    private final SimpleDateFormat LOGFILE_DATE_FORMAT = new SimpleDateFormat("yy-MM-dd HH-mm");

    private final static Character LOG_LEVEL_ERROR = 'E';
    private final static Character LOG_LEVEL_INFO = 'I';
    private final static Character LOG_LEVEL_LOCATION = 'L';
    private final static Character LOG_LEVEL_CELL = 'C';
    private final static Character LOG_LEVEL_NETWORK_CLASS = 'N';
    private final static Character LOG_LEVEL_WIFI = 'W';
    private final static Character LOG_LEVEL_BLUETOOTH = 'B';
    private final static Character LOG_LEVEL_UI_MODE = 'U';
    private final static Character LOG_LEVEL_POWER = 'P';
    private final static Character LOG_LEVEL_TRIGGER = 'T';
    private final static Character LOG_LEVEL_ACTION = 'A';
    private final static Character LOG_LEVEL_DEBUG = 'D';

    private final SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private File fileRoot;
    private Deque<File> logFiles = new LinkedList<File>();
    private FileOutputStream fileStream;
    private OutputStreamWriter fileStreamWriter;

    public GeoSwitchLog() {
        fileRoot = Environment.getExternalStorageDirectory();
        //fileRoot = context.getFilesDir(); - this location is not accessible from another app like
        // text edit and sometimes not visible from computer over USB connection

        findLogFilesOnFileSystem();

        File file = youngestLogFile();
        if(file == null) {
            file = newLogFile();
        }
        openLogFile(file);

        Log.i(TAG, "Saving GPS data to file " + currentLogFile().getAbsolutePath());
    }

    public void logPower(String tag, String message) {
        doLog(LOG_LEVEL_POWER, "", message);
        Log.e(tag, message);
    }

    public void logWifi(String tag, String message) {
        doLog(LOG_LEVEL_WIFI, "", message);
        Log.e(tag, message);
    }

    public void logBluetooth(String tag, String message) {
        doLog(LOG_LEVEL_BLUETOOTH, "", message);
        Log.e(tag, message);
    }

    public void logUiMode(String tag, String message) {
        doLog(LOG_LEVEL_UI_MODE, "", message);
        Log.e(tag, message);
    }

    public void logLocation(Location location) {
        String latitude = String.format(Locale.US, "%.8f", location.getLatitude());
        String longitude = String.format(Locale.US, "%.8f", location.getLongitude());
        String accuracy = String.valueOf(Math.round(location.getAccuracy()));

        String message = latitude + " " + longitude + " acc " + accuracy;

        doLog(LOG_LEVEL_LOCATION, "", message);
    }

    public void logCellId(String cellId) {
        String message = "Connected to cell " + cellId;

        doLog(LOG_LEVEL_CELL, "", message);
    }

    public void logNetworkClass(String networkClass) {
        String message = "Network class " + networkClass;

        doLog(LOG_LEVEL_NETWORK_CLASS, "", message);
    }

    public void logTrigger(GeoTrigger trigger) {
        String message = trigger.toString();
        doLog(LOG_LEVEL_TRIGGER, "", message);
    }

    public void logTriggerFired(Location location) {
        String latitude = String.format(Locale.US, "%.8f", location.getLatitude());
        String longitude = String.format(Locale.US, "%.8f", location.getLongitude());
        String message = "Trigger fired at (" + latitude + "," + longitude + ")";

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
        Log.e(tag, "", exception);

        String stackTrace = Log.getStackTraceString(exception);
        String text = exception.getMessage() + "\n" + stackTrace;

        doLog(LOG_LEVEL_ERROR, tag, text);
    }

    public String getAbsolutePath() {
        return currentLogFile().getAbsolutePath();
    }

    public File[] getLogFiles() {
        File[] array = new File[logFiles.size()];

        // last-to-first order
        Iterator<File> iter = logFiles.descendingIterator();
        int index = 0;
        while(iter.hasNext()) {
            array[index] = iter.next();
            index++;
        }
        return array;
    }

    // **************************** Private *****************************

    // may be called from different threads at the same time
    private synchronized void doLog(Character logLevel, String tag, String message) {
        rotateFileIfNeeded();

        try {
            Date now = new Date();

            String s = new StringBuilder()
                    .append(logDateFormat.format(now))
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openLogFile(File file) {
        try {
            fileStream = new FileOutputStream(file, true);
            fileStreamWriter = new OutputStreamWriter(fileStream, "UTF-8");
        } catch (Exception e) {
            Log.e(TAG, "failed to create log file");
            e.printStackTrace();
        }

        FileUtils.makeVisibleViaUsb(file);
    }

    private File currentLogFile() {
        return logFiles.size() > 0 ? logFiles.peekFirst() : null;
    }

    private File youngestLogFile() {
        return currentLogFile();
    }

    private void findLogFilesOnFileSystem() {
        File[] files = fileRoot.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(LOGFILE_BASENAME) &&
                        pathname.getName().endsWith(LOGFILE_EXTENTION);
            }
        });

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Long.compare(o2.lastModified(), o1.lastModified());
            }
        });

        for(File f: files)
            logFiles.addLast(f);
    }

    private File newLogFile() {
        String date = LOGFILE_DATE_FORMAT.format(new Date());
        String filename = LOGFILE_BASENAME + "." + date + "." + LOGFILE_EXTENTION;
        File file = new File(fileRoot, filename);

        logFiles.addFirst(file);

        return file;
    }

    private void rotateFileIfNeeded() {
        if (currentLogFile().length() > App.getPreferences().getMaxLogFileSize()) {
            try {
                fileStreamWriter.close();
                fileStream.close();
            } catch (IOException e) {
                Log.e(TAG, "exception when closing log file");
            }

            FileUtils.makeVisibleViaUsb(currentLogFile());

            File file = newLogFile();
            openLogFile(file);

            deleteOldestLogFilesIfNeeded();
        }
    }

    private void deleteOldestLogFilesIfNeeded() {
        int maxLogFiles = App.getPreferences().getMaxLogFiles();
        if(maxLogFiles < 0)
            return; // not limited

        int filesToDelete = logFiles.size() - maxLogFiles;
        while (filesToDelete > 0) {
            File fileToDelete = logFiles.removeLast();
            boolean success = fileToDelete.delete();
            filesToDelete--;
            Log.i(TAG, "log file " + fileToDelete.getName() + (success ? " successfully deleted" : " was not deleted"));
        }
    }
}