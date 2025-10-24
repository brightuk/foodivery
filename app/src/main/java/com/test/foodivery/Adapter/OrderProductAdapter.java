package com.test.foodivery.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.test.foodivery.Model.OrderProductModel;
import com.test.foodivery.R;

import java.util.List;

public class OrderProductAdapter extends RecyclerView.Adapter<OrderProductAdapter.ProductViewHolder> {

	private final List<OrderProductModel> productList;

	public OrderProductAdapter(List<OrderProductModel> productList) {
		this.productList = productList;
	}

	@NonNull
	@Override
	public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_view_item, parent, false);
		return new ProductViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
		OrderProductModel item = productList.get(position);
		holder.productName.setText(item.getProductName());
		String priceText = "\u20B9 " + item.getPrice();
		if (item.getQuantity() != null && item.getQuantity().length() > 0) {
			priceText += "  x" + item.getQuantity();
		}
		holder.productPrice.setText(priceText);
	}

	@Override
	public int getItemCount() {
		return productList.size();
	}

	static class ProductViewHolder extends RecyclerView.ViewHolder {
		TextView productName;
		TextView productPrice;

		ProductViewHolder(@NonNull View itemView) {
			super(itemView);
			productName = itemView.findViewById(R.id.cartProductTitle);
			productPrice = itemView.findViewById(R.id.cartProductPrice);
		}
	}
}


