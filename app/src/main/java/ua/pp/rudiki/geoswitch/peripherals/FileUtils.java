package ua.pp.rudiki.geoswitch.peripherals;


import android.media.MediaScannerConnection;

import java.io.File;

import ua.pp.rudiki.geoswitch.App;

public class FileUtils {

    public static void makeVisibleViaUsb(File file) {
        MediaScannerConnection.scanFile(App.getAppContext(), new String[] {file.getAbsolutePath()}, null, null);
    }
}
