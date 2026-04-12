/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.civicissues.model;

/**
 * Issue.java - POJO representing a row in the `Issues` table.
 * Status values: OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED
 */
public class Issue {

    private int    issueId;
    private int    citizenId;            // FK -> Users.user_id
    private String category;
    private String gpsLocation;
    private String description;
    private String photoUrl;
    private String status;               // OPEN | ASSIGNED | IN_PROGRESS | RESOLVED | CLOSED
    private int    assignedDepartmentId; // FK -> Departments (0 if unassigned)
    private int    assignedCrewId;       // FK -> Users (0 if unassigned)

    // Extra display fields (populated by JOIN queries, not stored columns)
    private String citizenName;
    private String departmentName;
    private String crewName;

    // --- No-arg Constructor ---
    public Issue() {}

    // --- Core Constructor ---
    public Issue(int issueId, int citizenId, String category, String gpsLocation,
                 String description, String photoUrl, String status,
                 int assignedDepartmentId, int assignedCrewId) {
        this.issueId              = issueId;
        this.citizenId            = citizenId;
        this.category             = category;
        this.gpsLocation          = gpsLocation;
        this.description          = description;
        this.photoUrl             = photoUrl;
        this.status               = status;
        this.assignedDepartmentId = assignedDepartmentId;
        this.assignedCrewId       = assignedCrewId;
    }

    // --- Getters & Setters ---
    public int    getIssueId()        { return issueId; }
    public void   setIssueId(int v)   { this.issueId = v; }

    public int    getCitizenId()       { return citizenId; }
    public void   setCitizenId(int v)  { this.citizenId = v; }

    public String getCategory()          { return category; }
    public void   setCategory(String v)  { this.category = v; }

    public String getGpsLocation()          { return gpsLocation; }
    public void   setGpsLocation(String v)  { this.gpsLocation = v; }

    public String getDescription()          { return description; }
    public void   setDescription(String v)  { this.description = v; }

    public String getPhotoUrl()          { return photoUrl; }
    public void   setPhotoUrl(String v)  { this.photoUrl = v; }

    public String getStatus()          { return status; }
    public void   setStatus(String v)  { this.status = v; }

    public int  getAssignedDepartmentId()      { return assignedDepartmentId; }
    public void setAssignedDepartmentId(int v) { this.assignedDepartmentId = v; }

    public int  getAssignedCrewId()       { return assignedCrewId; }
    public void setAssignedCrewId(int v)  { this.assignedCrewId = v; }

    // --- Display/join fields ---
    public String getCitizenName()          { return citizenName; }
    public void   setCitizenName(String v)  { this.citizenName = v; }

    public String getDepartmentName()          { return departmentName; }
    public void   setDepartmentName(String v)  { this.departmentName = v; }

    public String getCrewName()          { return crewName; }
    public void   setCrewName(String v)  { this.crewName = v; }

    @Override
    public String toString() {
        return "Issue{issueId=" + issueId + ", category='" + category + "', status='" + status + "'}";
    }
}