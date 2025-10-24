package com.test.foodivery.Model;

public class OfferModel {
    private String imageUrl;
    public String biz_domain_id;
    public String prod_id;
    public String biz_id;
    public String shop_id;
    public String prod_code;
    public String prod_code_home;
    public String prod_name;
    public String prod_url;
    public String prod_desc;
    public String prod_offer;
    public String prod_weight;
    public String prod_price;
    public String prod_unit_label;
    public String prod_position;
    public String prod_short_desc;
    public String prod_info;
    public String meta_title;
    public String meta_desc;
    public String meta_keyword;
    public String prod_image_thumb;
    public String prod_image;
    public String image_path;
    public String prod_gst_inc;
    public String prod_gst_rate;
    public String prod_min_qty;
    public String prod_max_qty;
    public String prod_interval;
    public String prod_type;
    public String prod_opt_type;
    public String prod_delivery_day;
    public String prod_addon;
    public String prod_active;
    public String prod_featured;
    public String prod_stock_avail;
    public String prod_cat_assigned;
    public String date_added;
    public String date_updated;
    public String access_key;
    public String foldern;
    public String logon;
    public String fulllogo;

    public OfferModel(String imageUrl, String prod_id,String shop_id, String prod_name, String prod_price,String prod_offer,String gst,String prodGstInc,String fullLogo,String folderName,String logoName,String biz_id) {
        this.imageUrl = imageUrl;

        this.prod_id = prod_id;
        this.shop_id = shop_id;
        this.prod_offer = prod_offer;
        this.prod_name = prod_name;
        this.prod_price = prod_price;
        this.prod_gst_rate=gst;
        this.prod_gst_inc=prodGstInc;
        this.foldern=folderName;
        this.logon=logoName;
        this.fulllogo=fullLogo;
        this.biz_id=biz_id;
    }


    public String getFoldern() {
        return foldern;
    }

    public void setFoldern(String foldern) {
        this.foldern = foldern;
    }

    public String getLogon() {
        return logon;
    }

    public void setLogon(String logon) {
        this.logon = logon;
    }

    public String getFulllogo() {
        return fulllogo;
    }

    public void setFulllogo(String fulllogo) {
        this.fulllogo = fulllogo;
    }

    public String getImageUrl() {
        return imageUrl;
    }


    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBiz_domain_id() {
        return biz_domain_id;
    }

    public void setBiz_domain_id(String biz_domain_id) {
        this.biz_domain_id = biz_domain_id;
    }

    public String getProd_id() {
        return prod_id;
    }

    public void setProd_id(String prod_id) {
        this.prod_id = prod_id;
    }



    public String getBiz_id() {
        return biz_id;
    }

    public void setBiz_id(String biz_id) {
        this.biz_id = biz_id;
    }

    public String getShop_id() {
        return shop_id;
    }

    public void setShop_id(String shop_id) {
        this.shop_id = shop_id;
    }

    public String getProd_code() {
        return prod_code;
    }

    public void setProd_code(String prod_code) {
        this.prod_code = prod_code;
    }

    public String getProd_code_home() {
        return prod_code_home;
    }

    public void setProd_code_home(String prod_code_home) {
        this.prod_code_home = prod_code_home;
    }

    public String getProd_name() {
        return prod_name;
    }

    public void setProd_name(String prod_name) {
        this.prod_name = prod_name;
    }

    public String getProd_url() {
        return prod_url;
    }

    public void setProd_url(String prod_url) {
        this.prod_url = prod_url;
    }

    public String getProd_desc() {
        return prod_desc;
    }

    public void setProd_desc(String prod_desc) {
        this.prod_desc = prod_desc;
    }

    public String getProd_offer() {
        return prod_offer;
    }

    public void setProd_offer(String prod_offer) {
        this.prod_offer = prod_offer;
    }

    public String getProd_weight() {
        return prod_weight;
    }

    public void setProd_weight(String prod_weight) {
        this.prod_weight = prod_weight;
    }

    public String getProd_price() {
        return prod_price;
    }

    public void setProd_price(String prod_price) {
        this.prod_price = prod_price;
    }

    public String getProd_unit_label() {
        return prod_unit_label;
    }

