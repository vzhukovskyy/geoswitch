package ua.pp.rudiki.geoswitch.kml;

import android.location.Location;
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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.pp.rudiki.geoswitch.App;
import ua.pp.rudiki.geoswitch.kml.log.AreaTriggerRecord;
import ua.pp.rudiki.geoswitch.kml.log.PointRecord;
import ua.pp.rudiki.geoswitch.kml.log.TransitionTriggerRecord;
import ua.pp.rudiki.geoswitch.kml.log.TriggerRecord;
import ua.pp.rudiki.geoswitch.kml.log.TriggerRecordFactory;
import ua.pp.rudiki.geoswitch.peripherals.FileUtils;
import ua.pp.rudiki.geoswitch.trigger.GeoArea;
import ua.pp.rudiki.geoswitch.trigger.GeoPoint;

public class Log2Kml {
    private static final String TAG = Log2Kml.class.getSimpleName();

    private final static int COPY_BUFFER_SIZE = 4096;
    private final static int JOIN_POINTS_INTO_POLYLINE_DELAY = 3000;
    private final static int JOIN_POINTS_INTO_POLYLINE_DELAY_CONDITIONAL = 60*1000;
    private final static int JOIN_POINTS_INTO_POLYLINE_DISTANCE = 20;
    private final static int JOIN_POLYLINES_INTO_PATH_DELAY = 5*60*1000;

    private static DateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static DateFormat folderDateFormat = new SimpleDateFormat("MM/dd HH:mm");

    public static void log2kml(File kmlFile) {
        File tempLogFile = createTempFile();
        concatLogFiles(tempLogFile);

        LogParserResult parserResult = extractGeoDataFromLog(tempLogFile);
        Date periodStartDate = getStartOfDay(parserResult);
        //removeOutdatedFixes(parserResult, periodStartDate);
        removeOutdatedTriggers(parserResult, periodStartDate);
        removeOutdatedActions(parserResult, periodStartDate);
        removeDuplicateTriggers(parserResult);

        List<Path> paths = combinePointsIntoPaths(parserResult.pointRecords);
        generateKml(parserResult, paths, kmlFile);

        tempLogFile.delete();

        FileUtils.makeVisibleViaUsb(kmlFile);
    }

    private static class LogParserResult {
        List<PointRecord> pointRecords = new ArrayList<>();
        List<TriggerRecord> triggerRecords = new ArrayList<>();
        List<PointRecord> actionRecords = new ArrayList<>();
    }

    private static LogParserResult extractGeoDataFromLog(File logFile) {
        LogParserResult result = new LogParserResult();

        try (
                FileReader fileReader = new FileReader(logFile);
                BufferedReader br = new BufferedReader(fileReader);
        ){
            int lastCellId = 0;
            String lastNetworkClass = "";

            String logLine;
            while((logLine = br.readLine()) != null) {

                String[] parts = logLine.split("[ ]+");

                Date date;
                try {
                    if(parts.length < 2)
                        continue;

                    date = logDateFormat.parse(parts[0] + " " + parts[1]);
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

                    PointRecord pointRecord = new PointRecord();
                    pointRecord.date = date;
                    pointRecord.position = new LatLng(latitude, longitude);
                    pointRecord.cellId = lastCellId;
                    pointRecord.networkClass = lastNetworkClass;

                    result.pointRecords.add(pointRecord);
                }
                else if(tag.equals("T")) {
                    String triggerType = parts[3];

                    TriggerRecord triggerRecord = TriggerRecordFactory.createTriggerRecord(triggerType);
                    triggerRecord.date = date;

                    if(triggerType.equals("Exit") || triggerType.equals("Enter")) {
                        LatLng ll = parseLatitudeLongitudeTuple(parts[4]);
                        double radius = parseRadius(parts[5]);

                        AreaTriggerRecord areaTriggerRecord = (AreaTriggerRecord)triggerRecord;
                        areaTriggerRecord.center = ll;
                        areaTriggerRecord.radius = radius;
                    }
                    else if(triggerType.equals("Transition")) {
                        LatLng centerFrom = parseLatitudeLongitudeTuple(parts[5]);
                        LatLng centerTo = parseLatitudeLongitudeTuple(parts[8]);
                        double radius = parseRadius(parts[6]);

                        TransitionTriggerRecord transitionTriggerRecord = (TransitionTriggerRecord)triggerRecord;
                        transitionTriggerRecord.from = centerFrom;
                        transitionTriggerRecord.to = centerTo;
                        transitionTriggerRecord.radius = radius;
                    }

                    result.triggerRecords.add(triggerRecord);
                }
                else if (tag.equals("A")) {
                    LatLng ll = parseLatitudeLongitudeTuple(parts[6]);

                    PointRecord pointRecord = new PointRecord();
                    pointRecord.date = date;
                    pointRecord.position = ll;

                    result.actionRecords.add(pointRecord);
                }
                else if (tag.equals("C")) {
                    try {
                        lastCellId = Integer.parseInt(parts[6]);
                    }
                    catch (NumberFormatException e) {
                        continue;
                    }
                }
                else if (tag.equals("N")) {
                    lastNetworkClass = parts[5];
                }
            }
        }
        catch(Exception e) {
            App.getLogger().exception(TAG, e);
            Log.e(TAG, e.getMessage(), e);
        }

        return result;
    }

//    private static Date getStartOfPeriod(LogParserResult logParserResult) {
//        final long timePeriodMillis = App.getPreferences().getDefaultTimePeriodForKml();
//
//        Date startDate;
//        if(timePeriodMillis > 0 && logParserResult.pointRecords.size() > 0) {
//            Date dateOfLastFix = logParserResult.pointRecords.get(logParserResult.pointRecords.size()-1).date;
//            startDate = new Date(dateOfLastFix.getTime() - timePeriodMillis);
//        } else {
//            startDate = new Date(0);
//        }
//
//        return startDate;
//    }


