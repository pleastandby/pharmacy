package com.pharmacy.dao;

import com.pharmacy.models.SaleItem;
import com.pharmacy.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SaleItemDAO {

    public void addSaleItem(SaleItem saleItem, Connection conn) throws SQLException {
        String sql = "INSERT INTO sale_items(sale_id, medicine_id, quantity, price, subtotal) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ptmt = conn.prepareStatement(sql);) {
            ptmt.setInt(1, saleItem.getSaleId());
            ptmt.setInt(2, saleItem.getMedicineId());
            ptmt.setInt(3, saleItem.getQuantity());
            ptmt.setDouble(4, saleItem.getPrice());
            ptmt.setDouble(5, saleItem.getSubTotal());

            ptmt.executeUpdate();
        }
    }

    public SaleItem getSaleItemById(int id) {
        String sql = "SELECT * FROM sale_items WHERE id=?";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ptmt = conn.prepareStatement(sql);) {
            ptmt.setInt(1, id);
            try (ResultSet rs = ptmt.executeQuery();) {
                if (rs.next()) {
                    SaleItem saleItem = new SaleItem(rs.getInt("sale_id"), rs.getInt("medicine_id"),
                            rs.getInt("quantity"), rs.getDouble("price"));
                    saleItem.setId(rs.getInt("id"));
                    return saleItem;
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL EXCEPTION: " + e.getMessage());
        }
        return null;

    }

    public List<SaleItem> getSaleItemsBySaleId(int saleId) {
        String sql = "SELECT * FROM sale_items WHERE sale_id=?";
        List<SaleItem> saleItems = new ArrayList<>();
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ptmt = conn.prepareStatement(sql);) {
            ptmt.setInt(1, saleId);
            try (ResultSet rs = ptmt.executeQuery();) {
                while (rs.next()) {
                    SaleItem saleItem = new SaleItem(rs.getInt("sale_id"), rs.getInt("medicine_id"),
                            rs.getInt("quantity"), rs.getDouble("price"));
                    saleItem.setId(rs.getInt("id"));
                    saleItems.add(saleItem);
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL EXCEPTION: " + e.getMessage());
        }
        return saleItems;
    }

    public void deleteSaleItem(SaleItem saleItem) {
        String sql = "DELETE FROM sale_items WHERE id=?";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ptmt = conn.prepareStatement(sql)) {
            ptmt.setInt(1, saleItem.getId());
            ptmt.executeUpdate();
            System.out.println(saleItem.getSaleId() + "Deleted successfully");

        } catch (SQLException e) {
            System.out.println("SQL Exception" + e.getMessage());
        }
    }

    public void deleteSaleItemsBySaleId(int saleId) {
        String sql = "DELETE FROM sale_items WHERE sale_id=?";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ptmt = conn.prepareStatement(sql);) {
            ptmt.setInt(1, saleId);
            ptmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Exception : " + e.getMessage());
        }
    }

}
