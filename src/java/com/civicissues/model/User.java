package com.civicissues.model;

import java.sql.Timestamp;

public class User {
    private int       userId;
    private String    name;
    private String    phone;
    private String    passwordHash;
    private String    role;
    private int       departmentId;
    private int       failedAttempts;
    private Timestamp lockedUntil;

    public User() {}

    public int       getUserId()                   { return userId; }
    public void      setUserId(int v)              { this.userId = v; }
    public String    getName()                     { return name; }
    public void      setName(String v)             { this.name = v; }
    public String    getPhone()                    { return phone; }
    public void      setPhone(String v)            { this.phone = v; }
    public String    getPasswordHash()             { return passwordHash; }
    public void      setPasswordHash(String v)     { this.passwordHash = v; }
    public String    getRole()                     { return role; }
    public void      setRole(String v)             { this.role = v; }
    public int       getDepartmentId()             { return departmentId; }
    public void      setDepartmentId(int v)        { this.departmentId = v; }
    public int       getFailedAttempts()           { return failedAttempts; }
    public void      setFailedAttempts(int v)      { this.failedAttempts = v; }
    public Timestamp getLockedUntil()              { return lockedUntil; }
    public void      setLockedUntil(Timestamp v)   { this.lockedUntil = v; }
}

