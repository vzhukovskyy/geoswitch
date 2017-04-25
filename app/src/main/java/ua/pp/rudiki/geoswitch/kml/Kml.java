package ua.pp.rudiki.geoswitch.kml;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import ua.pp.rudiki.geoswitch.App;

public class Kml {
    public final static String KML_COLOR_RED = "FF0000FF"; // aabbggrr
    public final static String KML_COLOR_GREEN = "FF00FF00";
    public final static String KML_COLOR_BLUE = "FFFF0000";
    public final static String KML_COLOR_YELLOW = "4F00FFFF";
    public final static String KML_COLOR_CORAL = "7F7F7FFF";

    public final static String URL_YELLOW_PIN = "http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png";
    public final static String URL_RED_PIN = "http://maps.google.com/mapfiles/kml/pushpin/red-pushpin.png";
    public final static String URL_BLUE_PIN = "http://maps.google.com/mapfiles/kml/pushpin/blue-pushpin.png";

    final static String TAG = Kml.class.getSimpleName();

    private FileOutputStream stream;
    private OutputStreamWriter writer;
    private DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    private Map<String, Integer> mapId = new TreeMap<>();

    public Kml(File file, String documentName) {
        try {
            stream = new FileOutputStream(file);
            writer = new OutputStreamWriter(stream);
        }
        catch(Exception e) {
            App.getLogger().exception(TAG, e);
        }

        startXml();
        startDocument(documentName);
    }

    public void finish() {
        closeFolder();
        endDocument();
        endXml();
        closeStream();
    }

    public void addLineStyle(String styleName, String color, int lineWidth) {
        String style =
                "        <Style id=\""+styleName+"\">\n" +
                "            <LineStyle id=\"" + newId("LineSubStyle") + "\">\n" +
                "                <color>" + color + "</color>\n" +
                "                <colorMode>normal</colorMode>\n" +
                "                <width>" + lineWidth + "</width>\n" +
                "            </LineStyle>\n" +
                "        </Style>\n";
        write(style);
    }

    public void addCircleStyle(String styleName, String color) {
        String style =
                "        <Style id=\"" + styleName + "\">\n" +
                "            <PolyStyle id=\"" + newId("PolySubStyle") + "\">\n" +
                "                <color>" + color + "</color>\n" +
                "                <colorMode>normal</colorMode>\n" +
                "                <fill>1</fill>\n" +
                "                <outline>1</outline>\n" +
                "            </PolyStyle>\n" +
                "        </Style>\n";
        write(style);
    }

    public void addIconStyle(String styleName, String iconUrl) {
        String style =
                "        <Style id=\""+styleName+"\">\n" +
                "            <IconStyle>\n" +
                "                <Icon>\n" +
                "                   <href>" + iconUrl + "</href>\n" +
                "                </Icon>\n" +
                "            </IconStyle>\n" +
                "        </Style>\n";
        write(style);
    }

    public void addLineString(List<LatLng> coordinates, Date begin, Date end, String description, String lineStyle) {
        String placemarkHeader =
                "        <Placemark id=\"" + newId("Placemark") + "\">\n" +
                "            <description>" + description + "</description>\n" +
                "            <TimeSpan id=\"" + newId("Timespan") + "\">\n" +
                "                <begin>" + isoDateFormat.format(begin) + "</begin>\n" +
                "                <end>" + isoDateFormat.format(end) + "</end>\n" +
                "            </TimeSpan>\n" +
                "            <styleUrl>#"+lineStyle+"</styleUrl>\n" +
                "            <LineString id=\"" + newId("LineString") + "\">\n" +
                "                <coordinates>";
        writeLine(placemarkHeader);

        //30.63654993,50.23638183,0.0 30.63640310,50.23658868,0.0 30.63625572,50.23679820,0.0 30.63610434,50.23701454,0.0 30.63593247,50.23723833,0.0 30.63577110,50.23746488,0.0 30.63560968,50.23769238,0.0 30.63544774,50.23793890,0.0 30.63528170,50.23816571,0.0 30.63512884,50.23839679,0.0 30.63495004,50.23862722,0.0 30.63475828,50.23884555,0.0 30.63458398,50.23908217,0.0 30.63447346,50.23931430,0.0 30.63433309,50.23953526,0.0 30.63414524,50.23976568,0.0
        for(LatLng ll: coordinates) {
            write(formatDouble(ll.longitude) + "," + formatDouble(ll.latitude) + ",0.0 ");
        }

        String placemarkFooter = "\n" +
                "                </coordinates>\n" +
                "            </LineString>\n" +
                "        </Placemark>";
        writeLine(placemarkFooter);
    }

    public void addPlacemark(String name, LatLng ll, String description, String iconStyle) {
        String placemark =
                "        <Placemark id=\"" + newId("Placemark") + "\">\n" +
                "            <name>" + name + "</name>\n" +
                "            <styleUrl>#" + iconStyle + "</styleUrl>\n" +
                "            <description>" + description + "</description>\n" +
                "            <Point id=\"" + newId("Point") + "\">\n" +
                "                <coordinates>" + formatDouble(ll.longitude) + "," + formatDouble(ll.latitude) + ",0.0</coordinates>\n" +
                "            </Point>\n" +
                "        </Placemark>";
        writeLine(placemark);
    }

