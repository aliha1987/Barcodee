package com.holiday.barcodefinder.app.model;

import java.io.Serializable;

/**
 * Created by hashemi on 8/1/2018.
 */
public class ItemTO implements Serializable {

    int state;
    String name;
    int price;
    String discount;
    int netPrice;
    String barcode;

    public ItemTO(){

    }
    public ItemTO(int state, String name, int price, String discount, int netPrice, String barcode){
        this.state = state;
        this.name = name;
        this.discount = discount;
        this.netPrice = netPrice;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public int getNetPrice() {
        return netPrice;
    }

    public void setNetPrice(int netPrice) {
        this.netPrice = netPrice;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
