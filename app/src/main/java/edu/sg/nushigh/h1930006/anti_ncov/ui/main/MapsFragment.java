package edu.sg.nushigh.h1930006.anti_ncov.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.sg.nushigh.h1930006.anti_ncov.R;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private List<PHPC> phpc = new ArrayList<>();
    private List<Case> cases = new ArrayList<>();
    private boolean isLocationPermissionGranted = false;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int RESULT_LOCATION_ENABLE = 420;
    private Location lastKnownLocation;
    private FusedLocationProviderClient locationProviderClient;
    private View enableLocationNotification;

    private static final float DEFAULT_ZOOM = 15;
    private String TAG = "MapsFragment";
    private final LatLng SINGAPORE = new LatLng(1.290270, 103.851959);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_maps, container, false);

        Button okButton = root.findViewById(R.id.button_ok);
        okButton.setOnClickListener(e -> startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), RESULT_LOCATION_ENABLE));
        enableLocationNotification = root.findViewById(R.id.layout_enable_location);
        mapView = root.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        locationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("phpc")
                .get()
                .addOnSuccessListener(task -> {
                    task.getDocuments().forEach(docs -> {
                        Map<String, Object> data = docs.getData();
                        PHPC m = new PHPC(data.get("name").toString(), Double.parseDouble(data.get("latitude").toString()), Double.parseDouble(data.get("longitude").toString()));
                        phpc.add(m);
                    });
                    loadCoronaCasesFromFirebase();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load PHPCs! Please check your wifi connection!", Toast.LENGTH_LONG).show());

        return root;
    }

    private void loadCoronaCasesFromFirebase() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("cases")
                .get()
                .addOnSuccessListener(task -> {
                    task.getDocuments().forEach(docs -> {
                        Map<String, Object> data = docs.getData();
                        Case c = new Case(data.get("name").toString(), data.get("desc").toString(), Double.parseDouble(data.get("latitude").toString()), Double.parseDouble(data.get("longitude").toString()), Integer.parseInt(data.get("category").toString()));
                        cases.add(c);
                        updateLocationUI();
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load cases! Please check your wifi connection!", Toast.LENGTH_LONG).show());
    }

    //linkages between cases using color markers
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map));
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LinearLayout info = new LinearLayout(getContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getContext());
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getContext());
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);
                return info;
            }
        });

        getLocationPermission();
        getDeviceLocation();
        updateLocationUI();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            isLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        isLocationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isLocationPermissionGranted = true;
            }
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (googleMap == null) {
            return;
        }
        try {
            if (isLocationPermissionGranted) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);

                phpc.forEach(phpc -> {
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(phpc.getLatitude(), phpc.getLongitude()))
                            .title(phpc.getName())
                            .snippet("Government certified PHPC clinic")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.hospital_marker)));
                });
                cases.forEach(cases -> {
                    float hue = BitmapDescriptorFactory.HUE_RED;

                    if (cases.getCategory() == 0) {
                        hue = BitmapDescriptorFactory.HUE_AZURE;
                    } else if (cases.getCategory() == 1) {
                        hue = BitmapDescriptorFactory.HUE_ORANGE;
                    } else if (cases.getCategory() == 2) {
                        hue = BitmapDescriptorFactory.HUE_YELLOW;
                    } else if (cases.getCategory() == 3) {
                        hue = BitmapDescriptorFactory.HUE_GREEN;
                    }

                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(cases.getLatitude(), cases.getLongitude()))
                            .title(cases.getName())
                            .snippet(cases.getDesc())
                            .icon(BitmapDescriptorFactory.defaultMarker(hue)));
                });
            } else {
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security error", e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOCATION_ENABLE && isGPSEnabled()) {
            googleMap.getUiSettings().setAllGesturesEnabled(true);
            enableLocationNotification.setVisibility(View.GONE);
            zoomIntoCurrentLocation();
        }
    }

    private void getDeviceLocation() {
        if (!isGPSEnabled()) {
            googleMap.getUiSettings().setAllGesturesEnabled(false);
            enableLocationNotification.setVisibility(View.VISIBLE);
            return;
        }

        zoomIntoCurrentLocation();
    }

    private boolean isGPSEnabled() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void zoomIntoCurrentLocation() {
        try {
            if (isLocationPermissionGranted) {
                Task<Location> locationResult = locationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        lastKnownLocation = task.getResult();
                        if (lastKnownLocation != null) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            new Handler(Looper.getMainLooper()).postDelayed(this::zoomIntoCurrentLocation, 3000);
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        googleMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(SINGAPORE, DEFAULT_ZOOM));
                        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Error", e);
        }
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
