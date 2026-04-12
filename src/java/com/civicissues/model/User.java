package com.civicissues.model;

/**
 * User.java - POJO representing a row in the `Users` table.
 * Roles: CITIZEN, MUNICIPAL_ADMIN, MAINTENANCE_CREW
 */
public class User {

    private int    userId;
    private String name;
    private String phone;
    private String email;
    private String passwordHash;
    private String role;           // 'CITIZEN' | 'MUNICIPAL_ADMIN' | 'MAINTENANCE_CREW'
    private int    departmentId;   // FK to Departments (0 if none)

    // --- No-arg Constructor ---
    public User() {}

    // --- Full Constructor ---
    public User(int userId, String name, String phone, String email,
                String passwordHash, String role, int departmentId) {
        this.userId       = userId;
        this.name         = name;
        this.phone        = phone;
        this.email        = email;
        this.passwordHash = passwordHash;
        this.role         = role;
        this.departmentId = departmentId;
    }

    // --- Getters & Setters ---
    public int    getUserId()       { return userId; }
    public void   setUserId(int v)  { this.userId = v; }

    public String getName()         { return name; }
    public void   setName(String v) { this.name = v; }

    public String getPhone()          { return phone; }
    public void   setPhone(String v)  { this.phone = v; }

    public String getEmail()          { return email; }
    public void   setEmail(String v)  { this.email = v; }

    public String getPasswordHash()          { return passwordHash; }
    public void   setPasswordHash(String v)  { this.passwordHash = v; }

    public String getRole()          { return role; }
    public void   setRole(String v)  { this.role = v; }

    public int  getDepartmentId()      { return departmentId; }
    public void setDepartmentId(int v) { this.departmentId = v; }

    @Override
    public String toString() {
        return "User{userId=" + userId + ", name='" + name + "', role='" + role + "'}";
    }
}
