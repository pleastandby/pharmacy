package com.pharmacy;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.List;
import java.util.ArrayList;

import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.database.DatabaseConnection;
import com.pharmacy.models.Medicine;

public class Main {
    public static void main(String[] args) {
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
}
