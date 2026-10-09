package com.pharmacy.service;

import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.database.DatabaseConnection;
import com.pharmacy.models.Medicine;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MedicineService {

    private final MedicineDAO medicineDAO;

    public MedicineService() {
        this.medicineDAO = new MedicineDAO();
    }

    public MedicineService(MedicineDAO medicineDAO) {
        this.medicineDAO = medicineDAO != null ? medicineDAO : new MedicineDAO();
    }

    public boolean addMedicine(Medicine medicine) {
        if (!validateMedicine(medicine)) {
            return false;
        }

        boolean success = medicineDAO.addMedicine(medicine);
        if (success) {
            System.out.println("Medicine '" + medicine.getName() + "' added successfully (ID: " + medicine.getId() + ").");
        } else {
            System.err.println("Failed to add medicine '" + medicine.getName() + "'.");
        }
        return success;
    }

    public boolean updateMedicine(Medicine medicine) {
        if (medicine == null) {
            System.err.println("Medicine cannot be null.");
            return false;
        }
        return updateMedicine(medicine.getId(), medicine);
    }

    public boolean updateMedicine(int id, Medicine medicine) {
        if (id <= 0) {
            System.err.println("Invalid medicine ID: " + id);
            return false;
        }

        if (!validateMedicine(medicine)) {
            return false;
        }

        Medicine existing = medicineDAO.getMedicineById(id);
        if (existing == null) {
            System.err.println("Cannot update. Medicine not found with ID: " + id);
            return false;
        }

        boolean success = medicineDAO.updateMedicine(id, medicine);
        if (success) {
            System.out.println("Medicine ID " + id + " updated successfully.");
        } else {
            System.err.println("Failed to update medicine ID " + id + ".");
        }
        return success;
    }

    public Medicine getMedicineById(int id) {
        if (id <= 0) {
            System.err.println("Invalid medicine ID: " + id);
            return null;
        }

        return medicineDAO.getMedicineById(id);
    }

    public List<Medicine> getAllMedicines() {
        return medicineDAO.getAllMedicines();
    }

    public boolean deleteMedicine(int id) {
        return deleteMedicineById(id);
    }

    public boolean deleteMedicineById(int id) {
        if (id <= 0) {
            System.err.println("Invalid medicine ID: " + id);
            return false;
        }

        Medicine existing = medicineDAO.getMedicineById(id);
        if (existing == null) {
            System.err.println("Cannot delete. Medicine not found with ID: " + id);
            return false;
        }

        boolean success = medicineDAO.deleteMedicineById(id);
        if (success) {
            System.out.println("Medicine '" + existing.getName() + "' (ID: " + id + ") deleted successfully.");
        } else {
            System.err.println("Failed to delete medicine ID " + id + ".");
        }
        return success;
    }

    public List<Medicine> searchMedicinesByName(String name) {
        if (name == null || name.isBlank()) {
            System.err.println("Search term cannot be empty.");
            return new ArrayList<>();
        }
        return medicineDAO.searchMedicinesByName(name.trim());
    }

    public List<Medicine> getMedicinesByManufacturer(String manufacturer) {
        if (manufacturer == null || manufacturer.isBlank()) {
            System.err.println("Manufacturer name cannot be empty.");
            return new ArrayList<>();
        }
        return medicineDAO.getMedicinesByManufacturer(manufacturer.trim());
    }

    public double calculateItemSubtotal(int medicineId, int quantity) {
        if (medicineId <= 0) {
            System.err.println("Invalid medicine ID: " + medicineId);
            return 0.0;
        }

        if (quantity <= 0) {
            System.err.println("Quantity must be greater than zero.");
            return 0.0;
        }

        Medicine medicine = medicineDAO.getMedicineById(medicineId);
        if (medicine == null) {
            System.err.println("Medicine not found with ID: " + medicineId);
            return 0.0;
        }

        if (medicine.isExpired()) {
            System.err.println("Warning: Medicine '" + medicine.getName() + "' is expired!");
            return 0.0;
        }

        if (quantity > medicine.getQuantity()) {
            System.err.println("Insufficient stock for '" + medicine.getName() + "'. Available: " + medicine.getQuantity() + ", Requested: " + quantity);
            return 0.0;
        }

        return medicine.getPrice() * quantity;
    }

    public boolean isStockAvailable(int medicineId, int requiredQuantity) {
        if (medicineId <= 0 || requiredQuantity <= 0) {
            return false;
        }

        Medicine medicine = medicineDAO.getMedicineById(medicineId);
        if (medicine == null || medicine.isExpired()) {
            return false;
        }

        return medicine.getQuantity() >= requiredQuantity;
    }

    public boolean restockMedicine(int medicineId, int quantity) {
        if (medicineId <= 0 || quantity <= 0) {
            System.err.println("Invalid medicine ID or restock quantity.");
            return false;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Database connection failed during restock.");
                return false;
            }

            conn.setAutoCommit(false);

            try {
                Medicine medicine = medicineDAO.getMedicineById(medicineId, conn);
                if (medicine == null) {
                    System.err.println("Medicine not found with ID: " + medicineId);
                    conn.rollback();
                    return false;
                }

                medicine.increaseStock(quantity);
                medicineDAO.updateStock(medicineId, medicine.getQuantity(), conn);

                conn.commit();
                System.out.println("Successfully restocked " + quantity + " units of " + medicine.getName() + ". New stock: " + medicine.getQuantity());
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Restock failed: " + e.getMessage());
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Database error during restock: " + e.getMessage());
            return false;
        }
    }

    public boolean reduceStock(int medicineId, int quantity) {
        if (medicineId <= 0 || quantity <= 0) {
            System.err.println("Invalid medicine ID or quantity.");
            return false;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Database connection failed during stock reduction.");
                return false;
            }

            conn.setAutoCommit(false);

            try {
                Medicine medicine = medicineDAO.getMedicineById(medicineId, conn);
                if (medicine == null) {
                    System.err.println("Medicine not found with ID: " + medicineId);
                    conn.rollback();
                    return false;
                }

                if (medicine.getQuantity() < quantity) {
                    System.err.println("Insufficient stock. Available: " + medicine.getQuantity() + ", Requested: " + quantity);
                    conn.rollback();
                    return false;
                }

                medicine.decreaseStock(quantity);
                medicineDAO.updateStock(medicineId, medicine.getQuantity(), conn);

                conn.commit();
                System.out.println("Successfully reduced " + quantity + " units of " + medicine.getName() + ". Remaining stock: " + medicine.getQuantity());
                return true;

            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Stock reduction failed: " + e.getMessage());
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Database error during stock reduction: " + e.getMessage());
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
        return medicineDAO.getExpiredMedicines();
    }

    public List<Medicine> getMedicinesNearingExpiry(int days) {
        if (days < 0) {
            System.err.println("Days cannot be negative.");
            return new ArrayList<>();
        }
        return medicineDAO.getMedicinesNearingExpiry(days);
    }

    public List<Medicine> getLowStockMedicines(int threshold) {
        if (threshold < 0) {
            System.err.println("Threshold cannot be negative.");
            return new ArrayList<>();
        }
        return medicineDAO.getLowStockMedicines(threshold);
    }

    private boolean validateMedicine(Medicine medicine) {
        if (medicine == null) {
            System.err.println("Medicine cannot be null.");
            return false;
        }

        if (medicine.getName() == null || medicine.getName().isBlank()) {
            System.err.println("Medicine name is required.");
            return false;
        }

        if (medicine.getExpiryDate() == null) {
            System.err.println("Expiry date is required.");
            return false;
        }

        if (medicine.getManufacturingDate() != null && medicine.getManufacturingDate().isAfter(medicine.getExpiryDate())) {
            System.err.println("Manufacturing date cannot be after expiry date.");
            return false;
        }

        if (medicine.getQuantity() < 0) {
            System.err.println("Quantity cannot be negative.");
            return false;
        }

        if (medicine.getPrice() <= 0) {
            System.err.println("Price must be greater than zero.");
            return false;
        }

        return true;
    }
}
