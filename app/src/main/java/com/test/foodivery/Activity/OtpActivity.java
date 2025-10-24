package com.test.foodivery.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.test.foodivery.Attributes.Attributes;
import com.test.foodivery.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OtpActivity extends AppCompatActivity {

    EditText otp1, otp2, otp3, otp4;
    TextView numShow, btnResendOtp;
    Button otpConfirm;
    private static final int SMS_PERMISSION_CODE = 100;
    private SmsBroadcastReceiver smsBroadcastReceiver;
    private boolean isAutoVerifying = false;
    private boolean isAutoFillEnabled = true; // Flag to control auto-fill
    private TextView btnEnterManually;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        Intent intent = getIntent();
        String receivedText = intent.getStringExtra("phone");
        numShow = findViewById(R.id.numShow);
        numShow.setText(receivedText);
        requestSmsPermission();

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(OtpActivity.this, Manifest.permission.POST_NOTIFICATIONS)
//                    != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(OtpActivity.this,
//                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
//                        101);
//            }
//        }

        // Request SMS permissions


        ImageView btnBack = findViewById(R.id.goBackBtn);
        btnBack.setOnClickListener(v -> finish());

        TextView backText = findViewById(R.id.goBackText);
        backText.setOnClickListener(v -> finish());

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        btnResendOtp = findViewById(R.id.btnResendOtp);

        // Add the "Enter Manually" button
        btnEnterManually = findViewById(R.id.btnEnterManually);
        if (btnEnterManually != null) {
            btnEnterManually.setOnClickListener(v -> toggleManualEntry());
        }

        new Handler().postDelayed(() -> {
            otp1.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(otp1, InputMethodManager.SHOW_IMPLICIT);
        }, 100);

        btnResendOtp.setOnClickListener(V -> {
            sendSMS(receivedText, "verifyotp");
        });

        EditText[] editTexts = {otp1, otp2, otp3, otp4};

        for (int i = 0; i < editTexts.length; i++) {
            int finalI = i;

            editTexts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 0 && finalI < editTexts.length - 1) {
                        editTexts[finalI + 1].requestFocus();
                    }

                    // Auto-verify when all fields are filled only if auto-fill is enabled
                    if (isAllOtpFieldsFilled() && !isAutoVerifying && isAutoFillEnabled) {
                        isAutoVerifying = true;
                        autoVerifyOtp();
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });

            editTexts[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (editTexts[finalI].getText().toString().isEmpty() && finalI > 0) {
                        editTexts[finalI - 1].requestFocus();
                    }
                }
                return false;
            });
        }

        otpConfirm = findViewById(R.id.otpConfirm);
        otpConfirm.setOnClickListener(v -> {
            if (isAllOtpFieldsFilled()) {
                String OtpS = otp1.getText().toString() + otp2.getText().toString() +
                        otp3.getText().toString() + otp4.getText().toString();
                String phoneNo = intent.getStringExtra("phone");
                Log.d("numbers", phoneNo + "/" + OtpS);

                customerDetails(phoneNo, OtpS);
            }
        });

        // Register SMS receiver only if auto-fill is enabled
        if (isAutoFillEnabled) {
            registerSmsReceiver();
        }
    }



    private void toggleManualEntry() {
        isAutoFillEnabled = !isAutoFillEnabled;

        if (isAutoFillEnabled) {
            // Enable auto-fill
            if (btnEnterManually != null) {
                btnEnterManually.setText("Enter Manually");
            }
            Toast.makeText(this, "Auto-fill enabled", Toast.LENGTH_SHORT).show();
            registerSmsReceiver();
        } else {
            // Disable auto-fill
            if (btnEnterManually != null) {
                btnEnterManually.setText("Auto-fill OTP");
            }
            Toast.makeText(this, "Manual entry enabled", Toast.LENGTH_SHORT).show();
            unregisterSmsReceiver();

            // Show keyboard for manual entry
            otp1.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(otp1, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void sendSMS(String phone, String smsType) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("mobileNo", phone)
                .add("smsType", smsType)
                .build();

        Request request = new Request.Builder()
                .url(Attributes.User_Profile_Created)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(OtpActivity.this, "API Request Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Log.e("API_ERROR", "Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) return;
                try {
                    String responseBody = response.body().string();
                    Log.d("SMS_RESPONSE", responseBody);

                    JSONObject responseObject = new JSONObject(responseBody);
                    // Handle response if needed

                } catch (JSONException e) {
                    Log.e("JSON_ERROR", "Parsing failed: " + e.getMessage());
                }
            }
        });
    }

    private boolean isAllOtpFieldsFilled() {
        return !otp1.getText().toString().isEmpty() &&
                !otp2.getText().toString().isEmpty() &&
                !otp3.getText().toString().isEmpty() &&
                !otp4.getText().toString().isEmpty();
    }

    private void autoVerifyOtp() {
        // Auto-click the confirm button after a short delay
        new Handler().postDelayed(() -> {
            if (isAllOtpFieldsFilled() && isAutoFillEnabled) {
                otpConfirm.performClick();
            }
            isAutoVerifying = false;
        }, 500); // 500ms delay to allow user to see the OTP being filled
    }

    private void requestSmsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS},
                        SMS_PERMISSION_CODE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
                if (isAutoFillEnabled) {
                    registerSmsReceiver();
                }
            } else {
                Toast.makeText(this, "SMS permission denied. Please enter OTP manually.", Toast.LENGTH_SHORT).show();
                isAutoFillEnabled = false;
                if (btnEnterManually != null) {
                    btnEnterManually.setText("Auto-fill OTP");
                }
            }
        }
    }

    private void registerSmsReceiver() {
        try {
            if (smsBroadcastReceiver == null) {
                smsBroadcastReceiver = new SmsBroadcastReceiver();
                smsBroadcastReceiver.setOtpListener(new SmsBroadcastReceiver.OtpListener() {
                    @Override
                    public void onOtpReceived(String otp) {
                        if (isAutoFillEnabled) { // Only auto-fill if enabled
                            // Auto-fill OTP
                            if (otp.length() == 4) {
                                otp1.setText(String.valueOf(otp.charAt(0)));
                                otp2.setText(String.valueOf(otp.charAt(1)));
                                otp3.setText(String.valueOf(otp.charAt(2)));
                                otp4.setText(String.valueOf(otp.charAt(3)));

                                // Hide keyboard
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(otp4.getWindowToken(), 0);
                            }
                        }
                    }

                    @Override
                    public void onOtpTimeout() {
                        if (isAutoFillEnabled) {
                            Toast.makeText(OtpActivity.this, "SMS not received, please enter manually", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
            intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
            registerReceiver(smsBroadcastReceiver, intentFilter);
        } catch (Exception e) {
            Log.e("SMS_Receiver", "Error registering SMS receiver: " + e.getMessage());
        }
    }

    private void unregisterSmsReceiver() {
        try {
            if (smsBroadcastReceiver != null) {
                unregisterReceiver(smsBroadcastReceiver);
            }
        } catch (Exception e) {
            Log.e("SMS_Receiver", "Error unregistering SMS receiver: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterSmsReceiver();
    }

    private void customerDetails(String phone, String OtpS) {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("phone", phone)
                .add("otp", OtpS)
                .build();

        Request request = new Request.Builder()
                .url(Attributes.OTP_Verify)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(OtpActivity.this, "API Request Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Log.e("API_ERROR", "Request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(OtpActivity.this, "OTP Verification Failed", Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    String responseBody = response.body().string();
                    Log.d("OTP_RESPONSE", responseBody);

                    JSONObject responseObject = new JSONObject(responseBody);
                    String status = responseObject.getString("status");
                    if (status.equals("true")) {
                        String data = responseObject.getString("data");
                        String customer_mobile = responseObject.getString("customer_mobile");
                        String token = responseObject.getString("token");
                        runOnUiThread(() -> {

                            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.putString("cust_id", data);
                            editor.putString("mobileNo", customer_mobile);
                            editor.putString("token", token);
                            Log.d("mobileeeeee", customer_mobile);
                            editor.apply();
                            // Start MainActivity only on success
                            Intent intent = new Intent(OtpActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    } else {
                        // Show failure message if status is false
                        runOnUiThread(() -> Toast.makeText(OtpActivity.this, "OTP Verification Failed", Toast.LENGTH_SHORT).show());
                    }

                } catch (JSONException e) {
                    Log.e("JSON_ERROR", "Parsing failed: " + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(OtpActivity.this, "JSON Parsing Error", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }


    // SMS Broadcast Receiver class
    public static class SmsBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "SmsBroadcastReceiver";
        private OtpListener otpListener;

        public void setOtpListener(OtpListener otpListener) {
            this.otpListener = otpListener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    try {
                        Object[] pdus = (Object[]) bundle.get("pdus");
                        if (pdus != null) {
                            for (Object pdu : pdus) {
                                android.telephony.SmsMessage smsMessage = android.telephony.SmsMessage.createFromPdu((byte[]) pdu);
                                String message = smsMessage.getMessageBody();

                                // Extract OTP from message
                                Pattern pattern = Pattern.compile("(|^)\\d{4}");
                                Matcher matcher = pattern.matcher(message);
                                if (matcher.find()) {
                                    String otp = matcher.group(0);
                                    if (otpListener != null) {
                                        otpListener.onOtpReceived(otp);
                                    }
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing SMS: " + e.getMessage());
                    }
                }
            }
        }

        public interface OtpListener {
            void onOtpReceived(String otp);
            void onOtpTimeout();
        }
    }
}