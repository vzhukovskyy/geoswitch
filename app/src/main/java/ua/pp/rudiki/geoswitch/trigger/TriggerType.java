package ua.pp.rudiki.geoswitch.trigger;

import java.util.HashMap;
import java.util.Map;

public enum TriggerType {
    // values correspond to index in array strings.xml/trigger_types
    Bidirectional(0),
    Unidirectional(1);

    private int value;
    private static Map<Integer, TriggerType> map = new HashMap<>();

    TriggerType(int value) {
        this.value = value;
    }

    static {
        for (TriggerType pageType : TriggerType.values()) {
            map.put(pageType.value, pageType);
        }
    }

    public static TriggerType valueOf(int pageType) {
        return map.get(pageType);
    }

    public int getValue() {
        return value;
    }
}