    public void addCircle(String name, LatLng center, double radius, String circleStyle) {
        String placemarkHeader =
                "        <Placemark id=\"" + newId("Placemark") + "\">\n" +
                "            <name>" + name + "</name>\n" +
                "            <styleUrl>#" + circleStyle + "</styleUrl>\n" +
                "            <Polygon id=\"" + newId("Polygon") + "\">\n" +
                "                <outerBoundaryIs>\n" +
                "                    <LinearRing id=\"" + newId("Ring") + "\">\n" +
                "                        <coordinates>";
        writeLine(placemarkHeader);

        //30.667216144502166,50.19652704674885,0.0 30.66743162308411,50.19651494503234,0.0 30.667640554139798,50.196479007605205,0.0 30.667836589124672,50.19642032645948,0.0 30.668013771406983,50.19634068467098,0.0 30.668166717282105,50.19624250221187,0.0 30.66829077956703,50.19612876240947,0.0 30.668382188802667,50.19600292128688,0.0 30.668438167773587,50.195868802541845,0.0 30.668457015867244,50.195730481355994,0.0 30.668438160712412,50.19559216056646,0.0 30.668382175532,50.195458042962564,0.0 30.668290761687505,50.19533220358834,0.0 30.668166696950255,50.19521846593061,0.0 30.668013751075126,50.19512028575381,0.0 30.66783657124514,50.19504064610996,0.0 30.66764054086913,50.194981966712575,0.0 30.667431616022935,50.19494603042659,0.0 30.667216144502166,50.19493392910639,0.0 30.6670006729814,50.19494603042659,0.0 30.666791748135203,50.194981966712575,0.0 30.666595717759193,50.19504064610996,0.0 30.666418537929207,50.19512028575381,0.0 30.666265592054078,50.19521846593061,0.0 30.666141527316828,50.19533220358834,0.0 30.666050113472334,50.195458042962564,0.0 30.66599412829192,50.19559216056646,0.0 30.66597527313709,50.195730481355994,0.0 30.665994121230746,50.195868802541845,0.0 30.666050100201666,50.19600292128688,0.0 30.666141509437303,50.19612876240947,0.0 30.666265571722228,50.19624250221187,0.0 30.66641851759735,50.19634068467098,0.0 30.66659569987966,50.19642032645948,0.0 30.666791734864535,50.196479007605205,0.0 30.667000665920224,50.19651494503234,0.0 30.667216144502166,50.19652704674885,0.0" +
        List<LatLng> coordinates = generateCircularPolygon(center, radius);
        for(LatLng ll: coordinates) {
            write(formatDouble(ll.longitude) + "," + formatDouble(ll.latitude) + ",0.0 ");
        }

        String placemarkFooter = "\n" +
                "                        </coordinates>\n" +
                "                    </LinearRing>\n" +
                "                </outerBoundaryIs>\n" +
                "            </Polygon>\n" +
                "        </Placemark>";
        writeLine(placemarkFooter);

    }

    private List<LatLng> generateCircularPolygon(LatLng center, double radius) {
        final int nPoints = 36;

        List<LatLng> coordinates = new ArrayList<>();

        for(int i=0; i<nPoints+1 /* need to repeat first point in the end*/; i++) {
            double alpha = i*360/nPoints;
            double dy = radius*Math.cos(alpha/180*Math.PI);
            double dx = radius*Math.sin(alpha/180*Math.PI);

            LatLng point = shiftLatLngByMeters(center, dx, dy);
            coordinates.add(point);
        }

        return coordinates;
    }

    public static LatLng shiftLatLngByMeters(LatLng point, double dx, double dy) {
        final double earthRadius = 6371000;

        final double earthCircumference = 2*Math.PI*earthRadius;
        double сircumferenceAtLatitude = earthCircumference*Math.cos(point.latitude/180*Math.PI);

        double metersPerLatitudeDegree = earthCircumference/360;
        double metersPerLongitudeDegreeAtLatitude = сircumferenceAtLatitude/360;

        double dlat = dy/metersPerLatitudeDegree;
        double dlon = dx/metersPerLongitudeDegreeAtLatitude;

        return new LatLng(point.latitude+dlat, point.longitude+dlon);
    }

    private String newId(String prefix) {
        int id = 1;
        if(mapId.containsKey(prefix)) {
            id = mapId.get(prefix);
            id++;
        }
        mapId.put(prefix, id);

        return prefix + id;
    }

    private String formatDouble(double d) {
        return String.format(Locale.US, "%f", d);
    }

    public void startXml() {
        writeLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writeLine("<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">");
    }

    public void endXml() {
        writeLine("</kml>");
    }

    private void startDocument(String name) {
        writeLine("<Document id=\"doc1\">");
    }

    private void endDocument() {
        writeLine("</Document>");
    }

    private boolean folderOpened = false;

    public void openFolder(String name) {
        writeLine("<Folder>");
        writeLine("<name>"+name+"</name>");
        folderOpened = true;
    }

    public void closeFolder() {
        if(folderOpened) {
            writeLine("</Folder>");
            folderOpened = false;
        }
    }

    private void write(String line) {
        if(writer == null)
            return;

        try {
            writer.write(line);
        } catch (IOException e) {
            App.getLogger().exception(TAG, e);
        }
    }

    private void writeLine(String line) {
        write(line+"\n");
    }

    private void closeStream() {
        try {
            if(writer != null)
                writer.close();
        } catch (IOException e) {
            App.getLogger().exception(TAG, e);
        }

        try {
            if(stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            App.getLogger().exception(TAG, e);
        }
    }

}
