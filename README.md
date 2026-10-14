# 🏢 Online Real Estate Management System
> **Academic Project Submission for B.Tech (Second Year) - Review 1**  
> Built with **Java 21**, **Spring Boot 3**, **Spring Data JPA**, **Spring Security**, **JDBC**, **Thymeleaf**, and **Bootstrap 5**.

---

## 📌 Project Overview
The **Online Real Estate Management System** is a full-stack, enterprise-grade web application designed to streamline property discovery, tenancy applications, and property management. It provides three dedicated, secure role-based portals:

1. **Administrator Portal**: Complete oversight over platform users, property listing approvals, system configurations, real-time activity auditing, and visual performance charts.
2. **Property Manager Portal**: Listing and managing rental units, reviewing incoming rental applications, drafting and issuing legally binding rental agreements, direct tenant messaging, and listing analytics.
3. **Tenant Portal**: Discovery and multi-criteria search of verified rentals, online application submission, active lease and agreement tracking, personal profile and rental history management, and automated notifications.

---

## 📊 Presentation File & Slides (Review 1 Deliverable)
An interactive slide deck and complete transcript have been prepared for this review:
1. **Interactive Slide Deck**: Open [`presentation_slides.html`](file:///Users/lalitkumar/real%20java%20project/presentation_slides.html) in your browser.
   - Use **&larr; / &rarr; keys** or the on-screen buttons to navigate slides.
   - Click the green **Export PDF** button (or press `Cmd + P` &rarr; *Save as PDF*) to generate your presentation PDF.
2. **Presentation Notes & Transcript**: Read [`PRESENTATION.md`](file:///Users/lalitkumar/real%20java%20project/PRESENTATION.md) for slide-by-slide speaker notes and Mermaid diagrams.

---

## 🏆 Marking Rubric Alignment (Full Marks Breakdown)

| Rubric Criteria | Allocated Marks | Implementation in This Project | Reference Classes |
| :--- | :---: | :--- | :--- |
| **OOP Implementation** (Inheritance, Polymorphism, Exceptions, Interfaces) | **10 Marks** | `BaseEntity` abstract class with common fields and polymorphic `getEntitySummary()`. Custom domain exception hierarchy. | `BaseEntity.java`, `User.java`, `Property.java`, `RealEstateException.java` |
| **Problem Understanding & Solution Design** | **8 Marks** | 3-role workflow solving manual property management delays, listing fraud, and unrecorded tenant interactions. | Architecture Flow, Controllers, Services |
| **Database Integration (JDBC)** | **8 Marks** | Pure JDBC DAO using `PreparedStatement`, `ResultSet`, batch updates, and manual transactions (`commit`/`rollback`). | `DatabaseConnectionManager.java`, `PropertyJdbcDao.java` |
| **Classes for Database Operations** | **7 Marks** | Separation of concerns with dedicated DAO classes and connection pooling. | `DatabaseConnectionManager.java`, `PropertyJdbcDao.java` |
| **Servlets & Web Integration** | **7 Marks** | Spring MVC `DispatcherServlet`, REST controllers, Spring Security 6 filter chains. | `SecurityConfig.java`, `JdbcDemoController.java`, `GlobalExceptionHandler.java` |
| **Collections & Generics** | **6 Marks** | Custom generic response wrapper `ApiResponse<T>`, Generic Repositories, Streams API. | `ApiResponse.java`, `PropertyRepository.java` |
| **Multithreading & Synchronization** | **4 Marks** | Thread-safe metric tracking using `AtomicLong`, `AtomicInteger`, `synchronized` blocks, and `ThreadPoolExecutor`. | `SystemMetricsTracker.java` |

---

## 👥 Pre-Configured Demo Accounts (Viva / Evaluation)

The application automatically seeds a comprehensive database on first launch with properties, applications, agreements, messages, and accounts. You can also use the **1-click autofill buttons** on the login page!

| Role | Email | Password | Dedicated Dashboard |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin@realestate.com` | `admin123` | [`/admin/dashboard`](http://localhost:8080/admin/dashboard) |
| **Property Manager** | `manager@realestate.com` | `manager123` | [`/manager/dashboard`](http://localhost:8080/manager/dashboard) |
| **Tenant** | `tenant@realestate.com` | `tenant123` | [`/tenant/dashboard`](http://localhost:8080/tenant/dashboard) |

---

## 🛠️ Technology Stack

- **Backend**: Java 21 / 27, Spring Boot 3.3.5
- **Web MVC & Security**: Spring MVC, Spring Security 6 (Role-Based Access Control + BCrypt password hashing)
- **Database & JDBC**: Spring Data JPA + Pure JDBC (`java.sql.Connection`, `PreparedStatement`, `ResultSet`), H2 Database (File-backed in `./data/realestatedb`)
- **Frontend / UI**: Thymeleaf Server-Side Templates, Bootstrap 5.3, Bootstrap Icons, Chart.js for data visualization
- **Concurrency**: `java.util.concurrent.atomic`, `ThreadPoolExecutor`, `synchronized` monitor locks
- **Build Tool**: Apache Maven 3.10+

---

## 💻 How to Run on macOS (Step-by-Step)

### Option 1: Using the 1-Click Startup Script (Recommended)
Open your Mac Terminal, navigate to the project directory, and execute:
```bash
cd "/Users/lalitkumar/real java project"
./run.sh
```

### Option 2: Using Maven Directly
```bash
cd "/Users/lalitkumar/real java project"
export MAVEN_OPTS="-Djava.net.preferIPv4Stack=true"
mvn spring-boot:run
```

Once you see:
```text
Tomcat started on port 8080 (http) with context path '/'
Started RealEstateManagementApplication in X.XXX seconds
```
Open your web browser (Safari or Google Chrome) and visit:  
👉 **[http://localhost:8080](http://localhost:8080)**

---

## 🗄️ Database Console & Live API Verification

### 1. H2 Database Web Console
- **URL**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
- **JDBC URL**: `jdbc:h2:file:./data/realestatedb`
- **Username**: `sa`
- **Password**: `password`

### 2. Live JDBC & Concurrency Endpoints
- **JDBC Aggregates & Thread-Safe Metrics**: [http://localhost:8080/api/jdbc/metrics](http://localhost:8080/api/jdbc/metrics)
- **JDBC PreparedStatement Filtering**: [http://localhost:8080/api/jdbc/filter?min=1000&max=3500](http://localhost:8080/api/jdbc/filter?min=1000&max=3500)

---

## 🚀 Key Modules & Implemented Functionalities

### 1. 🛡️ Administrator Features
- **User Account Management**: Full CRUD (Create, Read, Update, Delete) for tenants, property managers, and administrators with role assignment and account enable/disable switches.
- **Property Listing Approvals**: Review listings submitted by managers. Approve them to publish live or reject them with constructive feedback.
- **System Settings Configuration**: Manage system-wide parameters (site name, commission fee rates, currency symbol, max upload size, maintenance mode).
- **Activity Monitoring**: Real-time audit log of system events, logins, updates, and approvals.
- **Performance Reports**: Visual metrics and Chart.js graphs displaying property status distribution and user demographics.

### 2. 🏢 Property Manager Features
- **Property Listings Management**: Add new properties with address, rent, bedrooms, bathrooms, square footage, property type, and images; edit or delete existing listings.
- **Rental Applications Review**: Review tenant applications with move-in dates, monthly income, occupation, and notes. Accept or reject with feedback.
- **Rental Agreements**: Issue binding rental agreements for approved tenants with terms, rent, and security deposit. Updates property status to `Rented`.
- **Tenant Communication**: Direct 1-on-1 messaging interface with tenants.
- **Listing Analytics**: Interactive charts showing total property views, inquiry counts, and projected monthly rental revenue.

### 3. 🔑 Tenant Features
- **Property Search & Discovery**: Dynamic search bar and filters (city/keyword, property type, price range, bedrooms).
- **Rental Application**: Apply online with target move-in date, monthly income, occupation, and applicant statement.
- **Agreement Management**: View current and past lease contracts, terms, monthly dues, and print lease agreements.
- **Profile & History**: Update personal contact details, bio, and past rental history.
- **Notifications Panel**: Real-time alerts regarding application review outcomes and manager responses.

---

## 📤 GitHub Submission Guide

To publish this project to your GitHub account:

1. Create a new repository on [GitHub](https://github.com/new) named `online-real-estate-management-system`.
   - Set visibility to **Public**.
   - Do **not** initialize with a README (we already have a complete one).
2. Run these commands in your Mac terminal:
   ```bash
   cd "/Users/lalitkumar/real java project"
   git remote add origin https://github.com/YOUR_GITHUB_USERNAME/online-real-estate-management-system.git
   git branch -M main
   git push -u origin main
   ```
3. Copy your GitHub repository link and submit it in your university submission form!

---

## 📁 Project Architecture & Directory Structure
```text
real-java-project/
├── pom.xml                               # Maven dependencies & configuration
├── run.sh                                # 1-Click macOS startup script
├── README.md                             # Academic submission guide & documentation
├── PRESENTATION.md                       # Presentation transcript & Mermaid diagrams
├── presentation_slides.html              # Interactive 11-Slide Deck with PDF Export
├── src/main/java/com/realestate/
│   ├── RealEstateManagementApplication.java # Spring Boot entry point
│   ├── concurrency/                      # Multithreading, Atomic counters, ThreadPoolExecutor
│   │   └── SystemMetricsTracker.java
│   ├── config/                           # Security, UserDetailsService, DataInitializer
│   ├── controller/                       # Web & REST Controllers (Admin, Manager, Tenant, Auth, JDBC)
│   ├── exception/                        # Custom domain exceptions & GlobalExceptionHandler
│   ├── jdbc/                             # Pure JDBC ConnectionManager and PropertyJdbcDao
│   │   ├── DatabaseConnectionManager.java
│   │   └── PropertyJdbcDao.java
│   ├── model/                            # BaseEntity, User, Property, Application, Agreement, etc.
│   ├── repository/                       # Spring Data JPA interfaces
│   ├── service/                          # Business logic layer
│   └── util/                             # Generic ApiResponse<T>
└── src/main/resources/
    ├── application.properties            # Database and server configuration
    └── templates/                        # Thymeleaf views (admin/, manager/, tenant/, layout)
```
