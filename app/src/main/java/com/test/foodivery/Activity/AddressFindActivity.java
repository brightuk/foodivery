package com.test.foodivery.Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.test.foodivery.Adapter.AddressAdapter;
import com.test.foodivery.Attributes.Attributes;
import com.test.foodivery.Model.AddressModel;
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

public class AddressFindActivity extends AppCompatActivity {

    private LinearLayout currentAddressGet,addNewAddress;
    private RecyclerView recyclerView;
    private List<AddressModel> imageList;
    private List<AddressModel> originalList = new ArrayList<>();
    private AddressAdapter adapter;
    private View progressBarContainer;

    private JSONArray addressJsonArray; // Add this field to your activity

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_address_find);
        currentAddressGet=findViewById(R.id.currentAddressGet);
        ImageView backBtn = findViewById(R.id.backbtn);
        recyclerView=findViewById(R.id.recyclerView);
        addNewAddress=findViewById(R.id.addNewAddress);
        progressBarContainer = findViewById(R.id.progressBarContainer);
        // Show spinner initially

        backBtn.setOnClickListener(v -> finish());
        addNewAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AddressFindActivity.this,AddressActivity.class);
                startActivity(intent);
            }
        });
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String custId = prefs.getString("cust_id", "");
        Log.d("savedCustom", String.valueOf(custId));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        imageList = new ArrayList<>();

        adapter = new AddressAdapter(this, imageList, new AddressAdapter.OnAddressClickListener() {
            @Override
            public void onAddressClick(int position) {
                try {
                    JSONObject addressObject = addressJsonArray.getJSONObject(position);
                    double latitude = Double.parseDouble(addressObject.optString("latitude", "0.0"));
                    double longitude = Double.parseDouble(addressObject.optString("longitude", "0.0"));
                    String address = addressObject.optString("apifetch_address", "");
                    String area = addressObject.optString("cust_area", "");
                    String address_id = addressObject.optString("id", "");
                    SharedPreferences prefss = getSharedPreferences("UserSession", MODE_PRIVATE);
                    String cust_id = prefss.getString("cust_id", "");

                    setDefaultAddress(cust_id,address_id);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("latitude", latitude);
                    resultIntent.putExtra("longitude", longitude);
                    resultIntent.putExtra("faddress", address);
                    resultIntent.putExtra("sublocality", area);

                    setResult(RESULT_OK, resultIntent);
                    Intent intent=new Intent(AddressFindActivity.this,MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(AddressFindActivity.this, "Error selecting address", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onEditClick(int position) {
                try {
                    JSONObject addressObject = addressJsonArray.getJSONObject(position);
                    String addressId = addressObject.optString("id", "");
                    String custName = addressObject.optString("cust_name", "");
                    String custMobile = addressObject.optString("cust_mobileno", "");
                    String custAddress = addressObject.optString("cust_address", "");
                    String custArea = addressObject.optString("cust_area", "");
                    String custCity = addressObject.optString("cust_city", "");
                    
                    // Launch edit address activity with pre-filled data
                    Intent intent = new Intent(AddressFindActivity.this, AddressActivity.class);
                    intent.putExtra("isEdit", true);
                    intent.putExtra("addressId", addressId);
                    intent.putExtra("custName", custName);
                    intent.putExtra("custMobile", custMobile);
                    intent.putExtra("custAddress", custAddress);
                    intent.putExtra("custArea", custArea);
                    intent.putExtra("custCity", custCity);
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(AddressFindActivity.this, "Error editing address", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDeleteClick(int position) {
                try {
                    JSONObject addressObject = addressJsonArray.getJSONObject(position);
                    String addressId = addressObject.optString("id", "");
                    String addressText = addressObject.optString("cust_address", "");
                    
                    showDeleteConfirmationDialog(addressId, addressText, position);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(AddressFindActivity.this, "Error deleting address", Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerView.setAdapter(adapter);
        progressBarContainer.setVisibility(View.VISIBLE);
        fetchAlladdress(custId);
        currentAddressGet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AddressFindActivity.this,GoogleAddressActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showDeleteConfirmationDialog(String addressId, String addressText, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Address")
                .setMessage("Are you sure you want to delete this address?\n\n" + addressText)
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteAddress(addressId, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAddress(String addressId, int position) {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String custId = prefs.getString("cust_id", "");

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("address_id", addressId)
                .add("cust_id", custId)
                .build();

        Request request = new Request.Builder()
                .url(Attributes.Delete_Address)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(AddressFindActivity.this, "Failed to delete address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("DELETE_ADDRESS", "Response: " + responseBody);
                
                try {
                    JSONObject responseObject = new JSONObject(responseBody);
                    String status = responseObject.optString("status", "");
                    
                    if ("true".equals(status) || "success".equals(status.toLowerCase())) {
                        runOnUiThread(() -> {
                            // Remove from lists
                            imageList.remove(position);
                            adapter.notifyItemRemoved(position);
                            adapter.notifyItemRangeChanged(position, imageList.size());
                            
                            // Update JSON array
                            JSONArray newArray = new JSONArray();
                            for (int i = 0; i < addressJsonArray.length(); i++) {
                                if (i != position) {
                                    try {
                                        newArray.put(addressJsonArray.get(i));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            addressJsonArray = newArray;
                            
                            Toast.makeText(AddressFindActivity.this, "Address deleted successfully", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        String message = responseObject.optString("message", "Failed to delete address");
                        runOnUiThread(() -> {
                            Toast.makeText(AddressFindActivity.this, message, Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(AddressFindActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void fetchAlladdress(String cust_id) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("cust_id", cust_id)
                .build();

        Request request = new Request.Builder()
                .url(Attributes.Fetch_Address)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("API_ERROR", "Request failed: " + e.getMessage());
                runOnUiThread(() -> {
                    findViewById(R.id.progressBarContainer).setVisibility(View.GONE);
                    Toast.makeText(AddressFindActivity.this, "API Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("addressaaaaaaaaa", "Response: " + responseBody);

                try {
                    JSONObject responseObject = new JSONObject(responseBody);
                    JSONArray dataArray = responseObject.getJSONArray("data");
                    addressJsonArray = dataArray;
                    imageList.clear();

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject addressObject = dataArray.getJSONObject(i);

                        String id = addressObject.getString("id");
                        String custId = addressObject.getString("cust_id");
                        String custName = addressObject.getString("cust_name");
                        String custMobileno = addressObject.getString("cust_mobileno");
                        String custAddress = addressObject.getString("cust_address");
                        String apifetch_address = addressObject.getString("apifetch_address");
                        String custArea = addressObject.getString("cust_area");
                        String custCity = addressObject.getString("cust_city");
                        String custPrimary = addressObject.getString("cust_address_primary");
                        String custStatus = addressObject.getString("cust_status");

                        imageList.add(new AddressModel(id,custId, custName,
                                custMobileno, custAddress,apifetch_address, custArea, custCity,custPrimary,custStatus));
                    }

                    runOnUiThread(() -> {
                        progressBarContainer.setVisibility(View.GONE);
                        originalList = new ArrayList<>(imageList);

                        adapter.notifyDataSetChanged(); // <--- THIS IS IMPORTANT
                    });

                } catch (JSONException e) {
                    Log.e("API_ERROR", "JSON Parsing Error: " + e.getMessage());
                }
            }
        });
    }

    private void setDefaultAddress(String custId,String addressId) {
        OkHttpClient client = new OkHttpClient();

        // Build the request body (modify keys as per your API)
        RequestBody requestBody = new FormBody.Builder()
                .add("address_id", addressId)
                .add("cust_id", custId)

                .build();

        Request request = new Request.Builder()
                .url(Attributes.Set_Default_Address) // Replace with your actual endpoint
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(AddressFindActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("kkkkkkkkkkkkl",responseBody);
                try {
//                    Log.d("kkkkkkkkkkkkl",responseBody);
//                    JSONObject responseObject = new JSONObject(responseBody);
//                    JSONArray dataArray = responseObject.getJSONArray("status");
//                    if (dataArray.equals("true")){
                        runOnUiThread(() -> Toast.makeText(AddressFindActivity.this, "Set as default successful!", Toast.LENGTH_SHORT).show());
//                    }


                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                // Handle success or error based on your API response
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the address list when returning from edit/add activity
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String custId = prefs.getString("cust_id", "");
        if (!custId.isEmpty()) {
            fetchAlladdress(custId);
        }
    }
}