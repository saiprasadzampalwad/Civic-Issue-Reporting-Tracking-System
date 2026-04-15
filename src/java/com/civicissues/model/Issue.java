/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.civicissues.model;

import java.sql.Timestamp;
import java.util.List;

public class Issue {
    private int       issueId;
    private int       citizenId;
    private String    category;
    private String    gpsLocation;
    private String    description;
    private String    status;
    private int       assignedDepartmentId;
    private int       assignedCrewId;
    private Timestamp slaDeadline;
    private Timestamp createdAt;

    // JOIN / display fields
    private String    citizenName;
    private String    departmentName;
    private String    crewName;
    private boolean   slaBreached;
    private List<String> photoUrls;   // from Issue_Photos table
    private Rating    rating;         // if rated

    public Issue() {}

    public int       getIssueId()                      { return issueId; }
    public void      setIssueId(int v)                 { this.issueId = v; }
    public int       getCitizenId()                    { return citizenId; }
    public void      setCitizenId(int v)               { this.citizenId = v; }
    public String    getCategory()                     { return category; }
    public void      setCategory(String v)             { this.category = v; }
    public String    getGpsLocation()                  { return gpsLocation; }
    public void      setGpsLocation(String v)          { this.gpsLocation = v; }
    public String    getDescription()                  { return description; }
    public void      setDescription(String v)          { this.description = v; }
    public String    getStatus()                       { return status; }
    public void      setStatus(String v)               { this.status = v; }
    public int       getAssignedDepartmentId()         { return assignedDepartmentId; }
    public void      setAssignedDepartmentId(int v)    { this.assignedDepartmentId = v; }
    public int       getAssignedCrewId()               { return assignedCrewId; }
    public void      setAssignedCrewId(int v)          { this.assignedCrewId = v; }
    public Timestamp getSlaDeadline()                  { return slaDeadline; }
    public void      setSlaDeadline(Timestamp v)       { this.slaDeadline = v; }
    public Timestamp getCreatedAt()                    { return createdAt; }
    public void      setCreatedAt(Timestamp v)         { this.createdAt = v; }
    public String    getCitizenName()                  { return citizenName; }
    public void      setCitizenName(String v)          { this.citizenName = v; }
    public String    getDepartmentName()               { return departmentName; }
    public void      setDepartmentName(String v)       { this.departmentName = v; }
    public String    getCrewName()                     { return crewName; }
    public void      setCrewName(String v)             { this.crewName = v; }
    public boolean   isSlaBreached()                   { return slaBreached; }
    public void      setSlaBreached(boolean v)         { this.slaBreached = v; }
    public List<String> getPhotoUrls()                 { return photoUrls; }
    public void      setPhotoUrls(List<String> v)      { this.photoUrls = v; }
    public Rating    getRating()                       { return rating; }
    public void      setRating(Rating v)               { this.rating = v; }
}