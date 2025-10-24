package com.test.foodivery.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.test.foodivery.Fragment.OfferFragment;
import com.test.foodivery.Fragment.CartFragment;
import com.test.foodivery.Fragment.HistoryFragment;
import com.test.foodivery.Fragment.ProfileFragment;
import com.test.foodivery.Attributes.Attributes;
import com.test.foodivery.Database.CartDatabaseHelper;
import com.test.foodivery.Model.OfferModel;
import com.test.foodivery.Model.OrderHistoryModel;
import com.test.foodivery.Model.OrderProductModel;
import com.test.foodivery.Adapter.PendingOrderAdapter;
import com.test.foodivery.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

public class MainActivity extends AppCompatActivity {

    // UI Components
    public ImageView off1, off2, off3, off4, off5, off6;
    private AutoCompleteTextView autoCompleteProducts;
    private TextView cityTextView, addressTextView, androidVersionTextView, menus;
    private LinearLayout layout;

    private static final int SMS_PERMISSION_CODE = 100;

    // Location Services
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final int REQUEST_CHECK_SETTINGS = 1001;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;

    private String lat = null, lon = null;
    private double pickerLat = 0.0, pickerLng = 0.0;
    private boolean isFromAddressPicker = false;

    // Data
    private CartDatabaseHelper cartDatabaseHelper;
    private List<OfferModel> imageList;
    private ArrayList<String> shopIdList, productIdList, products, sendProduct;
    private ArrayList<HashMap<String, String>> shopDetailsList; // For shop details
    private boolean hasValidShops = false;
    private boolean checkAddress = false;

    // Pending Orders
    private RecyclerView rvPendingOrders;
    private LinearLayout pendingOrdersSection;
    private PendingOrderAdapter pendingOrderAdapter;
    private List<OrderHistoryModel> pendingOrdersList;

    // Refresh Handler
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private static final long REFRESH_INTERVAL = 1000; // 1 second

    // SharedPreferences keys
    private static final String PREF_LAST_LAT = "last_lat";
    private static final String PREF_LAST_LNG = "last_lng";
    private static final String PREF_LAST_ADDRESS = "last_address";
    private static final String PREF_LAST_CITY = "last_city";
    private static final int REQUEST_CODE_ADDRESS = 2000;
    LinearLayout mainLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestSmsPermission();

        initializeViews();
        setupUIComponents();
        checkUserSession();
        checkFcmToken(tokenFetch());
        setupLocationServices();

        // Initialize refresh handler
        setupRefreshHandler();

        // Check if coming from Address Picker or use saved location
        handleIntentLocation();
        handleNewLocationFromIntent(getIntent());

        handleIntent(getIntent());

    }

    private void setupRefreshHandler() {
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshData();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        };
    }

    private void refreshData() {
        // Only refresh if we're on the home screen
        View contentRoot = findViewById(R.id.main);
        if (contentRoot != null && contentRoot.getVisibility() == View.VISIBLE) {
            // Refresh cart badge
            updateCartBadge();

            // Refresh pending orders if user is logged in
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            String cust_id = prefs.getString("cust_id", "");
            if (!cust_id.isEmpty()) {
                fetchPrimaryAddress(cust_id);
                fetchPendingOrders(cust_id);
            }
        }
    }

    private void checkFcmToken(String token) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String currentToken = task.getResult();
                Log.d("TokenCheck", "Current: " + currentToken + " | Stored: " + token);

                if (!currentToken.equals(token)) {
                    Log.d("TokenCheck", "Token needs update");
                    FCMTokenUpdate(SessionFetch(), currentToken);
                } else {
                    Log.d("TokenCheck", "Token is up-to-date");
                }
            } else {
                Log.e("TokenCheck", "Error getting token: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
            }
        });
    }
    private String SessionFetch() {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        return prefs.getString("cust_id", ""); // default empty string
    }
    private void FCMTokenUpdate(String cust_id,String currentToken) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("cust_id", cust_id)
                .add("token", currentToken)
                .build();
        Request request = new Request.Builder()
                .url(Attributes.Update_UserToken)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Log.d("hhhhjjjjkk",cust_id+" : "+currentToken);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "API Request Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Log.e("API_ERROR", "Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "API Response Failed", Toast.LENGTH_SHORT).show());
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    Log.d("API_RESPONSEggglll", responseBody);
                    JSONObject responseObject = new JSONObject(responseBody);
                    JSONObject dataObject = responseObject.getJSONObject("data");
                    String tokens = dataObject.getString("token");
                    SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("token", tokens);
                    editor.apply(); // or editor.commit()
