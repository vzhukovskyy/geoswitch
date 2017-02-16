package ua.pp.rudiki.geoswitch.trigger;

public class EnterAreaTrigger extends SmoothingAreaTrigger implements GeoTrigger {

    public EnterAreaTrigger(GeoArea area) {
        super(area, SMOOTHING_COUNT);
    }

    public EnterAreaTrigger(GeoPoint point, double radius) {
        super(point, radius, SMOOTHING_COUNT);
    }

    @Override
    public TriggerType getType() { return TriggerType.EnterArea; }

    @Override
    public void changeLocation(double latitude, double longitude) {
        super.changeLocation(latitude, longitude);
    }

    @Override
    public boolean isTriggered() {
        return entered();
    }

    public String toString() {
        return "Enter " + area.toString();
    }

}

