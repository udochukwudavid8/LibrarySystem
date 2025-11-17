Frontend README – Library Management System

Overview
This is the JavaFX frontend for the Library Management System. It connects to the backend via REST APIs to:
Display books in a table
Add, update, delete books
Search books by keyword
Paginate results

Prerequisites
Java 17 or higher
JavaFX 21+
Maven 3.8+
IDE: IntelliJ IDEA

Setup
1. Clone the repository
2. Configure JavaFX
Make sure JavaFX SDK is installed.
3. Configure Backend URL
In BookService.java:
private static final String BASE_URL = "http://localhost:8085/api/books";
Ensure it matches your backend URL and port.
4. Build and Run
   mvn clean install
   mvn javafx:run
# LibrarySystem
Front end for the management system
