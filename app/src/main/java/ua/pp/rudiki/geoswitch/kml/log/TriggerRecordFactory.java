package ua.pp.rudiki.geoswitch.kml.log;


public class TriggerRecordFactory {
    public static TriggerRecord createTriggerRecord(String triggerTag) {
        if(triggerTag.equals("Exit") || triggerTag.equals("Enter"))
            return new AreaTriggerRecord();
        if(triggerTag.equals("Transition"))
            return new TransitionTriggerRecord();

        return null;
    }
}
