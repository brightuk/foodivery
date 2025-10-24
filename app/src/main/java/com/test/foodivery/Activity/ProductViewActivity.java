package com.test.foodivery.Activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.test.foodivery.Attributes.Attributes;
import com.test.foodivery.Database.CartDatabaseHelper;
import com.test.foodivery.Model.OfferModel;
import com.test.foodivery.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProductViewActivity extends AppCompatActivity {
    private TextView productCost, productTitles;
    private final HashMap<String, String> productVariantIds = new HashMap<>();
    private String selectedListboxAmount = "0";
    private String selectedListboxValue = "0";
    private CartDatabaseHelper cartDbHelper;
    private String basePrice;
    private final List<RadioButton> allRadioButtons = new ArrayList<>();
    private final List<EditText> allEditTexts = new ArrayList<>();
    private final HashMap<String, String> selectedRadioAmounts = new HashMap<>();
    private final HashMap<String, String> selectedAddonAmounts = new HashMap<>();
    private JSONObject jsonResponseObject;
    private String imageUrl = "";
    private String productId, productName, gst_rate, gstInc, shopid, shopLogoImage, bizIds,productMessage;
    private String prodOptType = "1";
    LinearLayout optionsLayout;
    private final HashMap<String, List<RadioButton>> attributeRadioButtons = new HashMap<>();

    // --- EDIT MODE FIELDS ---
    private boolean isCartEdit = false;
    private String editingVariantIds = "";
    private String editingProductOptions = "";
    private String editingProductMessage = "";
    private String editingProductWeight = "",shopIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_view);

        ImageView fullImageView = findViewById(R.id.fullImageView);
        ImageView backBtn = findViewById(R.id.backbtn);
        productTitles = findViewById(R.id.productTitle);
        productCost = findViewById(R.id.productCost);
        ImageView cartOpen = findViewById(R.id.cartOpen);
        ImageView shopLogo = findViewById(R.id.shopLogoImage);
        optionsLayout = findViewById(R.id.optionsLayout);

        // Fetching data from intent
        imageUrl = getIntent().getStringExtra("image_url");
        productName = getIntent().getStringExtra("productTitle");
        basePrice = getIntent().getStringExtra("productPrice");
        productId = getIntent().getStringExtra("id");
        gst_rate = getIntent().getStringExtra("gst");
        gstInc = getIntent().getStringExtra("gstInc");
        shopid = getIntent().getStringExtra("shopid");
        shopLogoImage = getIntent().getStringExtra("shopLogo");
        bizIds = getIntent().getStringExtra("bizIds");








        // --- Get edit mode intent extras ---
        isCartEdit = getIntent().getBooleanExtra("cart_edit", false);
        editingVariantIds = getIntent().getStringExtra("allVariantIds");
        editingProductOptions = getIntent().getStringExtra("product_options");
        editingProductWeight = getIntent().getStringExtra("product_weight");
        editingProductMessage = getIntent().getStringExtra("product_message");

        productTitles.setText(productName);
        productCost.setText("₹ " + basePrice);
        if (shopLogoImage.equals("null") && bizIds.equals("null")) {
            fetchLogo(shopid);
        }

        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.foolivery_appicon)
                .error(R.drawable.foolivery_appicon)
                .into(fullImageView);

        Glide.with(this)
                .load(shopLogoImage)
                .placeholder(R.drawable.foolivery_appicon)
                .error(R.drawable.foolivery_appicon)
                .into(shopLogo);

        shopLogo.setOnClickListener(v -> openBrandPage(bizIds));
        backBtn.setOnClickListener(v -> finish());
        cartOpen.setOnClickListener(v -> {
            Intent intent = new Intent(ProductViewActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("OPEN_CART", true);
            startActivity(intent);
        });

        Button addToCartBtn = findViewById(R.id.addToCart);
        cartDbHelper = new CartDatabaseHelper(this);

        // Change button text if in edit mode
        if (isCartEdit) {
            addToCartBtn.setText("Update Cart");
        } else {
            addToCartBtn.setText("Add To Cart");
        }

        fetchAttributes(productId);

        addToCartBtn.setOnClickListener(v -> {
            if ("1".equals(prodOptType)) {
                optionsLayout.setVisibility(View.VISIBLE);
                StringBuilder productMessageBuilder = new StringBuilder();
                for (EditText editText : allEditTexts) {
                    String text = editText.getText().toString().trim();
                    if (!text.isEmpty()) {
                        if (productMessageBuilder.length() > 0) productMessageBuilder.append(", ");
                        productMessageBuilder.append(text);
                    }
                }
                productMessage = productMessageBuilder.toString();


                boolean allRadioGroupsSelected = true;
                for (List<RadioButton> rbList : attributeRadioButtons.values()) {
                    boolean found = false;
                    for (RadioButton rb : rbList) {
                        if (rb.isChecked()) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        allRadioGroupsSelected = false;
                        break;
                    }
                }
                if (!allRadioGroupsSelected) {
                    Toast.makeText(ProductViewActivity.this, "Please select all required options.", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean allEditTextsFilled = true;
                for (EditText editText : allEditTexts) {
                    if (editText.getText().toString().trim().isEmpty()) {
                        allEditTextsFilled = false;
                        break;
                    }
                }
                if (!allEditTextsFilled) {
                    Toast.makeText(ProductViewActivity.this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String price = productCost.getText().toString().replace("₹", "").trim();
            String selectedVariantIds = getSelectedVariantIds();
            String finalSelectedSpinnerValue = selectedListboxValue;

            String cartShopId = cartDbHelper.getCurrentCartShopId();
            if (cartShopId != null && !cartShopId.equals(shopid)) {
                new AlertDialog.Builder(ProductViewActivity.this)
                        .setTitle("Replace Cart Items?")
                        .setMessage("Your cart contains products from another shop. Do you want to clear the cart and add this product?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            cartDbHelper.clearCart();
                            boolean success = cartDbHelper.addOrIncreaseCartItem(
                                    productId,
                                    productName,
                                    price,
                                    productMessage, // <-- this maps to COL_PRODUCT_MESSAGE
                                    getSelectedOptions(),
                                    imageUrl,
                                    gst_rate,
                                    gstInc,
                                    shopid,
                                    selectedVariantIds,
                                    finalSelectedSpinnerValue
                            );

                            if (success) {
                                goToCart();
                                Toast.makeText(ProductViewActivity.this, "Item added to cart!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ProductViewActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            } else {

                boolean success = cartDbHelper.addOrIncreaseCartItem(
                        productId,
                        productName,
                        price,
                        productMessage, // <-- this maps to COL_PRODUCT_MESSAGE
                        getSelectedOptions(),
                        imageUrl,
                        gst_rate,
                        gstInc,
                        shopid,
                        selectedVariantIds,
                        finalSelectedSpinnerValue
                );


                if (success) {
                    goToCart();
                    if (isCartEdit) {
                        Toast.makeText(ProductViewActivity.this, "Cart item updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProductViewActivity.this, "Item added to cart!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProductViewActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchLogo(String shopids){
        OkHttpClient client = new OkHttpClient();


        RequestBody requestBody = new FormBody.Builder()

                .add("id", shopids)
                .build();

        Request request = new Request.Builder()
                .url(Attributes.Fetch_Shop_Logo)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {

                    Toast.makeText(ProductViewActivity.this,
                            "Failed to load offers. Please try again.",
                            Toast.LENGTH_LONG).show();
                    Log.e("API_ERROR", "Request failed: " + e.getMessage());
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    JSONObject responseObject = new JSONObject(responseBody);
                    Log.d("Response", String.valueOf(responseObject));

                    JSONObject dataObject = responseObject.getJSONObject("data");

                    String folderName = dataObject.getString("store_folder");
                    String logoName = dataObject.getString("shop_logo");
                    String fullImageUrl = Attributes.STORE_IMAGE_BASE_URL + folderName + "/" + logoName;
                    Log.d("fgggggfffff",fullImageUrl);

                    int bizId = dataObject.getInt("biz_id");



                    // If you want to update UI or do further processing, do it here or on the UI thread
                    runOnUiThread(() -> {
                        bizIds = String.valueOf(bizId);
                        shopLogoImage = fullImageUrl;
                        ImageView shopLogo = findViewById(R.id.shopLogoImage);
                        Glide.with(ProductViewActivity.this)
                                .load(fullImageUrl)
                                .placeholder(R.drawable.foolivery_appicon)
                                .error(R.drawable.foolivery_appicon)
                                .into(shopLogo);
                    });


                } catch (JSONException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(ProductViewActivity.this,
                                "Failed to parse logo data.",
                                Toast.LENGTH_LONG).show();
                        Log.e("JSON_ERROR", "Parsing error: " + e.getMessage());
                    });
                }
            }

        });
    }


    private void goToCart() {
        Intent intent = new Intent(ProductViewActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("OPEN_CART", true);
        startActivity(intent);
        finish();
    }

    private String getSelectedVariantIds() {
        StringBuilder variantIdsBuilder = new StringBuilder();
        for (String variantId : productVariantIds.values()) {
            if (variantId != null && !variantId.isEmpty()) {
                variantIdsBuilder.append(variantId).append(",");
            }
        }
        if (variantIdsBuilder.length() > 0) {
            variantIdsBuilder.setLength(variantIdsBuilder.length() - 1);
        }
        return variantIdsBuilder.toString();
    }

    private String getSelectedOptions() {
        if ("0".equals(prodOptType)) {
            return productName;
        }
        optionsLayout.setVisibility(View.VISIBLE);

        StringBuilder optionsBuilder = new StringBuilder();
        for (Map.Entry<String, List<RadioButton>> entry : attributeRadioButtons.entrySet()) {
            for (RadioButton radioButton : entry.getValue()) {
                if (radioButton.isChecked()) {
                    optionsBuilder.append(radioButton.getText().toString()).append(", ");
                }
            }
        }

        for (String key : selectedRadioAmounts.keySet()) {
            optionsBuilder.append(key)
                    .append(": ")
                    .append("₹").append(selectedRadioAmounts.get(key))
                    .append(", ");
        }

        if (!selectedListboxValue.equals("0")) {
            optionsBuilder.append("Weight: ")
                    .append(selectedListboxValue)
                    .append("kg @ ₹")
                    .append(selectedListboxAmount)
                    .append("/kg");
        }

        for (EditText editText : allEditTexts) {
            optionsBuilder.append(", ")
                    .append(editText.getHint().toString())
                    .append(": ")
                    .append(editText.getText().toString().trim());
        }

        return optionsBuilder.toString();
    }

    private void fetchAttributes(String productId) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("id", productId)
                .build();

        Request request = new Request.Builder()
                .url(Attributes.PRODUCT_ATTRIBUTES_VALUES)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(ProductViewActivity.this,
                            "Network error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e("API_ERROR", "Request failed", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    Log.d("API_RESPONSE", "Response: " + responseBody);

                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    jsonResponseObject = new JSONObject(responseBody);
                    boolean status = jsonResponseObject.getBoolean("status");

                    runOnUiThread(() -> {
                        if (status) {
                            try {
                                generateDynamicUI(jsonResponseObject);
                            } catch (Exception e) {
                                Log.e("UI_ERROR", "Error generating UI", e);
                                Toast.makeText(ProductViewActivity.this,
                                        "Error displaying product options",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } catch (JSONException e) {
                    Log.e("API_ERROR", "JSON parsing error", e);
                    runOnUiThread(() ->
                            Toast.makeText(ProductViewActivity.this,
                                    "Data format error",
                                    Toast.LENGTH_LONG).show());
                } finally {
                    response.close();
                }
            }
        });
    }

    @SuppressLint("ResourceType")
    private void generateDynamicUI(JSONObject jsonObject) {
        allRadioButtons.clear();
        allEditTexts.clear();
        productVariantIds.clear();
        selectedRadioAmounts.clear();
        selectedAddonAmounts.clear();
        attributeRadioButtons.clear();

        LinearLayout layout = findViewById(R.id.optionsLayout);
        layout.removeAllViews();

        try {
            JSONObject data = jsonObject.getJSONObject("data");
            JSONObject products = data.getJSONObject("product");
            prodOptType = products.getString("prod_opt_type");
            if ("0".equals(prodOptType)) {
                updateTotalPrice(products);
                return;
            }
            optionsLayout.setVisibility(View.VISIBLE);

            JSONArray attributes = data.getJSONArray("attributes");
            JSONObject variants = data.getJSONObject("variants");

            int insertIndex = 0;

            for (int i = 0; i < attributes.length(); i++) {
                JSONObject attribute = attributes.getJSONObject(i);
                String operatorCode = attribute.optString("prod_operator_code", "");
                if (operatorCode.equals("primary")) {
                    String itemList = attribute.getString("prod_item_list");
                    String attributeId = attribute.getString("prod_attribute_id");
                    String attributeName = attribute.getString("prod_attribute_name");
                    String attributeTitle = attribute.getString("prod_attribute_title");
                    String associatedType = attribute.optString("prod_item_associated", "");

                    if (variants.has(attributeId)) {
                        JSONArray variantArray = variants.getJSONArray(attributeId);
                        TextView attributeTitleView = createEnhancedAttributeTitle(attributeTitle.isEmpty() ? attributeName : attributeTitle);
                        layout.addView(attributeTitleView, insertIndex++);
                        if (itemList.equals("listbox")) {
                            createListBox(attribute, variantArray, layout, insertIndex++, attributeId);
                        } else if (itemList.equals("radio")) {
                            createRadioGroup(attribute, variantArray, layout, insertIndex++, attributeId);
                        }
                    }
                }
            }

            for (int i = 0; i < attributes.length(); i++) {
                JSONObject attribute = attributes.getJSONObject(i);
                String operatorCode = attribute.optString("prod_operator_code", "");
                String associatedType = attribute.optString("prod_item_associated", "");
                if (!operatorCode.equals("primary") && !associatedType.equals("msg")) {
                    String itemList = attribute.getString("prod_item_list");
                    String attributeId = attribute.getString("prod_attribute_id");
                    String attributeName = attribute.getString("prod_attribute_name");
                    String attributeTitle = attribute.getString("prod_attribute_title");

                    if (variants.has(attributeId)) {
                        JSONArray variantArray = variants.getJSONArray(attributeId);
                        TextView attributeTitleView = createEnhancedAttributeTitle(attributeTitle.isEmpty() ? attributeName : attributeTitle);
                        layout.addView(attributeTitleView, insertIndex++);
                        if (itemList.equals("listbox")) {
                            createListBox(attribute, variantArray, layout, insertIndex++, attributeId);
                        } else if (itemList.equals("radio")) {
                            createRadioGroup(attribute, variantArray, layout, insertIndex++, attributeId);
                        }
                    }
                }
            }

            for (int i = 0; i < attributes.length(); i++) {
                JSONObject attribute = attributes.getJSONObject(i);
                String associatedType = attribute.optString("prod_item_associated", "");
                if (associatedType.equals("msg")) {
                    String attributeId = attribute.getString("prod_attribute_id");
                    String attributeName = attribute.getString("prod_attribute_name");
                    String attributeTitle = attribute.getString("prod_attribute_title");

                    TextView attributeTitleView = createEnhancedAttributeTitle(attributeTitle.isEmpty() ? attributeName : attributeTitle);
                    layout.addView(attributeTitleView, insertIndex++);
                    createEditText(attribute, layout, insertIndex++, attributeId);
                }
            }
            updateTotalPrice(products);
        } catch (JSONException e) {
            Log.e("DynamicUI", "Error parsing JSON", e);
            Toast.makeText(this, "Error processing product options", Toast.LENGTH_SHORT).show();
        }
    }

    private TextView createEnhancedAttributeTitle(String title) {
        TextView attributeTitleView = new TextView(this);
        attributeTitleView.setText(title);
        attributeTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        attributeTitleView.setTypeface(null, Typeface.BOLD);
        attributeTitleView.setTextColor(Color.parseColor("#2C3E50"));


        attributeTitleView.setPadding(10, 5, 10, 5);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, 5, 0, 10);
        attributeTitleView.setLayoutParams(titleParams);

        return attributeTitleView;
    }

    private void createListBox(JSONObject attribute, JSONArray variants, LinearLayout layout, int insertIndex, String attributeId) throws JSONException {
        String operatorCode = attribute.optString("prod_operator_code", "");
        Spinner spinner = new Spinner(this);
        List<String> displayList = new ArrayList<>();
        HashMap<String, String> variantAmounts = new HashMap<>();
        HashMap<String, String> variantValues = new HashMap<>();
        HashMap<String, String> variantIds = new HashMap<>();

        for (int i = 0; i < variants.length(); i++) {
            JSONObject variant = variants.getJSONObject(i);
            String variantName = variant.getString("prod_variant_name");
            String variantAmount = variant.getString("prod_variant_amount");
            String variantValue = variant.getString("prod_variant_value");
            String variantId = variant.getString("prod_variant_id");

            String displayText = variantName;
            if (!variantAmount.equals("0")) {
                displayText += " (₹" + variantAmount + ")";
            }
            displayList.add(displayText);
            variantAmounts.put(displayText, variantAmount);
            variantValues.put(displayText, variantValue);
            variantIds.put(displayText, variantId);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, displayList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = getLayoutInflater().inflate(R.layout.spinner_item, parent, false);
                }
                TextView tv = view.findViewById(R.id.spinner_item_text);
                if (tv != null) {
                    tv.setText(getItem(position));
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = getLayoutInflater().inflate(R.layout.spinner_dropdown_item, parent, false);
                }
                TextView tv = view.findViewById(R.id.spinner_dropdown_item_text);
                if (tv != null) {
                    tv.setText(getItem(position));
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setBackgroundResource(R.drawable.spinner_background);
        spinner.setPadding(16, 12, 16, 12);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                selectedListboxAmount = variantAmounts.get(selectedItem);
                selectedListboxValue = variantValues.get(selectedItem);
                productVariantIds.put(attributeId, variantIds.get(selectedItem));
                try {
                    JSONObject products = jsonResponseObject.getJSONObject("data").getJSONObject("product");
                    updateTotalPrice(products);
                } catch (JSONException e) {
                    Log.e("DynamicUI", "Error updating price", e);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // --- EDIT CART pre-select spinner ---
        if (isCartEdit && editingVariantIds != null && !editingVariantIds.isEmpty()) {
            String[] selectedIds = editingVariantIds.split(",");
            for (int i = 0; i < displayList.size(); i++) {
                String vId = variantIds.get(displayList.get(i));
                for (String sid : selectedIds) {
                    if (sid.equals(vId)) {
                        spinner.setSelection(i);
                        selectedListboxAmount = variantAmounts.get(displayList.get(i));
                        selectedListboxValue = variantValues.get(displayList.get(i));
                        productVariantIds.put(attributeId, vId);
                        break;
                    }
                }
            }
        } else if (operatorCode.equals("primary")) {
            spinner.setSelection(0);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 20);
        spinner.setLayoutParams(params);
        layout.addView(spinner, insertIndex);
    }

    private void createRadioGroup(JSONObject attribute, JSONArray variants,
                                  LinearLayout layout, int insertIndex, String attributeId) throws JSONException {
        String operatorCode = attribute.optString("prod_operator_code", "");

        List<RadioButton> radioButtonList = new ArrayList<>();
        attributeRadioButtons.put(attributeId, radioButtonList);

        LinearLayout radioContainer = new LinearLayout(this);
        radioContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(0, 0, 0, 20);
        radioContainer.setLayoutParams(containerParams);

        int totalVariants = variants.length();
        int maxButtonsPerRow = 2;
        int totalRows = (int) Math.ceil((double) totalVariants / maxButtonsPerRow);

        for (int row = 0; row < totalRows; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            rowParams.setMargins(0, 8, 0, 8);
            rowLayout.setLayoutParams(rowParams);

            int buttonsInThisRow = Math.min(maxButtonsPerRow, totalVariants - (row * maxButtonsPerRow));
            for (int i = 0; i < buttonsInThisRow; i++) {
                int variantIndex = (row * maxButtonsPerRow) + i;
                JSONObject variant = variants.getJSONObject(variantIndex);
                RadioButton radioButton = createRadioButtonFromVariant(variant, operatorCode);
                radioButtonList.add(radioButton);
                radioButton.setOnClickListener(v -> {
                    for (RadioButton rb : radioButtonList) {
                        rb.setChecked(rb == radioButton);
                    }
                    handleRadioButtonSelection(radioButton, attributeId);
                });

                rowLayout.addView(radioButton);
            }
            radioContainer.addView(rowLayout);
        }

        // --- EDIT CART pre-select radio buttons ---
        boolean anySelected = false;
        if (isCartEdit && editingVariantIds != null && !editingVariantIds.isEmpty()) {
            String[] selectedIds = editingVariantIds.split(",");
            for (RadioButton radioButton : radioButtonList) {
                String[] tagData = (String[]) radioButton.getTag();
                String variantId = tagData[1];
                for (String sid : selectedIds) {
                    if (sid.equals(variantId)) {
                        radioButton.setChecked(true);
                        handleRadioButtonSelection(radioButton, attributeId);
                        anySelected = true;
                        break;
                    }
                }
            }
        }

        // --- DEFAULT selection for non-edit mode ---
        if (!isCartEdit && !radioButtonList.isEmpty() && !anySelected) {
            RadioButton defaultBtn = radioButtonList.get(0);
            defaultBtn.setChecked(true);
            handleRadioButtonSelection(defaultBtn, attributeId);
        }

        layout.addView(radioContainer, insertIndex);
    }


    private RadioButton createRadioButtonFromVariant(JSONObject variant, String operatorCode)
            throws JSONException {
        String variantName = variant.getString("prod_variant_name");
        String variantAmount = variant.getString("prod_variant_amount");
        String valueAmount = variant.getString("prod_variant_value");
        String variantId = variant.getString("prod_variant_id");

        RadioButton radioButton = new RadioButton(this);
        radioButton.setText(variantAmount.equals("0") ? variantName : String.format("%s (₹%s)", variantName, variantAmount));
        radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        radioButton.setTextColor(Color.parseColor("#2C3E50"));
        radioButton.setBackgroundResource(R.drawable.radio_button_background);
        radioButton.setButtonTintList(ContextCompat.getColorStateList(this, R.color.radio_button_tint));
        radioButton.setPadding(16, 12, 16, 12);
        radioButton.setTag(new String[] { variantAmount, variantId, operatorCode, valueAmount });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f
        );
        params.setMargins(6, 0, 6, 0);
        radioButton.setLayoutParams(params);

        return radioButton;
    }

    private void handleRadioButtonSelection(RadioButton selectedRadio, String attributeId) {
        String[] tagData = (String[]) selectedRadio.getTag();
        String variantAmount = tagData[0];
        String variantId = tagData[1];
        String opCode = tagData[2];
        String displayText = selectedRadio.getText().toString();

        productVariantIds.put(attributeId, variantId);
        if (opCode.equals("associated")) {
            selectedRadioAmounts.put(displayText, variantAmount);
        } else if (opCode.equals("addon")) {
            selectedAddonAmounts.put(displayText, variantAmount);
        }
        updateProductPrice();
    }

    private void createEditText(JSONObject attribute, LinearLayout layout, int insertIndex, String attributeId) throws JSONException {
        String attributeDesc = attribute.getString("prod_attribute_desc");
        String attributeItemList = attribute.getString("prod_item_list");

        EditText editText = new EditText(this);
        editText.setHint(attributeDesc);
        editText.setBackgroundResource(R.drawable.edittext_background);
        editText.setPadding(20, 16, 20, 16);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        editText.setTextColor(Color.parseColor("#2C3E50"));
        allEditTexts.add(editText);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 20);
        editText.setLayoutParams(params);
        layout.addView(editText, insertIndex);

        // --- EDIT CART pre-fill ---
        if (isCartEdit && editingProductMessage != null && !editingProductMessage.isEmpty()) {
            editText.setText(editingProductMessage);
        }
    }

    private void updateTotalPrice(JSONObject products) throws JSONException {
        double totalPrice = 0;
        double basePrice = Double.parseDouble(products.getString("prod_price"));

        if (!"1".equals(prodOptType)) {
            productCost.setText(String.format("₹ %.2f", basePrice));
            return;
        }

        double primaryAmount = 0;
        double primaryValue = 1;

        if (!selectedListboxAmount.equals("0") && !selectedListboxValue.equals("0")) {
            primaryAmount = Double.parseDouble(selectedListboxAmount);
            primaryValue = Double.parseDouble(selectedListboxValue);
        }

        outer:
        for (Map.Entry<String, List<RadioButton>> entry : attributeRadioButtons.entrySet()) {
            for (RadioButton radioButton : entry.getValue()) {
                if (radioButton.isChecked()) {
                    String[] tag = (String[]) radioButton.getTag();
                    String opCode = tag[2];
                    if (opCode.equals("primary")) {
                        primaryAmount = Double.parseDouble(tag[0]);
                        primaryValue = Double.parseDouble(tag[3]);
                        break outer;
                    }
                }
            }
        }

        totalPrice = (primaryAmount != 0) ? primaryAmount : basePrice;

        for (Map.Entry<String, List<RadioButton>> entry : attributeRadioButtons.entrySet()) {
            for (RadioButton radioButton : entry.getValue()) {
                if (radioButton.isChecked()) {
                    String[] tag = (String[]) radioButton.getTag();
                    double amount = Double.parseDouble(tag[0]);
                    String opCode = tag[2];
                    if (opCode.equals("associated")) {
                        totalPrice += (amount * primaryValue);
                    }
                    else if (opCode.equals("addon")) {
                        totalPrice += amount;
                    }
                }
            }
        }

        productCost.setText(String.format("₹ %.2f", totalPrice));
    }

    private void updateProductPrice() {
        try {
            JSONObject products = jsonResponseObject.getJSONObject("data").getJSONObject("product");
            updateTotalPrice(products);
        } catch (JSONException e) {
            Log.e("DynamicUI", "Error updating price", e);
        }
    }

    private void openBrandPage(String bizId) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("biz_id", bizId)
                .build();

        Request request = new Request.Builder()
                .url(Attributes.BrandFetch)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(ProductViewActivity.this,
                            "Network error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e("API_ERROR", "Request failed", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                Log.d("API_brandssss", "Response: " + responseBody);
                try {
                    JSONObject jsonResponseObject = new JSONObject(responseBody);
                    boolean status = jsonResponseObject.getBoolean("status");
                    String message = jsonResponseObject.getString("message");

                    if (status) {
                        JSONArray dataArray = jsonResponseObject.optJSONArray("data");
                        JSONArray shopIdsArray = jsonResponseObject.optJSONArray("shopIds");

                        // Convert shopIds array to ArrayList
                        ArrayList<String> shopIdList = new ArrayList<>();
                        if (shopIdsArray != null) {
                            for (int i = 0; i < shopIdsArray.length(); i++) {
                                shopIdList.add(shopIdsArray.getString(i));
                            }
                        }

                        // Create shop details list with current shop's info
                        ArrayList<HashMap<String, String>> shopDetailsList = new ArrayList<>();
                        HashMap<String, String> currentShop = new HashMap<>();
                        currentShop.put("biz_id", bizId);
                        currentShop.put("shop_logo", extractFilenameFromLogoUrl(shopLogoImage));
                        currentShop.put("store_folder", extractFolderFromLogoUrl(shopLogoImage));
                        shopDetailsList.add(currentShop);

                        // Start activity on UI thread
                        runOnUiThread(() -> {
                            Intent intent = new Intent(ProductViewActivity.this, BrandPage.class);
                            intent.putExtra("brand_data", dataArray != null ? dataArray.toString() : "");
                            intent.putStringArrayListExtra("SHOP_ID_LIST", shopIdList);

                            // Convert shop details to JSON strings
                            ArrayList<String> shopDetailsJsonList = new ArrayList<>();
                            for (HashMap<String, String> shop : shopDetailsList) {
                                shopDetailsJsonList.add(new JSONObject(shop).toString());
                            }
                            intent.putStringArrayListExtra("SHOP_DETAILS_LIST", shopDetailsJsonList);

                            startActivity(intent);
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(ProductViewActivity.this,
                                "Error: " + message,
                                Toast.LENGTH_LONG).show());
                    }
                } catch (JSONException e) {
                    Log.e("API_ERROR", "JSON parsing error", e);
                    runOnUiThread(() ->
                            Toast.makeText(ProductViewActivity.this,
                                    "Data format error",
                                    Toast.LENGTH_LONG).show());
                } finally {
                    response.close();
                }
            }
        });
    }


    private String extractFolderFromLogoUrl(String logoUrl) {
        if (logoUrl == null || logoUrl.isEmpty()) {
            return "";
        }
        String[] parts = logoUrl.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("store") && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        return "";
    }

    private String extractFilenameFromLogoUrl(String logoUrl) {
        if (logoUrl == null || logoUrl.isEmpty()) {
            return "";
        }
        String[] parts = logoUrl.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            if (!parts[i].isEmpty()) {
                return parts[i];
            }
        }
        return "";
    }
}
