package com.example.studymate.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.studymate.MainScreenActivity;
import com.example.studymate.R;
import com.example.studymate.ui.floor.FloorFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private MapViewModel mapViewModel;
    private MapView mMapView;

    private GoogleMap mMap;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mapViewModel =
                ViewModelProviders.of(this).get(MapViewModel.class);
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = root.findViewById(R.id.mapView);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setIndoorEnabled(true);

        // Add a marker in Sydney and move the camera
        LatLng grainger = new LatLng(40.112485, -88.226841);
        LatLng ugl = new LatLng(40.1047226,-88.2272146);

        Marker marker_at_grainger = mMap.addMarker(new MarkerOptions().position(grainger).title("Grainger Library"));
        Marker marker_at_ugl = mMap.addMarker(new MarkerOptions().position(ugl).title("Undergraduate Library"));
        mMap.setOnInfoWindowClickListener(marker -> {
            if (marker.equals(marker_at_grainger)) {
                selectLibrary("grainger", "2");
            } else if (marker.equals(marker_at_ugl)) {
                selectLibrary("ugl", "B1");
            }
        });
        mMap.moveCamera(CameraUpdateFactory.newLatLng(grainger));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f));
    }

    public void selectLibrary(String library, String floor) {
        MainScreenActivity activity = (MainScreenActivity) getActivity();
        activity.switchToFloor("grainger", 1, "map");
    }
}