package ua.pp.rudiki.geoswitch.kml;

import android.graphics.Color;
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

    private final static String areaCircleStyle = "CircleStyleArea";
    private final static String areaToCircleStyle = "CircleStyleTo";

    private final static String triggerPinStyle = "PinStyleTrigger";
    private final static String firePinStyle = "PinStyleFire";

    private DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSS");

    private File kmlFile;
    private Kml kml;
    private Map<String, Integer> mapUniqueName = new TreeMap<>();
    private Map<String, String> mapCellToPathStyle = new TreeMap<>();


    GeoSwitchKml(File file) {
        kml = new Kml(file, "GeoSwitch");
        this.kmlFile = file;

        kml.addCircleStyle(areaCircleStyle, Kml.KML_COLOR_YELLOW);
        kml.addCircleStyle(areaToCircleStyle, Kml.KML_COLOR_CORAL);
        kml.addIconStyle(triggerPinStyle, Kml.URL_YELLOW_PIN);
        kml.addIconStyle(firePinStyle, Kml.URL_RED_PIN);
    }

    public void addPoint(LatLng point, Date date, String cellId, String networkClass) {
        List<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(point);
        LatLng newPoint = Kml.shiftLatLngByMeters(point, 2, 2);
        coordinates.add(newPoint);
        addPath(coordinates, date, date, cellId, networkClass);
    }

    public void addPath(List<LatLng> coordinates, Date begin, Date end, String cellId, String networkClass) {
        String style = getPathStyle(cellId);

        String description = simpleDateFormat.format(begin)+
                "\n-\n"+simpleDateFormat.format(end)+
                "\n\nClass: "+String.valueOf(networkClass)+ // String.valueOf for handling null
                "\nCell ID: "+String.valueOf(cellId);

        kml.addLineString(coordinates, begin, end, description, style);
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

    public void openFolder(String name) {
        kml.openFolder(name);
    }

    public void closeFolder() {
        kml.closeFolder();
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
        return new StringBuilder()
                .append("(")
                .append(ll.longitude)
                .append(",")
                .append(ll.latitude)
                .append(")")
                .toString();
    }

    private String getPathStyle(String cellId) {
        if(cellId == null)
            cellId = "NoCell";

        String style;
        if(!mapCellToPathStyle.containsKey(cellId)) {
            style = "LineStyle"+cellId;

            int color = cellColor(cellId);

            String s = String.format("%X", color);
            kml.addLineStyle(style, s, 5);

            mapCellToPathStyle.put(cellId, style);
        }
        else {
            style = mapCellToPathStyle.get(cellId);
        }

        return style;
    }

    private int cellColor(String сellId) {
        int[] material_design_colors = {
                0xffebee,0xFCE4EC,0xF3E5F5,0xEDE7F6,0xE8EAF6,0xE3F2FD,0xE1F5FE,0xE0F7FA,0xE0F2F1,0xE8F5E9,0xF1F8E9,0xF9FBE7,0xFFFDE7,0xFFF8E1,0xFFF3E0,0xFBE9E7,0xEFEBE9,0xFAFAFA,0xECEFF1,0xffcdd2,0xF8BBD0,0xE1BEE7,0xD1C4E9,0xC5CAE9,0xBBDEFB,0xB3E5FC,0xB2EBF2,0xB2DFDB,0xC8E6C9,0xDCEDC8,0xF0F4C3,0xFFF9C4,0xFFECB3,0xFFE0B2,0xFFCCBC,0xD7CCC8,0xF5F5F5,0xCFD8DC,0xef9a9a,0xF48FB1,0xCE93D8,0xB39DDB,0x9FA8DA,0x90CAF9,0x81D4FA,0x80DEEA,0x80CBC4,0xA5D6A7,0xC5E1A5,0xE6EE9C,0xFFF59D,0xFFE082,0xFFCC80,0xFFAB91,0xBCAAA4,0xEEEEEE,0xB0BEC5,0xe57373,0xF06292,0xBA68C8,0x9575CD,0x7986CB,0x64B5F6,0x4FC3F7,0x4DD0E1,0x4DB6AC,0x81C784,0xAED581,0xDCE775,0xFFF176,0xFFD54F,0xFFB74D,0xFF8A65,0xA1887F,0xE0E0E0,0x90A4AE,0xef5350,0xEC407A,0xAB47BC,0x7E57C2,0x5C6BC0,0x42A5F5,0x29B6F6,0x26C6DA,0x26A69A,0x66BB6A,0x9CCC65,0xD4E157,0xFFEE58,0xFFCA28,0xFFA726,0xFF7043,0x8D6E63,0xBDBDBD,0x78909C,0xf44336,0xE91E63,0x9C27B0,0x673AB7,0x3F51B5,0x2196F3,0x03A9F4,0x00BCD4,0x009688,0x4CAF50,0x8BC34A,0xCDDC39,0xFFEB3B,0xFFC107,0xFF9800,0xFF5722,0x795548,0x9E9E9E,0x607D8B,0xe53935,0xD81B60,0x8E24AA,0x5E35B1,0x3949AB,0x1E88E5,0x039BE5,0x00ACC1,0x00897B,0x43A047,0x7CB342,0xC0CA33,0xFDD835,0xFFB300,0xFB8C00,0xF4511E,0x6D4C41,0x757575,0x546E7A,0xd32f2f,0xC2185B,0x7B1FA2,0x512DA8,0x303F9F,0x1976D2,0x0288D1,0x0097A7,0x00796B,0x388E3C,0x689F38,0xAFB42B,0xFBC02D,0xFFA000,0xF57C00,0xE64A19,0x5D4037,0x616161,0x455A64,0xc62828,0xAD1457,0x6A1B9A,0x4527A0,0x283593,0x1565C0,0x0277BD,0x00838F,0x00695C,0x2E7D32,0x558B2F,0x9E9D24,0xF9A825,0xFF8F00,0xEF6C00,0xD84315,0x4E342E,0x424242,0x37474F,0xb71c1c,0x880E4F,0x4A148C,0x311B92,0x1A237E,0x0D47A1,0x01579B,0x006064,0x004D40,0x1B5E20,0x33691E,0x827717,0xF57F17,0xFF6F00,0xE65100,0xBF360C,0x3E2723,0x212121,0x263238,0xff8a80,0xFF80AB,0xEA80FC,0xB388FF,0x8C9EFF,0x82B1FF,0x80D8FF,0x84FFFF,0xA7FFEB,0xB9F6CA,0xCCFF90,0xF4FF81,0xFFFF8D,0xFFE57F,0xFFD180,0xFF9E80,0xff5252,0xFF4081,0xE040FB,0x7C4DFF,0x536DFE,0x448AFF,0x40C4FF,0x18FFFF,0x64FFDA,0x69F0AE,0xB2FF59,0xEEFF41,0xFFFF00,0xFFD740,0xFFAB40,0xFF6E40,0xff1744,0xF50057,0xD500F9,0x651FFF,0x3D5AFE,0x2979FF,0x00B0FF,0x00E5FF,0x1DE9B6,0x00E676,0x76FF03,0xC6FF00,0xFFEA00,0xFFC400,0xFF9100,0xFF3D00,0xd50000,0xC51162,0xAA00FF,0x6200EA,0x304FFE,0x2962FF,0x0091EA,0x00B8D4,0x00BFA5,0x00C853,0x64DD17,0xAEEA00,0xFFD600,0xFFAB00,0xFF6D00,0xDD2C00
                // 254 total
        };
        int i = cellHash(сellId) % material_design_colors.length;
        int rgb = material_design_colors[i];
        int bgr = Color.rgb(Color.blue(rgb), Color.green(rgb), Color.red(rgb));
        return 0xFF000000 | bgr;
    }

    private int cellHash(String сellId) {
        int id = makeNumericCellId(сellId);
        return hash8(id);
    }

    private int makeNumericCellId(String cellId) {
        int lastMinus = cellId.lastIndexOf('-');
        String cid = cellId.substring(lastMinus+1);

        int numericId;
        try {
            numericId = Integer.parseInt(cid);
        }
        catch(NumberFormatException e) {
            numericId = 0;
        }

        return numericId;
    }

    private int hash8(int value) {
        // based on http://burtleburtle.net/bob/hash/integer.html
        int a = value;
        a = (a+0x7ed55d16) + (a<<12);
        a = (a^0xc761c23c) ^ (a>>19);
        a = (a+0x165667b1) + (a<<5);
        a = (a+0xd3a2646c) ^ (a<<9);
        a = (a+0xfd7046c5) + (a<<3);
        a = (a^0xb55a4f09) ^ (a>>16);

        // XOR all bytes
        int h = 0;
        for(int i=0; i<4; i++){
            h ^= a&0xFF;
            a >>= 8;
        }

        return h;
    }
}
