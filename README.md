# Civic Issue Reporting & Tracking System

## 📋 Overview
Web application for citizens to report civic issues (potholes, garbage, etc.) to municipal authorities. Built with Java Servlets, JSP, MySQL, Bootstrap.

## 🚀 Features
- User roles: Citizen, Admin, Maintenance Crew
- Issue reporting with status tracking
- Login/Logout with session management
- Responsive UI (Bootstrap 5)

## 🛠 Tech Stack
- Backend: Java 8, Servlet 3.1, JDBC
- Frontend: JSP, Bootstrap 5, JSTL
- Database: MySQL 8.0 (`civic_issues_db`)
- Server: Apache Tomcat 8
- IDE: NetBeans 8.2

## 📦 Deployment
1. **NetBeans:** Open project → Clean & Build → Run
2. **URL:** `http://localhost:8084/Civic_Issue_reporting_system/`

## 🗄 Database Setup
```
mysql -u root -p
CREATE DATABASE civic_issues_db;
```
See SQL schema in conversation history (users, issues tables).



## 📁 Project Structure
```
├── src/java/com/civicissues/
│   ├── controller/  (Servlets)
│   ├── dao/         (Data Access)
│   ├── model/       (POJOs)
│   └── util/        (DBConnection)
├── web/WEB-INF/views/ (JSPs)
├── lib/             (mysql-connector)
└── build/           (compiled)
```

## 🔧 Configuration
- **DBConnection.java:** Update password line 20
- **Add JSTL:** `web/WEB-INF/lib/jstl-1.2.jar`

## 📄 License
MIT License - Free to use/modify/distribute.

