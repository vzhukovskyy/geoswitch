package ua.pp.rudiki.geoswitch.trigger;


import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//***********************************************************
// Tests SmoothingAreaTrigger with fixesCount = 2
// a point can reside either inside the area, or outside
//
// 0 gps entries - 1 case
// 1 gps entry - 2 cases (inside, ouside)
// 2 gps entries - 4 cases (inside,outside)->(inside,outside)
// 3 gps entries - 2^3=8
// 4 gps entries - 2^4=16
//
// 31 tests total
//***********************************************************

@RunWith(MockitoJUnitRunner.class)
public class SmoothingAreaTriggerTest {

    private static GeoPoint mockedGeoPoint;
    private SmoothingAreaTrigger areaTrigger;

    private static GeoPoint areaCenter = new GeoPoint(10, 10);
    private static int areaRadius = 2;

    private static GeoPoint insideEast = new GeoPoint(9, 10);
    private static GeoPoint insideNorth = new GeoPoint(10, 11);
    private static GeoPoint insideWest = new GeoPoint(11, 10);
    private static GeoPoint insideSouth = new GeoPoint(10, 9);

    private static GeoPoint outsideEast = new GeoPoint(7, 10);
    private static GeoPoint outsideNorth = new GeoPoint(10, 13);
    private static GeoPoint outsideWest = new GeoPoint(13, 10);
    private static GeoPoint outsideSouth = new GeoPoint(10, 7);

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
        mockedGeoPoint.latitude = areaCenter.latitude;
        mockedGeoPoint.longitude = areaCenter.longitude;
        GeoArea area = new GeoArea(mockedGeoPoint, areaRadius);
        areaTrigger = new SmoothingAreaTrigger(area, 2 /*fixesCount*/);
    }

    // 0 gps entries - 1 case

    @Test
    public void testAreaEmpty() {
        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertFalse(areaTrigger.inside());
    }

    // 1 gps entry - 2 cases (inside, ouside)

    @Test
    public void testAreaInitiallyOut() {
        genericTest1(outsideEast);
    }

    @Test
    public void testAreaInitiallyIn() {
        genericTest1(insideNorth);
    }

    private void genericTest1(GeoPoint point) {
        areaTrigger.changeLocation(point);
        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertFalse(areaTrigger.inside());
    }

    // 2 gps entries - 4 cases (inside,outside)->(inside,outside)

    @Test
    public void testAreaOutToOut() {
        genericTest2(outsideEast, outsideNorth);
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaInToIn() {
        genericTest2(insideEast, insideNorth);
        assertTrue(areaTrigger.inside());
    }

    @Test
    public void testAreaInToOut() {
        genericTest2(insideEast, outsideEast);
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaOutToIn() {
        genericTest2(outsideEast, insideEast);
        assertFalse(areaTrigger.inside());
    }

    private void genericTest2(GeoPoint point1, GeoPoint point2) {
        areaTrigger.changeLocation(point1);
        areaTrigger.changeLocation(point2);
        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
    }

    // 3 gps entries - 2^3=8

    @Test
    public void testAreaOOO() {
        genericTest3(outsideEast, outsideNorth, outsideWest);
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaOOI() {
        genericTest3(outsideEast, outsideNorth, insideNorth);
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaOIO() {
        genericTest3(outsideEast, insideNorth, outsideNorth);
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaOII() {
        genericTest3(outsideEast, insideNorth, insideSouth);
        assertTrue(areaTrigger.inside());
    }

    @Test
    public void testAreaIOO() {
        genericTest3(insideNorth, outsideNorth, outsideEast);
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaIOI() {
        genericTest3(insideNorth, outsideNorth, insideSouth);
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaIIO() {
        genericTest3(insideNorth, insideSouth, outsideNorth);
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaIII() {
        genericTest3(insideNorth, insideEast, insideSouth);
        assertTrue(areaTrigger.inside());
    }

    private void genericTest3(GeoPoint point1, GeoPoint point2, GeoPoint point3) {
        areaTrigger.changeLocation(point1);
        areaTrigger.changeLocation(point2);
        areaTrigger.changeLocation(point3);
        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
    }

    // 4 gps entries - 2^4=16

    @Test
    public void testAreaOOOO() {
        setup4(outsideEast, outsideNorth, outsideWest, outsideSouth);

        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaOOOI() {
        setup4(outsideEast, outsideNorth, outsideWest, insideWest);

        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaOOIO() {
        setup4(outsideEast, outsideNorth, insideNorth, outsideNorth);

        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaOOII() {
        setup4(outsideEast, outsideNorth, insideNorth, insideEast);

        assertTrue(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertTrue(areaTrigger.inside());
    }

    @Test
    public void testAreaOIOO() {
        setup4(outsideEast, insideEast, outsideNorth, outsideWest);

        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaOIOI() {
        setup4(outsideEast, insideEast, outsideNorth, insideNorth);

        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaOIIO() {
        setup4(outsideEast, insideEast, insideNorth, outsideNorth);

        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaOIII() {
        setup4(outsideEast, insideEast, insideNorth, insideWest);

        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertTrue(areaTrigger.inside());
    }

    @Test
    public void testAreaIOOO() {
        setup4(insideEast, outsideNorth, outsideWest, outsideSouth);

        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaIOOI() {
        setup4(insideEast, outsideNorth, outsideWest, insideWest);

        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaIOIO() {
        setup4(insideEast, outsideNorth, insideWest, outsideWest);

        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaIOII() {
        setup4(insideEast, outsideNorth, insideWest, insideSouth);

        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertTrue(areaTrigger.inside());
    }

    @Test
    public void testAreaIIOO() {
        setup4(insideEast, insideSouth, outsideWest, outsideNorth);

        assertFalse(areaTrigger.entered());
        assertTrue(areaTrigger.exited());
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaIIOI() {
        setup4(insideEast, insideSouth, outsideWest, insideWest);

        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaIIIO() {
        setup4(insideEast, insideSouth, insideWest, outsideNorth);

        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertFalse(areaTrigger.inside());
    }

    @Test
    public void testAreaIIII() {
        setup4(insideEast, insideSouth, insideWest, insideNorth);

        assertFalse(areaTrigger.entered());
        assertFalse(areaTrigger.exited());
        assertTrue(areaTrigger.inside());
    }

    private void setup4(GeoPoint point1, GeoPoint point2, GeoPoint point3, GeoPoint point4) {
        areaTrigger.changeLocation(point1);
        areaTrigger.changeLocation(point2);
        areaTrigger.changeLocation(point3);
        areaTrigger.changeLocation(point4);
    }


}
