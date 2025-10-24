package com.test.foodivery.Activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.test.foodivery.Attributes.Attributes;
import com.test.foodivery.R;

import org.json.JSONArray;
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

public class ProfileUserDetails extends AppCompatActivity {

    private EditText etName, etMobile, etEmail, etAddress, etOtp;
    private Button btnSave;
    private Button btnVerifyOtp;
    private Button btnGenerateOtp;
    private Button btnResendOtp;
    private ImageView backBtn;
    private LinearLayout otpContainer;
    private boolean isUpdating = false;
    private boolean isOtpVerified = false;
    private String originalMobile = "";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_user_details);

        initializeViews();
        setupListeners();
        loadUserData();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        etOtp = findViewById(R.id.etOtp);
        btnSave = findViewById(R.id.btnSave);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnGenerateOtp = findViewById(R.id.btnGenerateOtp);
        btnResendOtp = findViewById(R.id.btnResendOtp);
        otpContainer = findViewById(R.id.otpContainer);
        backBtn = findViewById(R.id.backButton);
    }

    private void setupListeners() {
        backBtn.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            if (!isUpdating && isOtpVerified) {
                saveUserDetails();
            }
        });

        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
        
        btnGenerateOtp.setOnClickListener(v -> generateOtp());
        
        btnResendOtp.setOnClickListener(v -> resendOtp());

        // Add text change listeners to enable/disable save button
        etName.addTextChangedListener(new SimpleTextWatcher(this::validateForm));
        etMobile.addTextChangedListener(new SimpleTextWatcher(this::validateForm));
        etEmail.addTextChangedListener(new SimpleTextWatcher(this::validateForm));
        etAddress.addTextChangedListener(new SimpleTextWatcher(this::validateForm));
        etOtp.addTextChangedListener(new SimpleTextWatcher(this::validateForm));

        // Check if mobile number changed to show Generate OTP button
        etMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String newMobile = s.toString().trim();
                if (!newMobile.equals(originalMobile) && newMobile.length() == 10) {
                    showGenerateOtpButton();
                } else if (newMobile.equals(originalMobile)) {
                    hideOtpVerification();
                } else {
                    hideGenerateOtpButton();
                }
                validateForm();
            }
        });
    }

    private void validateForm() {
        String mobile = etMobile.getText().toString().trim();
        boolean mobileChanged = !mobile.equals(originalMobile);
        
        boolean isValid = !etName.getText().toString().trim().isEmpty() &&
                !mobile.isEmpty() &&
                !etEmail.getText().toString().trim().isEmpty() &&
                !etAddress.getText().toString().trim().isEmpty() &&
                (!mobileChanged || isOtpVerified);

        btnSave.setEnabled(isValid);
        btnSave.setAlpha(isValid ? 1f : 0.5f);
    }

    private void showOtpVerification() {
        otpContainer.setVisibility(View.VISIBLE);
        btnVerifyOtp.setVisibility(View.VISIBLE);
        btnResendOtp.setVisibility(View.VISIBLE);
        btnGenerateOtp.setVisibility(View.GONE);
        isOtpVerified = false;
        validateForm();
    }

    private void hideOtpVerification() {
        otpContainer.setVisibility(View.GONE);
        btnVerifyOtp.setVisibility(View.GONE);
        btnResendOtp.setVisibility(View.GONE);
        btnGenerateOtp.setVisibility(View.GONE);
        isOtpVerified = true;
        validateForm();
    }
    private void generateOtp() {
        String mobile = etMobile.getText().toString().trim();

        if (mobile.isEmpty() || mobile.length() != 10) {
            showToast("Please enter a valid 10-digit mobile number");
            return;
        }

        btnGenerateOtp.setEnabled(false);
        btnGenerateOtp.setText("Generating...");
        etMobile.setEnabled(false);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("mobileNo", mobile)
                .add("smsType", "verifyotp")
                .build();

        Request request = new Request.Builder()
                .url(Attributes.User_Profile_Created)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    btnGenerateOtp.setEnabled(true);
                    btnGenerateOtp.setText("Generate OTP");
                    etMobile.setEnabled(true);
                    showToast("Failed to generate OTP");
                    Log.e("OtpGenerate", "Network error", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    Log.d("OtpGenerateResponse", responseBody);
                    JSONObject responseObject = new JSONObject(responseBody);

                    runOnUiThread(() -> {
                        btnGenerateOtp.setEnabled(true);
                        btnGenerateOtp.setText("Generate OTP");
                        etMobile.setEnabled(true);

                        try {
                            // Check if the response indicates success
                            if (responseObject.has("status") && responseObject.getBoolean("status")) {
                                showToast("OTP sent successfully to " + mobile);
                                showOtpVerification();
                            } else if (responseObject.has("message")) {
                                String message = responseObject.getString("message");
                                showToast(message);
                            } else {
                                // If no status field, assume success (like in LogInActivity)
                                showToast("OTP sent successfully to " + mobile);
                                showOtpVerification();
                            }
                        } catch (JSONException e) {
                            // If JSON parsing fails, assume success (like in LogInActivity)
                            showToast("OTP sent successfully to " + mobile);
                            showOtpVerification();
                            Log.d("OtpGenerate", "Response parsing issue, but proceeding with OTP flow");
                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        btnGenerateOtp.setEnabled(true);
                        btnGenerateOtp.setText("Generate OTP");
                        etMobile.setEnabled(true);
                        showToast("Error processing OTP generation");
                        Log.e("OtpGenerate", "Error parsing response", e);
                    });
                }
            }
        });
    }



    private void verifyOtp() {
        String mobile = etMobile.getText().toString().trim();
        String otp = etOtp.getText().toString().trim();

        if (mobile.isEmpty() || mobile.length() != 10) {
            showToast("Please enter a valid mobile number");
            return;
        }

        if (otp.isEmpty() || otp.length() != 4) {
            showToast("Please enter a valid 4-digit OTP");
            return;
        }

        btnVerifyOtp.setEnabled(false);
        btnVerifyOtp.setText("Verifying...");

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("phone", mobile)
                .add("otp", otp)
                .build();

        Request request = new Request.Builder()
                .url(Attributes.OTP_Verify)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    btnVerifyOtp.setEnabled(true);
                    btnSave.setEnabled(false);
                    btnVerifyOtp.setText("Verify OTP");
                    showToast("Failed to verify OTP");
                    Log.e("OtpVerify", "Network error", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    Log.d("OtpVerifyResponse", responseBody);
                    JSONObject responseObject = new JSONObject(responseBody);

                    runOnUiThread(() -> {
                        btnVerifyOtp.setEnabled(true);
                        btnSave.setEnabled(false);
                        btnVerifyOtp.setText("Verify OTP");

                        try {
                            if (responseObject.getBoolean("status")) {
                                showToast("OTP verified successfully");
                                isOtpVerified = true;
                                originalMobile = mobile;
                                hideOtpVerification();
                                validateForm();
                            } else {
                                String message = responseObject.optString("message", "Invalid OTP");
                                showToast(message);
                                isOtpVerified = false;
                            }
                        } catch (JSONException e) {
                            showToast("Error processing OTP verification");
                            Log.e("OtpVerify", "Error parsing response", e);
                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        btnVerifyOtp.setEnabled(true);
                        btnSave.setEnabled(false);
                        btnVerifyOtp.setText("Verify OTP");
                        showToast("Error processing OTP verification");
                        Log.e("OtpVerify", "Error parsing response", e);
                    });
                }
            }
        });
    }

    private void loadUserData() {
        String custId = checkUserSession();
        if (custId.isEmpty()) {
            showToast("User session not found");
            finish();
            return;
        }
        fetchUserDetails(custId);
    }

    private void saveUserDetails() {
        String name = etName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (!validateInputs(name, mobile, email, address)) {
            return;
        }

        isUpdating = true;
        btnSave.setEnabled(false);
        showProgress("Saving profile...");

        String custId = checkUserSession();
        userProfileUpdate(custId, name, mobile, email, address);
    }

    private boolean validateInputs(String name, String mobile, String email, String address) {
        if (name.isEmpty()) {
            etName.setError("Name is required");
            return false;
        }

        if (mobile.isEmpty() || mobile.length() != 10) {
            etMobile.setError("Valid 10-digit mobile number required");
            return false;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email required");
            return false;
        }

        if (address.isEmpty()) {
            etAddress.setError("Address is required");
            return false;
        }

        return true;
    }

    private void fetchUserDetails(String custId) {
        showProgress("Loading profile...");

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("cust_id", custId)
                .build();

        Request request = new Request.Builder()
                .url(Attributes.Fetch_User_details)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    hideProgress();
                    showToast("Failed to fetch user details");
                    Log.e("FetchUserDetails", "Network error", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    Log.d("UserDetailsResponse", responseBody);
                    JSONObject responseObject = new JSONObject(responseBody);

                    if (!responseObject.getBoolean("status") || !responseObject.has("data")) {
                        throw new Exception("Invalid response format");
                    }

                    JSONArray dataArray = responseObject.getJSONArray("data");
                    if (dataArray.length() == 0) {
                        throw new Exception("No user data found");
                    }

                    JSONObject userData = dataArray.getJSONObject(0);
                    final String name = userData.optString("customer_name", "");
                    final String mobile = userData.optString("customer_mobile", "");
                    final String email = userData.optString("customer_email", "");
                    final String address = userData.optString("customer_address", "");
                    final String area = userData.optString("customer_area", "");

                    runOnUiThread(() -> {
                        hideProgress();
                        populateUserData(name, mobile, email, address, area);
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        hideProgress();
                        showToast("Error processing user details");
                        Log.e("FetchUserDetails", "Error parsing response", e);
                    });
                }
            }
        });
    }

    private void populateUserData(String name, String mobile, String email, String address, String area) {
        etName.setText(name);
        etMobile.setText(mobile);
        originalMobile = mobile;
        etEmail.setText(email);

        String fullAddress = "";
        if (!area.isEmpty() && !address.isEmpty()) {
            fullAddress = area + ", " + address;
        } else if (!area.isEmpty()) {
            fullAddress = area;
        } else {
            fullAddress = address;
        }
        etAddress.setText(fullAddress);

        validateForm();
    }

    private void userProfileUpdate(String custId, String name, String mobile, String email, String address) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("cust_id", custId)
                .add("name", name)
                .add("mobile", mobile)
                .add("email", email)
                .add("address", address)
                .build();

        Request request = new Request.Builder()
                .url(Attributes.Fetch_User_details_update)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    isUpdating = false;
                    btnSave.setEnabled(true);
                    hideProgress();
                    showToast("Failed to update profile");
                    Log.e("ProfileUpdate", "Network error", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    JSONObject responseObject = new JSONObject(responseBody);

                    runOnUiThread(() -> {
                        isUpdating = false;
                        btnSave.setEnabled(true);
                        hideProgress();

                        try {
                            if (responseObject.getBoolean("status")) {
                                showToast("Profile updated successfully");
                                // Optional: Refresh data
                                fetchUserDetails(custId);
                            } else {
                                String message = responseObject.optString("message", "Failed to update profile");
                                showToast(message);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        isUpdating = false;
                        btnSave.setEnabled(true);
                        hideProgress();
                        showToast("Error processing update");
                        Log.e("ProfileUpdate", "Error parsing response", e);
                    });
                }
            }
        });
    }

    private void showProgress(String message) {
        // Implement your progress dialog here
        btnSave.setText("Saving...");
    }

    private void hideProgress() {
        // Hide your progress dialog
        btnSave.setText("Save");
    }

    private void showToast(String message) {
        if (!isFinishing() && !isDestroyed()) {
            runOnUiThread(() ->
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show());
        }
    }

    private String checkUserSession() {
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        return prefs.getString("cust_id", "");
    }

    private void showGenerateOtpButton() {
        btnGenerateOtp.setVisibility(View.VISIBLE);
        etMobile.setEnabled(true);
    }

    private void hideGenerateOtpButton() {
        btnGenerateOtp.setVisibility(View.GONE);
    }

    private void resendOtp() {
        String mobile = etMobile.getText().toString().trim();
        if (mobile.isEmpty() || mobile.length() != 10) {
            showToast("Please enter a valid 10-digit mobile number");
            return;
        }

        btnResendOtp.setEnabled(false);
        btnResendOtp.setText("Resending...");
        etMobile.setEnabled(false);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("mobileNo", mobile)
                .add("smsType", "verifyotp")
                .build();

        Request request = new Request.Builder()
                .url(Attributes.User_Profile_Created)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    btnResendOtp.setEnabled(true);
                    btnResendOtp.setText("Resend OTP");
                    etMobile.setEnabled(true);
                    showToast("Failed to resend OTP");
                    Log.e("ResendOtp", "Network error", e);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    Log.d("ResendOtpResponse", responseBody);
                    JSONObject responseObject = new JSONObject(responseBody);

                    runOnUiThread(() -> {
                        btnResendOtp.setEnabled(true);
                        btnResendOtp.setText("Resend OTP");
                        etMobile.setEnabled(true);

                        try {
                            if (responseObject.has("status") && responseObject.getBoolean("status")) {
                                showToast("OTP resent successfully to " + mobile);
                                showOtpVerification();
                            } else if (responseObject.has("message")) {
                                String message = responseObject.getString("message");
                                showToast(message);
                            } else {
                                showToast("OTP resent successfully to " + mobile);
                                showOtpVerification();
                            }
                        } catch (JSONException e) {
                            showToast("Error processing resend OTP");
                            Log.e("ResendOtp", "Error parsing response", e);
                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        btnResendOtp.setEnabled(true);
                        btnResendOtp.setText("Resend OTP");
                        etMobile.setEnabled(true);
                        showToast("Error processing resend OTP");
                        Log.e("ResendOtp", "Error parsing response", e);
                    });
                }
            }
        });
    }

    // Simple TextWatcher implementation
    private static class SimpleTextWatcher implements TextWatcher {
        private final Runnable callback;

        SimpleTextWatcher(Runnable callback) {
            this.callback = callback;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            callback.run();
        }
    }
}