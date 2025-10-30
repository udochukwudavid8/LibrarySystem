package com.library.frontend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.library.frontend.model.Book;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookService {

    private static final Logger logger = Logger.getLogger(BookService.class.getName());
    private static final String BASE_URL = "http://localhost:8085/api/books";
    private final ObjectMapper mapper;
    private final HttpClient client;

    public BookService() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();

        client = HttpClient.newHttpClient();
    }

    // get books
    public List<Book> getBooks(int page, int size, String search) throws Exception {
        String url = BASE_URL + "?page=" + page + "&size=" + size;
        if (search != null && !search.isBlank()) {
            url += "&search=" + URLEncoder.encode(search, StandardCharsets.UTF_8);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        logger.info("Fetching books: " + url);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        logger.info("Response code: " + response.statusCode());

        if (response.statusCode() == 200) {
            Map<String, Object> map = mapper.readValue(response.body(), new TypeReference<>() {});
            String contentJson = mapper.writeValueAsString(map.get("content"));
            return mapper.readValue(contentJson, new TypeReference<List<Book>>() {});
        } else {
            throw new RuntimeException("Failed to fetch books: HTTP " + response.statusCode());
        }
    }

    // Total count
    public int getTotalCount(String searchText) {
        try {
            String url = (searchText == null || searchText.isBlank())
                    ? BASE_URL + "/count"
                    : BASE_URL + "/search/count?query=" + URLEncoder.encode(searchText, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            logger.info("Fetching total count: " + url);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Response code: " + response.statusCode());

            if (response.statusCode() == 200) return Integer.parseInt(response.body());
            else throw new RuntimeException("Failed to get book count: HTTP " + response.statusCode());

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching book count", e);
            throw new RuntimeException("Error fetching book count", e);
        }
    }

    // Fetch all books
    public List<Book> getAllBooks() throws Exception {
        String url = BASE_URL + "/all";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        logger.info("Fetching all books: " + url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        logger.info("Response code: " + response.statusCode());

        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), new TypeReference<List<Book>>() {});
        } else {
            throw new RuntimeException("Failed to fetch all books: HTTP " + response.statusCode());
        }
    }

    // Add book
    public Book addBook(Book book) throws Exception {
        String json = mapper.writeValueAsString(book);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        logger.info("Adding book: " + json);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        logger.info("Response code: " + response.statusCode());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            return mapper.readValue(response.body(), Book.class);
        } else if (response.statusCode() == 400) {
            throw new RuntimeException(response.body()); // validation errors
        } else {
            throw new RuntimeException("Failed to add book: HTTP " + response.statusCode());
        }
    }

    // Update book with validation handling
    public Book updateBook(Book book) throws Exception {
        if (book.getBookId() == null) throw new IllegalArgumentException("Book ID cannot be null");

        String json = mapper.writeValueAsString(book);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + book.getBookId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        logger.info("Updating book ID " + book.getBookId() + ": " + json);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        logger.info("Response code: " + response.statusCode());

        if (response.statusCode() == 200) {
            return mapper.readValue(response.body(), Book.class);
        } else if (response.statusCode() == 400) {
            // Pass validation errors as runtime exception (controller handles it)
            throw new RuntimeException(response.body());
        } else {
            throw new RuntimeException("Failed to update book: HTTP " + response.statusCode());
        }
    }


    // Delete book
    public void deleteBook(Long id) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .DELETE()
                .build();

        logger.info("Deleting book ID: " + id);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        logger.info("Response code: " + response.statusCode());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to delete book: HTTP " + response.statusCode());
        }
    }
}
