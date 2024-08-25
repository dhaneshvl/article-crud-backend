package com.dw.article_world.another;

import com.dw.article_world.exception.InvalidArticleException;
import com.dw.article_world.model.Article;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ControllerTest {
    @LocalServerPort
    private int port;
    private static RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    private String baseUrl = "http://localhost:%d" + "/api/articles";
    @Autowired
    private ArticleRepositoryTest articleRepositoryTest;

    @BeforeAll
    void init() {
        restTemplate = new RestTemplate();

        Article article1 = Article.builder()
                .title("demo title 1")
                .content("demo content 1")
                .userId(1)
                .build();

        Article article2 = Article.builder()
                .title("demo title 2")
                .content("demo content 2")
                .userId(2)
                .build();

        var preLoadedArticles = List.of(article1, article2);

        articleRepositoryTest.saveAll(preLoadedArticles);

    }

    @AfterAll
    void clean() {
        articleRepositoryTest.deleteAll();
    }

    @BeforeEach
    void setup() {
        baseUrl = String.format(baseUrl, port);
    }

    @Test
    @Order(1)
    void addArticle() throws JsonProcessingException {
        Article article = Article.builder()
                .title("test title 1")
                .content("test content 1")
                .userId(3)
                .build();

        String requestPayload = objectMapper.writeValueAsString(article);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(requestPayload, httpHeaders);

        ResponseEntity<Article> responseEntity = restTemplate.exchange(
                baseUrl, HttpMethod.POST, httpEntity, Article.class
        );

        Article responseBody = Optional.ofNullable(responseEntity.getBody()).orElseThrow();

        assertAll(
                () -> assertNotNull(responseEntity, "Add article's response should not be null"),
                () -> assertNotNull(responseEntity.getBody()),
                () -> assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode()),
                () -> assertEquals(article.getTitle(), responseBody.getTitle()),
                () -> assertEquals(article.getContent(), responseBody.getContent()),
                () -> assertEquals(article.getUserId(), responseBody.getUserId())
        );

    }

    @Order(3)
    @Test
    void getArticles() {
        ResponseEntity<List<Article>> responseEntity = restTemplate.exchange(
                baseUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                }
        );

        List<Article> responseBody = Optional.ofNullable(responseEntity.getBody()).orElseThrow();

        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertFalse(responseBody.isEmpty()),
                () -> assertEquals(articleRepositoryTest.findAll().size(), responseBody.size())
        );

    }

    @Order(2)
    @Test
    void getArticle() {
        var articleToFetch = articleRepositoryTest.findAll().get(0);

        ResponseEntity<Article> responseEntity = restTemplate.exchange(
                baseUrl + "/" + articleToFetch.getId(), HttpMethod.GET, null, Article.class
        );

        Article responseBody = Optional.ofNullable(responseEntity.getBody()).orElseThrow();

        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(articleToFetch, responseBody)
        );

    }

    @Order(4)
    @Test
    void updateArticle() throws Exception {
        var articleToUpdate = articleRepositoryTest.findAll().get(0);
        articleToUpdate.setContent("updated content " + LocalDateTime.now());
        articleToUpdate.setTitle("updated title " + LocalDateTime.now());
        articleToUpdate.setUserId(11);

        String requestPayload = objectMapper.writeValueAsString(articleToUpdate);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> httpEntity = new HttpEntity<>(requestPayload, httpHeaders);

        ResponseEntity<Article> responseEntity = restTemplate.exchange(
                baseUrl + "/" + articleToUpdate.getId(), HttpMethod.PUT, httpEntity, Article.class
        );

        Article responseBody = Optional.ofNullable(responseEntity.getBody()).orElseThrow();

        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals(articleToUpdate.getUserId(), responseBody.getUserId()),
                () -> assertEquals(articleToUpdate.getId(), responseBody.getId()),
                () -> assertEquals(articleToUpdate.getContent(), responseBody.getContent()),
                () -> assertEquals(articleToUpdate.getTitle(), responseBody.getTitle()),
                () -> assertEquals(articleToUpdate.getPostedDate(), responseBody.getPostedDate()),
                () -> assertNotNull(responseBody.getUpdatedDate()),
                () -> assertNotEquals(articleToUpdate.getUpdatedDate(), responseBody.getUpdatedDate())
        );

    }

    @Order(5)
    @Test
    void deleteArticle() {
        var articleToDelete = articleRepositoryTest.findAll().get(0);
        ResponseEntity<Void> responseEntity = restTemplate.exchange(
                baseUrl + "/" + articleToDelete.getId(), HttpMethod.DELETE, null, Void.class
        );

        assertAll(
                () -> assertNotNull(responseEntity),
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertThrows(InvalidArticleException.class,
                        () -> articleRepositoryTest.findById(articleToDelete.getId())
                                .orElseThrow(() -> new InvalidArticleException("Invalid Article ID")))
        );

    }

    @Order(6)
    @Test
    void shouldReturnBadRequestForInvalidArticleId() {
        var invalidArticleId = -1;
        try {
            ResponseEntity<Article> responseEntity = restTemplate.exchange(
                    baseUrl + "/" + invalidArticleId, HttpMethod.GET, null, Article.class
            );
            fail("Expected the HttpClientException, hence test failed");
        } catch (HttpClientErrorException httpClientErrorException) {
            assertAll(
                    () -> assertEquals(HttpStatus.BAD_REQUEST, httpClientErrorException.getStatusCode()),
                    () -> assertEquals("Invalid Article ID", httpClientErrorException.getResponseBodyAsString())
            );
        }

    }


}
