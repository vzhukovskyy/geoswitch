package ua.pp.rudiki.geoswitch.peripherals;

public class ConversionUtils {
    public static double toDouble(String value) {
        return toDouble(value, Double.NaN);
    }

    public static double toDouble(String value, double defaultValue) {
        Double d;
        try {
            d = Double.parseDouble(value);
        }
        catch(NumberFormatException e) {
            d = defaultValue;
        }

        return d;
    }
}
