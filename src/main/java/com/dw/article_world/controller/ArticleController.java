package com.dw.article_world.controller;

import com.dw.article_world.model.Article;
import com.dw.article_world.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin(origins = "http://localhost:3000")
public class ArticleController {

    @Autowired
    ArticleService articleService;

    @PostMapping
    ResponseEntity<?> addArticle(@RequestBody Article article) {
        Article _article = articleService.addArticle(article);
        if (_article != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(_article);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Oops!, An error occurred.");
        }
    }

    @GetMapping
    ResponseEntity<?> getArticles() {
        System.out.println("getArticles" +System.currentTimeMillis());
        var articles = articleService.getArticles();
        if (articles != null) {
            return ResponseEntity.status(HttpStatus.OK).body(articles);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Oops!, An error occurred.");
        }
    }

    @GetMapping("/{articleId}")
    Article getArticle(@PathVariable Integer articleId) {
        return articleService.getArticle(articleId);
    }

    @PutMapping("/{articleId}")
    Article updateArticle(@RequestBody Article article, @PathVariable Integer articleId) {
        return articleService.updateArticle(article, articleId);
    }

    @DeleteMapping("/{articleId}")
    ResponseEntity<String> deleteArticle(@PathVariable Integer articleId) {
        boolean deleteById = articleService.deleteArticle(articleId);
        if (deleteById) {
            return ResponseEntity.status(HttpStatus.OK).body("Article deleted successfully");
        } else return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No such article exists");
    }

}
