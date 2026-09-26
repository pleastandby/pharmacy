package com.pharmacy.dao;

import com.pharmacy.models.Customer;
import com.pharmacy.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public void addCustomer(Customer customer) {
        String sql = "INSERT INTO customers(name,phone) VALUES(?,?)";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.executeUpdate();
            System.out.println("Data Added Successfully");

        } catch (SQLException e) {
            System.err.println("SQL Exception : " + e.getMessage());
        }
    }

    public List<Customer> getAllCustomers() {
        String sql = "SELECT * FROM customers";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            try (ResultSet rs = ps.executeQuery()) {
                List<Customer> customers = new ArrayList<>();

                while (rs.next()) {
                    Customer customer = new Customer(rs.getString("name"), rs.getString("phone"));
                    customer.setId(rs.getInt("id"));
                    customers.add(customer);
                }
                return customers;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Customer getCustomerById(int id) {
        String sql = "SELECT * FROM customers WHERE id=?";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pt = conn.prepareStatement(sql)) {
            pt.setInt(1, id);
            try (ResultSet rs = pt.executeQuery()) {
                if (rs.next()) {
                    Customer customer = new Customer(rs.getString("name"), rs.getString("phone"));
                    customer.setId(rs.getInt("id"));
                    return customer;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception : " + e.getMessage());
        }
        return null;
    }

    public void updateCustomer(int id, Customer customer) {
        String sql = "UPDATE customers SET name=?, phone=? WHERE id=?";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhone());
            ps.setInt(3, id);
            ps.executeUpdate();
            System.out.println(customer.getName() + " Updated Successfully");
        } catch (SQLException e) {
            System.out.println("SQL Exception : " + e.getMessage());
        }
    }

    public void deleteCustomer(int id) {
        String sql = "DELETE FROM customers WHERE id =?";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pt = conn.prepareStatement(sql);) {
            pt.setInt(1, id);
            pt.executeUpdate();
            System.out.println("Customer Deleted Successfully");
        } catch (SQLException e) {
            System.out.println("SQL Exception : " + e.getMessage());
        }
    }
}
