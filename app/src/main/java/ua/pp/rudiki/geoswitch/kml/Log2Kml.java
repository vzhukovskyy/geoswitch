package ua.pp.rudiki.geoswitch.kml;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.pp.rudiki.geoswitch.App;
import ua.pp.rudiki.geoswitch.peripherals.FileUtils;

public class Log2Kml {
    private static final String TAG = Log2Kml.class.getSimpleName();

    private final static int COPY_BUFFER_SIZE = 4096;

    public static void log2kml(int timePeriodMillis, File kmlFile) {
        File tempLogFile = createTempFile();
        concatLogFiles(tempLogFile);

        LogParserResult parserResult = extractGeoDataFromLog(tempLogFile, timePeriodMillis);
        removeDuplicateTriggers(parserResult);
        generateKml(parserResult, kmlFile);

        tempLogFile.delete();

        FileUtils.makeVisibleViaUsb(kmlFile);
    }

    private static class LogParserResult {
        List<PointData> pointData = new ArrayList<>();
        List<AreaTriggerData> areaTriggerData = new ArrayList<>();
        List<TransitionTriggerData> transitionTriggerData = new ArrayList<>();
    }

    private static class PointData {
        Date date;
        LatLng position;
    }

    private static class AreaTriggerData {
        Date date;
        LatLng center;
        double radius;
    }

    private static class TransitionTriggerData {
        Date date;
        LatLng from;
        LatLng to;
        double radius;
    }

    private static LogParserResult extractGeoDataFromLog(File logFile, int timePeriodMillis) {
        LogParserResult result = new LogParserResult();

        Date startDate = null;
        if(timePeriodMillis > 0){
            startDate = new Date(new Date().getTime()-timePeriodMillis);
        }

        try (
                FileReader fileReader = new FileReader(logFile);
                BufferedReader br = new BufferedReader(fileReader);
        ){
            String logLine;
            while((logLine = br.readLine()) != null) {

                String[] parts = logLine.split("[ ]+");

                Date date;
                try {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    if(parts.length < 2)
                        continue;

                    date = df.parse(parts[0] + " " + parts[1]);
                } catch (ParseException e) {
                    // it's OK, exception stack lines do not have date
                    continue;
                }
                String tag = parts[2];

                if(tag.equals("L")) {
                    if(startDate != null && date.before(startDate))
                        continue;

                    double latitude = 0, longitude = 0;
                    try {
                        latitude = Double.parseDouble(parts[3]);
                        longitude = Double.parseDouble(parts[4]);
                    } catch(NumberFormatException e) {
                        App.getLogger().exception(TAG, e);
                    }

                    PointData pointData = new PointData();
                    pointData.date = date;
                    pointData.position = new LatLng(latitude, longitude);

                    result.pointData.add(pointData);
                }
                else if(tag.equals("T")) {
                    String triggerType = parts[3];

                    if(triggerType.equals("Exit") || triggerType.equals("Enter")) {
                        LatLng ll = parseLatitudeLongitudeTuple(parts[4]);
                        double radius = parseRadius(parts[5]);

                        AreaTriggerData areaTriggerData = new AreaTriggerData();
                        areaTriggerData.date = date;
                        areaTriggerData.center = ll;
                        areaTriggerData.radius = radius;

                        result.areaTriggerData.add(areaTriggerData);
                    }
                    else if(triggerType.equals("Transition")) {
                        LatLng centerFrom = parseLatitudeLongitudeTuple(parts[5]);
                        LatLng centerTo = parseLatitudeLongitudeTuple(parts[8]);
                        double radius = parseRadius(parts[6]);

                        TransitionTriggerData transitionTriggerData = new TransitionTriggerData();
                        transitionTriggerData.date = date;
                        transitionTriggerData.from = centerFrom;
                        transitionTriggerData.to = centerTo;
                        transitionTriggerData.radius = radius;

                        result.transitionTriggerData.add(transitionTriggerData);
                    }
                }
            }
        }
        catch(Exception e) {
            App.getLogger().exception(TAG, e);
            Log.e(TAG, e.getMessage(), e);
        }

        return result;
    }

    private static void removeDuplicateTriggers(LogParserResult logParserResult) {

        // final ordering does not matter

        Set<AreaTriggerData> areaTriggerSet = new HashSet<>(logParserResult.areaTriggerData);
        logParserResult.areaTriggerData = new ArrayList<>(areaTriggerSet);

        Set<TransitionTriggerData> transitionTriggerSet = new HashSet<>(logParserResult.transitionTriggerData);
        logParserResult.transitionTriggerData = new ArrayList<>(transitionTriggerSet);
    }

    private static void generateKml(LogParserResult logParserResult, File kmlFile) {
        try (
                FileWriter fileWriter = new FileWriter(kmlFile, false);
                Kml kml = new Kml(kmlFile, "GeoSwitch");
        ){
            for(AreaTriggerData areaTriggerData: logParserResult.areaTriggerData) {
                kml.addCircle(areaTriggerData.center, areaTriggerData.radius, 0x6400ffff);
                kml.addPoint(areaTriggerData.center, "");
            }

            for(TransitionTriggerData transitionTriggerData: logParserResult.transitionTriggerData) {
                kml.addCircle(transitionTriggerData.from, transitionTriggerData.radius, 0x6400ffff);
                kml.addPoint(transitionTriggerData.from, "From");
                kml.addCircle(transitionTriggerData.to, transitionTriggerData.radius, 0x64ff00ff);
                kml.addPoint(transitionTriggerData.to, "To");
            }

            List<LatLng> coordinates = new ArrayList<>();
            Date startDate = null, prevDate = null;
            for(PointData pointData: logParserResult.pointData) {
                if(prevDate == null) {
                    startDate = pointData.date;
                    prevDate = pointData.date;
                }
                else {
                    long delta = pointData.date.getTime() - prevDate.getTime();
                    if(delta < 3000) {
                        coordinates.add(pointData.position);
                    }
                    else {
                        kml.addLineString(coordinates, startDate, prevDate);
                        startDate = pointData.date;
                        coordinates.clear();
                        coordinates.add(pointData.position);
                    }
                    prevDate = pointData.date;
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
                byte[] buffer = new byte[COPY_BUFFER_SIZE];
                int bytesRead;
                do {
                    bytesRead = in.read(buffer);
                    out.write(buffer, 0, bytesRead);
                }
                while(bytesRead == COPY_BUFFER_SIZE);
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
