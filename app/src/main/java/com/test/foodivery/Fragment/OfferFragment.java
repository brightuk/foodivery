package com.test.foodivery.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.test.foodivery.Activity.MainActivity;
import com.test.foodivery.Adapter.OfferAdapter;
import com.test.foodivery.Attributes.Attributes;
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

public class OfferFragment extends Fragment {

    private RecyclerView recyclerView;
    private ImageView btnBack, cartOpen;
    private OfferAdapter adapter;
    private List<OfferModel> imageList;
    private EditText searchEditText;
    private ProgressBar progressBar;
    private List<OfferModel> originalList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_offer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();

        Bundle args = getArguments();
        String names = args != null ? args.getString("name", "all") : "all";
        ArrayList<String> shopIdList = args != null ? args.getStringArrayList("SHOP_ID_LIST") : null;
        ArrayList<String> shopDetailsList = args != null ? args.getStringArrayList("SHOP_DETAILS_LIST") : null;

        if (shopIdList == null || shopIdList.isEmpty()) {
            Toast.makeText(getContext(), "No shops available in your area. Please try a different location.", Toast.LENGTH_LONG).show();
            return;
        }

        showLoading();
        fetchAllOffer(names, shopIdList, shopDetailsList);
        setupSearch();
    }

    private void initializeViews(View root) {
        searchEditText = root.findViewById(R.id.searchEditText);
        btnBack = root.findViewById(R.id.btnBack);
        recyclerView = root.findViewById(R.id.cartRecyclerView);
        cartOpen = root.findViewById(R.id.cartOpen);
        progressBar = root.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        imageList = new ArrayList<>();
        adapter = new OfferAdapter(getContext(), imageList);
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        cartOpen.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MainActivity.class);
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
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    hideLoading();
                    Toast.makeText(getContext(), "Failed to load offers. Please try again.", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    List<OfferModel> tempList = new ArrayList<>();
                    JSONObject responseObject = new JSONObject(responseBody);
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

                        if (shopLogo != null) {
                            for (int index = 0; index < shopLogo.size(); index++) {
                                String jsonString = shopLogo.get(index).trim();
                                try {
                                    if (jsonString.startsWith("{")) {
                                        JSONObject shopObject = new JSONObject(jsonString);
                                        biz_id = shopObject.getString("biz_id");
                                        storeFolder = shopObject.getString("store_folder");
                                        logoName = shopObject.getString("shop_logo");
                                        if (String.valueOf(bizId).equals(biz_id)){
                                            folderNames=storeFolder;
                                            logoN = logoName;
                                            logofullImageUrl = Attributes.Shop_Logo + "images/store/" + folderNames + "/" + logoN;
                                        }
                                    }
                                } catch (JSONException ignored) {}
                            }
                        }

                        tempList.add(new OfferModel(
                                fullImageUrl,
                                String.valueOf(id),
                                String.valueOf(shopIds),
                                imageObject.getString("prod_name"),
                                String.valueOf((float) imageObject.getDouble("prod_price")),
                                String.valueOf((float) imageObject.getDouble("prod_offer")),
                                String.valueOf(imageObject.getInt("prod_gst_rate")),
                                String.valueOf(imageObject.getInt("prod_gst_inc")),
                                logofullImageUrl,
                                folderNames,
                                logoN,
                                imageObject.getString("biz_id")
                        ));
                    }

                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        imageList.clear();
                        imageList.addAll(tempList);
                        originalList = new ArrayList<>(imageList);
                        adapter.updateList(imageList);
                        hideLoading();
                    });
                } catch (Exception e) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        hideLoading();
                        Toast.makeText(getContext(), "Error parsing data", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}


