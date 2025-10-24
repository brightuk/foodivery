package com.test.foodivery.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.foodivery.Model.AddressModel;
import com.test.foodivery.R;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

    public interface OnAddressClickListener {
        void onAddressClick(int position);
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    private Context context;
    private List<AddressModel> orderList;
    private OnAddressClickListener listener;

    public AddressAdapter(Context context, List<AddressModel> orderList, OnAddressClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.address_background, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AddressModel image = orderList.get(position);
        holder.cust_name.setText(image.getCust_name());
        holder.cust_address.setText(image.getCust_address());
        
        // Set area and city
        String areaCity = image.getCust_area() + ", " + image.getCust_city();
        holder.area_city.setText(areaCity);
        
        // Set mobile number
        holder.mobile_number.setText("Mobile: " + image.getCust_mobileno());

        // Address click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddressClick(position);
            }
        });

        // Edit button click listener
        holder.edit_button.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(position);
            }
        });

        // Delete button click listener
        holder.delete_button.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView cust_name, cust_address, area_city, mobile_number;
        ImageView edit_button, delete_button;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cust_name = itemView.findViewById(R.id.cust_name);
            cust_address = itemView.findViewById(R.id.addressText);
            area_city = itemView.findViewById(R.id.area_city);
            mobile_number = itemView.findViewById(R.id.mobile_number);
            edit_button = itemView.findViewById(R.id.edit_button);
            delete_button = itemView.findViewById(R.id.delete_button);
        }
    }
}
