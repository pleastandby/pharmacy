package com.pharmacy;

import com.pharmacy.models.Medicine;
import com.pharmacy.models.Customer;
import com.pharmacy.models.Sale;
import com.pharmacy.models.SaleItem;
import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.dao.SaleItemDAO;

import java.sql.SQLException;
import java.util.List;

import com.pharmacy.dao.CustomerDAO;
import com.pharmacy.service.SaleService;

public class Main {

    public void saleTest() {

        SaleService saleService = new SaleService();

        MedicineDAO medAccess = new MedicineDAO();
        Medicine medicine1 = medAccess.getMedicineById(1);

        CustomerDAO customerAccess = new CustomerDAO();
        Customer custom = customerAccess.getCustomerById(2);

        Sale sale = new Sale(custom.getId());
        SaleItem saleItem = new SaleItem(
                sale.getId(),
                medicine1.getId(),
                3,
                medicine1.getPrice());

        saleService.addItemToSale(sale, saleItem);
        saleService.processSale(sale);

    }

    public void salesHistoryTest() {
        SaleService saleService = new SaleService();
        MedicineDAO medicineDAO = new MedicineDAO();
        CustomerDAO customerDAO = new CustomerDAO();

        try {
            List<Sale> sales = saleService.getSalesHistory();

            if (sales.isEmpty()) {
                System.out.println("No sales found.");
                return;
            }

            for (Sale sale : sales) {

                Customer customer = customerDAO.getCustomerById(sale.getCustomerId());

                System.out.println("----------------------------");
                System.out.println("Sale ID: " + sale.getId());
                System.out.println("Customer Name : " + customer.getName());
                System.out.println("Sale Date: " + sale.getSaleDate());
                System.out.println("Total Amount: " + sale.getTotalAmount());

                System.out.println("----------------------------");
                System.out.println("       ITEMS PURCHASED      ");
                System.out.println("----------------------------");

                List<SaleItem> items = saleService.getSaleItems(sale.getId());

                // printin the item---testing this mf
                for (SaleItem item : items) {

                    System.out.printf("Total Amount: %.2f%n", sale.getTotalAmount());

                    Medicine medicine = saleService.getMedicineForSaleItem(item.getMedicineId());

                    System.out.println("Medicine : " + medicine.getName());
                    System.out.println("Quantity : " + item.getQuantity());
                    System.out.printf("Unit Price: %.2f%n", item.getPrice());
                    System.out.printf("Sub Total: %.2f%n", item.getSubTotal());
                }
                System.out.println("----------------------------");

            }

        } catch (SQLException e) {
            System.out.println("Failed to retrieve sales: " + e.getMessage());
        }
    }

    public void searchSaleTest() {
        SaleService saleService = new SaleService();

        try {
            Sale sale = saleService.searchSaleById(1);

            if (sale == null) {
                System.out.println("Sale not found.");
                return;
            }

            System.out.println("Sale ID: " + sale.getId());
            System.out.println("Customer ID: " + sale.getCustomerId());
            System.out.println("Total Amount: " + sale.getTotalAmount());
            System.out.println("Items Found: " + sale.getItems().size());

        } catch (SQLException e) {
            System.out.println("Failed to search sale: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Main test = new Main();
        // test.saleTest();
        // test.salesHistoryTest();
        test.searchSaleTest();
    }
}
