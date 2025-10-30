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

Usage
Validation
Add Book: Fill the fields and click Add.
Update Book: Select a book, edit fields, click Update.
Delete Book: Select a book, click Delete.
Search: Enter keyword,auto search, press Enter or click Search.
Pagination: Use Prev / Next buttons.
Refresh Table: Click Refresh to reload all books.
Backend validates title, author, ISBN, and publish date.
Errors are shown next to respective fields.

Notes
Start the backend first before launching the frontend.
Frontend logs are printed using Logger for debugging.
Ensure backend API URLs and ports match the frontend configuration.