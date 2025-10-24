package com.test.foodivery.Model;


public class CartItem {
    private String productId;
    private String name;
    private double price;
    private int quantity;
    private int productGst;

    public CartItem(String productId, String name, double price, int quantity,int productGst) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.productGst=productGst;
    }

    public int getProductGst() {
        return productGst;
    }

    public void setProductGst(int productGst) {
        this.productGst = productGst;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}


