package com.pharmacy.models;

public class User {
    private int id;
    private String name;
    private String username;
    private String passwordHash;

    public enum Role {
        ADMIN,
        PHARMACIST,
        STAFF
    }

    private Role role;

    public User(String name, String username, String passwordHash, Role role) {
        this.name = name;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        if (username == null) {
            System.out.println("Username Cannot be Empty");
        }
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public void setRole(Role role) {
        if (role == null) {
            System.out.println("Role Cannot be Empty");
            return;
        }
        this.role = role;
    }

    public Role getRole() {
        return this.role;
    }
}
