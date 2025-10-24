package com.test.foodivery.Activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.test.foodivery.Adapter.BrandAdapter;
import com.test.foodivery.Attributes.Attributes;
import com.test.foodivery.Model.BrandModel;
import com.test.foodivery.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class BrandPage extends AppCompatActivity {
    RecyclerView recyclerView;
    BrandAdapter adapter;
    ArrayList<BrandModel> brandList = new ArrayList<>();
    TextView brandName;
    String brandname;
    private ArrayList<String> shopIdList;
    private ArrayList<HashMap<String, String>> shopDetailsList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_page);

        recyclerView = findViewById(R.id.brandRecyclerView);
        brandName = findViewById(R.id.brandName);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        // Initialize lists
        shopIdList = new ArrayList<>();
        shopDetailsList = new ArrayList<>();

        // Get data from intent
        String brandData = getIntent().getStringExtra("brand_data");
        shopIdList = getIntent().getStringArrayListExtra("SHOP_ID_LIST");
        ArrayList<String> shopDetailsJsonList = getIntent().getStringArrayListExtra("SHOP_DETAILS_LIST");
        Log.d("kkkkkkkkkkkk", "brand_data: " + brandData);
        Log.d("kkkkkkkkkkkk", "SHOP_ID_LIST: " + shopIdList);
        Log.d("kkkkkkkkkkkk", "SHOP_DETAILS_LIST: " + shopDetailsJsonList);
        Log.d("ttttssssweew",String.valueOf(shopDetailsJsonList));

        // Parse shop details
        if (shopDetailsJsonList != null) {
            for (String jsonString : shopDetailsJsonList) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("biz_id", jsonObject.getString("biz_id"));
                    map.put("shop_logo", jsonObject.getString("shop_logo"));
                    map.put("store_folder", jsonObject.getString("store_folder"));
                    shopDetailsList.add(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if (brandData != null) {
            try {
                JSONArray dataArray = new JSONArray(brandData);
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject obj = dataArray.getJSONObject(i);
                    brandname = obj.optString("store_folder");

                    BrandModel brand = new BrandModel(
                            obj.optString("s_id"),
                            obj.optString("biz_id"),
                            obj.optString("shop_id"),
                            obj.optString("new_shop_id"),
                            obj.optString("shop_branch"),
                            obj.optString("shop_name"),
                            obj.optString("shop_logo"),
                            obj.optString("store_folder"),
                            obj.optString("shop_image"),
                            obj.optString("shop_url"),
                            obj.optString("shop_area_url"),
                            obj.optString("shop_address"),
                            obj.optString("shop_area"),
                            obj.optString("shop_city"),
                            obj.optString("shop_state"),
                            obj.optString("shop_pincode"),
                            obj.optString("shop_phone"),
                            obj.optString("shop_mobile"),
                            obj.optString("shop_mobile2"),
                            obj.optString("shop_whatsapp"),
                            obj.optString("shop_fssai"),
                            obj.optString("shop_gst"),
                            obj.optString("shop_admin_name"),
                            obj.optString("shop_admin_mobile"),
                            obj.optString("shop_admin_whatsapp"),
                            obj.optString("shop_dine_in"),
                            obj.optString("shop_pickup"),
                            obj.optString("shop_delivery"),
                            obj.optString("shop_status"),
                            obj.optString("shop_added_date"),
                            obj.optString("shop_setting"),
                            obj.optString("shop_key"),
                            obj.optString("image_path"),
                            obj.optString("biz_domain_id"),
                            obj.optString("latitude"),
                            obj.optString("longitude"),
                            Attributes.STORE_IMAGE_BASE_URL+obj.optString("shop_logo")
                    );
                    Log.d("gghhhjhhh",Attributes.STORE_IMAGE_BASE_URL+obj.optString("shop_logo"));
                    brandList.add(brand);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (brandname != null && !brandname.isEmpty()) {
            brandName.setText(brandname.substring(0, 1).toUpperCase() + brandname.substring(1));
        }


        adapter = new BrandAdapter(this, brandList, shopIdList, shopDetailsList);
        recyclerView.setAdapter(adapter);
    }
}
