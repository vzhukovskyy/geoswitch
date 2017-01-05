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
import android.util.Log;

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

import ua.pp.rudiki.geoswitch.trigger.GeoArea;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private final String TAG = getClass().getSimpleName();

    private GoogleMap map;
    private Marker marker;
    private Circle circle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void onMapLongClick(LatLng point) {
        GeoArea area = GeoSwitchApp.getPreferences().loadArea();
        if(area != null) {
            area.setLatitude(point.latitude);
            area.setLongitude(point.longitude);
        }
        else {
            area = new GeoArea(point.latitude, point.longitude, GeoSwitchApp.getPreferences().getRadius());
        }

        updateMarkerPos(area);
        updateResult();
    }

    private void updateResult() {
        Intent resultData = new Intent();
        if(marker != null) {
            double latitude = marker.getPosition().latitude;
            double longitude = marker.getPosition().longitude;
            resultData.putExtra(Preferences.latitudeKey, latitude);
            resultData.putExtra(Preferences.longitudeKey, longitude);
            Log.e(TAG, "Prepared as a result ("+latitude+","+longitude+")");
        }
        setResult(Activity.RESULT_OK, resultData);
    }

    private void updateMarkerPos(GeoArea area) {
        if(marker != null){
            marker.remove();
        }
        if(circle != null) {
            circle.remove();
        }

        LatLng ll = new LatLng(area.getLatitude(), area.getLongitude());

        marker = map.addMarker(new MarkerOptions()
                .position(ll)
                .title("Area")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        int fillColor = Color.argb(50, 255, 255, 0);
        int strokeColor = Color.argb(255, 255, 255, 0);
        circle = map.addCircle(new CircleOptions()
                .center(ll)
                .radius(area.getRadius())
                .strokeColor(strokeColor)
                .strokeWidth(2)
                .fillColor(fillColor));
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setOnMapLongClickListener(this);
        map.setMyLocationEnabled(true);

        float zoomLevel = GeoSwitchApp.getPreferences().getDefaultMapZoomLevel();

        GeoArea area = GeoSwitchApp.getPreferences().loadArea();
        if(area != null) {
//            Log.i(TAG, "Map is jumping to area "+area);

            updateMarkerPos(area);

            LatLng ll = new LatLng(area.getLatitude(), area.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, zoomLevel));
        }
        else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
//                Log.i(TAG, "Map is jumping to current location "+ll);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, zoomLevel));
            }
            else {
//                Log.i(TAG, "Current location unknown. Map is not jumping ");
            }
        }

        //LatLngBounds bounds  = new LatLngBounds(area, latestPos);
        //bounds.extend(loc);
        //map.fitBounds(bounds);       // auto-zoom
        //map.panToBounds(bounds);     // auto-center


    }
}
