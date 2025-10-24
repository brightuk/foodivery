package com.test.foodivery.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.test.foodivery.Attributes.Attributes;
import com.test.foodivery.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddressActivity extends AppCompatActivity {

    private EditText cust_name, cust_mobileno, cust_address;
    private InputMethodManager inputMethodManager;
    private Button submitButton;
    private ImageView backBtn;
    private TextView headerTitle;

    // Google Map variables
    private GoogleMap mMap;
    private LatLng selectedLatLng = null;
    private Marker marker;
    private Geocoder geocoder;
    private String selectedFullAddress = "";
    private String selectedSublocality = "";
    private String selectedCity = "";

    // Edit mode variables
    private boolean isEditMode = false;
    private String addressId = "";
    private String originalLatitude = "";
    private String originalLongitude = "";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_address);

        // Initialize views
        submitButton = findViewById(R.id.address_add);
        cust_name = findViewById(R.id.cust_Name);
        cust_mobileno = findViewById(R.id.cust_PhoneNo);
        cust_address = findViewById(R.id.cust_address);
        headerTitle = findViewById(R.id.headerTitle);

        backBtn = findViewById(R.id.backButton);
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        // Check if this is edit mode
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("isEdit", false)) {
            isEditMode = true;
            addressId = intent.getStringExtra("addressId");
            headerTitle.setText("Edit Address");
            submitButton.setText("Update Address");
            
            // Pre-fill the fields with existing data
            cust_name.setText(intent.getStringExtra("custName"));
            cust_mobileno.setText(intent.getStringExtra("custMobile"));
            cust_address.setText(intent.getStringExtra("custAddress"));
            selectedSublocality = intent.getStringExtra("custArea");
            selectedCity = intent.getStringExtra("custCity");
            
            // For edit mode, we'll use the original coordinates
            // You might want to fetch the actual coordinates from your database
            originalLatitude = "13.0827"; // Default to Chennai, you should get this from your data
            originalLongitude = "80.2707";
        } else {
            headerTitle.setText("Add New Address");
            submitButton.setText("Add Address");
        }

        // Hide keyboard on root touch
        View rootView = findViewById(R.id.main);
        rootView.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });

        // Back button
        backBtn.setOnClickListener(v -> finish());

        // Hide keyboard when done editing address
        cust_address.setOnEditorActionListener((v, actionId, event) -> {
            hideKeyboard();
            return true;
        });

        // Get customer ID from shared preferences
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String custId = prefs.getString("cust_id", "");

        // Request location permission if needed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // --- Google Map setup ---
        geocoder = new Geocoder(this, Locale.getDefault());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;

                    // Enable My Location layer if permission granted
                    if (ActivityCompat.checkSelfPermission(AddressActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);

                        if (isEditMode) {
                            // In edit mode, move to the original location
                            LatLng originalLatLng = new LatLng(Double.parseDouble(originalLatitude), Double.parseDouble(originalLongitude));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originalLatLng, 16));
                            marker = mMap.addMarker(new MarkerOptions()
                                    .position(originalLatLng)
                                    .title("Current Address Location")
                                    .draggable(true));
                            selectedLatLng = originalLatLng;
                            fetchAddressFromLatLng(originalLatLng);
                        } else {
                            // Move camera to current location for new address
                            fusedLocationClient.getLastLocation()
                                    .addOnSuccessListener(AddressActivity.this, location -> {
                                        if (location != null) {
                                            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
                                            marker = mMap.addMarker(new MarkerOptions()
                                                    .position(currentLatLng)
                                                    .title("Your Location")
                                                    .draggable(true));
                                            selectedLatLng = currentLatLng;
                                            fetchAddressFromLatLng(currentLatLng);
                                        }
                                    });
                        }
                    } else {
                        // Permission not granted, move camera to default location (e.g. Chennai)
                        LatLng defaultLatLng = new LatLng(13.0827, 80.2707);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 12));
                    }

                    // User taps map: place a draggable marker
                    mMap.setOnMapClickListener(latLng -> {
                        selectedLatLng = latLng;
                        if (marker != null) marker.remove();
                        marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title("Selected Location")
                                .draggable(true));
                        fetchAddressFromLatLng(latLng);
                    });

                    // Enable drag-and-drop for marker
                    mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                        @Override
                        public void onMarkerDragStart(Marker marker) {}

                        @Override
                        public void onMarkerDrag(Marker marker) {}

                        @Override
                        public void onMarkerDragEnd(Marker marker) {
                            selectedLatLng = marker.getPosition();
                            fetchAddressFromLatLng(selectedLatLng);
                        }
                    });
                }
            });
        }

        // Submit button logic
        submitButton.setOnClickListener(v -> {
            hideKeyboard();

            String addressToSend = !selectedFullAddress.isEmpty() ? selectedFullAddress : "";
            String mAddress=cust_address.getText().toString();
            String sublocalityToSend = !selectedSublocality.isEmpty() ? selectedSublocality : "";
            String cityToSend = !selectedCity.isEmpty() ? selectedCity : "";

            if (!cust_name.getText().toString().isEmpty() &&
                    !cust_mobileno.getText().toString().isEmpty() &&
                    !addressToSend.isEmpty() &&
                    !cityToSend.isEmpty()) {

                if (cust_mobileno.length() == 10) {
                    if (selectedLatLng == null) {
                        Toast.makeText(AddressActivity.this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isEditMode) {
                        // Update existing address
                        updateCustAddress(
                                addressId,
                                custId,
                                cust_name.getText().toString(),
                                cust_mobileno.getText().toString(),
                                addressToSend,
                                mAddress,
                                sublocalityToSend,
                                cityToSend,
                                Double.parseDouble(String.format(Locale.US, "%.5f", selectedLatLng.latitude)),
                                Double.parseDouble(String.format(Locale.US, "%.5f", selectedLatLng.longitude))
                        );
                    } else {
                        // Add new address
                        addCustAddress(
                                custId,
                                cust_name.getText().toString(),
                                cust_mobileno.getText().toString(),
                                addressToSend,
                                mAddress,
                                sublocalityToSend,
                                cityToSend,
                                Double.parseDouble(String.format(Locale.US, "%.5f", selectedLatLng.latitude)),
                                Double.parseDouble(String.format(Locale.US, "%.5f", selectedLatLng.longitude))
                        );
                    }
                } else {
                    Toast.makeText(AddressActivity.this, "Please enter a valid Mobile Number", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(AddressActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Reverse geocode to get address, sublocality, city
    private void fetchAddressFromLatLng(LatLng latLng) {
        new Thread(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    selectedCity = address.getLocality() != null ? address.getLocality() : "";
                    selectedSublocality = address.getSubLocality() != null ? address.getSubLocality() : "";
                    selectedFullAddress = address.getAddressLine(0) != null ? address.getAddressLine(0) : "";

                    runOnUiThread(() -> {
                        // Update the address field with the fetched address
                        if (!selectedFullAddress.isEmpty()) {
                            cust_address.setText(selectedFullAddress);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Hide keyboard helper
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    // Hide keyboard on touch outside
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)ev.getRawX(), (int)ev.getRawY())) {
                    hideKeyboard();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    // Update existing customer address
    private void updateCustAddress(String addressId, String cust_id, String c_name, String c_mobileno, String c_address, String maddress, String c_area, String c_city, double latitude, double longitude) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("address_id", addressId)
                .add("cust_id", cust_id)
                .add("c_name", c_name)
                .add("c_mobileno", c_mobileno)
                .add("c_address", maddress)
                .add("c_apifetchAddress", c_address)
                .add("c_area", c_area)
                .add("c_city", c_city)
                .add("latitude", String.format(Locale.US, "%.8f", latitude))
                .add("longitude", String.format(Locale.US, "%.8f", longitude))
                .build();

        Request request = new Request.Builder()
                .url(Attributes.Edit_Address)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AddressActivity.this, "API Request Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Log.e("API_ERROR", "Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) return;
                try {
                    String responseBody = response.body().string();
                    Log.d("API_RESPONSE", responseBody);

                    JSONObject responseObject = new JSONObject(responseBody);
                    runOnUiThread(() -> {
                        Toast.makeText(AddressActivity.this, "Address updated successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AddressActivity.this, AddressFindActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("JSON_ERROR", "Failed to parse: " + e.getMessage());
                }
            }
        });
    }

    // Add customer address with latitude and longitude
    public void addCustAddress(String cust_id, String c_name, String c_mobileno, String c_address, String maddress,String c_area, String c_city, double latitude, double longitude) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("cust_id", cust_id)
                .add("c_name", c_name)
                .add("c_mobileno", c_mobileno)
                .add("c_address", maddress)
                .add("c_apifetchAddress",c_address)
                .add("c_area", c_area)
                .add("c_city", c_city)
                .add("latitude", String.format(Locale.US, "%.8f", latitude))
                .add("longitude", String.format(Locale.US, "%.8f", longitude))
                .build();

        Request request = new Request.Builder()
                .url(Attributes.Add_Address)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AddressActivity.this, "API Request Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Toast.makeText(AddressActivity.this, "API Request Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("API_ERROR", "Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) return;
                try {
                    String responseBody = response.body().string();
                    Log.d("API_RESPONSE", responseBody);

                    JSONObject responseObject = new JSONObject(responseBody);
                    runOnUiThread(() -> {
                        Toast.makeText(AddressActivity.this, "Address added successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AddressActivity.this, AddressFindActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("JSON_ERROR", "Failed to parse: " + e.getMessage());
                }
            }
        });
    }

    // Handle location permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate(); // Restart activity to initialize map with location
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
