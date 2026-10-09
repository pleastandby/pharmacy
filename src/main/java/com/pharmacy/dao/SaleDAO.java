package com.pharmacy.dao;

import com.pharmacy.database.DatabaseConnection;
import com.pharmacy.models.Sale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    public boolean addSale(Sale sale) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return false;
            }
            return addSale(sale, conn);
        } catch (SQLException e) {
            System.err.println("Failed to add sale: " + e.getMessage());
            return false;
        }
    }

    public boolean addSale(Sale sale, Connection conn) throws SQLException {
        String sql;
        boolean hasDate = sale.getSaleDate() != null;
        if (hasDate) {
            sql = "INSERT INTO sales(customer_id, total_amount, sale_date) VALUES (?, ?, ?)";
        } else {
            sql = "INSERT INTO sales(customer_id, total_amount) VALUES (?, ?)";
        }

        try (PreparedStatement pstmtInsert = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmtInsert.setInt(1, sale.getCustomerId());
            pstmtInsert.setDouble(2, sale.getTotalAmount());
            if (hasDate) {
                pstmtInsert.setTimestamp(3, Timestamp.valueOf(sale.getSaleDate()));
            }

            int rows = pstmtInsert.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Failed to insert sale.");
            }

            try (ResultSet rs = pstmtInsert.getGeneratedKeys()) {
                if (rs.next()) {
                    sale.setId(rs.getInt(1));
                }
            }

            if (!hasDate) {
                sale.setSaleDate(LocalDateTime.now());
            }

            return true;
        }
    }

    public Sale getSaleById(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return null;
            }
            return getSaleById(id, conn);
        } catch (SQLException e) {
            System.err.println("Failed to retrieve sale: " + e.getMessage());
            return null;
        }
    }

    public Sale getSaleById(int id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM sales WHERE id = ?";

        try (PreparedStatement pstmtSelect = conn.prepareStatement(sql)) {
            pstmtSelect.setInt(1, id);
            try (ResultSet rs = pstmtSelect.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSale(rs);
                }
                return null;
            }
        }
    }

    public List<Sale> getAllSales() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return new ArrayList<>();
            }
            return getAllSales(conn);
        }
    }

    public List<Sale> getAllSales(Connection conn) throws SQLException {
        String sql = "SELECT * FROM sales ORDER BY id DESC";
        try (PreparedStatement pstmtSelect = conn.prepareStatement(sql);
             ResultSet rs = pstmtSelect.executeQuery()) {
            List<Sale> saleList = new ArrayList<>();
            while (rs.next()) {
                saleList.add(mapResultSetToSale(rs));
            }
            return saleList;
        }
    }

    public List<Sale> getSalesByCustomerId(int customerId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return new ArrayList<>();
            }
            return getSalesByCustomerId(customerId, conn);
        } catch (SQLException e) {
            System.err.println("Failed to retrieve sales for customer " + customerId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Sale> getSalesByCustomerId(int customerId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM sales WHERE customer_id = ? ORDER BY id DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Sale> sales = new ArrayList<>();
                while (rs.next()) {
                    sales.add(mapResultSetToSale(rs));
                }
                return sales;
            }
        }
    }

    public List<Sale> getSalesBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return new ArrayList<>();
            }
            return getSalesBetweenDates(startDate, endDate, conn);
        } catch (SQLException e) {
            System.err.println("Failed to retrieve sales between dates: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Sale> getSalesBetweenDates(LocalDateTime startDate, LocalDateTime endDate, Connection conn) throws SQLException {
        String sql = "SELECT * FROM sales WHERE sale_date >= ? AND sale_date <= ? ORDER BY sale_date DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(startDate));
            pstmt.setTimestamp(2, Timestamp.valueOf(endDate));
            try (ResultSet rs = pstmt.executeQuery()) {
                List<Sale> sales = new ArrayList<>();
                while (rs.next()) {
                    sales.add(mapResultSetToSale(rs));
                }
                return sales;
            }
        }
    }

    public boolean updateSale(Sale sale) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return false;
            }
            return updateSale(sale, conn);
        } catch (SQLException e) {
            System.err.println("Failed to update sale: " + e.getMessage());
            return false;
        }
    }

    public boolean updateSale(Sale sale, Connection conn) throws SQLException {
        String sql = "UPDATE sales SET customer_id = ?, total_amount = ? WHERE id = ?";
        try (PreparedStatement pstmtUpdate = conn.prepareStatement(sql)) {
            pstmtUpdate.setInt(1, sale.getCustomerId());
            pstmtUpdate.setDouble(2, sale.getTotalAmount());
            pstmtUpdate.setInt(3, sale.getId());

            return pstmtUpdate.executeUpdate() > 0;
        }
    }

    public boolean deleteSale(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                System.err.println("Could not connect to database.");
                return false;
            }

            conn.setAutoCommit(false);
            try {
                boolean success = deleteSale(id, conn);
                if (success) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Failed to delete sale: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Database error during sale deletion: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteSale(int id, Connection conn) throws SQLException {
        SaleItemDAO saleItemDAO = new SaleItemDAO();
        saleItemDAO.deleteSaleItemsBySaleId(id, conn);

        String sql = "DELETE FROM sales WHERE id = ?";
        try (PreparedStatement pstmtDelete = conn.prepareStatement(sql)) {
            pstmtDelete.setInt(1, id);
            return pstmtDelete.executeUpdate() > 0;
        }
    }

    private Sale mapResultSetToSale(ResultSet rs) throws SQLException {
        Sale sale = new Sale(rs.getInt("customer_id"));
        sale.setId(rs.getInt("id"));
        Timestamp ts = rs.getTimestamp("sale_date");
        if (ts != null) {
            sale.setSaleDate(ts.toLocalDateTime());
        }
        sale.setTotalAmount(rs.getDouble("total_amount"));
        return sale;
    }
}
