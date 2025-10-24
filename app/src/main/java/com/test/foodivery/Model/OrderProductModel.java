package com.test.foodivery.Model;

public class OrderProductModel {
	private final String productId;
	private final String productName;
	private final String price;
	private final String quantity;

	public OrderProductModel(String productId, String productName, String price, String quantity) {
		this.productId = productId;
		this.productName = productName;
		this.price = price;
		this.quantity = quantity;
	}

	public String getProductId() {
		return productId;
	}

	public String getProductName() {
		return productName;
	}

	public String getPrice() {
		return price;
	}

	public String getQuantity() {
		return quantity;
	}
}


