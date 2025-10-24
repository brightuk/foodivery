package com.test.foodivery.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.test.foodivery.Attributes.Attributes;
import com.test.foodivery.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CcavenueWebViewActivity extends AppCompatActivity {

    private WebView webView;

    // Merchant/payment details
    private String merchantId = "";
    private String accessCode = "";
    private String workingKey = "";
    private String orderNo = "";  // placeholder, will be set dynamically
    private final String currency = "INR";
    private double totalAmount = 0;
    private final double usdRate = 80.0;

    // Customer details
    private String deliveryName = "John Doe";
    private String deliveryAddress = "123 Street Address";
    private String deliveryCity = "Chennai";
    private final String deliveryState = "Tamil Nadu";
    private final String deliveryCountry = "India";
    private final String deliveryZip = "600001";
    private String deliveryTel = "9999999999";


    // Your redirect URL after payment completion
    private final String redirectUrl = "https://www.chennaibiz.in/web_order/callback";

    // CCAvenue payment URL
    private final String ccAvenuePaymentUrl = "https://secure.ccavenue.com/transaction/transaction.do?command=initiateTransaction";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ccavenue_web_view);

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        String orderid = getIntent().getStringExtra("orderId");
        String amount = getIntent().getStringExtra("amount");
        String working_key = getIntent().getStringExtra("working_key");
        String access_code = getIntent().getStringExtra("access_code");
        String merchant_id = getIntent().getStringExtra("merchant_id");
        String custName = getIntent().getStringExtra("custName");
        String CustAddress = getIntent().getStringExtra("CustAddress");
        String custPhoneNo = getIntent().getStringExtra("custPhoneNo");
        String custcity = getIntent().getStringExtra("custcity");
        orderNo = orderid;
        merchantId=merchant_id;
        accessCode=access_code;
        workingKey=working_key;
        totalAmount = Double.parseDouble(amount);
        deliveryName=custName;
        deliveryAddress=CustAddress;
        deliveryCity=custcity;
        deliveryTel=custPhoneNo;

        try {
            String merchantData = buildMerchantDataString();
            String encryptedData = encrypt(merchantData, workingKey);

            String postData = "encRequest=" + URLEncoder.encode(encryptedData, "UTF-8")
                    + "&access_code=" + URLEncoder.encode(accessCode, "UTF-8");

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.startsWith(redirectUrl)) {
                        // Load the URL in WebView but don't show it to user
                        view.loadUrl(url);
                        return true; // We've handled the URL
                    }
                    return false;
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        String url = request.getUrl().toString();
                        if (url.startsWith(redirectUrl)) {
                            // Load the URL in WebView but don't show it to user
                            view.loadUrl(url);
                            return true; // We've handled the URL
                        }
                    }
                    return false;
                }


                @Override
                public void onPageFinished(WebView view, String url) {
                    if (url.startsWith(redirectUrl)) {
                        webView.setVisibility(View.GONE);

                        view.evaluateJavascript("(function() { return document.body.innerText; })();", html -> {
                            try {
                                String json = html.trim();
                                if (json.startsWith("\"") && json.endsWith("\"")) {
                                    json = json.substring(1, json.length() - 1)
                                            .replace("\\n", "")
                                            .replace("\\\"", "\"");
                                }
                                JSONObject response = new JSONObject(json);
                                String status = response.optString("status", "");
                                String message = response.optString("message", "");
                                String orderId = response.optString("order_id", "");

                                Intent intent = new Intent(CcavenueWebViewActivity.this, PaymentResultActivity.class);
                                intent.putExtra("status", status);
                                intent.putExtra("message", message);
                                intent.putExtra("order_id", orderId);

                                SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                                String custId = prefs.getString("cust_id", "");

                                // Update backend status based on payment result
                                if ("success".equalsIgnoreCase(status)) {
                                    updatePaymentStatus(orderId, "success");
                                    startActivity(intent);
                                    Toast.makeText(CcavenueWebViewActivity.this, "Payment Successful!", Toast.LENGTH_LONG).show();
                                } else {
                                    updatePaymentStatus(orderId, "fail"); // or "cancelled" if needed
                                    startActivity(intent);
                                    Toast.makeText(CcavenueWebViewActivity.this, "Payment Failed or Cancelled.", Toast.LENGTH_LONG).show();

                                }


                                finish();

                            } catch (Exception e) {
                                e.printStackTrace();
                                runOnUiThread(() -> Toast.makeText(CcavenueWebViewActivity.this,
                                        "Error processing payment response.", Toast.LENGTH_LONG).show());
                                finish();
                            }
                        });
                    }
                }


                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Toast.makeText(CcavenueWebViewActivity.this,
                                "Payment error: " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }
                    finish();
                }
            });


            webView.postUrl(ccAvenuePaymentUrl, postData.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Payment Initialization failed. Try again!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private String buildMerchantDataString() throws Exception {
        StringBuilder sb = new StringBuilder();

        sb.append("currency=").append(URLEncoder.encode(currency, "UTF-8")).append("&");
        sb.append("merchant_id=").append(URLEncoder.encode(merchantId, "UTF-8")).append("&");
        sb.append("order_id=").append(URLEncoder.encode(orderNo, "UTF-8")).append("&");

        // Uncomment below if payment_option is needed
        // sb.append("payment_option=OPTUPI&");

        if ("USD".equalsIgnoreCase(currency)) {
            double amountUsd = totalAmount / usdRate;
            sb.append("amount=").append(String.format("%.2f", amountUsd)).append("&");
        } else {
            sb.append("amount=").append(String.format("%.0f", totalAmount)).append("&");
        }

        sb.append("billing_name=").append(URLEncoder.encode(deliveryName, "UTF-8")).append("&");
        sb.append("billing_address=").append(URLEncoder.encode(deliveryAddress, "UTF-8")).append("&");
        sb.append("billing_city=").append(URLEncoder.encode(deliveryCity, "UTF-8")).append("&");
        sb.append("billing_state=").append(URLEncoder.encode(deliveryState, "UTF-8")).append("&");
        sb.append("billing_country=").append(URLEncoder.encode(deliveryCountry, "UTF-8")).append("&");
        sb.append("billing_zip=").append(URLEncoder.encode(deliveryZip, "UTF-8")).append("&");
        sb.append("billing_tel=").append(URLEncoder.encode(deliveryTel, "UTF-8")).append("&");

        sb.append("redirect_url=").append(URLEncoder.encode(redirectUrl, "UTF-8")).append("&");

        return sb.toString();
    }

    private void updatePaymentStatus(String orderId,String status) {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new FormBody.Builder()
                .add("order_id", orderId)
                .add("status", status)
                .build();

        Request request = new Request.Builder()
                .url(Attributes.Payment_Status_Update)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(CcavenueWebViewActivity.this, "Failed to fetch timing: " + e.getMessage(), Toast.LENGTH_SHORT).show()
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
                    runOnUiThread(() -> Toast.makeText(CcavenueWebViewActivity.this, "Empty timing API response", Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    JSONObject responseObject = new JSONObject(responseBody);

                    if (!responseObject.optBoolean("status", false)) {
                        throw new JSONException("API returned false status");
                    }

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(CcavenueWebViewActivity.this, "Failed to parse timing: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    public static String encrypt(String plainText, String workingKey) throws Exception {
        String md5Key = md5(workingKey);
        byte[] keyBytes = hexStringToByteArray(md5Key);

        byte[] ivBytes = new byte[]{
                0x00, 0x01, 0x02, 0x03,
                0x04, 0x05, 0x06, 0x07,
                0x08, 0x09, 0x0a, 0x0b,
                0x0c, 0x0d, 0x0e, 0x0f
        };

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));

        return bytesToHex(encrypted);
    }

    private static String md5(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(s.getBytes("UTF-8"));
        return bytesToHex(digest);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