//                    runOnUiThread(() -> {
//                        if ("1".equals(shopStatus)) {
//                            btnOpenClose.setText("Close");
//                        } else {
//                            btnOpenClose.setText("Open");
//                        }
//                    });
                } catch (JSONException e) {
                    Log.e("JSON_ERROR", "Parsing failed: " + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "JSON Parsing Error", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            // If asked to open cart directly, switch to cart tab
            if (intent.getBooleanExtra("OPEN_CART", false)) {
                BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
                if (bottomNavigationView != null) {
                    bottomNavigationView.setSelectedItemId(R.id.ic_cart);
                }
                swapFragment(new CartFragment());
                return;
            }
            if (intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
                // Handle address selection from AddressFindActivity
                handleAddressSelection(intent);
            } else {
                // Check saved location or fetch new
                checkSavedLocationOrFetchNew();
            }
        } else {
            checkSavedLocationOrFetchNew();
        }
    }

    private void handleAddressSelection(Intent intent) {
        double latitude = intent.getDoubleExtra("latitude", 0.0);
        double longitude = intent.getDoubleExtra("longitude", 0.0);
        String address = intent.getStringExtra("faddress");
        String sublocality = intent.getStringExtra("sublocality");

        // Save to shared preferences
        saveLocationToPreferences(latitude, longitude, address, sublocality);

        // Update UI immediately
        updateLocationUI(sublocality, address);

        // Fetch vendors for this location
        fetchAllVendor(String.valueOf(latitude), String.valueOf(longitude));
    }

    private void initializeViews() {
        off1 = findViewById(R.id.oneKgCake);
        off2 = findViewById(R.id.halfKgCake);
        off3 = findViewById(R.id.kKgCake);
        off4 = findViewById(R.id.cake499);
        off5 = findViewById(R.id.cake699);
        off6 = findViewById(R.id.cake999);
        mainLayout = findViewById(R.id.mainLayout);
        layout = findViewById(R.id.addAddress);
        cityTextView = findViewById(R.id.city);
        addressTextView = findViewById(R.id.address);
        autoCompleteProducts = findViewById(R.id.autoCompleteProducts);
        menus = findViewById(R.id.menus);

        // Initialize pending orders views
        rvPendingOrders = findViewById(R.id.rv_pending_orders);
        pendingOrdersSection = findViewById(R.id.pending_orders_section);

        shopIdList = new ArrayList<>();
        productIdList = new ArrayList<>();
        products = new ArrayList<>();
        shopDetailsList = new ArrayList<>(); // Initialize shop details list
        cartDatabaseHelper = new CartDatabaseHelper(this);

        // Initialize pending orders
        pendingOrdersList = new ArrayList<>();
        pendingOrderAdapter = new PendingOrderAdapter(this, pendingOrdersList);
        rvPendingOrders.setLayoutManager(new LinearLayoutManager(this));
        rvPendingOrders.setAdapter(pendingOrderAdapter);
    }

    private void setupUIComponents() {
        layout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddressFindActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADDRESS);
        });

        menus.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.putStringArrayListExtra("SHOP_ID_LIST", new ArrayList<>(shopIdList));
            ArrayList<String> shopDetailsJsonList = new ArrayList<>();
            for (HashMap<String, String> shop : shopDetailsList) {
                shopDetailsJsonList.add(new JSONObject(shop).toString());
            }
            intent.putStringArrayListExtra("SHOP_DETAILS_LIST", shopDetailsJsonList);
            startActivity(intent);
        });

        setupOfferClickListeners();
        setupAutoCompleteTextView();
        setupBottomNavigation();
    }

    private void setupLocationServices() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void handleIntentLocation() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
            handleNewLocationFromIntent(intent);
        } else {
            checkSavedLocationOrFetchNew();
        }
    }

    private void handleNewLocationFromIntent(Intent intent) {
        if (intent != null && intent.hasExtra("latitude") && intent.hasExtra("longitude")) {
            pickerLat = intent.getDoubleExtra("latitude", 0.0);
            pickerLng = intent.getDoubleExtra("longitude", 0.0);
            String address = intent.getStringExtra("faddress");
            String sublocality = intent.getStringExtra("sublocality");

            // Save to shared preferences
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREF_LAST_LAT, String.valueOf(pickerLat));
            editor.putString(PREF_LAST_LNG, String.valueOf(pickerLng));
            editor.putString(PREF_LAST_ADDRESS, address);
            editor.putString(PREF_LAST_CITY, sublocality);
            editor.apply();

            // Update UI immediately
            runOnUiThread(() -> {
                cityTextView.setText(sublocality != null ? sublocality : "Unknown Area");
                addressTextView.setText(address != null ?
                        (address.length() > 50 ? address.substring(0, 50) + "..." : address)
                        : "Unknown Address");
            });

            // Fetch vendors for this location
            fetchAllVendor(String.valueOf(pickerLat), String.valueOf(pickerLng));
            isFromAddressPicker = true;
        } else {
            // If no location in intent, check saved preferences
            checkSavedLocationOrFetchNew();
        }
    }

    private void saveLocationToPreferences(double lat, double lng, String address, String city) {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        prefs.edit()
                .putString(PREF_LAST_LAT, String.valueOf(lat))
                .putString(PREF_LAST_LNG, String.valueOf(lng))
                .putString(PREF_LAST_ADDRESS, address)
                .putString(PREF_LAST_CITY, city)
                .apply();
    }

    private void checkSavedLocationOrFetchNew() {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String lastLat = prefs.getString(PREF_LAST_LAT, null);
        String lastLng = prefs.getString(PREF_LAST_LNG, null);
        String lastAddress = prefs.getString(PREF_LAST_ADDRESS, null);
        String lastCity = prefs.getString(PREF_LAST_CITY, null);

        if (lastLat != null && lastLng != null) {
            pickerLat = Double.parseDouble(lastLat);
            pickerLng = Double.parseDouble(lastLng);
            updateLocationUI(lastCity, lastAddress);
            fetchAllVendor(lastLat, lastLng);
        } else {
            setupLocationLogic();
        }
    }
    private String tokenFetch() {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        return prefs.getString("token", ""); // default empty string
    }

    private void updateLocationUI(String city, String address) {
        runOnUiThread(() -> {
            cityTextView.setText(city != null && !city.isEmpty() ? city : "Unknown City");
            if (address != null) {
                addressTextView.setText(address.length() > 50 ?
                        address.substring(0, 50) + "......" : address);
            } else {
                addressTextView.setText("Unknown Address");
            }
        });
    }

    private void setupLocationLogic() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> getCurrentLocation());
        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ((ResolvableApiException) e).startResolutionForResult(
                            MainActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    showToast("Location settings could not be resolved");
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        pickerLat = location.getLatitude();
                        pickerLng = location.getLongitude();
                        getAddressFromLocation(pickerLat, pickerLng);
                    } else {
                        showToast("Could not get current location");
                    }
                });
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String fullAddress = address.getAddressLine(0);
                String sublocality = address.getSubLocality();
                saveLocationToPreferences(latitude, longitude, fullAddress, sublocality);
                updateLocationUI(sublocality, fullAddress);
                fetchAllVendor(String.valueOf(latitude), String.valueOf(longitude));
            } else {
                updateLocationUI("Unknown City", "Unknown Address");
                showToast("Could not get address from location");
            }
        } catch (IOException e) {
            Log.e("GeocoderError", "Error getting address", e);
            updateLocationUI("Unknown City", "Unknown Address");
            showToast("Error getting address: " + e.getMessage());
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.ic_home);
        updateCartBadge();

        bottomNavigationView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_UNLABELED);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.ic_home) {
                showHomeContent();
                return true;
            } else if (id == R.id.ic_history) {
                swapFragment(new HistoryFragment());
                return true;
            } else if (id == R.id.ic_profile) {
                swapFragment(new ProfileFragment());
                return true;
            } else if (id == R.id.ic_productView) {
                OfferFragment offerFragment = new OfferFragment();
                Bundle args = new Bundle();
                args.putString("name", "all");
                args.putStringArrayList("SHOP_ID_LIST", new ArrayList<>(shopIdList));
                ArrayList<String> shopDetailsJsonList = new ArrayList<>();
                for (HashMap<String, String> shop : shopDetailsList) {
                    shopDetailsJsonList.add(new JSONObject(shop).toString());
                }
                args.putStringArrayList("SHOP_DETAILS_LIST", shopDetailsJsonList);
                offerFragment.setArguments(args);
                swapFragment(offerFragment);
                return true;
            } else if (id == R.id.ic_cart) {
                swapFragment(new CartFragment());
                return true;
            }
            return false;
        });
    }

    private void updateCartBadge() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        int cartCount = cartDatabaseHelper.getCartItemCount();
        if (cartCount > 0) {
            BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.ic_cart);
            badge.setVisible(true);
            badge.setNumber(cartCount);
        } else {
            bottomNavigationView.removeBadge(R.id.ic_cart);
        }
    }

    private void showProgressBar() {
        FrameLayout progressOverlay = findViewById(R.id.progressOverlay);
        if (progressOverlay != null) {
            progressOverlay.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgressBar() {
        FrameLayout progressOverlay = findViewById(R.id.progressOverlay);
        if (progressOverlay != null) {
            progressOverlay.setVisibility(View.GONE);
        }
    }

    private void setupAutoCompleteTextView() {
        autoCompleteProducts.setOnItemClickListener((parent, view, position, id) -> {
            String selected = parent.getItemAtPosition(position).toString().trim();

            HashMap<String, String> selectedShop = null;
            boolean isShop = false;
            for (HashMap<String, String> shop : shopDetailsList) {
                if (shop.get("store_folder").equalsIgnoreCase(selected)) {
                    isShop = true;
                    selectedShop = shop;
                    break;
                }
            }

            if (isShop && selectedShop != null) {
                // Build a JSON object for the shop, with expected keys
                JSONObject shopJson = new JSONObject();
                try {
                    shopJson.put("shop_id", selectedShop.get("shop_id")); // Add shop_id!
                    shopJson.put("biz_id", selectedShop.get("biz_id"));
                    shopJson.put("shop_logo", selectedShop.get("shop_logo"));
                    shopJson.put("shop_name", selectedShop.get("shop_name"));

                    shopJson.put("store_folder", selectedShop.get("store_folder"));
                    // Add other keys as needed for BrandModel
                } catch (Exception e) {
                    e.printStackTrace();
                }
                JSONArray brandDataArray = new JSONArray();
                brandDataArray.put(shopJson);

                ArrayList<String> shopIdList = new ArrayList<>();
                shopIdList.add(selectedShop.get("shop_id"));

                ArrayList<String> shopDetailsJsonList = new ArrayList<>();
                shopDetailsJsonList.add(shopJson.toString());

                Intent intent = new Intent(MainActivity.this, BrandPage.class);
                intent.putExtra("brand_data", brandDataArray.toString());
                intent.putStringArrayListExtra("SHOP_ID_LIST", shopIdList);
                intent.putStringArrayListExtra("SHOP_DETAILS_LIST", shopDetailsJsonList);
                Log.d("kkkkkkkkkkkk", "brand_data: " + brandDataArray);
                Log.d("kkkkkkkkkkkk", "SHOP_ID_LIST: " + shopIdList);
                Log.d("kkkkkkkkkkkk", "SHOP_DETAILS_LIST: " + shopDetailsJsonList);

                startActivity(intent);
                autoCompleteProducts.setText("");
            } else {
                // Product selected, launch FilterActivity
                Intent intent = new Intent(MainActivity.this, FilterActivity.class);
                intent.putExtra("selected", selected);
                intent.putStringArrayListExtra("SHOP_ID_LIST", new ArrayList<>(shopIdList));
                ArrayList<String> shopDetailsJsonList = new ArrayList<>();
                for (HashMap<String, String> shop : shopDetailsList) {
                    shopDetailsJsonList.add(new JSONObject(shop).toString());
                }
                intent.putStringArrayListExtra("SHOP_DETAILS_LIST", shopDetailsJsonList);

                startActivity(intent);
                autoCompleteProducts.setText("");
            }
        });
    }

    private void swapFragment(Fragment fragment) {
        View contentRoot = findViewById(R.id.main);
        View fragmentContainer = findViewById(R.id.fragment_container);
        if (contentRoot != null && fragmentContainer != null) {
            contentRoot.setVisibility(View.GONE);
            fragmentContainer.setVisibility(View.VISIBLE);
        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        tx.replace(R.id.fragment_container, fragment);
        tx.commitAllowingStateLoss();
    }

    private void showHomeContent() {
        View contentRoot = findViewById(R.id.main);
        View fragmentContainer = findViewById(R.id.fragment_container);
        if (contentRoot != null) contentRoot.setVisibility(View.VISIBLE);
        if (fragmentContainer != null) fragmentContainer.setVisibility(View.GONE);
    }

    private void checkUserSession() {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String cust_id = prefs.getString("cust_id", "");
        Log.d("UserSession", "Customer ID: " + cust_id);
        fetchPrimaryAddress(cust_id);

        // Fetch pending orders if user is logged in
        if (!cust_id.isEmpty()) {
            fetchPendingOrders(cust_id);
        }
    }

    private void setupOfferClickListeners() {
        View.OnClickListener offerClickListener = v -> {
            if (shopIdList == null || shopIdList.isEmpty()) {
                showToast("No shops available in your area");
                return;
            }

            String offerName = "";
            int id = v.getId();
            if (id == R.id.oneKgCake) offerName = "1kg";
            else if (id == R.id.halfKgCake) offerName = "500g";
            else if (id == R.id.kKgCake) offerName = "250g";
            else if (id == R.id.cake499) offerName = "less499";
            else if (id == R.id.cake699) offerName = "less699";
            else if (id == R.id.cake999) offerName = "less999";

            Intent intent = new Intent(MainActivity.this, OfferActivity.class);
            intent.putExtra("name", offerName);
            intent.putStringArrayListExtra("SHOP_ID_LIST", new ArrayList<>(shopIdList));

            // Convert shopDetailsList to JSON strings for passing via Intent
            ArrayList<String> shopDetailsJsonList = new ArrayList<>();
            for (HashMap<String, String> shop : shopDetailsList) {
                shopDetailsJsonList.add(new JSONObject(shop).toString());
            }
            Log.d("ggggrreeee", String.valueOf(shopDetailsJsonList));
            intent.putStringArrayListExtra("SHOP_DETAILS_LIST", shopDetailsJsonList);

            startActivity(intent);
        };

        off1.setOnClickListener(offerClickListener);
        off2.setOnClickListener(offerClickListener);
        off3.setOnClickListener(offerClickListener);
        off4.setOnClickListener(offerClickListener);
        off5.setOnClickListener(offerClickListener);
        off6.setOnClickListener(offerClickListener);
    }

    private void fetchPrimaryAddress(String custId) {
        Log.d("FetchAddress", "Fetching primary address for customer: " + custId);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("cust_id", custId)
                .build();

        Request request = new Request.Builder()
                .url(Attributes.Fetch_Address)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FetchAddress", "Network error: " + e.getMessage());
                runOnUiThread(() -> {
                    showToast("Failed to fetch address");
                    setupLocationLogic();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    JSONObject responseObject = new JSONObject(responseBody);

                    if (!responseObject.getBoolean("status") || !responseObject.has("data")) {
                        throw new Exception("No address data available");
                    }

                    JSONArray dataArray = responseObject.getJSONArray("data");
                    if (dataArray.length() == 0) {
                        throw new Exception("No addresses found");
                    }

                    String custName = null;
                    String custAddress = null;
                    String latitude = null;
                    String longitude = null;

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject addressObj = dataArray.getJSONObject(i);
                        if ("1".equals(addressObj.getString("cust_address_primary"))) {
                            custName = addressObj.getString("cust_area");
                            custAddress = addressObj.getString("apifetch_address");
                            latitude = addressObj.getString("latitude");
                            longitude = addressObj.getString("longitude");
                            break;
                        }
                    }

                    if (custName == null) {
                        JSONObject firstAddress = dataArray.getJSONObject(0);
                        custName = firstAddress.getString("cust_area");
                        custAddress = firstAddress.getString("apifetch_address");
                        latitude = firstAddress.getString("latitude");
                        longitude = firstAddress.getString("longitude");
                    }

                    final String finalLat = latitude;
                    final String finalLon = longitude;
                    String finalCustName = custName;
                    String finalCustAddress = custAddress;
                    runOnUiThread(() -> {
                        cityTextView.setText(finalCustName != null ? finalCustName : "Unknown Location");
                        addressTextView.setText(finalCustAddress != null ? finalCustAddress : "");
                        addressTextView.setText((finalCustAddress.length() > 50 && finalCustAddress != null) ? finalCustAddress.substring(0, 50) + "..." : finalCustAddress);

                        if (finalLat != null && finalLon != null) {
                            pickerLat = Double.parseDouble(finalLat);
                            pickerLng = Double.parseDouble(finalLon);
                            fetchAllVendor(finalLat, finalLon);
                        } else {
                            showToast("No valid coordinates found");
                            setupLocationLogic();
                        }
                    });

                } catch (Exception e) {
                    Log.e("FetchAddress", "Error processing address: " + e.getMessage());
                    runOnUiThread(() -> {
                        setupLocationLogic();
                    });
                }
            }
        });
    }

    private void fetchPendingOrders(String custId) {
        Log.d("FetchPendingOrders", "Fetching pending orders for customer: " + custId);

        OkHttpClient client = new OkHttpClient();
        String url = Attributes.Fetch_History + "?cust_id=" + custId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-api", "C4CCBC195C709FB4SE98AA8")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FetchPendingOrders", "Network error: " + e.getMessage());
                runOnUiThread(() -> {
                    // Don't show error toast for pending orders, just hide the section
                    pendingOrdersSection.setVisibility(View.GONE);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    JSONObject responseObject = new JSONObject(responseBody);
                    JSONArray ordersArray = responseObject.optJSONArray("orders");

                    List<OrderHistoryModel> pendingOrders = new ArrayList<>();

                    if (ordersArray != null) {
                        for (int i = 0; i < ordersArray.length(); i++) {
                            try {
                                JSONObject obj = ordersArray.getJSONObject(i);
                                String deliveryStatus = obj.optString("delivery_status");
                                String orderStatus = obj.optString("order_status");

                                // Only show orders that are not delivered or cancelled
                                if (!"delivered".equalsIgnoreCase(deliveryStatus) &&
                                        !"cancelled".equalsIgnoreCase(deliveryStatus) &&
                                        !"CNL".equalsIgnoreCase(orderStatus)) {

                                    OrderHistoryModel order = new OrderHistoryModel(
                                            obj.optString("order_id"),
                                            obj.optString("order_no"),
                                            obj.optString("total_amount"),
                                            obj.optString("payment_status"),
                                            obj.optString("delivery_status"),
                                            obj.optString("delivery_date"),
                                            obj.optString("delivery_time"),
                                            obj.optString("delivery_time_end"),
                                            obj.optString("delivery_location"),
                                            obj.optString("status")
                                    );
                                    order.setOrderStatus(obj.optString("order_status"));
                                    order.setPaymentMethod(obj.optString("payment_method"));
                                    order.setDeliveryMethod(obj.optString("delivery_method"));
                                    order.setOrderedDate(obj.optString("ordered_date"));
                                    order.setOrderNotes(obj.optString("order_notes"));
                                    order.setGrossAmount(obj.optString("gross_amount"));
                                    order.setGstAmount(obj.optString("gst_amount"));
                                    order.setDeliveryAmount(obj.optString("delivery_amount"));
                                    order.setDiscountAmount(obj.optString("discount_amount"));
                                    order.setNetAmount(obj.optString("net_amount"));
                                    order.setAdvanceAmount(obj.optString("advance_amount"));
                                    order.setCashReceivedAmount(obj.optString("cash_received_amount"));
                                    order.setOnlineAmount(obj.optString("online_amount"));
                                    order.setInvoiceNo(obj.optString("invoice_no"));
                                    order.setInvoiceDate(String.valueOf(obj.opt("invoice_date")));
                                    String invoiceUrl = obj.optString("invoice_url", null);
                                    if (invoiceUrl == null || invoiceUrl.length() == 0) invoiceUrl = obj.optString("invoice_pdf", null);
                                    if (invoiceUrl == null || invoiceUrl.length() == 0) invoiceUrl = obj.optString("invoice_link", null);
                                    order.setInvoiceUrl(invoiceUrl);

                                    String senderTop = obj.optString("caller_info", null);
                                    String receiverTop = obj.optString("delivery_info", null);
                                    if (senderTop != null && senderTop.length() > 0) order.setSenderInfo(senderTop);
                                    if (receiverTop != null && receiverTop.length() > 0) order.setReceiverInfo(receiverTop);

                                    // Parse order products
                                    JSONObject details = obj.optJSONObject("details");
                                    if (details != null) {
                                        JSONArray cart = details.optJSONArray("order_cart");
                                        if (cart != null) {
                                            for (int j = 0; j < cart.length(); j++) {
                                                JSONObject p = cart.getJSONObject(j);
                                                order.addProduct(new OrderProductModel(
                                                        p.optString("prod_id"),
                                                        p.optString("prod_name").trim(),
                                                        p.optString("prod_price"),
                                                        p.optString("prod_quantity")
                                                ));
                                            }
                                        }
                                        String sender = details.optString("sender_info", null);
                                        String receiver = details.optString("receiver_info", null);
                                        if (sender != null && sender.length() > 0) order.setSenderInfo(sender);
                                        if (receiver != null && receiver.length() > 0) order.setReceiverInfo(receiver);
                                    }

                                    pendingOrders.add(order);
                                }
                            } catch (Exception e) {
                                Log.e("FetchPendingOrders", "Error parsing order: " + e.getMessage());
                            }
                        }
                    }

                    final List<OrderHistoryModel> finalPendingOrders = pendingOrders;
                    runOnUiThread(() -> {
                        pendingOrdersList.clear();
                        pendingOrdersList.addAll(finalPendingOrders);
                        pendingOrderAdapter.notifyDataSetChanged();

                        // Show/hide the pending orders section
                        if (!finalPendingOrders.isEmpty()) {
                            pendingOrdersSection.setVisibility(View.VISIBLE);
                        } else {
                            pendingOrdersSection.setVisibility(View.GONE);
                        }
                    });

                } catch (Exception e) {
                    Log.e("FetchPendingOrders", "Error processing response: " + e.getMessage());
                    runOnUiThread(() -> {
                        pendingOrdersSection.setVisibility(View.GONE);
                    });
                }
            }
        });
    }

    private void requestSmsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS},
                        SMS_PERMISSION_CODE
                );
            }
        }
    }

    private void fetchAllVendor(String latitude, String longitude) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("latitude", latitude)
                .add("longitude", longitude)
                .build();

        Request request = new Request.Builder()
                .url(Attributes.PRODUCT_Fetch)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FetchVendors", "Network error", e);
                runOnUiThread(() ->
                        showToast("Failed to fetch vendors: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body() != null ? response.body().string() : null;
                    if (responseBody == null) {
                        throw new IOException("Empty response from server");
                    }

                    JSONObject responseObject = new JSONObject(responseBody);
                    Log.d("alllllss", String.valueOf(responseObject));
                    JSONArray shopsArray = responseObject.getJSONArray("shops");

                    JSONArray productsArray = responseObject.getJSONArray("products");

                    if (shopsArray.length() == 0) {
                        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("isCheckIn", 0);
                        editor.apply();
                    } else {
                        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        editor.putInt("isCheckIn", 1);
                        editor.apply();
                    }

                    if (responseObject.has("message") &&
                            "NO_SHOPS_WITHIN_RADIUS".equals(responseObject.getString("message"))) {
                        throw new Exception("No shops within 5 km");
                    }

                    processVendorData(responseObject);
                } catch (Exception e) {
                    Log.e("FetchVendors", "Error processing response", e);
                    runOnUiThread(() -> {
                        showToast(e.getMessage());
                        shopIdList.clear();
                        productIdList.clear();
                        products.clear();
                        shopDetailsList.clear();
                        autoCompleteProducts.setAdapter(null);
                    });
                }
            }
        });
    }

    private void processVendorData(JSONObject responseObject) throws JSONException {
        ArrayList<String> newShopIdList = new ArrayList<>();
        ArrayList<String> newProductIdList = new ArrayList<>();
        ArrayList<String> newProducts = new ArrayList<>();
        ArrayList<HashMap<String, String>> newShopDetailsList = new ArrayList<>();

        // New: Autocomplete suggestions will contain both shop and product names
        ArrayList<String> autoCompleteItems = new ArrayList<>();

        HashSet<String> uniqueBizIds = new HashSet<>();

        JSONArray shopsArray = responseObject.getJSONArray("shops");
        Log.d("shoparray", String.valueOf(shopsArray));
        JSONArray productsArray = responseObject.getJSONArray("products");

        for (int i = 0; i < shopsArray.length(); i++) {
            JSONObject shop = shopsArray.getJSONObject(i);
            String shopId = shop.getString("shop_id");
            String bizId = shop.getString("biz_id");
            String shopLogo = shop.getString("shop_logo");
            String folderName = shop.getString("store_folder");
            String shopName = shop.getString("store_folder");

            // Add shop name to autocomplete
            if (!autoCompleteItems.contains(shopName)) {
                autoCompleteItems.add(shopName);
            }

            if (!uniqueBizIds.contains(bizId)) {
                uniqueBizIds.add(bizId);

                if (!newShopIdList.contains(shopId)) {
                    newShopIdList.add(shopId);

                    HashMap<String, String> shopDetails = new HashMap<>();

                    shopDetails.put("s_id", shop.getString("s_id"));
                    shopDetails.put("biz_id", shop.getString("biz_id"));
                    shopDetails.put("shop_id", shop.getString("shop_id"));
                    shopDetails.put("new_shop_id", shop.getString("new_shop_id"));
                    shopDetails.put("shop_branch", shop.getString("shop_branch"));
                    shopDetails.put("shop_name", shop.getString("shop_name"));
                    shopDetails.put("shop_logo", shop.getString("shop_logo"));
                    shopDetails.put("store_folder", shop.getString("store_folder"));
                    shopDetails.put("shop_image", shop.getString("shop_image"));
                    shopDetails.put("shop_url", shop.getString("shop_url"));
                    shopDetails.put("shop_area_url", shop.getString("shop_area_url"));
                    shopDetails.put("shop_address", shop.getString("shop_address"));
                    shopDetails.put("shop_area", shop.getString("shop_area"));
                    shopDetails.put("shop_city", shop.getString("shop_city"));
                    shopDetails.put("shop_state", shop.getString("shop_state"));
                    shopDetails.put("shop_pincode", shop.getString("shop_pincode"));
                    shopDetails.put("shop_phone", shop.getString("shop_phone"));
                    shopDetails.put("shop_mobile", shop.getString("shop_mobile"));
                    shopDetails.put("shop_mobile2", shop.getString("shop_mobile2"));
                    shopDetails.put("shop_whatsapp", shop.getString("shop_whatsapp"));
                    shopDetails.put("shop_fssai", shop.getString("shop_fssai"));
                    shopDetails.put("shop_gst", shop.getString("shop_gst"));
                    shopDetails.put("shop_admin_name", shop.getString("shop_admin_name"));
                    shopDetails.put("shop_admin_mobile", shop.getString("shop_admin_mobile"));
                    shopDetails.put("shop_admin_whatsapp", shop.getString("shop_admin_whatsapp"));
                    shopDetails.put("shop_dine_in", shop.getString("shop_dine_in"));
                    shopDetails.put("shop_pickup", shop.getString("shop_pickup"));
                    shopDetails.put("shop_delivery", shop.getString("shop_delivery"));
                    shopDetails.put("shop_status", shop.getString("shop_status"));
                    shopDetails.put("shop_added_date", shop.getString("shop_added_date"));
                    shopDetails.put("shop_setting", shop.getString("shop_setting"));
                    shopDetails.put("shop_key", shop.getString("shop_key"));
                    shopDetails.put("image_path", shop.getString("image_path"));
                    shopDetails.put("biz_domain_id", shop.getString("biz_domain_id"));
                    shopDetails.put("latitude", shop.getString("latitude"));
                    shopDetails.put("longitude", shop.getString("longitude"));

                    newShopDetailsList.add(shopDetails);
                }
            }
        }

        for (int i = 0; i < productsArray.length(); i++) {
            JSONObject product = productsArray.getJSONObject(i);
            newProducts.add(product.toString());
            String prodName = product.getString("prod_name");

            // Add product names to autocomplete
            if (!autoCompleteItems.contains(prodName)) {
                autoCompleteItems.add(prodName);
            }

            if (!newProductIdList.contains(prodName)) {
                newProductIdList.add(prodName);
            }
        }

        runOnUiThread(() -> {
            shopIdList = newShopIdList;
            productIdList = newProductIdList;
            products = newProducts;
            shopDetailsList = newShopDetailsList;

            // Set both shop & product names in AutoCompleteView
            if (!autoCompleteItems.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        MainActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        autoCompleteItems
                );
                autoCompleteProducts.setAdapter(adapter);
                autoCompleteProducts.setThreshold(3);
            }
        });
    }

    private void showToast(String message) {
        if (!isFinishing() && !isDestroyed()) {
            runOnUiThread(() ->
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupLocationLogic();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADDRESS && resultCode == RESULT_OK && data != null) {
            double latitude = data.getDoubleExtra("latitude", 0.0);
            double longitude = data.getDoubleExtra("longitude", 0.0);
            String fullAddress = data.getStringExtra("faddress");
            String sublocality = data.getStringExtra("sublocality");

            cityTextView.setText(sublocality != null ? sublocality : "Unknown Area");
            if (fullAddress != null) {
                addressTextView.setText(fullAddress.length() > 50 ? fullAddress.substring(0, 50) + "...." : fullAddress);
            } else {
                addressTextView.setText("Unknown Address");
            }

            saveLocationToPreferences(latitude, longitude, fullAddress, sublocality);
            fetchAllVendor(String.valueOf(latitude), String.valueOf(longitude));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.ic_home);
        updateCartBadge();

        // Start refreshing when activity is visible
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
        }

        // Refresh pending orders when returning to main page
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String cust_id = prefs.getString("cust_id", "");
        if (!cust_id.isEmpty()) {
            fetchPendingOrders(cust_id);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop refreshing when activity is not visible
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop refreshing when activity is destroyed
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }
}