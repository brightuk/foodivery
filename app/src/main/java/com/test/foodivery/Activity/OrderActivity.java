package com.test.foodivery.Activity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.test.foodivery.Adapter.OrderAdapter;
import com.test.foodivery.Attributes.Attributes;
import com.test.foodivery.Database.CartDatabaseHelper;
import com.test.foodivery.Model.CartModel;
import com.test.foodivery.R;
import com.test.foodivery.Services.FCMNotificationSender;
import com.test.foodivery.Services.MyFirebaseMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
public class OrderActivity extends AppCompatActivity {
    private static final String TAG = "OrderActivity";
    private RecyclerView recyclerView;
    private CartDatabaseHelper dbHelper;
    private TextView addressUsername, addressText,totalAmount, subtotalTextView, gstTextView, totalTextView, placeOrder,
            platformFee, platformFeeGst, deliveryCharge;
    private int platformFeeRate = 10;
    private int deliveryChargeRate = 25;
    private boolean isAddressAvailable = false;
    private RadioGroup paymentMethodRadioGroup;
    private RadioButton cashOnDeliveryRadioButton, onlinePayment;
    private String shopId;
    private Spinner spinner2;
    private double subtotal = 0.0;
    private double totalGst = 0.0;
    private double total = 0.0;
    private String custId;
    private String selectedTiming;
    private final Handler handler = new Handler();
    private String seqOrderId, orderId, orderKey,api_id,api_secret,merchant_id,access_code,working_key,cust_name,cust_address,cust_apiAddress,custName,custAddress,apiCustAddress,custPhoneNo,custcity;
    private int pendingCartPosts = 0;
    private String pendingDeliveryType;
    private double lastTotalAmount = 0.0;
    private FirebaseFirestore db;
    // Removed lat, lon, api_id, api_secret since apiKey() is commented
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order);
        // Initialize views
        subtotalTextView = findViewById(R.id.subTotal);
        gstTextView = findViewById(R.id.subGst);
        totalTextView = findViewById(R.id.grandTotal);
        placeOrder = findViewById(R.id.placeOrder);
//        addressChange = findViewById(R.id.addressAddressChange);
        recyclerView = findViewById(R.id.orderRecyclerview);
        platformFee = findViewById(R.id.platformFee);
        platformFeeGst = findViewById(R.id.platformFeeGst);
        deliveryCharge = findViewById(R.id.deliveryCharge);
//        deliveryChargeGst = findViewById(R.id.deliveryChargeGST);
        addressUsername = findViewById(R.id.addressUserName);
        addressText = findViewById(R.id.addressText);
        ImageView backBtn = findViewById(R.id.backButton);
        spinner2 = findViewById(R.id.spinnerTime);
        totalAmount=findViewById(R.id.totalAmount);
        paymentMethodRadioGroup = findViewById(R.id.paymentMethodRadioGroup);
        cashOnDeliveryRadioButton = findViewById(R.id.cashOnDeliveryRadioButton);
        onlinePayment = findViewById(R.id.onlinePayment);
        db = FirebaseFirestore.getInstance();
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedTiming = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTiming = null; // No selection
            }
        });


        backBtn.setOnClickListener(v -> finish());
        // Initialize database helper
        dbHelper = new CartDatabaseHelper(this);
        List<CartModel> cartItems = dbHelper.getAllCartItems();
        // Set up RecyclerView with adapter
        OrderAdapter adapter = new OrderAdapter(this, cartItems, dbHelper);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        platformFee.setText("₹" + platformFeeRate);
        deliveryCharge.setText("₹" + deliveryChargeRate);
        // In onCreate after findViewById calls you already have:
        LinearLayout billDetailsHeader = findViewById(R.id.billDetailsHeader);
        LinearLayout billDetailsContent = findViewById(R.id.billDetailsContent);
        ImageView expandIcon = findViewById(R.id.expandIcon);
        billDetailsHeader.setOnClickListener(v -> {
            boolean expanded = billDetailsContent.getVisibility() == View.VISIBLE;
            billDetailsContent.setVisibility(expanded ? View.GONE : View.VISIBLE);
            expandIcon.animate().rotation(expanded ? 0f : 180f).setDuration(150).start();
        });
