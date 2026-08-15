package com.librarymanagement.integration;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class OpenLibraryClient {
    private static final Logger logger = LoggerFactory.getLogger(OpenLibraryClient.class);
    private static final String BASE_URL = "https://openlibrary.org";
    private static final String COVER_URL = "https://covers.openlibrary.org/b/isbn";

    private final CloseableHttpClient httpClient;
    private final Gson gson;
    private final Random random;

    public OpenLibraryClient() {
        this.httpClient = HttpClients.createDefault();
        this.gson = new Gson();
        this.random = new Random();
    }

    /**
     * Импорт случайных книг по жанру
     */
    public List<BookMetadata> importRandomByGenre(String genre, int limit) throws IOException {
        String subject = mapGenreToSubject(genre);
        String url = BASE_URL + "/subjects/" + URLEncoder.encode(subject, StandardCharsets.UTF_8)
                + ".json?limit=" + Math.min(limit * 2, 100);

        String response = executeGet(url);
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

        List<BookMetadata> allBooks = new ArrayList<>();
        JsonArray works = jsonObject.getAsJsonArray("works");

        for (int i = 0; i < works.size(); i++) {
            JsonObject work = works.get(i).getAsJsonObject();

            BookMetadata metadata = new BookMetadata();
            metadata.setTitle(work.get("title").getAsString());

            // Авторы
            JsonArray authors = work.getAsJsonArray("authors");
            if (authors != null && authors.size() > 0) {
                metadata.setAuthor(authors.get(0).getAsJsonObject().get("name").getAsString());
            }

            metadata.setGenre(genre);
            allBooks.add(metadata);
        }

        // Перемешиваем и берем случайные
        Collections.shuffle(allBooks);
        return allBooks.subList(0, Math.min(limit, allBooks.size()));
    }

    /**
     * Поиск книги по ISBN
     */
    public BookMetadata findByIsbn(String isbn) throws IOException {
        String url = BASE_URL + "/isbn/" + isbn + ".json";

        String response = executeGet(url);
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

        BookMetadata metadata = new BookMetadata();
        metadata.setTitle(jsonObject.get("title").getAsString());
        metadata.setIsbn(isbn);

        // Авторы
        if (jsonObject.has("authors")) {
            JsonArray authors = jsonObject.getAsJsonArray("authors");
            if (authors.size() > 0) {
                String authorKey = authors.get(0).getAsJsonObject().get("key").getAsString();
                metadata.setAuthor(getAuthorName(authorKey));
            }
        }

        // Описание
        if (jsonObject.has("description")) {
            if (jsonObject.get("description").isJsonObject()) {
                metadata.setDescription(jsonObject.get("description")
                        .getAsJsonObject().get("value").getAsString());
            } else {
                metadata.setDescription(jsonObject.get("description").getAsString());
            }
        }

        // Обложка
        metadata.setCoverUrl(COVER_URL + "/" + isbn + "-L.jpg");

        return metadata;
    }

    /**
     * Поиск книг по названию
     */
    public List<BookMetadata> searchByTitle(String title, int limit) throws IOException {
        String url = BASE_URL + "/search.json?title="
                + URLEncoder.encode(title, StandardCharsets.UTF_8)
                + "&limit=" + limit;

        String response = executeGet(url);
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

        List<BookMetadata> books = new ArrayList<>();
        JsonArray docs = jsonObject.getAsJsonArray("docs");

        for (int i = 0; i < docs.size(); i++) {
            JsonObject doc = docs.get(i).getAsJsonObject();

            BookMetadata metadata = new BookMetadata();
            metadata.setTitle(doc.get("title").getAsString());

            if (doc.has("author_name")) {
                metadata.setAuthor(doc.getAsJsonArray("author_name").get(0).getAsString());
            }

            if (doc.has("isbn")) {
                metadata.setIsbn(doc.getAsJsonArray("isbn").get(0).getAsString());
            }

            books.add(metadata);
        }

        return books;
    }

    /**
     * Получение имени автора по ключу
     */
    private String getAuthorName(String authorKey) throws IOException {
        String url = BASE_URL + authorKey + ".json";
        String response = executeGet(url);
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        return jsonObject.get("name").getAsString();
    }

    /**
     * Выполнение GET запроса
     */
    private String executeGet(String url) throws IOException {
        HttpGet request = new HttpGet(url);
        request.setHeader("User-Agent", "LibraryManagement/1.0");

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new IOException("Failed to fetch data: " + response.getStatusLine());
            }
            return EntityUtils.toString(response.getEntity());
        }
    }

    /**
     * Маппинг жанров на subjects OpenLibrary
     */
    private String mapGenreToSubject(String genre) {
        if (genre == null) return "fiction";

        return switch (genre.toLowerCase()) {
            case "фантастика", "science fiction" -> "science_fiction";
            case "детектив", "mystery" -> "mystery";
            case "роман", "romance" -> "romance";
            case "научная литература", "science" -> "science";
            case "приключения", "adventure" -> "adventure";
            case "поэзия", "poetry" -> "poetry";
            case "фэнтези", "fantasy" -> "fantasy";
            case "история", "history" -> "history";
            default -> genre.toLowerCase().replace(" ", "_");
        };
    }

    public void close() throws IOException {
        httpClient.close();
    }
}