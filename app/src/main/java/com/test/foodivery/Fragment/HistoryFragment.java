package com.test.foodivery.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.test.foodivery.Adapter.OrderHistoryAdapter;
import com.test.foodivery.Attributes.Attributes;
import com.test.foodivery.Model.OrderHistoryModel;
import com.test.foodivery.Model.OrderProductModel;
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

public class HistoryFragment extends Fragment {

    private RecyclerView rvOrders;
    private View loadingView;
    private View emptyState;
    private TabLayout tabLayout;
    private OrderHistoryAdapter adapter;
    private final List<OrderHistoryModel> currentOrders = new ArrayList<>();
    private final List<OrderHistoryModel> allOrders = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        String custId = checkUserSession();
        fetchHistory(custId);
    }

    private void initViews(View root) {
        rvOrders = root.findViewById(R.id.rvOrders);
        loadingView = root.findViewById(R.id.loadingView);
        emptyState = root.findViewById(R.id.emptyState);
        tabLayout = root.findViewById(R.id.tabLayout);

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
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
    }

    private void fetchHistory(String custId) {
        OkHttpClient client = new OkHttpClient();
        String url = Attributes.Fetch_History + "?cust_id=" + custId;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-api","C4CCBC195C709FB4SE98AA8")
                .build();

        showLoading(true);
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FetchAddress", "Network error: " + e.getMessage());
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
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
                    JSONArray ordersArray = responseObject.optJSONArray("orders");
                    JSONArray allArray = responseObject.optJSONArray("alldata");

                    parseOrdersIntoList(ordersArray, currentOrders, true);
                    parseOrdersIntoList(allArray, allOrders, false);

                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        applyTab();
                    });
                } catch (Exception e) {
                    Log.e("FetchAddress", "Error processing address: " + e.getMessage());
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
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
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    private String checkUserSession() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", 0);
        String cust_id = prefs.getString("cust_id", "");
        Log.d("UserSession", "Customer ID: " + cust_id);
        return cust_id;
    }
}


