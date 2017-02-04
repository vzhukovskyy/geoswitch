package ua.pp.rudiki.geoswitch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;

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
import com.google.maps.android.ui.BubbleIconFactory;
import com.google.maps.android.ui.IconGenerator;

import ua.pp.rudiki.geoswitch.peripherals.Preferences;
import ua.pp.rudiki.geoswitch.trigger.A2BTrigger;
import ua.pp.rudiki.geoswitch.trigger.GeoArea;
import ua.pp.rudiki.geoswitch.trigger.GeoPoint;
import ua.pp.rudiki.geoswitch.trigger.TriggerType;

public class ActivityMap extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private final String TAG = getClass().getSimpleName();

    IntentParameters params = new IntentParameters();
    AreaBuilder areaBuilder;

    private GoogleMap map;
    private IconGenerator iconFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        iconFactory = new IconGenerator(getApplicationContext());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.activity_map);
        mapFragment.getMapAsync(this);

        if(params.getTriggerType() == TriggerType.Bidirectional)
            areaBuilder = new SingleAreaBuilder();
        else
            areaBuilder = new DoubleAreaBuilder();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setOnMapLongClickListener(this);
        map.setMyLocationEnabled(true);

        float zoomLevel = GeoSwitchApp.getPreferences().getDefaultMapZoomLevel();

        GeoArea area = params.getArea();

        if (area != null) {
//            Log.d(TAG, "Map is jumping to area "+area);

            areaBuilder.newArea(area);
            areaBuilder.updateActivityResult();
            if (params.getTriggerType() == TriggerType.Unidirectional) {
                area = params.getAreaTo();
                areaBuilder.newArea(area);
                areaBuilder.updateActivityResult();
            }

            LatLng ll = new LatLng(area.getLatitude(), area.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, zoomLevel));
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
//                Log.d(TAG, "Map is jumping to current location "+ll);
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
        GeoArea area = new GeoArea(point.latitude, point.longitude, params.getArea().getRadius());
        areaBuilder.newArea(area);
        areaBuilder.updateActivityResult();
    }


    // area builders

    interface AreaBuilder {
        void newArea(GeoArea area);
        void updateActivityResult();
    }

    class SingleAreaBuilder implements AreaBuilder {
        public Marker marker;
        public Circle circle;

        @Override
        public void newArea(GeoArea area) {
            cleanArea();

            LatLng ll = new LatLng(area.getLatitude(), area.getLongitude());

            marker = map.addMarker(new MarkerOptions()
                    .position(ll)
                    .icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("Area"))));

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

        public void updateActivityResult() {
            Intent resultData = new Intent();

            if (marker != null) {
                double latitude = marker.getPosition().latitude;
                double longitude = marker.getPosition().longitude;
                double radius = circle.getRadius();
                resultData.putExtra(Preferences.latitudeKey, String.valueOf(latitude));
                resultData.putExtra(Preferences.longitudeKey, String.valueOf(longitude));
                resultData.putExtra(Preferences.radiusKey, String.valueOf(radius));

                Log.d(TAG, "Prepared result (" + latitude + "," + longitude + "), R="+radius);
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

                double radius = A2BTrigger.calculateRadius(
                        circle1.getCenter().latitude, circle1.getCenter().longitude,
                        circle2.getCenter().latitude, circle2.getCenter().longitude);
                circle1.setRadius(radius);
                circle2.setRadius(radius);

            }
        }

        private void colorMarker(Marker marker, Circle circle, boolean isFrom) {
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(isFrom ? "From" : "To")));

            int fillColor = isFrom ? Color.argb(50, 255, 255, 0) : Color.argb(50, 255, 127, 127);
            int strokeColor = isFrom ? Color.argb(255, 255, 255, 0) : Color.argb(255, 255, 127, 127);
            circle.setStrokeColor(strokeColor);
            circle.setFillColor(fillColor);
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

                Log.d(TAG, "Prepared result from=(" + latitude + "," + longitude + "), radius="+radius);
            }

            if (marker2 != null) {
                double latitude = marker2.getPosition().latitude;
                double longitude = marker2.getPosition().longitude;
                resultData.putExtra(Preferences.latitudeToKey, String.valueOf(latitude));
                resultData.putExtra(Preferences.longitudeToKey, String.valueOf(longitude));

                Log.d(TAG, "Prepared result to=(" + latitude + "," + longitude + ")");
            }

            setResult(Activity.RESULT_OK, resultData);
        }
    }


    // intent parameters

    class IntentParameters {

        TriggerType getTriggerType() {
            String name = getIntent().getStringExtra(Preferences.triggerTypeKey);
            return TriggerType.valueOf(name);
        }

        GeoArea getArea() {
            double latitude = getDouble(Preferences.latitudeKey);
            double longitude = getDouble(Preferences.longitudeKey);
            double radius = getDouble(Preferences.radiusKey);

            GeoArea area = null;
            if (latitude != Double.NaN && longitude != Double.NaN && radius != Double.NaN) {
                area = new GeoArea(latitude, longitude, radius);
            }

            return area;
        }

        GeoArea getAreaTo() {
            double latitudeTo = getDouble(Preferences.latitudeToKey);
            double longitudeTo = getDouble(Preferences.longitudeToKey);
            double radiusTo = getDouble(Preferences.radiusKey);

            GeoArea area = null;
            if (latitudeTo != Double.NaN && longitudeTo != Double.NaN && radiusTo != Double.NaN) {
                area = new GeoArea(latitudeTo, longitudeTo, radiusTo);
            }

            return area;
        }

        private double getDouble(String key) {
            String value = getIntent().getStringExtra(key);
            double d = Double.NaN;
            try {
                d = Double.parseDouble(value);
            } catch (NumberFormatException e) {
            }

            return d;
        }
    }
}
