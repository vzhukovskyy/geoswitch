package ua.pp.rudiki.geoswitch.kml;

import android.media.MediaScannerConnection;

import com.google.android.gms.maps.model.LatLng;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ua.pp.rudiki.geoswitch.App;

public class GeoSwitchKml implements Closeable {

    private final static String pathStyle = "LineStyle";

    private final static String areaCircleStyle = "CircleStyleArea";
    private final static String areaToCircleStyle = "CircleStyleTo";

    private final static String triggerPinStyle = "PinStyleTrigger";
    private final static String firePinStyle = "PinStyleFire";
    private final static String pointPinStyle = "PointStyleTrigger";

    private DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS");

    private File kmlFile;
    private Kml kml;
    private Map<String, Integer> mapUniqueName = new TreeMap<>();


    GeoSwitchKml(File file) {
        kml = new Kml(file, "GeoSwitch");
        this.kmlFile = file;

        kml.addLineStyle(pathStyle, Kml.KML_COLOR_GREEN, 5);
        kml.addCircleStyle(areaCircleStyle, Kml.KML_COLOR_YELLOW);
        kml.addCircleStyle(areaToCircleStyle, Kml.KML_COLOR_CORAL);
        kml.addIconStyle(triggerPinStyle, Kml.URL_YELLOW_PIN);
        kml.addIconStyle(firePinStyle, Kml.URL_RED_PIN);
        kml.addIconStyle(pointPinStyle, Kml.URL_BLUE_PIN);
    }

    public void addPoint(LatLng point, Date date) {
        //kml.addPlacemark("", point, simpleDateFormat.format(date), pointPinStyle);
        List<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(point);
        LatLng newPoint = Kml.shiftLatLngByMeters(point, 2, 2);
        coordinates.add(newPoint);
        addPath(coordinates, date, date);
    }

    public void addPath(List<LatLng> coordinates, Date begin, Date end) {
        String description = simpleDateFormat.format(begin) + "\n-\n" + simpleDateFormat.format(end);
        kml.addLineString(coordinates, begin, end, description, pathStyle);
    }

    public void addAreaTrigger(LatLng center, double radius) {
        String name = uniqueName("Area");
        String description = formatCoordinates(center) + ", R="+radius;

        kml.addPlacemark(name, center, description, triggerPinStyle);
        kml.addCircle(name, center, radius, areaCircleStyle);
    }

    public void addTransitionTrigger(LatLng from, LatLng to, double radius) {
        String nameFrom = uniqueName("From");
        String nameTo = uniqueName("To");
        String descriptionFrom = formatArea(from, radius);
        String descriptionTo = formatArea(to, radius);

        kml.addPlacemark(nameFrom, from, descriptionFrom, triggerPinStyle);
        kml.addCircle(nameFrom, from, radius, areaCircleStyle);

        kml.addPlacemark(nameTo, to, descriptionTo, triggerPinStyle);
        kml.addCircle(nameTo, to, radius, areaToCircleStyle);
    }

    public void addActionFire(LatLng ll, Date date) {
        String name = uniqueName("Action");
        String description = simpleDateFormat.format(date) + "\n" + formatCoordinates(ll);

        kml.addPlacemark(name, ll, description, firePinStyle);
    }

    @Override
    public void close() throws IOException {
        kml.finish();

        MediaScannerConnection.scanFile(App.getAppContext(), new String[] {kmlFile.getAbsolutePath()}, null, null);
    }

    private String uniqueName(String name) {
        int id = 1;
        if(mapUniqueName.containsKey(name)) {
            id = mapUniqueName.get(name);
            id++;
        }
        mapUniqueName.put(name, id);

        return name + id;
    }

    private String formatArea(LatLng center, double radius) {
        return formatCoordinates(center) + ", R=" + radius;
    }

    private String formatCoordinates(LatLng ll) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(ll.longitude);
        sb.append(",");
        sb.append(ll.latitude);
        sb.append(")");
        return sb.toString();
    }
}
