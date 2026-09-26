package com.pharmacy;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

import com.pharmacy.models.Medicine;
import com.pharmacy.models.Customer;
import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.dao.CustomerDAO;

public class Main {

    public void medicineTest() {
        Medicine medicine = new Medicine("Amoxyllin", "Glaxo", LocalDate.of(2026, 1, 1), LocalDate.of(2028, 1, 1), 100,
                23.4);
        MedicineDAO medicineDAO = new MedicineDAO();
        medicineDAO.addMedicine(medicine);

        Medicine m = medicineDAO.getMedicineById(1);
        System.out.println("First Medicine");
        System.out.println("ID : " + m.getId());
        System.out.println("Name : " + m.getName());
        System.out.println("Manufacturer : " + m.getManufacturer());
        System.out.println("Manufacturing Date : " + m.getManufacturingDate());
        System.out.println("Expiry Date : " + m.getExpiryDate());
        System.out.println("Quantity : " + m.getQuantity());
        System.out.println("Price : " + m.getPrice());

        System.out.println("ALL MEDICINES");
        List<Medicine> medicineList = new ArrayList<>();
        medicineList = medicineDAO.getAllMedicines();

        for (Medicine med : medicineList) {
            System.out.println(med);
        }
    }

    public void customerTest() {
        Customer customer = new Customer("John", "1234567890");
        CustomerDAO customerDAO = new CustomerDAO();
        customerDAO.addCustomer(customer);

        Customer c = customerDAO.getCustomerById(1);
        System.out.println("First Customer");
        System.out.println("ID : " + c.getId());
        System.out.println("Name : " + c.getName());
        System.out.println("Phone : " + c.getPhone());

        System.out.println("ALL CUSTOMERS");
        List<Customer> customerList = new ArrayList<>();
        customerList = customerDAO.getAllCustomers();

        for (Customer cust : customerList) {
            System.out.println(cust.getName());
        }
        customerDAO.deleteCustomer(1);
    }

    public static void main(String[] args) {
        Main test = new Main();
        test.medicineTest();
        test.customerTest();
    }
}
