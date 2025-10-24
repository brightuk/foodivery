package com.test.foodivery.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.test.foodivery.Activity.OfferActivity;
import com.test.foodivery.Activity.ProfileActivity;
import com.test.foodivery.Model.BrandModel;
import com.test.foodivery.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.BrandViewHolder> {
    private List<BrandModel> brandList;
    private Context context;
    private ArrayList<String> shopIdList;
    private ArrayList<HashMap<String, String>> shopDetailsList;

    public BrandAdapter(Context context, List<BrandModel> brandList,
                        ArrayList<String> shopIdList,
                        ArrayList<HashMap<String, String>> shopDetailsList) {
        this.context = context;
        this.brandList = brandList;
        this.shopIdList = shopIdList;
        this.shopDetailsList = shopDetailsList;
    }

    @Override
    public BrandViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_brand, parent, false);
        return new BrandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BrandViewHolder holder, int position) {
        BrandModel brand = brandList.get(position);
        Log.d("BrandAdapter", "Image URL: " + shopDetailsList);

        holder.progressBar.setVisibility(View.VISIBLE);
        holder.logo.setVisibility(View.INVISIBLE);

        Glide.with(context)
                .load(brand.getBrandImage())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.logo.setVisibility(View.VISIBLE);
                        holder.logo.setImageResource(R.drawable.foolivery_appicon);
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource,
                                                   boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.logo.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .placeholder(R.drawable.foolivery_appicon)
                .error(R.drawable.foolivery_appicon)
                .into(holder.logo);
        holder.name.setText(brand.getShopName());


        holder.productShow.setOnClickListener(v -> {
            if (shopIdList == null || shopIdList.isEmpty()) {
                Toast.makeText(context, "No shops available", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(context, OfferActivity.class);
            intent.putExtra("name", "all");

            // Pass only the current shop's ID
            ArrayList<String> singleShopList = new ArrayList<>();
            singleShopList.add(brand.getShopId());
            intent.putStringArrayListExtra("SHOP_ID_LIST", singleShopList);

            // Convert shop details to JSON strings
            ArrayList<String> shopDetailsJsonList = new ArrayList<>();
            for (HashMap<String, String> shop : shopDetailsList) {
                if (shop.get("biz_id").equals(brand.getBizId())) {
                    shopDetailsJsonList.add(new JSONObject(shop).toString());
                    break; // Only need the matching shop
                }
            }
            Log.d("hhhhhhjuyy",String.valueOf(shopDetailsJsonList));
            intent.putStringArrayListExtra("SHOP_DETAILS_LIST", shopDetailsJsonList);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return brandList.size();
    }

    public static class BrandViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        LinearLayout productShow;
        ImageView logo;
        ProgressBar progressBar;

        public BrandViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.brandName);
            logo = itemView.findViewById(R.id.imageView);
            progressBar = itemView.findViewById(R.id.progressBar);
            productShow = itemView.findViewById(R.id.productShow);
        }
    }
}