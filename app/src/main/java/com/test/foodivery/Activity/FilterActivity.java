package com.test.foodivery.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import com.test.foodivery.Adapter.OfferAdapter;
import com.test.foodivery.Attributes.Attributes;
import com.test.foodivery.Database.CartDatabaseHelper;
import com.test.foodivery.Fragment.CartFragment;
import com.test.foodivery.Model.OfferModel;
import com.test.foodivery.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FilterActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ImageView btnBack, cartOpen;
    private OfferAdapter adapter;
    private CartDatabaseHelper cartDatabaseHelper;
    private List<OfferModel> imageList;
    private EditText searchEditText;
    private List<OfferModel> originalList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_filter);

        cartDatabaseHelper = new CartDatabaseHelper(this);
        searchEditText = findViewById(R.id.searchEditText);
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.cartRecyclerView);
        cartOpen = findViewById(R.id.cartOpen);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (bottomNavigationView != null) {
            bottomNavigationView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_UNLABELED);
            bottomNavigationView.setSelectedItemId(R.id.ic_home);
            updateCartBadge();
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.ic_home) {
                    showProgressBar();
                    startActivity(new Intent(FilterActivity.this, MainActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.ic_history) {
                    showProgressBar();
                    startActivity(new Intent(FilterActivity.this, HistoryViewActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.ic_profile) {
                    showProgressBar();
                    Intent intent = new Intent(FilterActivity.this, ProfileActivity.class);
                    ArrayList<String> shopIdList = getIntent().getStringArrayListExtra("SHOP_ID_LIST");
                    ArrayList<String> shopDetailsList = getIntent().getStringArrayListExtra("SHOP_DETAILS_LIST");
                    if (shopIdList != null && shopDetailsList != null) {
                        intent.putStringArrayListExtra("SHOP_ID_LIST", shopIdList);
                        intent.putStringArrayListExtra("SHOP_DETAILS_LIST", shopDetailsList);
                    }
                    startActivity(intent);
                    finish();
                    return true;
                } else if (id == R.id.ic_cart) {
                    swapFragment(new CartFragment());
                    showProgressBar();
                    Intent intent = new Intent(FilterActivity.this, MainActivity.class);
                    intent.putExtra("OPEN_CART", true);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (id == R.id.ic_productView) {
                    showProgressBar();
                    Intent intent = new Intent(FilterActivity.this, OfferActivity.class);
                    intent.putExtra("name", "all");
                    ArrayList<String> shopIdList = getIntent().getStringArrayListExtra("SHOP_ID_LIST");
                    ArrayList<String> SHOP_DETAILS_LIST = getIntent().getStringArrayListExtra("SHOP_DETAILS_LIST");
                    if (shopIdList != null && SHOP_DETAILS_LIST != null) {
                        intent.putStringArrayListExtra("SHOP_ID_LIST", shopIdList);
                        intent.putStringArrayListExtra("SHOP_DETAILS_LIST", SHOP_DETAILS_LIST);
                    }
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            });
            bottomNavigationView.setOnItemReselectedListener(item -> {});
        }

        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        runOnUiThread(this::updateCartBadge); // Always refresh badge on resume
    }

    public void updateCartBadge() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            int cartCount = cartDatabaseHelper.getCartItemCount();
            BadgeDrawable badge = bottomNavigationView.getOrCreateBadge(R.id.ic_cart);
            if (cartCount > 0) {
                badge.setVisible(true);
                badge.setNumber(cartCount);
            } else {
                badge.setVisible(false);
                badge.clearNumber();
                bottomNavigationView.removeBadge(R.id.ic_cart);
            }
        }
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

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        cartOpen.setOnClickListener(v -> {
            Intent intent = new Intent(FilterActivity.this, MainActivity.class);
            intent.putExtra("OPEN_CART", true);
            startActivity(intent);
            runOnUiThread(this::updateCartBadge);  // Refresh after open
        });
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        imageList = new ArrayList<>();
        adapter = new OfferAdapter(this, imageList);
        recyclerView.setAdapter(adapter);

        String selected = getIntent().getStringExtra("selected");
        ArrayList<String> shopIdList = getIntent().getStringArrayListExtra("SHOP_ID_LIST");
        ArrayList<String> SHOP_DETAILS_LIST = getIntent().getStringArrayListExtra("SHOP_DETAILS_LIST");
        fetchSearch(selected, shopIdList, SHOP_DETAILS_LIST);
        Log.d("SHOP_DETAILS_LISTllll", String.valueOf(SHOP_DETAILS_LIST));
    }

    private void fetchSearch(String selected, ArrayList<String> shopIdList, ArrayList<String> shopLogo) {
        OkHttpClient client = new OkHttpClient();
        String shopIds = TextUtils.join(",", shopIdList);
        Log.d("DEBUG_API", "Sending productName: " + selected);
        Log.d("DEBUG_API", "Sending shopId: " + shopIds);

        RequestBody requestBody = new FormBody.Builder()
                .add("name", selected)
                .add("id", shopIds)
                .build();

        Request request = new Request.Builder()
                .url(Attributes.PRODUCT_RELATED)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_ERROR", "Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("API_RESPONSE", "Response: " + responseBody);

                try {
                    JSONArray imagesArray = new JSONArray(responseBody);
                    imageList.clear();

                    for (int i = 0; i < imagesArray.length(); i++) {
                        JSONObject imageObject = imagesArray.getJSONObject(i);
                        Integer id = imageObject.getInt("prod_id");
                        String imageName = imageObject.getString("prod_image");
                        String folderName = imageObject.getString("image_path");
                        String fullImageUrl = Attributes.IMAGE_BASE_URL + folderName + "/" + imageName;
                        Integer shopIds = imageObject.getInt("shop_id");
                        Integer gst = imageObject.getInt("prod_gst_rate");
                        Integer gstInc = imageObject.getInt("prod_gst_inc");
                        Integer bizId = imageObject.getInt("biz_id");

                        String storeFolder = "", biz_id = "", logoName = "";
                        String folderNames = "", logoN = "";
                        String logofullImageUrl = "";

                        for (int index = 0; index < shopLogo.size(); index++) {
                            String jsonString = shopLogo.get(index).trim();
                            try {
                                if (jsonString.startsWith("{")) {
                                    JSONObject shopObject = new JSONObject(jsonString);
                                    biz_id = shopObject.getString("biz_id");
                                    storeFolder = shopObject.getString("store_folder");
                                    logoName = shopObject.getString("shop_logo");
                                    if (String.valueOf(bizId).equals(biz_id)) {
                                        folderNames = storeFolder;
                                        logoN = logoName;
                                        logofullImageUrl = Attributes.Shop_Logo + "images/store/" + folderNames + "/" + logoN;
                                    }
                                    Log.d("StoreFolder", "Object in shopLogo index " + index + " : " + biz_id + ": " + storeFolder + " " + logoName);
                                } else {
                                    Log.w("JSON_WARNING", "Unknown JSON format at shopLogo index " + index);
                                }
                            } catch (JSONException e) {
                                Log.e("JSON_ERROR", "Failed to parse shopLogo element at index " + index + ": " + e.getMessage());
                            }
                        }

                        float productPrice = (float) imageObject.getDouble("prod_price");
                        float productOffer = (float) imageObject.getDouble("prod_offer");
                        String productTitle = imageObject.getString("prod_name");
                        String bizIds = imageObject.getString("biz_id");

                        imageList.add(new OfferModel(fullImageUrl, String.valueOf(id), String.valueOf(shopIds), productTitle,
                                String.valueOf(productPrice), String.valueOf(productOffer), String.valueOf(gst), String.valueOf(gstInc), logofullImageUrl,
                                folderNames, logoN, bizIds));
                    }

                    runOnUiThread(() -> {
                        originalList = new ArrayList<>(imageList);
                        adapter.updateList(imageList);

                        updateCartBadge(); // Refresh badge after products update (or after any add/remove in your actual app logic)
                    });
                } catch (JSONException e) {
                    Log.e("API_ERROR", "JSON Parsing Error: " + e.getMessage());
                }
            }
        });
    }
}
