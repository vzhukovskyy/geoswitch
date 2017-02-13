package ua.pp.rudiki.geoswitch.trigger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//*******************************************************************************
// 2 areas: A and B
// a point can reside in 1 of 4 zones:
//   outside of A and B, in A but not in B, in intersection of A and B, in B only
//
// 0 gps entries: 1 case
// 1 gps entry: 4 cases for each zone
// 2 gps entries: 16 cases from any zone to any zone (4*4)
//
// 21 test total
//*******************************************************************************

public class TransitionTriggerTest {

    private static GeoPoint mockedGeoPointA;
    private static GeoPoint mockedGeoPointB;
    private TransitionTrigger transitionTrigger;

    @BeforeClass
    public static void initMocks() {
        mockedGeoPointA = createMockedGeoPoint();
        mockedGeoPointB = createMockedGeoPoint();
    }

    private static GeoPoint createMockedGeoPoint() {
        // make GeoPoint calculating distances in plain 2D space so tests can verify the logic in Euclidean coordinates
        GeoPoint mockedGeoPoint = mock(GeoPoint.class);
        when(mockedGeoPoint.distanceTo(any(GeoPoint.class))).thenAnswer(new Answer<Double>() {
            @Override
            public Double answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                GeoPoint thismock = (GeoPoint)invocation.getMock();
                GeoPoint p = (GeoPoint)args[0];

                double distance = Math.sqrt(Math.pow(thismock.latitude - p.latitude, 2)+Math.pow(thismock.longitude - p.longitude, 2));

//                System.out.println("thismock latitude="+thismock.latitude+", longitude="+thismock.longitude);
//                System.out.println("p.latitude="+p.latitude+", longitude="+p.longitude);
                return distance;
            }
        });

        return mockedGeoPoint;
    }

    @Before
    public void initTest() {
        mockedGeoPointA.latitude = 10;
        mockedGeoPointA.longitude = 10;
        mockedGeoPointB.latitude = 10;
        mockedGeoPointB.longitude = 11;

        transitionTrigger = new TransitionTrigger(mockedGeoPointA, mockedGeoPointB);
    }

    @Test
    public void testEmpty() {
        assertFalse(transitionTrigger.isTriggered());
    }

    @Test
    public void testInitialOutside() {
        transitionTrigger.changeLocation(10,7);
        assertFalse(transitionTrigger.isTriggered());
    }

    @Test
    public void testInitialInsideA() {
        transitionTrigger.changeLocation(10,9.5);
        assertFalse(transitionTrigger.isTriggered());
    }

    @Test
    public void testInitialInsideAB() {
        transitionTrigger.changeLocation(10,10.5);
        assertFalse(transitionTrigger.isTriggered());
    }

    @Test
    public void testInitialInsideB() {
        transitionTrigger.changeLocation(10,11.5);
        assertFalse(transitionTrigger.isTriggered());
    }

    @Test
    public void testOut2Out() {
        transitionTrigger.changeLocation(10,7);
        transitionTrigger.changeLocation(10,6);
        assertFalse(transitionTrigger.isTriggered());
    }

    @Test
    public void testOut2A() {
        transitionTrigger.changeLocation(10,7);
        transitionTrigger.changeLocation(10,9.5);
        assertFalse(transitionTrigger.isTriggered());
    }

    @Test
    public void testOut2AB() {
        transitionTrigger.changeLocation(10,7);
        transitionTrigger.changeLocation(10,10.5);
        assertFalse(transitionTrigger.isTriggered());
    }

    @Test
    public void testOut2B() {
        transitionTrigger.changeLocation(10,7);
        transitionTrigger.changeLocation(10,11.5);
        assertFalse(transitionTrigger.isTriggered());
    }


    @Test
    public void testA2Out() {
        transitionTrigger.changeLocation(10,9.5);
        transitionTrigger.changeLocation(10,7);
        assertFalse(transitionTrigger.isTriggered());
    }

    @Test
    public void testA2A() {
        transitionTrigger.changeLocation(10,9.5);
        transitionTrigger.changeLocation(10,9.6);
        assertFalse(transitionTrigger.isTriggered());
    }

    @Test
    public void testA2AB() {
        transitionTrigger.changeLocation(10,9.5);
        transitionTrigger.changeLocation(10,10.5);
        assertFalse(transitionTrigger.isTriggered());
    }

    @Test
    public void testA2B() {
        transitionTrigger.changeLocation(10,9.5);
        transitionTrigger.changeLocation(10,11.5);
        assertTrue(transitionTrigger.isTriggered());
    }

    @Test
    public void testAB2Out() {
        transitionTrigger.changeLocation(10,10.5);
        transitionTrigger.changeLocation(10,13);
        assertFalse(transitionTrigger.isTriggered());
    }

    @Test
    public void testAB2A() {
        transitionTrigger.changeLocation(10,10.5);
        transitionTrigger.changeLocation(10,9.5);
        assertFalse(transitionTrigger.isTriggered());
    }

    @Test
    public void testAB2AB() {
        transitionTrigger.changeLocation(10,10.5);
        transitionTrigger.changeLocation(10,10.6);
        assertFalse(transitionTrigger.isTriggered());
    }

    @Test
    public void testAB2B() {
        transitionTrigger.changeLocation(10,10.5);
        transitionTrigger.changeLocation(10,11.5);
        assertTrue(transitionTrigger.isTriggered());
    }

    @Test
    public void testB2Out() {
        transitionTrigger.changeLocation(10,11.5);
        transitionTrigger.changeLocation(10,13);
        assertFalse(transitionTrigger.isTriggered());
    }

    @Test
    public void testB2A() {
        transitionTrigger.changeLocation(10,11.5);
        transitionTrigger.changeLocation(10,9.5);
        assertFalse(transitionTrigger.isTriggered());
    }

    @Test
    public void testB2AB() {
        transitionTrigger.changeLocation(10,11.5);
        transitionTrigger.changeLocation(10,10.5);
        assertFalse(transitionTrigger.isTriggered());
    }

    @Test
    public void testB2B() {
        transitionTrigger.changeLocation(10,11.5);
        transitionTrigger.changeLocation(10,11.6);
        assertFalse(transitionTrigger.isTriggered());
    }

}