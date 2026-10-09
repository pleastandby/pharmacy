package com.pharmacy.dao;

import com.pharmacy.database.DatabaseConnection;
import com.pharmacy.models.Medicine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO {

    public boolean addMedicine(Medicine medicine) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return false;
            }
            return addMedicine(medicine, conn);
        } catch (SQLException e) {
            System.err.println("Failed to add medicine: " + e.getMessage());
            return false;
        }
    }

    public boolean addMedicine(Medicine medicine, Connection conn) throws SQLException {
        String sql = "INSERT INTO medicines "
                + "(name, manufacturer, manufacturing_date, expiry_date, quantity, price) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, medicine.getName());
            pstmt.setString(2, medicine.getManufacturer());

            if (medicine.getManufacturingDate() != null) {
                pstmt.setDate(3, java.sql.Date.valueOf(medicine.getManufacturingDate()));
            } else {
                pstmt.setNull(3, java.sql.Types.DATE);
            }

            if (medicine.getExpiryDate() != null) {
                pstmt.setDate(4, java.sql.Date.valueOf(medicine.getExpiryDate()));
            } else {
                pstmt.setNull(4, java.sql.Types.DATE);
            }

            pstmt.setInt(5, medicine.getQuantity());
            pstmt.setDouble(6, medicine.getPrice());

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted == 0) {
                throw new SQLException("Medicine was not inserted.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    medicine.setId(generatedKeys.getInt(1));
                }
            }

            return true;
        }
    }

    public Medicine getMedicineById(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return null;
            }
            return getMedicineById(id, conn);
        } catch (SQLException e) {
            System.err.println("Failed to retrieve medicine: " + e.getMessage());
            return null;
        }
    }

    public Medicine getMedicineById(int id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE id = ?";

        try (PreparedStatement pstmtSelect = conn.prepareStatement(sql)) {
            pstmtSelect.setInt(1, id);

            try (ResultSet rs = pstmtSelect.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMedicine(rs);
                }
                return null;
            }
        }
    }

    public List<Medicine> getAllMedicines() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return new ArrayList<>();
            }
            return getAllMedicines(conn);
        } catch (SQLException e) {
            System.err.println("Failed to retrieve medicines: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Medicine> getAllMedicines(Connection conn) throws SQLException {
        String sql = "SELECT * FROM medicines";
        try (PreparedStatement pstmtSelect = conn.prepareStatement(sql);
             ResultSet rs = pstmtSelect.executeQuery()) {
            List<Medicine> medicineList = new ArrayList<>();
            while (rs.next()) {
                medicineList.add(mapResultSetToMedicine(rs));
            }
            return medicineList;
        }
    }

    public List<Medicine> searchMedicinesByName(String name) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return new ArrayList<>();
            }
            return searchMedicinesByName(name, conn);
        } catch (SQLException e) {
            System.err.println("Failed to search medicines by name: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Medicine> searchMedicinesByName(String name, Connection conn) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE LOWER(name) LIKE LOWER(?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Medicine> medicines = new ArrayList<>();
                while (rs.next()) {
                    medicines.add(mapResultSetToMedicine(rs));
                }
                return medicines;
            }
        }
    }

    public List<Medicine> getMedicinesByManufacturer(String manufacturer) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return new ArrayList<>();
            }
            return getMedicinesByManufacturer(manufacturer, conn);
        } catch (SQLException e) {
            System.err.println("Failed to get medicines by manufacturer: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Medicine> getMedicinesByManufacturer(String manufacturer, Connection conn) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE LOWER(manufacturer) LIKE LOWER(?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + manufacturer + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Medicine> medicines = new ArrayList<>();
                while (rs.next()) {
                    medicines.add(mapResultSetToMedicine(rs));
                }
                return medicines;
            }
        }
    }

    public List<Medicine> getLowStockMedicines(int threshold) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return new ArrayList<>();
            }
            return getLowStockMedicines(threshold, conn);
        } catch (SQLException e) {
            System.err.println("Failed to retrieve low stock medicines: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Medicine> getLowStockMedicines(int threshold, Connection conn) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE quantity <= ? ORDER BY quantity ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, threshold);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Medicine> medicines = new ArrayList<>();
                while (rs.next()) {
                    medicines.add(mapResultSetToMedicine(rs));
                }
                return medicines;
            }
        }
    }

    public List<Medicine> getExpiredMedicines() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return new ArrayList<>();
            }
            return getExpiredMedicines(conn);
        } catch (SQLException e) {
            System.err.println("Failed to retrieve expired medicines: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Medicine> getExpiredMedicines(Connection conn) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE expiry_date < CURRENT_DATE ORDER BY expiry_date ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            List<Medicine> medicines = new ArrayList<>();
            while (rs.next()) {
                medicines.add(mapResultSetToMedicine(rs));
            }
            return medicines;
        }
    }

    public List<Medicine> getMedicinesNearingExpiry(int days) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return new ArrayList<>();
            }
            return getMedicinesNearingExpiry(days, conn);
        } catch (SQLException e) {
            System.err.println("Failed to retrieve medicines nearing expiry: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Medicine> getMedicinesNearingExpiry(int days, Connection conn) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE expiry_date >= CURRENT_DATE AND expiry_date <= DATE_ADD(CURRENT_DATE, INTERVAL ? DAY) ORDER BY expiry_date ASC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, days);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Medicine> medicines = new ArrayList<>();
                while (rs.next()) {
                    medicines.add(mapResultSetToMedicine(rs));
                }
                return medicines;
            }
        }
    }

    public boolean updateStock(int medicineId, int newQuantity) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return false;
            }
            return updateStock(medicineId, newQuantity, conn);
        } catch (SQLException e) {
            System.err.println("Failed to update stock: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStock(int medicineId, int newQuantity, Connection conn) throws SQLException {
        String sql = "UPDATE medicines SET quantity = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, medicineId);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("Medicine not found with ID: " + medicineId);
            }
            return true;
        }
    }

    public boolean updateMedicine(Medicine medicine) {
        if (medicine == null) {
            System.err.println("Medicine cannot be null for update.");
            return false;
        }
        return updateMedicine(medicine.getId(), medicine);
    }

    public boolean updateMedicine(int id, Medicine medicine) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return false;
            }
            return updateMedicine(id, medicine, conn);
        } catch (SQLException e) {
            System.err.println("Failed to update medicine: " + e.getMessage());
            return false;
        }
    }

    public boolean updateMedicine(int id, Medicine medicine, Connection conn) throws SQLException {
        String sql = "UPDATE medicines SET name = ?, manufacturer = ?, manufacturing_date = ?, expiry_date = ?, quantity = ?, price = ? WHERE id = ?";

        try (PreparedStatement pstmtUpdate = conn.prepareStatement(sql)) {
            pstmtUpdate.setString(1, medicine.getName());
            pstmtUpdate.setString(2, medicine.getManufacturer());

            if (medicine.getManufacturingDate() != null) {
                pstmtUpdate.setDate(3, java.sql.Date.valueOf(medicine.getManufacturingDate()));
            } else {
                pstmtUpdate.setNull(3, java.sql.Types.DATE);
            }

            if (medicine.getExpiryDate() != null) {
                pstmtUpdate.setDate(4, java.sql.Date.valueOf(medicine.getExpiryDate()));
            } else {
                pstmtUpdate.setNull(4, java.sql.Types.DATE);
            }

            pstmtUpdate.setInt(5, medicine.getQuantity());
            pstmtUpdate.setDouble(6, medicine.getPrice());
            pstmtUpdate.setInt(7, id);

            int rowsUpdated = pstmtUpdate.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    public boolean deleteMedicineById(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return false;
            }
            return deleteMedicineById(id, conn);
        } catch (SQLException e) {
            System.err.println("Failed to delete medicine: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteMedicineById(int id, Connection conn) throws SQLException {
        String sql = "DELETE FROM medicines WHERE id = ?";

        try (PreparedStatement pstmtDelete = conn.prepareStatement(sql)) {
            pstmtDelete.setInt(1, id);
            int rowsDeleted = pstmtDelete.executeUpdate();
            return rowsDeleted > 0;
        }
    }

    public boolean deleteMedicine(int id) {
        return deleteMedicineById(id);
    }

    private Medicine mapResultSetToMedicine(ResultSet rs) throws SQLException {
        java.sql.Date mfgDate = rs.getDate("manufacturing_date");
        java.sql.Date expDate = rs.getDate("expiry_date");

        Medicine med = new Medicine(
                rs.getString("name"),
                rs.getString("manufacturer"),
                mfgDate != null ? mfgDate.toLocalDate() : null,
                expDate != null ? expDate.toLocalDate() : null,
                rs.getInt("quantity"),
                rs.getDouble("price"));
        med.setId(rs.getInt("id"));
        return med;
    }
}
