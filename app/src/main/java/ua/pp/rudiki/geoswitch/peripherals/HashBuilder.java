package ua.pp.rudiki.geoswitch.peripherals;


public class HashBuilder {
    int hash = 1;

    public HashBuilder combine(Object o) {
        hash = 37*hash + o.hashCode();
        return this;
    }

    public HashBuilder combine(double d) {
        long l = Double.doubleToLongBits(d);
        int i = (int)(l ^ (l >>> 32));
        hash = 37*hash + i;
        return this;
    }

    public int build() {
        return hash;
    }
}
