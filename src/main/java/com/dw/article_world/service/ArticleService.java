package com.dw.article_world.service;

import com.dw.article_world.model.Article;
import com.dw.article_world.exception.InvalidArticleException;
import com.dw.article_world.repo.ArticleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class ArticleService {
    @Autowired
    ArticleRepository articleRepository;

    public Article addArticle(Article article) {
        try {
            return articleRepository.save(article);
        } catch (Exception e) {
            log.error("Exception occurred while adding article: {}", e.getMessage(), e);
            return null;
        }
    }

    public List<Article> getArticles() {
        try {
            return articleRepository.findAll()
                    .stream()
                    .sorted(Comparator.comparing(Article::getPostedDate).reversed())
                    .toList();
        } catch (Exception e) {
            log.error("Exception occurred while fetching all articles: {}", e.getMessage(), e);
            return null;
        }
    }

    public Article getArticle(Integer articleId) {
        return articleRepository
                .findById(articleId)
                .orElseThrow(() -> new InvalidArticleException("Invalid Article ID"));
    }

    public Article updateArticle(Article article, Integer articleId) {

        Article existingArticle = articleRepository
                .findById(articleId)
                .orElseThrow(() -> new InvalidArticleException("Invalid Article ID"));

        existingArticle.setUserId(article.getUserId());
        existingArticle.setTitle(article.getTitle());
        existingArticle.setContent(article.getContent());

        return articleRepository.save(existingArticle);

    }

    public boolean deleteArticle(Integer articleId) {
        boolean exists = articleRepository.existsById(articleId);
        if (exists) {
            articleRepository.deleteById(articleId);
            return true;
        } else {
            return false;
        }
    }

}
