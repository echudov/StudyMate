package com.example.studymate.ui.floor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.studymate.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class FloorFragment extends Fragment implements OnMapReadyCallback {

    private FloorViewModel floorViewModel;
    private MapView mMapView;

    private GoogleMap mMap;
    private UiSettings mUiSettings;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        floorViewModel =
                ViewModelProviders.of(this).get(FloorViewModel.class);
        View root = inflater.inflate(R.layout.fragment_floor, container, false);

        mMapView = (MapView) root.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            System.out.println("Error with initializing map in current activity");
            e.printStackTrace();
        }

        mMapView.getMapAsync(this::onMapReady);

        return root;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // pull UiSettings object to hardcode some necessary settings/precautions
        // so users don't use too many features and break the app
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setMapToolbarEnabled(false);
        mUiSettings.setTiltGesturesEnabled(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setIndoorEnabled(true);
        // Add a marker at Grainger
        LatLng grainger = new LatLng(40.112485, -88.226841);
        mMap.addMarker(new MarkerOptions().position(grainger).title("Marker at Grainger"));

        mMap.setMinZoomPreference(17.0f);
        mMap.setMaxZoomPreference(27.0f);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(grainger));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(20.0f));
    }
}