package com.pharmacy.dao;

import com.pharmacy.database.DatabaseConnection;
import com.pharmacy.models.Medicine;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class MedicineDAO {

    public void addMedicine(Medicine medicine) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Could not connect to database.");
            }

            addMedicine(medicine, conn);

        } catch (SQLException e) {
            System.err.println("Failed to add medicine: " + e.getMessage());
        }
    }

    // Connection-aware operation
    public void addMedicine(Medicine medicine, Connection conn)
            throws SQLException {

        String sql = "INSERT INTO medicines "
                + "(name, manufacturer, manufacturing_date, expiry_date, quantity, price) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, medicine.getName());
            pstmt.setString(2, medicine.getManufacturer());

            if (medicine.getManufacturingDate() != null) {
                pstmt.setDate(3,
                        java.sql.Date.valueOf(medicine.getManufacturingDate()));
            } else {
                pstmt.setNull(3, java.sql.Types.DATE);
            }

            pstmt.setDate(4,
                    java.sql.Date.valueOf(medicine.getExpiryDate()));
            pstmt.setInt(5, medicine.getQuantity());
            pstmt.setDouble(6, medicine.getPrice());

            int rowsInserted = pstmt.executeUpdate();

            if (rowsInserted == 0) {
                throw new SQLException("Medicine was not inserted.");
            }
        }
    }

    public Medicine getMedicineById(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Could not connect to database.");
            }

            return getMedicineById(id, conn);

        } catch (SQLException e) {
            System.err.println("Failed to retrieve medicine: " + e.getMessage());
            return null;
        }
    }

    // we're overloading this for the sale transactions
    public Medicine getMedicineById(int id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE id=?";

        try (PreparedStatement pstmtSelect = conn.prepareStatement(sql)) {

            pstmtSelect.setInt(1, id);

            try (ResultSet rs = pstmtSelect.executeQuery()) {
                if (rs.next()) {
                    Medicine med = new Medicine(
                            rs.getString("name"),
                            rs.getString("manufacturer"),
                            rs.getDate("manufacturing_date").toLocalDate(),
                            rs.getDate("expiry_date").toLocalDate(),
                            rs.getInt("quantity"),
                            rs.getDouble("price"));

                    med.setId(rs.getInt("id"));
                    return med;
                }

                return null;
            }
        }
    }

    public List<Medicine> getAllMedicines() {
        String sql = "SELECT * FROM medicines";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmtSelect = conn.prepareStatement(sql);) {
            ResultSet rs = pstmtSelect.executeQuery();
            List<Medicine> medicineList = new ArrayList<>();
            while (rs.next()) {
                Medicine med = new Medicine(
                        rs.getString("name"),
                        rs.getString("manufacturer"),
                        rs.getDate("manufacturing_date").toLocalDate(),
                        rs.getDate("expiry_date").toLocalDate(),
                        rs.getInt("quantity"),
                        rs.getDouble("price"));
                med.setId(rs.getInt("id"));
                medicineList.add(med);
            }
            return medicineList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    public void updateStock(int medicineId, int newQuantity, Connection conn)
            throws SQLException {

        String sql = "UPDATE medicines SET quantity = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, medicineId);

            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated == 0) {
                throw new SQLException("Medicine not found: " + medicineId);
            }
        }
    }

    public void updateMedicine(int id, Medicine medicine) {
        String sql = "UPDATE medicines SET name = ? , manufacturer = ? , manufacturing_date = ? , expiry_date = ? , quantity = ? , price = ? WHERE id = ?";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmtUpdate = conn.prepareStatement(sql);) {
            pstmtUpdate.setString(1, medicine.getName());
            pstmtUpdate.setString(2, medicine.getManufacturer());
            pstmtUpdate.setDate(3, java.sql.Date.valueOf(medicine.getManufacturingDate()));
            pstmtUpdate.setDate(4, java.sql.Date.valueOf(medicine.getExpiryDate()));
            pstmtUpdate.setInt(5, medicine.getQuantity());
            pstmtUpdate.setDouble(6, medicine.getPrice());
            pstmtUpdate.setInt(7, id);

            pstmtUpdate.executeUpdate();
            System.out.println("Medicine updated successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteMedicineById(int id) {
        String sql = "DELETE FROM medicines WHERE id=?";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmtDelete = conn.prepareStatement(sql);) {
            Medicine med = this.getMedicineById(id);
            pstmtDelete.setInt(1, id);
            pstmtDelete.executeUpdate();
            System.out.println(med.getName() + " Medicine deleted successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
