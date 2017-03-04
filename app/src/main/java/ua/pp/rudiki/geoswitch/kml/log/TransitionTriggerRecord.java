package ua.pp.rudiki.geoswitch.kml.log;


import com.google.android.gms.maps.model.LatLng;

import java.util.Objects;

import ua.pp.rudiki.geoswitch.peripherals.HashBuilder;

public class TransitionTriggerRecord  extends TriggerRecord {
    public LatLng from;
    public LatLng to;
    public double radius;

    @Override
    public boolean equals(Object object) {
        if(!super.equals(object))
            return false;

        TransitionTriggerRecord otherTransitionTriggerRecord = (TransitionTriggerRecord)object;
        return radius == otherTransitionTriggerRecord.radius &&
               Objects.equals(from, otherTransitionTriggerRecord.from) &&
               Objects.equals(to, otherTransitionTriggerRecord.to);
    }

    @Override
    public int hashCode() {
        return new HashBuilder()
            .combine(super.hashCode())
            .combine(from)
            .combine(to)
            .combine(radius)
            .build();
    }

}
