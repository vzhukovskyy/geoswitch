package ua.pp.rudiki.geoswitch.kml.log;


import com.google.android.gms.maps.model.LatLng;

import java.util.Objects;

import ua.pp.rudiki.geoswitch.peripherals.HashBuilder;

public class AreaTriggerRecord extends TriggerRecord {
    public LatLng center;
    public double radius;

    @Override
    public boolean equals(Object object) {
        if(!super.equals(object))
            return false;

        AreaTriggerRecord otherAreaTriggerRecord = (AreaTriggerRecord)object;
        return radius == otherAreaTriggerRecord.radius &&
               Objects.equals(center, otherAreaTriggerRecord.center);
    }

    @Override
    public int hashCode() {
        return new HashBuilder()
                .combine(super.hashCode())
                .combine(center)
                .combine(radius)
                .build();
    }
}
