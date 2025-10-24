package com.test.foodivery.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.test.foodivery.Database.CartDatabaseHelper;
import com.test.foodivery.Model.CartModel;
import com.test.foodivery.R;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

// ... imports & package ...

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartModel> cartList;
    private Context context;
    private CartDatabaseHelper dbHelper;
    private OnItemDeletedListener deletedListener;
    private final Set<String> expandedProductIds = new HashSet<>();

    public CartAdapter(Context context, List<CartModel> cartList, CartDatabaseHelper dbHelper, OnItemDeletedListener deletedListener) {
        this.context = context;
        this.cartList = cartList;
        this.dbHelper = dbHelper;
        this.deletedListener = deletedListener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_view_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartModel item = cartList.get(position);

        holder.productName.setText(item.getProductName());
        // Display line total = price * quantity
        try {
            double unitPrice = Double.parseDouble(item.getPrice());
            int qty = Math.max(1, item.getQuantity());
            double lineTotal = unitPrice * qty;
            holder.productPrice.setText("₹ " + String.format("%.2f", lineTotal));
        } catch (Exception e) {
            holder.productPrice.setText("₹ " + item.getPrice());
        }
        Log.d("cartasssd",String.valueOf(item.getSelectedOptions()));
        String optionsText = item.getSelectedOptions() == null ? "" : item.getSelectedOptions();
        holder.productOptions.setText(optionsText);
        boolean hasOptions = optionsText.trim().length() > 0;
        holder.detailsToggle.setVisibility(hasOptions ? View.VISIBLE : View.GONE);

        // Expand/collapse state
        boolean isExpanded = hasOptions && expandedProductIds.contains(item.getProductId());
        holder.detailsContainer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.detailsExpandIcon.setRotation(isExpanded ? 180f : 0f);
        holder.detailsLabel.setText(isExpanded ? "Hide details" : "View details");

        holder.detailsToggle.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;
            CartModel current = cartList.get(adapterPos);
            String key = current.getProductId();
            if (expandedProductIds.contains(key)) {
                expandedProductIds.remove(key);
            } else {
                expandedProductIds.add(key);
            }
            notifyItemChanged(adapterPos);
        });

        Glide.with(holder.itemView.getContext())
                .load(item.getProductImage())
                .into(holder.productImage);

        holder.cartDelete.setOnClickListener(v -> {
            int positiona = holder.getAdapterPosition();
            if (positiona != RecyclerView.NO_POSITION) {
                CartModel itema = cartList.get(positiona);
                dbHelper.deleteItem(itema.getProductId());
                cartList.remove(positiona);
                notifyItemRemoved(positiona);
                if (deletedListener != null) {
                    deletedListener.onItemDeleted();
                }
            }
        });

        // ==== EDIT CART FUNCTIONALITY ====
        holder.cartEdit.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                CartModel editingItem = cartList.get(pos);
                Intent intent = new Intent(context, com.test.foodivery.Activity.ProductViewActivity.class);
                // Pass all needed product details for editing
                intent.putExtra("image_url", editingItem.getProductImage());
                intent.putExtra("productTitle", editingItem.getProductName());
                intent.putExtra("productPrice", editingItem.getPrice());
                intent.putExtra("id", editingItem.getProductId());
                intent.putExtra("gst", editingItem.getProductGst());
                intent.putExtra("gstInc", editingItem.getGstInc());
                intent.putExtra("shopid", editingItem.getShopId());
                intent.putExtra("shopLogo", "null"); // Add if you have it
                intent.putExtra("bizIds", "null");   // Add if available



                // for edit prefill
                intent.putExtra("cart_edit", true);
                intent.putExtra("product_options", editingItem.getSelectedOptions());
                intent.putExtra("allVariantIds", editingItem.getGetallVariantIds());
                intent.putExtra("product_weight", editingItem.getGetproduct_Weight());
                intent.putExtra("product_message", editingItem.getMessage());

                if (!(context instanceof android.app.Activity)) return;
                ((android.app.Activity) context).startActivity(intent);
            }
        });

        // Quantity controls
        holder.quantityText.setText(String.valueOf(Math.max(1, item.getQuantity())));
        holder.increaseQuantity.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;
            CartModel model = cartList.get(adapterPos);
            int newQty = Math.max(1, model.getQuantity()) + 1;
            if (dbHelper.updateQuantity(model.getProductId(), newQty)) {
                model.setQuantity(newQty);
                notifyItemChanged(adapterPos);
                if (deletedListener != null) deletedListener.onItemDeleted();
            }
        });
        holder.decreaseQuantity.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;
            CartModel model = cartList.get(adapterPos);
            int newQty = Math.max(0, model.getQuantity() - 1);
            if (newQty == 0) {
                // delete item
                if (dbHelper.deleteItem(model.getProductId())) {
                    cartList.remove(adapterPos);
                    notifyItemRemoved(adapterPos);
                    if (deletedListener != null) deletedListener.onItemDeleted();
                }
            } else {
                if (dbHelper.updateQuantity(model.getProductId(), newQty)) {
                    model.setQuantity(newQty);
                    notifyItemChanged(adapterPos);
                    if (deletedListener != null) deletedListener.onItemDeleted();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }


    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productOptions, quantityText;
        ImageView productImage, cartDelete, cartEdit, decreaseQuantity, increaseQuantity, detailsExpandIcon;
        View detailsToggle, detailsContainer;
        TextView detailsLabel;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.cartProductName);
            productPrice = itemView.findViewById(R.id.cartProductPrice);
            productOptions = itemView.findViewById(R.id.cartProductOptions);
            productImage = itemView.findViewById(R.id.cartProductImage);
            cartDelete = itemView.findViewById(R.id.cartDelete);
            cartEdit = itemView.findViewById(R.id.cartEdit);
            decreaseQuantity = itemView.findViewById(R.id.decreaseQuantity);
            increaseQuantity = itemView.findViewById(R.id.increaseQuantity);
            quantityText = itemView.findViewById(R.id.quantityText);
            detailsToggle = itemView.findViewById(R.id.detailsToggle);
            detailsContainer = itemView.findViewById(R.id.detailsContainer);
            detailsExpandIcon = itemView.findViewById(R.id.detailsExpandIcon);
            detailsLabel = itemView.findViewById(R.id.detailsLabel);
        }
    }

    public interface OnItemDeletedListener {
        void onItemDeleted();
    }
}
