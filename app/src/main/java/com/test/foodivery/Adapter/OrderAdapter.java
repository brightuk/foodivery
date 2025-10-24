package com.test.foodivery.Adapter;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.test.foodivery.Database.CartDatabaseHelper;
import com.test.foodivery.Model.CartModel;
import com.test.foodivery.R;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.CartViewHolder> {
    private List<CartModel> cartList;
    private Context context;
    private CartDatabaseHelper dbHelper;

    public OrderAdapter(Context context, List<CartModel> cartList,CartDatabaseHelper dbHelper) {
        this.context = context;
        this.cartList = cartList;
//        this.dbHelper = new CartDatabaseHelper(context);
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_view_item, parent, false);
        return new CartViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartModel item = cartList.get(position);

        holder.productName.setText(item.getProductName());
        // Show unit price x qty = line total
        try {
            double unitPrice = Double.parseDouble(item.getPrice());
            int qty = Math.max(1, item.getQuantity());
            double lineTotal = unitPrice * qty;
            holder.productPrice.setText("₹ " + String.format("%.2f", lineTotal) + "  (" + qty + "x)");
        } catch (Exception e) {
            holder.productPrice.setText("₹ " + item.getPrice());
        }



    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice;


        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.cartProductTitle);
            productPrice = itemView.findViewById(R.id.cartProductPrice);



        }
    }
}
