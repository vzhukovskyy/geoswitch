package ua.pp.rudiki.geoswitch.peripherals;


public class HashUtils {
    public static int combineHashCode(int hashCode, Object o) {
        return 37*hashCode + o.hashCode();
    }

    public static int combineHashCode(int hashCode, double d) {
        long l = Double.doubleToLongBits(d);
        int i = (int)(l ^ (l >>> 32));
        return 37*hashCode + i;
    }

}
