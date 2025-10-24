package com.test.foodivery.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.test.foodivery.Activity.ProductViewActivity;
import com.test.foodivery.Model.OfferModel;
import com.test.foodivery.R;

import java.util.List;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.ViewHolder> {

    private final Context context;
    private List<OfferModel> orderList;

    public OfferAdapter(Context context, List<OfferModel> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    public void updateList(List<OfferModel> newList) {
        this.orderList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.offer_show, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OfferModel image = orderList.get(position);

        // Set text views
        holder.productTitle.setText(image.getProd_name());
        holder.productPrice.setText("₹" + image.getProd_price());
        holder.shopId.setText(image.getShop_id());

        // Show progress bars and hide images initially
        holder.progressBar.setVisibility(View.VISIBLE);
        holder.imageView.setVisibility(View.INVISIBLE);
        holder.shopLogo.setVisibility(View.INVISIBLE);

        // Load product image with Glide
        Glide.with(context)
                .load(image.getImageUrl())
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.imageView.setVisibility(View.VISIBLE);
                        holder.imageView.setImageResource(R.drawable.foolivery_appicon);
                        return true; // Glide will not handle the error, we did
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.imageView.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .placeholder(R.drawable.foolivery_appicon)
                .error(R.drawable.foolivery_appicon)
                .into(holder.imageView);

        // Load shop logo with Glide
        Glide.with(context)
                .load(image.getFulllogo())
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        holder.shopLogo.setVisibility(View.VISIBLE);
                        holder.shopLogo.setImageResource(R.drawable.foolivery_appicon);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        holder.shopLogo.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .placeholder(R.drawable.foolivery_appicon)
                .error(R.drawable.foolivery_appicon)
                .into(holder.shopLogo);

        // Handle item click
        holder.layout.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductViewActivity.class);
            intent.putExtra("image_url", image.getImageUrl());
            intent.putExtra("productTitle", image.getProd_name());
            intent.putExtra("productPrice", image.getProd_price());
            intent.putExtra("id", image.getProd_id());
            intent.putExtra("gst", image.getProd_gst_rate());
            intent.putExtra("gstInc", image.getProd_gst_inc());
            intent.putExtra("shopid", image.getShop_id());
            intent.putExtra("shopLogo", image.getFulllogo());
            intent.putExtra("bizIds", image.getBiz_id());
            context.startActivity(intent);
        });

        // Center item inside parent if parent is LinearLayout
        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        if (params instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) params).gravity = Gravity.CENTER;
            holder.itemView.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView shopLogo;
        TextView productTitle, productPrice, shopId;
        ProgressBar progressBar;
        LinearLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            productTitle = itemView.findViewById(R.id.productTitle);
            productPrice = itemView.findViewById(R.id.productCost);
            shopId = itemView.findViewById(R.id.shopId);
            shopLogo = itemView.findViewById(R.id.shopLogo);
            progressBar = itemView.findViewById(R.id.progressBar);
            layout=itemView.findViewById(R.id.layout);
        }
    }
}
