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
        String sql = "INSERT INTO medicines(name, manufacturer, manufacturing_date, expiry_date, quantity, price) VALUES (?,?,?,?,?,?)";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmtInsert = conn.prepareStatement(sql);) {
            pstmtInsert.setString(1, medicine.getName());
            pstmtInsert.setString(2, medicine.getManufacturer());
            pstmtInsert.setDate(3, java.sql.Date.valueOf(medicine.getManufacturingDate()));
            pstmtInsert.setDate(4, java.sql.Date.valueOf(medicine.getExpiryDate()));
            pstmtInsert.setInt(5, medicine.getQuantity());
            pstmtInsert.setDouble(6, medicine.getPrice());

            pstmtInsert.executeUpdate();
            System.out.println("Medicine added successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Medicine getMedicineById(int id) {
        String sql = "SELECT * FROM medicines where id=?";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmtSelect = conn.prepareStatement(sql);) {
            pstmtSelect.setInt(1, id);
            ResultSet rs = pstmtSelect.executeQuery();
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
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
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

}
