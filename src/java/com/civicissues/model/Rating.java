/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.civicissues.model;

import java.sql.Timestamp;

public class Rating {
    private int       ratingId;
    private int       issueId;
    private int       citizenId;
    private int       stars;
    private String    feedback;
    private Timestamp ratedAt;

    public Rating() {}

    public int       getRatingId()             { return ratingId; }
    public void      setRatingId(int v)        { this.ratingId = v; }
    public int       getIssueId()              { return issueId; }
    public void      setIssueId(int v)         { this.issueId = v; }
    public int       getCitizenId()            { return citizenId; }
    public void      setCitizenId(int v)       { this.citizenId = v; }
    public int       getStars()                { return stars; }
    public void      setStars(int v)           { this.stars = v; }
    public String    getFeedback()             { return feedback; }
    public void      setFeedback(String v)     { this.feedback = v; }
    public Timestamp getRatedAt()              { return ratedAt; }
    public void      setRatedAt(Timestamp v)   { this.ratedAt = v; }
}