    private static Date getStartOfDay(LogParserResult logParserResult) {
        Date startDate;
        if(logParserResult.pointRecords.size() > 0) {
            Date dateOfLastFix = logParserResult.pointRecords.get(logParserResult.pointRecords.size()-1).date;
            startDate = new Date(dateOfLastFix.getTime());
            startDate.setHours(0);
            startDate.setMinutes(0);
            startDate.setSeconds(0);
        } else {
            startDate = new Date(0);
        }

        return startDate;
    }

    // removes GPS fixes outside of time period [TimeOfLastFix-timePeriodMillis, TimeOfLastFix]
    private static void removeOutdatedFixes(LogParserResult logParserResult, Date periodStartDate) {
        for(Iterator<PointRecord> it = logParserResult.pointRecords.iterator(); it.hasNext();) {
            PointRecord pointRecord = it.next();
            if(pointRecord.date.before(periodStartDate)) {
                it.remove();
            }
        }
    }

    private static void removeOutdatedTriggers(LogParserResult logParserResult, Date periodStartDate) {
        for(Iterator<TriggerRecord> it = logParserResult.triggerRecords.iterator(); it.hasNext();) {
            TriggerRecord triggerRecord = it.next();
            if(triggerRecord.date.before(periodStartDate)) {
                it.remove();
            }
        }
    }

    private static void removeOutdatedActions(LogParserResult logParserResult, Date periodStartDate) {
        for(Iterator<PointRecord> it = logParserResult.actionRecords.iterator(); it.hasNext();) {
            PointRecord actionRecord = it.next();
            if(actionRecord.date.before(periodStartDate)) {
                it.remove();
            }
        }
    }

    private static void removeDuplicateTriggers(LogParserResult logParserResult) {
        Set<TriggerRecord> set = new LinkedHashSet<>(logParserResult.triggerRecords);
        logParserResult.triggerRecords = new ArrayList<>(set);
    }


    private static class Polyline {
        List<LatLng> points = new ArrayList<>();
        Date startDate;
        Date endDate;
        int cellId;
        String networkClass;
        String tag;
    }

    private static class Path {
        List<Polyline> polylines = new ArrayList<>();
    }

    private static List<Path> combinePointsIntoPaths(List<PointRecord> pointRecords) {
        List<Path> paths = new ArrayList<>();
        Path path = new Path();
        Polyline polyline = new Polyline();

        PointRecord prevPointRecord = null;
        for(PointRecord pointRecord : pointRecords) {
            if(prevPointRecord == null) {
                polyline.startDate = pointRecord.date;
                polyline.points.add(pointRecord.position);
            }
            else {
                long timeDelta = pointRecord.date.getTime() - prevPointRecord.date.getTime();
                double distance = distanceBetween(pointRecord.position, prevPointRecord.position);

//                boolean joinPointsIntoPolyline = timeDelta <= JOIN_POINTS_INTO_POLYLINE_DELAY;
                boolean joinPointsIntoPolyline = (timeDelta <= JOIN_POINTS_INTO_POLYLINE_DELAY) ||
                        (timeDelta <= JOIN_POINTS_INTO_POLYLINE_DELAY_CONDITIONAL && distance <= JOIN_POINTS_INTO_POLYLINE_DISTANCE);
                boolean joinPolylinesIntoPathDelayExceeded = timeDelta > JOIN_POLYLINES_INTO_PATH_DELAY;
                boolean cellChanged = (pointRecord.cellId != prevPointRecord.cellId);
                boolean networkClassChanged = !Objects.equals(pointRecord.networkClass, prevPointRecord.networkClass);

                if(!joinPointsIntoPolyline || cellChanged || networkClassChanged) {
                    polyline.endDate = prevPointRecord.date;
                    polyline.cellId = prevPointRecord.cellId;
                    polyline.networkClass = prevPointRecord.networkClass;
                    path.polylines.add(polyline);

                    polyline = new Polyline();
                    polyline.startDate = pointRecord.date;
                }

                polyline.points.add(pointRecord.position);

                if(joinPolylinesIntoPathDelayExceeded) {
                    paths.add(path);
                    path = new Path();
                }
            }

            prevPointRecord = pointRecord;
        }

        if(polyline.points.size() > 0) {
            polyline.endDate = prevPointRecord.date;
            polyline.cellId = prevPointRecord.cellId;
            polyline.networkClass = prevPointRecord.networkClass;
            path.polylines.add(polyline);
            paths.add(path);
        }

        return paths;
    }

