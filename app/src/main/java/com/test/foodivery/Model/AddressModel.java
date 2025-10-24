package com.test.foodivery.Model;

public class AddressModel {
    private String id;
    private String cust_id;
    private String cust_name;
    private String cust_mobileno;
    private String cust_address;
    private String cust_area;
    private String cust_city;

    private String cust_address_primary;
    private String cust_status;
    private String apifetch_address;


    public AddressModel(String id, String cust_id, String cust_name, String cust_mobileno, String cust_address,String apifetch_address, String cust_area, String cust_city, String cust_address_primary, String cust_status) {
        this.id = id;
        this.cust_id = cust_id;
        this.cust_name = cust_name;
        this.cust_mobileno = cust_mobileno;
        this.cust_address = cust_address;
        this.cust_area = cust_area;
        this.cust_city = cust_city;
        this.cust_address_primary = cust_address_primary;
        this.cust_status = cust_status;
        this.apifetch_address=apifetch_address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCust_id() {
        return cust_id;
    }

    public void setCust_id(String cust_id) {
        this.cust_id = cust_id;
    }

    public String getCust_name() {
        return cust_name;
    }

    public void setCust_name(String cust_name) {
        this.cust_name = cust_name;
    }

    public String getCust_mobileno() {
        return cust_mobileno;
    }

    public void setCust_mobileno(String cust_mobileno) {
        this.cust_mobileno = cust_mobileno;
    }

    public String getCust_address() {
        return cust_address;
    }

    public void setCust_address(String cust_address) {
        this.cust_address = cust_address;
    }

    public String getCust_area() {
        return cust_area;
    }

    public void setCust_area(String cust_area) {
        this.cust_area = cust_area;
    }

    public String getCust_city() {
        return cust_city;
    }

    public void setCust_city(String cust_city) {
        this.cust_city = cust_city;
    }

    public String getCust_address_primary() {
        return cust_address_primary;
    }

    public void setCust_address_primary(String cust_address_primary) {
        this.cust_address_primary = cust_address_primary;
    }

    public String getCust_status() {
        return cust_status;
    }

    public void setCust_status(String cust_status) {
        this.cust_status = cust_status;
    }

    public String getApifetch_address() {
        return apifetch_address;
    }

    public void setApifetch_address(String apifetch_address) {
        this.apifetch_address = apifetch_address;
    }
}
