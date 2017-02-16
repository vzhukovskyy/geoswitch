package ua.pp.rudiki.geoswitch.trigger;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Objects;

// GPS fix is not a reliable source of geolocation info. It often provides false location.
// This class is for working around this issue, it uses history of location info and triggers
// only if several subsequent fixes are in the area or out of the area

public class SmoothingAreaTrigger {

    public final static int SMOOTHING_COUNT = 2;

    protected GeoArea area;

    protected transient ArrayDeque<Boolean> locationHistory; // inside=true, outside=false
    private int historySize;

    public SmoothingAreaTrigger(GeoArea area, int fixesCount) {
        this.area = area;

        historySize = 2*fixesCount;
        locationHistory = new ArrayDeque<Boolean>(historySize);
    }

    public SmoothingAreaTrigger(GeoPoint point, double radius, int fixesCount) {
        this(new GeoArea(point, radius), fixesCount);
    }

    public GeoArea getArea() {
        return area;
    }

    private boolean inTriggerArea(GeoPoint p) {
        return area.getCenter().distanceTo(p) < area.getRadius();
    }

    public TriggerType getType() {
        // this is base class, descendants must override this
        return TriggerType.Invalid;
    }

    public void changeLocation(GeoPoint point) {
        boolean inArea = inTriggerArea(point);
        locationHistory.add(inArea);
    }

    public void changeLocation(double latitude, double longitude) {
        GeoPoint point = new GeoPoint(latitude, longitude);
        changeLocation(point);
    }

    public boolean entered() {
        return historyMatchesPattern(false, true);
    }

    public boolean exited() {
        return historyMatchesPattern(true, false);
    }

    // returns true if location history matches pattern
    // [valueOfFirstHalf, .., valueOfFirstHalf, valueOfSecondHalf, .., valueOfSecondHalf]
    // ---t--->

    private boolean historyMatchesPattern(boolean valueOfOldestHalf, boolean valueOfNewestHalf) {
        if(locationHistory.size() != historySize)
            return false;

        // iterator order from head to tail (oldest to newest)
        Iterator<Boolean> iterator = locationHistory.iterator();
        int index = 0;
        while(iterator.hasNext() && index < historySize/2) {
            index++;
            if(iterator.next() != valueOfOldestHalf)
                return false;
        }
        while(iterator.hasNext()) {
            if(iterator.next() != valueOfNewestHalf)
                return false;
        }

        return true;
    }

    public boolean inside() {
        if(locationHistory.size() < historySize/2)
            return false;

        // iterator from newest to oldest
        Iterator<Boolean> iterator = locationHistory.descendingIterator();
        int index = 0;
        while(iterator.hasNext() && index < historySize/2) {
            index++;
            boolean inside = iterator.next();
            if(!inside)
                return false;
        }

        return true;
    }

    // Java methods override

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        if (getClass() != o.getClass())
            return false;

        SmoothingAreaTrigger trigger = (SmoothingAreaTrigger)o;

        return Objects.equals(area, trigger.getArea()) && historySize == trigger.historySize;
    }

}

