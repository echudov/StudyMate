package com.example.studymate.ui.floor;

import android.app.Activity;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FloorFragment extends Fragment implements OnMapReadyCallback {

    /** Tag for logging purposes. */
    private static final String TAG = "FloorFragment";
    /** Instance of FloorViewModel to access live data. */
    private FloorViewModel floorViewModel;
    /** MapView fragment within floor view. */
    private MapView mMapView;

    /** Keeps track of whether the map has synced or not. */
    private boolean mapSynced;
    /** Local map element. */
    private GoogleMap mMap;
    /** UI Settings for mMap. */
    private UiSettings mUiSettings;
    /** TileOverlay to place on top of mMap. */
    private TileOverlay tileOverlay;
    /** Most recent marker placed by the user (used to keep track of & delete past user marker. */
    private Marker mostRecent = null;

    /** Information about the users as a map between their email hashcode and their SearchResultData info object. */
    private HashMap<Integer, SearchResultData> users;
    /** List of currently drawn markers on the floor. */
    private List<Marker> markersOnMap;

    /** String for what the user is studying. */
    private String studying;
    /** Library currently selected
     *  Currently only grainger, but that can change.
     */
    private String library = "grainger";
    /** Floor that the view has selected */
    private int floor = 1;
    /** Button group for floor selection */
    private RadioGroup floorSelector;

    /**
     * Creates the floor view every time it is selected in navigation.
     * @param inflater necessary variable to create the root element to inflate.
     * @param container container for this view.
     * @param savedInstanceState passes data between past states.
     * @return the inflated view
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mapSynced = false;
        // inflate the view at the start
        floorViewModel =
                ViewModelProviders.of(this).get(FloorViewModel.class);
        View root = inflater.inflate(R.layout.fragment_floor, container, false);

        // check to see if any fragment (search or map) passed any values containing info about what to draw
        // if null that typically means that it was selected using the bottom nav view menu
        if (getArguments() != null) {
            library = getArguments().getString("library");
            floor = getArguments().getInt("floor");
        }
        // creates mapView w info about where it was before
        mMapView = root.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        // try to initialize map, throw exception otherwise
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            System.out.println("Error with initializing map in current activity");
            e.printStackTrace();
        }

        // Sync map
        mMapView.getMapAsync(this::onMapReady);
        mapSynced = true;

        // initialize empty container for users
        users = new HashMap<Integer, SearchResultData>();

        // RadioButton logic for floor selection
        RadioGroup floorSelector = root.findViewById(R.id.floorSelector);
        int id = getResources().getIdentifier("floorLevel" + floor, "id", getActivity().getPackageName());
        RadioButton selectedFloor = floorSelector.findViewById(id);
        selectedFloor.toggle();

        // checks which button is currently selected and changes the overlaid floor
        floorSelector.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            switch(checkedId) {
                case R.id.floorLevel0:
                    floor = 0;
                    addAllMarkers(mMap, markersOnMap, users, getActivity(), floor);
                    break;
                case R.id.floorLevel1:
                    floor = 1;
                    addAllMarkers(mMap, markersOnMap, users, getActivity(), floor);
                    break;
                case R.id.floorLevel2:
                    floor = 2;
                    addAllMarkers(mMap, markersOnMap, users, getActivity(), floor);
                    break;
                case R.id.floorLevel3:
                    floor = 3;
                    addAllMarkers(mMap, markersOnMap, users, getActivity(), floor);
                    break;
                case R.id.floorLevel4:
                    floor = 4;
                    addAllMarkers(mMap, markersOnMap, users, getActivity(), floor);
                    break;
                default:
                    Log.v(TAG, "this isn't supposed to happen lol");
                    addAllMarkers(mMap, markersOnMap, users, getActivity(), floor);
            }
            tileOverlay.remove();
            addTileOverlay(floor);
        });


        return root;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     * Creates the logic for overlaying tiles
     */
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // pull UiSettings object to hardcode some necessary settings/precautions
        // so users don't use too many features and break the app
        mUiSettings = mMap.getUiSettings();
        mUiSettings.setMapToolbarEnabled(true);
        mUiSettings.setTiltGesturesEnabled(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        // set zoom range to always look at the library
        mMap.setMinZoomPreference(3.5f);
        mMap.setMaxZoomPreference(4.0f);

        // bounds for grainger as a LatLngBounds object
        LatLngBounds graingerBounds = new LatLngBounds(new LatLng(90-11, -180 + 24), new LatLng(90, 0));
        // Camera movement and adjustment
        mMap.moveCamera(CameraUpdateFactory.newLatLng(graingerBounds.getCenter()));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(3.5f));
        mMap.setLatLngBoundsForCameraTarget(graingerBounds);
        // initializing the map with markers of each user
        markersOnMap = new ArrayList<Marker>();
        initializeMap(users, TAG, mMap, markersOnMap, getActivity(), floor);
        listenForUserChanges(users, TAG, mMap, markersOnMap, getActivity(), floor);

        // create new user marker logic
        // creates an AlertDialog to prompt the user for their subject
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
                        if (mostRecent != null) {
                            mostRecent.remove();
                        }

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

    /**
     * Adds user marker onto the map (might refactor so it returns the marker for ease of use
     * @param contentStudying content the user is studying (for the snippet)
     * @param location where to place the marker
     * @param googleMap google map to place it on
     */
    private void addUserMarker(String contentStudying, LatLng location, GoogleMap googleMap) {
        mostRecent = googleMap.addMarker(new MarkerOptions()
                .position(location)
                .title(GeneralFunctions.getEmail(getActivity()))
                .snippet(contentStudying));
    }

    /**
     * This helper function will go through all users in the Map
     * and add markers for each one that is in the current
     * library and floor
     */
    private static void addAllMarkers(GoogleMap mMap, List<Marker> markersOnMap, Map<Integer, SearchResultData> users,
                                      Activity currentActivity, int floor) {
        // First clear all current markers from map & List
        for (Marker m : markersOnMap) {
            m.remove();
        }
        markersOnMap.clear();
        // Go through all users
        for(SearchResultData currentUser : users.values()) {
            // Check if in correct library & correct floor
            if (currentUser.getLibrary().equals("grainger") && currentUser.getFloor() == floor) {
                // Add marker
                MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(currentUser.getSeatingLatitude(), currentUser.getSeatingLongitude()))
                    .title(GeneralFunctions.getEmail(currentActivity))
                    .snippet(currentUser.getStudyingContent());
                Marker justAdded = mMap.addMarker(markerOptions);
                // Add this marker to list
                markersOnMap.add(justAdded);
            }
        }
    }


    /**
     * Overlay tiles onto the map from the local storage
     * Requires that the library floor plans to be stored on each device
     * Might not be the most efficient storage/security wise, but it's the simplest
     * Implementation I know how to do
     * @param level what floor to overlay onto the map
     */
    private void addTileOverlay(int level) {
        // Logic for adding tile overlay
        TileProvider tileProvider = new TileProvider() {
            public Tile getTile(int x, int y, int zoom) {
                if (!checkTileExists(x, y, zoom)) {
                    return null;
                }
                String fileLocation;
                // File location within assets folder
                // All library files must follow this organizational structure
                // Uses MapTiler to create each individual tile
                // To make it more general, we could parse from the info file
                // available from each map tiling file tree as a json & use those values
                fileLocation = "libraries" + "/" + library + "/floor" + level + "/" + zoom + "/" + x + "/" + y + ".png"; //need to add x and y
                InputStream inputStream = null;
                // try to access the file, catches IO exception if the file is not in the right place
                try {
                    inputStream = getContext().getAssets().open(fileLocation);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // decode this file and turn it into a byte array
                // required for the tile overlay implementation of google maps
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

            /*
             * Gets bitmap as a byte array from passed bitmap
             */
            private byte[] getBytesFromBitmap(Bitmap bitmap) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                return stream.toByteArray();
            }
        };

        // overlays tiles
        tileOverlay = mMap.addTileOverlay(new TileOverlayOptions()
                .tileProvider(tileProvider));
    }

    /**
     * Creates listener for a change in database values
     * Needs to be implemented slightly differently in each class because
     * Android is finicky with contexts :(
     * I apologize to any poor soul that needs to parse this argument list
     * It's quite awful because this relies on some static functions
     * That cannot pull local variables :/
     * @param users map of user data (typically from local class)
     * @param TAG for debugging purposes
     * @param mMap map to modify
     * @param markersOnMap markers currently on the map
     * @param activity context (because getContext()/getActivity() doesnt work)
     * @param floor floor currently on
     */
    private static void listenForUserChanges(Map<Integer, SearchResultData> users, String TAG, GoogleMap mMap, List<Marker> markersOnMap, Activity activity, int floor) {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                GenericTypeIndicator<List<SearchResultData>> genericTypeIndicator =new GenericTypeIndicator<List<SearchResultData>>(){};

                // Iterate through all values in the data snapshot and add to the list of users
                // honestly this is pretty inefficient
                // refactor/change this algo bc it sucks lol
                List<SearchResultData> srd = new ArrayList<SearchResultData>();
                for (DataSnapshot values : dataSnapshot.getChildren()) {
                    SearchResultData searchResultData = values.getValue(SearchResultData.class);
                    srd.add(searchResultData);
                }
                for (SearchResultData user : srd) {
                    if (user != null) {
                        users.put(user.getSearchQueryNumber(), user);
                    }
                }
                addAllMarkers(mMap, markersOnMap, users, activity, floor);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        FirebaseDatabase currentDatabase = FirebaseDatabase.getInstance();
        currentDatabase.getReference("users").addValueEventListener(postListener);
    }

    /**
     * Initializes map with almost identical logic as listenForUserChanges
     * Might see if I can merge the two functions without losing core functionality
     * @param users map of user data
     * @param TAG for debugging purposes
     * @param mMap current map to modify
     * @param markersOnMap markers currently on the map (will probably be null but still
     *                     needs to be passed so that it can be modified given new info from database
     * @param activity current activity
     * @param floor floor that the map is on
     */
    private static void initializeMap(Map<Integer, SearchResultData> users, String TAG, GoogleMap mMap, List<Marker> markersOnMap, Activity activity, int floor) {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                GenericTypeIndicator<List<SearchResultData>> genericTypeIndicator =new GenericTypeIndicator<List<SearchResultData>>(){};

                List<SearchResultData> srd = new ArrayList<SearchResultData>();
                for (DataSnapshot values : dataSnapshot.getChildren()) {
                    SearchResultData searchResultData = values.getValue(SearchResultData.class);
                    srd.add(searchResultData);
                    System.out.println(values.toString());
                }
                for (SearchResultData user : srd) {
                    if (user != null) {
                        System.out.println("here");
                        users.put(user.getSearchQueryNumber(), user);
                    }
                }
                addAllMarkers(mMap, markersOnMap, users, activity, floor);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        FirebaseDatabase currentDatabase = FirebaseDatabase.getInstance();
        currentDatabase.getReference("users").addListenerForSingleValueEvent(postListener);
    }
}