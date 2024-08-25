package com.dw.article_world.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "ARTICLE")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Article implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false, length = 120, unique = true)
    private String title;

    @Column(nullable = false, length = 999)
    private String content;

    @Column(nullable = false)
    private Integer userId;

    @Column(updatable = false)
    private LocalDateTime postedDate;

    @Column(insertable = false)
    private LocalDateTime updatedDate;

    @PrePersist
    private void onPrePersist() {
        this.setPostedDate(LocalDateTime.now());
    }

    @PreUpdate
    private void onPreUpdate() {
        this.setUpdatedDate(LocalDateTime.now());
    }

}
