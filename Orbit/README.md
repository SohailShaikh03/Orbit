# 🪐 ORBIT — "Different People. Same Orbit."

An interest-based social platform built with Java EE / Jakarta EE, Servlets, JDBC, and a Single Page Application (SPA) frontend. Orbit matches users through dynamic passion-compatibility scoring, powers community discussions on the MyVibe feed, and enables 1-to-1 private chat within verified Circles.

---

## 🏗️ Architecture & Data Flow

Orbit follows the classic **4-Tier MVC (Model-View-Controller)** enterprise pattern:

```
[ Browser Client ]
       │  (Fetch API / JSON)
       ▼
[ Controller Layer ]  ───  com.orbit.controller.* (Jakarta Servlets)
       │  (Java Objects)
       ▼
[ Service Layer ]     ───  com.orbit.service.* (Business Logic & Match Algorithm)
       │  (Entities)
       ▼
[ DAO Layer ]         ───  com.orbit.dao.* (JDBC Prepared Statements)
       │  (SQL Queries)
       ▼
[ Database ]          ───  MySQL ('Orbit' schema)
```

---

## 📂 Project Structure Explained

The project structure is organized by separation of concerns:

```
Orbit/
├── schema.sql                                ── MySQL 8+ database creation and table definitions
├── README.md                                 ── Complete project architecture and run guide
├── src/main/java/com/orbit/
│   ├── controller/                           ── HTTP Servlet Endpoints (JSON REST API)
│   │   ├── AuthController.java               ── Handles /api/auth (Login, Register, Logout, Profile)
│   │   ├── CircleController.java             ── Handles /api/circle (Friend requests, Circle list)
│   │   ├── FeedController.java               ── Handles /api/feed (MyVibe posts and comments)
│   │   ├── MatchController.java              ── Handles /api/match (Orbit match calculations)
│   │   └── MessageController.java            ── Handles /api/messages (1-to-1 direct messaging)
│   │
│   ├── service/                              ── Business Logic Layer
│   │   ├── CircleService.java                ── Manages friend connections and message routing
│   │   ├── FeedService.java                  ── Manages post publishing and comment threads
│   │   ├── MatchService.java                 ── Implements the Orbit interest matching algorithm
│   │   └── UserService.java                  ── Manages authentication and user validation
│   │
│   ├── dao/                                  ── Data Access Layer (JDBC & SQL queries)
│   │   ├── CommentDao.java                   ── Database CRUD operations for 'comments' table
│   │   ├── ConnectionDao.java                ── Database CRUD operations for 'connections' table
│   │   ├── MessageDao.java                   ── Database CRUD operations for 'messages' table
│   │   ├── PostDao.java                      ── Database CRUD operations for 'posts' table
│   │   └── UserDao.java                      ── Database CRUD operations for 'users' table
│   │
│   ├── model/                                ── Domain Entities & POJOs
│   │   ├── Comment.java                      ── Post comment entity
│   │   ├── Connection.java                   ── Friend connection relationship entity
│   │   ├── MatchResult.java                  ── Compatibility result (score %, shared tags)
│   │   ├── Message.java                      ── Direct chat message entity
│   │   ├── Post.java                         ── Social feed post entity
│   │   └── User.java                         ── User profile entity
│   │
│   └── util/                                 ── Shared Utilities & Diagnostics
│       ├── DBConnection.java                 ── Centralized JDBC Connection provider
│       └── VerifyDBSchema.java               ── Diagnostic tool to verify MySQL tables
│
└── src/main/webapp/                          ── Presentation Layer (Client SPA)
    ├── index.html                            ── Main Single Page Application interface
    ├── css/
    │   └── style.css                         ── Cosmic glassmorphic dark design system
    ├── js/
    │   └── main.js                           ── Modular client controller (Auth, Match, Feed, Chat)
    └── WEB-INF/
        ├── web.xml                           ── Jakarta EE 5.0 deployment descriptor
        └── lib/
            └── mysql-connector-java-8.0.30.jar── MySQL JDBC Driver
```

