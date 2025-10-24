package com.test.foodivery.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.test.foodivery.Activity.MainActivity;
import com.test.foodivery.Activity.OrderActivity;
import com.test.foodivery.Adapter.CartAdapter;
import com.test.foodivery.Database.CartDatabaseHelper;
import com.test.foodivery.Model.CartModel;
import com.test.foodivery.R;

import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnItemDeletedListener {

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private CartDatabaseHelper dbHelper;
    private TextView subtotalTextView, gstTextView, totalTextView, emptyCartText;
    private LinearLayout summaryLayout;
    private Button placeOrderButton, continueBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new CartDatabaseHelper(getContext());

        cartRecyclerView = view.findViewById(R.id.cartRecyclerView);
        subtotalTextView = view.findViewById(R.id.subTotal);
        gstTextView = view.findViewById(R.id.subGst);
        totalTextView = view.findViewById(R.id.grandTotal);
        emptyCartText = view.findViewById(R.id.emptyCartText);
        continueBtn = view.findViewById(R.id.continueBtn);
        summaryLayout = view.findViewById(R.id.summaryLayout);
        placeOrderButton = view.findViewById(R.id.placeOrder);

        continueBtn.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), MainActivity.class));
        });

        List<CartModel> cartItems = dbHelper.getAllCartItems();

        if (cartItems == null) {
            Toast.makeText(getContext(), "No items in cart", Toast.LENGTH_SHORT).show();
            return;
        }

        cartAdapter = new CartAdapter(getContext(), cartItems, dbHelper, this);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartRecyclerView.setAdapter(cartAdapter);

        calculateTotals();
        loadCartItems();

        placeOrderButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), OrderActivity.class);
            intent.putExtra("subtotal", subtotalTextView.getText().toString());
            intent.putExtra("gst", gstTextView.getText().toString());
            intent.putExtra("total", totalTextView.getText().toString());
            startActivity(intent);
        });
    }

    private void loadCartItems() {
        List<CartModel> cartItems = dbHelper.getAllCartItems();
        if (cartItems == null || cartItems.isEmpty()) {
            cartRecyclerView.setVisibility(View.GONE);
            summaryLayout.setVisibility(View.GONE);
            placeOrderButton.setVisibility(View.GONE);
            emptyCartText.setVisibility(View.VISIBLE);
            emptyCartText.setText("Your cart is empty!");
        } else {
            cartRecyclerView.setVisibility(View.VISIBLE);
            summaryLayout.setVisibility(View.VISIBLE);
            placeOrderButton.setVisibility(View.VISIBLE);
            emptyCartText.setVisibility(View.GONE);
        }
    }

    private void calculateTotals() {
        List<CartModel> cartItems = dbHelper.getAllCartItems();
        double subtotal = 0.0;
        double totalGst = 0.0;
        for (CartModel item : cartItems) {
            int qty = Math.max(1, item.getQuantity());
            double unitPrice = Double.parseDouble(item.getPrice());
            double linePrice = unitPrice * qty;
            double gstRate = Double.parseDouble(item.getProductGst());
            String gstInc = item.getGstInc();
            double itemGst = linePrice * (gstRate / 100);
            if ("1".equals(gstInc)) {
                subtotal += linePrice;
            } else {
                subtotal += linePrice - itemGst;
            }
            totalGst += itemGst;
        }
        double total = subtotal + totalGst;
        subtotalTextView.setText("Subtotal: ₹" + String.format("%.2f", subtotal));
        gstTextView.setText("GST: ₹" + String.format("%.2f", totalGst));
        totalTextView.setText("Total: ₹" + String.format("%.2f", total));
    }

    @Override
    public void onItemDeleted() {
        calculateTotals();
        loadCartItems();
    }
}