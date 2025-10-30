package com.library.frontend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.library.frontend.model.Book;
import com.library.frontend.service.BookService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookController {

    private static final Logger logger = Logger.getLogger(BookController.class.getName());

    @FXML private TableView<Book> bookTable;
    @FXML private TableColumn<Book, String> colTitle;
    @FXML private TableColumn<Book, String> colAuthor;
    @FXML private TableColumn<Book, String> colIsbn;
    @FXML private TableColumn<Book, LocalDate> colPublishDate;

    @FXML private Label titleErrorLabel;
    @FXML private Label authorErrorLabel;
    @FXML private Label isbnErrorLabel;
    @FXML private Label publishDateErrorLabel;

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField isbnField;
    @FXML private DatePicker publishDateField;
    @FXML private TextField searchField;
    @FXML private Label pageLabel;
    @FXML private Button prevButton;
    @FXML private Button nextButton;

    private final BookService service = new BookService();
    private int currentPage = 0;
    private final int pageSize = 5;
    private int totalPages = 1;

    @FXML
    public void initialize() {
        logger.info("Initializing BookController");

        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAuthor.setCellValueFactory(new PropertyValueFactory<>("author"));
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        colPublishDate.setCellValueFactory(new PropertyValueFactory<>("publishDate"));

        Platform.runLater(this::loadCurrentPage);

        bookTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                titleField.setText(newSel.getTitle());
                authorField.setText(newSel.getAuthor());
                isbnField.setText(newSel.getIsbn());
                publishDateField.setValue(newSel.getPublishDate());
            }
        });
        searchField.textProperty().addListener((obs, oldText, newText) -> handleSearch());
    }

    @FXML
    private void addBook() {
        clearValidationMessages();
        try {
            Book book = new Book();
            book.setTitle(titleField.getText());
            book.setAuthor(authorField.getText());
            book.setIsbn(isbnField.getText());
            book.setPublishDate(publishDateField.getValue());

            service.addBook(book);
            loadCurrentPage();
            showAlert("Book added successfully!");
            clearFields();

        } catch (Exception e) {
            handleValidationException(e);
        }
    }
    @FXML
    private void updateBook() {
        clearValidationMessages();
        try {
            Book selected = bookTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select a book to update!");
                return;
            }

            Book bookToUpdate = new Book();
            bookToUpdate.setBookId(selected.getBookId());
            bookToUpdate.setTitle(titleField.getText());
            bookToUpdate.setAuthor(authorField.getText());
            bookToUpdate.setIsbn(isbnField.getText());
            bookToUpdate.setPublishDate(publishDateField.getValue());

            // Call updated service method that handles validation
            Book updatedBook = service.updateBook(bookToUpdate);

            showAlert("Book updated successfully!");
            clearFields();
            loadCurrentPage();

        } catch (Exception e) {
            handleValidationException(e);
        }
    }


    @FXML
    private void deleteBook() {
        try {
            Book selected = bookTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Select a book to delete!");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to delete this book?",
                    ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();
            if (confirm.getResult() != ButtonType.YES) return;

            service.deleteBook(selected.getBookId());
            loadCurrentPage();
            showAlert("Book deleted successfully!");
            clearFields();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting book", e);
            showAlert("Error deleting book: " + e.getMessage());
        }
    }

    @FXML
    private void refreshTable() {
        try {
            List<Book> books = service.getAllBooks();
            bookTable.setItems(FXCollections.observableArrayList(books));
            bookTable.refresh();
            clearFields();
            pageLabel.setText("All books loaded");
        } catch (Exception e) {
            showAlert("Error refreshing books: " + e.getMessage());
        }
    }

    @FXML
    private void clearFields() {
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        publishDateField.setValue(null);
    }

    @FXML
    private void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadCurrentPage();
        }
    }

    @FXML
    private void prevPage() {
        if (currentPage > 0) {
            currentPage--;
            loadCurrentPage();
        }
    }

    @FXML
    private void handleSearch() {
        currentPage = 0;
        loadCurrentPage();
    }

    private void loadCurrentPage() {
        try {
            String searchText = searchField.getText().trim();
            int totalCount = service.getTotalCount(searchText);
            totalPages = Math.max(1, (int) Math.ceil((double) totalCount / pageSize));

            List<Book> books = service.getBooks(currentPage, pageSize, searchText);
            bookTable.setItems(FXCollections.observableArrayList(books));
            pageLabel.setText("Page " + (currentPage + 1) + " of " + totalPages);

            prevButton.setDisable(currentPage == 0);
            nextButton.setDisable(currentPage >= totalPages - 1);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading books", e);
            showAlert("Error loading books: " + e.getMessage());
        }
    }

    private void clearValidationMessages() {
        titleErrorLabel.setText("");
        authorErrorLabel.setText("");
        isbnErrorLabel.setText("");
        publishDateErrorLabel.setText("");
    }

    private void handleValidationException(Exception e) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> response = mapper.readValue(e.getMessage(),
                    new TypeReference<Map<String,Object>>() {});

            Map<String, String> errors = (Map<String, String>) response.get("errors");
            titleErrorLabel.setText(errors.getOrDefault("title", ""));
            authorErrorLabel.setText(errors.getOrDefault("author", ""));
            isbnErrorLabel.setText(errors.getOrDefault("isbn", ""));
            publishDateErrorLabel.setText(errors.getOrDefault("publishDate", ""));

            String message = (String) response.getOrDefault("message", "Validation failed");
            showAlert(message);

        } catch (Exception ex) {
            showAlert("Error parsing validation response: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
