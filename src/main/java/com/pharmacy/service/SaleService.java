package com.pharmacy.service;

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

        double total = sale.getTotalAmount();
        saleDAO.addSale(sale);

        System.out.println("Sale ID: " + sale.getId());

        for (SaleItem item : sale.getItems()) {
            item.setSaleId(sale.getId());
            saleItemDAO.addSaleItem(item);

            Medicine medicine = medicineDAO.getMedicineById(item.getMedicineId());
            sellMedicine(medicine, item.getQuantity());
            medicineDAO.updateStock(medicine.getId(), medicine.getQuantity());
        }
        System.out.println("Sale Total: " + total);

        return true;
    }

}
