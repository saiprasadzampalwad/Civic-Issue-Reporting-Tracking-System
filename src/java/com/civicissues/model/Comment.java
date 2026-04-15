/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.civicissues.model;

import java.sql.Timestamp;

public class Comment {
    private int       commentId;
    private int       issueId;
    private int       userId;
    private String    comment;
    private Timestamp createdAt;
    private String    userName;  // JOIN field

    public Comment() {}

    public int       getCommentId()              { return commentId; }
    public void      setCommentId(int v)         { this.commentId = v; }
    public int       getIssueId()                { return issueId; }
    public void      setIssueId(int v)           { this.issueId = v; }
    public int       getUserId()                 { return userId; }
    public void      setUserId(int v)            { this.userId = v; }
    public String    getComment()                { return comment; }
    public void      setComment(String v)        { this.comment = v; }
    public Timestamp getCreatedAt()              { return createdAt; }
    public void      setCreatedAt(Timestamp v)   { this.createdAt = v; }
    public String    getUserName()               { return userName; }
    public void      setUserName(String v)       { this.userName = v; }
}