---

## 🗄️ Database Setup (MySQL)

1. Open your MySQL client (e.g., MySQL Workbench, phpMyAdmin, or command-line `mysql`).
2. Run the included [`schema.sql`](file:///c:/Users/sohai/eclipse-workspace/Batch436/Orbit/schema.sql) script:
   ```sql
   source schema.sql;
   ```
3. This creates the `Orbit` database and the 5 relational tables:
   - `users`: User profiles, credentials, bio, and comma-separated interests.
   - `connections`: Friend requests and accepted connections.
   - `posts`: Community posts on the MyVibe feed.
   - `comments`: Comments on posts.
   - `messages`: 1-to-1 direct chat messages between friends.

> **Database Credentials**:  
> Configured in [`DBConnection.java`](file:///c:/Users/sohai/eclipse-workspace/Batch436/Orbit/src/main/java/com/orbit/util/DBConnection.java):
> - **URL**: `jdbc:mysql://localhost:3306/Orbit`
> - **Username**: `root`
> - **Password**: `root` (change this in `DBConnection.java` if your local MySQL password differs)

---

## 🚀 How to Run in Eclipse

1. Open **Eclipse IDE for Enterprise Java and Web Developers**.
2. Make sure you have **Apache Tomcat v10.0 or v10.1** configured in Eclipse under the **Servers** tab.
3. Import the `Orbit` project into your Eclipse workspace.
4. Right-click the `Orbit` project &rarr; **Run As** &rarr; **Run on Server**.
5. Select **Apache Tomcat v10.1** and click **Finish**.
6. Access the application in your browser at:
   ```
   http://localhost:8080/Orbit/
   ```

---

## 📡 REST API Endpoints

All endpoints are served by Jakarta Servlets mapped under `/api/*`:

| Endpoint | Method | Action / Param | Description |
|---|---|---|---|
| `/api/auth` | `GET` | `action=me` | Returns current authenticated user session |
| `/api/auth` | `POST` | `action=login` | Authenticates with `email` and `password` |
| `/api/auth` | `POST` | `action=register` | Creates a new user with interests & bio |
| `/api/auth` | `POST` | `action=logout` | Clears current user session |
| `/api/auth` | `POST` | `action=updateProfile` | Updates bio and comma-separated interests |
| `/api/match` | `GET` | — | Computes & returns ranked matches for current user |
| `/api/feed` | `GET` | — | Fetches all community posts and comments |
| `/api/feed` | `POST` | `action=post` | Publishes a new post (`content`, optional `mediaUrl`) |
| `/api/feed` | `POST` | `action=comment` | Submits a comment (`postId`, `commentText`) |
| `/api/circle` | `GET` | `action=friends` | Lists accepted friends in user's circle |
| `/api/circle` | `GET` | `action=requests` | Lists incoming pending connection requests |
| `/api/circle` | `POST` | `action=connect` | Sends a connection request (`targetUserId`) |
| `/api/circle` | `POST` | `action=accept` | Accepts an incoming connection request (`senderId`) |
| `/api/messages` | `GET` | `userId={id}` | Fetches conversation history between two users |
| `/api/messages` | `POST` | — | Sends a direct message (`receiverId`, `messageText`) |

---

## 💡 Key Orbit Matching Logic

Located in [`MatchService.java`](file:///c:/Users/sohai/eclipse-workspace/Batch436/Orbit/src/main/java/com/orbit/service/MatchService.java):
1. Normalizes user interest tags (trims whitespace, case-insensitive comparison).
2. Computes the Jaccard-inspired intersection ratio:
   $$\text{Compatibility \%} = \left(\frac{\text{Shared Interests}}{\max(\text{User A Count}, \text{User B Count})}\right) \times 100$$
3. Attaches connection relationship status (`NONE`, `PENDING_SENT`, `PENDING_RECEIVED`, `ACCEPTED`).
4. Sorts candidates dynamically from highest to lowest compatibility.