    public void setProd_unit_label(String prod_unit_label) {
        this.prod_unit_label = prod_unit_label;
    }

    public String getProd_position() {
        return prod_position;
    }

    public void setProd_position(String prod_position) {
        this.prod_position = prod_position;
    }

    public String getProd_short_desc() {
        return prod_short_desc;
    }

    public void setProd_short_desc(String prod_short_desc) {
        this.prod_short_desc = prod_short_desc;
    }

    public String getProd_info() {
        return prod_info;
    }

    public void setProd_info(String prod_info) {
        this.prod_info = prod_info;
    }

    public String getMeta_title() {
        return meta_title;
    }

    public void setMeta_title(String meta_title) {
        this.meta_title = meta_title;
    }

    public String getMeta_desc() {
        return meta_desc;
    }

    public void setMeta_desc(String meta_desc) {
        this.meta_desc = meta_desc;
    }

    public String getMeta_keyword() {
        return meta_keyword;
    }

    public void setMeta_keyword(String meta_keyword) {
        this.meta_keyword = meta_keyword;
    }

    public String getProd_image_thumb() {
        return prod_image_thumb;
    }

    public void setProd_image_thumb(String prod_image_thumb) {
        this.prod_image_thumb = prod_image_thumb;
    }

    public String getProd_image() {
        return prod_image;
    }

    public void setProd_image(String prod_image) {
        this.prod_image = prod_image;
    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public String getProd_gst_inc() {
        return prod_gst_inc;
    }

    public void setProd_gst_inc(String prod_gst_inc) {
        this.prod_gst_inc = prod_gst_inc;
    }

    public String getProd_gst_rate() {
        return prod_gst_rate;
    }

    public void setProd_gst_rate(String prod_gst_rate) {
        this.prod_gst_rate = prod_gst_rate;
    }

    public String getProd_min_qty() {
        return prod_min_qty;
    }

    public void setProd_min_qty(String prod_min_qty) {
        this.prod_min_qty = prod_min_qty;
    }

    public String getProd_max_qty() {
        return prod_max_qty;
    }

    public void setProd_max_qty(String prod_max_qty) {
        this.prod_max_qty = prod_max_qty;
    }

    public String getProd_interval() {
        return prod_interval;
    }

    public void setProd_interval(String prod_interval) {
        this.prod_interval = prod_interval;
    }

    public String getProd_type() {
        return prod_type;
    }

    public void setProd_type(String prod_type) {
        this.prod_type = prod_type;
    }

    public String getProd_opt_type() {
        return prod_opt_type;
    }

    public void setProd_opt_type(String prod_opt_type) {
        this.prod_opt_type = prod_opt_type;
    }

    public String getProd_delivery_day() {
        return prod_delivery_day;
    }

    public void setProd_delivery_day(String prod_delivery_day) {
        this.prod_delivery_day = prod_delivery_day;
    }

    public String getProd_addon() {
        return prod_addon;
    }

    public void setProd_addon(String prod_addon) {
        this.prod_addon = prod_addon;
    }

    public String getProd_active() {
        return prod_active;
    }

    public void setProd_active(String prod_active) {
        this.prod_active = prod_active;
    }

    public String getProd_featured() {
        return prod_featured;
    }

    public void setProd_featured(String prod_featured) {
        this.prod_featured = prod_featured;
    }

    public String getProd_stock_avail() {
        return prod_stock_avail;
    }

    public void setProd_stock_avail(String prod_stock_avail) {
        this.prod_stock_avail = prod_stock_avail;
    }

    public String getProd_cat_assigned() {
        return prod_cat_assigned;
    }

    public void setProd_cat_assigned(String prod_cat_assigned) {
        this.prod_cat_assigned = prod_cat_assigned;
    }

    public String getDate_added() {
        return date_added;
    }

    public void setDate_added(String date_added) {
        this.date_added = date_added;
    }

    public String getDate_updated() {
        return date_updated;
    }

    public void setDate_updated(String date_updated) {
        this.date_updated = date_updated;
    }

    public String getAccess_key() {
        return access_key;
    }

    public void setAccess_key(String access_key) {
        this.access_key = access_key;
    }
}
