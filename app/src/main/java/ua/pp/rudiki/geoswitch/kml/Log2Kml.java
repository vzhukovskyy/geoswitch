package ua.pp.rudiki.geoswitch.kml;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.pp.rudiki.geoswitch.App;
import ua.pp.rudiki.geoswitch.peripherals.FileUtils;
import ua.pp.rudiki.geoswitch.peripherals.HashUtils;

public class Log2Kml {
    private static final String TAG = Log2Kml.class.getSimpleName();

    private final static int COPY_BUFFER_SIZE = 4096;

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public static void log2kml(long timePeriodMillis, File kmlFile) {
        File tempLogFile = createTempFile();
        concatLogFiles(tempLogFile);

        LogParserResult parserResult = extractGeoDataFromLog(tempLogFile);
        removeOutdatedFixes(parserResult, timePeriodMillis);
        removeDuplicateTriggers(parserResult);
        generateKml(parserResult, kmlFile);

        tempLogFile.delete();

        FileUtils.makeVisibleViaUsb(kmlFile);
    }

    private static class LogParserResult {
        List<PointData> pointData = new ArrayList<>();
        List<AreaTriggerData> areaTriggerData = new ArrayList<>();
        List<TransitionTriggerData> transitionTriggerData = new ArrayList<>();
        List<PointData> actionData = new ArrayList<>();
    }

    private static class PointData {
        Date date;
        LatLng position;
    }

    private static class AreaTriggerData {
        Date date;
        LatLng center;
        double radius;

        @Override
        public boolean equals(Object object) {
            if(this == object)
                return true;
            if(object == null)
                return false;
            if(!object.getClass().equals(this.getClass()))
                return false;

            AreaTriggerData areaTriggerData = (AreaTriggerData)object;
            return this.radius == areaTriggerData.radius &&
                   this.center.equals(areaTriggerData.center);
        }

        @Override
        public int hashCode() {
            return HashUtils.combineHashCode(center.hashCode(), radius);
        }
    }

    private static class TransitionTriggerData {
        Date date;
        LatLng from;
        LatLng to;
        double radius;

        @Override
        public boolean equals(Object object) {
            if(this == object)
                return true;

            if(object == null)
                return false;

            if(!object.getClass().equals(this.getClass()))
                return false;

            TransitionTriggerData transitionTriggerData = (TransitionTriggerData)object;
            return this.radius == transitionTriggerData.radius &&
                   this.from.equals(transitionTriggerData.from) &&
                   this.to.equals(transitionTriggerData.to);
        }

        @Override
        public int hashCode() {
            int hash = HashUtils.combineHashCode(1, from.hashCode());
            hash = HashUtils.combineHashCode(hash, to.hashCode());
            hash = HashUtils.combineHashCode(hash, radius);
            return hash;
        }

    }

    private static LogParserResult extractGeoDataFromLog(File logFile) {
        LogParserResult result = new LogParserResult();

        Date startDate = null;

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
                else if (tag.equals("A")) {
                    LatLng ll = parseLatitudeLongitudeTuple(parts[6]);

                    PointData pointData = new PointData();
                    pointData.date = date;
                    pointData.position = ll;

                    result.actionData.add(pointData);
                }
            }
        }
        catch(Exception e) {
            App.getLogger().exception(TAG, e);
            Log.e(TAG, e.getMessage(), e);
        }

        return result;
    }

    // removes GPS fixes outside of time period [TimeOfLastFix-timePeriodMillis, TimeOfLastFix]
    private static void removeOutdatedFixes(LogParserResult parserResult, long timePeriodMillis) {
        Date startDate;
        if(timePeriodMillis > 0 && parserResult.pointData.size() > 0) {
            Date dateOfLastFix = parserResult.pointData.get(parserResult.pointData.size()-1).date;
            startDate= new Date(dateOfLastFix.getTime() - timePeriodMillis);
        } else {
            startDate = new Date(0);
        }

        for(Iterator<PointData> it = parserResult.pointData.iterator(); it.hasNext();) {
            PointData pointData = it.next();
            if(pointData.date.before(startDate)) {
                it.remove();
            }
        }
    }

    private static void removeDuplicateTriggers(LogParserResult logParserResult) {
        Set<AreaTriggerData> areaTriggerDataSet = new HashSet<>(logParserResult.areaTriggerData);
        logParserResult.areaTriggerData = new ArrayList<AreaTriggerData>(areaTriggerDataSet);

        Set<TransitionTriggerData> transitionTriggerDataSet = new HashSet<>(logParserResult.transitionTriggerData);
        logParserResult.transitionTriggerData = new ArrayList<TransitionTriggerData>(transitionTriggerDataSet);
    }

    private static void generateKml(LogParserResult logParserResult, File kmlFile) {
        try (
                GeoSwitchKml kml = new GeoSwitchKml(kmlFile);
        ){
            for(AreaTriggerData areaTriggerData: logParserResult.areaTriggerData) {
                kml.addAreaTrigger(areaTriggerData.center, areaTriggerData.radius);
            }

            for(TransitionTriggerData transitionTriggerData: logParserResult.transitionTriggerData) {
                kml.addTransitionTrigger(transitionTriggerData.from, transitionTriggerData.to, transitionTriggerData.radius);
            }

            for(PointData pointData: logParserResult.actionData) {
                kml.addActionFire(pointData.position, pointData.date);
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
                        kml.addPath(coordinates, startDate, prevDate);
                        startDate = pointData.date;
                        coordinates.clear();
                        coordinates.add(pointData.position);
                    }
                    prevDate = pointData.date;
                }
            }

            if(coordinates.size() > 0) {
                kml.addPath(coordinates, startDate, prevDate);
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

//    private static void generateSampleKml(File f) {
//        final Kml kml = new Kml(f, "GeoSwitch");
//
//        Date beginTime = null, endTime = null;
//        try {
//            beginTime = dateFormat.parse("2017-02-21 10:56:10.369");
//            endTime = dateFormat.parse("2017-02-21 10:57:10.370");
//        } catch (ParseException e) {
//        }
//
//        List<LatLng> coordinates = new ArrayList<>();
//        coordinates.add(new LatLng(30.63652993,50.23638183));
//        coordinates.add(new LatLng(30.637,50.23638183));
//        coordinates.add(new LatLng(30.638,50.23638183));
//
//        kml.addLineString(coordinates, beginTime, endTime);
//        kml.addPlacemark("Test", new LatLng(50.19542742565555, 30.666), "Test description");
//        kml.addCircle("Circle1", new LatLng(50.19542742565555, 30.666), 100, Kml.KML_COLOR_YELLOW);
//        kml.addCircle("Circle2", new LatLng(50.19542742565555, 30.667), 100, Kml.KML_COLOR_CORAL);
//        kml.finish();
//    }

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
