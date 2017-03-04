package ua.pp.rudiki.geoswitch.kml.log;

import java.util.Date;

// Base class for Trigger records
public class TriggerRecord {
    public transient Date date;

    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null)
            return false;
        if (!object.getClass().equals(this.getClass()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
