# AR Pharmacy Management System

## Project Description
AR Pharmacy Management System is a Java-based desktop application developed using Object-Oriented Programming (OOP) concepts and MySQL database integration. The system is designed to help manage medicines, inventory, customer records, and billing operations efficiently. This project demonstrates the practical implementation of core OOP principles such as encapsulation, inheritance, polymorphism, exception handling, and collections framework in Java.

---

## Group Members

| Name | CMS/ID | Section |
|------|---------|---------|
| Arsalan | 023-25-0153 | C |
| Rehan | 023-25-0176 | C |

---

## Purpose of the Project
The purpose of this project is to develop an efficient pharmacy management system that simplifies medicine handling, inventory tracking, and billing operations. The project also aims to demonstrate the use of Object-Oriented Programming concepts in a real-world Java application with database connectivity.

---

## Main Modules

- Medicine Management Module
- Inventory Management Module
- Customer Management Module
- Billing System
- Database Connectivity Module
- User Interface / Console Menu

---

## OOP Concepts Used

- Classes and Objects
- Encapsulation
- Inheritance
- Polymorphism
- Exception Handling
- Collections Framework

---

## Technologies Used

- Java
- MySQL
- JDBC (IDE: Netbeans)

---

## How to Run the Project

1. Install Java JDK on your system.
2. Import the project into IntelliJ IDEA or Eclipse.
3. Configure the MySQL database.
4. Run the SQL script provided in the `sql` folder.
5. Add the JDBC library from the `lib` folder.
6. Run the `Main.java` file to start the application.

## Project Structure
```
ARPharmacy/
├── src/
│   └── arpharmacy/
│       ├── Main.java               ← Entry point
│       ├── Theme.java              ← Colour / font constants
│       ├── UIHelper.java           ← Reusable Swing component factory
│       ├── db/
│       │   └── DBConnection.java   ← JDBC singleton
│       └── panels/
│           ├── LoginPanel.java
│           ├── AdminPanel.java
│           ├── AddUserPanel.java
│           ├── ViewUsersPanel.java
│           ├── UpdateRolePanel.java
│           ├── MedicinePanel.java  ← Admin CRUD + Customer buy
│           └── CustomerPanel.java
├── sql/
│   └── pharmacy_setup.sql
├── lib/                            ← Put mysql-connector-j.jar here
├── nbproject/
│   ├── project.xml
│   └── project.properties
├── build.xml
├── manifest.mf
└── README.md
```

## Troubleshooting
**"Cannot connect to database"**
- Make sure MySQL service is running (`services.msc` on Windows).
- Check DB_USER / DB_PASS in `DBConnection.java`.
- Verify `ar_pharmacy` database exists (run the SQL script).

**"MySQL Driver not found"**
- The `mysql-connector-j-*.jar` is not in your classpath.
- In NetBeans: right-click Libraries → Add JAR/Folder.

**Compile errors**
- Ensure JDK 11+ is set as the project platform in NetBeans.





## Demo Video Link

https://youtu.be/kAXr9yIge_Y

---

## GitHub Repository Link

https://github.com/arslanalishaikh72-spec/AR-Pharmacy-Management-System

