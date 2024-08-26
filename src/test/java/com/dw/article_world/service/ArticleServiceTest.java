package com.dw.article_world.service;

import com.dw.article_world.exception.InvalidArticleException;
import com.dw.article_world.model.Article;
import com.dw.article_world.repo.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleService articleService;

    @Test
    void addArticleSuccess() {
        Article article = Article.builder()
                .title("title")
                .userId(1)
                .content("content")
                .build();

        when(articleRepository.save(article)).thenReturn(article);

        Article result = articleService.addArticle(article);

        assertNotNull(result);
        assertEquals(article.getTitle(), result.getTitle());
        verify(articleRepository, times(1)).save(article);
    }


    @Test
    void addArticleException() {
        Article article = Article.builder()
                .title("title")
                .userId(1)
                .content("content")
                .build();

        when(articleRepository.save(article)).thenThrow(new RuntimeException("Database error"));

        Article result = articleService.addArticle(article);

        assertNull(result);
        verify(articleRepository, times(1)).save(article);
        // Optionally verify logging
        // verify(log, times(1)).error("Exception occurred while adding article: Database error", any(RuntimeException.class));
    }


    @Test
    void getAllArticles() {
        Article article1 = Article.builder()
                .title("title1")
                .userId(1)
                .content("content1")
                .postedDate(LocalDateTime.now())
                .build();

        Article article2 = Article.builder()
                .title("title2")
                .userId(1)
                .content("content2")
                .postedDate(LocalDateTime.now())
                .build();

        List<Article> articles = List.of(article1, article2);

        when(articleRepository.findAll()).thenReturn(articles);

        List<Article> articlesFromService = articleService.getArticles();

        assertEquals(articles.size(), articlesFromService.size());
        assertEquals(article2.getTitle(), articlesFromService.get(0).getTitle()); // Check sorting
        assertEquals(article1.getTitle(), articlesFromService.get(1).getTitle()); // Check sorting

        verify(articleRepository, times(1)).findAll();
    }

    @Test
    void getAllArticlesWhenExceptionThrown() {
        when(articleRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        List<Article> articlesFromService = articleService.getArticles();

        assertNull(articlesFromService);

        verify(articleRepository, times(1)).findAll();
    }

    @Test
    void updateArticleSuccess() {
        Integer articleId = 1;

        Article existingArticle = Article.builder()
                .id(articleId)
                .title("Old Title")
                .userId(1)
                .content("Old Content")
                .build();

        Article updatedArticle = Article.builder()
                .userId(2)
                .title("New Title")
                .content("New Content")
                .build();

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(existingArticle));
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Article result = articleService.updateArticle(updatedArticle, articleId);

        assertEquals("New Title", result.getTitle());
        assertEquals("New Content", result.getContent());
        assertEquals(2, result.getUserId());

        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, times(1)).save(existingArticle);
    }

    @Test
    void updateArticleInvalidId() {
        Integer articleId = 1;

        Article updatedArticle = Article.builder()
                .userId(2)
                .title("New Title")
                .content("New Content")
                .build();

        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        assertThrows(InvalidArticleException.class, () -> {
            articleService.updateArticle(updatedArticle, articleId);
        });

        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, never()).save(any(Article.class)); // Save should not be called
    }

    @Test
    void deleteArticleSuccess() {
        Integer articleId = 1;

        when(articleRepository.existsById(articleId)).thenReturn(true);

        boolean result = articleService.deleteArticle(articleId);

        assertTrue(result);
        verify(articleRepository, times(1)).existsById(articleId);
        verify(articleRepository, times(1)).deleteById(articleId);
    }


    @Test
    void deleteArticleNotFound() {
        Integer articleId = 1;

        when(articleRepository.existsById(articleId)).thenReturn(false);
        boolean result = articleService.deleteArticle(articleId);

        assertFalse(result);
        verify(articleRepository, times(1)).existsById(articleId);
        verify(articleRepository, never()).deleteById(articleId);
    }

    @Test
    void getArticleSuccess() {
        Integer articleId = 1;
        Article article = Article.builder()
                .id(articleId)
                .title("title")
                .userId(1)
                .content("content")
                .build();

        // Set up the repository mock
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));

        // Call the method
        Article result = articleService.getArticle(articleId);

        // Verify the results
        assertNotNull(result);
        assertEquals(articleId, result.getId());
        verify(articleRepository, times(1)).findById(articleId);
    }

    @Test
    void getArticleNotFound() {
        Integer articleId = 1;

        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        InvalidArticleException thrown = assertThrows(
                InvalidArticleException.class,
                () -> articleService.getArticle(articleId),
                "Expected getArticle() to throw, but it didn't"
        );

        assertEquals("Invalid Article ID", thrown.getMessage());
        verify(articleRepository, times(1)).findById(articleId);
    }

}
