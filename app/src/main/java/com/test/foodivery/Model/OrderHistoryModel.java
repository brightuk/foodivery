package com.test.foodivery.Model;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryModel {
	private final String orderId;
	private final String orderNo;
	private final String totalAmount;
	private final String paymentStatus;
	private final String deliveryStatus;
	private final String deliveryDate;
	private final String deliveryTime;
	private final String deliveryTimeEnd;
	private final String deliveryLocation;
	private final String statusCode;
	private String orderStatus;
	private String paymentMethod;
	private String deliveryMethod;
	private String orderedDate;
	private String senderInfo;
	private String receiverInfo;
	private String orderNotes;
	private String grossAmount;
	private String gstAmount;
	private String deliveryAmount;
	private String discountAmount;
	private String netAmount;
	private String advanceAmount;
	private String cashReceivedAmount;
	private String onlineAmount;
	private String invoiceNo;
	private String invoiceDate;
	private String invoiceUrl;
	private final List<OrderProductModel> products;
	private boolean expanded;

	public OrderHistoryModel(
			String orderId,
			String orderNo,
			String totalAmount,
			String paymentStatus,
			String deliveryStatus,
			String deliveryDate,
			String deliveryTime,
			String deliveryTimeEnd,
			String deliveryLocation,
			String statusCode
	) {
		this.orderId = orderId;
		this.orderNo = orderNo;
		this.totalAmount = totalAmount;
		this.paymentStatus = paymentStatus;
		this.deliveryStatus = deliveryStatus;
		this.deliveryDate = deliveryDate;
		this.deliveryTime = deliveryTime;
		this.deliveryTimeEnd = deliveryTimeEnd;
		this.deliveryLocation = deliveryLocation;
		this.statusCode = statusCode;
		this.products = new ArrayList<>();
		this.expanded = false;
	}

	public String getOrderId() { return orderId; }
	public String getOrderNo() { return orderNo; }
	public String getTotalAmount() { return totalAmount; }
	public String getPaymentStatus() { return paymentStatus; }
	public String getDeliveryStatus() { return deliveryStatus; }
	public String getDeliveryDate() { return deliveryDate; }
	public String getDeliveryTime() { return deliveryTime; }
	public String getDeliveryTimeEnd() { return deliveryTimeEnd; }
	public String getDeliveryLocation() { return deliveryLocation; }
	public String getStatusCode() { return statusCode; }
	public List<OrderProductModel> getProducts() { return products; }

	public String getOrderStatus() { return orderStatus; }
	public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

	public String getPaymentMethod() { return paymentMethod; }


	public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
	public void setPaymentStatus(String paymentStatus) { this.paymentMethod = paymentStatus; }

	public String getDeliveryMethod() { return deliveryMethod; }
	public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }

	public String getOrderedDate() { return orderedDate; }
	public void setOrderedDate(String orderedDate) { this.orderedDate = orderedDate; }

	public String getSenderInfo() { return senderInfo; }
	public void setSenderInfo(String senderInfo) { this.senderInfo = senderInfo; }

	public String getReceiverInfo() { return receiverInfo; }
	public void setReceiverInfo(String receiverInfo) { this.receiverInfo = receiverInfo; }

	public String getOrderNotes() { return orderNotes; }
	public void setOrderNotes(String orderNotes) { this.orderNotes = orderNotes; }

	public String getGrossAmount() { return grossAmount; }
	public void setGrossAmount(String grossAmount) { this.grossAmount = grossAmount; }

	public String getGstAmount() { return gstAmount; }
	public void setGstAmount(String gstAmount) { this.gstAmount = gstAmount; }

	public String getDeliveryAmount() { return deliveryAmount; }
	public void setDeliveryAmount(String deliveryAmount) { this.deliveryAmount = deliveryAmount; }

	public String getDiscountAmount() { return discountAmount; }
	public void setDiscountAmount(String discountAmount) { this.discountAmount = discountAmount; }

	public String getNetAmount() { return netAmount; }
	public void setNetAmount(String netAmount) { this.netAmount = netAmount; }

	public String getAdvanceAmount() { return advanceAmount; }
	public void setAdvanceAmount(String advanceAmount) { this.advanceAmount = advanceAmount; }

	public String getCashReceivedAmount() { return cashReceivedAmount; }
	public void setCashReceivedAmount(String cashReceivedAmount) { this.cashReceivedAmount = cashReceivedAmount; }

	public String getOnlineAmount() { return onlineAmount; }
	public void setOnlineAmount(String onlineAmount) { this.onlineAmount = onlineAmount; }

	public String getInvoiceNo() { return invoiceNo; }
	public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }

	public String getInvoiceDate() { return invoiceDate; }
	public void setInvoiceDate(String invoiceDate) { this.invoiceDate = invoiceDate; }

	public String getInvoiceUrl() { return invoiceUrl; }
	public void setInvoiceUrl(String invoiceUrl) { this.invoiceUrl = invoiceUrl; }

	public void addProduct(OrderProductModel product) { this.products.add(product); }

	public boolean isExpanded() { return expanded; }
	public void setExpanded(boolean expanded) { this.expanded = expanded; }
}


