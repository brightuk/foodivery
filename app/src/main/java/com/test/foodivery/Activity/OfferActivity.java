package com.test.foodivery.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import com.test.foodivery.Adapter.OfferAdapter;
import com.test.foodivery.Attributes.Attributes;
import com.test.foodivery.Database.CartDatabaseHelper;
import com.test.foodivery.Model.OfferModel;
import com.test.foodivery.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OfferActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageView btnBack, cartOpen;
    private OfferAdapter adapter;
    private List<OfferModel> imageList;
    private EditText searchEditText;
    private ProgressBar progressBar;
    private List<OfferModel> originalList = new ArrayList<>();
    private CartDatabaseHelper cartDatabaseHelper;

    // Refresh Handler
    private Handler refreshHandler;
    private Runnable refreshRunnable;
    private ArrayList<String> shopIdList,SHOP_Logo_LIST;
    private static final long REFRESH_INTERVAL = 5000; // Increased to 5 seconds to reduce API calls
    private String names;
    private boolean isRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        names = getIntent().getStringExtra("name");
        SHOP_Logo_LIST = getIntent().getStringArrayListExtra("SHOP_DETAILS_LIST");
        Log.d("hhhhhhkk", String.valueOf(SHOP_Logo_LIST));

        // Check for shops before proceeding
        shopIdList = getIntent().getStringArrayListExtra("SHOP_ID_LIST");
        Log.d("hhhhhhkk",String.valueOf(shopIdList));
        if (shopIdList == null || shopIdList.isEmpty()) {
            Toast.makeText(this, "No shops available in your area. Please try a different location.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_offer);

        // Initialize database helper
        cartDatabaseHelper = new CartDatabaseHelper(this);

        initializeViews();
        setupRecyclerView();
        setupClickListeners();

        // Initialize refresh handler
        setupRefreshHandler();

        showLoading();
        fetchAllOffer(names, shopIdList, SHOP_Logo_LIST);
        setupSearch();
    }

    private void setupRefreshHandler() {
        refreshHandler = new Handler(Looper.getMainLooper());
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRefreshing) {
                    refreshData();
                }
                refreshHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        };
    }

    private void refreshData() {
        // Only update cart badge, not the entire product list

        fetchAllOffer(names, shopIdList, SHOP_Logo_LIST);
        updateCartBadge();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start refreshing when activity is visible
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
        }
        updateCartBadge(); // Always refresh badge on resume
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

    private void updateCartBadge() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            int cartCount = cartDatabaseHelper.getCartItemCount();
            if (cartCount > 0) {
                // Create or update badge
                com.google.android.material.badge.BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.ic_cart);
                badge.setVisible(true);
                badge.setNumber(cartCount);
            } else {
                // Remove badge if cart is empty
                bottomNavigationView.removeBadge(R.id.ic_cart);
            }
        }
    }

    private void initializeViews() {
        searchEditText = findViewById(R.id.searchEditText);
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.cartRecyclerView);
        cartOpen = findViewById(R.id.cartOpen);
        progressBar = findViewById(R.id.progressBar);

        // Initialize Bottom Navigation
        setupBottomNavigation();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        imageList = new ArrayList<>();
        adapter = new OfferAdapter(this, imageList);
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        cartOpen.setOnClickListener(v -> {
            Intent intent = new Intent(OfferActivity.this, MainActivity.class);
            intent.putExtra("OPEN_CART", true);
            startActivity(intent);
        });
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void filterProducts(String query) {
        List<OfferModel> filteredList = new ArrayList<>();

        if (TextUtils.isEmpty(query)) {
            filteredList.addAll(originalList);
        } else {
            for (OfferModel item : originalList) {
                if (item.getProd_name().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }

        adapter.updateList(filteredList);
    }

    private void fetchAllOffer(String names, ArrayList<String> shopIdList, ArrayList<String> shopLogo) {
        isRefreshing = true;
        OkHttpClient client = new OkHttpClient();
        String shopIds = TextUtils.join(",", shopIdList);

        RequestBody requestBody = new FormBody.Builder()
                .add("name", names)
                .add("id", shopIds)
                .build();

        Request request = new Request.Builder()
                .url(Attributes.API_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    isRefreshing = false;
                    hideLoading();
                    Toast.makeText(OfferActivity.this,
                            "Failed to load offers. Please try again.",
                            Toast.LENGTH_LONG).show();
                    Log.e("API_ERROR", "Request failed: " + e.getMessage());
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    List<OfferModel> tempList = new ArrayList<>();
                    JSONObject responseObject = new JSONObject(responseBody);
                    Log.d("jjjjfddsds",String.valueOf(responseObject));
                    JSONArray imagesArray = responseObject.getJSONArray("data");

                    for (int i = 0; i < imagesArray.length(); i++) {
                        JSONObject imageObject = imagesArray.getJSONObject(i);
                        Integer id = imageObject.getInt("prod_id");
                        String imageName = imageObject.getString("prod_image");
                        Integer shopIds = imageObject.getInt("shop_id");
                        String folderName = imageObject.getString("image_path");
                        String fullImageUrl = Attributes.IMAGE_BASE_URL + folderName + "/" + imageName;
                        Integer bizId = imageObject.getInt("biz_id");
                        String storeFolder = "", biz_id = "", logoName = "";
                        String folderNames = "", logoN = "";
                        String logofullImageUrl = "";

                        for (int index = 0; index < shopLogo.size(); index++) {
                            String jsonString = shopLogo.get(index).trim();

                            try {
                                if (jsonString.startsWith("{")) {
                                    // It's a JSON Object
                                    JSONObject shopObject = new JSONObject(jsonString);
                                    biz_id = shopObject.getString("biz_id");
                                    storeFolder = shopObject.getString("store_folder");
                                    logoName = shopObject.getString("shop_logo");
                                    if (String.valueOf(bizId).equals(biz_id)){
                                        folderNames=storeFolder;
                                        logoN = logoName;
                                        logofullImageUrl = Attributes.Shop_Logo + "images/store/" + folderNames + "/" + logoN;
                                    }
                                    Log.d("StoreFolder", "Object in shopLogo index " + index +" : "+biz_id+ ": " + storeFolder+" "+logoName);
                                } else {
                                    Log.w("JSON_WARNING", "Unknown JSON format at shopLogo index " + index);
                                }
                            } catch (JSONException e) {
                                Log.e("JSON_ERROR", "Failed to parse shopLogo element at index " + index + ": " + e.getMessage());
                            }
                        }

                        Log.d("jaaakkkk",logofullImageUrl);

                        Integer gst = imageObject.getInt("prod_gst_rate");
                        Integer gstInc = imageObject.getInt("prod_gst_inc");
                        float productPrice = (float) imageObject.getDouble("prod_price");
                        float productOffer = (float) imageObject.getDouble("prod_offer");
                        String productTitle = imageObject.getString("prod_name");
                        String bizIds = imageObject.getString("biz_id");

                        tempList.add(new OfferModel(
                                fullImageUrl,
                                String.valueOf(id),
                                String.valueOf(shopIds),
                                productTitle,
                                String.valueOf(productPrice),
                                String.valueOf(productOffer),
                                String.valueOf(gst),
                                String.valueOf(gstInc),
                                logofullImageUrl,
                                folderNames,
                                logoN,
                                bizIds
                        ));
                    }

                    runOnUiThread(() -> {
                        isRefreshing = false;
                        imageList.clear();
                        imageList.addAll(tempList);
                        originalList = new ArrayList<>(imageList);
                        adapter.updateList(imageList);
                        hideLoading();
                        updateCartBadge(); // Update cart badge after loading products
                    });

                } catch (JSONException e) {
                    runOnUiThread(() -> {
                        isRefreshing = false;
                        hideLoading();
                        Toast.makeText(OfferActivity.this,
                                "Error parsing data",
                                Toast.LENGTH_SHORT).show();
                        Log.e("API_ERROR", "JSON Parsing Error: " + e.getMessage());
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        isRefreshing = false;
                        hideLoading();
                        Toast.makeText(OfferActivity.this,
                                "Unexpected error occurred",
                                Toast.LENGTH_SHORT).show();
                        Log.e("API_ERROR", "Unexpected error: " + e.getMessage());
                    });
                }
            }
        });
    }

    private void parseShopDetails(ArrayList<String> shopLogoList) {
        try {
            // Assuming shopLogoList.get(0) contains the JSON array string
            JSONArray shopArray = new JSONArray(shopLogoList.get(0));

            for (int i = 0; i < shopArray.length(); i++) {
                JSONObject shop = shopArray.getJSONObject(i);

                String bizId = shop.getString("biz_id");
                String shopLogo = shop.getString("shop_logo");
                String storeFolder = shop.getString("store_folder");

                Log.d("ShopDetails",
                        "BizID: " + bizId +
                                ", Logo: " + shopLogo +
                                ", Folder: " + storeFolder);
            }
        } catch (Exception e) {
            Log.e("JSON_ERROR", "Failed to parse shop details", e);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setVisibility(View.GONE);
        if (bottomNavigationView != null) {
            bottomNavigationView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_UNLABELED);
            bottomNavigationView.setSelectedItemId(R.id.ic_productView); // Set to product view tab

            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.ic_home) {
                    showProgressBar();
                    Intent intent = new Intent(OfferActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.ic_history) {

                    showProgressBar();
                    Intent intent = new Intent(OfferActivity.this, HistoryViewActivity.class);
                    // Pass shop data to HistoryViewActivity
                    ArrayList<String> shopIdList = getIntent().getStringArrayListExtra("SHOP_ID_LIST");
                    ArrayList<String> shopDetailsList = getIntent().getStringArrayListExtra("SHOP_DETAILS_LIST");
                    if (shopIdList != null && shopDetailsList != null) {
                        intent.putStringArrayListExtra("SHOP_ID_LIST", shopIdList);
                        intent.putStringArrayListExtra("SHOP_DETAILS_LIST", shopDetailsList);
                    }
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.ic_cart) {
                    showProgressBar();
                    Intent intent = new Intent(OfferActivity.this, MainActivity.class);
                    intent.putExtra("OPEN_CART", true);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.ic_productView) {
                    // Already on product view/offers page
                    return true;
                } else if (itemId == R.id.ic_profile) {
                    showProgressBar();
                    Intent intent = new Intent(OfferActivity.this, ProfileActivity.class);
                    // Pass shop data to ProfileActivity
                    ArrayList<String> shopIdList = getIntent().getStringArrayListExtra("SHOP_ID_LIST");
                    ArrayList<String> shopDetailsList = getIntent().getStringArrayListExtra("SHOP_DETAILS_LIST");
                    if (shopIdList != null && shopDetailsList != null) {
                        intent.putStringArrayListExtra("SHOP_ID_LIST", shopIdList);
                        intent.putStringArrayListExtra("SHOP_DETAILS_LIST", shopDetailsList);
                    }
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            });
            bottomNavigationView.setOnItemReselectedListener(item -> {
                // Handle reselection if needed
            });
            // Update cart badge initially
            updateCartBadge();
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
}