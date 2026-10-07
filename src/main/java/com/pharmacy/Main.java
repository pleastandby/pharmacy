package com.pharmacy;

import com.pharmacy.models.Medicine;
import com.pharmacy.models.Customer;
import com.pharmacy.models.Sale;
import com.pharmacy.models.SaleItem;
import com.pharmacy.dao.MedicineDAO;
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

    public static void main(String[] args) {
        Main test = new Main();
        test.saleTest();
    }
}
