package com.test.foodivery.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.test.foodivery.Model.OrderHistoryModel;
import com.test.foodivery.R;

import java.util.List;
import java.util.Objects;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.Holder> {

	private List<OrderHistoryModel> orders;

	public OrderHistoryAdapter(List<OrderHistoryModel> orders) {
		this.orders = orders;
	}

	public void updateData(List<OrderHistoryModel> newOrders) {
		this.orders = newOrders;
		notifyDataSetChanged();
	}

	@NonNull
	@Override
	public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_layout, parent, false);
		return new Holder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull Holder holder, int position) {
		OrderHistoryModel item = orders.get(position);
		Log.d("gggghhhffff",item.getPaymentStatus());

        int productCount = item.getProducts() != null ? item.getProducts().size() : 0;
        holder.tvOrderId.setText("Order #" + item.getOrderNo() + (productCount > 0 ? ("  (" + productCount + " items)") : ""));
		holder.tvTotal.setText("\u20B9 " + item.getTotalAmount());
		if ("online".equals(item.getPaymentMethod())) {
			holder.tvPaymentStatus.setText(
					(Objects.equals(item.getPaymentStatus(), "PPE")
							|| Objects.equals(item.getPaymentStatus(), "PPF"))
							? "Payment Pending"
							: "Paid"
			);
		} else if ("cod".equals(item.getPaymentMethod())) {
			// Show "Cash Pending" only when payment status is "CPE"
			if (Objects.equals(item.getPaymentStatus(), "CPE")||Objects.equals(item.getPaymentStatus(), "Cash Pending")) {
				holder.tvPaymentStatus.setText("Cash Pending");
			} else {
				// For all other statuses, show "Paid"
				holder.tvPaymentStatus.setText("Paid");
			}
		}






		holder.tvDeliveryStatus.setText("Delivery: " + item.getDeliveryStatus());

		String dt = (item.getDeliveryDate() != null ? item.getDeliveryDate() : "")
				+ ((item.getDeliveryTime() != null && item.getDeliveryTime().length() > 0) ? ("  " + item.getDeliveryTime()) : "");
		holder.tvDeliveryDatetime.setText("Delivery: " + dt);
		holder.tvLocation.setText("Location: " + (item.getDeliveryLocation() != null ? item.getDeliveryLocation() : "-"));
		holder.tvSenderInfo.setText(item.getSenderInfo() != null ? ("Sender: " + item.getSenderInfo()) : "");
		holder.tvReceiverInfo.setText(item.getReceiverInfo() != null ? ("Receiver: " + item.getReceiverInfo()) : "");
		holder.tvPaymentMethod.setText(item.getPaymentMethod() != null ? ("Payment: " + item.getPaymentMethod()) : "");
		String amount = "Amount: ";
		if (item.getGrossAmount() != null) amount += "Gross " + item.getGrossAmount() + "  ";
		if (item.getGstAmount() != null) amount += "GST " + item.getGstAmount() + "  ";
		if (item.getDeliveryAmount() != null) amount += "Delivery " + item.getDeliveryAmount() + "  ";
		if (item.getDiscountAmount() != null) amount += "Discount -" + item.getDiscountAmount() + "  ";
		if (item.getNetAmount() != null) amount += "Net " + item.getNetAmount();
		holder.tvAmountBreakup.setText(amount);

		// Map PEN and CNL to a status chip text/color if present
		TextView statusChip = holder.itemView.findViewById(R.id.tv_status_chip);
		if (statusChip != null) {
			String code = item.getOrderStatus();
			if (code != null) {
				if (code.equalsIgnoreCase("PEN")) {
					statusChip.setText("Processing");
					statusChip.setTextColor(0xFF0C5460);
					statusChip.setBackgroundResource(R.drawable.pill_status_bg);
				} else if (code.equalsIgnoreCase("CNL")) {
					statusChip.setText("Cancelled");
					statusChip.setTextColor(0xFF721C24);
					statusChip.setBackgroundResource(R.drawable.pill_payment_bg);
				}
			}
		}

		// Nested products
		boolean hasProducts = item.getProducts() != null && !item.getProducts().isEmpty();
		if (hasProducts) {
			holder.rvProducts.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
			holder.rvProducts.setAdapter(new OrderProductAdapter(item.getProducts()));
			holder.rvProducts.setVisibility(item.isExpanded() ? View.VISIBLE : View.GONE);
		} else {
			holder.rvProducts.setVisibility(View.GONE);
		}

		// Invoice download (if URL present)
		if (holder.btnInvoice != null) {
			boolean hasUrl = item.getInvoiceUrl() != null && item.getInvoiceUrl().length() > 4;
			holder.btnInvoice.setVisibility(hasUrl ? View.VISIBLE : View.GONE);
			holder.btnInvoice.setOnClickListener(v -> {
				// Delegate intent launch to Activity via ACTION_VIEW
				android.content.Context ctx = v.getContext();
				android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(item.getInvoiceUrl()));
				ctx.startActivity(intent);
			});
		}

		holder.layoutExpandable.setVisibility(item.isExpanded() ? View.VISIBLE : View.GONE);
		holder.ivExpand.setRotation(item.isExpanded() ? 180f : 0f);

		View.OnClickListener toggle = v -> {
			boolean now = !item.isExpanded();
			item.setExpanded(now);
			notifyItemChanged(holder.getAdapterPosition());
		};
		holder.header.setOnClickListener(toggle);
		holder.ivExpand.setOnClickListener(toggle);
	}

	@Override
	public int getItemCount() {
		return orders.size();
	}

	static class Holder extends RecyclerView.ViewHolder {
		TextView tvOrderId;
		TextView tvTotal;
		TextView tvPaymentStatus;
		TextView tvDeliveryStatus;
		TextView tvDeliveryDatetime;
		TextView tvLocation;
		TextView tvSenderInfo;
		TextView tvReceiverInfo;
		TextView tvPaymentMethod;
		TextView tvAmountBreakup;
		LinearLayout layoutExpandable;
		RecyclerView rvProducts;
		ImageView ivExpand;
		View header;
		TextView btnInvoice;

		Holder(@NonNull View itemView) {
			super(itemView);
			tvOrderId = itemView.findViewById(R.id.tv_order_id);
			tvTotal = itemView.findViewById(R.id.tv_total);
			tvPaymentStatus = itemView.findViewById(R.id.tv_payment_status);
			tvDeliveryStatus = itemView.findViewById(R.id.tv_delivery_status);
			tvDeliveryDatetime = itemView.findViewById(R.id.tv_delivery_datetime);
			tvLocation = itemView.findViewById(R.id.tv_location);
			layoutExpandable = itemView.findViewById(R.id.layout_expandable);
			rvProducts = itemView.findViewById(R.id.rv_products);
			ivExpand = itemView.findViewById(R.id.iv_expand);
			header = itemView.findViewById(R.id.layout_header);
			tvSenderInfo = itemView.findViewById(R.id.tv_sender_info);
			tvReceiverInfo = itemView.findViewById(R.id.tv_receiver_info);
			tvPaymentMethod = itemView.findViewById(R.id.tv_payment_method);
			tvAmountBreakup = itemView.findViewById(R.id.tv_amount_breakup);
			btnInvoice = itemView.findViewById(R.id.btn_invoice);
		}
	}
}


