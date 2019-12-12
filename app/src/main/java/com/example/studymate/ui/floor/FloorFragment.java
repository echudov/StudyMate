package com.example.studymate.ui.floor;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.studymate.GeneralFunctions;
import com.example.studymate.R;
import com.example.studymate.SearchResultData;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FloorFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "FloorFragment";
    private FloorViewModel floorViewModel;
    private MapView mMapView;

    private boolean mapSynced;

    private GoogleMap mMap;
    private UiSettings mUiSettings;
    private TileOverlay tileOverlay;
    private Marker mostRecent = null;

    private HashMap<Integer, SearchResultData> users;

    private String studying;
    private String library = "grainger";
    private int floor = 1;
    private RadioGroup floorSelector;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mapSynced = false;
        floorViewModel =
                ViewModelProviders.of(this).get(FloorViewModel.class);
        View root = inflater.inflate(R.layout.fragment_floor, container, false);

        if (getArguments() != null) {
            library = getArguments().getString("library");
            floor = getArguments().getInt("floor");
        }
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
        mapSynced = true;

        floorSelector = root.findViewById(R.id.floorSelector);
        int id = getResources().getIdentifier("floorLevel" + floor, "id", getActivity().getPackageName());
        RadioButton selectedFloor = floorSelector.findViewById(id);
        selectedFloor.toggle();

        floorSelector.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            switch(checkedId) {
                case R.id.floorLevel0:
                    floor = 0;
                    break;
                case R.id.floorLevel1:
                    floor = 1;
                    break;
                case R.id.floorLevel2:
                    floor = 2;
                    break;
                case R.id.floorLevel3:
                    floor = 3;
                    break;
                case R.id.floorLevel4:
                    floor = 4;
                    break;
                default:
                    Log.v(TAG, "this isn't supposed to happen lol");
            }
            tileOverlay.remove();
            addTileOverlay(floor);
        });


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
        mUiSettings.setMapToolbarEnabled(true);
        mUiSettings.setTiltGesturesEnabled(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        mMap.setMinZoomPreference(3.5f);
        mMap.setMaxZoomPreference(4.0f);
        LatLngBounds graingerBounds = new LatLngBounds(new LatLng(90-11, -180 + 24), new LatLng(90, 0));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(graingerBounds.getCenter()));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(3.5f));
        mMap.setLatLngBoundsForCameraTarget(graingerBounds);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Subject");

                // Set up the input
                final EditText input = new EditText(getContext());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        studying = input.getText().toString();
                        mostRecent.remove();
                        addUserMarker(studying, latLng, googleMap);
                        // Send information about new marker to FireBase Database
                        SearchResultData toSend = new SearchResultData(mostRecent.getSnippet(),
                                GeneralFunctions.getEmail(getActivity()),
                                library,
                                floor,
                                mostRecent.getPosition().latitude,
                                mostRecent.getPosition().longitude);
                        GeneralFunctions.writeToDatabase("", toSend, "sitDown");
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });
        addTileOverlay(floor);


    }

    private void addUserMarker(String contentStudying, LatLng location, GoogleMap googleMap) {
        mostRecent = googleMap.addMarker(new MarkerOptions()
                .position(location)
                .title(GeneralFunctions.getEmail(getActivity()))
                .snippet(contentStudying));
    }

    private void addTileOverlay(int level) {
        // Logic for adding tile overlay
        TileProvider tileProvider = new TileProvider() {
            public Tile getTile(int x, int y, int zoom) {
                if (!checkTileExists(x, y, zoom)) {
                    return null;
                }
                String fileLocation;
                // this is not correct, figure out which file to pull from.
                fileLocation = "libraries" + "/" + library + "/floor" + level + "/" + zoom + "/" + x + "/" + y + ".png"; //need to add x and y
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

                return (zoom >= minZoom && zoom <= maxZoom);
            }

            public byte[] getBytesFromBitmap(Bitmap bitmap) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                return stream.toByteArray();
            }
        };

        tileOverlay = mMap.addTileOverlay(new TileOverlayOptions()
                .tileProvider(tileProvider));
    }
}