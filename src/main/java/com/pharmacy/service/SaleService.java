package com.pharmacy.service;

import java.sql.Connection;
import java.sql.SQLException;

import com.pharmacy.database.DatabaseConnection;

import com.pharmacy.models.Sale;
import com.pharmacy.models.SaleItem;
import com.pharmacy.models.Medicine;

import com.pharmacy.dao.SaleDAO;
import com.pharmacy.dao.SaleItemDAO;
import com.pharmacy.dao.MedicineDAO;

public class SaleService {

    private SaleDAO saleDAO;
    private SaleItemDAO saleItemDAO;
    private MedicineDAO medicineDAO;

    public SaleService() {
        saleDAO = new SaleDAO();
        saleItemDAO = new SaleItemDAO();
        medicineDAO = new MedicineDAO();
    }

    public void addItemToSale(Sale sale, SaleItem item) {
        if (sale == null || item == null) {
            System.out.println("Cannot add item to sale!");
            return;
        }
        sale.addItem(item);
    }

    public SaleItem createSaleItem(Sale sale, Medicine medicine, int quantity) {

        if (sale == null || medicine == null || quantity <= 0 || quantity > medicine.getQuantity()) {
            System.out.println("Item cannot be created!");
            return null;
        }

        SaleItem item = new SaleItem(sale.getId(), medicine.getId(), quantity, medicine.getPrice());

        return item;
    }

    public boolean sellMedicine(Medicine medicine, int quantity) {
        if (medicine == null || quantity <= 0 || quantity > medicine.getQuantity()) {
            return false;
        }
        medicine.decreaseStock(quantity);
        return true;
    }

    public boolean processSale(Sale sale) {

        if (sale == null) {
            System.out.println("Sale cannot be null!");
            return false;
        }

        if (sale.getItems().isEmpty()) {
            System.out.println("Sale must contain at least one item!");
            return false;
        }

        for (SaleItem item : sale.getItems()) {

            Medicine medicine = medicineDAO.getMedicineById(item.getMedicineId());

            if (medicine == null || item.getQuantity() > medicine.getQuantity()) {
                System.out.println("Insufficient stock for medicine ID: " + item.getMedicineId());
                return false;
            }
        }

        Connection conn = DatabaseConnection.getConnection();

        try {
            conn.setAutoCommit(false);

            double total = sale.getTotalAmount();
            saleDAO.addSale(sale, conn);

            System.out.println("Sale ID: " + sale.getId());

            for (SaleItem item : sale.getItems()) {
                item.setSaleId(sale.getId());
                saleItemDAO.addSaleItem(item, conn);

                Medicine medicine = medicineDAO.getMedicineById(item.getMedicineId(), conn);
                if (!sellMedicine(medicine, item.getQuantity())) {
                    throw new SQLException("Failed to reduce medicine stock.");
                }

                medicineDAO.updateStock(medicine.getId(), medicine.getQuantity(), conn);
            }
            System.out.println("Sale Total: " + total);
            conn.commit();

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                System.out.println("Error: " + rollbackException.getMessage());
            }
            System.out.println("Error: " + e.getMessage());
            return false;
        } finally {
            try {
                conn.close();
            } catch (SQLException closeException) {
                System.out.println("Error: " + closeException.getMessage());
            }
        }

        return true;
    }

}
