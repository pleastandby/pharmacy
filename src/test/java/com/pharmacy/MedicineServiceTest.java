package com.pharmacy;

import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.models.Medicine;
import com.pharmacy.service.MedicineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MedicineServiceTest {

    private MedicineService medicineService;
    private MockMedicineDAO mockDAO;

    // Fake in-memory DAO to test MedicineService logic independently of MariaDB
    static class MockMedicineDAO extends MedicineDAO {
        private final Map<Integer, Medicine> storage = new HashMap<>();
        private int idSequence = 1;

        @Override
        public boolean addMedicine(Medicine medicine) {
            int newId = idSequence++;
            medicine.setId(newId);
            storage.put(newId, medicine);
            return true;
        }

        @Override
        public Medicine getMedicineById(int id) {
            return storage.get(id);
        }

        @Override
        public List<Medicine> getAllMedicines() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public boolean updateMedicine(int id, Medicine medicine) {
            if (storage.containsKey(id)) {
                medicine.setId(id);
                storage.put(id, medicine);
                return true;
            }
            return false;
        }

        @Override
        public boolean deleteMedicineById(int id) {
            return storage.remove(id) != null;
        }

        @Override
        public List<Medicine> searchMedicinesByName(String name) {
            List<Medicine> results = new ArrayList<>();
            for (Medicine med : storage.values()) {
                if (med.getName().toLowerCase().contains(name.toLowerCase())) {
                    results.add(med);
                }
            }
            return results;
        }

        @Override
        public List<Medicine> getMedicinesByManufacturer(String manufacturer) {
            List<Medicine> results = new ArrayList<>();
            for (Medicine med : storage.values()) {
                if (med.getManufacturer() != null && med.getManufacturer().toLowerCase().contains(manufacturer.toLowerCase())) {
                    results.add(med);
                }
            }
            return results;
        }

        @Override
        public List<Medicine> getLowStockMedicines(int threshold) {
            List<Medicine> results = new ArrayList<>();
            for (Medicine med : storage.values()) {
                if (med.getQuantity() <= threshold) {
                    results.add(med);
                }
            }
            return results;
        }

        @Override
        public List<Medicine> getExpiredMedicines() {
            List<Medicine> results = new ArrayList<>();
            for (Medicine med : storage.values()) {
                if (med.isExpired()) {
                    results.add(med);
                }
            }
            return results;
        }

        @Override
        public List<Medicine> getMedicinesNearingExpiry(int days) {
            List<Medicine> results = new ArrayList<>();
            LocalDate today = LocalDate.now();
            LocalDate cutoff = today.plusDays(days);
            for (Medicine med : storage.values()) {
                if (med.getExpiryDate() != null &&
                        !med.getExpiryDate().isBefore(today) &&
                        !med.getExpiryDate().isAfter(cutoff)) {
                    results.add(med);
                }
            }
            return results;
        }
    }

    @BeforeEach
    public void setUp() {
        mockDAO = new MockMedicineDAO();
        medicineService = new MedicineService(mockDAO);
    }

    @Test
    public void testAddMedicineValidation() {
        // Null medicine
        assertFalse(medicineService.addMedicine(null));

        // Blank name
        Medicine noName = new Medicine("", "PharmaCorp", LocalDate.now(), LocalDate.now().plusYears(1), 10, 5.0);
        assertFalse(medicineService.addMedicine(noName));

        // Null expiry date
        Medicine noExpiry = new Medicine("Paracetamol", "PharmaCorp", LocalDate.now(), null, 10, 5.0);
        assertFalse(medicineService.addMedicine(noExpiry));

        // Manufacturing date after expiry date
        Medicine invalidDates = new Medicine("Paracetamol", "PharmaCorp", LocalDate.now().plusYears(2), LocalDate.now().plusYears(1), 10, 5.0);
        assertFalse(medicineService.addMedicine(invalidDates));

        // Negative quantity
        Medicine negativeQty = new Medicine("Paracetamol", "PharmaCorp", LocalDate.now(), LocalDate.now().plusYears(1), -5, 5.0);
        assertFalse(medicineService.addMedicine(negativeQty));

        // Zero price
        Medicine zeroPrice = new Medicine("Paracetamol", "PharmaCorp", LocalDate.now(), LocalDate.now().plusYears(1), 10, 0.0);
        assertFalse(medicineService.addMedicine(zeroPrice));

        // Valid medicine
        Medicine validMed = new Medicine("Amoxicillin", "HealthCare", LocalDate.now().minusMonths(1), LocalDate.now().plusYears(1), 50, 12.5);
        assertTrue(medicineService.addMedicine(validMed));
        assertTrue(validMed.getId() > 0);
    }

    @Test
    public void testGetMedicineById() {
        Medicine validMed = new Medicine("Ibuprofen", "HealthCare", LocalDate.now().minusMonths(1), LocalDate.now().plusYears(1), 20, 8.0);
        medicineService.addMedicine(validMed);

        assertNull(medicineService.getMedicineById(-1));
        assertNull(medicineService.getMedicineById(0));
        assertNull(medicineService.getMedicineById(999));

        Medicine retrieved = medicineService.getMedicineById(validMed.getId());
        assertNotNull(retrieved);
        assertEquals("Ibuprofen", retrieved.getName());
    }

    @Test
    public void testUpdateMedicine() {
        Medicine validMed = new Medicine("Cetirizine", "BioMed", LocalDate.now(), LocalDate.now().plusYears(1), 30, 4.5);
        medicineService.addMedicine(validMed);
        int medId = validMed.getId();

        // Update with non-existent id
        Medicine updateMed = new Medicine("Cetirizine", "BioMed", LocalDate.now(), LocalDate.now().plusYears(1), 40, 5.0);
        assertFalse(medicineService.updateMedicine(999, updateMed));

        // Update with valid id
        assertTrue(medicineService.updateMedicine(medId, updateMed));
        assertEquals(40, medicineService.getMedicineById(medId).getQuantity());
        assertEquals(5.0, medicineService.getMedicineById(medId).getPrice());
    }

    @Test
    public void testDeleteMedicine() {
        Medicine validMed = new Medicine("Aspirin", "Bayer", LocalDate.now(), LocalDate.now().plusYears(1), 15, 6.0);
        medicineService.addMedicine(validMed);
        int id = validMed.getId();

        assertFalse(medicineService.deleteMedicine(-1));
        assertFalse(medicineService.deleteMedicine(999));

        assertTrue(medicineService.deleteMedicine(id));
        assertNull(medicineService.getMedicineById(id));
    }

    @Test
    public void testCalculateItemSubtotal() {
        Medicine validMed = new Medicine("Paracetamol", "GSK", LocalDate.now().minusMonths(2), LocalDate.now().plusYears(1), 20, 2.50);
        medicineService.addMedicine(validMed);
        int id = validMed.getId();

        // Invalid medicine ID
        assertEquals(0.0, medicineService.calculateItemSubtotal(-1, 5));

        // Invalid quantity
        assertEquals(0.0, medicineService.calculateItemSubtotal(id, 0));
        assertEquals(0.0, medicineService.calculateItemSubtotal(id, -3));

        // Insufficient stock
        assertEquals(0.0, medicineService.calculateItemSubtotal(id, 25));

        // Valid subtotal: 4 * 2.50 = 10.0
        assertEquals(10.0, medicineService.calculateItemSubtotal(id, 4), 0.001);

        // Expired medicine
        Medicine expiredMed = new Medicine("Old Drug", "Old Labs", LocalDate.now().minusYears(2), LocalDate.now().minusDays(1), 10, 5.0);
        medicineService.addMedicine(expiredMed);
        assertEquals(0.0, medicineService.calculateItemSubtotal(expiredMed.getId(), 2));
    }

    @Test
    public void testIsStockAvailable() {
        Medicine med = new Medicine("Metformin", "Merck", LocalDate.now(), LocalDate.now().plusYears(1), 10, 15.0);
        medicineService.addMedicine(med);

        assertTrue(medicineService.isStockAvailable(med.getId(), 5));
        assertTrue(medicineService.isStockAvailable(med.getId(), 10));
        assertFalse(medicineService.isStockAvailable(med.getId(), 15));
        assertFalse(medicineService.isStockAvailable(med.getId(), -1));
        assertFalse(medicineService.isStockAvailable(999, 1));
    }

    @Test
    public void testSearchAndFiltering() {
        Medicine m1 = new Medicine("Paracetamol Extra", "GSK", LocalDate.now(), LocalDate.now().plusYears(1), 50, 3.0);
        Medicine m2 = new Medicine("Paracetamol Junior", "GSK", LocalDate.now(), LocalDate.now().plusYears(1), 5, 2.0);
        Medicine m3 = new Medicine("Ibuprofen Rapid", "Pfizer", LocalDate.now(), LocalDate.now().plusDays(10), 2, 8.0);
        Medicine m4 = new Medicine("Expired Syrups", "Pfizer", LocalDate.now().minusYears(1), LocalDate.now().minusDays(5), 20, 4.0);

        medicineService.addMedicine(m1);
        medicineService.addMedicine(m2);
        medicineService.addMedicine(m3);
        medicineService.addMedicine(m4);

        // Search by name
        assertEquals(2, medicineService.searchMedicinesByName("paracetamol").size());
        assertEquals(1, medicineService.searchMedicinesByName("ibuprofen").size());
        assertEquals(0, medicineService.searchMedicinesByName("nonexistent").size());
        assertEquals(0, medicineService.searchMedicinesByName("").size());

        // By manufacturer
        assertEquals(2, medicineService.getMedicinesByManufacturer("Pfizer").size());

        // Low stock (threshold 5)
        List<Medicine> lowStock = medicineService.getLowStockMedicines(5);
        assertEquals(2, lowStock.size()); // m2 (5) and m3 (2)

        // Expired
        List<Medicine> expired = medicineService.getExpiredMedicines();
        assertEquals(1, expired.size());
        assertEquals("Expired Syrups", expired.get(0).getName());

        // Nearing expiry (within 30 days)
        List<Medicine> nearing = medicineService.getMedicinesNearingExpiry(30);
        assertEquals(1, nearing.size());
        assertEquals("Ibuprofen Rapid", nearing.get(0).getName());
    }
}
