package com.test.foodivery.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.foodivery.Activity.HistoryViewActivity;
import com.test.foodivery.Model.OrderHistoryModel;
import com.test.foodivery.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PendingOrderAdapter extends RecyclerView.Adapter<PendingOrderAdapter.PendingOrderViewHolder> {

    private List<OrderHistoryModel> pendingOrders;
    private Context context;

    public PendingOrderAdapter(Context context, List<OrderHistoryModel> pendingOrders) {
        this.context = context;
        this.pendingOrders = pendingOrders;
    }

    @NonNull
    @Override
    public PendingOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pending_order_card, parent, false);
        return new PendingOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingOrderViewHolder holder, int position) {
        OrderHistoryModel order = pendingOrders.get(position);
        
        // Set order status and number
        holder.tvOrderStatus.setText("Order #" + order.getOrderNo() + " • " + getStatusText(order.getDeliveryStatus()));
        
        // Set order time
        String orderTime = formatOrderTime(order.getOrderedDate());
        holder.tvOrderTime.setText(orderTime);
        
        // Set restaurant name from order details
        String restaurantName = getRestaurantName(order);
        holder.tvRestaurantName.setText(restaurantName);
        
        // Set order items and amount
        int itemCount = order.getProducts() != null ? order.getProducts().size() : 0;
        holder.tvOrderItems.setText(itemCount + " items • ₹" + order.getTotalAmount());
        
        // Set progress based on delivery status
        setProgressStatus(holder, order.getDeliveryStatus());
        
        // Set click listeners
        holder.btnTrackOrder.setOnClickListener(v -> {
            // Navigate to order tracking
            Intent intent = new Intent(context, HistoryViewActivity.class);
            intent.putExtra("order_id", order.getOrderId());
            context.startActivity(intent);
        });
        
        holder.btnViewDetails.setOnClickListener(v -> {
            // Navigate to order details
            Intent intent = new Intent(context, HistoryViewActivity.class);
            intent.putExtra("order_id", order.getOrderId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return pendingOrders.size();
    }

    public void updateData(List<OrderHistoryModel> newOrders) {
        this.pendingOrders = newOrders;
        notifyDataSetChanged();
    }

    private String getStatusText(String deliveryStatus) {
        if (deliveryStatus == null) return "Processing";
        
        switch (deliveryStatus.toLowerCase()) {
            case "pending":
            case "pen":
                return "Processing";
            case "preparing":
                return "Preparing";
            case "out_for_delivery":
            case "on_the_way":
                return "On the way";
            case "delivered":
                return "Delivered";
            case "cancelled":
            case "cnl":
                return "Cancelled";
            default:
                return "Processing";
        }
    }

    private String formatOrderTime(String orderDate) {
        if (orderDate == null || orderDate.isEmpty()) {
            return "";
        }
        
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
            Date date = inputFormat.parse(orderDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return "";
        }
    }

    private String getRestaurantName(OrderHistoryModel order) {
        // Try to get restaurant name from order details
        // For now, return a default name since restaurant info might not be in the order model
        // In a real implementation, you would extract this from order details
        return "Restaurant";
    }

    private void setProgressStatus(PendingOrderViewHolder holder, String deliveryStatus) {
        if (deliveryStatus == null) {
            // Default to step 1 (Ordered)
            holder.viewStep1.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
            holder.viewStep2.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.viewStep3.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.viewStep4.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            return;
        }
        
        switch (deliveryStatus.toLowerCase()) {
            case "pending":
            case "pen":
                // Step 1: Ordered
                holder.viewStep1.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
                holder.viewStep2.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                holder.viewStep3.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                holder.viewStep4.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                break;
            case "preparing":
                // Step 2: Preparing
                holder.viewStep1.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
                holder.viewStep2.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
                holder.viewStep3.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                holder.viewStep4.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                break;
            case "out_for_delivery":
            case "on_the_way":
                // Step 3: On the way
                holder.viewStep1.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
                holder.viewStep2.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
                holder.viewStep3.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
                holder.viewStep4.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                break;
            case "delivered":
                // Step 4: Delivered
                holder.viewStep1.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
                holder.viewStep2.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
                holder.viewStep3.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
                holder.viewStep4.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
                break;
            default:
                // Default to step 1
                holder.viewStep1.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));
                holder.viewStep2.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                holder.viewStep3.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                holder.viewStep4.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
                break;
        }
    }

    static class PendingOrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderStatus, tvOrderTime, tvRestaurantName, tvOrderItems;
        View viewStep1, viewStep2, viewStep3, viewStep4;
        Button btnTrackOrder, btnViewDetails;

        public PendingOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvOrderTime = itemView.findViewById(R.id.tv_order_time);
            tvRestaurantName = itemView.findViewById(R.id.tv_restaurant_name);
            tvOrderItems = itemView.findViewById(R.id.tv_order_items);
            viewStep1 = itemView.findViewById(R.id.view_step1);
            viewStep2 = itemView.findViewById(R.id.view_step2);
            viewStep3 = itemView.findViewById(R.id.view_step3);
            viewStep4 = itemView.findViewById(R.id.view_step4);
            btnTrackOrder = itemView.findViewById(R.id.btn_track_order);
            btnViewDetails = itemView.findViewById(R.id.btn_view_details);
        }
    }
}
