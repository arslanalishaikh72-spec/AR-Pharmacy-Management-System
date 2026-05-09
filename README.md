# AR Pharmacy System
**Java Swing + MySQL | NetBeans IDE**

---

## Quick Start

### 1. Prerequisites
| Tool | Version |
|------|---------|
| Java JDK | 11 or higher |
| NetBeans IDE | 12 or higher |
| MySQL Server | 5.7 / 8.x |
| MySQL Connector/J | 8.x (`mysql-connector-j-*.jar`) |

---

### 2. Database Setup
Open MySQL Workbench (or CLI) and run:
```sql
SOURCE /path/to/ARPharmacy/sql/pharmacy_setup.sql;
```
This creates the `ar_pharmacy` database with tables **users**, **medicines**, **sales**
and inserts a default admin account:
- **Username:** `admin`
- **Password:** `admin123`

---

### 3. Add MySQL Connector JAR
1. Download `mysql-connector-j-*.jar` from https://dev.mysql.com/downloads/connector/j/
2. Copy it into the `ARPharmacy/lib/` folder.
3. In NetBeans → right-click **Libraries → Add JAR/Folder** → select the jar.

---

### 4. Configure Database Connection
Open `src/arpharmacy/db/DBConnection.java` and edit:
```java
private static final String DB_URL  = "jdbc:mysql://localhost:3306/ar_pharmacy...";
private static final String DB_USER = "root";       // ← your MySQL user
private static final String DB_PASS = "";           // ← your MySQL password
```

---

### 5. Open in NetBeans
- **File → Open Project** → select the `ARPharmacy` folder.
- NetBeans will detect `nbproject/project.xml` automatically.
- Press **F6** (or Run → Run Project) to launch.

---

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

---

## Features
| Feature | Details |
|---------|---------|
| Role-based login | Admin → Admin Panel, Customer → Customer Panel |
| User CRUD | Add, View, Update Role, Delete |
| Medicine CRUD | Add, View, Update, Delete (Admin) |
| Buy Medicine | Stock deduction + sales record (Customer) |
| Input validation | All forms validate before DB calls |
| Error handling | Try/catch with user-friendly dialogs |
| CardLayout navigation | Single JFrame, zero extra windows |
| Styled UI | Green/Blue health theme, rounded buttons, striped tables |

---

## Default Login
| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |

Create customer accounts via Admin → Add User.

---

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
