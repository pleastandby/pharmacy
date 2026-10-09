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

    public SaleItem(int id, int saleId, int medicineId, int quantity, double price) {
        this.id = id;
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

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getMedicineId() {
        return this.medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public double getPrice() {
        return this.price;
    }

    public void setPrice(double price) {
        if (price >= 0) {
            this.price = price;
        } else {
            System.err.println("Price cannot be negative!");
        }
    }

    public void setQuantity(int quantity) {
        if (quantity > 0) {
            this.quantity = quantity;
        } else {
            System.err.println("Item quantity must be greater than zero!");
        }
    }

    public int getQuantity() {
        return this.quantity;
    }

    public double getSubTotal() {
        return this.price * this.quantity;
    }

    @Override
    public String toString() {
        return "SaleItem{" +
                "id=" + id +
                ", saleId=" + saleId +
                ", medicineId=" + medicineId +
                ", quantity=" + quantity +
                ", price=" + price +
                ", subTotal=" + getSubTotal() +
                '}';
    }
}
