package com.pharmacy.service;

import com.pharmacy.models.Medicine;

import java.sql.SQLException;
import java.util.List;
import java.sql.Connection;

import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.database.DatabaseConnection;

public class MedicineService {

    private MedicineDAO medicineDAO;

    public MedicineService() {
        medicineDAO = new MedicineDAO();
    }

    public double calculateItemSubtotal(int medicineId, int quantity) {

        if (medicineId <= 0) {
            System.out.println("Invalid medicine ID.");
            return 0;
        }

        Medicine medicine = medicineDAO.getMedicineById(medicineId);

        if (medicine == null) {
            System.out.println("Invalid medicine");
            return 0;
        }

        double subTotal = 0;

        if (quantity <= 0) {
            System.out.println("Item Invalid");
            return 0;
        } else if (quantity > medicine.getQuantity()) {
            System.err.println("Insufficient Stock");
            return 0;
        }

        subTotal = medicine.getPrice() * quantity;

        return subTotal;
    }

    public void addMedicine(Medicine medicine) {

        if (medicine == null) {
            System.out.println("Medicine cannot be null.");
            return;
        }

        if (medicine.getName() == null ||
                medicine.getName().isBlank()) {
            System.out.println("Medicine name is required.");
            return;
        }

        if (medicine.getExpiryDate() == null) {
            System.out.println("Expiry date is required.");
            return;
        }

        if (medicine.getQuantity() < 0) {
            System.out.println("Quantity cannot be negative.");
            return;
        }

        if (medicine.getPrice() <= 0) {
            System.out.println("Price must be greater than zero.");
            return;
        }

        medicineDAO.addMedicine(medicine);
    }

    public void updateMedicine(int id, Medicine medicine) {

        if (id <= 0) {
            System.out.println("Invalid medicine ID.");
            return;
        }

        if (medicine == null) {
            System.out.println("Medicine cannot be null.");
            return;
        }

        if (medicine.getName() == null ||
                medicine.getName().isBlank()) {
            System.out.println("Medicine name is required.");
            return;
        }

        if (medicine.getExpiryDate() == null) {
            System.out.println("Expiry date is required.");
            return;
        }

        if (medicine.getQuantity() < 0) {
            System.out.println("Quantity cannot be negative.");
            return;
        }

        if (medicine.getPrice() <= 0) {
            System.out.println("Price must be greater than zero.");
            return;
        }

        medicineDAO.updateMedicine(id, medicine);
    }

    public Medicine getMedicineById(int id) {
        if (id <= 0) {
            System.out.println("Invalid medicine ID.");
            return null;
        }

        return medicineDAO.getMedicineById(id);
    }

    public List<Medicine> getAllMedicines() {
        return medicineDAO.getAllMedicines();
    }

    public boolean restockMedicine(int medicineId, int quantity) {

        if (medicineId <= 0 || quantity <= 0) {
            System.out.println("Invalid medicine ID or quantity.");
            return false;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                return false;
            }

            conn.setAutoCommit(false);

            try {
                Medicine medicine = medicineDAO.getMedicineById(medicineId, conn);

                if (medicine == null) {
                    conn.rollback();
                    return false;
                }

                medicine.increaseStock(quantity);

                medicineDAO.updateStock(
                        medicineId,
                        medicine.getQuantity(),
                        conn);

                conn.commit();
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Restock failed: " + e.getMessage());
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    public boolean isMedicineExpired(int medicineId) {
        Medicine medicine = getMedicineById(medicineId);

        if (medicine == null) {
            return false;
        }

        return medicine.isExpired();
    }

    public List<Medicine> getExpiredMedicines() {

        List<Medicine> expiredMedicines = new java.util.ArrayList<>();

        for (Medicine medicine : getAllMedicines()) {
            if (medicine.isExpired()) {
                expiredMedicines.add(medicine);
            }
        }

        return expiredMedicines;
    }

    public List<Medicine> getMedicinesNearingExpiry(int days) {

        if (days < 0) {
            throw new IllegalArgumentException(
                    "Days cannot be negative.");
        }

        List<Medicine> nearingExpiry = new java.util.ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate cutoffDate = today.plusDays(days);

        for (Medicine medicine : getAllMedicines()) {
            if (medicine.getExpiryDate() != null &&
                    !medicine.getExpiryDate().isBefore(today) &&
                    !medicine.getExpiryDate().isAfter(cutoffDate)) {
                nearingExpiry.add(medicine);
            }
        }

        return nearingExpiry;
    }

}
