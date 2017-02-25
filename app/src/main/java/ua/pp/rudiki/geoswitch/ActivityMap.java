package ua.pp.rudiki.geoswitch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import ua.pp.rudiki.geoswitch.peripherals.Preferences;
import ua.pp.rudiki.geoswitch.trigger.TransitionTrigger;
import ua.pp.rudiki.geoswitch.trigger.GeoArea;
import ua.pp.rudiki.geoswitch.trigger.TriggerType;

public class ActivityMap extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private static final String TAG = ActivityMap.class.getSimpleName();

    MapIntentParametersParser params = new MapIntentParametersParser();
    AreaBuilder areaBuilder;

    private GoogleMap map;
    private IconGenerator iconFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.getLogger().debug(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        iconFactory = new IconGenerator(getApplicationContext());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.activity_map);
        mapFragment.getMapAsync(this);

        switch(params.getTriggerType()) {
            case EnterArea:
            case ExitArea:
            default:
                areaBuilder = new SingleAreaBuilder();
                break;
            case Transition:
                areaBuilder = new DoubleAreaBuilder();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        App.getLogger().debug(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        App.getLogger().debug(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        App.getLogger().debug(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        App.getLogger().debug(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.getLogger().debug(TAG, "onDestroy");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setOnMapLongClickListener(this);
        try {
            map.setMyLocationEnabled(true);
        } catch(SecurityException e) {
            // ignore
        }

        float zoomLevel = App.getPreferences().getDefaultMapZoomLevel();

        GeoArea area = params.getArea();
        if (area != null) {
            areaBuilder.readIntentParameters(params);

            LatLng ll = new LatLng(area.getLatitude(), area.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, zoomLevel));
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            Location location = null;
            try {
                location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            } catch (SecurityException e) {
                // ignore
            }
            if (location != null) {
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, zoomLevel));
            }
        }

        //LatLngBounds bounds  = new LatLngBounds(area, latestPos);
        //bounds.extend(loc);
        //map.fitBounds(bounds);       // auto-zoom
        //map.panToBounds(bounds);     // auto-center
    }

    // UI callbacks

    public void onMapLongClick(LatLng point) {
        GeoArea area = new GeoArea(point.latitude, point.longitude, params.getRadius());
        areaBuilder.newArea(area);
        areaBuilder.updateActivityResult();
    }


    // area builders

    interface AreaBuilder {
        void newArea(GeoArea area);
        void readIntentParameters(MapIntentParametersParser params);
        void updateActivityResult();
    }

    class SingleAreaBuilder implements AreaBuilder {
        public Marker marker;
        public Circle circle;

        @Override
        public void newArea(GeoArea area) {
            cleanArea();

            LatLng ll = new LatLng(area.getLatitude(), area.getLongitude());
            String areaTag = getString(R.string.activity_map_area);

            marker = map.addMarker(new MarkerOptions()
                    .position(ll)
                    .icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(areaTag))));

            int fillColor = Color.argb(50, 255, 255, 0);
            int strokeColor = Color.argb(255, 255, 255, 0);
            circle = map.addCircle(new CircleOptions()
                    .center(ll)
                    .radius(area.getRadius())
                    .strokeColor(strokeColor)
                    .strokeWidth(2)
                    .fillColor(fillColor));
        }

        private void cleanArea() {
            if (marker != null) {
                marker.remove();
            }
            if (circle != null) {
                circle.remove();
            }
        }

        public void readIntentParameters(MapIntentParametersParser params) {
            GeoArea area = params.getArea();
            if (area != null) {
                newArea(area);
//                updateActivityResult();
            }
        }

        public void updateActivityResult() {
            Intent resultData = new Intent();

            if (marker != null) {
                double latitude = marker.getPosition().latitude;
                double longitude = marker.getPosition().longitude;
                double radius = circle.getRadius();
                resultData.putExtra(Preferences.latitudeKey, String.valueOf(latitude));
                resultData.putExtra(Preferences.longitudeKey, String.valueOf(longitude));
                resultData.putExtra(Preferences.radiusKey, String.valueOf(radius));
            }

            setResult(Activity.RESULT_OK, resultData);
        }
    }

    class DoubleAreaBuilder implements AreaBuilder {
        public Marker marker1, marker2;
        public Circle circle1, circle2;

        @Override
        public void newArea(GeoArea area) {
            LatLng ll = new LatLng(area.getLatitude(), area.getLongitude());

            Marker marker = map.addMarker(new MarkerOptions().position(ll));
            Circle circle = map.addCircle(new CircleOptions()
                    .center(ll)
                    .strokeWidth(2));

            if(marker1 == null) {
                marker1 = marker;
                circle1 = circle;
                colorMarker(marker1, circle1, true);

                circle1.setRadius(area.getRadius());
            }
            else {
                if(marker2 == null) {
                    marker2 = marker;
                    circle2 = circle;
                    colorMarker(marker2, circle2, false);
                }
                else {
                    marker1.remove();
                    circle1.remove();
                    marker1 = marker2;
                    circle1 = circle2;
                    marker2 = marker;
                    circle2 = circle;
                    colorMarker(marker1, circle1, true);
                    colorMarker(marker2, circle2, false);
                }

                double radius = TransitionTrigger.calculateRadius(
                        circle1.getCenter().latitude, circle1.getCenter().longitude,
                        circle2.getCenter().latitude, circle2.getCenter().longitude);
                circle1.setRadius(radius);
                circle2.setRadius(radius);

            }
        }

        private void colorMarker(Marker marker, Circle circle, boolean isFrom) {
            String tag = getString(isFrom ? R.string.activity_map_from : R.string.activity_map_to);
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(tag)));

            int fillColor = isFrom ? Color.argb(50, 255, 255, 0) : Color.argb(50, 255, 127, 127);
            int strokeColor = isFrom ? Color.argb(255, 255, 255, 0) : Color.argb(255, 255, 127, 127);
            circle.setStrokeColor(strokeColor);
            circle.setFillColor(fillColor);
        }

        public void readIntentParameters(MapIntentParametersParser params) {
            GeoArea area = params.getArea();
            if (area != null) {
                newArea(area);
//                updateActivityResult();
                area = params.getAreaTo();
                if (area != null) {
                    newArea(area);
//                    updateActivityResult();
                }
            }
        }

        public void updateActivityResult() {
            Intent resultData = new Intent();

            if (marker1 != null) {
                double latitude = marker1.getPosition().latitude;
                double longitude = marker1.getPosition().longitude;
                double radius = circle1.getRadius();
                resultData.putExtra(Preferences.latitudeKey, String.valueOf(latitude));
                resultData.putExtra(Preferences.longitudeKey, String.valueOf(longitude));
                resultData.putExtra(Preferences.radiusKey, String.valueOf(radius));
            }

            if(marker2 != null) {
                double latitude = marker2.getPosition().latitude;
                double longitude = marker2.getPosition().longitude;
                resultData.putExtra(Preferences.latitudeToKey, String.valueOf(latitude));
                resultData.putExtra(Preferences.longitudeToKey, String.valueOf(longitude));
            }

            setResult(Activity.RESULT_OK, resultData);
        }
    }


    // intent parameters

    class MapIntentParametersParser {

        TriggerType getTriggerType() {
            String name = getIntent().getStringExtra(Preferences.triggerTypeKey);
            return TriggerType.valueOf(name);
        }

        GeoArea getArea() {
            return getArea(Preferences.latitudeKey, Preferences.longitudeKey, Preferences.radiusKey);
        }

        GeoArea getAreaTo() {
            return getArea(Preferences.latitudeToKey, Preferences.longitudeToKey, Preferences.radiusKey);
        }

        private GeoArea getArea(String latitudeKey, String longitudeKey, String radiusKey) {
            double latitudeTo = getIntent().getDoubleExtra(latitudeKey, Double.NaN);
            double longitudeTo = getIntent().getDoubleExtra(longitudeKey, Double.NaN);
            double radiusTo = getIntent().getDoubleExtra(radiusKey, Double.NaN);

            GeoArea area = null;
            if(!Double.isNaN(latitudeTo) && !Double.isNaN(latitudeTo) && !Double.isNaN(latitudeTo))
                area = new GeoArea(latitudeTo, longitudeTo, radiusTo);

            return area;
        }

        double getRadius() {
            return getIntent().getDoubleExtra(Preferences.radiusKey, Double.NaN);
        }
    }
}
