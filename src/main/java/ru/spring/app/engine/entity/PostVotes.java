package ru.spring.app.engine.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "post_votes")
@NoArgsConstructor
public class PostVotes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "serial")
    private long id;

    @Column(nullable = false)
    private long userId;

    @Column(nullable = false)
    private long postId;

    @Column(nullable = false)
    private LocalDate time;

    @Column(nullable = false)
    private int value;

    public PostVotes(long userId, long postId, LocalDate time, int value) {
        this.userId = userId;
        this.postId = postId;
        this.time = time;
        this.value = value;
    }
}
