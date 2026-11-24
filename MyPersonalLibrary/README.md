# My Personal Library — JSP Edition

A personal reading tracker built with **Jakarta Server Pages (JSP)**, **Servlets**, **JDBC**, and **MySQL**.
Migrated from an original Laravel/PHP Midterm Exam project for the Web Programming Final Exam.

---

## Features

| # | Feature | CRUD |
|---|---|---|
| 1 | User Accounts | Register, View Profile, Edit Name/Email, Change Password, Delete Account |
| 2 | Authors | Add, List, View, Edit, Delete |
| 3 | Categories | Add, List, View, Edit, Delete |
| 4 | Books | Add (with cover image + PDF/TXT upload), List, Search, Filter, View, Edit, Delete |
| 5 | Book Notes | Write, View, Edit, Delete private notes per book |
| 6 | Reading Logs | Log sessions (date + pages), View, Edit, Delete |
| 7 | Reading Goals | Create goals (count or finish-book type), View, Edit, Toggle Complete, Delete |
| 8 | Favorites | Add, View, Remove |
| + | Dashboard | Aggregated stats, recent books, recent logs, favorites, progress |
| + | In-Browser Reader | Stream PDF/TXT book files through an authenticated protected route |

---

## Technology Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Jakarta Servlets, JSP (JSTL) |
| Database | MySQL 8.0+ |
| DB Connectivity | JDBC (`mysql-connector-j`) |
| Password Hashing | jBCrypt |
| File Uploads | Jakarta Part API (multipart/form-data) |
| Frontend | HTML5, CSS3 (custom — preserves original design), vanilla JS |
| Build | Maven, WAR packaging |
| Server | Apache Tomcat 10+ |

---

## Project Structure

```
MyPersonalLibrary/
├── pom.xml
├── sql/
│   └── my_personal_library.sql      ← Full DB schema
├── uploads/                         ← Uploaded files (outside webroot)
│   ├── covers/
│   └── books/
└── src/main/
    ├── resources/
    │   └── db.properties            ← DB credentials (DO NOT commit)
    ├── java/com/mylibrary/
    │   ├── dao/                     ← JDBC Data Access Objects
    │   ├── filter/                  ← AuthFilter
    │   ├── model/                   ← Plain Java model classes
    │   ├── servlet/                 ← All Servlet controllers
    │   └── util/                    ← DBConnection, PasswordUtil, FileUploadUtil
    └── webapp/
        ├── css/app.css
        ├── js/app.js
        ├── images/                  ← Place welcome_buku.jpg here
        └── WEB-INF/
            ├── web.xml
            ├── db.properties        ← (also keep here for reference)
            └── jsp/                 ← All JSP views
```

---

## Local Setup

### Prerequisites
- Java 17+
- Maven 3.8+
- Apache Tomcat 10.1+ (Jakarta EE 10 / Servlet 6.0)
- MySQL 8.0.16+

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/MyPersonalLibrary.git
cd MyPersonalLibrary
```

### 2. Create the database

```bash
mysql -u root -p < sql/my_personal_library.sql
```

### 3. Configure database credentials

Edit `src/main/resources/db.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/my_personal_library?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
db.password=YOUR_PASSWORD
db.driver=com.mysql.cj.jdbc.Driver
```

### 4. Configure upload directory

The app needs a writable directory outside the webapp root to store uploaded files.
Subdirectories `covers/` and `books/` are created automatically on first startup.

The upload directory is resolved in this priority order:

| Priority | Source | Example |
|---|---|---|
| 1 | Environment variable `UPLOAD_DIR` | `export UPLOAD_DIR=/var/mylibrary/uploads` |
| 2 | JVM system property `-DuploadDir` | `JAVA_OPTS="-DuploadDir=/var/mylibrary/uploads"` |
| 3 | `web.xml` context-param `uploadDir` | see below |
| 4 | Default (no config needed) | `<user.home>/mylibrary/uploads` |

**Option A — environment variable (recommended for production):**
```bash
# Linux/macOS
export UPLOAD_DIR=/var/mylibrary/uploads

# Windows
set UPLOAD_DIR=C:\mylibrary\uploads
```

**Option B — JVM property (Tomcat `setenv.sh`):**
```bash
# Linux/macOS  $CATALINA_HOME/bin/setenv.sh
export JAVA_OPTS="$JAVA_OPTS -DuploadDir=/var/mylibrary/uploads"
```

**Option C — edit `web.xml`:**
Change the `<context-param>` for `uploadDir` to an absolute path:
```xml
<context-param>
    <param-name>uploadDir</param-name>
    <param-value>/absolute/path/to/uploads</param-value>
</context-param>
```

**Option D — no config (default):**
Leave everything as-is. Files will be stored in `<user.home>/mylibrary/uploads`.
On Windows this is typically `C:\Users\<YourName>\mylibrary\uploads`.

> **Important:** The Tomcat process user must have read/write permission on the upload directory.

### 5. Build and deploy

```bash
mvn clean package
# Copy the generated WAR to Tomcat's webapps/
cp target/MyPersonalLibrary.war /path/to/tomcat/webapps/
# Start Tomcat
/path/to/tomcat/bin/startup.sh
```

Access the app at: **http://localhost:8080/MyPersonalLibrary**

### 6. Register the first account

Navigate to `/register` and create your account.
After registering, you can optionally run the sample seed data from `sql/my_personal_library.sql`
(the commented INSERT statements at the bottom of the file).

---

## Serving cover images

Cover images are served by the built-in `CoverServlet` mapped to `/covers/*`.
**No Tomcat `<Context>` alias is required.** The servlet resolves files from
`<uploadDir>/covers/<filename>`, validates the path, and streams them securely.

The file `MyPersonalLibrary.xml` (the old Tomcat alias config) is now obsolete
and can be ignored.

> **Access control:** `/covers/*` is protected by `AuthFilter` by default.
> Unauthenticated users are redirected to `/login`.
> To make covers publicly accessible, remove the `/covers/*` filter-mapping from `web.xml`.

---

## Security notes

- All database queries use `PreparedStatement` — SQL injection is prevented.
- Passwords are hashed with BCrypt (work factor 12).
- Book files are served through an authenticated Servlet route — the file path is never exposed.
- Every book, note, log, goal, and favorite operation verifies `user_id` ownership.
- The `AuthFilter` redirects unauthenticated requests to `/login`.
- `db.properties` with real credentials must never be committed to Git (see `.gitignore`).

---

## GitHub

```bash
git init
git add .
git commit -m "Initial commit — My Personal Library JSP Final Exam"
git remote add origin https://github.com/YOUR_USERNAME/MyPersonalLibrary.git
git push -u origin main
```

---

## License

University project — for educational use.
