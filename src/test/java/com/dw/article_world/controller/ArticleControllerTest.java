package com.dw.article_world.controller;

import com.dw.article_world.model.Article;
import com.dw.article_world.exception.InvalidArticleException;
import com.dw.article_world.repo.ArticleRepositoryTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArticleControllerTest {
    @LocalServerPort
    private int port;
    private String baseUrl;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ArticleRepositoryTest articleRepositoryTest;
    private static RestTemplate restTemplate;
    private List<Article> articles;

    @BeforeAll
    public void init() {
        restTemplate = new RestTemplate();
        articles = List.of(
                Article.builder().title("Test Article 1").content("Test content for article 1.").userId(1).build(),
                Article.builder().title("Test Article 2").content("Test content for article 2.").userId(1).build()
        );
        articleRepositoryTest.saveAll(articles);
    }

    @AfterAll
    public void clean() {
        articleRepositoryTest.deleteAll();
    }

    @BeforeEach
    public void setup() {
        baseUrl = String.format("http://localhost:%d/api/articles", port);
        articles = articleRepositoryTest.findAll();
    }

    @Test
    void addTestArticle() throws Exception {
        var article = Article.builder()
                .title("Test Article 5")
                .content("Test content for article 5.")
                .userId(3)
                .build();

        String requestData = objectMapper.writeValueAsString(article);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestData, headers);

        ResponseEntity<Article> response = restTemplate.postForEntity(baseUrl, request, Article.class);

        Article articleResponse = Optional.ofNullable(response.getBody()).orElseThrow();

        assertNotNull(articleResponse, "Article response shouldn't be null");
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(article.getTitle(), articleResponse.getTitle());
        assertEquals(article.getContent(), articleResponse.getContent());
        assertEquals(article.getUserId(), articleResponse.getUserId());
    }

    @Test
    void getArticles() {
        
        ResponseEntity<List<Article>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        List<Article> articlesFromResponse = Optional.ofNullable(response.getBody()).orElseThrow();

        assertNotNull(articlesFromResponse);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(articlesFromResponse.isEmpty());
        assertEquals(articles.size(), articlesFromResponse.size());
    }

    @Test
    void getArticle() {
        Article articleToFind = articles.get(0);

        ResponseEntity<Article> response = restTemplate.getForEntity(
                baseUrl + "/" + articleToFind.getId(), Article.class
        );

        Article articleFromResponse = Optional.ofNullable(response.getBody()).orElseThrow();

        assertNotNull(articleFromResponse);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(articleToFind, articleFromResponse);
    }

    @Test
    void getArticleShouldReturnBadRequestForInvalidArticleId() {
        int invalidArticleId = -1;
        try {
            restTemplate.getForEntity(baseUrl + "/" + invalidArticleId, String.class);
            fail("Expected HttpClientErrorException");
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
            assertEquals("Invalid Article ID", ex.getResponseBodyAsString());
        }
    }

    @Test
    void updateArticle() throws JsonProcessingException {

        var articleToBeUpdate = articles.get(0);

        var articleUpdated = Article.builder()
                .title("Test Article Updated at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss a", Locale.US)))
                .content("Test content Updated at " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm:ss a", Locale.US)))
                .userId(33)
                .build();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        String articleUpdatedAsString = objectMapper.writeValueAsString(articleUpdated);

        HttpEntity<String> httpEntity = new HttpEntity<>(articleUpdatedAsString, httpHeaders);

        ResponseEntity<Article> responseEntity = restTemplate.exchange(
                baseUrl + "/" + articleToBeUpdate.getId(), HttpMethod.PUT, httpEntity, Article.class
        );

        Article articleFromResponse = Optional.ofNullable(responseEntity.getBody()).orElseThrow();

        assertAll(
                () -> assertNotNull(articleFromResponse),
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(articleUpdated.getUserId(), articleFromResponse.getUserId()),
                () -> assertEquals(articleUpdated.getTitle(), articleFromResponse.getTitle()),
                () -> assertEquals(articleUpdated.getContent(), articleFromResponse.getContent())
        );

    }

    @Test
    void deleteArticle() {

        var articleToBeDeleted = articles.get(0);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                baseUrl + "/" + articleToBeDeleted.getId(), HttpMethod.DELETE, null, String.class
        );

        var responseFromServer = Optional.ofNullable(responseEntity.getBody()).orElseThrow();

        assertAll(
                () -> assertNotNull(responseFromServer),
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals("Article deleted successfully", responseFromServer),
                () -> assertThrows(InvalidArticleException.class,
                        () -> articleRepositoryTest.findById(articleToBeDeleted.getId())
                                .orElseThrow(() -> new InvalidArticleException("Invalid Article ID")))
        );

    }

}
