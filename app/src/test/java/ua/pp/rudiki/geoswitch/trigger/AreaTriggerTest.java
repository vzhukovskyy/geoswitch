package ua.pp.rudiki.geoswitch.trigger;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//***********************************************************
// a point can reside either inside the area, or outside
//
// 0 gps entries - 1 case
// 1 gps entry - 2 cases (inside, ouside)
// 2 gps entries - 4 cases (inside,outside)->(inside,outside)
//
// 7 tests total
//***********************************************************

@RunWith(MockitoJUnitRunner.class)
public class AreaTriggerTest {

    private static GeoPoint mockedGeoPoint;
    private AreaTrigger areaTrigger;

    @BeforeClass
    public static void initMocks() {
        // make GeoPoint calculating distances in plain 2D space so tests can verify the logic in Euclidean coordinates
        mockedGeoPoint = mock(GeoPoint.class);
        when(mockedGeoPoint.distanceTo(any(GeoPoint.class))).thenAnswer(new Answer<Double>() {
            @Override
            public Double answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                GeoPoint p = (GeoPoint)args[0];

                double distance = Math.sqrt(Math.pow(mockedGeoPoint.latitude - p.latitude, 2)+Math.pow(mockedGeoPoint.longitude - p.longitude, 2));
                return distance;
            }
        });
    }

    @Before
    public void setInitialPoint() {
        mockedGeoPoint.latitude = 10;
        mockedGeoPoint.longitude = 10;
        areaTrigger = new AreaTrigger(mockedGeoPoint, 1);
    }

    @Test
    public void testAreaEmpty() {
        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaInitiallyOut() {
        areaTrigger.changeLocation(10, 7);
        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaInitiallyIn() {
        areaTrigger.changeLocation(10, 10);
        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertTrue(areaTrigger.inside());
    }

    @Test
    public void testAreaOutToOut() {
        areaTrigger.changeLocation(10,7);
        areaTrigger.changeLocation(10,6);
        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaInToIn() {
        areaTrigger.changeLocation(10,10);
        areaTrigger.changeLocation(10,10.5);
        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertTrue(areaTrigger.inside());
    }

    @Test
    public void testAreaInToOut() {
        areaTrigger.changeLocation(10,10);
        areaTrigger.changeLocation(10,12);
        assertFalse(areaTrigger.entered());
        assertTrue(areaTrigger.exited());
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaOutToIn() {
        areaTrigger.changeLocation(10,8);
        areaTrigger.changeLocation(10,10);
        assertTrue(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertTrue(areaTrigger.inside());
    }
}
