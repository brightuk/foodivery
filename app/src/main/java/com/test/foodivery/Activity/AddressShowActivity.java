package com.test.foodivery.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.test.foodivery.Adapter.AddressAdapter;
import com.test.foodivery.Adapter.OfferAdapter;
import com.test.foodivery.Attributes.Attributes;
import com.test.foodivery.Model.AddressModel;
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

public class AddressShowActivity extends AppCompatActivity {


    private List<AddressModel> imageList;
    private List<AddressModel> originalList = new ArrayList<>();
    private AddressAdapter adapter;
    private RecyclerView recyclerView;
    private AppCompatButton addAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_address_show);
        recyclerView=findViewById(R.id.recyclerView);
        addAddress=findViewById(R.id.addAddress);
        ImageView backBtn = findViewById(R.id.backButton);

        AppCompatButton setDefaultBtn = findViewById(R.id.setPrimary);


        backBtn.setOnClickListener(v -> finish());
        addAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddressShowActivity.this, AddressActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });



        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        String custId = prefs.getString("cust_id", "");
        Log.d("savedCustom", String.valueOf(custId));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));

        imageList = new ArrayList<>();
//        adapter = new AddressAdapter(this, imageList);
        recyclerView.setAdapter(adapter);


        fetchAlladdress(custId);
        setDefaultBtn.setOnClickListener(v -> {


            // Start OrderActivity
            Intent intent = new Intent(AddressShowActivity.this, OrderActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);


            // Finish current activity so it won't remain in the back stack
            finish();
        });



    }

    private void fetchAlladdress(String cust_id) {
        OkHttpClient client = new OkHttpClient();

        // Convert shop_id list to a comma-separated string
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
                runOnUiThread(() ->
                        Toast.makeText(AddressShowActivity.this, "API Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("addressaaaaaaaaa", "Response: " + responseBody);

                try {
                    JSONObject responseObject = new JSONObject(responseBody);



                    JSONArray dataArray = responseObject.getJSONArray("data");
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
                        originalList = new ArrayList<>(imageList);
//                        adapter.updateList(imageList);

                    });

                } catch (JSONException e) {
                    Log.e("API_ERROR", "JSON Parsing Error: " + e.getMessage());
                }
            }


        });
    }

//    private void setDefaultAddress(String custId,String addressId) {
//        OkHttpClient client = new OkHttpClient();
//
//        // Build the request body (modify keys as per your API)
//        RequestBody requestBody = new FormBody.Builder()
//                .add("address_id", addressId)
//                .add("cust_id", custId)
//                .build();
//
//        Request request = new Request.Builder()
//                .url(Attributes.Set_Default_Address) // Replace with your actual endpoint
//                .post(requestBody)
//                .addHeader("Content-Type", "application/x-www-form-urlencoded")
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                runOnUiThread(() -> Toast.makeText(AddressShowActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                String responseBody = response.body().string();
//                runOnUiThread(() -> Toast.makeText(AddressShowActivity.this, "Set as default successful!", Toast.LENGTH_SHORT).show());
//                // Handle success or error based on your API response
//            }
//        });
//    }


}