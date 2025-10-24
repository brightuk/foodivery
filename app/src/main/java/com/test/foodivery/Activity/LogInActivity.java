package com.test.foodivery.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.hbb20.CountryCodePicker;
import com.test.foodivery.Attributes.Attributes;
import com.test.foodivery.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LogInActivity extends AppCompatActivity {

    CountryCodePicker countryCodeHolder;
    EditText phoneNum;
    private ScrollView scrollView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_log_in);
        Button btnOpen = findViewById(R.id.logbtn);
//        countryCodeHolder = findViewById(R.id.countryCodeHolder); // Uncommented this line
        phoneNum = findViewById(R.id.phoneNum);
        scrollView = findViewById(R.id.scrollView);
        setupKeyboardListener();

        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!phoneNum.getText().toString().isEmpty()) {
                    if (phoneNum.getText().toString().length() == 10) {
                        String fullPhoneNumber = phoneNum.getText().toString();

                        // Pass OTP to OtpActivity
                        sendSMS(fullPhoneNumber, "verifyotp");

                        Intent intent = new Intent(LogInActivity.this, OtpActivity.class);
                        intent.putExtra("phone", fullPhoneNumber);
                        startActivity(intent);
                    } else {
                        Toast.makeText(LogInActivity.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LogInActivity.this, "Please enter your number", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupKeyboardListener() {
        final View activityRootView = findViewById(R.id.main);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                activityRootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = activityRootView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) { // Keyboard is visible
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.scrollTo(0, phoneNum.getBottom());
                        }
                    });
                } else { // Keyboard is hidden
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.scrollTo(0, 0);
                        }
                    });
                }
            }
        });

        // Focus change listener to scroll to EditText when focused
        phoneNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.smoothScrollTo(0, phoneNum.getBottom());
                        }
                    });
                }
            }
        });
    }

    public void sendSMS(String phone, String smsType) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("mobileNo", phone)
                .add("smsType", smsType)
                .build();

        Request request = new Request.Builder()
                .url(Attributes.User_Profile_Created) // Make sure this URL is correct
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LogInActivity.this, "API Request Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Log.e("API_ERROR", "Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(LogInActivity.this, "API Error: " + response.code(), Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    Log.d("SMS_RESPONSE", responseBody);

                    JSONObject responseObject = new JSONObject(responseBody);
                    // Handle the response as needed

                } catch (JSONException e) {
                    Log.e("JSON_ERROR", "Parsing failed: " + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(LogInActivity.this, "Response parsing error", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}