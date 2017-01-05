package ua.pp.rudiki.geoswitch.trigger;

public class AreaTrigger {

    private GeoArea area;
    private boolean inAreaAtThisTick = false;
    private boolean inAreaAtPreviousTick = false;

    public AreaTrigger(GeoArea area) {
        this.area = area;
    }

    public AreaTrigger(GeoPoint point, double radius) {
        this.area = new GeoArea(point, radius);
    }

    public GeoArea getArea() {
        return area;
    }

    private boolean inTriggerArea(GeoPoint p) {
        return area.getCenter().distanceTo(p) < area.getRadius();
    }

    public void changeLocation(double latitude, double longitude) {
        changeLocation(new GeoPoint(latitude, longitude));
    }

    public void changeLocation(GeoPoint p) {
        inAreaAtPreviousTick = inAreaAtThisTick;
        inAreaAtThisTick = inTriggerArea(p);
    }

    public boolean entered() {
        return inAreaAtThisTick && !inAreaAtPreviousTick;
    }

    public boolean exited() {
        return !inAreaAtThisTick && inAreaAtPreviousTick;
    }

    public boolean inside() {
        return inAreaAtThisTick;
    }
}

