package com.dw.article_world.repo;

import com.dw.article_world.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article,Integer> {
}
