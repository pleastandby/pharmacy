package com.pharmacy.dao;

import com.pharmacy.database.DatabaseConnection;
import com.pharmacy.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public void addUser(User user) {
        String sql = "INSERT INTO users(username,password_hash,name,role) VALUES(?,?,?,?)";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getName());
            ps.setString(4, user.getRole().name());
            ps.executeUpdate();
            System.out.println("Data Added Successfully");

        } catch (SQLException e) {
            System.err.println("SQL Exception : " + e.getMessage());
        }
    }

    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id=?";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User(rs.getString("name"), rs.getString("username"), rs.getString("password_hash"),
                            User.Role.valueOf(rs.getString("role")));
                    user.setId(rs.getInt("id"));
                    return user;
                }
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception : " + e.getMessage());
        }
        return null;
    }

    public void updateUser(User user) {
        String sql = "UPDATE users SET username=?, password_hash=?, name=?, role=? WHERE id=?";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getName());
            ps.setString(4, user.getRole().name());
            ps.setInt(5, user.getId());
            ps.executeUpdate();
            System.out.println("User Updated Successfully");
        } catch (SQLException e) {
            System.err.println("SQL Exception : " + e.getMessage());
        }
    }

    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pt = conn.prepareStatement(sql);) {
            List<User> users = new ArrayList<>();
            try (ResultSet rs = pt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(rs.getString("name"), rs.getString("username"), rs.getString("password_hash"),
                            User.Role.valueOf(rs.getString("role")));
                    user.setId(rs.getInt("id"));
                    users.add(user);
                }
                return users;
            }
        } catch (SQLException e) {
            System.err.println("SQL Exception : " + e.getMessage());
        }
        return null;
    }

    public void deleteUser(User user) {
        String sql = "DELETE FROM users WHERE id=?";
        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ptmt = conn.prepareStatement(sql);) {
            ptmt.setInt(1, user.getId());
            ptmt.executeUpdate();
            System.out.println(user.getName() + "Deleted successfully!");
        } catch (SQLException e) {
            System.out.println("SQL Exception : " + e.getMessage());
        }
    }

}
