package com.test.foodivery.Activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.test.foodivery.R;

public class PaymentResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        ImageView icon = findViewById(R.id.result_icon);
        TextView statusTv = findViewById(R.id.result_status);
        TextView msgTv = findViewById(R.id.result_message);
        TextView orderIdTv = findViewById(R.id.result_order_id);

        String status = getIntent().getStringExtra("status");
        String message = getIntent().getStringExtra("message");
        String orderId = getIntent().getStringExtra("order_id");

        boolean success = "success".equalsIgnoreCase(status);

        if(success) {
            icon.setImageResource(R.drawable.ic_success); // success drawable
            statusTv.setText("Payment Successful");
            statusTv.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            icon.setImageResource(R.drawable.ic_failure); // failure drawable
            statusTv.setText("Payment Failed");
            statusTv.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        msgTv.setText(message != null ? message : "");
        orderIdTv.setText(orderId != null ? "Order ID: " + orderId : "");
    }
}
