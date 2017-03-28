package com.memorycreat.memorycreator;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.memorycreat.memorycreator.data.DBContract;

import static com.memorycreat.memorycreator.R.id.map;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
                                                    GoogleMap.OnMapClickListener,
                                                    GoogleMap.OnMarkerClickListener,
                                                LoaderManager.LoaderCallbacks<Cursor>
{


    private static final String TAG = "MapsActivity";
    private final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private final float ZOOM_AFTER_SEARCH = 12;
    private static final int ID_MARKER_LOADER = 1325;

    private static final String[] MAIN_PROJECTION = {
            DBContract.MarkerData.COLUMN_LAT,
            DBContract.MarkerData.COLUMN_LNG };


    public static final int INDEX_COLUMN_LAT = 0;
    public static final int INDEX_COLUMN_LNG = 1;



    private GoogleMap mMap;


    /**
     * Launches the main activity.
     * @param savedInstanceState Bundle with saved info.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_render);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
            // Creates autocomplete search bar.
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                //  Gets info about the selected place and moves to its position.
                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_AFTER_SEARCH));
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                //  Handles the error.
                Toast.makeText(MapsActivity.this, "An error has occured while creating a search bar.",
                        Toast.LENGTH_LONG).show();
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        getSupportLoaderManager().initLoader(ID_MARKER_LOADER, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();


        getSupportLoaderManager().initLoader(ID_MARKER_LOADER, null, this);

    }

    @Override
    public void onPause() {
        super.onPause();

        getLoaderManager().destroyLoader(ID_MARKER_LOADER);
    }

    /**
     * Google's method for search result handling.
     * @param requestCode request
     * @param resultCode result
     * @param data data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);

                Log.i(TAG, "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_LONG).show();
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Toast.makeText(this, "You canceled the operation.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Initializes GoogleMap  with Retro style.
     * @param googleMap map to display
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.448853, 30.513346), 4));
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapstyle_retro));

            if (!success) {
                Log.e("MapsActivityRaw", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivityRaw", "Can't find style.", e);
        }
    }


    /**
     * Handles the event. Opens custom template view with two text fields: date and impressions.
     * @param marker current marker
     * @return true if successful.
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLng latLng = marker.getPosition();
        double lat = latLng.latitude;
        double lng = latLng.longitude;

        startMarkerInfoActivity(lat, lng);
        return true;
    }

    private void startMarkerInfoActivity(double lat, double lng) {
        Intent detailIntent = new Intent(MapsActivity.this, MarkerInfoActivity.class);
        Uri uriForMarkerClicked = DBContract.MarkerData.buildUriWithLatLng(lat, lng);
        detailIntent.setData(uriForMarkerClicked);
        startActivity(detailIntent);
    }

    /**
     * Creates marker with custom icon.
     * @param point coordinates of the marker.
     */
    @Override
    public void onMapClick(LatLng point) {
        double lat = point.latitude;
        double lng = point.longitude;

        //startMarkerInfoActivity(lat, lng);
        makeMarker(lat, lng);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

        switch (loaderId) {
            case ID_MARKER_LOADER:
                Uri markerQueryUri = DBContract.MarkerData.CONTENT_URI;

                return new CursorLoader(this,
                        markerQueryUri,
                        MAIN_PROJECTION,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || !data.moveToFirst()) {
            return;
        }
        else {
            setUpMarkers(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {    }

    /**
     * Sets up all the markers from the database.
     *
     */
    public void setUpMarkers(Cursor cursor) {
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            double lat = cursor.getDouble(INDEX_COLUMN_LAT);
            double lng = cursor.getDouble(INDEX_COLUMN_LNG);
            makeMarker(lat, lng);
            cursor.moveToNext();
        }
    }

    /**
     * Makes markers with custom icons.
     *
     */
    public void makeMarker(double lat, double lng) {

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin)));
    }

}


