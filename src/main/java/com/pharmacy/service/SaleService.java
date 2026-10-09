package com.pharmacy.service;

import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.dao.SaleDAO;
import com.pharmacy.dao.SaleItemDAO;
import com.pharmacy.database.DatabaseConnection;
import com.pharmacy.models.Medicine;
import com.pharmacy.models.Sale;
import com.pharmacy.models.SaleItem;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaleService {

    private final SaleDAO saleDAO;
    private final SaleItemDAO saleItemDAO;
    private final MedicineDAO medicineDAO;

    public SaleService() {
        this.saleDAO = new SaleDAO();
        this.saleItemDAO = new SaleItemDAO();
        this.medicineDAO = new MedicineDAO();
    }

    public SaleService(SaleDAO saleDAO, SaleItemDAO saleItemDAO, MedicineDAO medicineDAO) {
        this.saleDAO = saleDAO != null ? saleDAO : new SaleDAO();
        this.saleItemDAO = saleItemDAO != null ? saleItemDAO : new SaleItemDAO();
        this.medicineDAO = medicineDAO != null ? medicineDAO : new MedicineDAO();
    }

    public boolean addItemToSale(Sale sale, SaleItem item) {
        if (sale == null || item == null) {
            System.err.println("Cannot add null item or item to null sale.");
            return false;
        }
        sale.addItem(item);
        return true;
    }

    public SaleItem createSaleItem(Sale sale, Medicine medicine, int quantity) {
        if (sale == null) {
            System.err.println("Sale cannot be null.");
            return null;
        }
        if (medicine == null) {
            System.err.println("Medicine cannot be null.");
            return null;
        }
        if (quantity <= 0) {
            System.err.println("Quantity must be greater than zero.");
            return null;
        }
        if (medicine.isExpired()) {
            System.err.println("Cannot create sale item: Medicine '" + medicine.getName() + "' is expired!");
            return null;
        }
        if (quantity > medicine.getQuantity()) {
            System.err.println("Insufficient stock for medicine '" + medicine.getName() + "'. Available: "
                    + medicine.getQuantity() + ", Requested: " + quantity);
            return null;
        }

        return new SaleItem(sale.getId(), medicine.getId(), quantity, medicine.getPrice());
    }

    public boolean sellMedicine(Medicine medicine, int quantity) {
        if (medicine == null || quantity <= 0 || quantity > medicine.getQuantity()) {
            return false;
        }
        medicine.decreaseStock(quantity);
        return true;
    }

    public boolean processSale(Sale sale) {
        if (!validateSale(sale)) {
            return false;
        }

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Database connection failed. Cannot process sale.");
            return false;
        }

        try {
            conn.setAutoCommit(false);

            double total = sale.getTotalAmount();
            saleDAO.addSale(sale, conn);

            for (SaleItem item : sale.getItems()) {
                item.setSaleId(sale.getId());
                saleItemDAO.addSaleItem(item, conn);

                Medicine medicine = medicineDAO.getMedicineById(item.getMedicineId(), conn);
                if (medicine == null || !sellMedicine(medicine, item.getQuantity())) {
                    throw new SQLException("Failed to reduce stock for medicine ID: " + item.getMedicineId());
                }

                medicineDAO.updateStock(medicine.getId(), medicine.getQuantity(), conn);
            }

            conn.commit();
            System.out.println("Sale #" + sale.getId() + " processed successfully. Total: " + total);
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                System.err.println("Rollback failed: " + rollbackException.getMessage());
            }
            System.err.println("Transaction failed: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.close();
            } catch (SQLException closeException) {
                System.err.println("Error closing connection: " + closeException.getMessage());
            }
        }
    }

    public boolean validateSale(Sale sale) {
        if (sale == null) {
            System.err.println("Sale cannot be null!");
            return false;
        }

        if (sale.getCustomerId() <= 0) {
            System.err.println("Invalid customer ID: " + sale.getCustomerId());
            return false;
        }

        if (sale.getItems() == null || sale.getItems().isEmpty()) {
            System.err.println("Sale must contain at least one item!");
            return false;
        }

        // Aggregate required quantities per medicine ID to ensure combined item totals don't exceed stock
        Map<Integer, Integer> requiredStock = new HashMap<>();
        for (SaleItem item : sale.getItems()) {
            if (item.getQuantity() <= 0) {
                System.err.println("Invalid quantity (" + item.getQuantity() + ") for medicine ID: " + item.getMedicineId());
                return false;
            }
            if (item.getPrice() < 0) {
                System.err.println("Invalid price (" + item.getPrice() + ") for medicine ID: " + item.getMedicineId());
                return false;
            }
            requiredStock.merge(item.getMedicineId(), item.getQuantity(), Integer::sum);
        }

        for (Map.Entry<Integer, Integer> entry : requiredStock.entrySet()) {
            int medicineId = entry.getKey();
            int totalNeeded = entry.getValue();

            Medicine medicine = medicineDAO.getMedicineById(medicineId);
            if (medicine == null) {
                System.err.println("Medicine not found with ID: " + medicineId);
                return false;
            }

            if (medicine.isExpired()) {
                System.err.println("Cannot process sale: Medicine '" + medicine.getName() + "' (ID: " + medicineId + ") is expired!");
                return false;
            }

            if (totalNeeded > medicine.getQuantity()) {
                System.err.println("Insufficient stock for '" + medicine.getName() + "'. Available: "
                        + medicine.getQuantity() + ", Total requested: " + totalNeeded);
                return false;
            }
        }

        return true;
    }

    public List<Sale> getSalesHistory() throws SQLException {
        return saleDAO.getAllSales();
    }

    public Sale getSaleDetails(int saleId) throws SQLException {
        if (saleId <= 0) {
            return null;
        }

        Sale sale = saleDAO.getSaleById(saleId);
        if (sale == null) {
            return null;
        }

        List<SaleItem> items = getSaleItems(saleId);
        sale.setItems(items);

        return sale;
    }

    public List<SaleItem> getSaleItems(int saleId) {
        if (saleId <= 0) {
            return new ArrayList<>();
        }
        return saleItemDAO.getSaleItemsBySaleId(saleId);
    }

    public Medicine getMedicineForSaleItem(int medicineId) {
        if (medicineId <= 0) {
            return null;
        }
        return medicineDAO.getMedicineById(medicineId);
    }

    public Sale searchSaleById(int saleId) throws SQLException {
        if (saleId <= 0) {
            return null;
        }
        return getSaleDetails(saleId);
    }

    public List<Sale> getSalesByCustomerId(int customerId) {
        if (customerId <= 0) {
            System.err.println("Invalid customer ID: " + customerId);
            return new ArrayList<>();
        }
        return saleDAO.getSalesByCustomerId(customerId);
    }

    public List<Sale> getSalesBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            System.err.println("Dates cannot be null.");
            return new ArrayList<>();
        }
        if (startDate.isAfter(endDate)) {
            System.err.println("Start date cannot be after end date.");
            return new ArrayList<>();
        }
        return saleDAO.getSalesBetweenDates(startDate, endDate);
    }

    public double calculateTotalRevenue() {
        try {
            List<Sale> sales = getSalesHistory();
            double totalRevenue = 0.0;
            for (Sale sale : sales) {
                totalRevenue += sale.getTotalAmount();
            }
            return totalRevenue;
        } catch (SQLException e) {
            System.err.println("Failed to calculate total revenue: " + e.getMessage());
            return 0.0;
        }
    }

    public boolean cancelSale(int saleId) {
        if (saleId <= 0) {
            System.err.println("Invalid sale ID: " + saleId);
            return false;
        }

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            System.err.println("Database connection failed. Cannot cancel sale.");
            return false;
        }

        try {
            conn.setAutoCommit(false);

            Sale sale = saleDAO.getSaleById(saleId, conn);
            if (sale == null) {
                System.err.println("Sale not found with ID: " + saleId);
                conn.rollback();
                return false;
            }

            List<SaleItem> items = saleItemDAO.getSaleItemsBySaleId(saleId, conn);
            for (SaleItem item : items) {
                Medicine medicine = medicineDAO.getMedicineById(item.getMedicineId(), conn);
                if (medicine != null) {
                    medicine.increaseStock(item.getQuantity());
                    medicineDAO.updateStock(medicine.getId(), medicine.getQuantity(), conn);
                }
            }

            saleDAO.deleteSale(saleId, conn);

            conn.commit();
            System.out.println("Sale #" + saleId + " cancelled and medicine stock restored successfully.");
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback error: " + rollbackEx.getMessage());
            }
            System.err.println("Failed to cancel sale: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.close();
            } catch (SQLException closeEx) {
                System.err.println("Error closing connection: " + closeEx.getMessage());
            }
        }
    }
}
