package com.test.foodivery.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.test.foodivery.Adapter.OrderHistoryAdapter;
import com.test.foodivery.Model.OrderHistoryModel;
import com.test.foodivery.Model.OrderProductModel;
import com.test.foodivery.Attributes.Attributes;
import com.test.foodivery.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HistoryViewActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private View loadingView;
    private View emptyState;
    private TabLayout tabLayout;
    private OrderHistoryAdapter adapter;
    private final List<OrderHistoryModel> currentOrders = new ArrayList<>();
    private final List<OrderHistoryModel> allOrders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history_view);
        initViews();
        String custId=checkUserSession();
        fetchHistory(custId);
    }

    private void initViews() {
        rvOrders = findViewById(R.id.rvOrders);
        loadingView = findViewById(R.id.loadingView);
        emptyState = findViewById(R.id.emptyState);
        tabLayout = findViewById(R.id.tabLayout);
        View back = findViewById(R.id.backButton);
        back.setOnClickListener(v -> finish());
        if (back != null) back.setOnClickListener(v -> onBackPressed());

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderHistoryAdapter(new ArrayList<>());
        rvOrders.setAdapter(adapter);

        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) { applyTab(); }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) { applyTab(); }
            });
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_UNLABELED);
            bottomNavigationView.setSelectedItemId(R.id.ic_history);
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.ic_home) {
                    showProgressBar();
                    startActivity(new android.content.Intent(HistoryViewActivity.this, MainActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.ic_history) {
                    return true;
                } else if (id == R.id.ic_profile) {
                    showProgressBar();
                    Intent intent = new Intent(HistoryViewActivity.this, ProfileActivity.class);
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
                } else if (id == R.id.ic_cart) {
                    showProgressBar();
                    Intent intent = new Intent(HistoryViewActivity.this, MainActivity.class);
                    intent.putExtra("OPEN_CART", true);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (id == R.id.ic_productView) {
                    // Navigate to offers page with shop data
                    showProgressBar();
                    Intent intent = new Intent(HistoryViewActivity.this, OfferActivity.class);
                    intent.putExtra("name", "all");
                    
                    // Get shop data from intent extras
                    ArrayList<String> shopIdList = getIntent().getStringArrayListExtra("SHOP_ID_LIST");
                    ArrayList<String> shopDetailsList = getIntent().getStringArrayListExtra("SHOP_DETAILS_LIST");
                    
                    if (shopIdList != null && shopDetailsList != null) {
                        intent.putStringArrayListExtra("SHOP_ID_LIST", shopIdList);
                        intent.putStringArrayListExtra("SHOP_DETAILS_LIST", shopDetailsList);
                        startActivity(intent);
                        finish();
                    } else {
                        // Fallback: navigate to MainActivity if no shop data
                        startActivity(new Intent(HistoryViewActivity.this, MainActivity.class));
                        finish();
                    }
                    return true;
                }
                return false;
            });
            bottomNavigationView.setOnItemReselectedListener(item -> {});
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

    private void fetchHistory(String custId) {
        Log.d("FetchAddress", "Fetching primary address for customer: " + custId);

        OkHttpClient client = new OkHttpClient();

        // Append the query parameter to the URL for GET
        String url = Attributes.Fetch_History + "?cust_id=" + custId;

        Request request = new Request.Builder()
                .url(url)
                .get() // GET method

                .addHeader("x-api","C4CCBC195C709FB4SE98AA8")
                .build();

        showLoading(true);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FetchAddress", "Network error: " + e.getMessage());
                runOnUiThread(() -> {
                    showToast("Failed to fetch orders");
                    showLoading(false);
                    showEmpty(true);

                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    JSONObject responseObject = new JSONObject(responseBody);
                    Log.d("FetchAddressdddddd", String.valueOf(responseObject));


                    System.out.println(responseObject);
                    JSONArray ordersArray = responseObject.optJSONArray("orders");
                    JSONArray allArray = responseObject.optJSONArray("alldata");
                    Log.d("FetchHistory", "Orders count: " + (ordersArray != null ? ordersArray.length() : 0));

                    parseOrdersIntoList(ordersArray, currentOrders, true);
                    parseOrdersIntoList(allArray, allOrders, false);

                    runOnUiThread(() -> {
                        showLoading(false);
                        applyTab();
                    });



                } catch (Exception e) {
                    Log.e("FetchAddress", "Error processing address: " + e.getMessage());
                    runOnUiThread(() -> {
                        showLoading(false);
                        showEmpty(true);
                    });
                }
            }
        });
    }

    private void parseOrdersIntoList(JSONArray jsonArray, List<OrderHistoryModel> target, boolean includeProducts) {
        target.clear();
        if (jsonArray == null) return;
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject obj = jsonArray.getJSONObject(i);
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
                order.setPaymentStatus(obj.optString("payment_status"));
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
                // Prefer explicit url keys if backend provides any of these
                String invoiceUrl = obj.optString("invoice_url", null);
                if (invoiceUrl == null || invoiceUrl.length() == 0) invoiceUrl = obj.optString("invoice_pdf", null);
                if (invoiceUrl == null || invoiceUrl.length() == 0) invoiceUrl = obj.optString("invoice_link", null);
                order.setInvoiceUrl(invoiceUrl);

                // sender/receiver info (top-level fallbacks commonly provided by backend)
                String senderTop = obj.optString("caller_info", null);
                String receiverTop = obj.optString("delivery_info", null);
                if (senderTop != null && senderTop.length() > 0) order.setSenderInfo(senderTop);
                if (receiverTop != null && receiverTop.length() > 0) order.setReceiverInfo(receiverTop);

                if (includeProducts) {
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
                        // sender/receiver details inside details if present
                        String sender = details.optString("sender_info", null);
                        String receiver = details.optString("receiver_info", null);
                        if (sender != null && sender.length() > 0) order.setSenderInfo(sender);
                        if (receiver != null && receiver.length() > 0) order.setReceiverInfo(receiver);
                    }
                }

                target.add(order);
            } catch (Exception ignore) {}
        }
    }

    private void applyTab() {
        if (tabLayout == null) {
            adapter.updateData(currentOrders);
            showEmpty(currentOrders.isEmpty());
            return;
        }
        int pos = tabLayout.getSelectedTabPosition();
        List<OrderHistoryModel> data = pos == 0 ? currentOrders : allOrders;
        adapter.updateData(data);
        showEmpty(data.isEmpty());
    }

    private void showLoading(boolean show) {
        if (loadingView != null) loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        if (rvOrders != null) rvOrders.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmpty(boolean show) {
        if (emptyState != null) emptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        if (rvOrders != null) rvOrders.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    private void showToast(String message) {
        if (!isFinishing() && !isDestroyed()) {
            runOnUiThread(() ->
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show());
        }
    }

    private String checkUserSession() {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String cust_id = prefs.getString("cust_id", "");
        Log.d("UserSession", "Customer ID: " + cust_id);
        return cust_id;

    }

}