package ua.pp.rudiki.geoswitch.trigger;

public class ExitAreaTrigger extends AreaTrigger implements GeoTrigger {

    public ExitAreaTrigger(GeoArea area) {
        super(area);
    }

    public ExitAreaTrigger(GeoPoint point, double radius) {
        super(point, radius);
    }

    @Override
    public TriggerType getType() { return TriggerType.ExitArea; }

    @Override
    public void changeLocation(double latitude, double longitude) {
        super.changeLocation(latitude, longitude);
    }

    @Override
    public boolean isTriggered() {
        return exited();
    }

    public String toString() {
        return "Exit " + area.toString();
    }

}

