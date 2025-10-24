package com.test.foodivery.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.test.foodivery.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GoogleAddressActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText etAddress;
    private Button btnComfirmLocation;


    private Marker currentMarker;
    private double selectedLat = 0.0, selectedLng = 0.0;
    private String selectedAddress = "", selectedSubLocality = "",areas="";
    private TextView areaName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_address);

        etAddress = findViewById(R.id.etAddress);
        btnComfirmLocation = findViewById(R.id.btnComfirmLocation);
        ImageView backBtn = findViewById(R.id.backbtn);
        backBtn.setOnClickListener(v -> finish());
        areaName=findViewById(R.id.areaName);



        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        checkLocationPermissionAndFetch();

        btnComfirmLocation.setOnClickListener(v -> {
            if (selectedLat == 0.0 && selectedLng == 0.0) {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(GoogleAddressActivity.this, MainActivity.class);
            intent.putExtra("latitude", selectedLat);
            intent.putExtra("longitude", selectedLng);
            intent.putExtra("address", selectedAddress);
            intent.putExtra("sublocality", selectedSubLocality);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

    }

    private void checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchCurrentLocation();
        }
    }

    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        moveMapMarker(latitude, longitude);
                        setAddressFromLocation(latitude, longitude);
                    } else {
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addressObj = addresses.get(0);
                selectedAddress = addressObj.getAddressLine(0);
                selectedSubLocality = addressObj.getSubLocality() != null ? addressObj.getSubLocality() : "";
                areas=addressObj.getSubLocality();
                selectedLat = latitude;
                selectedLng = longitude;
                etAddress.setText(selectedAddress);
                areaName.setText(areas);


            } else {
                etAddress.setText("");
                Toast.makeText(this, "Address not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            etAddress.setText("");
            Toast.makeText(this, "Geocoder error", Toast.LENGTH_SHORT).show();
        }
    }


    private void moveMapMarker(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        if (mMap != null) {
            if (currentMarker != null) currentMarker.remove();
            currentMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Drag to set location")
                    .draggable(true));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng defaultLatLng = new LatLng(20.5937, 78.9629); // Center of India
        moveMapMarker(defaultLatLng.latitude, defaultLatLng.longitude);

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {}

            @Override
            public void onMarkerDrag(Marker marker) {}

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng position = marker.getPosition();
                setAddressFromLocation(position.latitude, position.longitude);
                Toast.makeText(GoogleAddressActivity.this,
                        "Location set: " + position.latitude + ", " + position.longitude,
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Allow tap to set marker
        mMap.setOnMapClickListener(latLng -> {
            moveMapMarker(latLng.latitude, latLng.longitude);
            setAddressFromLocation(latLng.latitude, latLng.longitude);
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }



}