    private static double distanceBetween(LatLng ll1, LatLng ll2) {
        float[] results = new float[1];
        Location.distanceBetween(ll1.latitude, ll1.longitude, ll2.latitude, ll2.longitude, results);
        float distanceInMeters = results[0];
        return distanceInMeters;
    }

    private static void generateKml(LogParserResult logParserResult, List<Path> paths, File kmlFile) {
        try (
                GeoSwitchKml kml = new GeoSwitchKml(kmlFile);
        ){
            for(TriggerRecord triggerRecord : logParserResult.triggerRecords) {
                if(triggerRecord instanceof AreaTriggerRecord) {
                    AreaTriggerRecord areaTriggerRecord = (AreaTriggerRecord)triggerRecord;
                    kml.addAreaTrigger(areaTriggerRecord.center, areaTriggerRecord.radius);
                }
                else if (triggerRecord instanceof TransitionTriggerRecord) {
                    TransitionTriggerRecord transitionTriggerRecord = (TransitionTriggerRecord)triggerRecord;
                    kml.addTransitionTrigger(transitionTriggerRecord.from, transitionTriggerRecord.to, transitionTriggerRecord.radius);
                }
            }

            for(PointRecord pointRecord : logParserResult.actionRecords) {
                kml.addActionFire(pointRecord.position, pointRecord.date);
            }

            for(Path path: paths) {
                String folderName;
                if(isAtHome(path))
                    folderName = "At Home";
                else if(isPathFromHome(path))
                    folderName = "Path From Home";
                else if(isPathToHome(path))
                    folderName = "Path To Home";
                else
                    folderName = "Other Path";

                kml.openFolder(folderName+" "+folderDateFormat.format(path.polylines.get(0).startDate));

                for(Polyline polyline: path.polylines) {
                    if(isSingularPath(polyline.points)) {
                        kml.addPoint(polyline.points.get(0), polyline.startDate, polyline.cellId, polyline.networkClass);
                    } else {
                        kml.addPath(polyline.points, polyline.startDate, polyline.endDate, polyline.cellId, polyline.networkClass);
                    }
                }

                kml.closeFolder();
            }
        }
        catch(Exception e) {
            App.getLogger().exception(TAG, e);
        }
    }

    private static boolean isAtHome(Path path) {
        for(Polyline polyline: path.polylines)
            for(LatLng ll: polyline.points)
                if(!isInsideHomeArea(ll))
                    return false;

        return true;
    }

    private static boolean isPathToHome(Path path) {
        Polyline lastPolyline = path.polylines.get(path.polylines.size()-1);
        LatLng lastFix = lastPolyline.points.get(lastPolyline.points.size()-1);

        return isInsideHomeArea(lastFix);
    }

    private static boolean isPathFromHome(Path path) {
        LatLng firstFix = path.polylines.get(0).points.get(0);

        return isInsideHomeArea(firstFix);
    }

    private static boolean isInsideHomeArea(LatLng ll) {
        GeoArea homeArea = App.getPreferences().loadHomeArea();
        GeoPoint point = new GeoPoint(ll.latitude, ll.longitude);

        boolean inside = (homeArea.getCenter().distanceTo(point) < homeArea.getRadius());
        return inside;
    }

    private static boolean isSingularPath(List<LatLng> coordinates) {
        int size = coordinates.size();
        if(size < 2)
            return true;

        LatLng firstFix = coordinates.get(0);
        for(int i=1; i<coordinates.size(); i++) {
            if(!firstFix.equals(coordinates.get(i))) {
                return false;
            }
        }

        return true;
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