//        createNotificationChannel();
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        custId = prefs.getString("cust_id", "");
        // First spinner setup
        Spinner spinner = findViewById(R.id.spinner);
        String[] items = {"Today", "Tomorrow"};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                return view;
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
                return view;
            }
        };
        spinner.setAdapter(adapter2);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // no-op but can handle if needed
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // no-op
            }
        });
        // Fetch timing info for shops in cart
        List<CartModel> cartItemList = dbHelper.getAllCartItems();
        for (CartModel item : cartItemList) {
            String shopIdTmp = item.getShopId();
            if (shopIdTmp != null && !shopIdTmp.isEmpty()) {
                fetchTimeing(shopIdTmp);
            }
        }
//         Setup addressChange click once here; text dynamically updated elsewhere
//        addressChange.setOnClickListener(v -> {
//            if (isAddressAvailable) {
//                Intent intent = new Intent(OrderActivity.this, AddressFindActivity.class);
//                startActivity(intent);
//            } else {
//                Intent intent = new Intent(OrderActivity.this, AddressActivity.class);
//                startActivity(intent);
//            }
//        });
        apiKey();
        calculateTotals();
        // Place Order button click
        placeOrder.setOnClickListener(v -> {


            if (!isAddressAvailable) {
                Toast.makeText(OrderActivity.this, "Please add the address", Toast.LENGTH_SHORT).show();
                return;
            }
            List<CartModel> cartItemListInner = dbHelper.getAllCartItems();
            if (cartItemListInner.isEmpty()) {
                Toast.makeText(OrderActivity.this, "Cart is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            shopId = cartItemListInner.get(0).getShopId();
            if (shopId == null || shopId.isEmpty()) {
                Toast.makeText(OrderActivity.this, "ShopId is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            // Reset subtotal and totalGst before calculation
            subtotal = 0.0;
            totalGst = 0.0;
            for (CartModel item : cartItemListInner) {
                int qty = Math.max(1, item.getQuantity());
                double unitPrice = Double.parseDouble(item.getPrice());
                double linePrice = unitPrice * qty;
                double gstRate = Double.parseDouble(item.getProductGst());
                String gstInc = item.getGstInc();
                double itemGst = linePrice * (gstRate / 100.0);
                if ("1".equals(gstInc)) {
                    subtotal += linePrice; // GST already included in line price
                } else {
                    subtotal += linePrice - itemGst; // add price excluding GST
                }
                totalGst += itemGst;
            }
            double platformGstCalculate = platformFeeRate * (18.0 / 100);
            total = subtotal + totalGst + platformFeeRate + platformGstCalculate + deliveryChargeRate ;
            // Safe spinner2 selection check
            selectedTiming = (spinner2.getSelectedItem() != null) ? spinner2.getSelectedItem().toString() : "default_time";
            if (cashOnDeliveryRadioButton.isChecked()) {
                sendOrderDataToAPI(shopId, subtotal, totalGst, total, cartItemListInner, custId, selectedTiming, "cod");
//                sendOrderNotification();

            } else if (onlinePayment.isChecked()) {
                int amountInPaise = (int) Math.round(total * 100);
                String amountString = String.format("%.2f", (amountInPaise / 100.0));
                // Send order data first and launch payment after success callback (see sendOrderDataToAPI)
                sendOrderDataToAPI(shopId, subtotal, totalGst, total, cartItemListInner, custId, selectedTiming, "online");
                // NOTE: Launching payment activity moved inside sendOrderDataToAPI after order is created
            } else {
                Toast.makeText(OrderActivity.this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            }
        });
        fetchPrimaryAddress(custId);
    }


    private void sendOrderDataToAPI(String shopId, double subtotal, double totalGst, double total,
                                    List<CartModel> cartItems, String custid, String selectedTiming, String deliveryType) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("shop_id", shopId)
                .add("subtotal", String.valueOf(subtotal))
                .add("total_gst", String.valueOf(totalGst))
                .add("total", String.valueOf(total))
                .add("custid", custid)
                .add("timing", selectedTiming)
                .add("deliveryType", deliveryType)
                .build();

        Request request = new Request.Builder()
                .url(Attributes.Add_Order)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(OrderActivity.this, "API Request Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Order API Request failed: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = null;
                try {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() ->
                                Toast.makeText(OrderActivity.this, "API Response Unsuccessful: " + response.code(), Toast.LENGTH_LONG).show());
                        return;
                    }

                    if (response.body() != null) {
                        responseBody = response.body().string();
                    }

                    if (responseBody == null) {
                        runOnUiThread(() -> Toast.makeText(OrderActivity.this, "Empty order API response", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    Log.d(TAG, "Order API Response: " + responseBody);

                    try {
                        JSONObject responseObject = new JSONObject(responseBody);
                        if (responseObject.optBoolean("status", false)) {
                            seqOrderId = String.valueOf(responseObject.optInt("seq_order_id"));
                            orderId = responseObject.optString("order_id");
                            orderKey = responseObject.optString("order_key");

                            // Track pending cart item posts and defer next steps until all succeed
                            pendingCartPosts = cartItems.size();
                            pendingDeliveryType = deliveryType;
                            lastTotalAmount = total;

                            // Get FCM token and send notification
                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    String token = task.getResult();
                                    sendNotification(shopId, token, orderId); // Pass orderId for notification
                                    Log.d("FCMd", token);
                                } else {
                                    Log.e("FCMd", "Failed to get FCM token");
                                }
                            });

                            // After getting order details, send cart items
                            for (CartModel item : cartItems) {
                                sendOrderCart(item.getProductId(), item.getProductName(), item.getPrice(),
                                        item.getSelectedOptions(), item.getProductGst(), item.getGstInc(),
                                        item.getGetallVariantIds(), seqOrderId, orderId, orderKey,
                                        item.getGetproduct_Weight(), item.getQuantity());
                            }

                            runOnUiThread(() -> {
                                String notificationTitle = "Order Placed Successfully!";
                                String notificationMessage = "Your order #" + orderId + " has been placed successfully.";
                                // Your notification code here
                            });
                        } else {
                            String errorMessage = responseObject.optString("message", "Order API error: Status false");
                            runOnUiThread(() ->
                                    Toast.makeText(OrderActivity.this, errorMessage, Toast.LENGTH_SHORT).show()
                            );
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON Exception while parsing order API response: " + e.getMessage());
                        runOnUiThread(() ->
                                Toast.makeText(OrderActivity.this, "JSON Parsing Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }
                } finally {
                    response.close();
                }
            }
        });
    }

    public void sendNotification(String shopId, String token, String orderId) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("shopId", shopId)
                .add("token", token)
                .add("orderId", orderId) // Add orderId to identify the order
                .build();

        Request request = new Request.Builder()
                .url(Attributes.SEND_NOTIFICATION_API)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(OrderActivity.this, "Notification API Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Log.e("API_ERROR", "Notification request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        Log.e("API_ERROR", "Notification API unsuccessful: " + response.code());
                        return;
                    }

                    if (response.body() != null) {
                        String responseBody = response.body().string();
                        Log.d("NOTIFICATION_RESPONSE", responseBody);

                        try {
                            JSONObject responseObject = new JSONObject(responseBody);
                            boolean status = responseObject.optBoolean("status", false);
                            if (!status) {
                                Log.e("NOTIFICATION_ERROR", responseObject.optString("message", "Unknown error"));
                            }
                        } catch (JSONException e) {
                            Log.e("JSON_ERROR", "Notification response parsing failed: " + e.getMessage());
                        }
                    }
                } finally {
                    response.close();
                }
            }
        });
    }
    private synchronized void handleCartPostCompletion() {
        if (pendingCartPosts > 0) {
            pendingCartPosts--;
        }
        if (pendingCartPosts == 0) {
            runOnUiThread(() -> {
                if ("online".equals(pendingDeliveryType)) {
                    Intent intent = new Intent(OrderActivity.this, CcavenueWebViewActivity.class);
                    String amountString = String.format(Locale.getDefault(), "%.2f", lastTotalAmount);
                    intent.putExtra("amount", amountString);
                    intent.putExtra("orderId", orderId);
                    intent.putExtra("working_key", working_key);
                    intent.putExtra("access_code", access_code);
                    intent.putExtra("merchant_id", merchant_id);
                    intent.putExtra("custName", custName);
                    intent.putExtra("custPhoneNo", custPhoneNo);
                    intent.putExtra("custcity", custcity);
                    String addressToShow = (apiCustAddress != null && !apiCustAddress.isEmpty()) ? apiCustAddress : custAddress;
                    intent.putExtra("CustAddress", addressToShow);
                    startActivity(intent);
                } else if ("cod".equals(pendingDeliveryType)) {

                    Intent intent = new Intent(OrderActivity.this, ComfirmOrderActivity.class);
                    intent.putExtra("orderId", orderId);
                    startActivity(intent);
                    dbHelper.clearCart();
                }
            });
        }
    }
    private void calculateTotals() {
        List<CartModel> cartItems = dbHelper.getAllCartItems();
        if (cartItems == null || cartItems.isEmpty()) {
            subtotalTextView.setText("Subtotal: ₹0.00");
            gstTextView.setText("GST: ₹0.00");
            totalTextView.setText("Total: ₹0.00");
            platformFeeGst.setText("₹0.00");
//            deliveryChargeGst.setText("₹0.00");
            return;
        }
        double localSubtotal = 0.0;
        double localTotalGst = 0.0;
        for (CartModel item : cartItems) {
            int qty = Math.max(1, item.getQuantity());
            double unitPrice = Double.parseDouble(item.getPrice());
            double linePrice = unitPrice * qty;
            double gstRate = Double.parseDouble(item.getProductGst());
            String gstInc = item.getGstInc();
            double itemGst = linePrice * (gstRate / 100.0);
            if ("1".equals(gstInc)) {
                localSubtotal += linePrice;
            } else {
                localSubtotal += linePrice - itemGst;
            }
            localTotalGst += itemGst;
        }
        double platformGstCalculate = platformFeeRate * (18.0 / 100);
        double deliveryChargeCalculate = deliveryChargeRate * (18.0 / 100);
        double localTotal = localSubtotal + localTotalGst + platformFeeRate + platformGstCalculate + deliveryChargeRate ;
        subtotalTextView.setText("Subtotal: ₹" + String.format("%.2f", localSubtotal));
        gstTextView.setText("GST: ₹" + String.format("%.2f", localTotalGst));
        totalTextView.setText("Total: ₹" + String.format("%.2f", localTotal));
        totalAmount.setText("Total: ₹" + String.format("%.2f", localTotal));
        platformFeeGst.setText("₹" + String.format("%.2f", platformGstCalculate));
//        deliveryChargeGst.setText("₹" + String.format("%.2f", deliveryChargeCalculate));
    }

