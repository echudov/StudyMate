package com.example.studymate.ui.floor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.studymate.GeneralFunctions;
import com.example.studymate.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FloorFragment extends Fragment implements OnMapReadyCallback {

    private FloorViewModel floorViewModel;
    private MapView mMapView;

    private boolean mapSynced;

    private GoogleMap mMap;
    private UiSettings mUiSettings;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mapSynced = false;
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
        mapSynced = true;


        return root;
    }
e.printStackTrace();


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
        mUiSettings.setMapToolbarEnabled(true);
        mUiSettings.setTiltGesturesEnabled(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        // Add a marker at Grainger
        LatLng grainger = new LatLng(40.112485, -88.226841);
        mMap.addMarker(new MarkerOptions().position(grainger).title("Marker at Grainger"));

        mMap.setMinZoomPreference(3.5f);
        mMap.setMaxZoomPreference(4.0f);
        LatLngBounds grainger2 = new LatLngBounds(new LatLng(90-9, -180 + 18), new LatLng(90, 0));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(grainger2.getCenter()));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(3.5f));
        mMap.setLatLngBoundsForCameraTarget(grainger2);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // addUserMarker(GeneralFunctions.getProfilePic(getActivity()), latLng, googleMap);
                // Send information about new marker to FireBase Database

                JsonObject data = new JsonObject();
                JsonObject location = new JsonObject();
                location.addProperty("Longitude", latLng.longitude);
                location.addProperty("Latitude", latLng.latitude);
                data.add("Location", location);
                data.addProperty("Email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
                GeneralFunctions.writeToDatabase("", data);


            }
        });


        // Logic for adding tile overlay
        TileProvider tileProvider = new TileProvider() {
            public Tile getTile(int x, int y, int zoom) {
                if (!checkTileExists(x, y, zoom)) {
                    return null;
                }
                String fileLocation;
                // this is not correct, figure out which file to pull from.
                fileLocation = "libraries" + "/grainger" + "/floor" + 2 + "/" + zoom + "/" + x + "/" + y + ".png"; //need to add x and y
                InputStream inputStream = null;
                try {
                    inputStream = getContext().getAssets().open(fileLocation);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                byte[] data = getBytesFromBitmap(bitmap);
                return new Tile(256, 256, data);
            }
            /*
             * Check that the tile server supports the requested x, y and zoom.
             * Complete this stub according to the tile range you support.
             * If you support a limited range of tiles at different zoom levels, then you
             * need to define the supported x, y range at each zoom level.
             */
            private boolean checkTileExists(int x, int y, int zoom) {
                int minZoom = 0;
                int maxZoom = 4;

                if ((zoom < minZoom || zoom > maxZoom)) {
                    return false;
                }

                return true;
            }

            public byte[] getBytesFromBitmap(Bitmap bitmap) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                return stream.toByteArray();
            }
        };

        TileOverlay tileOverlay = mMap.addTileOverlay(new TileOverlayOptions()
                .tileProvider(tileProvider));
    }

    private void addUserMarker(Bitmap picture, LatLng location, GoogleMap googleMap) {
        BitmapDescriptor icon = BitmapDescriptorFactory
                .fromBitmap(picture);

        googleMap.addMarker(new MarkerOptions()
                .position(location)
                .title("Your marker title")
                .snippet("Your marker snippet").icon(icon));
    }

}