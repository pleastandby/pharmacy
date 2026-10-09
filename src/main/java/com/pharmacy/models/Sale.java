package com.pharmacy.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sale {
    private int id;
    private int customerId;
    private LocalDateTime saleDate;
    private List<SaleItem> items;
    private double totalAmount;

    public Sale(int customerId) {
        this.customerId = customerId;
        this.items = new ArrayList<>();
    }

    public Sale(int id, int customerId, LocalDateTime saleDate, double totalAmount) {
        this.id = id;
        this.customerId = customerId;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.items = new ArrayList<>();
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

    public List<SaleItem> getItems() {
        return this.items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getTotalAmount() {
        if (items.isEmpty()) {
            return totalAmount;
        }

        double total = 0;
        for (SaleItem saleItem : items) {
            total += saleItem.getSubTotal();
        }

        return total;
    }

    public LocalDateTime getSaleDate() {
        return this.saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    @Override
    public String toString() {
        return "Sale{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", saleDate=" + saleDate +
                ", totalAmount=" + getTotalAmount() +
                ", itemsCount=" + (items != null ? items.size() : 0) +
                '}';
    }
}
