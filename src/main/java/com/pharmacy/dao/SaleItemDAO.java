package com.pharmacy.dao;

import com.pharmacy.database.DatabaseConnection;
import com.pharmacy.models.SaleItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SaleItemDAO {

    public boolean addSaleItem(SaleItem saleItem) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return false;
            }
            return addSaleItem(saleItem, conn);
        } catch (SQLException e) {
            System.err.println("Failed to add sale item: " + e.getMessage());
            return false;
        }
    }

    public boolean addSaleItem(SaleItem saleItem, Connection conn) throws SQLException {
        String sql = "INSERT INTO sale_items(sale_id, medicine_id, quantity, price, subtotal) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, saleItem.getSaleId());
            pstmt.setInt(2, saleItem.getMedicineId());
            pstmt.setInt(3, saleItem.getQuantity());
            pstmt.setDouble(4, saleItem.getPrice());
            pstmt.setDouble(5, saleItem.getSubTotal());

            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Failed to insert sale item.");
            }

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    saleItem.setId(rs.getInt(1));
                }
            }
            return true;
        }
    }

    public SaleItem getSaleItemById(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return null;
            }
            return getSaleItemById(id, conn);
        } catch (SQLException e) {
            System.err.println("Failed to retrieve sale item: " + e.getMessage());
            return null;
        }
    }

    public SaleItem getSaleItemById(int id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM sale_items WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSaleItem(rs);
                }
                return null;
            }
        }
    }

    public List<SaleItem> getSaleItemsBySaleId(int saleId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return new ArrayList<>();
            }
            return getSaleItemsBySaleId(saleId, conn);
        } catch (SQLException e) {
            System.err.println("Failed to retrieve sale items for sale " + saleId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<SaleItem> getSaleItemsBySaleId(int saleId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM sale_items WHERE sale_id = ?";
        List<SaleItem> saleItems = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    saleItems.add(mapResultSetToSaleItem(rs));
                }
            }
        }
        return saleItems;
    }

    public boolean deleteSaleItem(SaleItem saleItem) {
        if (saleItem == null) {
            return false;
        }
        return deleteSaleItemById(saleItem.getId());
    }

    public boolean deleteSaleItemById(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return false;
            }
            return deleteSaleItemById(id, conn);
        } catch (SQLException e) {
            System.err.println("Failed to delete sale item: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteSaleItemById(int id, Connection conn) throws SQLException {
        String sql = "DELETE FROM sale_items WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteSaleItemsBySaleId(int saleId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return false;
            }
            return deleteSaleItemsBySaleId(saleId, conn);
        } catch (SQLException e) {
            System.err.println("Failed to delete sale items for sale " + saleId + ": " + e.getMessage());
            return false;
        }
    }

    public boolean deleteSaleItemsBySaleId(int saleId, Connection conn) throws SQLException {
        String sql = "DELETE FROM sale_items WHERE sale_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            pstmt.executeUpdate();
            return true;
        }
    }

    private SaleItem mapResultSetToSaleItem(ResultSet rs) throws SQLException {
        SaleItem item = new SaleItem(
                rs.getInt("sale_id"),
                rs.getInt("medicine_id"),
                rs.getInt("quantity"),
                rs.getDouble("price"));
        item.setId(rs.getInt("id"));
        return item;
    }
}
