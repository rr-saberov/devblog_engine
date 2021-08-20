package ru.spring.app.engine.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "post_comments")
public class PostComments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "serial")
    private long id;

    private long parentId;

    @Column(nullable = false)
    private long postId;

    @Column(nullable = false)
    private long userId;

    @Column(nullable = false)
    private LocalDateTime time;

    @Column(nullable = false, columnDefinition = "text")
    private String text;


}


