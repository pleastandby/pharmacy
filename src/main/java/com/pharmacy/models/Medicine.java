package com.pharmacy.models;

import java.time.LocalDate;

public class Medicine {
    private int id;
    private String name;
    private String manufacturer;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private int quantity;
    private double price;

    public Medicine(String name, String manufacturer, LocalDate manufacturingDate, LocalDate expiryDate, int quantity,
            double price) {
        this.name = name;
        this.manufacturer = manufacturer;
        this.manufacturingDate = manufacturingDate;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
        this.price = price;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public void setManufacturingDate(LocalDate manufacturingDate) {
        this.manufacturingDate = manufacturingDate;
    }

    public LocalDate getManufacturingDate() {
        return this.manufacturingDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity > 0) {
            this.quantity = quantity;
        } else {
            System.err.println("Quantity must be greater than 0!");
        }
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price > 0) {
            this.price = price;
        } else {
            System.err.println("Price must be greater than 0!");
        }
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public void increaseStock(int amount) {
        if (amount > 0) {
            this.quantity += amount;
        } else {
            System.err.println("New quantity must be greater than 0!");
        }
    }

    public void decreaseStock(int amount) {
        if (amount > 0 && amount <= this.quantity) {
            this.quantity -= amount;
        } else {
            System.err.println("Invalid quantity!");
        }
    }

}
