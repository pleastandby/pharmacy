package com.pharmacy.service;

import com.pharmacy.models.Medicine;

public class MedicineService {

    public double calculateItemSubtotal(Medicine medicine, int quantity) {

        double subTotal = 0;

        if (quantity <= 0) {
            System.out.println("Item Invalid");
        } else if (quantity > medicine.getQuantity()) {
            System.err.println("Insufficient Stock");
        } else {
            subTotal = medicine.getPrice() * quantity;
        }

        return subTotal;
    }

}
