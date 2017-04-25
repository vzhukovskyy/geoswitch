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
import java.util.Random;
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
    private Map<Integer, String> mapCellToPathStyle = new TreeMap<>();


    GeoSwitchKml(File file) {
        kml = new Kml(file, "GeoSwitch");
        this.kmlFile = file;

        kml.addCircleStyle(areaCircleStyle, Kml.KML_COLOR_YELLOW);
        kml.addCircleStyle(areaToCircleStyle, Kml.KML_COLOR_CORAL);
        kml.addIconStyle(triggerPinStyle, Kml.URL_YELLOW_PIN);
        kml.addIconStyle(firePinStyle, Kml.URL_RED_PIN);
    }

    public void addPoint(LatLng point, Date date, int cellId) {
        List<LatLng> coordinates = new ArrayList<LatLng>();
        coordinates.add(point);
        LatLng newPoint = Kml.shiftLatLngByMeters(point, 2, 2);
        coordinates.add(newPoint);
        addPath(coordinates, date, date, cellId);
    }

    public void addPath(List<LatLng> coordinates, Date begin, Date end, int cellId) {
        String description = simpleDateFormat.format(begin) + "\n-\n" + simpleDateFormat.format(end) + "\ncell: " + cellId;
        kml.addLineString(coordinates, begin, end, description, getPathStyle(cellId));
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

    public void startFolder(String name) {
        kml.closeFolder();
        kml.openFolder(name);
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

    private String getPathStyle(int cellId) {
        String style;
        if(!mapCellToPathStyle.containsKey(cellId)) {
            style = "LineStyle"+cellId;

            //int color = nextColor(mapCellToPathStyle.size());
            // TODO: external cell color mapping needed
            int cellHash = cellId;
            int color = nextColor(cellHash);

            String s = String.format("%X", color);
            kml.addLineStyle(style, s, 5);

            mapCellToPathStyle.put(cellId, style);
        }
        else {
            style = mapCellToPathStyle.get(cellId);
        }

        return style;
    }

    private int nextColor(int index) {
//        int[] colors = {
//                0x00a2ff, 0x6483d9, 0x963bff, 0xa97bb3, 0xb3527c, 0x0089d9, 0x526cb3, 0x8032d9, 0x85618c, 0x8c4161, 0x0071b3, 0x41558c, 0x6929b3, 0xff00cc, 0xffb0d2, 0x00598c, 0xb0c5ff, 0x5a2399, 0xcc00a3, 0xd996b3, 0x3bb7ff, 0x96a7d9, 0xb675ff, 0x99007a, 0xb37b93, 0x329cd9, 0x7b8ab3, 0x9a64d9, 0xe635c2, 0x8c6174, 0x2980b3, 0x616c8c, 0x7f52b3, 0xb32997, 0xff0011, 0x20658c, 0x1a00ff, 0x64418c, 0xff75e3, 0xd9000e, 0x75cdff, 0x1600d9, 0xd5b0ff, 0xd964c1, 0xb2000c, 0x64aed9, 0x1200b3, 0xb596d9, 0xb3529f, 0x99000a, 0x528fb3, 0x0f0099, 0x957bb3, 0x8c417d, 0xff3b48, 0x41708c, 0x4635e6, 0x75618c, 0xffb0ef, 0xd9323d, 0xb0e2ff, 0x3b2cbf, 0xd500ff, 0xd996cb, 0xb22932, 0x96c0d9, 0x8375ff, 0xb500d9, 0xb37ba7, 0x8c2027, 0x7b9eb3, 0x6f64d9, 0x9500b3, 0x8c6184, 0xff757e, 0x617c8c, 0x5c52b3, 0x75008c, 0xff006f, 0xd9646c, 0x0044ff, 0x48418c, 0xd338f2, 0xd9005e, 0xb25259, 0x003ad9, 0xb8b0ff, 0xb22fcc, 0xb2004d, 0x8c4146, 0x0030b3, 0x9c96d9, 0x9026a6, 0x8c003d, 0xffb0b5, 0x002999, 0x817bb3, 0xe875ff, 0xff3b90, 0xd9969a, 0x3b6fff, 0x65618c, 0xc564d9, 0xd9327a, 0xb37b7f, 0x325ed9, 0x7700ff, 0xa252b3, 0xb22965, 0x8c6164, 0x294eb3, 0x6500d9, 0x8b4699, 0x8c204f, 0x203d8c, 0x5300b3, 0xf2b0ff, 0xff75b1, 0x759aff, 0x470099, 0xce96d9, 0xd96496,
//                0xff0000, 0xd96464, 0x993800, 0xd9ae96, 0x8c6f20, 0xf20000, 0xb25252, 0xff833b, 0xb38f7b, 0xffda75, 0xcc0000, 0x8c4141, 0xd96f32, 0x8c7161, 0xd9ba64, 0xa60000, 0xffb0b0, 0xb25b29, 0xffbb00, 0xb29952, 0x8c0000, 0xd99696, 0x8c4820, 0xd99f00, 0x998346, 0xff3b3b, 0xb37b7b, 0xffa875, 0xb28300, 0xffeab0, 0xd93232, 0x8c6161, 0xd98f64, 0x997000, 0xd9c796, 0xb22929, 0xff5e00, 0xb27552, 0xffcb3b, 0xb3a47b, 0x992323, 0xd94f00, 0x8c5c41, 0xd9ac32, 0x8c8161, 0xff7575, 0xb24100, 0xffcdb0, 0xb28e29
//        };


        int[] material_design_colors = {
                0xffebee,0xFCE4EC,0xF3E5F5,0xEDE7F6,0xE8EAF6,0xE3F2FD,0xE1F5FE,0xE0F7FA,0xE0F2F1,0xE8F5E9,0xF1F8E9,0xF9FBE7,0xFFFDE7,0xFFF8E1,0xFFF3E0,0xFBE9E7,0xEFEBE9,0xFAFAFA,0xECEFF1,0xffcdd2,0xF8BBD0,0xE1BEE7,0xD1C4E9,0xC5CAE9,0xBBDEFB,0xB3E5FC,0xB2EBF2,0xB2DFDB,0xC8E6C9,0xDCEDC8,0xF0F4C3,0xFFF9C4,0xFFECB3,0xFFE0B2,0xFFCCBC,0xD7CCC8,0xF5F5F5,0xCFD8DC,0xef9a9a,0xF48FB1,0xCE93D8,0xB39DDB,0x9FA8DA,0x90CAF9,0x81D4FA,0x80DEEA,0x80CBC4,0xA5D6A7,0xC5E1A5,0xE6EE9C,0xFFF59D,0xFFE082,0xFFCC80,0xFFAB91,0xBCAAA4,0xEEEEEE,0xB0BEC5,0xe57373,0xF06292,0xBA68C8,0x9575CD,0x7986CB,0x64B5F6,0x4FC3F7,0x4DD0E1,0x4DB6AC,0x81C784,0xAED581,0xDCE775,0xFFF176,0xFFD54F,0xFFB74D,0xFF8A65,0xA1887F,0xE0E0E0,0x90A4AE,0xef5350,0xEC407A,0xAB47BC,0x7E57C2,0x5C6BC0,0x42A5F5,0x29B6F6,0x26C6DA,0x26A69A,0x66BB6A,0x9CCC65,0xD4E157,0xFFEE58,0xFFCA28,0xFFA726,0xFF7043,0x8D6E63,0xBDBDBD,0x78909C,0xf44336,0xE91E63,0x9C27B0,0x673AB7,0x3F51B5,0x2196F3,0x03A9F4,0x00BCD4,0x009688,0x4CAF50,0x8BC34A,0xCDDC39,0xFFEB3B,0xFFC107,0xFF9800,0xFF5722,0x795548,0x9E9E9E,0x607D8B,0xe53935,0xD81B60,0x8E24AA,0x5E35B1,0x3949AB,0x1E88E5,0x039BE5,0x00ACC1,0x00897B,0x43A047,0x7CB342,0xC0CA33,0xFDD835,0xFFB300,0xFB8C00,0xF4511E,0x6D4C41,0x757575,0x546E7A,0xd32f2f,0xC2185B,0x7B1FA2,0x512DA8,0x303F9F,0x1976D2,0x0288D1,0x0097A7,0x00796B,0x388E3C,0x689F38,0xAFB42B,0xFBC02D,0xFFA000,0xF57C00,0xE64A19,0x5D4037,0x616161,0x455A64,0xc62828,0xAD1457,0x6A1B9A,0x4527A0,0x283593,0x1565C0,0x0277BD,0x00838F,0x00695C,0x2E7D32,0x558B2F,0x9E9D24,0xF9A825,0xFF8F00,0xEF6C00,0xD84315,0x4E342E,0x424242,0x37474F,0xb71c1c,0x880E4F,0x4A148C,0x311B92,0x1A237E,0x0D47A1,0x01579B,0x006064,0x004D40,0x1B5E20,0x33691E,0x827717,0xF57F17,0xFF6F00,0xE65100,0xBF360C,0x3E2723,0x212121,0x263238,0xff8a80,0xFF80AB,0xEA80FC,0xB388FF,0x8C9EFF,0x82B1FF,0x80D8FF,0x84FFFF,0xA7FFEB,0xB9F6CA,0xCCFF90,0xF4FF81,0xFFFF8D,0xFFE57F,0xFFD180,0xFF9E80,0xff5252,0xFF4081,0xE040FB,0x7C4DFF,0x536DFE,0x448AFF,0x40C4FF,0x18FFFF,0x64FFDA,0x69F0AE,0xB2FF59,0xEEFF41,0xFFFF00,0xFFD740,0xFFAB40,0xFF6E40,0xff1744,0xF50057,0xD500F9,0x651FFF,0x3D5AFE,0x2979FF,0x00B0FF,0x00E5FF,0x1DE9B6,0x00E676,0x76FF03,0xC6FF00,0xFFEA00,0xFFC400,0xFF9100,0xFF3D00,0xd50000,0xC51162,0xAA00FF,0x6200EA,0x304FFE,0x2962FF,0x0091EA,0x00B8D4,0x00BFA5,0x00C853,0x64DD17,0xAEEA00,0xFFD600,0xFFAB00,0xFF6D00,0xDD2C00
                // 254 total
        };
        int i = hash8(index) % material_design_colors.length;
        int rgb = material_design_colors[i];
        int bgr = Color.rgb(Color.blue(rgb), Color.green(rgb), Color.red(rgb));
        return 0xFF000000 | bgr;
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
