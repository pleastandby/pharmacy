package com.pharmacy.models;

public class SaleItem {
    private int id;
    private int saleId;
    private int medicineId;
    private int quantity;
    private double price;

    public SaleItem(int saleId, int medicineId, int quantity, double price) {
        this.saleId = saleId;
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.price = price;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSaleId() {
        return this.saleId;
    }

    public int getMedicineId() {
        return this.medicineId;
    }

    public double getPrice() {
        return this.price;
    }

    public void setQuantity(int quantity) {
        if (quantity > 0) {
            this.quantity = quantity;
        } else {
            System.err.println("Item Cannot be zero!");
        }
    }

    public int getQuantity() {
        return this.quantity;
    }

    public double getSubTotal() {
        double subtotal = this.price * this.quantity;
        return subtotal;
    }
}