//    private void checkShopTokenAndSendNotification(String shopId) {
//        Log.d("CheckShopToken", "Method called, shopId: " + shopId);
//
//        if (shopId == null || shopId.trim().isEmpty()) {
//            Log.d("ShopIdisnull", "Shop ID is null or empty: " + shopId);
//            return;
//        }
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        db.collection("shops").document(shopId.trim()).get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    Log.d("CheckShopToken", "Firestore call succeeded.");
//                    if (documentSnapshot.exists()) {
//                        String token = documentSnapshot.getString("token");
//                        sendNotificationToToken(token, "New Order Received", "You have a new order for shop ID: " + shopId);
//                        Log.d("Firestore", "Shop Token: " + token);
//                    } else {
//                        Log.d("Firestore", "Shop not found for shopId: " + shopId);
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("Firestore", "Error fetching shop: ", e);
//                });
//    }
//
//    private void sendNotificationToToken(String token, String title, String message) {
//        Log.d(TAG, "Sending notification to token: " + token);
//
//        FCMNotificationSender.sendNotification(token, title, message, OrderActivity.this,
//                new FCMNotificationSender.NotificationCallback() {
//                    @Override
//                    public void onSuccess() {
//                        runOnUiThread(() -> {
//                            Toast.makeText(OrderActivity.this, "Notification sent to shop device.", Toast.LENGTH_SHORT).show();
//                        });
//                        Log.d(TAG, "Notification sent successfully");
//                    }
//
//                    @Override
//                    public void onFailure(String error) {
//                        runOnUiThread(() -> {
//                            Toast.makeText(OrderActivity.this, "Failed to send notification: " + error, Toast.LENGTH_SHORT).show();
//                        });
//                        Log.e(TAG, "Failed to send notification: " + error);
//                    }
//                });
//    }
//
//
//    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    "FCM-CHANNEL",
//                    "Foodivery Notifications",
//                    NotificationManager.IMPORTANCE_DEFAULT
//            );
//            channel.setDescription("Channel for Foodivery app notifications");
//
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }


    public void getFCMToken(){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                String token=task.getResult();

                Log.d("FCMd",token);
            }
        });
    }
    private void sendOrderCart(String product_id, String product_name, String product_price,
                               String product_options, String product_gst,
                               String gst_Inc, String get_allVariantIds, String seq_order_id,
                               String orderId, String orderKey, String product_weight, int quantity) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("seq_order_id", seq_order_id)
                .add("order_id", orderId)
                .add("order_key", orderKey)
                .add("product_id", product_id)
                .add("product_name", product_name)
                .add("product_price", product_price)
                .add("quantity", String.valueOf(quantity)) // Add quantity parameter
                .add("product_options", product_options)
                .add("product_gst", product_gst)
                .add("product_weight", product_weight)
                .add("gst_Inc", gst_Inc)
                .add("get_allVariantIds", get_allVariantIds)
                .build();
        Log.d("OrderCart", "Product: " + product_name + ", Price: " + product_price + ", Quantity: " + quantity);
        Request request = new Request.Builder()
                .url(Attributes.Add_Order_Cart)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(OrderActivity.this, "Cart API Request Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Cart API Request failed: " + e.getMessage());
                });
                handleCartPostCompletion();
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    response.close();
                    handleCartPostCompletion();
                    return;
                }
                String responseBody = null;
                try {
                    if (response.body() != null) {
                        responseBody = response.body().string();
                    }
                } finally {
                    response.close();
                }
                if (responseBody != null) {
                    Log.d("gggggggggggghhhh", "Cart API Response: " + responseBody);
                }
                handleCartPostCompletion();
            }
        });
    }
    private void fetchTimeing(String custId) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("cust_id", custId)
                .build();
        Request request = new Request.Builder()
                .url(Attributes.Shop_Timing_Fetch)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(OrderActivity.this, "Failed to fetch timing: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
            @Override
            public void onResponse(Call call, @NonNull Response response) throws IOException {
                String responseBody = null;
                try {
                    if (response.body() != null) {
                        responseBody = response.body().string();
                    }
                } finally {
                    response.close();
                }
                if (responseBody == null) {
                    runOnUiThread(() -> Toast.makeText(OrderActivity.this, "Empty timing API response", Toast.LENGTH_SHORT).show());
                    return;
                }
                try {
                    JSONObject responseObject = new JSONObject(responseBody);
                    if (!responseObject.optBoolean("status", false)) {
                        throw new JSONException("API returned false status");
                    }
                    JSONArray dataArray = responseObject.optJSONArray("data");
                    Log.d("tamilkkkk",String.valueOf(responseObject));
                    if (dataArray == null) {
                        runOnUiThread(() -> Toast.makeText(OrderActivity.this, "No timing data found", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    List<String> timeSlots = new ArrayList<>();
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject slotObj = dataArray.getJSONObject(i);
                        String start = slotObj.optString("time_slot_start");
                        String end = slotObj.optString("time_slot_end");
                        String time_slot_option = slotObj.optString("time_slot_option");
                        if ("dely".equalsIgnoreCase(time_slot_option)) {
                            try {
                                // Parser for input time (24-hour format from API)
                                SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                                Date startTime = sdf24.parse(start);
                                Date endTimeObj = sdf24.parse(end);
                                // Reference noon time
                                Date noonTime = sdf24.parse("12:00:00");
                                // Formatter for output in AM/PM format
                                SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                                if (startTime != null && startTime.after(noonTime)) {
                                    // Convert both start and end to AM/PM format before adding
                                    String formattedStart = sdf12.format(startTime);
                                    String formattedEnd = sdf12.format(endTimeObj);
                                    timeSlots.add(formattedStart + " - " + formattedEnd);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(OrderActivity.this,
                                android.R.layout.simple_spinner_dropdown_item, timeSlots) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                TextView textView = (TextView) view;
                                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                                return view;
                            }
                            @Override
                            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                View view = super.getDropDownView(position, convertView, parent);
                                TextView textView = view.findViewById(android.R.id.text1);
                                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
                                view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
                                return view;
                            }
                        };
                        spinner2.setAdapter(adapter);
                    });
                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(OrderActivity.this, "Failed to parse timing: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
    private void apiKey() {
        OkHttpClient client = new OkHttpClient();
        // Build the query string
        String url = Attributes.Payment_Key_Api;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-api", Attributes.ApiToken)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(OrderActivity.this, "API Request Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Log.e("API_ERROR", "Request failed: " + e.getMessage());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    response.close();
                    runOnUiThread(() ->
                            Toast.makeText(OrderActivity.this, "API Response Unsuccessful: " + response.code(), Toast.LENGTH_LONG).show()
                    );
                    return;
                }
                try {
                    String responseBody = response.body().string();
                    Log.d("API_RESPONSE", responseBody);
                    JSONObject responseObject = new JSONObject(responseBody);
                    api_id = responseObject.getString("api_id");
                    api_secret = responseObject.getString("api_secret");
                    JSONObject ccavenue = responseObject.getJSONObject("ccavenue");
                    working_key = ccavenue.getString("working_key");
                    access_code = ccavenue.getString("access_code");
                    merchant_id = ccavenue.getString("merchant_id");
                    // Use or store these as needed
                    Log.d("CCAvenue", "Working Key: " + working_key);
                    Log.d("CCAvenue", "Access Code: " + access_code);
                    Log.d("CCAvenue", "Merchant ID: " + merchant_id);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("JSON_ERROR", "Failed to parse JSON: " + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(OrderActivity.this, "JSON Parsing Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }
    private void fetchPrimaryAddress(String custId) {
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
                runOnUiThread(() -> {
                    isAddressAvailable = false;
                    Toast.makeText(OrderActivity.this, "Failed to fetch address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    addressUsername.setText("");
                    addressText.setText("");
//                    addressChange.setText("Add Address");
//                    addressChange.setOnClickListener(v -> {
//                        Intent intent = new Intent(OrderActivity.this, AddressActivity.class);
//                        startActivity(intent);
//                    });
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = null;
                try {
                    if (!response.isSuccessful()) {
                        response.close();
                        runOnUiThread(() -> Toast.makeText(OrderActivity.this, "API Response Unsuccessful: " + response.code(), Toast.LENGTH_LONG).show());
                        return;
                    }
                    if (response.body() != null) {
                        responseBody = response.body().string();
                    }
                } finally {
                    response.close();
                }
                if (responseBody == null) {
                    runOnUiThread(() -> {
                        isAddressAvailable = false;
                        Toast.makeText(OrderActivity.this, "Empty address API response", Toast.LENGTH_SHORT).show();
                        addressUsername.setText("");
                        addressText.setText("");
//                        addressChange.setText("Add Address");
                    });
                    return;
                }
                try {
                    JSONObject responseObject = new JSONObject(responseBody);
                    if (!responseObject.optBoolean("status", false)) {
                        throw new JSONException("Please Add your Address");
                    }
                    JSONArray dataArray = responseObject.optJSONArray("data");
                    if (dataArray != null && dataArray.length() > 0) {
                        custName = null;
                        custAddress = null;
                        apiCustAddress = null;
                        custcity=null;
                        custPhoneNo=null;
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject addressObject = dataArray.getJSONObject(i);
                            String isPrimary = addressObject.optString("cust_address_primary", "0");
                            if ("1".equals(isPrimary)) {
                                custName = addressObject.optString("cust_name", "");
                                custAddress = addressObject.optString("cust_address", "");
                                apiCustAddress = addressObject.optString("apifetch_address", "");
                                custPhoneNo = addressObject.optString("cust_mobileno", "");
                                custcity = addressObject.optString("cust_city", "");
                                break;
                            }
                        }
                        if (TextUtils.isEmpty(custName)) {
                            // If no primary, use first address
                            JSONObject firstAddress = dataArray.getJSONObject(0);
                            custName = firstAddress.optString("cust_name", "");
                            custAddress = firstAddress.optString("cust_address", "");
                            apiCustAddress = firstAddress.optString("apifetch_address", "");
                            custPhoneNo = firstAddress.optString("custPhoneNo", "");
                            custcity = firstAddress.optString("cust_city", "");
                        }
                        final String finalName = !TextUtils.isEmpty(custName) ? custName : "No name provided";
                        final String finalAddress = !TextUtils.isEmpty(custAddress) ? custAddress : apiCustAddress;
                        runOnUiThread(() -> {
                            isAddressAvailable = true;
                            addressUsername.setText(finalName);
                            addressText.setText(finalAddress != null ? finalAddress : "No address found");
//                            addressChange.setText("Change Address");
                        });
                    } else {
                        runOnUiThread(() -> {
                            isAddressAvailable = false;
                            addressUsername.setText("No address found");
                            addressText.setText("");
//                            addressChange.setText("Add Address");
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        isAddressAvailable = false;
                        addressUsername.setText("No address found");
                        addressText.setText("");
//                        addressChange.setText("Add Address");
                        Toast.makeText(OrderActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}