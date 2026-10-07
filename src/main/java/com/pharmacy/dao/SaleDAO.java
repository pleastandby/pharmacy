package com.pharmacy.dao;

import com.pharmacy.database.DatabaseConnection;
import com.pharmacy.models.Sale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    public void addSale(Sale sale, Connection conn) throws SQLException {
        String sql = "INSERT INTO sales(customer_id, total_amount) VALUES (?,?)";
        try (PreparedStatement pstmtInsert = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);) {

            pstmtInsert.setInt(1, sale.getCustomerId());
            pstmtInsert.setDouble(2, sale.getTotalAmount());

            pstmtInsert.executeUpdate();
            try (ResultSet rs = pstmtInsert.getGeneratedKeys();) {
                if (rs.next()) {
                    sale.setId(rs.getInt(1));
                }
            }
        }
    }

    public void updateSale(Sale sale) {
        String sql = "UPDATE sales SET customer_id = ? , total_amount = ? WHERE id = ?";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmtUpdate = conn.prepareStatement(sql);) {
            pstmtUpdate.setInt(1, sale.getCustomerId());
            pstmtUpdate.setDouble(2, sale.getTotalAmount());
            pstmtUpdate.setInt(3, sale.getId());

            pstmtUpdate.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Exception : " + e.getMessage());
        }
    }

    public Sale getSaleById(int id) {
        String sql = "SELECT * FROM sales WHERE id=?";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmtSelect = conn.prepareStatement(sql);) {
            pstmtSelect.setInt(1, id);
            try (ResultSet rs = pstmtSelect.executeQuery();) {
                if (rs.next()) {
                    Sale sale = new Sale(rs.getInt("customer_id"));
                    sale.setId(rs.getInt("id"));
                    sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
                    return sale;
                }
            }
            return null;
        } catch (SQLException e) {
            System.out.println("SQL Exception : " + e.getMessage());
            return null;
        }
    }

    public List<Sale> getAllSales() {
        String sql = "SELECT * FROM sales";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmtSelect = conn.prepareStatement(sql);) {
            try (ResultSet rs = pstmtSelect.executeQuery();) {
                List<Sale> saleList = new ArrayList<>();
                while (rs.next()) {
                    Sale sale = new Sale(rs.getInt("customer_id"));
                    sale.setId(rs.getInt("id"));
                    sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
                    saleList.add(sale);
                }
                return saleList;
            }
        } catch (SQLException e) {
            System.out.println("SQL Exception : " + e.getMessage());
            return null;
        }
    }

    public void deleteSale(int id) {
        SaleItemDAO saleItemDAO = new SaleItemDAO();
        saleItemDAO.deleteSaleItemsBySaleId(id);
        String sql = "DELETE FROM sales WHERE id=?";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmtDelete = conn.prepareStatement(sql);) {
            pstmtDelete.setInt(1, id);
            pstmtDelete.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQL Exception : " + e.getMessage());
        }
    }
}
