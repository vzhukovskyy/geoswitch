package ua.pp.rudiki.geoswitch.kml;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.pp.rudiki.geoswitch.App;
import ua.pp.rudiki.geoswitch.peripherals.FileUtils;

public class Log2Kml {
    private static final String TAG = Log2Kml.class.getSimpleName();

    private final static int BUFFER_SIZE = 4096;

    public static void log2kml(File kmlFile) {
        File tempFile = createTempFile();
        concatLogFiles(tempFile);

        generateKml(tempFile, kmlFile);

        tempFile.delete();

        FileUtils.makeVisibleViaUsb(kmlFile);
    }

    private static void generateKml(File logFile, File kmlFile) {
        try (
                FileReader fileReader = new FileReader(logFile);
                BufferedReader br = new BufferedReader(fileReader);
                FileWriter fileWriter = new FileWriter(kmlFile, false);
                Kml kml = new Kml(kmlFile, "GeoSwitch");
        ){
            List<LatLng> coordinates = new ArrayList<>();
            Date startDate = null, prevDate = null;

            String logLine;
            while((logLine = br.readLine()) != null) {
                if(logLine.contains(" L ")) {
                    String[] parts = logLine.split("[ ]+");

                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    Date date = df.parse(parts[0] + " " + parts[1]);
                    String tag = parts[2];
                    double latitude = 0, longitude = 0;
                    try {
                        latitude = Double.parseDouble(parts[3]);
                        longitude = Double.parseDouble(parts[4]);
                    } catch(NumberFormatException e) {
                        App.getLogger().exception(TAG, e);
                    }

                    if(prevDate == null) {
                        startDate = date;
                        prevDate = date;
                    }
                    else {
                        long delta = date.getTime() - prevDate.getTime();
                        if(delta < 3000) {
                            coordinates.add(new LatLng(latitude, longitude));
                        }
                        else {
                            kml.addLineString(coordinates, startDate, prevDate);
                            startDate = date;
                            coordinates.clear();
                            coordinates.add(new LatLng(latitude, longitude));
                        }
                        prevDate = date;
                    }
                }
                else if(logLine.contains(" T ")) {
                    String[] parts = logLine.split("[ ]+");

                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    Date date = df.parse(parts[0] + " " + parts[1]);
                    String tag = parts[2];
                    String triggerType = parts[3];

                    if(triggerType.equals("Exit") || triggerType.equals("Enter")) {
                        LatLng ll = parseLatitudeLongitudeTuple(parts[4]);
                        double radius = parseRadius(parts[5]);

                        kml.addCircle(ll, radius, 0x6400ffff);
                        kml.addPoint(ll, triggerType);
                    }
                    else if(triggerType.equals("Transition")) {
                        LatLng centerFrom = parseLatitudeLongitudeTuple(parts[5]);
                        LatLng centerTo = parseLatitudeLongitudeTuple(parts[8]);
                        double radius = parseRadius(parts[6]);

                        kml.addCircle(centerFrom, radius, 0x6400ffff);
                        kml.addPoint(centerFrom, "From");
                        kml.addCircle(centerTo, radius, 0x64ff00ff);
                        kml.addPoint(centerTo, "To");
                    }
                }
            }

            if(coordinates.size() > 0) {
                kml.addLineString(coordinates, startDate, prevDate);
            }
        }
        catch(Exception e) {
            App.getLogger().exception(TAG, e);
        }
    }

    private static LatLng parseLatitudeLongitudeTuple(String s) {
        Pattern pattern = Pattern.compile("\\((\\d+\\.\\d+),(\\d+\\.\\d+)\\)");
        Matcher matcher = pattern.matcher(s);
        boolean found = matcher.find();
        if(found) {
            double latitude = 0, longitude = 0;
            try {
                latitude = Double.parseDouble(matcher.group(1));
                longitude = Double.parseDouble(matcher.group(2));
                return new LatLng(latitude, longitude);
            }
            catch (NumberFormatException e) {
                App.getLogger().exception(TAG, e);
            }
        }

        return null;
    }

    private static double parseRadius(String s) {
        Pattern pattern = Pattern.compile("R=(\\d+\\.\\d+)");
        Matcher matcher = pattern.matcher(s);
        boolean found = matcher.find();
        if(found) {
            double radius = 0;
            try {
                radius = Double.parseDouble(matcher.group(1));
                return radius;
            }
            catch (NumberFormatException e) {
                App.getLogger().exception(TAG, e);
            }
        }

        return 0;
    }

    private static void generateSampleKml(File f) {
        final Kml kml = new Kml(f, "GeoSwitch");

        Date beginTime = null, endTime = null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        try {
            beginTime = df.parse("2017-02-21 10:56:10.369");
            endTime = df.parse("2017-02-21 10:57:10.370");
        } catch (ParseException e) {
        }

        List<LatLng> coordinates = new ArrayList<>();
        coordinates.add(new LatLng(30.63652993,50.23638183));
        coordinates.add(new LatLng(30.637,50.23638183));
        coordinates.add(new LatLng(30.638,50.23638183));

        kml.addLineString(coordinates, beginTime, endTime);
        kml.addPoint(new LatLng(50.19542742565555, 30.666), "Test");
        kml.addCircle(new LatLng(50.19542742565555, 30.666), 100, 0x6400ffff);
        kml.addCircle(new LatLng(50.19542742565555, 30.667), 100, 0x64ff00ff);
        kml.finish();
    }

    private static void concatLogFiles(File tempFile) {
        File[] logFiles = App.getLogger().getLogFiles();
        for(File f: logFiles) {
            try (
                    FileInputStream in = new FileInputStream(f);
                    FileOutputStream out = new FileOutputStream(tempFile, true);
            ) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                do {
                    bytesRead = in.read(buffer);
                    out.write(buffer, 0, bytesRead);
                }
                while(bytesRead == BUFFER_SIZE);
            }
            catch (FileNotFoundException e) {
                // it's OK, proceed to the next file
                continue;
            }
            catch (IOException e) {
                App.getLogger().exception(TAG, e);
                return;
            }
        }
    }

    private static File createTempFile() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("geoswitch", null);
        }
        catch(IOException e) {
            App.getLogger().exception(TAG, e);
        }

        return tempFile;
    }

}
