package com.pharmacy.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int id;
    private int customerId;
    private LocalDateTime saleDate;
    private List<SaleItem> items;

    public Sale(int customerId) {
        this.customerId = customerId;
        items = new ArrayList<>();
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return this.customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void addItem(SaleItem item) {
        if (item == null) {
            System.err.println("Item Cannot be Null");
            return;
        }
        items.add(item);
    }

    public double getTotalAmount() {
        double totalAmount = 0;
        for (SaleItem saleItem : items) {
            totalAmount += saleItem.getSubTotal();
        }
        return totalAmount;
    }

    public LocalDateTime getSaleDate() {
        return this.saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }
}
