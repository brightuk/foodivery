package com.test.foodivery.Model;

public class CartModel {
    private String productId;
    private String productName;
    private String price;
    private String selectedOptions;
    private String productImage;
    private String productGst;
    private String gstInc;
    private String shopId;
    private String getallVariantIds;
    private String getproduct_Weight;
    private String message;
    private int quantity;


    public CartModel(String productId, String productName, String price,String message, String selectedOptions, String productImage, String productGst, String gstInc, String shopId, String getallVariantIds,String getproduct_Weight, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.message=message;
        this.selectedOptions = selectedOptions;
        this.productImage = productImage;
        this.productGst = productGst;
        this.gstInc = gstInc;
        this.shopId = shopId;
        this.getallVariantIds = getallVariantIds;
        this.getproduct_Weight=getproduct_Weight;
        this.quantity = quantity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getGstInc() {
        return gstInc;
    }

    public void setGstInc(String gstInc) {
        this.gstInc = gstInc;
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

    public String getSelectedOptions() {
        return selectedOptions;
    }

    public String getProductImage() {
        return productImage;
    }

    public String getProductGst() {
        return productGst;
    }

    public void setProductGst(String productGst) {
        this.productGst = productGst;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getGetallVariantIds() {
        return getallVariantIds;
    }

    public void setGetallVariantIds(String getallVariantIds) {
        this.getallVariantIds = getallVariantIds;
    }

    public String getGetproduct_Weight() {
        return getproduct_Weight;
    }

    public void setGetproduct_Weight(String getproduct_Weight) {
        this.getproduct_Weight = getproduct_Weight;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
